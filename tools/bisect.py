#!/usr/bin/env python
# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

import gradle
import os
import subprocess
import sys
import utils

JAR = os.path.join(utils.REPO_ROOT, 'build', 'libs', 'bisect.jar')

def run(args, build, debug):
  if build:
    gradle.RunGradle(['bisect'])
  cmd = ['java']
  if debug:
    cmd.append('-ea')
  cmd.extend(['-jar', JAR])
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
  run(args, build, True)

if __name__ == '__main__':
  sys.exit(main())
