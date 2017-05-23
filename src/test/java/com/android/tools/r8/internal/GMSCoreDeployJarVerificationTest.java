// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.internal;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.CompilationMode;
import com.android.tools.r8.R8RunArtTestsTest.CompilerUnderTest;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class GMSCoreDeployJarVerificationTest extends GMSCoreCompilationTestBase {

  public void buildFromDeployJar(
      CompilerUnderTest compiler, CompilationMode mode, String base, boolean hasReference)
      throws ExecutionException, IOException, ProguardRuleParserException, CompilationException {
    runAndCheckVerification(
        compiler, mode, hasReference ? base + REFERENCE_APK : null, null, null, base + DEPLOY_JAR);
  }
}
