// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.debuginfo;

import static org.junit.Assert.assertEquals;

import com.android.tools.r8.utils.AndroidApp;
import org.junit.Test;

public class ExceptionLocalTestRunner extends DebugInfoTestBase {

  @Test
  public void testExceptionLocal() throws Exception {
    Class clazz = ExceptionLocalTest.class;

    AndroidApp d8App = compileWithD8(clazz);
    AndroidApp dxApp = getDxCompiledSources();

    String expected = "2";
    assertEquals(expected, runOnJava(clazz));
    assertEquals(expected, runOnArt(d8App, clazz.getCanonicalName()));
    assertEquals(expected, runOnArt(dxApp, clazz.getCanonicalName()));

    checkExceptionLocal(inspectMethod(d8App, clazz, "void", "foo", "int"));
    checkExceptionLocal(inspectMethod(dxApp, clazz, "void", "foo", "int"));
  }

  private void checkExceptionLocal(DebugInfoInspector info) {
    String self = ExceptionLocalTest.class.getCanonicalName();
    String Integer = "java.lang.Integer";
    info.checkStartLine(10);
    info.checkLineHasExactLocals(10, "this", self, "x", "int");
    info.checkLineHasExactLocals(11, "this", self, "x", "int", "obj", Integer);
    info.checkNoLine(12);
    info.checkLineHasExactLocals(13, "this", self, "x", "int", "obj", Integer, "l", "long");
    info.checkLineHasExactLocals(14, "this", self, "x", "int", "obj", Integer, "l", "long");
    info.checkLineHasExactLocals(15, "this", self, "x", "int", "obj", Integer, "l", "long");
    info.checkLineHasExactLocals(18, "this", self, "x", "int", "obj", Integer, "l", "long",
        "e", "java.lang.ArithmeticException");
    info.checkLineHasExactLocals(20, "this", self, "x", "int", "obj", Integer, "l", "long",
        "e", "java.lang.RuntimeException");
    info.checkLineHasExactLocals(22, "this", self, "x", "int", "obj", Integer, "l", "long",
        "e", "java.lang.Throwable");
  }
}
