# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

import glob
import os
import utils

THIRD_PARTY = os.path.join(utils.REPO_ROOT, 'third_party')
ANDROID_L_API = '21'
BASE = os.path.join(THIRD_PARTY, 'youtube')

V12_10_BASE = os.path.join(BASE, 'youtube.android_12.10')
V12_10_PREFIX = os.path.join(V12_10_BASE, 'YouTubeRelease')

V12_17_BASE = os.path.join(BASE, 'youtube.android_12.17')
V12_17_PREFIX = os.path.join(V12_17_BASE, 'YouTubeRelease')

V12_22_BASE = os.path.join(BASE, 'youtube.android_12.22')
V12_22_PREFIX = os.path.join(V12_22_BASE, 'YouTubeRelease')

# NOTE: we always use android.jar for SDK v25, later we might want to revise it
#       to use proper android.jar version for each of youtube version separately.
ANDROID_JAR = os.path.join(THIRD_PARTY, 'android_jar', 'lib-v25', 'android.jar')

VERSIONS = {
  '12.10': {
    'dex' : {
      'inputs': [os.path.join(V12_10_BASE, 'YouTubeRelease_unsigned.apk')],
      'pgmap': '%s_proguard.map' % V12_10_PREFIX,
      'libraries' : [ANDROID_JAR],
      'r8-flags': '--ignore-missing-classes',
      'min-api' : ANDROID_L_API,
    },
    'deploy' : {
      'inputs': ['%s_deploy.jar' % V12_10_PREFIX],
      'pgconf': ['%s_proguard.config' % V12_10_PREFIX,
                 '%s/proguardsettings/YouTubeRelease_proguard.config' % THIRD_PARTY],
      'min-api' : ANDROID_L_API,
    },
    'proguarded' : {
      'inputs': ['%s_proguard.jar' % V12_10_PREFIX],
      'pgmap': '%s_proguard.map' % V12_10_PREFIX,
      'min-api' : ANDROID_L_API,
    }
  },
  '12.17': {
    'dex' : {
      'inputs': [os.path.join(V12_17_BASE, 'YouTubeRelease_unsigned.apk')],
      'pgmap': '%s_proguard.map' % V12_17_PREFIX,
      'libraries' : [ANDROID_JAR],
      'r8-flags': '--ignore-missing-classes',
      'min-api' : ANDROID_L_API,
    },
    'deploy' : {
      'inputs': ['%s_deploy.jar' % V12_17_PREFIX],
      'pgconf': ['%s_proguard.config' % V12_17_PREFIX,
                 '%s/proguardsettings/YouTubeRelease_proguard.config' % THIRD_PARTY],
      'min-api' : ANDROID_L_API,
    },
    'proguarded' : {
      'inputs': ['%s_proguard.jar' % V12_17_PREFIX],
      'pgmap': '%s_proguard.map' % V12_17_PREFIX,
      'min-api' : ANDROID_L_API,
    }
  },
  '12.22': {
    'dex' : {
      'inputs': [os.path.join(V12_22_BASE, 'YouTubeRelease_unsigned.apk')],
      'pgmap': '%s_proguard.map' % V12_22_PREFIX,
      'libraries' : [ANDROID_JAR],
      'r8-flags': '--ignore-missing-classes',
      'min-api' : ANDROID_L_API,
    },
    'deploy' : {
      'inputs': ['%s_deploy.jar' % V12_22_PREFIX],
      'pgconf': [
          '%s_proguard.config' % V12_22_PREFIX,
          '%s/proguardsettings/YouTubeRelease_proguard.config' % THIRD_PARTY],
      'maindexrules' : [
          os.path.join(V12_22_BASE, 'mainDexClasses.rules'),
          os.path.join(V12_22_BASE, 'main-dex-classes-release.cfg'),
          os.path.join(V12_22_BASE, 'main_dex_YouTubeRelease_proguard.cfg')],
    },
    'proguarded' : {
      'inputs': ['%s_proguard.jar' % V12_22_PREFIX],
      'pgmap': '%s_proguard.map' % V12_22_PREFIX,
      'min-api' : ANDROID_L_API,
    }
  },
}
