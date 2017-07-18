# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

# Different utility functions used accross scripts

import hashlib
import os
import re
import shutil
import subprocess
import sys
import tempfile

TOOLS_DIR = os.path.abspath(os.path.normpath(os.path.join(__file__, '..')))
REPO_ROOT = os.path.realpath(os.path.join(TOOLS_DIR, '..'))
MEMORY_USE_TMP_FILE = 'memory_use.tmp'
DEX_SEGMENTS_JAR = os.path.join(REPO_ROOT, 'build', 'libs',
    'dexsegments.jar')
DEX_SEGMENTS_RESULT_PATTERN = re.compile('- ([^:]+): ([0-9]+)')

def PrintCmd(s):
  if type(s) is list:
    s = ' '.join(s)
  print 'Running: %s' % s
  # I know this will hit os on windows eventually if we don't do this.
  sys.stdout.flush()

def IsWindows():
  return os.name == 'nt'

def DownloadFromGoogleCloudStorage(sha1_file, bucket='r8-deps'):
  suffix = '.bat' if IsWindows() else ''
  download_script = 'download_from_google_storage%s' % suffix
  cmd = [download_script, '-n', '-b', bucket, '-u', '-s',
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

def makedirs_if_needed(path):
  try:
    os.makedirs(path)
  except OSError:
    if not os.path.isdir(path):
        raise

def upload_html_to_cloud_storage(directory, destination):
  # Upload and make the content encoding right for viewing directly
  cmd = ['gsutil.py', 'cp', '-z', 'html', '-a',
         'public-read', '-R', directory, destination]
  PrintCmd(cmd)
  subprocess.check_call(cmd)

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
   print 'Enter directory = ', self._working_directory
   os.chdir(self._working_directory)

 def __exit__(self, *_):
   print 'Enter directory = ', self._old_cwd
   os.chdir(self._old_cwd)

# Reading Android CTS test_result.xml

class CtsModule(object):
  def __init__(self, module_name):
    self.name = module_name

class CtsTestCase(object):
  def __init__(self, test_case_name):
    self.name = test_case_name

class CtsTest(object):
  def __init__(self, test_name, outcome):
    self.name = test_name
    self.outcome = outcome

# Generator yielding CtsModule, CtsTestCase or CtsTest from
# reading through a CTS test_result.xml file.
def read_cts_test_result(file_xml):
  re_module = re.compile('<Module name="([^"]*)"')
  re_test_case = re.compile('<TestCase name="([^"]*)"')
  re_test = re.compile('<Test result="(pass|fail)" name="([^"]*)"')
  with open(file_xml) as f:
    for line in f:
      m = re_module.search(line)
      if m:
        yield CtsModule(m.groups()[0])
        continue
      m = re_test_case.search(line)
      if m:
        yield CtsTestCase(m.groups()[0])
        continue
      m = re_test.search(line)
      if m:
        outcome = m.groups()[0]
        assert outcome in ['fail', 'pass']
        yield CtsTest(m.groups()[1], outcome == 'pass')

def grep_memoryuse(logfile):
  re_vmhwm = re.compile('^VmHWM:[ \t]*([0-9]+)[ \t]*([a-zA-Z]*)')
  result = None
  with open(logfile) as f:
    for line in f:
      m = re_vmhwm.search(line)
      if m:
        groups = m.groups()
        s = len(groups)
        if s >= 1:
          result = int(groups[0])
          if s >= 2:
            unit = groups[1]
            if unit == 'kB':
              result *= 1024
            elif unit != '':
              raise Exception('Unrecognized unit in memory usage log: {}'
                  .format(unit))
  if result is None:
    raise Exception('No memory usage found in log: {}'.format(logfile))
  return result

# Return a dictionary: {segment_name -> segments_size}
def getDexSegmentSizes(dex_files):
  assert len(dex_files) > 0
  cmd = ['java', '-jar', DEX_SEGMENTS_JAR]
  cmd.extend(dex_files)
  PrintCmd(cmd)
  output = subprocess.check_output(cmd)

  matches = DEX_SEGMENTS_RESULT_PATTERN.findall(output)

  if matches is None or len(matches) == 0:
    raise Exception('DexSegments failed to return any output for' \
        ' these files: {}'.format(dex_files))

  result = {}

  for match in matches:
    result[match[0]] = int(match[1])

  return result

def print_dexsegments(prefix, dex_files):
  for segment_name, size in getDexSegmentSizes(dex_files).items():
    print('{}-{}(CodeSize): {}'
        .format(prefix, segment_name, size))
