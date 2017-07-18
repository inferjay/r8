#!/usr/bin/env python
# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

from __future__ import print_function
from glob import glob
import optparse
import os
import sys
import time

import d8
import gmail_data
import gmscore_data
import r8
import utils
import youtube_data

TYPES = ['dex', 'deploy', 'proguarded']
APPS = ['gmscore', 'youtube', 'gmail']

def ParseOptions():
  result = optparse.OptionParser()
  result.add_option('--compiler',
                    help='',
                    default='r8',
                    choices=['d8', 'r8'])
  result.add_option('--app',
                    help='',
                    default='gmscore',
                    choices=APPS)
  result.add_option('--type',
                    help='Default for R8: deploy, for D8: proguarded',
                    choices=TYPES)
  result.add_option('--out',
                    help='',
                    default=os.getcwd())
  result.add_option('--no-build',
                    help='',
                    default=False,
                    action='store_true')
  result.add_option('--no-libraries',
                    help='',
                    default=False,
                    action='store_true')
  result.add_option('--no-debug',
                    help='Run without debug asserts.',
                    default=False,
                    action='store_true')
  result.add_option('--version',
                    help='')
  result.add_option('-k',
                    help='Override the default ProGuard keep rules')
  result.add_option('--compiler-flags',
                    help='Additional option(s) for the compiler. ' +
                         'If passing several options use a quoted string.')
  result.add_option('--r8-flags',
                    help='Additional option(s) for the compiler. ' +
                         'Same as --compiler-flags, keeping it for backward'
                         ' compatibility. ' +
                         'If passing several options use a quoted string.')
  # TODO(tamaskenez) remove track-memory-to-file as soon as we updated golem
  # to use --print-memoryuse instead
  result.add_option('--track-memory-to-file',
                    help='Track how much memory the jvm is using while ' +
                    ' compiling. Output to the specified file.')
  result.add_option('--profile',
                    help='Profile R8 run.',
                    default=False,
                    action='store_true')
  result.add_option('--dump-args-file',
                    help='Dump a file with the arguments for the specified ' +
                    'configuration. For use as a @<file> argument to perform ' +
                    'the run.')
  result.add_option('--print-runtimeraw',
                    metavar='BENCHMARKNAME',
                    help='Print the line \'<BENCHMARKNAME>(RunTimeRaw):' +
                        ' <elapsed> ms\' at the end where <elapsed> is' +
                        ' the elapsed time in milliseconds.')
  result.add_option('--print-memoryuse',
                    metavar='BENCHMARKNAME',
                    help='Print the line \'<BENCHMARKNAME>(MemoryUse):' +
                        ' <mem>\' at the end where <mem> is the peak' +
                        ' peak resident set size (VmHWM) in bytes.')
  result.add_option('--print-dexsegments',
                    metavar='BENCHMARKNAME',
                    help='Print the sizes of individual dex segments as ' +
                        '\'<BENCHMARKNAME>-<segment>(CodeSize): <bytes>\'')
  return result.parse_args()

# Most apps have the -printmapping and -printseeds in the Proguard
# configuration. However we don't want to write these files in these
# locations. Instead generate an auxiliary Proguard configuration
# placing these two output files together with the dex output.
def GenerateAdditionalProguardConfiguration(temp, outdir):
  name = "output.config"
  with open(os.path.join(temp, name), 'w') as f:
    f.write('-printmapping ' + os.path.join(outdir, 'proguard.map') + "\n")
    f.write('-printseeds ' + os.path.join(outdir, 'proguard.seeds') + "\n")
    return os.path.abspath(f.name)

def main():
  app_provided_pg_conf = False;
  (options, args) = ParseOptions()
  outdir = options.out
  data = None
  if options.app == 'gmscore':
    options.version = options.version or 'v9'
    data = gmscore_data
  elif options.app == 'youtube':
    options.version = options.version or '12.22'
    data = youtube_data
  elif options.app == 'gmail':
    options.version = options.version or '170604.16'
    data = gmail_data
  else:
    raise 'Unexpected'

  if not options.version in data.VERSIONS.keys():
    print('No version {} for application {}'
        .format(options.version, options.app))
    print('Valid versions are {}'.format(data.VERSIONS.keys()))
    return 1

  version = data.VERSIONS[options.version]

  if not options.type:
    options.type = 'deploy' if options.compiler == 'r8' \
        else 'proguarded'

  if options.type not in version:
    print('No type {} for version {}'.format(options.type, options.version))
    print('Valid types are {}'.format(version.keys()))
    return 1
  values = version[options.type]
  inputs = None
  # For R8 'deploy' the JAR is located using the Proguard configuration
  # -injars option.
  if 'inputs' in values and (options.compiler != 'r8'
      or options.type != 'deploy'):
    inputs = values['inputs']

  args.extend(['--output', outdir])
  if 'min-api' in values:
    args.extend(['--min-api', values['min-api']])

  if options.compiler == 'r8':
    if 'pgmap' in values:
      args.extend(['--pg-map', values['pgmap']])
    if 'pgconf' in values and not options.k:
      for pgconf in values['pgconf']:
        args.extend(['--pg-conf', pgconf])
        app_provided_pg_conf = True
    if options.k:
      args.extend(['--pg-conf', options.k])
    if 'multidexrules' in values:
      for rules in values['multidexrules']:
        args.extend(['--multidex-rules', rules])

  if not options.no_libraries and 'libraries' in values:
    for lib in values['libraries']:
      args.extend(['--lib', lib])

  if not outdir.endswith('.zip') and not outdir.endswith('.jar') \
      and not os.path.exists(outdir):
    os.makedirs(outdir)

  if options.compiler == 'r8':
    if 'r8-flags' in values:
      args.extend(values['r8-flags'].split(' '))

  if options.compiler_flags:
    args.extend(options.compiler_flags.split(' '))
  if options.r8_flags:
    args.extend(options.r8_flags.split(' '))

  if inputs:
    args.extend(inputs)

  t0 = time.time()
  if options.dump_args_file:
    with open(options.dump_args_file, 'w') as args_file:
      args_file.writelines([arg + os.linesep for arg in args])
  else:
    with utils.TempDir() as temp:
      if options.print_memoryuse and not options.track_memory_to_file:
        options.track_memory_to_file = os.path.join(temp,
            utils.MEMORY_USE_TMP_FILE)
      if options.compiler == 'd8':
        d8.run(args, not options.no_build, not options.no_debug,
            options.profile, options.track_memory_to_file)
      else:
        if app_provided_pg_conf:
          # Ensure that output of -printmapping and -printseeds go to the output
          # location and not where the app Proguard configuration places them.
          if outdir.endswith('.zip') or outdir.endswith('.jar'):
            pg_outdir = os.path.dirname(outdir)
          else:
            pg_outdir = outdir
          additional_pg_conf = GenerateAdditionalProguardConfiguration(
              temp, os.path.abspath(pg_outdir))
          args.extend(['--pg-conf', additional_pg_conf])
        r8.run(args, not options.no_build, not options.no_debug,
            options.profile, options.track_memory_to_file)
      if options.print_memoryuse:
        print('{}(MemoryUse): {}'
            .format(options.print_memoryuse,
                utils.grep_memoryuse(options.track_memory_to_file)))

  if options.print_runtimeraw:
    print('{}(RunTimeRaw): {} ms'
        .format(options.print_runtimeraw, 1000.0 * (time.time() - t0)))

  if options.print_dexsegments:
    dex_files = glob(os.path.join(outdir, '*.dex'))
    utils.print_dexsegments(options.print_dexsegments, dex_files)

if __name__ == '__main__':
  sys.exit(main())
