// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.debuginfo;

import static org.junit.Assert.assertEquals;

import com.android.tools.r8.utils.AndroidApp;
import org.junit.Test;

public class ArgumentLocalsInLoopTestRunner extends DebugInfoTestBase {

  @Test
  public void testArgumentLocalsInLoop() throws Exception {
    Class clazz = ArgumentLocalsInLoopTest.class;
    AndroidApp d8App = compileWithD8(clazz);
    AndroidApp dxApp = getDxCompiledSources();

    String expected = "0";
    assertEquals(expected, runOnJava(clazz));
    assertEquals(expected, runOnArt(d8App, clazz.getCanonicalName()));
    assertEquals(expected, runOnArt(dxApp, clazz.getCanonicalName()));

    checkFoo(inspectMethod(d8App, clazz, "int", "foo", "int"), clazz);
    checkFoo(inspectMethod(dxApp, clazz, "int", "foo", "int"), clazz);
  }

  private void checkFoo(DebugInfoInspector info, Class clazz) {
    String[] locals = {"this", clazz.getCanonicalName(), "x", "int"};
    info.checkStartLine(10);
    info.checkLineHasExactLocals(10, locals);
    info.checkLineHasExactLocals(11, locals);
    info.checkNoLine(12);
    info.checkLineHasExactLocals(13, locals);
    info.checkNoLine(14);
    info.checkNoLine(15);
  }
}
