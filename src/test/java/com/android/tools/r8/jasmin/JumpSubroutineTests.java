// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.jasmin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.ToolHelper.ProcessResult;
import com.android.tools.r8.utils.AndroidApp;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class JumpSubroutineTests extends JasminTestBase {

  private void runTest(JasminBuilder builder, String main, String expected) throws Exception {
    String javaResult = runOnJava(builder, main);
    assertEquals(expected, javaResult);
    String artResult = runOnArt(builder, main);
    assertEquals(expected, artResult);
    String dxArtResult = runOnArtDx(builder, main);
    assertEquals(expected, dxArtResult);
  }

  private void expectDxFailure(JasminBuilder builder) throws Exception {
    // This expects this dx failure:
    // Uncaught translation error: com.android.dex.util.ExceptionWithContext: returning from
    // invalid subroutine
    // 1 error; aborting
    ProcessResult result = runOnArtDxRaw(builder);
    assertNotEquals(0, result.exitCode);
    assertTrue(result.stderr.contains("Uncaught translation error"));
    assertTrue(result.stderr.contains("invalid subroutine"));
  }

  @Test
  /*
   *  Compilation of the following code with JDK 1.3.0_05 (on Windows).
   *
   *  package test;
   *
   *  class Test {
   *    public static void main(String[] args) {
   *      try {
   *        System.out.println(0);
   *      } finally {
   *        System.out.println(2);
   *      }
   *    }
   *  }
   */
  public void testJsrJava130TryFinally() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    clazz.addMainMethod(
        ".limit stack 3",
        ".limit locals 3",
        "TryStart:",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  iconst_0",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  jsr Finally",
        "  goto Return",
        "TryEnd:",
        "Catch:",
        "  astore_1",
        "  jsr Finally",
        "  aload_1",
        "  athrow",
        "Finally:",
        "  astore_2",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  iconst_1",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  ret 2",
        "Return:",
        "  return",
        ".catch all from TryStart to TryEnd using Catch");

    runTest(builder, clazz.name, "01");
  }

  @Test
  /*
   *  Compilation of the following code with JDK 1.3.0_05 (on Windows).
   *
   *  package test;
   *
   *  class Test {
   *    public static void main(String[] args) {
   *      try {
   *        System.out.println(0);
   *        try {
   *          System.out.println(1);
   *        } finally {
   *          System.out.println(2);
   *        }
   *      } finally {
   *        System.out.println(3);
   *      }
   *    }
   *  }
   */
  public void testJsrJava130TryFinallyNested() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    clazz.addMainMethod(
        ".limit stack 3",
        ".limit locals 5",
        "TryStart:",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  iconst_0",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "TryStartInner:",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  iconst_1",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  jsr FinallyInner",
        "  goto DoneInner",
        "TryEndInner:",
        "CatchInner:",
        "  astore_1",
        "  jsr FinallyInner",
        "  aload_1",
        "  athrow",
        "FinallyInner:",
        "  astore_2",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  iconst_2",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  ret 2",
        "DoneInner:",
        "  jsr Finally",
        "  goto Return",
        "TryEnd:",
        "Catch:",
        "  astore_3",
        "  jsr Finally",
        "  aload_3",
        "  athrow",
        "Finally:",
        "  astore 4",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  iconst_3",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  ret 4",
        "Return:",
        "  return",
        ".catch all from TryStartInner to TryEndInner using CatchInner",
        ".catch all from TryStart to TryEnd using Catch");


    runTest(builder, clazz.name, "0123");
  }

  @Test
  public void testJsrWithStraightlineCode() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    clazz.addStaticMethod("foo", ImmutableList.of(), "I",
        ".limit stack 3",
        ".limit locals 3",
        "  ldc 0",
        "  ldc 1",
        "  jsr LabelSub",
        "  ldc 2",
        "  jsr LabelSub",
        "  ldc 3",
        "  jsr LabelSub",
        "  ireturn",
        "LabelSub:",
        "  astore 1",
        "  iadd",
        "  ret 1");

    clazz.addMainMethod(
        ".limit stack 2",
        ".limit locals 1",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  invokestatic Test/foo()I",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  return");

    runTest(builder, clazz.name, Integer.toString(3 * 4 / 2));
  }

  @Test
  public void testJsrWithStraightlineCodeMultiple() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    clazz.addStaticMethod("foo", ImmutableList.of(), "I",
        ".limit stack 3",
        ".limit locals 3",
        "  ldc 0",
        "  ldc 1",
        "  jsr LabelSub1",
        "  ldc 2",
        "  jsr LabelSub2",
        "  ldc 3",
        "  jsr LabelSub3",
        "  ireturn",
        "LabelSub1:",
        "  astore 1",
        "  iadd",
        "  ret 1",
        "LabelSub2:",
        "  astore 1",
        "  iadd",
        "  ret 1",
        "LabelSub3:",
        "  astore 1",
        "  iadd",
        "  ret 1");

    clazz.addMainMethod(
        ".limit stack 2",
        ".limit locals 1",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  invokestatic Test/foo()I",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  return");

    runTest(builder, clazz.name, Integer.toString(3 * 4 / 2));
  }

  @Test
  public void testJsrWithStraightlineCodeMultiple2() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    clazz.addStaticMethod("foo", ImmutableList.of(), "I",
        ".limit stack 4",
        ".limit locals 3",
        "  ldc 0",
        "  ldc 1",
        "  jsr LabelSub",
        "  ldc 2",
        "  jsr LabelSub",
        "  ldc 3",
        "  jsr LabelSub",
        "  ldc 4",
        "  ldc 5",
        "  jsr LabelSub2",
        "  ldc 6",
        "  ldc 7",
        "  jsr LabelSub2",
        "  ldc 8",
        "  ldc 9",
        "  jsr LabelSub2",
        "  ireturn",
        "LabelSub:",
        "  astore 1",
        "  iadd",
        "  ret 1",
        "LabelSub2:",
        "  astore 1",
        "  iadd",
        "  iadd",
        "  ret 1");

    clazz.addMainMethod(
        ".limit stack 2",
        ".limit locals 1",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  invokestatic Test/foo()I",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  return");

    runTest(builder, clazz.name, Integer.toString(9 * 10 / 2));
  }

  @Test
  public void testJsrWithControlFlowCode() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    clazz.addStaticMethod("foo", ImmutableList.of(), "V",
        ".limit stack 2",
        ".limit locals 2",
        "  ldc 0",
        "  jsr LabelSub",
        "  ldc 1",
        "  jsr LabelSub",
        "  return",
        "LabelSub:",
        "  astore 1",
        "  ifeq LabelZero",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  ldc \"Got non-zero\"",
        "  invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V",
        "  goto LabelRet",
        "LabelZero:",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  ldc \"Got zero\"",
        "  invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V",
        "LabelRet:",
        "  ret 1");

    clazz.addMainMethod(
        ".limit stack 0",
        ".limit locals 1",
        "  invokestatic Test/foo()V",
        "  return");

    runTest(builder, clazz.name, "Got zero" + ToolHelper.LINE_SEPARATOR + "Got non-zero"
            + ToolHelper.LINE_SEPARATOR);
  }

  @Test
  public void testJsrWithNestedJsr() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    clazz.addStaticMethod("foo", ImmutableList.of(), "V",
        ".limit stack 2",
        ".limit locals 3",
        "  ldc 0",
        "  jsr LabelSub",  // index 1.
        "  ldc 1",
        "  jsr LabelSub",  // index 3.
        "  return",
        "LabelSub:",
        "  astore 1",
        "  ifeq LabelZero",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  ldc \"Got non-zero, calling nested\"",
        "  invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V",
        "  jsr LabelSub2",  // index 11.
        "  goto LabelRet",
        "LabelZero:",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  ldc \"Got zero\"",
        "  invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V",
        "LabelRet:",
        "  ret 1",
        "LabelSub2:",
        "  astore 2",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  ldc \"In nested subroutine\"",
        "  invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V",
        "  ret 2");

    clazz.addMainMethod(
        ".limit stack 0",
        ".limit locals 1",
        "  invokestatic Test/foo()V",
        "  return");

    runTest(builder, clazz.name, "Got zero" + ToolHelper.LINE_SEPARATOR
            + "Got non-zero, calling nested" + ToolHelper.LINE_SEPARATOR + "In nested subroutine"
            + ToolHelper.LINE_SEPARATOR);
  }

  @Test
  public void testJsrWithNestedJsrPopReturnAddress() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    clazz.addStaticMethod("foo", ImmutableList.of(), "V",
        ".limit stack 1",
        ".limit locals 1",
        "  jsr LabelSub1",
        "  return",
        "LabelSub1:",
        "  astore 0",
        "  jsr LabelSub2",
        "LabelSub2:",
        "  pop",
        "  ret 0");

    clazz.addMainMethod(
        ".limit stack 1",
        ".limit locals 1",
        "  invokestatic Test/foo()V",
        "  return");

    String expected = "";
    String javaResult = runOnJava(builder, clazz.name);
    assertEquals(expected, javaResult);
    String artResult = runOnArt(builder, clazz.name);
    assertEquals(expected, artResult);
    // This fails with dx.
    expectDxFailure(builder);
  }

  @Test
  public void testJsrWithNestedPopReturnAddress2() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    clazz.addStaticMethod("foo", ImmutableList.of(), "V",
        ".limit stack 1",
        ".limit locals 1",
        "  jsr LabelSub1",
        "LabelSub1:",
        "  pop",
        "  jsr LabelSub2",
        "  return",
        "LabelSub2:",
        "  astore 0",
        "  ret 0");

    clazz.addMainMethod(
        ".limit stack 1",
        ".limit locals 1",
        "  invokestatic Test/foo()V",
        "  return");

    runTest(builder, clazz.name, "");
  }

  @Test
  public void testJsrJustThrows() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    clazz.addStaticMethod("foo", ImmutableList.of(), "V",
        ".limit stack 2",
        ".limit locals 1",
        "  jsr Jsr",
        // The return target is the same as the jsr target.
        "Jsr:",
        "  pop",  // Return address is not used.
        "  new java/lang/Exception",
        "  dup",
        "  invokenonvirtual java/lang/Exception.<init>()V",
        "  athrow");

    clazz.addMainMethod(
        ".limit stack 1",
        ".limit locals 1",
        "TryStart:",
        "  invokestatic Test/foo()V",
        "  return",
        "TryEnd:",
        "Catch:",
        "  pop",
        "  return",
        ".catch java/lang/Exception from TryStart to TryEnd using Catch");

    runTest(builder, clazz.name, "");
  }

  @Test
  public void testJsrJustThrows2() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    clazz.addStaticMethod("foo", ImmutableList.of(), "V",
        ".limit stack 2",
        ".limit locals 1",
        "  jsr Jsr",
        // The jsr does not return, so this is dead code.
        "  pop",
        "  pop",
        "  pop",
        "  pop",
        "  pop",
        "  pop",
        "  return",
        "Jsr:",
        "  pop",  // Return address is not used.
        "  new java/lang/Exception",
        "  dup",
        "  invokenonvirtual java/lang/Exception.<init>()V",
        "  athrow");

    clazz.addMainMethod(
        ".limit stack 1",
        ".limit locals 1",
        "TryStart:",
        "  invokestatic Test/foo()V",
        "  return",
        "TryEnd:",
        "Catch:",
        "  pop",
        "  return",
        ".catch java/lang/Exception from TryStart to TryEnd using Catch");

    runTest(builder, clazz.name, "");
  }

  @Test
  public void testJsrWithException() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    clazz.addStaticMethod("foo", ImmutableList.of(), "V",
        ".limit stack 3",
        ".limit locals 2",
        "  ldc 0",
        "  jsr LabelSub",
        "  ldc 1",
        "  jsr LabelSub",
        "  return",
        "LabelSub:",
        "  astore 1",
        "  ldc 42",
        "  swap",
        "  ldc 42",
        "  swap",
        "LabelTryStart:",
        "  idiv",
        "  pop",
        "LabelTryEnd:",
        "  pop",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  ldc \"Divided by non-zero\"",
        "  invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V",
        "  goto LabelRet",
        "LabelCatch:",
        "  pop",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  ldc \"Divided by zero\"",
        "  invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V",
        "  goto LabelRet",
        "LabelRet:",
        "  ret 1",
        ".catch java/lang/Exception from LabelTryStart to LabelTryEnd using LabelCatch");

    clazz.addMainMethod(
        ".limit stack 0",
        ".limit locals 1",
        "  invokestatic Test/foo()V",
        "  return");

    runTest(builder, clazz.name, "Divided by zero" + ToolHelper.LINE_SEPARATOR
            + "Divided by non-zero" + ToolHelper.LINE_SEPARATOR);
  }

  @Test
  public void testJsrWithAddressManipulation() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    clazz.addStaticMethod("foo", ImmutableList.of(), "V",
        ".limit stack 4",
        ".limit locals 4",
        "  ldc 0",
        "  jsr LabelSub",
        "  ldc 1",
        "  jsr LabelSub",
        "  return",
        "LabelSub:",
        "  ldc \"junk\"",
        "  swap",
        "  dup",
        // stack is now ..., arg, "junk", addr, addr
        "  astore 3",
        "  astore 2",
        "  astore 1",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  swap",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  ret 2");

    clazz.addMainMethod(
        ".limit stack 0",
        ".limit locals 1",
        "  invokestatic Test/foo()V",
        "  return");

    runTest(builder, clazz.name, "01");
  }

  @Test
  public void testJsrWithSharedExceptionHandler() throws Exception {
    // Regression test for b/37659886
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    clazz.addStaticMethod("foo", ImmutableList.of("I"), "V",
        ".limit stack 4",
        ".limit locals 2",
        "LabelTryStart:",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  iload 0",
        "  jsr LabelSub",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  return",
        "LabelSub:",
        "  astore 1",
        "  dup",
        "  ldc 168",
        "  swap",
        "  idiv", // First throwing shares the handler with JSR.
        "LabelTryEnd1:",
        "  swap",
        "  idiv", // Second throwing is in the outer handler, but still opened at the point of JSR.
        "  ret 1",
        "LabelTryEnd2:",
        "LabelCatch1:",
        "  return",
        "LabelCatch2:",
        "  return",
        ".catch java/lang/Exception from LabelTryStart to LabelTryEnd1 using LabelCatch1",
        ".catch java/lang/Exception from LabelTryStart to LabelTryEnd2 using LabelCatch2");

    clazz.addMainMethod(
        ".limit stack 1",
        ".limit locals 1",
        "  ldc 2",
        "  invokestatic Test/foo(I)V",
        "  return");

    runTest(builder, clazz.name, "42");
  }

  @Test
  public void regressJsrHitParentCatchHandler() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    clazz.addStaticMethod("test", ImmutableList.of(), "I",
        ".limit stack 2",
        ".limit locals 2",
        "  ldc 10",
        "  istore 1",
        "TryStart:",
        "  iinc 1 -1",
        "  iload 1",
        "  ifeq LabelReturn",
        "  jsr Jsr",
        "  goto LabelReturn",
        "Jsr:",
        "  astore 0",
        "  new java/lang/Exception",
        "  dup",
        "  invokenonvirtual java/lang/Exception.<init>()V",
        "  athrow",
        "  ret 0",
        "LabelReturn:",
        "  ldc 0",
        "  ireturn",
        "Catch:",
        "  pop",
        "  goto TryStart",
        "TryEnd:",
        ".catch java/lang/Exception from TryStart to TryEnd using Catch"
    );

    clazz.addMainMethod(
        ".limit stack 2",
        ".limit locals 1",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  invokestatic Test/test()I",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  return");

    runTest(builder, clazz.name, "0");
  }

  private void generateRegressJsrHitParentCatchHandler2(
      JasminBuilder.ClassBuilder clazz, String name, String exception, boolean decrementInOuter) {
    String outer = decrementInOuter ? "  iinc 0 -1" : "";
    String inner = decrementInOuter ? "" : "  iinc 0 -1";
    clazz.addStaticMethod(name, ImmutableList.of(), "I",
        ".limit stack 3",
        ".limit locals 1",
        "  ldc 10",
        "  istore 0",
        "TryStart:",
        outer,
        "  iload 0",
        "  ifeq LabelReturn1",
        "  jsr Jsr1",
        "Jsr1:",
        "  pop",  // Return value is not used.
        "Jsr1Retry:",
        inner,
        "  iload 0",
        "  ifeq LabelReturn0",
        "  jsr Jsr2",
        "  ldc 2",
        "  ireturn",
        "Jsr2:",
        "  pop",  // Return value is not used.
        "  new " + exception,
        "  dup",
        "  invokenonvirtual " + exception + ".<init>()V",
        "  athrow",
        "Jsr2Catch:",
        "  pop",
        "  goto Jsr1Retry",
        "Jsr2End:",
        "LabelReturn0:",
        "  ldc 0",
        "  ireturn",
        "LabelReturn1:",
        "  ldc 1",
        "  ireturn",
        "Catch:",
        "  pop",
        "  goto TryStart",
        "TryEnd:",
        ".catch java/lang/Exception from Jsr2 to Jsr2End using Jsr2Catch",
        ".catch java/lang/Throwable from TryStart to TryEnd using Catch"
    );
  }

  @Test
  public void regressJsrHitParentCatchHandler2() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    generateRegressJsrHitParentCatchHandler2(clazz, "test1", "java/lang/Exception", false);
    generateRegressJsrHitParentCatchHandler2(clazz, "test2", "java/lang/Throwable", true);

    clazz.addMainMethod(
        ".limit stack 2",
        ".limit locals 1",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  invokestatic Test/test1()I",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  invokestatic Test/test2()I",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  return");

    runTest(builder, clazz.name, "01");
  }

  @Test
  // Reduction of b/38156139 causing the infinite loop.
  //
  // The original code is this method:
  // https://github.com/cbeust/testng/blob/4a8459e36f2b0ed057ffa7e470f1057e8e5b0ff9/src/main/java/org/testng/internal/Invoker.java#L1066
  // compiled with some ancient version of javac generating code with jsr for try/finally.
  public void regress38156139() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    clazz.addStaticMethod("foo", ImmutableList.of("Z"), "I",
        ".limit stack 2",
        ".limit locals 3",
        "  ldc 10",
        "  istore 2",
        "LabelLoopStart:",
        "  iinc 2 -1",
        "  iload 2",
        "  ifeq LabelReturn",
        "LabelTryStart:",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  iload 2",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  jsr LabelFinally",
        "  goto LabelLoopStart",
        "LabelTryEnd:",
        "LabelReturn:",
        "  ldc 0",
        "  ireturn",
        "LabelCatch:",
        "  pop",
        "  jsr LabelFinally",
        "  goto LabelLoopStart",
        "LabelFinally:",
        "  astore 1",
        "  iload 0",
        "  ifeq LabelZero",
        "  goto LabelLoopStart",  // Jump to loop start without invoking ret.
        "LabelZero:",
        "  ret 1",  // Invoke ret, which will also continue at the loop start.
        ".catch java/lang/Exception from LabelTryStart to LabelTryEnd using LabelCatch");

    clazz.addMainMethod(
        ".limit stack 2",
        ".limit locals 1",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  ldc 0",
        "  invokestatic Test/foo(Z)I",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  ldc 1",
        "  invokestatic Test/foo(Z)I",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  return");

    runTest(builder, clazz.name, "98765432109876543210");
  }

  @Test
  public void regress37767254() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    // This is the code for the method org.apache.log4j.net.SocketAppender$Connector.run() from
    // log4j version 1.2.
    //
    // https://github.com/apache/log4j/blob/v1_2-branch/src/main/java/org/apache/log4j/net/SocketAppender.java#L373
    //
    clazz.addVirtualMethod("run", ImmutableList.of(), "V",
        ".limit stack 4",
        ".limit locals 4",
        ".var 0 is this Lorg/apache/log4j/net/SocketAppender$Connector; from L0 to L26",
        ".var 1 is socket Ljava/net/Socket; from L5 to L13",
        ".var 2 is e Ljava/io/IOException; from L22 to L1",
        "L0:",
        ".line 367",
        "  goto L1",
        "L2:",
        ".line 368",
        ".line 369",
        "  aload 0",
        "  getfield org/apache/log4j/net/SocketAppender$Connector.this$0 Lorg/apache/log4j/net/SocketAppender;",
        "  getfield org/apache/log4j/net/SocketAppender.reconnectionDelay I",
        "  i2l",
        "  invokestatic java/lang/Thread.sleep(J)V",
        "L3:",
        ".line 370",
        "  new java/lang/StringBuffer",
        "  dup",
        "  ldc \"Attempting connection to \"",
        "  invokenonvirtual java/lang/StringBuffer.<init>(Ljava/lang/String;)V",
        "  aload 0",
        "  getfield org/apache/log4j/net/SocketAppender$Connector.this$0 Lorg/apache/log4j/net/SocketAppender;",
        "  getfield org/apache/log4j/net/SocketAppender.address Ljava/net/InetAddress;",
        "  invokevirtual java/net/InetAddress.getHostName()Ljava/lang/String;",
        "  invokevirtual java/lang/StringBuffer.append(Ljava/lang/String;)Ljava/lang/StringBuffer;",
        "  invokevirtual java/lang/StringBuffer.toString()Ljava/lang/String;",
        "  invokestatic org/apache/log4j/helpers/LogLog.debug(Ljava/lang/String;)V",
        "L4:",
        ".line 371",
        "  new java/net/Socket",
        "  dup",
        "  aload 0",
        "  getfield org/apache/log4j/net/SocketAppender$Connector.this$0 Lorg/apache/log4j/net/SocketAppender;",
        "  getfield org/apache/log4j/net/SocketAppender.address Ljava/net/InetAddress;",
        "  aload 0",
        "  getfield org/apache/log4j/net/SocketAppender$Connector.this$0 Lorg/apache/log4j/net/SocketAppender;",
        "  getfield org/apache/log4j/net/SocketAppender.port I",
        "  invokenonvirtual java/net/Socket.<init>(Ljava/net/InetAddress;I)V",
        "  astore 1",
        "L5:",
        ".line 372",
        "  aload 0",
        "  astore 2",
        "  aload 2",
        "  monitorenter",
        "L6:",
        ".line 373",
        "  aload 0",
        "  getfield org/apache/log4j/net/SocketAppender$Connector.this$0 Lorg/apache/log4j/net/SocketAppender;",
        "  new java/io/ObjectOutputStream",
        "  dup",
        "  aload 1",
        "  invokevirtual java/net/Socket.getOutputStream()Ljava/io/OutputStream;",
        "  invokenonvirtual java/io/ObjectOutputStream.<init>(Ljava/io/OutputStream;)V",
        "  putfield org/apache/log4j/net/SocketAppender.oos Ljava/io/ObjectOutputStream;",
        "L7:",
        ".line 374",
        "  aload 0",
        "  getfield org/apache/log4j/net/SocketAppender$Connector.this$0 Lorg/apache/log4j/net/SocketAppender;",
        "  aconst_null",
        "  invokestatic org/apache/log4j/net/SocketAppender.access$1(Lorg/apache/log4j/net/SocketAppender;Lorg/apache/log4j/net/SocketAppender$Connector;)V",
        "L8:",
        ".line 375",
        "  ldc \"Connection established. Exiting connector thread.\"",
        "  invokestatic org/apache/log4j/helpers/LogLog.debug(Ljava/lang/String;)V",
        "L9:",
        ".line 376",
        "  jsr L10",
        "  goto L11",
        "L12:",
        ".line 372",
        "  aload 2",
        "  monitorexit",
        "  athrow",
        "L10:",
        "  astore 3",
        "  aload 2",
        "  monitorexit",
        "  ret 3",
        "L13:",
        ".line 378",
        "  pop",
        "L14:",
        ".line 379",
        "  ldc \"Connector interrupted. Leaving loop.\"",
        "  invokestatic org/apache/log4j/helpers/LogLog.debug(Ljava/lang/String;)V",
        "L15:",
        ".line 380",
        "  return",
        "L16:",
        ".line 381",
        "  pop",
        "L17:",
        ".line 382",
        "  new java/lang/StringBuffer",
        "  dup",
        "  ldc \"Remote host \"",
        "  invokenonvirtual java/lang/StringBuffer.<init>(Ljava/lang/String;)V",
        "  aload 0",
        "  getfield org/apache/log4j/net/SocketAppender$Connector.this$0 Lorg/apache/log4j/net/SocketAppender;",
        "  getfield org/apache/log4j/net/SocketAppender.address Ljava/net/InetAddress;",
        "  invokevirtual java/net/InetAddress.getHostName()Ljava/lang/String;",
        "  invokevirtual java/lang/StringBuffer.append(Ljava/lang/String;)Ljava/lang/StringBuffer;",
        "L18:",
        ".line 383",
        "  ldc \" refused connection.\"",
        "  invokevirtual java/lang/StringBuffer.append(Ljava/lang/String;)Ljava/lang/StringBuffer;",
        "  invokevirtual java/lang/StringBuffer.toString()Ljava/lang/String;",
        "L19:",
        ".line 382",
        "  invokestatic org/apache/log4j/helpers/LogLog.debug(Ljava/lang/String;)V",
        "L20:",
        ".line 368",
        "  goto L1",
        "L21:",
        ".line 384",
        "  astore 2",
        "L22:",
        ".line 385",
        "  new java/lang/StringBuffer",
        "  dup",
        "  ldc \"Could not connect to \"",
        "  invokenonvirtual java/lang/StringBuffer.<init>(Ljava/lang/String;)V",
        "  aload 0",
        "  getfield org/apache/log4j/net/SocketAppender$Connector.this$0 Lorg/apache/log4j/net/SocketAppender;",
        "  getfield org/apache/log4j/net/SocketAppender.address Ljava/net/InetAddress;",
        "  invokevirtual java/net/InetAddress.getHostName()Ljava/lang/String;",
        "  invokevirtual java/lang/StringBuffer.append(Ljava/lang/String;)Ljava/lang/StringBuffer;",
        "L23:",
        ".line 386",
        "  ldc \". Exception is \"",
        "  invokevirtual java/lang/StringBuffer.append(Ljava/lang/String;)Ljava/lang/StringBuffer;",
        "  aload 2",
        "  invokevirtual java/lang/StringBuffer.append(Ljava/lang/Object;)Ljava/lang/StringBuffer;",
        "  invokevirtual java/lang/StringBuffer.toString()Ljava/lang/String;",
        "L24:",
        ".line 385",
        "  invokestatic org/apache/log4j/helpers/LogLog.debug(Ljava/lang/String;)V",
        "L25:",
        ".line 368",
        "  goto L1",
        "L1:",
        ".line 367",
        "  aload 0",
        "  getfield org/apache/log4j/net/SocketAppender$Connector.interrupted Z",
        "  ifeq L2",
        "L11:",
        ".line 365",
        "  return",
        "L26:",
        ".catch all from L6 to L12 using L12",
        ".catch java/lang/InterruptedException from L2 to L13 using L13",
        ".catch java/net/ConnectException from L2 to L13 using L16",
        ".catch java/io/IOException from L2 to L13 using L21"
    );

    // Check that the code compiles without an infinite loop. It cannot run by itself.
    AndroidApp app = compileWithD8(builder);
    assertNotNull(app);
  }

  @Test
  public void regress37888855Reduced() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    // This is the code for the method
    //
    // void org.eclipse.jdt.internal.core.JavaModelOperation.run(org.eclipse.core.runtime.IProgressMonitor)
    //
    // from struts2/lib/core-3.1.1.jar
    //
    clazz.addVirtualMethod("run", ImmutableList.of("Lorg/eclipse/core/runtime/IProgressMonitor;"),
        "V",
        ".limit stack 3",
        ".limit locals 15",
        "TryStart1:",
        "TryStart2:",
        "  goto Finally2Start",
        "TryEnd2:",
        "Catch2:",
        "  astore 6",
        "  jsr Finally2Code",  // 6
        "  aload 6",
        "  athrow",
        "Finally2Code:",  // 9
        "  astore 5",
        "  ret 5",
        "Finally2Start:",
        "  jsr Finally2Code",  // 13
        "Finally2End:",
        "  goto Finally1Start",
        "TryEnd1:",
        "Catch1:",
        "  astore 8",
        "  jsr Finally1Code",  // 19
        "L19:",
        "  aload 8",
        "  athrow",
        "Finally1Code:",
        "  astore 7",
        "Try3Start:",
        "  goto Finally3Start",
        "Try3End:",
        "Catch3:",
        "  astore 14",  // Catch
        "  jsr Finally3Code",  // 30
        "  aload 14",
        "  athrow",
        "Finally3Code:",  // Finally
        "  astore 13",
        "  ret 13",
        "Finally3Start:",
        "  jsr Finally3Code",  // 37
        "Finally3End:",
        "  ret 7",
        "Finally1Start:",
        "  jsr Finally1Code",  // 41
        "Finally1End:",
        "  return",
        ".catch all from TryStart2 to TryEnd2 using Catch2",  // Block
        ".catch all from Finally2Start to Finally2End using Catch2",  // Finally
        ".catch all from TryStart1 to TryEnd1 using Catch1",  // Block
        ".catch all from Finally1Start to Finally1End using Catch1",  // Finally
        ".catch all from Try3Start to Try3End using Catch3",  // Block
        ".catch all from Finally3Start to Finally3End using Catch3"  // Finally
    );

    // Check that the code compiles without an infinite loop. It cannot run by itself.
    AndroidApp app = compileWithD8(builder);
    assertNotNull(app);
  }

  @Test
  public void regress37888855() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    // This is the code for the method
    //
    // void org.eclipse.jdt.internal.core.JavaModelOperation.run(org.eclipse.core.runtime.IProgressMonitor)
    //
    // from struts2/lib/core-3.1.1.jar
    //
    clazz.addVirtualMethod("run", ImmutableList.of("Lorg/eclipse/core/runtime/IProgressMonitor;"), "V",
        ".limit stack 3",
        ".limit locals 15",
        ".var 0 is this Lorg/eclipse/jdt/internal/core/JavaModelOperation; from L0 to L50",
        ".var 1 is monitor Lorg/eclipse/core/runtime/IProgressMonitor; from L0 to L50",
        ".var 2 is manager Lorg/eclipse/jdt/internal/core/JavaModelManager; from L1 to L50",
        ".var 3 is deltaProcessor Lorg/eclipse/jdt/internal/core/DeltaProcessor; from L2 to L50",
        ".var 4 is previousDeltaCount I from L1 to L50",
        ".var 9 is i I from L22 to L27",
        ".var 10 is size I from L23 to L27",
        ".var 9 is i I from L28 to L38",
        ".var 10 is length I from L29 to L38",
        ".var 11 is element Lorg/eclipse/jdt/core/IJavaElement; from L32 to L37",
        ".var 12 is openable Lorg/eclipse/jdt/internal/core/Openable; from L33 to L37",
        "L0:",
        ".line 705",
        "  invokestatic org/eclipse/jdt/internal/core/JavaModelManager.getJavaModelManager()Lorg/eclipse/jdt/internal/core/JavaModelManager;",
        "  astore 2",
        "L1:",
        ".line 706",
        "  aload 2",
        "  invokevirtual org/eclipse/jdt/internal/core/JavaModelManager.getDeltaProcessor()Lorg/eclipse/jdt/internal/core/DeltaProcessor;",
        "  astore 3",
        "L2:",
        ".line 707",
        "  aload 3",
        "  getfield org/eclipse/jdt/internal/core/DeltaProcessor.javaModelDeltas Ljava/util/ArrayList;",
        "  invokevirtual java/util/ArrayList.size()I",
        "  istore 4",
        "L3:",
        ".line 709",
        "  aload 0",
        "  aload 1",
        "  putfield org/eclipse/jdt/internal/core/JavaModelOperation.progressMonitor Lorg/eclipse/core/runtime/IProgressMonitor;",
        "L4:",
        ".line 710",
        "  aload 0",
        "  aload 0",
        "  invokevirtual org/eclipse/jdt/internal/core/JavaModelOperation.pushOperation(Lorg/eclipse/jdt/internal/core/JavaModelOperation;)V",
        "L5:",
        ".line 712",
        "  aload 0",
        "  invokevirtual org/eclipse/jdt/internal/core/JavaModelOperation.canModifyRoots()Z",
        "  ifeq L6",
        "L7:",
        ".line 715",
        "  invokestatic org/eclipse/jdt/internal/core/JavaModelManager.getJavaModelManager()Lorg/eclipse/jdt/internal/core/JavaModelManager;",
        "  getfield org/eclipse/jdt/internal/core/JavaModelManager.deltaState Lorg/eclipse/jdt/internal/core/DeltaProcessingState;",
        "  invokevirtual org/eclipse/jdt/internal/core/DeltaProcessingState.initializeRoots()V",
        "L6:",
        ".line 718",
        "  aload 0",
        "  invokevirtual org/eclipse/jdt/internal/core/JavaModelOperation.executeOperation()V",
        "  goto L8",
        "L9:",
        ".line 719",
        "  astore 6",
        "  jsr L10",
        "L11:",
        ".line 723",
        "  aload 6",
        "  athrow",
        "L10:",
        ".line 719",
        "  astore 5",
        "L12:",
        ".line 720",
        "  aload 0",
        "  invokevirtual org/eclipse/jdt/internal/core/JavaModelOperation.isTopLevelOperation()Z",
        "  ifeq L13",
        "L14:",
        ".line 721",
        "  aload 0",
        "  invokevirtual org/eclipse/jdt/internal/core/JavaModelOperation.runPostActions()V",
        "L13:",
        ".line 723",
        "  ret 5",
        "L8:",
        "  jsr L10",
        "L15:",
        "  goto L16",
        "L17:",
        ".line 724",
        "  astore 8",
        "  jsr L18",
        "L19:",
        ".line 764",
        "  aload 8",
        "  athrow",
        "L18:",
        ".line 724",
        "  astore 7",
        "L20:",
        ".line 727",
        "  aload 2",
        "  invokevirtual org/eclipse/jdt/internal/core/JavaModelManager.getDeltaProcessor()Lorg/eclipse/jdt/internal/core/DeltaProcessor;",
        "  astore 3",
        "L21:",
        ".line 730",
        "  iload 4",
        "  istore 9",
        "L22:",
        "  aload 3",
        "  getfield org/eclipse/jdt/internal/core/DeltaProcessor.javaModelDeltas Ljava/util/ArrayList;",
        "  invokevirtual java/util/ArrayList.size()I",
        "  istore 10",
        "L23:",
        "  goto L24",
        "L25:",
        ".line 731",
        "  aload 3",
        "  aload 3",
        "  getfield org/eclipse/jdt/internal/core/DeltaProcessor.javaModelDeltas Ljava/util/ArrayList;",
        "  iload 9",
        "  invokevirtual java/util/ArrayList.get(I)Ljava/lang/Object;",
        "  checkcast org/eclipse/jdt/core/IJavaElementDelta",
        "  invokevirtual org/eclipse/jdt/internal/core/DeltaProcessor.updateJavaModel(Lorg/eclipse/jdt/core/IJavaElementDelta;)V",
        "L26:",
        ".line 730",
        "  iinc 9 1",
        "L24:",
        "  iload 9",
        "  iload 10",
        "  if_icmplt L25",
        "L27:",
        ".line 737",
        "  iconst_0",
        "  istore 9",
        "L28:",
        "  aload 0",
        "  getfield org/eclipse/jdt/internal/core/JavaModelOperation.resultElements [Lorg/eclipse/jdt/core/IJavaElement;",
        "  arraylength",
        "  istore 10",
        "L29:",
        "  goto L30",
        "L31:",
        ".line 738",
        "  aload 0",
        "  getfield org/eclipse/jdt/internal/core/JavaModelOperation.resultElements [Lorg/eclipse/jdt/core/IJavaElement;",
        "  iload 9",
        "  aaload",
        "  astore 11",
        "L32:",
        ".line 739",
        "  aload 11",
        "  invokeinterface org/eclipse/jdt/core/IJavaElement.getOpenable()Lorg/eclipse/jdt/core/IOpenable; 0",
        "  checkcast org/eclipse/jdt/internal/core/Openable",
        "  astore 12",
        "L33:",
        ".line 740",
        "  aload 12",
        "  instanceof org/eclipse/jdt/internal/core/CompilationUnit",
        "  ifeq L34",
        "  aload 12",
        "  checkcast org/eclipse/jdt/internal/core/CompilationUnit",
        "  invokevirtual org/eclipse/jdt/internal/core/CompilationUnit.isWorkingCopy()Z",
        "  ifne L35",
        "L34:",
        ".line 741",
        "  aload 12",
        "  invokevirtual org/eclipse/jdt/internal/core/Openable.getParent()Lorg/eclipse/jdt/core/IJavaElement;",
        "  checkcast org/eclipse/jdt/internal/core/JavaElement",
        "  invokevirtual org/eclipse/jdt/internal/core/JavaElement.close()V",
        "L35:",
        ".line 743",
        "  aload 11",
        "  invokeinterface org/eclipse/jdt/core/IJavaElement.getElementType()I 0",
        "  tableswitch 3",
        "    L36",
        "    L36",
        "    default: L37",
        "L36:",
        ".line 746",
        "  aload 11",
        "  invokeinterface org/eclipse/jdt/core/IJavaElement.getJavaProject()Lorg/eclipse/jdt/core/IJavaProject; 0",
        "  checkcast org/eclipse/jdt/internal/core/JavaProject",
        "  invokevirtual org/eclipse/jdt/internal/core/JavaProject.resetCaches()V",
        "L37:",
        ".line 737",
        "  iinc 9 1",
        "L30:",
        "  iload 9",
        "  iload 10",
        "  if_icmplt L31",
        "L38:",
        ".line 755",
        "  aload 0",
        "  invokevirtual org/eclipse/jdt/internal/core/JavaModelOperation.isTopLevelOperation()Z",
        "  ifeq L39",
        "L40:",
        ".line 756",
        "  aload 3",
        "  getfield org/eclipse/jdt/internal/core/DeltaProcessor.javaModelDeltas Ljava/util/ArrayList;",
        "  invokevirtual java/util/ArrayList.size()I",
        "  iload 4",
        "  if_icmpgt L41",
        "  aload 3",
        "  getfield org/eclipse/jdt/internal/core/DeltaProcessor.reconcileDeltas Ljava/util/HashMap;",
        "  invokevirtual java/util/HashMap.isEmpty()Z",
        "  ifne L39",
        "L41:",
        ".line 757",
        "  aload 0",
        "  invokevirtual org/eclipse/jdt/internal/core/JavaModelOperation.hasModifiedResource()Z",
        "  ifne L39",
        "L42:",
        ".line 758",
        "  aload 3",
        "  aconst_null",
        "  iconst_0",
        "  invokevirtual org/eclipse/jdt/internal/core/DeltaProcessor.fire(Lorg/eclipse/jdt/core/IJavaElementDelta;I)V",
        "  goto L39",
        "L43:",
        ".line 761",
        "  astore 14",
        "  jsr L44",
        "L45:",
        ".line 763",
        "  aload 14",
        "  athrow",
        "L44:",
        ".line 761",
        "  astore 13",
        "L46:",
        ".line 762",
        "  aload 0",
        "  invokevirtual org/eclipse/jdt/internal/core/JavaModelOperation.popOperation()Lorg/eclipse/jdt/internal/core/JavaModelOperation;",
        "  pop",
        "L47:",
        ".line 763",
        "  ret 13",
        "L39:",
        "  jsr L44",
        "L48:",
        ".line 764",
        "  ret 7",
        "L16:",
        "  jsr L18",
        "L49:",
        ".line 765",
        "  return",
        "L50:",
        ".catch all from L5 to L9 using L9",
        ".catch all from L8 to L15 using L9",
        ".catch all from L3 to L17 using L17",
        ".catch all from L16 to L49 using L17",
        ".catch all from L20 to L43 using L43",
        ".catch all from L39 to L48 using L43"
    );

    // Check that the code compiles without an infinite loop. It cannot run by itself.
    AndroidApp app = compileWithD8(builder);
    assertNotNull(app);
  }

  // Some jsr tests that fails with org.objectweb.asm.commons.JSRInlinerAdapter.

  @Test
  // This test is based on the example on http://asm.ow2.org/doc/developer-guide.html.
  public void testJsrWithNestedJsrRetBasedOnControlFlow() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    clazz.addStaticMethod("foo", ImmutableList.of("Z"), "I",
        ".limit stack 2",
        ".limit locals 3",
        "  jsr LabelSub1",
        "  ldc 1",
        "  ireturn",
        "LabelSub1:",
        "  astore 1",
        "  jsr LabelSub2",
        "  ldc 0",
        "  ireturn",
        "LabelSub2:",
        "  astore 2",
        "  iload 0",
        "  ifeq LabelZero",
        "  ret 2",
        "LabelZero:",
        "  ret 1");

    clazz.addMainMethod(
        ".limit stack 2",
        ".limit locals 1",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  ldc 0",
        "  invokestatic Test/foo(Z)I",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  ldc 1",
        "  invokestatic Test/foo(Z)I",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  return");

    String expected = "10";
    String javaResult = runOnJava(builder, clazz.name);
    assertEquals(expected, javaResult);
    String artResult = runOnArt(builder, clazz.name);
    // The ASM jsr inliner does not get the control-flow dependent ret right in his case.
    assertNotEquals(expected, artResult);
    // This fails with dx.
    expectDxFailure(builder);
  }

  @Test
  // This test is based on the example on http://asm.ow2.org/doc/developer-guide.html.
  public void testJsrWithNestedRetBasedOnControlFlow2() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    clazz.addStaticMethod("foo", ImmutableList.of("Z"), "I",
        ".limit stack 2",
        ".limit locals 3",
        "  jsr LabelSub1",
        "  ldc 1",
        "  ireturn",
        "LabelSub1:",
        "  jsr LabelSub2",
        "  ldc 0",
        "  ireturn",
        "LabelSub2:",
        "  astore 2",
        "  astore 1",
        "  iload 0",
        "  ifeq LabelZero",
        "  ret 2",
        "LabelZero:",
        "  ret 1");

    clazz.addMainMethod(
        ".limit stack 2",
        ".limit locals 1",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  ldc 0",
        "  invokestatic Test/foo(Z)I",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  ldc 1",
        "  invokestatic Test/foo(Z)I",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  return");

    String expected = "10";
    String javaResult = runOnJava(builder, clazz.name);
    assertEquals(expected, javaResult);
    String artResult = runOnArt(builder, clazz.name);
    // The ASM jsr inliner does not get the control-flow dependent ret right in his case.
    assertNotEquals(expected, artResult);
    // This fails with dx.
    expectDxFailure(builder);
  }

  @Test
  // This test is based on the example on http://asm.ow2.org/doc/developer-guide.html.
  public void testJsrWithNestedRetBasedOnControlFlow3() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    clazz.addStaticMethod("foo", ImmutableList.of("Z"), "I",
        ".limit stack 2",
        ".limit locals 3",
        "  jsr LabelSub1",
        "  ldc 1",
        "  ireturn",
        "LabelSub1:",
        "  jsr LabelSub2",
        "  ldc 0",
        "  ireturn",
        "LabelSub2:",
        "  swap",
        "  astore 1",
        "  astore 2",
        "  iload 0",
        "  ifeq LabelZero",
        "  ret 2",
        "LabelZero:",
        "  ret 1");

    clazz.addMainMethod(
        ".limit stack 2",
        ".limit locals 1",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  ldc 0",
        "  invokestatic Test/foo(Z)I",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  ldc 1",
        "  invokestatic Test/foo(Z)I",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  return");

    String expected = "10";
    String javaResult = runOnJava(builder, clazz.name);
    assertEquals(expected, javaResult);
    String artResult = runOnArt(builder, clazz.name);
    // The ASM jsr inliner does not get the control-flow dependent ret right in his case.
    assertNotEquals(expected, artResult);
    // This fails with dx.
    expectDxFailure(builder);
  }

  // Some jsr tests that fails bytecode verification on the Java VM.

  @Test
  public void testReuseAddr() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    clazz.addMainMethod(
        ".limit stack 3",
        ".limit locals 2",
        "  jsr LabelSub1",
        "  return",
        "LabelSub1:",
        "  jsr LabelSub2",
        "  dup",
        "  astore 0",
        "  ret 0",
        "LabelSub2:",
        "  dup",
        "  astore 0",
        "  ret 0");

    // This does not run on the Java VM (verification error).
    assertNotEquals(0, runOnJavaRaw(builder, clazz.name));
  }

  @Test
  public void testSwitchAddr() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    clazz.addMainMethod(
        ".limit stack 3",
        ".limit locals 2",
        "  jsr LabelSub1",
        "  astore 0",
        "  ret 0",
        "LabelSub1:",
        "  jsr LabelSub2",
        "  return",
        "LabelSub2:",
        "  swap",
        "  astore 0",
        "  ret 0");

    // This does not run on the Java VM (verification error).
    assertNotEquals(0, runOnJavaRaw(builder, clazz.name));
  }

  @Test
  public void testJsrPreserveRetAddressOverJsr() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    clazz.addStaticMethod("foo", ImmutableList.of("Z"), "I",
        ".limit stack 2",
        ".limit locals 2",
        "  jsr LabelSub1",
        "  ldc 0",
        "  ireturn",
        "LabelSub1:",
        "  iload 0",
        "  ifeq StoreInFirst",
        "  pop",
        "  goto NextJsr",
        "StoreInFirst:",
        "  astore 1",
        "NextJsr:",
        "  jsr LabelSub2",
        "  ldc 1",
        "  ireturn",
        "LabelSub2:",
        "  iload 0",
        "  ifeq DontStoreInSecond",  // Same as StoreInFirst
        "  astore 1",
        "  goto Ret",
        "DontStoreInSecond:",
        "  pop",
        "Ret:",
        // There will always be an address in local 1 at this point.
        "  ret 1");

    clazz.addMainMethod(
        ".limit stack 2",
        ".limit locals 1",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  ldc 0",
        "  invokestatic Test/foo(Z)I",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  ldc 1",
        "  invokestatic Test/foo(Z)I",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  return");

    // This does not run on the Java VM (verification error).
    assertNotEquals(0, runOnJavaRaw(builder, clazz.name));
  }
}
