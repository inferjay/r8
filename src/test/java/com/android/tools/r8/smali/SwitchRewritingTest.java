// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.smali;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.android.tools.r8.R8;
import com.android.tools.r8.code.Const4;
import com.android.tools.r8.code.IfEq;
import com.android.tools.r8.code.IfEqz;
import com.android.tools.r8.code.PackedSwitch;
import com.android.tools.r8.code.SparseSwitch;
import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexCode;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.ir.code.Switch;
import com.android.tools.r8.jasmin.JasminBuilder;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.StringUtils;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class SwitchRewritingTest extends SmaliTestBase {

  private void runSingleCaseDexTest(Switch.Type type, int key) {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);
    String switchInstruction;
    String switchData;
    if (type == Switch.Type.PACKED) {
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
      assertTrue(code.instructions[0] instanceof Const4);
      assertTrue(code.instructions[1] instanceof IfEq);
    }
  }

  @Test
  public void singleCaseDex() {
    runSingleCaseDexTest(Switch.Type.PACKED, 0);
    runSingleCaseDexTest(Switch.Type.SPARSE, 0);
    runSingleCaseDexTest(Switch.Type.PACKED, 1);
    runSingleCaseDexTest(Switch.Type.SPARSE, 1);
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
    if (key1 + 1 == key2) {
      assertTrue(code.instructions[0] instanceof PackedSwitch);
    } else {
      assertTrue(code.instructions[0] instanceof SparseSwitch);
    }
  }

  @Test
  public void twoCaseSparseToPackedDex() {
    runTwoCaseSparseToPackedDexTest(0, 1);
    runTwoCaseSparseToPackedDexTest(-1, 0);
    runTwoCaseSparseToPackedDexTest(Integer.MIN_VALUE, Integer.MIN_VALUE + 1);
    runTwoCaseSparseToPackedDexTest(Integer.MAX_VALUE - 1, Integer.MAX_VALUE);
    runTwoCaseSparseToPackedDexTest(0, 2);
    runTwoCaseSparseToPackedDexTest(-1, 1);
    runTwoCaseSparseToPackedDexTest(Integer.MIN_VALUE, Integer.MIN_VALUE + 2);
    runTwoCaseSparseToPackedDexTest(Integer.MAX_VALUE - 2, Integer.MAX_VALUE);
  }

  private void runSingleCaseJarTest(Switch.Type type, int key) throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    String switchCode;
    if (type == Switch.Type.PACKED) {
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
      assertTrue(code.instructions[1] instanceof IfEq);
      assertTrue(code.instructions[2] instanceof Const4);
    }
  }

  @Test
  public void singleCaseJar() throws Exception {
    runSingleCaseJarTest(Switch.Type.PACKED, 0);
    runSingleCaseJarTest(Switch.Type.SPARSE, 0);
    runSingleCaseJarTest(Switch.Type.PACKED, 1);
    runSingleCaseJarTest(Switch.Type.SPARSE, 1);
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
    if (key1 + 1 == key2) {
      assertTrue(code.instructions[0] instanceof PackedSwitch);
    } else {
      assertTrue(code.instructions[0] instanceof SparseSwitch);
    }
  }

  @Test
  public void twoCaseSparseToPackedJar() throws Exception {
    runTwoCaseSparseToPackedJarTest(0, 1);
    runTwoCaseSparseToPackedJarTest(-1, 0);
    runTwoCaseSparseToPackedJarTest(Integer.MIN_VALUE, Integer.MIN_VALUE + 1);
    runTwoCaseSparseToPackedJarTest(Integer.MAX_VALUE - 1, Integer.MAX_VALUE);
    runTwoCaseSparseToPackedJarTest(0, 2);
    runTwoCaseSparseToPackedJarTest(-1, 1);
    runTwoCaseSparseToPackedJarTest(Integer.MIN_VALUE, Integer.MIN_VALUE + 2);
    runTwoCaseSparseToPackedJarTest(Integer.MAX_VALUE - 2, Integer.MAX_VALUE);
  }
}
