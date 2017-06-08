// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.jasmin;

import static org.junit.Assert.assertEquals;

import com.android.tools.r8.ToolHelper;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class TryCatchStateTests extends JasminTestBase {

  @Test
  public void testTryCatchStackHeight() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    clazz.addStaticMethod("foo", ImmutableList.of("I"), "I",
        ".limit stack 5",
        ".limit locals 1",
        "  ldc 42",
        "  ldc 12",
        "  iload 0", // {42, 12, i}
        "LabelTryStart:",
        "  idiv", // {42, 12/i}
        "LabelTryEnd:",
        "  swap",
        "  pop", // {12/i}
        "  goto LabelRet",
        "LabelCatch:", // Entry stack is {java/lang/Throwable}
        "  pop",
        "  ldc 0", // {0}
        "  goto LabelRet",
        "LabelRet:",
        "  ireturn",
        ".catch java/lang/Exception from LabelTryStart to LabelTryEnd using LabelCatch"
    );

    clazz.addMainMethod(
        ".limit stack 2",
        ".limit locals 1",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  ldc 0",
        "  invokestatic Test/foo(I)I",
        "  invokevirtual java/io/PrintStream/println(I)V",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  ldc 2",
        "  invokestatic Test/foo(I)I",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  return");

    String expected = "0" + ToolHelper.LINE_SEPARATOR + "6";
    String javaResult = runOnJava(builder, clazz.name);
    assertEquals(expected, javaResult);
    String artResult = runOnArt(builder, clazz.name);
    assertEquals(expected, artResult);
  }

  @Test
  public void testTryCatchLocals() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    clazz.addStaticMethod("foo", ImmutableList.of("I"), "I",
        ".limit stack 5",
        ".limit locals 2",
        "  ldc 42",
        "  ldc 12",
        "  iload 0", // {42, 12, i}
        "  ldc 0",
        "  istore 1", // Must initialize local before entry to try-catch block.
        "LabelTryStart:",
        "  swap",
        "  istore 1",
        "  idiv", // {42/i}
        "LabelTryEnd:",
        "  goto LabelRet",
        "LabelCatch:", // Entry stack is {java/lang/Throwable}
        "  pop",
        "  iload 1", // {12}
        "  goto LabelRet",
        "LabelRet:",
        "  ireturn",
        ".catch java/lang/Exception from LabelTryStart to LabelTryEnd using LabelCatch"
    );

    clazz.addMainMethod(
        ".limit stack 2",
        ".limit locals 1",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  ldc 0",
        "  invokestatic Test/foo(I)I",
        "  invokevirtual java/io/PrintStream/println(I)V",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  ldc 2",
        "  invokestatic Test/foo(I)I",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  return");

    String expected = "12" + ToolHelper.LINE_SEPARATOR + "21";
    String javaResult = runOnJava(builder, clazz.name);
    assertEquals(expected, javaResult);
    String artResult = runOnArt(builder, clazz.name);
    assertEquals(expected, artResult);
  }

  @Test
  public void testTryCatchOnUnreachableLabel() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    clazz.addStaticMethod("foo", ImmutableList.of("I"), "I",
        ".limit stack 5",
        ".limit locals 2",
        "  ldc 42",
        "  ldc 12",
        "  iload 0", // {42, 12, i}
        "  ldc 0",
        "  istore 1", // Must initialize local before entry to try-catch block.
        "  goto RealStart",
        "LabelTryStart:", // Start the catch range on unreachable label.
        "  goto RealStart",
        "RealStart:",
        "  swap",
        "  istore 1",
        "  idiv", // {42/i}
        "LabelTryEnd:",
        "  goto LabelRet",
        "LabelCatch:", // Entry stack is {java/lang/Throwable}
        "  pop",
        "  iload 1", // {12}
        "  goto LabelRet",
        "LabelRet:",
        "  ireturn",
        ".catch java/lang/Exception from LabelTryStart to LabelTryEnd using LabelCatch"
    );

    clazz.addMainMethod(
        ".limit stack 2",
        ".limit locals 1",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  ldc 0",
        "  invokestatic Test/foo(I)I",
        "  invokevirtual java/io/PrintStream/println(I)V",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  ldc 2",
        "  invokestatic Test/foo(I)I",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  return");

    String expected = "12" + ToolHelper.LINE_SEPARATOR + "21";
    String javaResult = runOnJava(builder, clazz.name);
    assertEquals(expected, javaResult);
    String artResult = runOnArt(builder, clazz.name);
    assertEquals(expected, artResult);
  }
}
