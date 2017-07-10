# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

import glob
import os
import utils

THIRD_PARTY = os.path.join(utils.REPO_ROOT, 'third_party')
BASE = os.path.join(THIRD_PARTY, 'gmscore')

V4_BASE = os.path.join(BASE, 'v4')
V5_BASE = os.path.join(BASE, 'v5')
V6_BASE = os.path.join(BASE, 'v6')
V7_BASE = os.path.join(BASE, 'v7')
V8_BASE = os.path.join(BASE, 'v8')

V9_BASE = os.path.join(BASE, 'gmscore_v9')
V9_PREFIX = os.path.join(V9_BASE, 'GmsCore_prod_alldpi_release_all_locales')

V10_BASE = os.path.join(BASE, 'gmscore_v10')
V10_PREFIX = os.path.join(V10_BASE, 'GmsCore_prod_alldpi_release_all_locales')

LATEST_BASE = os.path.join(BASE, 'latest')
LATEST_PREFIX = os.path.join(LATEST_BASE, 'GmsCore_prod_alldpi_release_all_locales')
ANDROID_L_API = '21'

# NOTE: we always use android.jar for SDK v25, later we might want to revise it
#       to use proper android.jar version for each of gmscore version separately.
ANDROID_JAR = os.path.join(THIRD_PARTY, 'android_jar', 'lib-v25', 'android.jar')

VERSIONS = {
  'v4': {
    'dex' : {
      'inputs' : glob.glob(os.path.join(V4_BASE, '*.dex')),
      'pgmap' : os.path.join(V4_BASE, 'proguard.map'),
      'libraries' : [ANDROID_JAR],
      'r8-flags': '--ignore-missing-classes',
      'min-api' : ANDROID_L_API,
    }
  },
  'v5': {
    'dex' : {
      'inputs' : glob.glob(os.path.join(V5_BASE, '*.dex')),
      'pgmap' : os.path.join(V5_BASE, 'proguard.map'),
      'libraries' : [ANDROID_JAR],
      'r8-flags': '--ignore-missing-classes',
      'min-api' : ANDROID_L_API,
    }
  },
  'v6': {
    'dex' : {
      'inputs' : glob.glob(os.path.join(V6_BASE, '*.dex')),
      'pgmap' : os.path.join(V6_BASE, 'proguard.map'),
      'libraries' : [ANDROID_JAR],
      'r8-flags': '--ignore-missing-classes',
      'min-api' : ANDROID_L_API,
    }
  },
  'v7': {
    'dex' : {
      'inputs' : glob.glob(os.path.join(V7_BASE, '*.dex')),
      'pgmap' : os.path.join(V7_BASE, 'proguard.map'),
      'libraries' : [ANDROID_JAR],
      'r8-flags': '--ignore-missing-classes',
      'min-api' : ANDROID_L_API,
    }
  },
  'v8': {
    'dex' : {
      'inputs' : glob.glob(os.path.join(V8_BASE, '*.dex')),
      'pgmap' : os.path.join(V8_BASE, 'proguard.map'),
      'libraries' : [ANDROID_JAR],
      'r8-flags': '--ignore-missing-classes',
      'min-api' : ANDROID_L_API,
    }
  },
  'v9': {
    'dex' : {
      'inputs': [os.path.join(V9_BASE, 'armv7_GmsCore_prod_alldpi_release.apk')],
      'pgmap': '%s_proguard.map' % V9_PREFIX,
      'libraries' : [ANDROID_JAR],
      'r8-flags': '--ignore-missing-classes',
      'min-api' : ANDROID_L_API,
    },
    'deploy' : {
      'pgconf': ['%s_proguard.config' % V9_PREFIX],
      'inputs': ['%s_deploy.jar' % V9_PREFIX],
      'min-api' : ANDROID_L_API,
    },
    'proguarded' : {
      'inputs': ['%s_proguard.jar' % V9_PREFIX],
      'pgmap': '%s_proguard.map' % V9_PREFIX,
      'min-api' : ANDROID_L_API,
     }
  },
  'v10': {
    'dex' : {
      'inputs': [os.path.join(V10_BASE, 'armv7_GmsCore_prod_alldpi_release.apk')],
      'pgmap': '%s_proguard.map' % V10_PREFIX,
      'libraries' : [ANDROID_JAR],
      'r8-flags': '--ignore-missing-classes',
      'min-api' : ANDROID_L_API,
    },
    'deploy' : {
      'inputs': ['%s_deploy.jar' % V10_PREFIX],
      'pgconf': ['%s_proguard.config' % V10_PREFIX],
      'min-api' : ANDROID_L_API,
    },
    'proguarded' : {
      'inputs': ['%s_proguard.jar' % V10_PREFIX],
      'pgmap': '%s_proguard.map' % V10_PREFIX,
      'min-api' : ANDROID_L_API,
    }
  },
  'latest': {
    'deploy' : {
      'inputs': ['%s_deploy.jar' % LATEST_PREFIX],
      'pgconf': [
          '%s_proguard.config' % LATEST_PREFIX,
          '%s/proguardsettings/GmsCore_proguard.config' % THIRD_PARTY],
      'min-api' : ANDROID_L_API,
    },
  },
}
