#!/bin/bash
#
# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.
#
# Clone and build AOSP, using D8 instead of JACK or DX,
# then run the Android-CTS on the emulator and compare results
# to a baseline.
#
# This script uses the repo manifest file 'third_party/aosp_manifest.xml'
# which is a snapshot of the aosp repo set.
# The manifest file can be updated with the following commands:
#
#     cd build/aosp
#     repo manifest -o ../../third_party/aosp_manifest.xml -r
#
# The baseline is the `test_result.xml` file which is created with an AOSP
# build which uses the default (JACK) toolset.
#
# To reproduce the baseline results, follow the instructions in this script,
# except don't set `ANDROID_COMPILE_WITH_JACK=false` for the `make` command.
# Also, you don't need to apply either of the patches (PATCH#1 and #2, see
# below)
#

set -e

readonly R8_ROOT=$(cd "$(dirname ${BASH_SOURCE[0]})"/..; pwd)
readonly AOSP_ROOT="$R8_ROOT/build/aosp"
readonly CTS_BASELINE="$R8_ROOT/third_party/android_cts_baseline/test_result.xml"
readonly D8_JAR="$R8_ROOT/build/libs/d8.jar"
readonly J_OPTION="-j8"
readonly OUT_IMG=out_img # output dir for android image build
readonly OUT_CTS=out_cts # output dir for CTS build

# Process an Android CTS test_result.xml file for easy comparison with a
# baseline.
#
# The function transforms these lines:
#
#     <Test result="pass|fail" name="<name>" />
#
# to this:
#
#     <module-name>/<testcase-name>/<name> -> PASS|FAIL
#
flatten_xml() {
  local input_file="$1"
  local module
  local testcase
  local testname
  while IFS='' read -r line || [[ -n "$line" ]]; do
    if [[ $line =~ \<Module\ name=\"([^\"]*)\" ]]; then
      module=${BASH_REMATCH[1]}
    elif [[ $line =~ \<TestCase\ name=\"([^\"]*)\" ]]; then
      testcase=${BASH_REMATCH[1]}
    elif [[ $line =~ \<Test\ result=\"pass\"\ name=\"([^\"]*)\" ]]; then
      echo "$module/$testcase/${BASH_REMATCH[1]} -> PASS"
    elif [[ $line =~ \<Test\ result=\"fail\"\ name=\"([^\"]*)\" ]]; then
      echo "$module/$testcase/${BASH_REMATCH[1]} -> FAIL"
    fi
  done < "$input_file"
}

#### MAIN ####

cd "$R8_ROOT"
tools/gradle.py d8

mkdir -p "$AOSP_ROOT"
cd "$AOSP_ROOT"

# Two output dirs, one for the android image and one for cts tests. The output
# is compiled with d8 and jack, respectively.
mkdir -p "$OUT_IMG" "$OUT_CTS"

# remove dex files older than the current d8 tool
find "$OUT_IMG" ! -newer "$R8_ROOT/build/libs/d8.jar" -name '*.dex' -exec rm {} \;

# checkout AOSP source
mkdir -p .repo/manifests
cp "$R8_ROOT/third_party/aosp_manifest.xml" .repo/manifests

repo init -u "https://android.googlesource.com/platform/manifest" -m aosp_manifest.xml
repo sync -dq $J_OPTION

# activate $OUT_CTS
rm -rf out
ln -s "$OUT_CTS" out

. build/envsetup.sh
lunch aosp_x86-eng
make $J_OPTION cts

# activate $OUT_IMG
rm -rf out
ln -s "$OUT_IMG" out

. build/envsetup.sh
lunch aosp_x86-eng
make $J_OPTION ANDROID_COMPILE_WITH_JACK=false DX_ALT_JAR="$D8_JAR"

# create sdcard image for media tests

mkdir -p "$R8_ROOT/build/tmp"
sdcard_file="$R8_ROOT/build/tmp/sdcard.img"
rm -f "$sdcard_file"
mksdcard 4G "$sdcard_file"

emulator -partition-size 4096 -wipe-data -sdcard "$sdcard_file" &
emulator_pid=$!

adb wait-for-device
adb shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done; input keyevent 82'

echo "exit" | \
  ANDROID_BUILD_TOP= \
    "$OUT_CTS/host/linux-x86/cts/android-cts/tools/cts-tradefed" run cts

kill $emulator_pid
rm -f "$sdcard_file"

# find the newest test_result.xml

results_dir="$OUT_CTS/host/linux-x86/cts/android-cts/results"
timestamp="$(ls --group-directories-first -t "$results_dir" | head -1)"
results_xml="$results_dir/$timestamp/test_result.xml"

echo "Summary from current test results: $results_xml"
grep "<Summary " "$results_xml"

echo "Summary from baseline: $CTS_BASELINE"
grep "<Summary " "$CTS_BASELINE"

echo "Comparing test results to baseline"

diff <(flatten_xml "$results_xml") <(flatten_xml "$CTS_BASELINE")
exit $? # make it explicit that the result of the diff must be the result of this script

