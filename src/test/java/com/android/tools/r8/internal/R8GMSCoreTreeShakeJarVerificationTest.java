// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.internal;

import static junit.framework.TestCase.assertTrue;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.CompilationMode;
import com.android.tools.r8.R8RunArtTestsTest.CompilerUnderTest;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.Resource;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class R8GMSCoreTreeShakeJarVerificationTest extends GMSCoreCompilationTestBase {

  public void buildAndTreeShakeFromDeployJar(
      CompilationMode mode, String base, boolean hasReference, int maxSize)
      throws ExecutionException, IOException, ProguardRuleParserException, CompilationException {
    AndroidApp app = runAndCheckVerification(
        CompilerUnderTest.R8,
        mode,
        hasReference ? base + REFERENCE_APK : null,
        null,
        base + PG_CONF,
        // Don't pass any inputs. The input will be read from the -injars in the Proguard
        // configuration file.
        ImmutableList.of());
    int bytes = 0;
    try (Closer closer = Closer.create()) {
      for (Resource dex : app.getDexProgramResources()) {
        bytes += ByteStreams.toByteArray(dex.getStream(closer)).length;
      }
    }
    assertTrue("Expected max size of " + maxSize + ", got " + bytes, bytes < maxSize);
  }
}
