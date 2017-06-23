// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.internal;

import static org.junit.Assert.assertEquals;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.android.tools.r8.utils.AndroidApp;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import org.junit.Test;

public class R8GMSCoreFixedPointTest extends GMSCoreCompilationTestBase {

  @Test
  public void fixedPoint()
      throws ExecutionException, IOException, ProguardRuleParserException, CompilationException {
    // First compilation.
    AndroidApp app = AndroidApp.fromProgramDirectory(Paths.get(GMSCORE_V7_DIR));
    AndroidApp app1 =
        ToolHelper.runR8(app, options -> options.minApiLevel = Constants.ANDROID_L_API);

    // Second compilation.
    // Add option --skip-outline-opt for second compilation. The second compilation can find
    // additional outlining opportunities as member rebinding from the first compilation can move
    // methods.
    // See b/33410508 and b/33475705.
    AndroidApp app2 = ToolHelper.runR8(app1, options -> {
      options.outline.enabled = false;
      options.minApiLevel = Constants.ANDROID_L_API;
    });

    // TODO: Require that the results of the two compilations are the same.
    assertEquals(
        app1.getDexProgramResources().size(),
        app2.getDexProgramResources().size());
  }
}
