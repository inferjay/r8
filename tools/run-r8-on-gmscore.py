#!/usr/bin/env python
# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

import sys
import run_on_app

if __name__ == '__main__':
  # Default compiler is R8.
  sys.exit(run_on_app.main())
