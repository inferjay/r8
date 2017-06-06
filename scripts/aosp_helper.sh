#!/bin/bash
#
# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

# Helper script for tools/test_android_cts.py

readonly AOSP_PRESET="$1"
shift
readonly TASK="$1"
shift

. build/envsetup.sh
lunch "$AOSP_PRESET"

if [[ "$TASK" == "make" ]]; then
  make "$@"
elif [[ "$TASK" == "emulator" ]]; then
  # Launch emulator in bg and kill it in a trap
  # otherwise it won't get killed when this parent process is killed
  emulator "$@" &
  emulator_pid=$!
  trap "kill $emulator_pid; exit 1" 2 15
  while true; do sleep 5; done
elif [[ "$TASK" == "run-cts" ]]; then
  adb wait-for-device
  adb shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done; input keyevent 82'

  echo "exit" | \
    ANDROID_BUILD_TOP= \
    "$@"
else
  echo "Invalid task: '$TASK'" >&2
  exit 1
fi
