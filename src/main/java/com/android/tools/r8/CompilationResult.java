// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.graph.AppInfo;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.utils.AndroidApp;

public class CompilationResult {
  public final AndroidApp androidApp;
  public final DexApplication dexApplication;
  public final AppInfo appInfo;

  public CompilationResult(AndroidApp androidApp, DexApplication dexApplication, AppInfo appInfo) {
    this.androidApp = androidApp;
    this.dexApplication = dexApplication;
    this.appInfo = appInfo;
  }
}
