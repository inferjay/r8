// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.ir.code.Add;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.ConstNumber;
import com.android.tools.r8.ir.code.ConstType;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.MoveType;
import com.android.tools.r8.ir.code.NumericType;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.code.ValueNumberGenerator;
import com.android.tools.r8.smali.SmaliTestBase;
import com.android.tools.r8.utils.InternalOptions;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;

public class SplitBlockTest extends SmaliTestBase {

  TestApplication codeWithoutCatchHandlers() {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    String returnType = "int";
    List<String> parameters = ImmutableList.of("int", "int");
    MethodSignature signature = builder.addStaticMethod(
        returnType,
        DEFAULT_METHOD_NAME,
        parameters,
        0,
        "    add-int             p0, p0, p0",
        "    sub-int             p1, p1, p0",
        "    mul-int             p0, p0, p1",
        "    return              p0"
    );

    builder.addMainMethod(
        3,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    const/4             v1, 1",
        "    const/4             v2, 5",
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

    return new TestApplication(application, method, code, valueNumberGenerator, options);
  }

  @Test
  public void noCatchHandlers() {
    final int initialBlockCount = 1;
    final int argumentInstructions = 2;
    final int firstBlockInstructions = 6;
    // Try split between all non-argument instructions in the first block.
    for (int i = argumentInstructions; i < firstBlockInstructions; i++) {
      TestApplication test = codeWithoutCatchHandlers();
      IRCode code = test.code;
      assertEquals(initialBlockCount, code.blocks.size());

      BasicBlock block = code.blocks.get(0);
      int instructionCount = block.getInstructions().size();
      assertEquals(firstBlockInstructions, instructionCount);

      assertEquals(argumentInstructions, test.countArgumentInstructions());
      assertEquals(firstBlockInstructions, block.getInstructions().size());
      assertTrue(!block.getInstructions().get(i).isArgument());

      InstructionListIterator iterator = test.listIteratorAt(block, i);
      BasicBlock newBlock = iterator.split(code);
      assertTrue(code.isConsistentSSA());

      assertEquals(initialBlockCount + 1, code.blocks.size());
      assertEquals(i + 1, code.blocks.get(0).getInstructions().size());
      assertEquals(instructionCount - i, code.blocks.get(1).getInstructions().size());
      assertSame(newBlock, code.blocks.get(1));

      // Run code and check result (code in the test object is updated).
      String result = test.run();
      assertEquals("6", result);
    }
  }

  @Test
  public void noCatchHandlersSplitThree() {
    final int initialBlockCount = 1;
    final int argumentInstructions = 2;
    final int firstBlockInstructions = 6;
    // Try split out all non-argument instructions in the first block.
    for (int i = argumentInstructions; i < firstBlockInstructions - 1; i++) {
      TestApplication test = codeWithoutCatchHandlers();
      IRCode code = test.code;
      assertEquals(initialBlockCount, code.blocks.size());

      BasicBlock block = code.blocks.get(0);
      int instructionCount = block.getInstructions().size();
      assertEquals(firstBlockInstructions, instructionCount);

      assertEquals(argumentInstructions, test.countArgumentInstructions());
      assertEquals(firstBlockInstructions, block.getInstructions().size());
      assertTrue(!block.getInstructions().get(i).isArgument());

      InstructionListIterator iterator = test.listIteratorAt(block, i);
      BasicBlock newBlock = iterator.split(1, code);
      assertTrue(code.isConsistentSSA());

      assertEquals(initialBlockCount + 2, code.blocks.size());
      assertEquals(i + 1, code.blocks.get(0).getInstructions().size());
      assertEquals(2, code.blocks.get(1).getInstructions().size());
      assertEquals(instructionCount - i - 1, code.blocks.get(2).getInstructions().size());
      assertSame(newBlock, code.blocks.get(1));

      // Run code and check result (code in the test object is updated).
      String result = test.run();
      assertEquals("6", result);
    }
  }

  TestApplication codeWithCatchHandlers(boolean shouldThrow, boolean twoGuards) {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    String secondGuard = twoGuards ?
        "    .catch Ljava/lang/Exception; {:try_start .. :try_end} :catch" : "    ";

    String returnType = "int";
    List<String> parameters = ImmutableList.of("int", "int");
    MethodSignature signature = builder.addStaticMethod(
        returnType,
        DEFAULT_METHOD_NAME,
        parameters,
        0,
        "    :try_start",
        "    add-int             p0, p0, p0",
        "    add-int             p1, p1, p1",
        "    div-int             p0, p0, p1",
        "    :try_end",
        "    .catch Ljava/lang/ArithmeticException; {:try_start .. :try_end} :catch",
        secondGuard,
        "    :return",
        "    return              p0",
        "    :catch",
        "    const/4             p0, -1",
        "    goto :return"
    );

    builder.addMainMethod(
        3,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    const/4             v1, 2",
        "    const/4             v2, " + (shouldThrow ? "0" : "1"),
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
    return new TestApplication(application, method, code, valueNumberGenerator, options);
  }

  public void hasCatchandlerIfThrowing(BasicBlock block) {
    boolean throwing = false;
    for (Instruction instruction : block.getInstructions()) {
      throwing |= instruction.instructionTypeCanThrow();
    }
    assertEquals(throwing, block.hasCatchHandlers());
  }

  public void runCatchHandlerTest(boolean codeThrows, boolean twoGuards) {
    final int secondBlockInstructions = 4;
    final int initialBlockCount = 5;
    // Try split between all instructions in second block.
    for (int i = 1; i < secondBlockInstructions; i++) {
      TestApplication test = codeWithCatchHandlers(codeThrows, twoGuards);
      IRCode code = test.code;
      assertEquals(initialBlockCount, code.blocks.size());

      BasicBlock block = code.blocks.get(1);
      int instructionCount = block.getInstructions().size();
      assertEquals(secondBlockInstructions, instructionCount);

      InstructionListIterator iterator = test.listIteratorAt(block, i);
      BasicBlock newBlock = iterator.split(code);
      assertTrue(code.isConsistentSSA());

      assertEquals(initialBlockCount + 1, code.blocks.size());
      assertEquals(i + 1, code.blocks.get(1).getInstructions().size());
      assertEquals(instructionCount - i, newBlock.getInstructions().size());
      assertSame(newBlock, code.blocks.get(2));

      code.blocks.forEach(this::hasCatchandlerIfThrowing);

      // Run code and check result (code in the test object is updated).
      String result = test.run();
      assertEquals(codeThrows ? "-1" : "2", result);
    }
  }

  @Test
  public void catchHandlers() {
    runCatchHandlerTest(false, false);
    runCatchHandlerTest(true, false);
    runCatchHandlerTest(false, true);
    runCatchHandlerTest(true, true);
  }

  public void runCatchHandlerSplitThreeTest(boolean codeThrows, boolean twoGuards) {
    final int secondBlockInstructions = 4;
    final int initialBlockCount = 5;
    // Try split out all instructions in second block.
    for (int i = 1; i < secondBlockInstructions - 1; i++) {
      TestApplication test = codeWithCatchHandlers(codeThrows, twoGuards);
      IRCode code = test.code;
      assertEquals(initialBlockCount, code.blocks.size());

      BasicBlock block = code.blocks.get(1);
      int instructionCount = block.getInstructions().size();
      assertEquals(secondBlockInstructions, instructionCount);

      InstructionListIterator iterator = test.listIteratorAt(block, i);
      BasicBlock newBlock = iterator.split(1, code);
      assertTrue(code.isConsistentSSA());

      assertEquals(initialBlockCount + 2, code.blocks.size());
      assertEquals(i + 1, code.blocks.get(1).getInstructions().size());
      assertEquals(2, newBlock.getInstructions().size());
      assertEquals(instructionCount - i - 1, code.blocks.get(3).getInstructions().size());
      assertSame(newBlock, code.blocks.get(2));

      code.blocks.forEach(this::hasCatchandlerIfThrowing);

      // Run code and check result (code in the test object is updated).
      String result = test.run();
      assertEquals(codeThrows ? "-1" : "2", result);
    }
  }

  @Test
  public void catchHandlersSplitThree() {
    runCatchHandlerSplitThreeTest(false, false);
    runCatchHandlerSplitThreeTest(true, false);
    runCatchHandlerSplitThreeTest(false, true);
    runCatchHandlerSplitThreeTest(true, true);
  }

  TestApplication codeWithIf(boolean hitTrueBranch) {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    String returnType = "int";
    List<String> parameters = ImmutableList.of("int", "int");
    MethodSignature signature = builder.addStaticMethod(
        returnType,
        DEFAULT_METHOD_NAME,
        parameters,
        0,
        "    if-eq               p0, p1, :eq",
        "    const/4             p0, 1",
        "    return              p0",
        "    :eq",
        "    const/4             p0, 0",
        "    return              p0"
    );

    builder.addMainMethod(
        3,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    const/4             v1, 2",
        "    const/4             v2, " + (hitTrueBranch ? "2" : "3"),
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

    return new TestApplication(application, method, code, valueNumberGenerator, options);
  }

  public void runWithIfTest(boolean hitTrueBranch) {
    final int initialBlockCount = 4;
    final int argumentInstructions = 2;
    final int firstBlockInstructions = 3;
    // Try split between all non-argument instructions in the first block.
    for (int i = argumentInstructions; i < firstBlockInstructions; i++) {
      TestApplication test = codeWithIf(hitTrueBranch);
      IRCode code = test.code;
      assertEquals(initialBlockCount, code.blocks.size());

      BasicBlock block = code.blocks.get(0);
      int instructionCount = block.getInstructions().size();
      assertEquals(firstBlockInstructions, instructionCount);

      assertEquals(argumentInstructions, test.countArgumentInstructions());
      assertEquals(firstBlockInstructions, block.getInstructions().size());
      assertTrue(!block.getInstructions().get(i).isArgument());

      InstructionListIterator iterator = test.listIteratorAt(block, i);
      BasicBlock newBlock = iterator.split(code);
      assertTrue(code.isConsistentSSA());

      assertEquals(initialBlockCount + 1, code.blocks.size());
      assertEquals(i + 1, code.blocks.get(0).getInstructions().size());
      assertEquals(instructionCount - i, newBlock.getInstructions().size());
      assertSame(newBlock, code.blocks.get(1));

      // Run code and check result (code in the test object is updated).
      String result = test.run();
      assertEquals(hitTrueBranch ? "0" : "1", result);
    }
  }

  @Test
  public void withIf() {
    runWithIfTest(false);
    runWithIfTest(true);
  }

  public void splitBeforeReturn(boolean hitTrueBranch) {
    TestApplication test = codeWithIf(hitTrueBranch);
    IRCode code = test.code;
    // Locate the exit block and split before the return (the first instruction in the block).
    BasicBlock originalReturnBlock = code.getNormalExitBlock();
    BasicBlock newReturnBlock = originalReturnBlock.listIterator().split(code);
    // Modify the code to make the inserted block add the constant 10 to the original return value.
    Value newConstValue = new Value(test.valueNumberGenerator.next(), MoveType.SINGLE, null);
    Value newReturnValue = new Value(test.valueNumberGenerator.next(), MoveType.SINGLE, null);
    Value oldReturnValue = newReturnBlock.listIterator().next().asReturn().returnValue();
    newReturnBlock.listIterator().next().asReturn().returnValue().replaceUsers(newReturnValue);
    Instruction constInstruction = new ConstNumber(ConstType.INT, newConstValue, 10);
    Instruction addInstruction = new Add(
        NumericType.INT,
        newReturnValue,
        oldReturnValue,
        newConstValue);
    InstructionListIterator iterator = originalReturnBlock.listIterator();
    iterator.add(constInstruction);
    iterator.add(addInstruction);

    // Run code and check result (code in the test object is updated).
    String result = test.run();
    assertEquals(hitTrueBranch ? "10" : "11", result);
  }

  @Test
  public void splitBeforeReturn() {
    splitBeforeReturn(false);
    splitBeforeReturn(true);
  }

  TestApplication codeWithSwitch(boolean hitCase) {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    String returnType = "int";
    List<String> parameters = ImmutableList.of("int");
    MethodSignature signature = builder.addStaticMethod(
        returnType,
        DEFAULT_METHOD_NAME,
        parameters,
        0,
        "    packed-switch       p0, :packed_switch_data",
        "    const/4             p0, 0x5",
        "    goto                :return",
        "    :case_0",
        "    const/4             p0, 0x2",
        "    goto                :return",
        "    :case_1",
        "    const/4             p0, 0x3",
        "    :return",
        "    return              p0",
        "    :packed_switch_data",
        "    .packed-switch 0x0",
        "      :case_0",
        "      :case_1",
        "    .end packed-switch"
    );

    builder.addMainMethod(
        2,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    const/4             v1, " + (hitCase ? "1" : "2"),
        "    invoke-static       { v1 }, LTest;->method(I)I",
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

    return new TestApplication(application, method, code, valueNumberGenerator, options);
  }

  public void runWithSwitchTest(boolean hitCase) {
    final int initialBlockCount = 5;
    final int argumentInstructions = 1;
    final int firstBlockInstructions = 2;
    // Try split between all non-argument instructions in the first block.
    for (int i = argumentInstructions; i < firstBlockInstructions; i++) {
      TestApplication test = codeWithSwitch(hitCase);
      IRCode code = test.code;
      assertEquals(initialBlockCount, code.blocks.size());

      BasicBlock block = code.blocks.get(0);
      int instructionCount = block.getInstructions().size();
      assertEquals(firstBlockInstructions, instructionCount);

      assertEquals(argumentInstructions, test.countArgumentInstructions());
      assertEquals(firstBlockInstructions, block.getInstructions().size());
      assertTrue(!block.getInstructions().get(i).isArgument());

      InstructionListIterator iterator = test.listIteratorAt(block, i);
      BasicBlock newBlock = iterator.split(code);
      assertTrue(code.isConsistentSSA());

      assertEquals(initialBlockCount + 1, code.blocks.size());
      assertEquals(i + 1, code.blocks.get(0).getInstructions().size());
      assertEquals(instructionCount - i, newBlock.getInstructions().size());
      assertSame(newBlock, code.blocks.get(1));

      // Run code and check result (code in the test object is updated).
      String result = test.run();
      assertEquals(hitCase ? "3" : "5", result);
    }
  }

  @Test
  public void withSwitch() {
    runWithSwitchTest(false);
    runWithSwitchTest(true);
  }
}
