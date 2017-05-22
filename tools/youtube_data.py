# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

import glob
import os
import utils

THIRD_PARTY = os.path.join(utils.REPO_ROOT, 'third_party')
BASE = os.path.join(THIRD_PARTY, 'youtube')

V11_47_BASE = os.path.join(BASE, 'youtube.android_11.47')
V11_47_PREFIX = os.path.join(V11_47_BASE, 'YouTubeRelease')

V12_10_BASE = os.path.join(BASE, 'youtube.android_12.10')
V12_10_PREFIX = os.path.join(V12_10_BASE, 'YouTubeRelease')

V12_17_BASE = os.path.join(BASE, 'youtube.android_12.17')
V12_17_PREFIX = os.path.join(V12_17_BASE, 'YouTubeRelease')

# NOTE: we always use android.jar for SDK v25, later we might want to revise it
#       to use proper android.jar version for each of youtube version separately.
ANDROID_JAR = os.path.join(THIRD_PARTY, 'android_jar', 'lib-v25', 'android.jar')

VERSIONS = {
  '11.47': {
    'dex' : {
      'inputs': [os.path.join(V11_47_BASE, 'YouTubeRelease_unsigned.apk')],
      'pgmap': '%s_proguard.map' % V11_47_PREFIX,
      'libraries' : [ANDROID_JAR],
      'r8-flags': '--ignore-missing-classes',
    },
    'deploy' : {
      'inputs': ['%s_deploy.jar' % V11_47_PREFIX],
      'pgconf': ['%s_proguard.config' % V11_47_PREFIX,
                 '%s/proguardsettings/YouTubeRelease_proguard.config' % THIRD_PARTY],
    },
    'proguarded' : {
      'inputs': ['%s_proguard.jar' % V11_47_PREFIX],
      'pgmap': '%s_proguard.map' % V11_47_PREFIX
    }
  },
  '12.10': {
    'dex' : {
      'inputs': [os.path.join(V12_10_BASE, 'YouTubeRelease_unsigned.apk')],
      'pgmap': '%s_proguard.map' % V12_10_PREFIX,
      'libraries' : [ANDROID_JAR],
      'r8-flags': '--ignore-missing-classes',
    },
    'deploy' : {
      'inputs': ['%s_deploy.jar' % V12_10_PREFIX],
      'pgconf': ['%s_proguard.config' % V12_10_PREFIX,
                 '%s/proguardsettings/YouTubeRelease_proguard.config' % THIRD_PARTY],
    },
    'proguarded' : {
      'inputs': ['%s_proguard.jar' % V12_10_PREFIX],
      'pgmap': '%s_proguard.map' % V12_10_PREFIX
    }
  },
  '12.17': {
    'dex' : {
      'inputs': [os.path.join(V12_17_BASE, 'YouTubeRelease_unsigned.apk')],
      'pgmap': '%s_proguard.map' % V12_17_PREFIX,
      'libraries' : [ANDROID_JAR],
      'r8-flags': '--ignore-missing-classes',
    },
    'deploy' : {
      'inputs': ['%s_deploy.jar' % V12_17_PREFIX],
      'pgconf': ['%s_proguard.config' % V12_17_PREFIX,
                 '%s/proguardsettings/YouTubeRelease_proguard.config' % THIRD_PARTY],
    },
    'proguarded' : {
      'inputs': ['%s_proguard.jar' % V12_17_PREFIX],
      'pgmap': '%s_proguard.map' % V12_17_PREFIX
    }
  },
}
