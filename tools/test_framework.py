#!/usr/bin/env python
# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

# Run D8, DX or Goyt on 'third_party/framework/framework_<version>.jar'.
# Report Golem-compatible CodeSize and RunTimeRaw values:
#
#     <NAME>-Total(CodeSize): <size>
#     <NAME>-Total(RunTimeRaw>: <time> ms
#
# and also detailed segment sizes for each dex segment:
#
#    <NAME>-Code(CodeSize): <size>
#    <NAME>-AnnotationSets(CodeSize): <size>
#    ...
#
# Uses the DexSegment Java tool (Gradle target).

from __future__ import print_function
from glob import glob
import argparse
import os
import re
import subprocess
import sys
import time

import utils

DX_JAR = os.path.join(utils.REPO_ROOT, 'tools', 'linux', 'dx', 'framework',
    'dx.jar')
D8_JAR = os.path.join(utils.REPO_ROOT, 'build', 'libs', 'd8.jar')
GOYT_EXE = os.path.join('third_party', 'goyt',
    'goyt_160525751')
FRAMEWORK_JAR = os.path.join('third_party', 'framework',
    'framework_160115954.jar')
MIN_SDK_VERSION = '24'

def parse_arguments():
  parser = argparse.ArgumentParser(
      description = 'Run D8, DX or Goyt on'
          ' third_party/framework/framework*.jar.'
          ' Report Golem-compatible CodeSize and RunTimeRaw values.')
  parser.add_argument('--tool',
      choices = ['dx', 'd8', 'd8-release', 'goyt'],
      required = True,
      help = 'Compiler tool to use.')
  parser.add_argument('--name',
      required = True,
      help = 'Results will be printed using the specified benchmark name (e.g.'
          ' <NAME>-<segment>(CodeSize): <bytes>)')
  parser.add_argument('--print-memoryuse',
      help = 'Prints the line \'<NAME>(MemoryUse):' +
             ' <mem>\' at the end where <mem> is the peak' +
             ' peak resident set size (VmHWM) in bytes.',
      default = False,
      action = 'store_true')
  return parser.parse_args()

def Main():
  args = parse_arguments()

  with utils.TempDir() as temp_dir:

    if args.tool in ['dx', 'goyt']:
      tool_args = ['--dex', '--output=' + temp_dir, '--multi-dex',
          '--min-sdk-version=' + MIN_SDK_VERSION]

    if args.tool == 'goyt':
      tool_file = GOYT_EXE
      tool_args = ['--num-threads=4'] + tool_args
    elif args.tool == 'dx':
      tool_file = DX_JAR
    else:
      tool_file = D8_JAR
      tool_args = ['--output', temp_dir, '--min-api', MIN_SDK_VERSION]
      if args.tool == 'd8-release':
        tool_args.append('--release')


    cmd = []

    track_memory_file = None
    if args.print_memoryuse:
      track_memory_file = os.path.join(temp_dir, utils.MEMORY_USE_TMP_FILE)
      cmd.extend(['tools/track_memory.sh', track_memory_file])

    if tool_file.endswith('.jar'):
      cmd.extend(['java', '-jar'])

    cmd.extend([tool_file] + tool_args + [FRAMEWORK_JAR])

    utils.PrintCmd(cmd)

    t0 = time.time()
    subprocess.check_call(cmd)
    dt = time.time() - t0

    if args.print_memoryuse:
      print('{}(MemoryUse): {}'
          .format(args.name, utils.grep_memoryuse(track_memory_file)))

    dex_files = [f for f in glob(os.path.join(temp_dir, '*.dex'))]
    code_size = 0
    for dex_file in dex_files:
      code_size += os.path.getsize(dex_file)

    print('{}-Total(RunTimeRaw): {} ms'
      .format(args.name, 1000.0 * dt))

    print('{}-Total(CodeSize): {}'
      .format(args.name, code_size))

    utils.print_dexsegments(args.name, dex_files)

if __name__ == '__main__':
  sys.exit(Main())
