#!/usr/bin/env python
# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

import gradle
import os
import subprocess
import sys
import utils

COMPATDX_JAR = os.path.join(utils.REPO_ROOT, 'build', 'libs', 'compatdx.jar')

def run(args, build = True, debug = True, profile = False, track_memory_file=None):
  if build:
    gradle.RunGradle(['CompatDX'])
  cmd = []
  if track_memory_file:
    cmd.extend(['tools/track_memory.sh', track_memory_file])
  cmd.append('java')
  if debug:
    cmd.append('-ea')
  if profile:
    cmd.append('-agentlib:hprof=cpu=samples,interval=1,depth=8')
  cmd.extend(['-jar', COMPATDX_JAR])
  cmd.extend(args)
  subprocess.check_call(cmd)

def main():
  build = True
  args = []
  for arg in sys.argv[1:]:
    if arg in ("--build", "--no-build"):
      build = arg == "--build"
    else:
      args.append(arg)
  run(args, build)

if __name__ == '__main__':
  sys.exit(main())
