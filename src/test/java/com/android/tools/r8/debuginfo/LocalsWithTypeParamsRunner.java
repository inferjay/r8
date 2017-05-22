// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.debuginfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.android.tools.r8.utils.AndroidApp;
import org.junit.Test;

public class LocalsWithTypeParamsRunner extends DebugInfoTestBase {

  static final Class clazzMain = LocalsWithTypeParamsTest.class;
  static final Class clazzA = A.class;
  static final Class clazzB = B.class;

  static final String nameMain = clazzMain.getCanonicalName();
  static final String nameA = clazzA.getCanonicalName();
  static final String nameB = clazzB.getCanonicalName();

  @Test
  public void testLocalsWithTypeParams() throws Exception {
    AndroidApp d8App = compileWithD8(clazzMain, clazzA, clazzB);
    AndroidApp dxApp = getDxCompiledSources();

    String expected = "42";
    assertEquals(expected, runOnJava(clazzMain));
    assertEquals(expected, runOnArt(d8App, nameMain));
    assertEquals(expected, runOnArt(dxApp, nameMain));

    checkSyncInstance(inspectMethod(d8App, clazzA, "int", "foo", nameB));
    checkSyncInstance(inspectMethod(dxApp, clazzA, "int", "foo", nameB));
  }

  private void checkSyncInstance(DebugInfoInspector info) {
    // Assert that the parameter entry is null since it is explicitly introduced in the stream.
    assertEquals(1, info.info.parameters.length);
    assertNull(info.info.parameters[0]);

    info.checkStartLine(8);
    info.checkLineHasExactLocals(8, "this", nameA, "b", nameB);
    info.checkLineHasLocal(8, "this", nameA, "TT;");
    info.checkLineHasLocal(8, "b", nameB, "TT;", "Ljava/lang/String;");

    info.checkLineHasExactLocals(9, "this", nameA, "b", nameB, "otherB", nameB);
    info.checkLineHasLocal(9, "this", nameA, "TT;");
    info.checkLineHasLocal(9, "b", nameB, "TT;", "Ljava/lang/String;");
    info.checkLineHasLocal(9, "otherB", nameB, "Ljava/lang/String;", "Ljava/lang/String;");
  }
}
