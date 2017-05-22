# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

# Different utility functions used accross scripts

import hashlib
import os
import shutil
import subprocess
import sys
import tempfile

TOOLS_DIR = os.path.abspath(os.path.normpath(os.path.join(__file__, '..')))
REPO_ROOT = os.path.realpath(os.path.join(TOOLS_DIR, '..'))
DOWNLOAD_DEPS = os.path.join(REPO_ROOT, 'scripts', 'download-deps.sh')

def PrintCmd(s):
  if type(s) is list:
    s = ' '.join(s)
  print 'Running: %s' % s
  # I know this will hit os on windows eventually if we don't do this.
  sys.stdout.flush()

def DownloadFromGoogleCloudStorage(sha1_file, bucket='r8-deps'):
  cmd = ["download_from_google_storage", "-n", "-b", bucket, "-u", "-s",
         sha1_file]
  PrintCmd(cmd)
  subprocess.check_call(cmd)

def get_sha1(filename):
  sha1 = hashlib.sha1()
  with open(filename, 'rb') as f:
    while True:
      chunk = f.read(1024*1024)
      if not chunk:
        break
      sha1.update(chunk)
  return sha1.hexdigest()

class TempDir(object):
 def __init__(self, prefix=''):
   self._temp_dir = None
   self._prefix = prefix

 def __enter__(self):
   self._temp_dir = tempfile.mkdtemp(self._prefix)
   return self._temp_dir

 def __exit__(self, *_):
   shutil.rmtree(self._temp_dir, ignore_errors=True)

class ChangedWorkingDirectory(object):
 def __init__(self, working_directory):
   self._working_directory = working_directory

 def __enter__(self):
   self._old_cwd = os.getcwd()
   print "Enter directory = ", self._working_directory
   os.chdir(self._working_directory)

 def __exit__(self, *_):
   print "Enter directory = ", self._old_cwd
   os.chdir(self._old_cwd)
