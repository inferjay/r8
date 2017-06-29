// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.smali;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.android.tools.r8.R8;
import com.android.tools.r8.code.Const;
import com.android.tools.r8.code.Const4;
import com.android.tools.r8.code.ConstHigh16;
import com.android.tools.r8.code.IfEq;
import com.android.tools.r8.code.IfEqz;
import com.android.tools.r8.code.Instruction;
import com.android.tools.r8.code.PackedSwitch;
import com.android.tools.r8.code.SparseSwitch;
import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexCode;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.jasmin.JasminBuilder;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.StringUtils;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class SwitchRewritingTest extends SmaliTestBase {

  private boolean twoCaseWillUsePackedSwitch(int key1, int key2) {
    return Math.abs((long) key1 - (long) key2) <= 2;
  }

  private boolean some16BitConst(Instruction instruction) {
    return instruction instanceof Const4
        || instruction instanceof ConstHigh16
        || instruction instanceof Const;
  }
  private void runSingleCaseDexTest(boolean packed, int key) {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);
    String switchInstruction;
    String switchData;
    if (packed) {
      switchInstruction = "packed-switch";
      switchData = StringUtils.join(
          "\n",
          "  :switch_data",
          "  .packed-switch " + key,
          "    :case_0",
          "  .end packed-switch");
    } else {
      switchInstruction = "sparse-switch";
      switchData = StringUtils.join(
          "\n",
          "  :switch_data",
          "  .sparse-switch",
          "    " + key + " -> :case_0",
          "  .end sparse-switch");
    }
    MethodSignature signature = builder.addStaticMethod(
        "int",
        DEFAULT_METHOD_NAME,
        ImmutableList.of("int"),
        0,
        "    " + switchInstruction + " p0, :switch_data",
        "    const/4 p0, 0x5",
        "    goto :return",
        "  :case_0",
        "    const/4 p0, 0x3",
        "  :return",
        "    return p0",
        switchData);

    builder.addMainMethod(
        2,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    const/4             v1, 0",
        "    invoke-static       { v1 }, LTest;->method(I)I",
        "    move-result         v1",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(I)V",
        "    return-void"
    );

    InternalOptions options = new InternalOptions();
    DexApplication originalApplication = buildApplication(builder, options);
    DexApplication processedApplication = processApplication(originalApplication, options);
    DexEncodedMethod method = getMethod(processedApplication, signature);
    DexCode code = method.getCode().asDexCode();

    if (key == 0) {
      assertEquals(5, code.instructions.length);
      assertTrue(code.instructions[0] instanceof IfEqz);
    } else {
      assertEquals(6, code.instructions.length);
      assertTrue(some16BitConst(code.instructions[0]));
      assertTrue(code.instructions[1] instanceof IfEq);
    }
  }

  @Test
  public void singleCaseDex() {
    for (boolean packed : new boolean[]{true, false}) {
      runSingleCaseDexTest(packed, Integer.MIN_VALUE);
      runSingleCaseDexTest(packed, -1);
      runSingleCaseDexTest(packed, 0);
      runSingleCaseDexTest(packed, 1);
      runSingleCaseDexTest(packed, Integer.MAX_VALUE);
    }
  }

  private void runTwoCaseSparseToPackedDexTest(int key1, int key2) {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    MethodSignature signature = builder.addStaticMethod(
        "int",
        DEFAULT_METHOD_NAME,
        ImmutableList.of("int"),
        0,
        "    sparse-switch p0, :sparse_switch_data",
        "    const/4 v0, 0x5",
        "    goto :return",
        "  :case_1",
        "    const/4 v0, 0x3",
        "    goto :return",
        "  :case_2",
        "    const/4 v0, 0x4",
        "  :return",
        "    return v0",
        "  :sparse_switch_data",
        "  .sparse-switch",
        "    " + key1 + " -> :case_1",
        "    " + key2 + " -> :case_2",
        "  .end sparse-switch");

    builder.addMainMethod(
        2,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    const/4             v1, 0",
        "    invoke-static       { v1 }, LTest;->method(I)I",
        "    move-result         v1",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(I)V",
        "    return-void"
    );

    InternalOptions options = new InternalOptions();
    DexApplication originalApplication = buildApplication(builder, options);
    DexApplication processedApplication = processApplication(originalApplication, options);
    DexEncodedMethod method = getMethod(processedApplication, signature);
    DexCode code = method.getCode().asDexCode();
    if (twoCaseWillUsePackedSwitch(key1, key2)) {
      assertTrue(code.instructions[0] instanceof PackedSwitch);
    } else {
      assertTrue(code.instructions[0] instanceof SparseSwitch);
    }
  }

  @Test
  public void twoCaseSparseToPackedDex() {
    for (int delta = 1; delta <= 3; delta++) {
      runTwoCaseSparseToPackedDexTest(0, delta);
      runTwoCaseSparseToPackedDexTest(-delta, 0);
      runTwoCaseSparseToPackedDexTest(Integer.MIN_VALUE, Integer.MIN_VALUE + delta);
      runTwoCaseSparseToPackedDexTest(Integer.MAX_VALUE - delta, Integer.MAX_VALUE);
    }
    runTwoCaseSparseToPackedDexTest(-1, 1);
    runTwoCaseSparseToPackedDexTest(-2, 1);
    runTwoCaseSparseToPackedDexTest(-1, 2);
    runTwoCaseSparseToPackedDexTest(Integer.MIN_VALUE, Integer.MAX_VALUE);
    runTwoCaseSparseToPackedDexTest(Integer.MIN_VALUE + 1, Integer.MAX_VALUE);
    runTwoCaseSparseToPackedDexTest(Integer.MIN_VALUE, Integer.MAX_VALUE - 1);
  }

  private void runLargerSwitchDexTest(int firstKey, int keyStep, int totalKeys,
      Integer additionalLastKey) throws Exception {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    StringBuilder switchSource = new StringBuilder();
    StringBuilder targetCode = new StringBuilder();
    for (int i = 0; i < totalKeys; i++) {
      String caseLabel = "case_" + i;
      switchSource.append("    " + (firstKey + i * keyStep) + " -> :" + caseLabel + "\n");
      targetCode.append("  :" + caseLabel + "\n");
      targetCode.append("    goto :return\n");
    }
    if (additionalLastKey != null) {
      String caseLabel = "case_" + totalKeys;
      switchSource.append("    " + additionalLastKey + " -> :" + caseLabel + "\n");
      targetCode.append("  :" + caseLabel + "\n");
      targetCode.append("    goto :return\n");
    }

    MethodSignature signature = builder.addStaticMethod(
        "void",
        DEFAULT_METHOD_NAME,
        ImmutableList.of("int"),
        0,
        "    sparse-switch p0, :sparse_switch_data",
        "    goto :return",
        targetCode.toString(),
        "  :return",
        "    return-void",
        "  :sparse_switch_data",
        "  .sparse-switch",
        switchSource.toString(),
        "  .end sparse-switch");

    builder.addMainMethod(
        2,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    const/4             v1, 0",
        "    invoke-static       { v1 }, LTest;->method(I)V",
        "    return-void"
    );

    InternalOptions options = new InternalOptions();
    options.verbose = true;
    options.printTimes = true;
    DexApplication originalApplication = buildApplication(builder, options);
    DexApplication processedApplication = processApplication(originalApplication, options);
    DexEncodedMethod method = getMethod(processedApplication, signature);
    DexCode code = method.getCode().asDexCode();
    if (keyStep <= 2) {
      assertTrue(code.instructions[0] instanceof PackedSwitch);
    } else {
      assertTrue(code.instructions[0] instanceof SparseSwitch);
    }
  }

  @Test
  public void twoMonsterSparseToPackedDex() throws Exception {
    runLargerSwitchDexTest(0, 1, 100, null);
    runLargerSwitchDexTest(0, 2, 100, null);
    runLargerSwitchDexTest(0, 3, 100, null);
    runLargerSwitchDexTest(100, 100, 100, null);
    runLargerSwitchDexTest(-10000, 100, 100, null);
    runLargerSwitchDexTest(-10000, 200, 100, 10000);
    runLargerSwitchDexTest(
        Integer.MIN_VALUE, (int) ((-(long)Integer.MIN_VALUE) / 16), 32, Integer.MAX_VALUE);

    // TODO(63090177): Currently this is commented out as R8 gets really slow for large switches.
    // runLargerSwitchDexTest(0, 1, Constants.U16BIT_MAX, null);
  }

  private void runSingleCaseJarTest(boolean packed, int key) throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    String switchCode;
    if (packed) {
      switchCode = StringUtils.join(
          "\n",
          "    tableswitch " + key,
          "      case_0",
          "      default : case_default");
    } else {
      switchCode = StringUtils.join(
          "\n",
          "    lookupswitch",
          "      " + key + " : case_0",
          "      default : case_default");
    }

    clazz.addStaticMethod("test", ImmutableList.of("I"), "I",
        "    .limit stack 1",
        "    .limit locals 1",
        "    iload 0",
        switchCode,
        "  case_0:",
        "    iconst_3",
        "    goto return_",
        "  case_default:",
        "    ldc 5",
        "  return_:",
        "    ireturn");

    clazz.addMainMethod(
        "    .limit stack 2",
        "    .limit locals 1",
        "    getstatic java/lang/System/out Ljava/io/PrintStream;",
        "    ldc 2",
        "    invokestatic Test/test(I)I",
        "    invokevirtual java/io/PrintStream/print(I)V",
        "    return");

    DexApplication app = builder.read();
    app = new R8(new InternalOptions()).optimize(app, new AppInfoWithSubtyping(app));

    MethodSignature signature = new MethodSignature("Test", "test", "int", ImmutableList.of("int"));
    DexEncodedMethod method = getMethod(app, signature);
    DexCode code = method.getCode().asDexCode();
    if (key == 0) {
      assertEquals(5, code.instructions.length);
      assertTrue(code.instructions[0] instanceof IfEqz);
    } else {
      assertEquals(6, code.instructions.length);
      assertTrue(some16BitConst(code.instructions[0]));
      assertTrue(code.instructions[1] instanceof IfEq);
      assertTrue(code.instructions[2] instanceof Const4);
    }
  }

  @Test
  public void singleCaseJar() throws Exception {
    for (boolean packed : new boolean[]{true, false}) {
      runSingleCaseJarTest(packed, Integer.MIN_VALUE);
      runSingleCaseJarTest(packed, -1);
      runSingleCaseJarTest(packed, 0);
      runSingleCaseJarTest(packed, 1);
      runSingleCaseJarTest(packed, Integer.MAX_VALUE);
    }
  }

  private void runTwoCaseSparseToPackedJarTest(int key1, int key2) throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    clazz.addStaticMethod("test", ImmutableList.of("I"), "I",
        "    .limit stack 1",
        "    .limit locals 1",
        "    iload 0",
        "    lookupswitch",
        "      " + key1 + " : case_1",
        "      " + key2 + " : case_2",
        "      default : case_default",
        "  case_1:",
        "    iconst_3",
        "    goto return_",
        "  case_2:",
        "    iconst_4",
        "    goto return_",
        "  case_default:",
        "    iconst_5",
        "  return_:",
        "    ireturn");

    clazz.addMainMethod(
        "    .limit stack 2",
        "    .limit locals 1",
        "    getstatic java/lang/System/out Ljava/io/PrintStream;",
        "    ldc 2",
        "    invokestatic Test/test(I)I",
        "    invokevirtual java/io/PrintStream/print(I)V",
        "    return");

    DexApplication app = builder.read();
    app = new R8(new InternalOptions()).optimize(app, new AppInfoWithSubtyping(app));

    MethodSignature signature = new MethodSignature("Test", "test", "int", ImmutableList.of("int"));
    DexEncodedMethod method = getMethod(app, signature);
    DexCode code = method.getCode().asDexCode();
    if (twoCaseWillUsePackedSwitch(key1, key2)) {
      assertTrue(code.instructions[0] instanceof PackedSwitch);
    } else {
      assertTrue(code.instructions[0] instanceof SparseSwitch);
    }
  }

  @Test
  public void twoCaseSparseToPackedJar() throws Exception {
    for (int delta = 1; delta <= 3; delta++) {
      runTwoCaseSparseToPackedJarTest(0, delta);
      runTwoCaseSparseToPackedJarTest(-delta, 0);
      runTwoCaseSparseToPackedJarTest(Integer.MIN_VALUE, Integer.MIN_VALUE + delta);
      runTwoCaseSparseToPackedJarTest(Integer.MAX_VALUE - delta, Integer.MAX_VALUE);
    }
    runTwoCaseSparseToPackedJarTest(-1, 1);
    runTwoCaseSparseToPackedJarTest(-2, 1);
    runTwoCaseSparseToPackedJarTest(-1, 2);
    runTwoCaseSparseToPackedJarTest(Integer.MIN_VALUE, Integer.MAX_VALUE);
    runTwoCaseSparseToPackedJarTest(Integer.MIN_VALUE + 1, Integer.MAX_VALUE);
    runTwoCaseSparseToPackedJarTest(Integer.MIN_VALUE, Integer.MAX_VALUE - 1);
  }

  private void runLargerSwitchJarTest(int firstKey, int keyStep, int totalKeys,
      Integer additionalLastKey) throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    StringBuilder switchSource = new StringBuilder();
    StringBuilder targetCode = new StringBuilder();
    for (int i = 0; i < totalKeys; i++) {
      String caseLabel = "case_" + i;
      switchSource.append("      " + (firstKey + i * keyStep) + " : " + caseLabel + "\n");
      targetCode.append("  " + caseLabel + ":\n");
      targetCode.append("    ldc " + i + "\n");
      targetCode.append("    goto return_\n");
    }
    if (additionalLastKey != null) {
      String caseLabel = "case_" + totalKeys;
      switchSource.append("      " + additionalLastKey + " : " + caseLabel + "\n");
      targetCode.append("  " + caseLabel + ":\n");
      targetCode.append("    ldc " + totalKeys + "\n");
      targetCode.append("    goto return_\n");
    }

    clazz.addStaticMethod("test", ImmutableList.of("I"), "I",
        "    .limit stack 1",
        "    .limit locals 1",
        "    iload 0",
        "  lookupswitch",
        switchSource.toString(),
        "      default : case_default",
        targetCode.toString(),
        "  case_default:",
        "    iconst_5",
        "  return_:",
        "    ireturn");

    clazz.addMainMethod(
        "    .limit stack 2",
        "    .limit locals 1",
        "    getstatic java/lang/System/out Ljava/io/PrintStream;",
        "    ldc 2",
        "    invokestatic Test/test(I)I",
        "    invokevirtual java/io/PrintStream/print(I)V",
        "    return");

    DexApplication app = builder.read();
    app = new R8(new InternalOptions()).optimize(app, new AppInfoWithSubtyping(app));

    MethodSignature signature = new MethodSignature("Test", "test", "int", ImmutableList.of("int"));
    DexEncodedMethod method = getMethod(app, signature);
    DexCode code = method.getCode().asDexCode();
    if (keyStep <= 2) {
      assertTrue(code.instructions[0] instanceof PackedSwitch);
    } else {
      assertTrue(code.instructions[0] instanceof SparseSwitch);
    }
  }

  @Test
  public void largerSwitchJar() throws Exception {
    runLargerSwitchJarTest(0, 1, 100, null);
    runLargerSwitchJarTest(0, 2, 100, null);
    runLargerSwitchJarTest(0, 3, 100, null);
    runLargerSwitchJarTest(100, 100, 100, null);
    runLargerSwitchJarTest(-10000, 100, 100, null);
    runLargerSwitchJarTest(-10000, 200, 100, 10000);
    runLargerSwitchJarTest(
        Integer.MIN_VALUE, (int) ((-(long)Integer.MIN_VALUE) / 16), 32, Integer.MAX_VALUE);

    // This is the maximal value possible with Jasmin with the generated code above. It depends on
    // the source, so making smaller source can raise this limit. However we never get close to the
    // class file max.
    runLargerSwitchJarTest(0, 1, 5503, null);
  }
}
