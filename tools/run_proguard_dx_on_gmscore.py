#!/usr/bin/env python
# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

# Run ProGuard and the DX or CompatDX (= D8) tool on GmsCore V10.

import sys

import run_proguard_dx_on_app

if __name__ == '__main__':
  sys.exit(run_proguard_dx_on_app.Main(sys.argv[1:] + ['--app', 'gmscore']))
