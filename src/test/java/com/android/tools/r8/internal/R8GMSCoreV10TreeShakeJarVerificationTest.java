// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.internal;

import com.android.tools.r8.CompilationMode;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.InternalOptions;
import org.junit.Test;

public class R8GMSCoreV10TreeShakeJarVerificationTest
    extends R8GMSCoreTreeShakeJarVerificationTest {

  @Test
  public void buildAndTreeShakeFromDeployJar() throws Exception {
    // TODO(tamaskenez): set hasReference = true when we have the noshrink file for V10
    buildAndTreeShakeFromDeployJar(
        CompilationMode.RELEASE, GMSCORE_V10_DIR, false, GMSCORE_V10_MAX_SIZE, null);
  }

  private void configureDeterministic(InternalOptions options) {
    options.skipMinification = true;
  }

  @Test
  public void deterministic() throws Exception {
    // TODO(sgjesse): When minification is deterministic remove this test and make the one above
    // check for deterministic output.
    AndroidApp app1 = buildAndTreeShakeFromDeployJar(
        CompilationMode.RELEASE, GMSCORE_V10_DIR, false, GMSCORE_V10_MAX_SIZE + 2000000,
        this::configureDeterministic);
    AndroidApp app2 = buildAndTreeShakeFromDeployJar(
        CompilationMode.RELEASE, GMSCORE_V10_DIR, false, GMSCORE_V10_MAX_SIZE + 2000000,
        this::configureDeterministic);

    // Verify that the result of the two compilations was the same.
    assertIdenticalApplications(app1, app2);
  }
}
