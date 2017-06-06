#!/usr/bin/env python
# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

# Clone and build AOSP, using D8 instead of JACK or DX,
# then run the Android-CTS on the emulator and compare results
# to a baseline.
#
# This script uses the repo manifest file 'third_party/aosp_manifest.xml'
# which is a snapshot of the aosp repo set.
# The manifest file can be updated with the following commands:
#
#   cd build/aosp
#   repo manifest -o ../../third_party/aosp_manifest.xml -r
#
# The baseline is the `test_result.xml` file which is created with an AOSP
# build which uses the default (JACK) toolset.
#
# Use this script, with '--tool=jack' to reproduce the baseline results
#

from __future__ import print_function
from glob import glob
from itertools import chain
from os.path import join
from shutil import copy2
from subprocess import check_call, Popen
import argparse
import os
import re
import sys

import gradle
import utils

CTS_BASELINE = join(utils.REPO_ROOT,
  'third_party/android_cts_baseline/test_result.xml')
AOSP_MANIFEST_XML = join(utils.REPO_ROOT, 'third_party',
  'aosp_manifest.xml')
AOSP_HELPER_SH = join(utils.REPO_ROOT, 'scripts', 'aosp_helper.sh')

D8_JAR = join(utils.REPO_ROOT, 'build/libs/d8.jar')
COMPATDX_JAR = join(utils.REPO_ROOT, 'build/libs/compatdx.jar')
D8LOGGER_JAR = join(utils.REPO_ROOT, 'build/libs/d8logger.jar')

AOSP_ROOT = join(utils.REPO_ROOT, 'build/aosp')

AOSP_MANIFEST_URL = 'https://android.googlesource.com/platform/manifest'
AOSP_PRESET = 'aosp_x86-eng'

AOSP_OUT = join(AOSP_ROOT, 'out')
OUT_IMG = join(AOSP_ROOT, 'out_img') # output dir for android img build
OUT_CTS = join(AOSP_ROOT, 'out_cts') # output dir for CTS build
RESULTS_DIR_BASE = join(OUT_CTS, 'host/linux-x86/cts/android-cts/results')
CTS_TRADEFED = join(OUT_CTS,
  'host/linux-x86/cts/android-cts/tools/cts-tradefed')

J_OPTION = '-j8'

EXIT_FAILURE = 1

def parse_arguments():
  parser = argparse.ArgumentParser(
      description = 'Download the AOSP source tree, build an Android image'
      ' and the CTS targets and run CTS with the emulator on the image.')
  parser.add_argument('--tool',
      choices = ['jack', 'dx', 'd8'],
      default = 'd8',
      help='compiler tool to use')
  parser.add_argument('--d8log',
      metavar = 'FILE',
      help = 'Enable logging d8 (compatdx) calls to the specified file. Works'
          ' only with --tool=d8')
  parser.add_argument('--save-result',
      metavar = 'FILE',
      help = 'Save final test_result.xml to the specified file.')
  parser.add_argument('--no-baseline',
      action = 'store_true',
      help = "Don't compare results to baseline hence don't return failure if"
      ' they differ.')
  parser.add_argument('--clean-dex',
      action = 'store_true',
      help = 'Remove AOSP/dex files always, before the build. By default they'
      " are removed only if '--tool=d8' and they're older then the D8 tool")
  return parser.parse_args()

# return False on error
def remove_aosp_out():
  if os.path.exists(AOSP_OUT):
    if os.path.islink(AOSP_OUT):
      os.remove(AOSP_OUT)
    else:
      print("The AOSP out directory ('" + AOSP_OUT + "') is expected"
        " to be a symlink", file = sys.stderr)
      return False
  return True

# Read the xml test result file into an in-memory tree:
# Extract only the Module/TestCase/Test names and outcome (True|False for
# PASS|FAIL):
#
#     tree[module_name][testcase_name][test_name] = True|False
#
def read_test_result_into_tree(filename):
  re_module = re.compile('<Module name="([^"]*)"')
  re_testcase = re.compile('<TestCase name="([^"]*)"')
  re_test = re.compile('<Test result="(pass|fail)" name="([^"]*)"')
  tree = {}
  module = None
  testcase = None
  with open(filename) as f:
    for line in f:
      m = re_module.search(line)
      if m:
        module_name = m.groups()[0]
        tree[module_name] = {}
        module = tree[module_name]
        continue

      m = re_testcase.search(line)
      if m:
        testcase_name = m.groups()[0]
        module[testcase_name] = {}
        testcase = module[testcase_name]
        continue

      m = re_test.search(line)
      if m:
        outcome = m.groups()[0]
        test_name = m.groups()[1]
        assert outcome in ["fail", "pass"]
        testcase[test_name] = outcome == "pass"
  return tree

# Report the items with the title
def report_key_diff(title, items, prefix = ''):
  if len(items) > 0:
    print(title, ":")
    for x in items:
      print("- {}{}".format(prefix, x))
    print()


def diff_sets(base_minus_result_title, result_minus_base_title,
    base_set, result_set, prefix = ''):
  base_minus_result = base_set - result_set
  result_minus_base = result_set - base_set
  report_key_diff(base_minus_result_title, base_minus_result, prefix)
  report_key_diff(result_minus_base_title, result_minus_base, prefix)
  return len(base_minus_result) > 0 or len(result_minus_base) > 0

def diff_tree_report(baseline_tree, result_tree):
  baseline_modules = set(baseline_tree.keys())
  result_modules = set(result_tree.keys())
  differ = diff_sets('Modules missing from current result',
      'New modules appeared in current result',
      baseline_modules, result_modules)
  for module in (result_modules & baseline_modules):
    baseline_module = baseline_tree[module]
    result_module = result_tree[module]
    baseline_testcases = set(baseline_module.keys())
    result_testcases = set(result_module.keys())
    differ = diff_sets('Test cases missing from current result',
        'New test cases appeared in current result',
        baseline_testcases, result_testcases, module + '/') \
        or differ
    for testcase in (result_testcases & baseline_testcases):
      baseline_testcase = baseline_module[testcase]
      result_testcase = result_module[testcase]
      baseline_tests = set(baseline_testcase.keys())
      result_tests = set(result_testcase.keys())
      differ = diff_sets('Tests missing from current result',
          'New tests appeared in current result',
          baseline_tests, result_tests, module + '/' + testcase + '/') \
          or differ
      need_newline_at_end = False
      for test in (result_tests & baseline_tests):
        baseline_outcome = baseline_testcase[test]
        result_outcome = result_testcase[test]
        if baseline_outcome != result_outcome:
          differ = True
          print('Test: {}/{}/{}, change: {}'.format(
            module, testcase, test,
            'PASS -> FAIL' if baseline_outcome else 'FAIL -> PASS'))
          need_newline_at_end = True
      if need_newline_at_end:
        print()
  return differ

def setup_and_clean(tool_is_d8, clean_dex):
  # Two output dirs, one for the android image and one for cts tests.
  # The output is compiled with d8 and jack, respectively.
  utils.makedirs_if_needed(AOSP_ROOT)
  utils.makedirs_if_needed(OUT_IMG)
  utils.makedirs_if_needed(OUT_CTS)

  # remove dex files older than the current d8 tool
  counter = 0
  if tool_is_d8 or clean_dex:
    if not clean_dex:
      d8jar_mtime = os.path.getmtime(D8_JAR)
    dex_files = (chain.from_iterable(glob(join(x[0], '*.dex'))
      for x in os.walk(OUT_IMG)))
    for f in dex_files:
      if clean_dex or os.path.getmtime(f) <= d8jar_mtime:
        os.remove(f)
        counter += 1
  if counter > 0:
    print('Removed {} dex files.'.format(counter))

def checkout_aosp():
  # checkout AOSP source
  manifests_dir = join(AOSP_ROOT, '.repo', 'manifests')
  utils.makedirs_if_needed(manifests_dir)

  copy2(AOSP_MANIFEST_XML, manifests_dir)
  check_call(['repo', 'init', '-u', AOSP_MANIFEST_URL, '-m',
    'aosp_manifest.xml', '--depth=1'], cwd = AOSP_ROOT)

  check_call(['repo', 'sync', '-dq', J_OPTION], cwd = AOSP_ROOT)

def Main():
  args = parse_arguments()

  if args.d8log and args.tool != 'd8':
    print("The '--d8log' option works only with '--tool=d8'.",
        file = sys.stderr)
    return EXIT_FAILURE

  assert args.tool in ['jack', 'dx', 'd8']

  jack_option = 'ANDROID_COMPILE_WITH_JACK=' \
      + ('true' if args.tool == 'jack' else 'false')

  # DX_ALT_JAR need to be cleared if not set, for 'make' to work properly
  alt_jar_option = 'DX_ALT_JAR='
  if args.tool == 'd8':
    if args.d8log:
      alt_jar_option += D8LOGGER_JAR
      os.environ['D8LOGGER_OUTPUT'] = args.d8log
    else:
      alt_jar_option += COMPATDX_JAR

  gradle.RunGradle(['d8','d8logger', 'compatdx'])

  setup_and_clean(args.tool == 'd8', args.clean_dex)

  checkout_aosp()

  # activate OUT_CTS and build Android CTS
  # AOSP has no clean way to set the output directory.
  # In order to do incremental builds we apply the following symlink-based
  # workaround.
  # Note: this does not work on windows, but the AOSP
  # doesn't build, either

  if not remove_aosp_out():
    return EXIT_FAILURE
  print("-- Building CTS with 'make {} cts'.".format(J_OPTION))
  os.symlink(OUT_CTS, AOSP_OUT)
  check_call([AOSP_HELPER_SH, AOSP_PRESET, 'make', J_OPTION, 'cts'],
      cwd = AOSP_ROOT)

  # activate OUT_IMG and build the Android image
  if not remove_aosp_out():
    return EXIT_FAILURE
  print("-- Building Android image with 'make {} {} {}'." \
    .format(J_OPTION, jack_option, alt_jar_option))
  os.symlink(OUT_IMG, AOSP_OUT)
  check_call([AOSP_HELPER_SH, AOSP_PRESET, 'make', J_OPTION, jack_option,
      alt_jar_option], cwd = AOSP_ROOT)

  emulator_proc = Popen([AOSP_HELPER_SH, AOSP_PRESET,
      'emulator', '-partition-size', '4096', '-wipe-data'], cwd = AOSP_ROOT)

  if emulator_proc.poll() is not None:
    print("Can't start Android Emulator.", file = sys.stderr)

  check_call([AOSP_HELPER_SH, AOSP_PRESET, 'run-cts',
      CTS_TRADEFED, 'run', 'cts'], cwd = AOSP_ROOT)

  emulator_proc.terminate()

  # find the newest test_result.xml
  result_dirs = \
      [f for f in glob(join(RESULTS_DIR_BASE, '*')) if os.path.isdir(f)]
  if len(result_dirs) == 0:
    print("Can't find result directories in ", RESULTS_DIR_BASE)
    return EXIT_FAILURE
  result_dirs.sort(key = os.path.getmtime)
  results_xml = join(result_dirs[-1], 'test_result.xml')

  # print summaries
  re_summary = re.compile('<Summary ')

  summaries = [('Summary from current test results: ', results_xml)]
  if not args.no_baseline:
    summaries.append(('Summary from baseline: ', CTS_BASELINE))

  for (title, result_file) in summaries:
    print(title, result_file)
    with open(result_file) as f:
      for line in f:
        if re_summary.search(line):
          print(line)
          break

  if args.no_baseline:
    r = 0
  else:
    print('Comparing test results to baseline:\n')

    result_tree = read_test_result_into_tree(results_xml)
    baseline_tree = read_test_result_into_tree(CTS_BASELINE)

    r = EXIT_FAILURE if diff_tree_report(baseline_tree, result_tree) else 0

  if args.save_result:
    copy2(results_xml, args.save_result)

  return r

if __name__ == '__main__':
  sys.exit(Main())
