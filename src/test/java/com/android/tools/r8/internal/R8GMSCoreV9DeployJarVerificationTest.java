// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.internal;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.CompilationMode;
import com.android.tools.r8.R8RunArtTestsTest.CompilerUnderTest;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.junit.Test;

public class R8GMSCoreV9DeployJarVerificationTest extends GMSCoreDeployJarVerificationTest {

  @Test
  public void buildFromDeployJar()
      throws ExecutionException, IOException, ProguardRuleParserException, CompilationException {
    buildFromDeployJar(
        CompilerUnderTest.R8, CompilationMode.RELEASE,
        GMSCoreCompilationTestBase.GMSCORE_V9_DIR, true);
  }
}
