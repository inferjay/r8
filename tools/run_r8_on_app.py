#!/usr/bin/env python
# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

import optparse
import os
import r8
import sys

import gmscore_data
import youtube_data

TYPES = ['dex', 'deploy', 'proguarded']
APPS = ['gmscore', 'youtube']

def ParseOptions():
  result = optparse.OptionParser()
  result.add_option('--app',
                    help='',
                    default='gmscore',
                    choices=APPS)
  result.add_option('--type',
                    help='',
                    default='deploy',
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
  result.add_option('--r8-flags',
                    help='Additional option(s) for R8. ' +
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
  return result.parse_args()

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
  else:
    raise 'Unexpected'

  if not options.version in data.VERSIONS.keys():
    print 'No version %s for application %s' % (options.version, options.app)
    print 'Valid versions are %s' % data.VERSIONS.keys()
    return 1

  version = data.VERSIONS[options.version]

  if options.type not in version:
    print 'No type %s for version %s' % (options.type, options.version)
    print 'Valid types are %s' % version.keys()
    return 1
  values = version[options.type]
  inputs = None
  # For 'deploy' the JAR is located using the Proguard configuration -injars option.
  if 'inputs' in values and options.type != 'deploy':
    inputs = values['inputs']

  args.extend(['--output', outdir])
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

  if 'r8-flags' in values:
    args.extend(values['r8-flags'].split(' '))
  if options.r8_flags:
    args.extend(options.r8_flags.split(' '))

  if inputs:
    args.extend(inputs)

  if options.dump_args_file:
    with open(options.dump_args_file, 'w') as args_file:
      args_file.writelines([arg + os.linesep for arg in args])
  else:
    r8.run(args, not options.no_build, not options.no_debug, options.profile,
           options.track_memory_to_file)

if __name__ == '__main__':
  sys.exit(main())
