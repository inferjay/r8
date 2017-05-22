#!/usr/bin/env python
# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

# Script for uploading to x20 as a dependency in the same way we use cloud
# storage.

import optparse
import os
import shutil
import stat
import subprocess
import sys
import tarfile
import utils

GMSCORE_DEPS = '/google/data/rw/teams/gmscore-size/deps'

def parse_options():
  return optparse.OptionParser().parse_args()

def create_archive(name):
  tarname = '%s.tar.gz' % name
  with tarfile.open(tarname, 'w:gz') as tar:
    tar.add(name)
  return tarname

def Main():
  (options, args) = parse_options()
  assert len(args) == 1
  name = args[0]
  print 'Creating archive for %s' % name
  if not name in os.listdir('.'):
    print 'You must be standing directly below the directory you are uploading'
    return 1
  filename = create_archive(name)
  sha1 = utils.get_sha1(filename)
  dest = os.path.join(GMSCORE_DEPS, sha1)
  print 'Uploading to %s' % dest
  shutil.copyfile(filename, dest)
  os.chmod(dest, stat.S_IRWXU | stat.S_IROTH | stat.S_IXOTH | stat.S_IRWXG)
  sha1_file = '%s.sha1' % filename
  with open(sha1_file, 'w') as output:
    output.write(sha1)
  print 'Sha (%s) written to: %s' % (sha1, sha1_file)

if __name__ == '__main__':
  sys.exit(Main())
