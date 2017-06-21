#!/usr/bin/env python
# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

import optparse
import os
import r8
import d8
import sys
import utils
import time

import gmscore_data
import youtube_data
import gmail_data

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
                         'Same as --compiler-flags, keeping it for backward compatibility. ' +
                         'If passing several options use a quoted string.')
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
                    help='Prints the line \'<BENCHMARKNAME>(RunTimeRaw):' +
                         ' <elapsed> ms\' at the end where <elapsed> is' +
                         ' the elapsed time in milliseconds.')
  return result.parse_args()

# Most apps have the -printmapping and -printseeds in the Proguard
# configuration. However we don't want to write these files in these
# locations. Instead generate an auxiliary Proguard configuration
# placing these two output files together with the dex output.
def GenerateAdditionalProguardConfiguration(temp, outdir):
  name = "output.config"
  with open(os.path.join(temp, name), 'w') as file:
    file.write('-printmapping ' + os.path.join(outdir, 'proguard.map') + "\n")
    file.write('-printseeds ' + os.path.join(outdir, 'proguard.seeds') + "\n")
    return os.path.abspath(file.name)

def main():
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
    print 'No version %s for application %s' % (options.version, options.app)
    print 'Valid versions are %s' % data.VERSIONS.keys()
    return 1

  version = data.VERSIONS[options.version]

  if not options.type:
    options.type = 'deploy' if options.compiler == 'r8' \
        else 'proguarded'

  if options.type not in version:
    print 'No type %s for version %s' % (options.type, options.version)
    print 'Valid types are %s' % version.keys()
    return 1
  values = version[options.type]
  inputs = None
  # For R8 'deploy' the JAR is located using the Proguard configuration -injars option.
  if 'inputs' in values and (options.compiler != 'r8' or options.type != 'deploy'):
    inputs = values['inputs']

  args.extend(['--output', outdir])

  if options.compiler == 'r8':
    if 'pgmap' in values:
      args.extend(['--pg-map', values['pgmap']])
    if 'pgconf' in values and not options.k:
      for pgconf in values['pgconf']:
        args.extend(['--pg-conf', pgconf])
    if options.k:
      args.extend(['--pg-conf', options.k])

  if not options.no_libraries and 'libraries' in values:
    for lib in values['libraries']:
      args.extend(['--lib', lib])

  if not outdir.endswith('.zip') and not outdir.endswith('.jar') and not os.path.exists(outdir):
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
    if options.compiler == 'd8':
      d8.run(args, not options.no_build, not options.no_debug, options.profile,
             options.track_memory_to_file)
    else:
      with utils.TempDir() as temp:
        if outdir.endswith('.zip') or outdir.endswith('.jar'):
          pg_outdir = os.path.dirname(outdir)
        else:
          pg_outdir = outdir
        additional_pg_conf = GenerateAdditionalProguardConfiguration(
            temp, os.path.abspath(pg_outdir))
        args.extend(['--pg-conf', additional_pg_conf])
        r8.run(args, not options.no_build, not options.no_debug, options.profile,
               options.track_memory_to_file)

  if options.print_runtimeraw:
    print('{}(RunTimeRaw): {} ms'
        .format(options.print_runtimeraw, 1000.0 * (time.time() - t0)))

if __name__ == '__main__':
  sys.exit(main())
