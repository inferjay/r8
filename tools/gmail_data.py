# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

import glob
import os
import utils

THIRD_PARTY = os.path.join(utils.REPO_ROOT, 'third_party')
BASE = os.path.join(THIRD_PARTY, 'gmail')

V170604_16_BASE = os.path.join(BASE, 'gmail_android_170604.16')
V170604_16_PREFIX = os.path.join(V170604_16_BASE, 'Gmail_release_unstripped')

# NOTE: We always use android.jar for SDK v25 for now.
ANDROID_JAR = os.path.join(THIRD_PARTY, 'android_jar', 'lib-v25', 'android.jar')

VERSIONS = {
  '170604.16': {
    'dex' : {
      'inputs': [os.path.join(V170604_16_BASE, 'Gmail_release_unsigned.apk')],
      'pgmap': '%s_proguard.map' % V170604_16_PREFIX,
      'libraries' : [ANDROID_JAR],
      'r8-flags': '--ignore-missing-classes',
    },
    'deploy' : {
      'inputs': ['%s_deploy.jar' % V170604_16_PREFIX],
      'pgconf': ['%s_proguard.config' % V170604_16_PREFIX],
    },
    'proguarded' : {
      'inputs': ['%s_proguard.jar' % V170604_16_PREFIX],
      'pgmap': '%s_proguard.map' % V170604_16_PREFIX,
    }
  },
}
