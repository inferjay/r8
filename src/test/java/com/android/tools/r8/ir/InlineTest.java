// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir;

import static org.junit.Assert.assertEquals;

import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.ValueNumberGenerator;
import com.android.tools.r8.smali.SmaliTestBase;
import com.android.tools.r8.utils.InternalOptions;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.junit.Test;

public class InlineTest extends SmaliTestBase {

  TestApplication codeForMethodReplaceTest(int a, int b) {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    MethodSignature signature = builder.addStaticMethod(
        "int",
        DEFAULT_METHOD_NAME,
        ImmutableList.of("int", "int"),
        0,
        "    invoke-static       { p0, p1 }, LTest;->a(II)I",
        "    move-result         p0",
        "    return              p0"
    );

    MethodSignature signatureA = builder.addStaticMethod(
        "int",
        "a",
        ImmutableList.of("int", "int"),
        1,
        "    add-int             p0, p0, p1",
        "    return              p0"
    );

    MethodSignature signatureB = builder.addStaticMethod(
        "int",
        "b",
        ImmutableList.of("int", "int"),
        1,
        "    if-eq               p0, p1, :eq",
        "    const/4             v0, 1",
        "    return              v0",
        "    :eq",
        "    const/4             v0, 0",
        "    return              v0"
    );

    builder.addMainMethod(
        2,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    const/4             v1, " + a,
        "    const/4             v2, " + b,
        "    invoke-static       { v1, v2 }, LTest;->method(II)I",
        "    move-result         v1",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(I)V",
        "    return-void"
    );

    InternalOptions options = new InternalOptions();
    DexApplication application = buildApplication(builder, options);

    // Return the processed method for inspection.
    ValueNumberGenerator valueNumberGenerator = new ValueNumberGenerator();
    DexEncodedMethod method = getMethod(application, signature);
    IRCode code = method.buildIR(valueNumberGenerator, new InternalOptions());

    DexEncodedMethod methodA = getMethod(application, signatureA);
    IRCode codeA = methodA.buildIR(valueNumberGenerator, new InternalOptions());

    DexEncodedMethod methodB = getMethod(application, signatureB);
    IRCode codeB = methodB.buildIR(valueNumberGenerator, new InternalOptions());

    return new SmaliTestBase.TestApplication(application, method, code,
        ImmutableList.of(codeA, codeB), valueNumberGenerator, options);
  }

  public void runInlineTest(int a, int b, int expectedA, int expectedB) {
    // Run code without inlining.
    TestApplication test = codeForMethodReplaceTest(a, b);
    String result = test.run();
    assertEquals(Integer.toString(expectedA), result);

    InstructionListIterator iterator;

    // Run code inlining a.
    test = codeForMethodReplaceTest(a, b);
    iterator = test.code.blocks.get(0).listIterator();
    iterator.nextUntil(instruction -> instruction.isInvoke());
    iterator.previous();
    iterator.inlineInvoke(test.code, test.additionalCode.get(0));
    result = test.run();
    assertEquals(Integer.toString(expectedA), result);

    // Run code inlining b (where a is actually called).
    test = codeForMethodReplaceTest(a, b);
    iterator = test.code.blocks.get(0).listIterator();
    iterator.nextUntil(instruction -> instruction.isInvoke());
    iterator.previous();
    iterator.inlineInvoke(test.code, test.additionalCode.get(1));
    result = test.run();
    assertEquals(Integer.toString(expectedB), result);
  }

  @Test
  public void inline() {
    runInlineTest(1, 1, 2, 0);
    runInlineTest(1, 2, 3, 1);
  }

  TestApplication codeForMethodReplaceReturnVoidTest(int a, int b) {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    MethodSignature signature = builder.addStaticMethod(
        "int",
        DEFAULT_METHOD_NAME,
        ImmutableList.of("int", "int"),
        0,
        "    invoke-static       { p0, p1 }, LTest;->a(II)V",
        "    return              p0"
    );

    MethodSignature signatureA = builder.addStaticMethod(
        "void",
        "a",
        ImmutableList.of("int", "int"),
        1,
        "    add-int             p0, p0, p1",
        "    return-void         "
    );

    builder.addMainMethod(
        2,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    const/4             v1, " + a,
        "    const/4             v2, " + b,
        "    invoke-static       { v1, v2 }, LTest;->method(II)I",
        "    move-result         v1",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(I)V",
        "    return-void"
    );

    InternalOptions options = new InternalOptions();
    DexApplication application = buildApplication(builder, options);

    // Return the processed method for inspection.
    ValueNumberGenerator valueNumberGenerator = new ValueNumberGenerator();
    DexEncodedMethod method = getMethod(application, signature);
    IRCode code = method.buildIR(valueNumberGenerator, new InternalOptions());

    DexEncodedMethod methodA = getMethod(application, signatureA);
    IRCode codeA = methodA.buildIR(valueNumberGenerator, new InternalOptions());

    return new TestApplication(application, method, code,
        ImmutableList.of(codeA), valueNumberGenerator, options);
  }

  @Test
  public void inlineReturnVoid() {
    // Run code without inlining.
    TestApplication test = codeForMethodReplaceReturnVoidTest(1, 2);
    String result = test.run();
    assertEquals(Integer.toString(1), result);

    InstructionListIterator iterator;

    // Run code inlining a.
    test = codeForMethodReplaceReturnVoidTest(1, 2);
    iterator = test.code.blocks.get(0).listIterator();
    iterator.nextUntil(instruction -> instruction.isInvoke());
    iterator.previous();
    iterator.inlineInvoke(test.code, test.additionalCode.get(0));
    result = test.run();
    assertEquals(Integer.toString(1), result);
  }

  TestApplication codeForMultipleMethodReplaceTest(int a, int b) {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    MethodSignature signature = builder.addStaticMethod(
        "int",
        DEFAULT_METHOD_NAME,
        ImmutableList.of("int", "int"),
        0,
        "    invoke-static       { p0, p1 }, LTest;->a(II)I",
        "    move-result         p0",
        "    invoke-static       { p0, p1 }, LTest;->a(II)I",
        "    move-result         p0",
        "    invoke-static       { p0, p1 }, LTest;->a(II)I",
        "    move-result         p0",
        "    return              p0"
    );

    MethodSignature signatureA = builder.addStaticMethod(
        "int",
        "a",
        ImmutableList.of("int", "int"),
        1,
        "    add-int             p0, p0, p1",
        "    return              p0"
    );

    MethodSignature signatureB = builder.addStaticMethod(
        "int",
        "b",
        ImmutableList.of("int", "int"),
        1,
        "    mul-int             p0, p0, p1",
        "    return              p0"
    );

    builder.addMainMethod(
        2,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    const/4             v1, " + a,
        "    const/4             v2, " + b,
        "    invoke-static       { v1, v2 }, LTest;->method(II)I",
        "    move-result         v1",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(I)V",
        "    return-void"
    );

    InternalOptions options = new InternalOptions();
    DexApplication application = buildApplication(builder, options);

    // Return the processed method for inspection.
    ValueNumberGenerator valueNumberGenerator = new ValueNumberGenerator();
    DexEncodedMethod method = getMethod(application, signature);
    IRCode code = method.buildIR(valueNumberGenerator, new InternalOptions());

    // Build three copies of a and b for inlining three times.
    List<IRCode> additionalCode = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      DexEncodedMethod methodA = getMethod(application, signatureA);
      IRCode codeA = methodA.buildIR(valueNumberGenerator, new InternalOptions());
      additionalCode.add(codeA);
    }

    for (int i = 0; i < 3; i++) {
      DexEncodedMethod methodB = getMethod(application, signatureB);
      IRCode codeB = methodB.buildIR(valueNumberGenerator, new InternalOptions());
      additionalCode.add(codeB);
    }

    return new TestApplication(application, method, code,
        additionalCode, valueNumberGenerator, options);
  }

  public void runInlineMultipleTest(int a, int b, int expectedA, int expectedB) {
    // Run code without inlining.
    TestApplication test = codeForMultipleMethodReplaceTest(a, b);
    String result = test.run();
    assertEquals(Integer.toString(expectedA), result);

    InstructionListIterator iterator;

    // Run code inlining all invokes with a.
    test = codeForMultipleMethodReplaceTest(a, b);
    ListIterator<BasicBlock> blocksIterator = test.code.blocks.listIterator();
    Iterator<IRCode> inlinee = test.additionalCode.listIterator();  // IR code for a's
    List<BasicBlock> blocksToRemove = new ArrayList<>();
    while (blocksIterator.hasNext()) {
      BasicBlock block = blocksIterator.next();
      iterator = block.listIterator();
      Instruction invoke = iterator.nextUntil(instruction -> instruction.isInvoke());
      if (invoke != null) {
        iterator.previous();
        iterator.inlineInvoke(test.code, inlinee.next(), blocksIterator, blocksToRemove, null);
        assert blocksToRemove.isEmpty();
      }
    }
    result = test.run();
    assertEquals(Integer.toString(expectedA), result);

    // Run code inlining all invokes with b.
    test = codeForMultipleMethodReplaceTest(a, b);
    blocksIterator = test.code.blocks.listIterator();
    inlinee = test.additionalCode.listIterator(3);  // IR code for b's
    while (blocksIterator.hasNext()) {
      BasicBlock block = blocksIterator.next();
      iterator = block.listIterator();
      Instruction invoke = iterator.nextUntil(instruction -> instruction.isInvoke());
      if (invoke != null) {
        iterator.previous();
        iterator.inlineInvoke(test.code, inlinee.next(), blocksIterator, blocksToRemove, null);
        assert blocksToRemove.isEmpty();
      }
    }
    result = test.run();
    assertEquals(Integer.toString(expectedB), result);
  }

  @Test
  public void inlineMultiple() {
    runInlineMultipleTest(1, 1, 4, 1);
    runInlineMultipleTest(1, 2, 7, 8);
  }

  TestApplication codeForMethodReplaceTestWithCatchHandler(int a, int b, boolean twoGuards) {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    String secondGuard = twoGuards ?
        "    .catch Ljava/lang/Exception; {:try_start .. :try_end} :catch" : "    ";

    MethodSignature signature = builder.addStaticMethod(
        "int",
        DEFAULT_METHOD_NAME,
        ImmutableList.of("int", "int"),
        0,
        "    :try_start",
        "    invoke-static       { p0, p1 }, LTest;->a(II)I",
        "    :try_end",
        "    move-result         p0",
        "    :return",
        "    return              p0",
        "    .catch Ljava/lang/ArithmeticException; {:try_start .. :try_end} :catch",
        secondGuard,
        "    :catch",
        "    const/4             p0, -1",
        "    goto :return"
    );

    MethodSignature signatureA = builder.addStaticMethod(
        "int",
        "a",
        ImmutableList.of("int", "int"),
        1,
        "    add-int             p0, p0, p1",
        "    return              p0"
    );

    MethodSignature signatureB = builder.addStaticMethod(
        "int",
        "b",
        ImmutableList.of("int", "int"),
        1,
        "    if-eq               p0, p1, :eq",
        "    const/4             v0, 1",
        "    return              v0",
        "    :eq",
        "    const/4             v0, 0",
        "    return              v0"
    );

    builder.addMainMethod(
        2,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    const/4             v1, " + a,
        "    const/4             v2, " + b,
        "    invoke-static       { v1, v2 }, LTest;->method(II)I",
        "    move-result         v1",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(I)V",
        "    return-void"
    );

    InternalOptions options = new InternalOptions();
    DexApplication application = buildApplication(builder, options);

    // Return the processed method for inspection.
    ValueNumberGenerator valueNumberGenerator = new ValueNumberGenerator();
    DexEncodedMethod method = getMethod(application, signature);
    IRCode code = method.buildIR(valueNumberGenerator, new InternalOptions());

    DexEncodedMethod methodA = getMethod(application, signatureA);
    IRCode codeA = methodA.buildIR(valueNumberGenerator, new InternalOptions());

    DexEncodedMethod methodB = getMethod(application, signatureB);
    IRCode codeB = methodB.buildIR(valueNumberGenerator, new InternalOptions());

    return new TestApplication(application, method, code,
        ImmutableList.of(codeA, codeB), valueNumberGenerator, options);
  }

  public void runInlineCallerHasCatchHandlersTest(
      int a, int b, boolean twoGuards, int expectedA, int expectedB) {
    // Run code without inlining.
    TestApplication test = codeForMethodReplaceTestWithCatchHandler(a, b, twoGuards);
    String result = test.run();
    assertEquals(Integer.toString(expectedA), result);

    InstructionListIterator iterator;

    // Run code inlining a.
    test = codeForMethodReplaceTestWithCatchHandler(a, b, twoGuards);
    iterator = test.code.blocks.get(1).listIterator();
    iterator.nextUntil(instruction -> instruction.isInvoke());
    iterator.previous();
    iterator.inlineInvoke(test.code, test.additionalCode.get(0));
    result = test.run();
    assertEquals(Integer.toString(expectedA), result);

    // Run code inlining b (where a is actually called).
    test = codeForMethodReplaceTestWithCatchHandler(a, b, twoGuards);
    iterator = test.code.blocks.get(1).listIterator();
    iterator.nextUntil(instruction -> instruction.isInvoke());
    iterator.previous();
    iterator.inlineInvoke(test.code, test.additionalCode.get(1));
    result = test.run();
    assertEquals(Integer.toString(expectedB), result);
  }

  @Test
  public void inlineCallerHasCatchHandlers() {
    runInlineCallerHasCatchHandlersTest(1, 1, false, 2, 0);
    runInlineCallerHasCatchHandlersTest(1, 2, false, 3, 1);
    runInlineCallerHasCatchHandlersTest(1, 1, true, 2, 0);
    runInlineCallerHasCatchHandlersTest(1, 2, true, 3, 1);
  }

  TestApplication codeForInlineCanThrow(int a, int b, boolean twoGuards) {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    String secondGuard = twoGuards ?
        "    .catch Ljava/lang/Exception; {:try_start .. :try_end} :catch" : "    ";

    MethodSignature signature = builder.addStaticMethod(
        "int",
        DEFAULT_METHOD_NAME,
        ImmutableList.of("int", "int"),
        0,
        "    invoke-static       { p0, p1 }, LTest;->a(II)I",
        "    move-result         p0",
        "    return              p0"
    );

    MethodSignature signatureA = builder.addStaticMethod(
        "int",
        "a",
        ImmutableList.of("int", "int"),
        0,
        "    div-int             p0, p0, p1",
        "    return              p0"
    );

    MethodSignature signatureB = builder.addStaticMethod(
        "int",
        "b",
        ImmutableList.of("int", "int"),
        0,
        "    :try_start",
        "    div-int             p0, p0, p1",
        "    :try_end",
        "    :return",
        "    return              p0",
        "    .catch Ljava/lang/ArithmeticException; {:try_start .. :try_end} :catch",
        secondGuard,
        "    :catch",
        "    const/4             p0, -1",
        "    goto :return"
    );

    builder.addMainMethod(
        2,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    const/4             v1, " + a,
        "    const/4             v2, " + b,
        "    :try_start",
        "    invoke-static       { v1, v2 }, LTest;->method(II)I",
        "    :try_end",
        "    move-result         v1",
        "    :print_result",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(I)V",
        "    return-void",
        "    .catch Ljava/lang/ArithmeticException; {:try_start .. :try_end} :catch",
        "    :catch",
        "    const/4             v1, -2",
        "    goto :print_result"
    );

    InternalOptions options = new InternalOptions();
    DexApplication application = buildApplication(builder, options);

    // Return the processed method for inspection.
    ValueNumberGenerator valueNumberGenerator = new ValueNumberGenerator();
    DexEncodedMethod method = getMethod(application, signature);
    IRCode code = method.buildIR(valueNumberGenerator, new InternalOptions());

    DexEncodedMethod methodA = getMethod(application, signatureA);
    IRCode codeA = methodA.buildIR(valueNumberGenerator, new InternalOptions());

    DexEncodedMethod methodB = getMethod(application, signatureB);
    IRCode codeB = methodB.buildIR(valueNumberGenerator, new InternalOptions());

    return new TestApplication(application, method, code,
        ImmutableList.of(codeA, codeB), valueNumberGenerator, options);
  }

  public void runInlineCanThrow(
      int a, int b, boolean twoGuards, int expectedA, int expectedB) {
    // Run code without inlining.
    TestApplication test = codeForInlineCanThrow(a, b, twoGuards);
    String result = test.run();
    assertEquals(Integer.toString(expectedA), result);

    InstructionListIterator iterator;

    // Run code inlining a.
    test = codeForInlineCanThrow(a, b, twoGuards);
    iterator = test.code.blocks.get(0).listIterator();
    iterator.nextUntil(instruction -> instruction.isInvoke());
    iterator.previous();
    iterator.inlineInvoke(test.code, test.additionalCode.get(0));
    result = test.run();
    assertEquals(Integer.toString(expectedA), result);

    // Run code inlining b (where a is actually called).
    test = codeForInlineCanThrow(a, b, twoGuards);
    iterator = test.code.blocks.get(0).listIterator();
    iterator.nextUntil(instruction -> instruction.isInvoke());
    iterator.previous();
    iterator.inlineInvoke(test.code, test.additionalCode.get(1));
    result = test.run();
    assertEquals(Integer.toString(expectedB), result);
  }

  @Test
  public void inlineCanThrow() {
    runInlineCanThrow(2, 2, false, 1, 1);
    runInlineCanThrow(2, 0, false, -2, -1);
    runInlineCanThrow(2, 2, true, 1, 1);
    runInlineCanThrow(2, 0, true, -2, -1);
  }

  private TestApplication codeForInlineAlwaysThrows(boolean twoGuards) {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    String secondGuard = twoGuards ?
        "    .catch Ljava/lang/Exception; {:try_start .. :try_end} :catch" : "    ";

    MethodSignature signature = builder.addStaticMethod(
        "int",
        DEFAULT_METHOD_NAME,
        ImmutableList.of(),
        1,
        "    invoke-static       { }, LTest;->a()I",
        "    move-result         v0",
        "    return              v0"
    );

    MethodSignature signatureA = builder.addStaticMethod(
        "int",
        "a",
        ImmutableList.of(),
        1,
        "    new-instance v0, Ljava/lang/Exception;",
        "    invoke-direct { v0 }, Ljava/lang/Exception;-><init>()V",
        "    throw v0"
    );

    MethodSignature signatureB = builder.addStaticMethod(
        "int",
        "b",
        ImmutableList.of(),
        1,
        "    :try_start",
        "    new-instance v0, Ljava/lang/Exception;",
        "    invoke-direct { v0 }, Ljava/lang/Exception;-><init>()V",
        "    throw v0",
        "    :try_end",
        "    .catch Ljava/lang/ArithmeticException; {:try_start .. :try_end} :catch",
        secondGuard,
        "    :catch",
        "    const/4             v0, -1",
        "    return              v0"
    );

    builder.addMainMethod(
        2,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    :try_start",
        "    invoke-static       { }, LTest;->method()I",
        "    :try_end",
        "    move-result         v1",
        "    :print_result",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(I)V",
        "    return-void",
        "    .catch Ljava/lang/Exception; {:try_start .. :try_end} :catch",
        "    :catch",
        "    const/4             v1, -2",
        "    goto :print_result"
    );

    InternalOptions options = new InternalOptions();
    DexApplication application = buildApplication(builder, options);

    // Return the processed method for inspection.
    ValueNumberGenerator valueNumberGenerator = new ValueNumberGenerator();
    DexEncodedMethod method = getMethod(application, signature);
    IRCode code = method.buildIR(valueNumberGenerator, new InternalOptions());

    DexEncodedMethod methodA = getMethod(application, signatureA);
    IRCode codeA = methodA.buildIR(valueNumberGenerator, new InternalOptions());

    DexEncodedMethod methodB = getMethod(application, signatureB);
    IRCode codeB = methodB.buildIR(valueNumberGenerator, new InternalOptions());

    return new TestApplication(application, method, code,
        ImmutableList.of(codeA, codeB), valueNumberGenerator, options);
  }

  private void runInlineAlwaysThrows(boolean twoGuards, int expectedA, int expectedB) {
    // Run code without inlining.
    TestApplication test = codeForInlineAlwaysThrows(twoGuards);
    String result = test.run();
    assertEquals(Integer.toString(expectedA), result);

    InstructionListIterator iterator;

    // Run code inlining a.
    test = codeForInlineAlwaysThrows(twoGuards);
    iterator = test.code.blocks.get(0).listIterator();
    iterator.nextUntil(Instruction::isInvoke);
    iterator.previous();
    iterator.inlineInvoke(test.code, test.additionalCode.get(0));

    result = test.run();
    assertEquals(Integer.toString(expectedA), result);

    // Run code inlining b (where a is actually called).
    test = codeForInlineAlwaysThrows(twoGuards);
    iterator = test.code.blocks.get(0).listIterator();
    iterator.nextUntil(Instruction::isInvoke);
    iterator.previous();
    iterator.inlineInvoke(test.code, test.additionalCode.get(1));
    result = test.run();
    assertEquals(Integer.toString(expectedB), result);
  }

  @Test
  public void inlineAlwaysThrows() {
    runInlineAlwaysThrows(false, -2, -2);
    runInlineAlwaysThrows(true, -2, -1);
  }

  private TestApplication codeForInlineAlwaysThrowsMultiple(boolean twoGuards) {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    String secondGuard = twoGuards ?
        "    .catch Ljava/lang/Exception; {:try_start .. :try_end} :catch" : "    ";

    MethodSignature signature = builder.addStaticMethod(
        "int",
        DEFAULT_METHOD_NAME,
        ImmutableList.of(),
        1,
        "    invoke-static       { }, LTest;->a()I",
        "    invoke-static       { }, LTest;->a()I",
        "    invoke-static       { }, LTest;->a()I",
        "    move-result         v0",
        "    return              v0"
    );

    MethodSignature signatureA = builder.addStaticMethod(
        "int",
        "a",
        ImmutableList.of(),
        1,
        "    new-instance v0, Ljava/lang/Exception;",
        "    invoke-direct { v0 }, Ljava/lang/Exception;-><init>()V",
        "    throw v0"
    );

    MethodSignature signatureB = builder.addStaticMethod(
        "int",
        "b",
        ImmutableList.of(),
        1,
        "    :try_start",
        "    new-instance v0, Ljava/lang/Exception;",
        "    invoke-direct { v0 }, Ljava/lang/Exception;-><init>()V",
        "    throw v0",
        "    :try_end",
        "    .catch Ljava/lang/ArithmeticException; {:try_start .. :try_end} :catch",
        secondGuard,
        "    :catch",
        "    const/4             v0, -1",
        "    return              v0"
    );

    builder.addMainMethod(
        2,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    :try_start",
        "    invoke-static       { }, LTest;->method()I",
        "    :try_end",
        "    move-result         v1",
        "    :print_result",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(I)V",
        "    return-void",
        "    .catch Ljava/lang/Exception; {:try_start .. :try_end} :catch",
        "    :catch",
        "    const/4             v1, -2",
        "    goto :print_result"
    );

    InternalOptions options = new InternalOptions();
    DexApplication application = buildApplication(builder, options);

    // Return the processed method for inspection.
    ValueNumberGenerator valueNumberGenerator = new ValueNumberGenerator();
    DexEncodedMethod method = getMethod(application, signature);
    IRCode code = method.buildIR(valueNumberGenerator, new InternalOptions());

    // Build three copies of a and b for inlining three times.
    List<IRCode> additionalCode = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      DexEncodedMethod methodA = getMethod(application, signatureA);
      IRCode codeA = methodA.buildIR(valueNumberGenerator, new InternalOptions());
      additionalCode.add(codeA);
    }

    for (int i = 0; i < 3; i++) {
      DexEncodedMethod methodB = getMethod(application, signatureB);
      IRCode codeB = methodB.buildIR(valueNumberGenerator, new InternalOptions());
      additionalCode.add(codeB);
    }

    return new TestApplication(
        application, method, code, additionalCode, valueNumberGenerator, options);
  }

  private void runInlineAlwaysThrowsMultiple(boolean twoGuards, int expectedA, int expectedB) {
    // Run code without inlining.
    TestApplication test = codeForInlineAlwaysThrows(twoGuards);
    String result = test.run();
    assertEquals(Integer.toString(expectedA), result);

    InstructionListIterator iterator;

    // Run code inlining all invokes with a.
    test = codeForInlineAlwaysThrowsMultiple(twoGuards);
    ListIterator<BasicBlock> blocksIterator = test.code.blocks.listIterator();
    Iterator<IRCode> inlinee = test.additionalCode.listIterator();  // IR code for a's.
    List<BasicBlock> blocksToRemove = new ArrayList<>();
    while (blocksIterator.hasNext()) {
      BasicBlock block = blocksIterator.next();
      if (blocksToRemove.contains(block)) {
        continue;
      }
      iterator = block.listIterator();
      Instruction invoke = iterator.nextUntil(Instruction::isInvoke);
      if (invoke != null) {
        iterator.previous();
        iterator.inlineInvoke(test.code, inlinee.next(), blocksIterator, blocksToRemove, null);
        assert !blocksToRemove.isEmpty();
      }
    }
    test.code.removeBlocks(blocksToRemove);
    result = test.run();
    assertEquals(Integer.toString(expectedA), result);

    // Run code inlining all invokes with b.
    test = codeForInlineAlwaysThrowsMultiple(twoGuards);
    blocksIterator = test.code.blocks.listIterator();
    inlinee = test.additionalCode.listIterator(3);  // IR code for b's.
    while (blocksIterator.hasNext()) {
      BasicBlock block = blocksIterator.next();
      if (blocksToRemove.contains(block)) {
        continue;
      }
      iterator = block.listIterator();
      Instruction invoke = iterator.nextUntil(Instruction::isInvoke);
      if (invoke != null) {
        iterator.previous();
        iterator.inlineInvoke(test.code, inlinee.next(), blocksIterator, blocksToRemove, null);
        assert !blocksToRemove.isEmpty();
      }
    }
    test.code.removeBlocks(blocksToRemove);
    result = test.run();
    assertEquals(Integer.toString(expectedB), result);
  }

  @Test
  public void inlineAlwaysThrowsMultiple() {
    runInlineAlwaysThrowsMultiple(false, -2, -2);
    runInlineAlwaysThrowsMultiple(true, -2, -1);
  }

  private TestApplication codeForInlineAlwaysThrowsMultipleWithControlFlow(
      int a, boolean twoGuards) {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    String secondGuard = twoGuards ?
        "    .catch Ljava/lang/Exception; {:try_start .. :try_end} :catch" : "    ";

    MethodSignature signature = builder.addStaticMethod(
        "int",
        DEFAULT_METHOD_NAME,
        ImmutableList.of("int"),
        1,
        "    const/4             v0, 0",
        "    if-ne               v0, p0, :not_zero",
        "    invoke-static       { }, LTest;->a()I",
        "    :not_zero",
        "    const/4             v0, 1",
        "    if-ne               v0, p0, :not_one",
        "    invoke-static       { }, LTest;->a()I",
        "    :not_one",
        "    invoke-static       { }, LTest;->a()I",
        "    move-result         v0",
        "    return              v0"
    );

    MethodSignature signatureA = builder.addStaticMethod(
        "int",
        "a",
        ImmutableList.of(),
        1,
        "    new-instance v0, Ljava/lang/Exception;",
        "    invoke-direct { v0 }, Ljava/lang/Exception;-><init>()V",
        "    throw v0"
    );

    MethodSignature signatureB = builder.addStaticMethod(
        "int",
        "b",
        ImmutableList.of(),
        1,
        "    :try_start",
        "    new-instance v0, Ljava/lang/Exception;",
        "    invoke-direct { v0 }, Ljava/lang/Exception;-><init>()V",
        "    throw v0",
        "    :try_end",
        "    .catch Ljava/lang/ArithmeticException; {:try_start .. :try_end} :catch",
        secondGuard,
        "    :catch",
        "    const/4             v0, -1",
        "    return              v0"
    );

    builder.addMainMethod(
        2,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    const/4             v1, " + a,
        "    :try_start",
        "    invoke-static       { v1 }, LTest;->method(I)I",
        "    :try_end",
        "    move-result         v1",
        "    :print_result",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(I)V",
        "    return-void",
        "    .catch Ljava/lang/Exception; {:try_start .. :try_end} :catch",
        "    :catch",
        "    const/4             v1, -2",
        "    goto :print_result"
    );

    InternalOptions options = new InternalOptions();
    DexApplication application = buildApplication(builder, options);

    // Return the processed method for inspection.
    ValueNumberGenerator valueNumberGenerator = new ValueNumberGenerator();
    DexEncodedMethod method = getMethod(application, signature);
    IRCode code = method.buildIR(valueNumberGenerator, new InternalOptions());

    // Build three copies of a and b for inlining three times.
    List<IRCode> additionalCode = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      DexEncodedMethod methodA = getMethod(application, signatureA);
      IRCode codeA = methodA.buildIR(valueNumberGenerator, new InternalOptions());
      additionalCode.add(codeA);
    }

    for (int i = 0; i < 3; i++) {
      DexEncodedMethod methodB = getMethod(application, signatureB);
      IRCode codeB = methodB.buildIR(valueNumberGenerator, new InternalOptions());
      additionalCode.add(codeB);
    }

    return new TestApplication(
        application, method, code, additionalCode, valueNumberGenerator, options);
  }

  private void runInlineAlwaysThrowsMultipleWithControlFlow(
      int a, boolean twoGuards, int expectedA, int expectedB) {
    // Run code without inlining.
    TestApplication test = codeForInlineAlwaysThrows(twoGuards);
    String result = test.run();
    assertEquals(Integer.toString(expectedA), result);

    InstructionListIterator iterator;

    // Run code inlining all invokes with a.
    test = codeForInlineAlwaysThrowsMultipleWithControlFlow(a, twoGuards);
    ListIterator<BasicBlock> blocksIterator = test.code.blocks.listIterator();
    Iterator<IRCode> inlinee = test.additionalCode.listIterator();  // IR code for a's.
    List<BasicBlock> blocksToRemove = new ArrayList<>();
    while (blocksIterator.hasNext()) {
      BasicBlock block = blocksIterator.next();
      if (blocksToRemove.contains(block)) {
        continue;
      }
      iterator = block.listIterator();
      Instruction invoke = iterator.nextUntil(Instruction::isInvoke);
      if (invoke != null) {
        iterator.previous();
        iterator.inlineInvoke(test.code, inlinee.next(), blocksIterator, blocksToRemove, null);
        assert !blocksToRemove.isEmpty();
      }
    }
    test.code.removeBlocks(blocksToRemove);
    result = test.run();
    assertEquals(Integer.toString(expectedA), result);

    // Run code inlining all invokes with b.
    test = codeForInlineAlwaysThrowsMultipleWithControlFlow(a, twoGuards);
    blocksIterator = test.code.blocks.listIterator();
    inlinee = test.additionalCode.listIterator(3);  // IR code for b's.
    while (blocksIterator.hasNext()) {
      BasicBlock block = blocksIterator.next();
      if (blocksToRemove.contains(block)) {
        continue;
      }
      iterator = block.listIterator();
      Instruction invoke = iterator.nextUntil(Instruction::isInvoke);
      if (invoke != null) {
        iterator.previous();
        iterator.inlineInvoke(test.code, inlinee.next(), blocksIterator, blocksToRemove, null);
        assert !blocksToRemove.isEmpty();
      }
    }
    test.code.removeBlocks(blocksToRemove);
    result = test.run();
    assertEquals(Integer.toString(expectedB), result);
  }

  @Test
  public void inlineAlwaysThrowsMultipleWithControlFlow() {
    runInlineAlwaysThrowsMultipleWithControlFlow(0, false, -2, -2);
    runInlineAlwaysThrowsMultipleWithControlFlow(0, true, -2, -1);
    runInlineAlwaysThrowsMultipleWithControlFlow(1, false, -2, -2);
    runInlineAlwaysThrowsMultipleWithControlFlow(1, true, -2, -1);
    runInlineAlwaysThrowsMultipleWithControlFlow(2, false, -2, -2);
    runInlineAlwaysThrowsMultipleWithControlFlow(2, true, -2, -1);
  }

  private TestApplication codeForInlineWithHandlersCanThrow(int a, int b, int c,
      boolean twoGuards, boolean callerHasCatchAll, boolean inlineeHasCatchAll) {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    String secondGuard = "";
    String secondGuardCode = "";
    String callerCatchAllGuard = "";
    String callerCatchAllCode = "";

    if (twoGuards) {
      String secondGuardLabel = "catch2";
      secondGuard =
          "    .catch Ljava/lang/Exception; {:try_start .. :try_end} :" + secondGuardLabel;
      secondGuardCode =
          "    :" + secondGuardLabel + "\n" +
          "    const               p0, -12\n" +
          "    goto :return";
    }

    if (callerHasCatchAll) {
      String catchAllLabel = "catch_all";
      callerCatchAllGuard = "    .catchall {:try_start .. :try_end} :" + catchAllLabel;
      callerCatchAllCode =
          "    :" + catchAllLabel + "\n" +
          "    const               p0, -13\n" +
          "    goto :return";
    }

    MethodSignature signature = builder.addStaticMethod(
        "int",
        DEFAULT_METHOD_NAME,
        ImmutableList.of("int", "int", "int"),
        0,
        "    :try_start",
        "    invoke-static       { p0, p1, p2 }, LTest;->a(III)I",
        "    :try_end",
        "    move-result         p0",
        "    :return",
        "    return              p0",
        "    .catch Ljava/lang/NullPointerException; {:try_start .. :try_end} :catch",
        secondGuard,
        callerCatchAllGuard,
        "    :catch",
        "    const               p0, -11",
        "    goto :return",
        secondGuardCode,
        callerCatchAllCode
    );

    MethodSignature signatureA = builder.addStaticMethod(
        "int",
        "a",
        ImmutableList.of("int", "int", "int"),
        1,
        "    const               v0, 4",
        "    div-int             p0, v0, p0",
        "    const               v0, 8",
        "    div-int             p1, v0, p1",
        "    add-int             p0, p0, p1",
        "    const               v0, 16",
        "    div-int             p2, v0, p2",
        "    add-int             p0, p0, p2",
        "    const               v0, 3",
        "    if-ne               v0, p0, :not_three",
        "    new-instance v0, Ljava/lang/Exception;",
        "    invoke-direct { v0 }, Ljava/lang/Exception;-><init>()V",
        "    throw v0",
        "    :not_three",
        "    return              p0"
    );

    String inlineeSecondGuard = "";
    String inlineeSecondGuardCode = "";
    if (twoGuards) {
      String secondGuardLabel = "catch2";
      inlineeSecondGuard =
          "    .catch Ljava/lang/Exception; {:try_start .. :try_end} :" + secondGuardLabel;
      inlineeSecondGuardCode =
          "    :" + secondGuardLabel + "\n" +
          "    const               p0, -2\n" +
          "    goto :return";
    }

    String inlineeCatchAllGuard = "";
    String inlineeCatchAllCode = "";
    if (inlineeHasCatchAll) {
      String catchAllLabel = "catch_all";
      inlineeCatchAllGuard = "    .catchall {:try_start .. :try_end} :" + catchAllLabel;
      inlineeCatchAllCode =
          "    :" + catchAllLabel + "\n" +
          "    const               p0, -3\n" +
          "    goto :return";
    }

    MethodSignature signatureB = builder.addStaticMethod(
        "int",
        "b",
        ImmutableList.of("int", "int", "int"),
        1,
        "    :try_start",
        "    const               v0, 4",
        "    div-int             p0, v0, p0",
        "    const               v0, 8",
        "    div-int             p1, v0, p1",
        "    add-int             p0, p0, p1",
        "    const               v0, 16",
        "    div-int             p2, v0, p2",
        "    add-int             p0, p0, p2",
        "    const               v0, 3",
        "    if-ne               v0, p0, :not_three",
        "    new-instance v0, Ljava/lang/Exception;",
        "    invoke-direct { v0 }, Ljava/lang/Exception;-><init>()V",
        "    throw v0",
        "    :not_three",
        "    :try_end",
        "    :return",
        "    return              p0",
        "    .catch Ljava/lang/ArithmeticException; {:try_start .. :try_end} :catch",
        inlineeSecondGuard,
        inlineeCatchAllGuard,
        "    :catch",
        "    const/4             p0, -1",
        "    goto :return",
        inlineeSecondGuardCode,
        inlineeCatchAllCode
    );

    builder.addMainMethod(
        3,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    const               v1, " + a,
        "    const               v2, " + b,
        "    const               v3, " + c,
        "    :try_start",
        "    invoke-static       { v1, v2, v3 }, LTest;->method(III)I",
        "    :try_end",
        "    move-result         v1",
        "    :print_result",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(I)V",
        "    return-void",
        "    .catch Ljava/lang/ArithmeticException; {:try_start .. :try_end} :catch",
        "    .catch Ljava/lang/Exception; {:try_start .. :try_end} :catch2",
        "    .catchall {:try_start .. :try_end} :catch_all",
        "    :catch",
        "    const               v1, -21",
        "    goto :print_result",
        "    :catch2",
        "    const               v1, -22",
        "    goto :print_result",
        "    :catch_all",
        "    const               v1, -23",
        "    goto :print_result"
    );

    InternalOptions options = new InternalOptions();
    DexApplication application = buildApplication(builder, options);

    // Return the processed method for inspection.
    ValueNumberGenerator valueNumberGenerator = new ValueNumberGenerator();
    DexEncodedMethod method = getMethod(application, signature);
    IRCode code = method.buildIR(valueNumberGenerator, new InternalOptions());

    DexEncodedMethod methodA = getMethod(application, signatureA);
    IRCode codeA = methodA.buildIR(valueNumberGenerator, new InternalOptions());

    DexEncodedMethod methodB = getMethod(application, signatureB);
    IRCode codeB = methodB.buildIR(valueNumberGenerator, new InternalOptions());

    return new TestApplication(application, method, code,
        ImmutableList.of(codeA, codeB), valueNumberGenerator, options);
  }

  private void runInlineWithHandlersCanThrow(int a, int b, int c,
      boolean twoGuards, boolean callerHasCatchAll, boolean inlineeHasCatchAll,
      int expectedA, int expectedB) {
    // Run code without inlining.
    TestApplication test = codeForInlineWithHandlersCanThrow(
        a, b, c, twoGuards, callerHasCatchAll, inlineeHasCatchAll);
    String result = test.run();
    assertEquals(Integer.toString(expectedA), result);

    InstructionListIterator iterator;

    // Run code inlining a.
    test = codeForInlineWithHandlersCanThrow(
        a, b, c, twoGuards, callerHasCatchAll, inlineeHasCatchAll);
    iterator = test.code.blocks.get(1).listIterator();
    iterator.nextUntil(Instruction::isInvoke);
    iterator.previous();
    iterator.inlineInvoke(test.code, test.additionalCode.get(0));
    result = test.run();
    assertEquals(Integer.toString(expectedA), result);

    // Run code inlining b (where a is actually called).
    test = codeForInlineWithHandlersCanThrow(
        a, b, c, twoGuards, callerHasCatchAll, inlineeHasCatchAll);
    iterator = test.code.blocks.get(1).listIterator();
    iterator.nextUntil(Instruction::isInvoke);
    iterator.previous();
    iterator.inlineInvoke(test.code, test.additionalCode.get(1));
    result = test.run();
    assertEquals(Integer.toString(expectedB), result);
  }

  @Test
  public void inlineCanWithHandlersThrow() {
    // The base generated code will be:
    //
    //  int method(int a, int b, int c) {
    //    try {
    //      return a(a, b, c);  // Either a or b will be inlined here.
    //    } catch (NullPointerException e) {
    //      return -11;
    //    }
    //    // More handlers can be added.
    //  }
    //
    //  int a(int a, int b, int c) {
    //    int result = 4 / a + 8 / b + 16 / c;
    //    if (result == 3) throw new Exception();
    //    return result
    //  }
    //
    //  int b(int a, int b, int c) {
    //    try {
    //      int result = 4 / a + 8 / b + 16 / c;
    //      if (result == 3) throw new Exception();
    //      return result
    //    } catch (ArithmeticException e) {
    //      return -1;
    //    }
    //    // More handlers can be added.
    //  }
    //
    //  void main(String[] args) {
    //    try {
    //    } catch (ArithmeticException e) {
    //      return -21;
    //    } catch (Exception e) {
    //      return -22;
    //    } catch (Throwable e) {  // Smali/dex catchall.
    //      return -23;
    //    }
    //  }
    //
    // The flags (secondGuard, callerHasCatchAll and inlineeHasCatchAll) will add more catch
    // handlers
    List<Boolean> allBooleans = ImmutableList.of(true, false);
    for (boolean secondGuard : allBooleans) {
      for (boolean callerHasCatchAll : allBooleans) {
        for (boolean inlineeHasCatchAll : allBooleans) {
          // This throws no exception, but always returns 6.
          runInlineWithHandlersCanThrow(
              2, 4, 8, secondGuard, callerHasCatchAll, inlineeHasCatchAll, 6, 6);

          // This is result for calling a.
          int resulta =
              secondGuard ? -12
                  : (callerHasCatchAll ? -13 : -21);
          // This is result for calling b.
          int resultb = - 1;
          // This group all throw ArithmeticException.
          runInlineWithHandlersCanThrow(
              0, 4, 8, secondGuard, callerHasCatchAll, inlineeHasCatchAll, resulta, resultb);
          runInlineWithHandlersCanThrow(
              2, 0, 8, secondGuard, callerHasCatchAll, inlineeHasCatchAll, resulta, resultb);
          runInlineWithHandlersCanThrow(
              2, 4, 0, secondGuard, callerHasCatchAll, inlineeHasCatchAll, resulta, resultb);
        }
      }
    }

    // The following group will throw Exception from the inlinee.
    runInlineWithHandlersCanThrow(4, 8, 16, false, false, false, -22, -22);
    runInlineWithHandlersCanThrow(4, 8, 16, true, false, false, -12, -2);
    runInlineWithHandlersCanThrow(4, 8, 16, false, true, false, -13, -13);
    runInlineWithHandlersCanThrow(4, 8, 16, true, true, false, -12, -2);
    runInlineWithHandlersCanThrow(4, 8, 16, false, false, true, -22, -3);
    runInlineWithHandlersCanThrow(4, 8, 16, true, false, true, -12, -2);
    runInlineWithHandlersCanThrow(4, 8, 16, false, true, true, -13, -3);
    runInlineWithHandlersCanThrow(4, 8, 16, true, true, true, -12, -2);
  }
}
