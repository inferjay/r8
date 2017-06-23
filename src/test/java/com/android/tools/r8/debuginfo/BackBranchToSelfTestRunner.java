// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.debuginfo;

import static org.junit.Assert.assertEquals;

import com.android.tools.r8.utils.AndroidApp;
import org.junit.Test;

public class BackBranchToSelfTestRunner extends DebugInfoTestBase {

  @Test
  public void testBackBranchToSelf() throws Exception {
    Class clazz = BackBranchToSelfTest.class;

    AndroidApp d8App = compileWithD8(clazz);
    AndroidApp dxApp = getDxCompiledSources();

    String expected = "42";
    assertEquals(expected, runOnJava(clazz));
    assertEquals(expected, runOnArt(d8App, clazz.getCanonicalName()));
    assertEquals(expected, runOnArt(dxApp, clazz.getCanonicalName()));

    checkBackBranchToSelf(inspectMethod(d8App, clazz, "int", "backBranchToSelf", "boolean"), false);
    checkBackBranchToSelf(inspectMethod(dxApp, clazz, "int", "backBranchToSelf", "boolean"), true);
  }

  private void checkBackBranchToSelf(DebugInfoInspector info, boolean dx) {
    info.checkStartLine(10);
    info.checkLineHasExactLocals(10, "loop", "boolean");
    info.checkNoLine(11);
    info.checkNoLine(12);
    info.checkLineHasExactLocals(13, "loop", "boolean");
    info.checkLineHasExactLocals(14, "loop", "boolean");
  }
}
