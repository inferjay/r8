#!/usr/bin/env python
# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

# Run ProGuard, Google's internal version

from __future__ import print_function
import os
import subprocess
import sys

import utils

PROGUARD_JAR = os.path.join(utils.REPO_ROOT, 'third_party', 'proguard',
    'proguard_internal_159423826', 'ProGuard_deploy.jar')

def run(args, track_memory_file = None):
  cmd = []
  if track_memory_file:
    cmd.extend(['tools/track_memory.sh', track_memory_file])
  cmd.extend(['java', '-jar', PROGUARD_JAR])
  cmd.extend(args)
  utils.PrintCmd(cmd)
  subprocess.check_call(cmd)

def Main():
  run(sys.argv[1:])

if __name__ == '__main__':
  sys.exit(Main())
