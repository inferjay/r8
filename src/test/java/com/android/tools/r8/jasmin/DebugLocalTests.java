// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.jasmin;

import static org.junit.Assert.assertEquals;

import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.debuginfo.DebugInfoInspector;
import com.android.tools.r8.graph.DexCode;
import com.android.tools.r8.graph.DexDebugInfo;
import com.android.tools.r8.naming.MemberNaming.MethodSignature;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.DexInspector;
import com.android.tools.r8.utils.DexInspector.ClassSubject;
import com.android.tools.r8.utils.DexInspector.MethodSubject;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class DebugLocalTests extends JasminTestBase {

  @Test
  public void testSwap() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");
    MethodSignature foo = clazz.addVirtualMethod("foo", ImmutableList.of("Ljava/lang/String;"), "V",
        // The first three vars are out-of-order to verify that the order is not relied on.
        ".var 5 is t I from L4 to L6",
        ".var 1 is bar Ljava/lang/String; from L0 to L9",
        ".var 0 is this LTest; from L0 to L9",
        ".var 2 is x I from L1 to L9",
        ".var 3 is y I from L2 to L9",
        ".var 4 is z I from L3 to L9",
        ".var 5 is foobar Ljava/lang/String; from L7 to L9",
        ".limit locals 6",
        ".limit stack 2",
        "L0:",
        ".line 23",
        " iconst_1",
        " istore 2",
        "L1:",
        ".line 24",
        " iconst_2",
        " istore 3",
        "L2:",
        ".line 25",
        " iconst_3",
        " istore 4",
        "L3:",
        " .line 27",
        " iload 3",
        " istore 5",
        "L4:",
        " .line 28",
        " iload 2",
        " istore 3",
        "L5:",
        " .line 29",
        " iload 5",
        " istore 2",
        "L6:",
        " .line 32",
        " new java/lang/StringBuilder",
        " dup",
        " invokespecial java/lang/StringBuilder/<init>()V",
        " ldc \"And the value of y is: \"",
        " invokevirtual java/lang/StringBuilder/append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        " iload 2",
        " invokevirtual java/lang/StringBuilder/append(I)Ljava/lang/StringBuilder;",
        " iload 3",
        " invokevirtual java/lang/StringBuilder/append(I)Ljava/lang/StringBuilder;",
        " iload 4",
        " invokevirtual java/lang/StringBuilder/append(I)Ljava/lang/StringBuilder;",
        " invokevirtual java/lang/StringBuilder/toString()Ljava/lang/String;",
        " astore 5",
        "L7:",
        " .line 34",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  aload 5",
        "  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V",
        "L8:",
        " .line 35",
        " return",
        "L9:");

    clazz.addMainMethod(
        ".limit stack 3",
        ".limit locals 1",
        "  new Test",
        "  dup",
        "  invokespecial Test/<init>()V",
        "  ldc \"Fsg\"",
        "  invokevirtual Test/foo(Ljava/lang/String;)V",
        "  return");

    String expected = "And the value of y is: 213";
    String javaResult = runOnJava(builder, clazz.name);
    assertEquals(expected, javaResult);

    AndroidApp jasminApp = builder.build();
    AndroidApp d8App = ToolHelper.runD8(jasminApp);

    DexInspector inspector = new DexInspector(d8App);
    ClassSubject classSubject = inspector.clazz("Test");
    MethodSubject methodSubject = classSubject.method(foo);
    DexCode code = methodSubject.getMethod().getCode().asDexCode();
    DexDebugInfo info = code.getDebugInfo();
    assertEquals(23, info.startLine);
    assertEquals(1, info.parameters.length);
    assertEquals("bar", info.parameters[0].toString());

    // TODO(zerny): Verify the debug computed locals information.

    String artResult = runOnArt(d8App, clazz.name);
    assertEquals(expected, artResult);
  }

  @Test
  public void testNoLocalInfoOnStack() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");
    MethodSignature foo = clazz.addVirtualMethod("foo", ImmutableList.of(), "I",
        ".var 0 is this LTest; from Init to End",
        ".var 1 is x I from XStart to XEnd",
        ".limit locals 2",
        ".limit stack 1",
        "Init:",
        ".line 1",
        "  ldc 0",
        "  istore 1",
        "XStart:",
        ".line 2",
        "  ldc 42",
        "  istore 1",
        "  iload 1",
        "XEnd:",
        ".line 3",
        "  ireturn",
        "End:");

    clazz.addMainMethod(
        ".limit stack 3",
        ".limit locals 1",
        "  new Test",
        "  dup",
        "  invokespecial Test/<init>()V",
        "  invokevirtual Test/foo()I",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  swap",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  return");

    String expected = "42";
    String javaResult = runOnJava(builder, clazz.name);
    assertEquals(expected, javaResult);

    AndroidApp jasminApp = builder.build();
    AndroidApp d8App = ToolHelper.runD8(jasminApp);

    DebugInfoInspector info = new DebugInfoInspector(d8App, clazz.name, foo);
    info.checkStartLine(1);
    info.checkLineHasExactLocals(1, "this", "Test");
    info.checkLineHasExactLocals(2, "this", "Test", "x", "int");
    info.checkLineHasExactLocals(3, "this", "Test");

    String artResult = runOnArt(d8App, clazz.name);
    assertEquals(expected, artResult);
  }

  // Check that we properly handle switching a local slot from one variable to another.
  @Test
  public void checkLocalChange() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    MethodSignature foo = clazz.addStaticMethod("foo", ImmutableList.of("I"), "I",
        ".limit stack 2",
        ".limit locals 2",
        ".var 0 is param I from MethodStart to MethodEnd",
        ".var 1 is x I from LabelXStart to LabelXEnd",
        ".var 1 is y I from LabelYStart to LabelYEnd",

        "MethodStart:",
        ".line 1",

        "LabelXStart:",
        "  ldc 0",
        "  istore 1",
        ".line 2",
        "  invokestatic Test/ensureLine()V",
        "LabelXEnd:",

        "  iload 0",
        "  lookupswitch",
        "  1: Case1",
        "  default: CaseDefault",

        "Case1:",
        "  ldc 42",
        "  istore 1",
        "LabelYStart:",
        ".line 3",
        "  invokestatic Test/ensureLine()V",
        "  goto AfterSwitch",

        "CaseDefault:",
        "  ldc -42",
        "  istore 1",
        ".line 4",
        "  invokestatic Test/ensureLine()V",

        "AfterSwitch:",
        ".line 5",
        "  iload 1",
        "  ireturn",
        "LabelYEnd:",

        "MethodEnd:"
    );

    clazz.addStaticMethod("ensureLine", ImmutableList.of(), "V",
        ".limit stack 0",
        ".limit locals 0",
        "  return");

    clazz.addMainMethod(
        ".limit stack 2",
        ".limit locals 1",
        "  ldc 0",
        "  invokestatic Test/foo(I)I",
        "  ldc 1",
        "  invokestatic Test/foo(I)I",
        "  pop",
        "  return");

    String expected = "";
    String javaResult = runOnJava(builder, clazz.name);
    assertEquals(expected, javaResult);

    AndroidApp jasminApp = builder.build();
    AndroidApp d8App = ToolHelper.runD8(jasminApp);
    String artResult = runOnArt(d8App, clazz.name);
    assertEquals(expected, artResult);

    DebugInfoInspector info = new DebugInfoInspector(d8App, clazz.name, foo);
    info.checkStartLine(1);
    info.checkLineHasExactLocals(1, "param", "int");
    info.checkLineHasExactLocals(2, "param", "int", "x", "int");
    info.checkLineHasExactLocals(3, "param", "int", "y", "int");
    info.checkLineHasExactLocals(4, "param", "int", "y", "int");
    info.checkLineHasExactLocals(5, "param", "int", "y", "int");
  }

  @Test
  public void testLocalManyRanges() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    MethodSignature foo = clazz.addStaticMethod("foo", ImmutableList.of("I"), "I",
        ".limit stack 2",
        ".limit locals 2",
        ".var 0 is param I from Init to End",
        ".var 1 is x I from LabelStart1 to LabelEnd1",
        ".var 1 is x I from LabelStart2 to LabelEnd2",
        ".var 1 is x I from LabelStart3 to LabelEnd3",
        ".var 1 is x I from LabelStart4 to LabelEnd4",
        "Init:",
        ".line 1",
        "  iload 0",
        "  istore 1",

        "LabelStart1:",
        ".line 2",
        "  invokestatic Test/ensureLine()V",
        "LabelEnd1:",
        ".line 3",
        "  invokestatic Test/ensureLine()V",

        "LabelStart2:",
        ".line 4",
        "  invokestatic Test/ensureLine()V",
        "LabelEnd2:",
        ".line 5",
        "  invokestatic Test/ensureLine()V",

        "LabelStart3:",
        ".line 6",
        "  invokestatic Test/ensureLine()V",
        "LabelEnd3:",
        ".line 7",
        "  invokestatic Test/ensureLine()V",

        "LabelStart4:",
        ".line 8",
        "  invokestatic Test/ensureLine()V",
        "LabelEnd4:",
        ".line 9",
        "  invokestatic Test/ensureLine()V",
        "  iload 1",
        "  ireturn",
        "End:"
    );

    clazz.addStaticMethod("ensureLine", ImmutableList.of(), "V",
        ".limit stack 0",
        ".limit locals 0",
        "  return");

    clazz.addMainMethod(
        ".limit stack 2",
        ".limit locals 1",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  ldc 42",
        "  invokestatic Test/foo(I)I",
        "  invokevirtual java/io/PrintStream/print(I)V",
        "  return");

    String expected = "42";
    String javaResult = runOnJava(builder, clazz.name);
    assertEquals(expected, javaResult);

    AndroidApp jasminApp = builder.build();
    AndroidApp d8App = ToolHelper.runD8(jasminApp);
    String artResult = runOnArt(d8App, clazz.name);
    assertEquals(expected, artResult);

    DebugInfoInspector info = new DebugInfoInspector(d8App, clazz.name, foo);
    info.checkStartLine(1);
    info.checkLineHasExactLocals(1, "param", "int");
    info.checkLineHasExactLocals(2, "param", "int", "x", "int");
    info.checkLineHasExactLocals(3, "param", "int");
    info.checkLineHasExactLocals(4, "param", "int", "x", "int");
    info.checkLineHasExactLocals(5, "param", "int");
    info.checkLineHasExactLocals(6, "param", "int", "x", "int");
    info.checkLineHasExactLocals(7, "param", "int");
    info.checkLineHasExactLocals(8, "param", "int", "x", "int");
    info.checkLineHasExactLocals(9, "param", "int");
  }
}
