#!/usr/bin/env python
# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

import os
import sys

import main_utils
utils = main_utils.GetUtils();

ANDROID_EMULATORS = os.path.join(utils.TOOLS_DIR, 'benchmarks',
                                 'android-sdk-linux.tar.gz.sha1')

def Main():
  utils.DownloadFromGoogleCloudStorage(ANDROID_EMULATORS)

if __name__ == '__main__':
  sys.exit(Main())


