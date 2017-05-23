// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.internal;

import static org.junit.Assert.assertTrue;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.CompilationMode;
import com.android.tools.r8.R8RunArtTestsTest.CompilerUnderTest;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.InternalResource;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.junit.Ignore;
import org.junit.Test;

public class YouTubeTreeShakeJarVerificationTest extends YouTubeCompilationBase {

  @Test
  @Ignore("b/35656577")
  public void buildAndTreeShakeFromDeployJar()
      throws ExecutionException, IOException, ProguardRuleParserException, CompilationException {
    int maxSize = 16000000;
    AndroidApp app = runAndCheckVerification(
        CompilerUnderTest.R8, CompilationMode.RELEASE,
        BASE + APK, null, BASE + PG_CONF, BASE + DEPLOY_JAR);
    int bytes = 0;
    try (Closer closer = Closer.create()) {
      for (InternalResource dex : app.getDexProgramResources()) {
        bytes += ByteStreams.toByteArray(dex.getStream(closer)).length;
      }
    }
    assertTrue("Expected max size of " + maxSize + ", got " + bytes, bytes < maxSize);
  }
}
