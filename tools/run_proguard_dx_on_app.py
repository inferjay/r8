#!/usr/bin/env python
# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

# Run ProGuard and the DX or CompatDX (= D8) tool on GmsCore V10.

from __future__ import print_function
from glob import glob
from os import makedirs
from os.path import exists, join, splitext
from subprocess import check_call
import argparse
import fnmatch
import gmscore_data
import os
import stat
import sys
import time

import gmail_data
import gmscore_data
import proguard
import utils
import youtube_data

APPS = ['gmscore', 'youtube']
DX_JAR = join(utils.REPO_ROOT, 'tools', 'linux', 'dx', 'framework', 'dx.jar')
COMPATDX_JAR = join(utils.REPO_ROOT, 'build', 'libs', 'compatdx.jar')

def parse_arguments(argv):
  parser = argparse.ArgumentParser(
      description = 'Run ProGuard and the DX tool on GmsCore V10.')
  parser.add_argument('--app', required = True, choices = APPS)
  parser.add_argument('--out',
      help = 'Output directory for the DX tool.',
      default = os.getcwd())
  parser.add_argument('--compatdx',
      help = 'Use CompatDx (D8) instead of DX.',
      default = False,
      action = 'store_true')
  parser.add_argument('--print-runtimeraw',
      metavar = 'BENCHMARKNAME',
      help = 'Print the line \'<BENCHMARKNAME>(RunTimeRaw): <elapsed>' +
             ' ms\' at the end where <elapsed> is the elapsed time in' +
             ' milliseconds.')
  parser.add_argument('--print-memoryuse',
      metavar='BENCHMARKNAME',
      help='Print the line \'<BENCHMARKNAME>(MemoryUse):' +
           ' <mem>\' at the end where <mem> is the peak' +
           ' peak resident set size (VmHWM) in bytes.')
  parser.add_argument('--print-dexsegments',
      metavar = 'BENCHMARKNAME',
      help = 'Print the sizes of individual dex segments as ' +
          '\'<BENCHMARKNAME>-<segment>(CodeSize): <bytes>\'')
  return parser.parse_args(argv)

def Main(argv):
  options = parse_arguments(argv)

  outdir = options.out

  if options.app == 'gmscore':
    version = 'v10'
    data = gmscore_data
    base = data.V10_BASE
  elif options.app == 'youtube':
    version = '12.22'
    data = youtube_data
    base = data.V12_22_BASE
  else:
    raise Exception('Unexpected')


  args = ['-forceprocessing']

  if not outdir.endswith('.zip') and not outdir.endswith('.jar') \
      and not exists(outdir):
    makedirs(outdir)


  values_deploy = data.VERSIONS[version]['deploy']
  values_proguarded = data.VERSIONS[version]['proguarded']
  assert 'pgconf' in values_deploy

  for pgconf in values_deploy['pgconf']:
    args.extend(['@' + pgconf])

  # find seeds file
  inputs = data.VERSIONS[version]['proguarded']['inputs']
  assert len(inputs) == 1
  basename_wo_ext = splitext(os.path.basename(inputs[0]))[0]
  seeds_filename = basename_wo_ext + '.seeds'

  seeds_files = []
  for root, dirnames, filenames in os.walk(join(base, 'blaze-out')):
    for filename in fnmatch.filter(filenames, seeds_filename):
        seeds_files.append(os.path.join(root, filename))
  assert len(seeds_files) == 1

  seeds_path = seeds_files[0]
  proguarded_jar_path = splitext(seeds_path)[0] + '.jar'

  # Remove write-protection from seeds file. The seeds file is an output of
  # ProGuard so it aborts if this is not writeable.
  st = os.stat(seeds_path)
  os.chmod(seeds_path,
      st.st_mode | stat.S_IWUSR | stat.S_IWGRP | stat.S_IWOTH)

  t0 = time.time()

  proguard_memoryuse = None

  with utils.TempDir() as temp:
    track_memory_file = None
    if options.print_memoryuse:
      track_memory_file = join(temp, utils.MEMORY_USE_TMP_FILE)
    proguard.run(args, track_memory_file = track_memory_file)
    if options.print_memoryuse:
      proguard_memoryuse = utils.grep_memoryuse(track_memory_file)

  # run dex on the result
  if options.compatdx:
    jar = COMPATDX_JAR
  else:
    jar = DX_JAR

  with utils.TempDir() as temp:
    track_memory_file = None
    cmd = []
    if options.print_memoryuse:
      track_memory_file = join(temp, utils.MEMORY_USE_TMP_FILE)
      cmd.extend(['tools/track_memory.sh', track_memory_file])
    cmd.extend(['java', '-jar', jar, '--multi-dex',
        '--output=' + outdir])
    if 'min-api' in values_proguarded:
      cmd.append('--min-sdk-version=' + values_proguarded['min-api'])
    cmd.extend(['--dex', proguarded_jar_path])
    utils.PrintCmd(cmd);
    check_call(cmd)
    if options.print_memoryuse:
      dx_memoryuse = utils.grep_memoryuse(track_memory_file)
      print('{}(MemoryUse): {}'
          .format(options.print_memoryuse,
              max(proguard_memoryuse, dx_memoryuse)))

  if options.print_runtimeraw:
    print('{}(RunTimeRaw): {} ms'
        .format(options.print_runtimeraw, 1000.0 * (time.time() - t0)))

  if options.print_dexsegments:
    dex_files = glob(os.path.join(outdir, '*.dex'))
    utils.print_dexsegments(options.print_dexsegments, dex_files)

if __name__ == '__main__':
  sys.exit(Main(sys.argv[1:]))
