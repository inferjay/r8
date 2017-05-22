#!/usr/bin/env python
# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

import d8
import gmscore_data
import optparse
import os
import sys

def ParseOptions():
  result = optparse.OptionParser()
  result.add_option('--out',
                    help = '',
                    default = os.getcwd())
  result.add_option('--no-build',
                    help = '',
                    default = False,
                    action = 'store_true')
  result.add_option('--no-debug',
                    help = 'Run without debug asserts.',
                    default = False,
                    action = 'store_true')
  result.add_option('--version',
                    help = '',
                    default = 'v9',
                    choices = ['v9', 'v10'])
  result.add_option('--type',
                    help = '',
                    default = 'proguarded',
                    choices = ['proguarded', 'deploy'])
  result.add_option('--d8-flags',
                    help = 'Additional option(s) for D8. ' +
                         'If passing several options use a quoted string.')
  result.add_option('--track-memory-to-file',
                    help = 'Track how much memory the jvm is using while ' +
                    ' compiling. Output to the specified file.')
  result.add_option('--profile',
                    help = 'Profile D8 run.',
                    default = False,
                    action = 'store_true')
  result.add_option('--dump-args-file',
                    help = 'Dump a file with the arguments for the specified ' +
                    'configuration. For use as a @<file> argument to perform ' +
                    'the run.')
  return result.parse_args()

def main():
  (options, args) = ParseOptions()
  outdir = options.out
  version = gmscore_data.VERSIONS[options.version]
  values = version[options.type]
  inputs = values['inputs']

  args.extend(['--output', outdir])

  if not os.path.exists(outdir):
    os.makedirs(outdir)

  if options.d8_flags:
    args.extend(options.d8_flags.split(' '))

  args.extend(inputs)

  if options.dump_args_file:
    with open(options.dump_args_file, 'w') as args_file:
      args_file.writelines([arg + os.linesep for arg in args])
  else:
    d8.run(args, not options.no_build, not options.no_debug, options.profile,
           options.track_memory_to_file)

if __name__ == '__main__':
  sys.exit(main())
