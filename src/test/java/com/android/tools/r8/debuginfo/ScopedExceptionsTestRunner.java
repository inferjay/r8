// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.debuginfo;

import static org.junit.Assert.assertEquals;

import com.android.tools.r8.utils.AndroidApp;
import org.junit.Test;

public class ScopedExceptionsTestRunner extends DebugInfoTestBase {

  @Test
  public void testScopedException() throws Exception {
    Class clazz = ScopedExceptionsTest.class;
    AndroidApp d8App = compileWithD8(clazz);
    AndroidApp dxApp = getDxCompiledSources();

    String expected = "42";
    assertEquals(expected, runOnJava(clazz));
    assertEquals(expected, runOnArt(d8App, clazz.getCanonicalName()));
    assertEquals(expected, runOnArt(dxApp, clazz.getCanonicalName()));

    checkScopedExceptions(inspectMethod(d8App, clazz, "int", "scopedExceptions"), false);
    checkScopedExceptions(inspectMethod(dxApp, clazz, "int", "scopedExceptions"), true);
  }

  private void checkScopedExceptions(DebugInfoInspector info, boolean dx) {
    info.checkStartLine(10);
    info.checkLineHasNoLocals(10);
    info.checkNoLine(11);
    info.checkLineHasNoLocals(12);
    info.checkLineHasNoLocals(13);
    info.checkLineHasExactLocals(14, "e", "java.lang.Throwable");
    // DX does not generate a position at the end of the try-catch blocks, Java does and so does D8.
    if (!dx) {
      info.checkLineHasNoLocals(15);
    }
    info.checkLineExists(16);
    // DX will still have an local entry for 'e' after its scope has ended.
    if (!dx) {
      info.checkLineHasNoLocals(16);
    }
  }
}
