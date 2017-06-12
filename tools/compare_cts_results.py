#!/usr/bin/env python
# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

# Compare multiple CTS test_result.xml files

from __future__ import print_function
from os.path import basename
import argparse
import os
import sys

import utils

class Module:
  def __init__(self):
    self.test_cases = {}
    self.bf_covered_in_file = 0 # bitfield, one bit per file

  def get_test_case_maybe_create(self, test_case_name):
    return self.test_cases.setdefault(test_case_name, TestCase())

  def set_file_index_present(self, file_idx):
    self.bf_covered_in_file |= (1 << file_idx)

  def report(self, module_name, files, diff_only):
    bf_all_files = self.bf_covered_in_file
    for test_case_name, test_case in self.test_cases.iteritems():
      if test_case.bf_covered_in_file != bf_all_files:
        report_missing_thing('test_case', module_name + '/' + test_case_name,
            test_case.bf_covered_in_file, files)
    for test_case_name, test_case in self.test_cases.iteritems():
      test_case.report(module_name, test_case_name, files, diff_only)

class TestCase:
  def __init__(self):
    self.tests = {}
    self.bf_covered_in_file = 0 # bitfield, one bit per file

  def get_test_maybe_create(self, test_name):
    return self.tests.setdefault(test_name, Test())

  def set_file_index_present(self, file_idx):
    self.bf_covered_in_file |= (1 << file_idx)

  def report(self, module_name, test_case_name, files, diff_only):
    bf_all_files = self.bf_covered_in_file
    for test_name, test in self.tests.iteritems():
      do_report = test.bf_passing_in_file != bf_all_files
      if diff_only:
        do_report = do_report and test.bf_failing_in_file != bf_all_files
      if do_report:
        test.report(module_name, test_case_name, test_name, files)

class Test:
  def __init__(self):
    self.bf_failing_in_file = 0 # bitfields, one bit per file
    self.bf_passing_in_file = 0

  def set_file_index_outcome(self, outcome_is_passed, file_idx):
    bf_value = (1 << file_idx)
    if outcome_is_passed:
      self.bf_passing_in_file |= bf_value
    else:
      self.bf_failing_in_file |= bf_value

  # Report test's status in all files: pass/fail/missing
  def report(self, module_name, test_case_name, test_name, files):
    print('Test: {}/{}/{}:'.format(module_name, test_case_name, test_name))
    for file_idx, f in enumerate(files):
      bf_value = 1 << file_idx
      print('\t- {:20}'.format(basename(f)), end = '')
      if self.bf_passing_in_file & bf_value:
        print('PASS')
      elif self.bf_failing_in_file & bf_value:
        print('     FAIL')
      else:
        print(' --   --  (missing)')

def parse_arguments():
  parser = argparse.ArgumentParser(
      description = 'Compare multiple Android CTS test_result.xml files.')
  parser.add_argument('files', nargs = '+',
      help = 'List of (possibly renamed) test_result.xml files')
  parser.add_argument('--diff-only',
      action = 'store_true',
      help = "Don't list tests that consistently fail in all result files,"
      " list only differences.")
  return parser.parse_args()

# Read CTS test_result.xml from file and merge into result_tree
def add_to_result_tree(result_tree, file_xml, file_idx):
  module = None
  test_case = None
  for x in utils.read_cts_test_result(file_xml):
    if type(x) is utils.CtsModule:
      module = result_tree.setdefault(x.name, Module())
      module.set_file_index_present(file_idx)
    elif type(x) is utils.CtsTestCase:
      test_case = module.get_test_case_maybe_create(x.name)
      test_case.set_file_index_present(file_idx)
    else:
      assert(type(x) is utils.CtsTest)
      v = test_case.get_test_maybe_create(x.name)
      v.set_file_index_outcome(x.outcome, file_idx)

# main tree_report function
def tree_report(result_tree, files, diff_only):
  bf_all_files = (1 << len(files)) - 1
  for module_name, module in result_tree.iteritems():
    if module.bf_covered_in_file != bf_all_files:
      report_missing_thing('module', module_name, module.bf_covered_in_file,
          files)
  for module_name, module in result_tree.iteritems():
    module.report(module_name, files, diff_only)

def report_missing_thing(thing_type, thing_name, bf_covered_in_file, files):
  print('Missing {}: {}, from:'.format(thing_type, thing_name))
  for file_idx, f in enumerate(files):
    if not (bf_covered_in_file & (1 << file_idx)):
      print('\t- ' + f)

def Main():
  m = Module()
  m.get_test_case_maybe_create('qwe')

  args = parse_arguments()

  result_tree = {}
  for file_idx, f in enumerate(args.files):
    add_to_result_tree(result_tree, f, file_idx)

  tree_report(result_tree, args.files, args.diff_only)

  return 0

if __name__ == '__main__':
  sys.exit(Main())
