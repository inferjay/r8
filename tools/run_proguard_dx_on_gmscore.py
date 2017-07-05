#!/usr/bin/env python
# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

# Run ProGuard and the DX or CompatDX (= D8) tool on GmsCore V10.

from __future__ import print_function
from os import makedirs
from os.path import exists, join
from subprocess import check_call
import argparse
import gmscore_data
import os
import stat
import sys
import time

import proguard
import utils

BLAZE_BUILD_DIR = join(gmscore_data.V10_BASE,
    'blaze-out', 'intel-linux-android-4.8-libcxx-x86-opt', 'bin', 'java',
    'com', 'google', 'android', 'gmscore', 'integ')
PROGUARDED_OUTPUT = join(BLAZE_BUILD_DIR,
    'GmsCore_prod_alldpi_release_all_locales_proguard.jar')
GMSCORE_SEEDS_FILE = join(BLAZE_BUILD_DIR,
    'GmsCore_prod_alldpi_release_all_locales_proguard.seeds')
DX_JAR = join(utils.REPO_ROOT, 'tools', 'linux', 'dx', 'framework', 'dx.jar')
COMPATDX_JAR = join(utils.REPO_ROOT, 'build', 'libs', 'compatdx.jar')

def parse_arguments():
  parser = argparse.ArgumentParser(
      description = 'Run ProGuard and the DX tool on GmsCore V10.')
  parser.add_argument('--out',
      help = 'Output directory for the DX tool.',
      default = os.getcwd())
  parser.add_argument('--print-runtimeraw',
      metavar = 'BENCHMARKNAME',
      help = 'Prints the line \'<BENCHMARKNAME>(RunTimeRaw): <elapsed>' +
             ' ms\' at the end where <elapsed> is the elapsed time in' +
             ' milliseconds.')
  parser.add_argument('--compatdx',
      help = 'Use CompatDx (D8) instead of DX.',
      default = False,
      action = 'store_true')
  return parser.parse_args()

def Main():
  options = parse_arguments()

  outdir = options.out

  args = ['-forceprocessing']

  if not outdir.endswith('.zip') and not outdir.endswith('.jar') \
      and not exists(outdir):
    makedirs(outdir)

  version = gmscore_data.VERSIONS['v10']
  values = version['deploy']
  assert 'pgconf' in values

  for pgconf in values['pgconf']:
    args.extend(['@' + pgconf])

  # Remove write-protection from seeds file. The seeds file is an output of
  # ProGuard so it aborts if this is not writeable.
  st = os.stat(GMSCORE_SEEDS_FILE)
  os.chmod(GMSCORE_SEEDS_FILE,
      st.st_mode | stat.S_IWUSR | stat.S_IWGRP | stat.S_IWOTH)

  t0 = time.time()

  proguard.run(args)

  # run dex on the result
  if options.compatdx:
    jar = COMPATDX_JAR
  else:
    jar = DX_JAR

  cmd = ['java', '-jar', jar, '--min-sdk-version=26', '--multi-dex',
      '--output=' + outdir, '--dex', PROGUARDED_OUTPUT];
  utils.PrintCmd(cmd);
  check_call(cmd)

  if options.print_runtimeraw:
    print('{}(RunTimeRaw): {} ms'
        .format(options.print_runtimeraw, 1000.0 * (time.time() - t0)))

if __name__ == '__main__':
  sys.exit(Main())
