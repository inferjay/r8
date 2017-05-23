// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.internal;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.CompilationMode;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.junit.Test;

public class R8GMSCoreV10TreeShakeJarVerificationTest
    extends R8GMSCoreTreeShakeJarVerificationTest {

  @Test
  public void buildAndTreeShakeFromDeployJar()
      throws ExecutionException, IOException, ProguardRuleParserException, CompilationException {
    // TODO(tamaskenez): set hasReference = true when we have the noshrink file for V10
    buildAndTreeShakeFromDeployJar(
        CompilationMode.RELEASE, GMSCORE_V10_DIR, false, GMSCORE_V10_MAX_SIZE);
  }
}
