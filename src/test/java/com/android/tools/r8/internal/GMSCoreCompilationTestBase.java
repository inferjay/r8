// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.internal;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.CompilationMode;
import com.android.tools.r8.R8RunArtTestsTest.CompilerUnderTest;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

public abstract class GMSCoreCompilationTestBase extends CompilationTestBase {
  public static final String GMSCORE_V4_DIR = "third_party/gmscore/v4/";
  public static final String GMSCORE_V5_DIR = "third_party/gmscore/v5/";
  public static final String GMSCORE_V6_DIR = "third_party/gmscore/v6/";
  public static final String GMSCORE_V7_DIR = "third_party/gmscore/v7/";
  public static final String GMSCORE_V8_DIR = "third_party/gmscore/v8/";
  public static final String GMSCORE_V9_DIR = "third_party/gmscore/gmscore_v9/";
  public static final String GMSCORE_V10_DIR = "third_party/gmscore/gmscore_v10/";

  public static final int GMSCORE_V9_MAX_SIZE = 35000000;
  public static final int GMSCORE_V10_MAX_SIZE = 35000000;

  static final String GMSCORE_APK = "GMSCore.apk";

  // Files pertaining to the full GMSCore build.
  static final String PG_MAP = "GmsCore_prod_alldpi_release_all_locales_proguard.map";
  static final String PG_CONF = "GmsCore_prod_alldpi_release_all_locales_proguard.config";
  static final String DEPLOY_JAR = "GmsCore_prod_alldpi_release_all_locales_deploy.jar";
  static final String REFERENCE_APK = "noshrink_x86_GmsCore_prod_alldpi_release_unsigned.apk";

  public void runR8AndCheckVerification(CompilationMode mode, String version)
      throws ProguardRuleParserException, ExecutionException, IOException, CompilationException {
    runAndCheckVerification(CompilerUnderTest.R8, mode, version);
  }

  public void runAndCheckVerification(
      CompilerUnderTest compiler, CompilationMode mode, String version)
      throws ExecutionException, IOException, ProguardRuleParserException, CompilationException {
    runAndCheckVerification(
        compiler, mode, version + GMSCORE_APK, null, null,
        Paths.get(version, GMSCORE_APK).toString());
  }
}
