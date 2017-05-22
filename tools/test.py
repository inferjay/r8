#!/usr/bin/env python
# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

# Convenience script for running tests. If no argument is given run all tests,
# if an argument is given, run only tests with that pattern. This script will
# force the tests to run, even if no input changed.

import gradle
import optparse
import sys

ALL_ART_VMS = ["default", "7.0.0", "6.0.1", "5.1.1"]

def ParseOptions():
  result = optparse.OptionParser()
  result.add_option('--no_internal',
      help='Do not run Google internal tests.',
      default=False, action='store_true')
  result.add_option('--only_internal',
      help='Only run Google internal tests.',
      default=False, action='store_true')
  result.add_option('--all_tests',
      help='Run tests in all configurations.',
      default=False, action='store_true')
  result.add_option('-v', '--verbose',
      help='Print test stdout to, well, stdout.',
      default=False, action='store_true')
  result.add_option('--dex_vm',
      help='The android version of the vm to use. "all" will run the tests on '
           'all art vm versions (stopping after first failed execution)',
      default="default",
      choices=ALL_ART_VMS + ["all"])
  result.add_option('--one_line_per_test',
      help='Print a line before a tests starts and after it ends to stdout.',
      default=False, action='store_true')
  result.add_option('--tool',
      help='Tool to run ART tests with: "r8" (default) or "d8". Ignored if "--all_tests" enabled.',
      default=None, choices=["r8", "d8"])
  result.add_option('--jctf',
      help='Run JCTF tests with: "r8" (default) or "d8".',
      default=False, action='store_true')
  result.add_option('--only_jctf',
      help='Run only JCTF tests with: "r8" (default) or "d8".',
      default=False, action='store_true')
  result.add_option('--jctf_compile_only',
      help="Don't run, only compile JCTF tests.",
      default=False, action='store_true')

  return result.parse_args()

def Main():
  (options, args) = ParseOptions()
  gradle_args = ['cleanTest', 'test']
  if len(args) > 1:
    print("test.py takes at most one argument, the pattern for tests to run")
    return -1
  if options.verbose:
    gradle_args.append('-Pprint_test_stdout')
  if options.no_internal:
    gradle_args.append('-Pno_internal')
  if options.only_internal:
    gradle_args.append('-Ponly_internal')
  if options.all_tests:
    gradle_args.append('-Pall_tests')
  if options.tool:
    gradle_args.append('-Ptool=%s' % options.tool)
  if options.one_line_per_test:
    gradle_args.append('-Pone_line_per_test')
  if options.jctf:
    gradle_args.append('-Pjctf')
  if options.only_jctf:
    gradle_args.append('-Ponly_jctf')
  if options.jctf_compile_only:
    gradle_args.append('-Pjctf_compile_only')
  if len(args) > 0:
    gradle_args.append('--tests')
    gradle_args.append(args[0])

  vms_to_test = [options.dex_vm] if options.dex_vm != "all" else ALL_ART_VMS
  for art_vm in vms_to_test:
    gradle.RunGradle(gradle_args + ['-Pdex_vm=%s' % art_vm])

if __name__ == '__main__':
  sys.exit(Main())


