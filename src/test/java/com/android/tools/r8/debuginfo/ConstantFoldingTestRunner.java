// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.debuginfo;

import static org.junit.Assert.assertEquals;

import com.android.tools.r8.utils.AndroidApp;
import org.junit.Test;

public class ConstantFoldingTestRunner extends DebugInfoTestBase {

  @Test
  public void testLocalsInSwitch() throws Exception {
    Class clazz = ConstantFoldingTest.class;
    AndroidApp d8App = compileWithD8(clazz);
    AndroidApp dxApp = getDxCompiledSources();

    String expected = "42";
    assertEquals(expected, runOnJava(clazz));
    assertEquals(expected, runOnArt(d8App, clazz.getCanonicalName()));
    assertEquals(expected, runOnArt(dxApp, clazz.getCanonicalName()));

    checkFoo(inspectMethod(d8App, clazz, "int", "foo", "int"), false);
    checkFoo(inspectMethod(dxApp, clazz, "int", "foo", "int"), true);
  }

  private void checkFoo(DebugInfoInspector info, boolean dx) {
    info.checkStartLine(9);
    info.checkLineHasExactLocals(9, "x", "int");
    info.checkNoLine(10);
    info.checkLineHasExactLocals(11, "x", "int", "res", "int");
    info.checkLineHasExactLocals(12, "x", "int", "res", "int", "tmp", "int");
    info.checkNoLine(13);
    info.checkLineHasAtLeastLocals(14, "x", "int");
    if (!dx) {
      // DX fails to close the scope of "tmp".
      info.checkLineHasExactLocals(14, "x", "int", "res", "int");
    }
    info.checkNoLine(15);
  }
}
