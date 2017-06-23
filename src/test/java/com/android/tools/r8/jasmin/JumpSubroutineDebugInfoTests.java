// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.jasmin;

import static org.junit.Assert.assertEquals;

import com.android.tools.r8.debuginfo.DebugInfoInspector;
import com.android.tools.r8.naming.MemberNaming.MethodSignature;
import com.android.tools.r8.utils.AndroidApp;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class JumpSubroutineDebugInfoTests extends JasminTestBase {

  @Test
  public void testJsrWithStraightlineAndDebugInfoCode() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    MethodSignature foo = clazz.addStaticMethod("foo", ImmutableList.of("I"), "I",
        ".limit stack 3",
        ".limit locals 3",
        ".var 0 is x I from LabelInit to LabelExit",
        "LabelInit:",
        ".line 1",
        "  ldc 0",
        "  ldc 1",
        "  jsr LabelSub",
        "  ldc 2",
        "  jsr LabelSub",
        "  ldc 3",
        "  jsr LabelSub",
        ".line 2",
        "  ireturn",
        "LabelSub:",
        ".line 3",
        "  astore 1",
        "  iadd",
        "  istore 0", // store and load in local 'x' to ensure we don't optimize out the subroutine.
        ".line 4",
        "  iload 0",
        "  ret 1",
        "LabelExit:");

    clazz.addMainMethod(
        ".limit stack 3",
        ".limit locals 1",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  ldc 1",
        "  invokestatic Test/foo(I)I",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  return");

    String expected = "6";
    String javaResult = runOnJava(builder, clazz.name);
    assertEquals(expected, javaResult);
    AndroidApp d8App = compileWithD8(builder);
    String artResult = runOnArt(d8App, clazz.name);
    assertEquals(expected, artResult);

    DebugInfoInspector info = new DebugInfoInspector(d8App, clazz.name, foo);
    info.checkStartLine(1);
    // Check the subroutine line is duplicated 3 times.
    assertEquals(3, info.checkLineHasExactLocals(3, "x", "int"));
  }
}
