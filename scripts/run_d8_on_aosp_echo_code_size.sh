#!/bin/bash
#
# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

# Compile AOSP jars with D8 (CompatDX) and print the code size in bytes

set -e

readonly DX_REPLAY="third_party/android_cts_baseline/dx_replay"

"${DX_REPLAY}/replay_script.py" \
        java -jar "build/libs/compatdx.jar" >/dev/null

codesize=$(du -b -d 0 "third_party/android_cts_baseline/dx_replay/out" \
    | grep -Eo "^[0-9]+")

echo "Aosp(CodeSize): $codesize"


