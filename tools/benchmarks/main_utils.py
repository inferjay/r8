# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

import imp
import os

TOOLS_DIR = os.path.abspath(
    os.path.normpath(os.path.join(__file__, '..', '..')))

def GetUtils():
  '''Dynamically load the tools/utils.py python module.'''
  return imp.load_source('utils', os.path.join(TOOLS_DIR, 'utils.py'))
