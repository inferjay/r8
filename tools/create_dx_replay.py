#!/usr/bin/env python
# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

# Take a file where each line is a tab-separated list of arguments for DX (or
# CompatDX/D8) and create a self-contained directory with all the input files
# and a script which replays the same DX invocations as the original list.
#
# Usage:
#
#     create_dx_replay.py <dx-args-script> <output-dir>
#
# The <dx-args-script> is a text file where each line contains tab-separated
# arguments for a DX (CompatDX/D8) call.
# The script 'tools/test_android_cts.py' can log DX invocations during an AOSP
# build to such a file. Use 'test_android_cts.py --tool=d8 --d8log=<file> ...'.

from __future__ import print_function
from os.path import join, isdir, exists, basename
from os import rmdir
from shutil import copy2
import argparse
import os
import stat
import sys

import utils

IN_SUBDIR = 'in' # subdirectory for the local copy of the input files
OUT_SUBDIR = 'out' # subdirectory prefix for the output of DX/CompatDX
REPLAY_SCRIPT_NAME = 'replay_script.py'

# This function will be called with arguments of the original DX invocation. It
# copies the original input files into the local input directory and replaces
# the references in orig_args to the local input files.
# Returns the new line to be appended to the replay script.
def process_line(out_dir, input_counter, orig_args):
  args = []
  inputs = []
  for arg in orig_args:
    if arg.startswith('--output='):
      continue # nothing to do, just skip this arg
    if arg.startswith('--'):
      args.append(arg)
    else:
      # 'arg' is the path of an input file: copy arg to local dir with
      # a new, unique name
      if isdir(arg):
        raise IOError("Adding directories ('{}') to the replay script is not"
          " implemented.".format(arg))
      elif not exists(arg):
        print("The input file to DX/CompatDX does not exist: '{}'.".format(arg))

      input_file = '{}_{}'.format(input_counter, basename(arg))

      copy2(arg, join(out_dir, join(IN_SUBDIR, input_file)))
      inputs.append(input_file)

  return 'call_dx({}, {}, {})\n'.format(input_counter, args, inputs)


def parse_arguments():
  parser = argparse.ArgumentParser(
      description = 'Creates a self-contained directory for playing back a '
      ' sequence of DX (CompatDX) calls.')
  parser.add_argument('dx_call_log',
      help = 'File containing tab-separated arguments for a DX call on each'
      ' line.')
  parser.add_argument('output_dir',
      help = 'Target path the create the self-contained directory at.')
  return parser.parse_args()

def Main():
  args = parse_arguments()

  if isdir(args.output_dir):
    rmdir(args.output_dir) # make sure to write only to empty out dir

  utils.makedirs_if_needed(join(args.output_dir, IN_SUBDIR))

  # create the first lines of the replay script
  replay_script = \
"""#!/usr/bin/env python
import os
import shutil
import subprocess
import sys

SCRIPT_DIR = os.path.abspath(os.path.normpath(os.path.join(__file__, '..')))
IN_SUBDIR = '{}'
OUT_SUBDIR = '{}'

def call_dx(input_counter, args, inputs):
  out_dir = os.path.join(SCRIPT_DIR, OUT_SUBDIR, str(input_counter))
  if not os.path.isdir(out_dir):
    os.makedirs(out_dir)
  full_inputs = [os.path.join(SCRIPT_DIR, IN_SUBDIR, i) for i in inputs]
  subprocess.check_call(sys.argv[1:] + args + ['--output=' + out_dir]
      + full_inputs)

if len(sys.argv) < 2:
  raise IOError('Usage: create_dx_replay.py <dx-command>'
      ' # can be multiple args')
abs_out_dir = os.path.join(SCRIPT_DIR, OUT_SUBDIR)
if os.path.isdir(abs_out_dir):
  shutil.rmtree(abs_out_dir)

""".format(IN_SUBDIR, OUT_SUBDIR)

  with open(args.dx_call_log) as f:
    lines = f.read().splitlines()

  input_counter = 1
  for line in lines:
    replay_script += \
        process_line(args.output_dir, input_counter, line.split('\t'))
    input_counter += 1

  script_file = join(args.output_dir, REPLAY_SCRIPT_NAME)
  with open(script_file, 'w') as f:
    f.write(replay_script)

  # chmod +x for script_file
  st = os.stat(script_file)
  os.chmod(script_file, st.st_mode | stat.S_IXUSR | stat.S_IXGRP | stat.S_IXOTH)

if __name__ == '__main__':
  sys.exit(Main())
