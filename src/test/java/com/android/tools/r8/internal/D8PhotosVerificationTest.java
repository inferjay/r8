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

public class D8PhotosVerificationTest extends CompilationTestBase {
  public static final String PHOTOS =
      "third_party/photos/2017-06-06/PhotosEnglishOnlyLegacy_proguard.jar";

  public void runD8AndCheckVerification(CompilationMode mode, String version)
      throws ProguardRuleParserException, ExecutionException, IOException, CompilationException {
    runAndCheckVerification(
        CompilerUnderTest.D8, mode, version, null, null, version);
  }

  @Test
  public void verify()
      throws ExecutionException, IOException, ProguardRuleParserException, CompilationException {
    runD8AndCheckVerification(CompilationMode.RELEASE, PHOTOS);
  }
}
