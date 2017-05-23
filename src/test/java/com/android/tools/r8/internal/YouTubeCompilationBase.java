// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.internal;

public abstract class YouTubeCompilationBase extends CompilationTestBase {
  static final String BASE = "third_party/youtube/youtube.android_12.17/";
  static final String APK = "YouTubeRelease_unsigned.apk";
  static final String DEPLOY_JAR = "YouTubeRelease_deploy.jar";
  static final String PG_JAR = "YouTubeRelease_proguard.jar";
  static final String PG_MAP = "YouTubeRelease_proguard.map";
  static final String PG_CONF = "YouTubeRelease_proguard.config";
}
