// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.smali;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.android.tools.r8.code.Const4;
import com.android.tools.r8.code.IfEqz;
import com.android.tools.r8.code.IfGez;
import com.android.tools.r8.code.IfGtz;
import com.android.tools.r8.code.IfLez;
import com.android.tools.r8.code.IfLtz;
import com.android.tools.r8.code.IfNez;
import com.android.tools.r8.code.InvokeVirtual;
import com.android.tools.r8.code.Return;
import com.android.tools.r8.code.ReturnObject;
import com.android.tools.r8.graph.DexCode;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.ir.code.If.Type;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class IfSimplificationTest extends SmaliTestBase {

  static String[] ifOpcode;
  static {
    ifOpcode = new String[6];
    ifOpcode[Type.EQ.ordinal()] = "if-eq";
    ifOpcode[Type.NE.ordinal()] = "if-ne";
    ifOpcode[Type.LE.ordinal()] = "if-le";
    ifOpcode[Type.GE.ordinal()] = "if-ge";
    ifOpcode[Type.LT.ordinal()] = "if-lt";
    ifOpcode[Type.GT.ordinal()] = "if-gt";
  }

  @Test
  public void ifZeroNeqZero() {
    DexEncodedMethod method = oneMethodApplication(
        "int",
        Collections.emptyList(),
        1,
        "  const v0, 0",
        "  if-nez v0, :label_2",
        ":label_1",
        "  return v0",
        ":label_2",
        "  const v0, 1",
        "  goto :label_1");
    DexCode code = method.getCode().asDexCode();
    assertEquals(2, code.instructions.length);
    assertTrue(code.instructions[0] instanceof Const4);
    assertEquals(0, ((Const4) code.instructions[0]).B);
    assertTrue(code.instructions[1] instanceof Return);
  }

  @Test
  public void ifTwoEqZero() {
    DexEncodedMethod method = oneMethodApplication(
        "int",
        Collections.emptyList(),
        1,
        "  const v0, 2",
        "  if-eqz v0, :label_2",
        ":label_1",
        "  return v0",
        ":label_2",
        "  const v0, 1",
        "  goto :label_1");
    DexCode code = method.getCode().asDexCode();
    assertEquals(2, code.instructions.length);
    assertTrue(code.instructions[0] instanceof Const4);
    assertEquals(2, ((Const4) code.instructions[0]).B);
    assertTrue(code.instructions[1] instanceof Return);
  }

  @Test
  public void b() {
    DexEncodedMethod method = oneMethodApplication(
        "int",
        Collections.singletonList("int"),
        1,
        "  const v0, 0",
        "  if-nez v0, :label_2",
        ":label_1",
        "  return v0",
        ":label_2",
        "  if-nez p0, :label_3",
        "  const v0, 1",
        "  goto :label_1",
        ":label_3",
        "  const v0, 2",
        "  goto :label_1");
    DexCode code = method.getCode().asDexCode();
    assertEquals(2, code.instructions.length);
    assertTrue(code.instructions[0] instanceof Const4);
    assertEquals(0, ((Const4) code.instructions[0]).B);
    assertTrue(code.instructions[1] instanceof Return);
  }

  @Test
  public void c() {
    DexEncodedMethod method = oneMethodApplication(
        "int",
        Collections.singletonList("int"),
        1,
        "  const v0, 0",
        "  if-nez v0, :label_2",
        ":label_1",
        "  return v0",
        ":label_2",
        "  if-nez p0, :label_3",
        "  const v0, 1",
        "  goto :label_1",
        ":label_3",
        "  const p0, 0",
        "  goto :label_2");
    DexCode code = method.getCode().asDexCode();
    assertEquals(2, code.instructions.length);
    assertTrue(code.instructions[0] instanceof Const4);
    assertEquals(0, ((Const4) code.instructions[0]).B);
    assertTrue(code.instructions[1] instanceof Return);
  }

  @Test
  public void d() {
    DexEncodedMethod method = oneMethodApplication(
        "int",
        Collections.singletonList("int"),
        1,
        "  const v0, 0",
        "  if-nez v0, :label_2",
        ":label_1",
        "  return v0",
        ":label_2",
        "  if-nez p0, :label_3",
        "  const v0, 1",
        "  goto :label_4",
        ":label_3",
        "  const p0, 0",
        "  goto :label_2",
        ":label_4",
        "  if-nez p0, :label_5",
        "  const v0, 1",
        "  goto :label_4",
        ":label_5",
        "  const p0, 0",
        "  goto :label_2");
    DexCode code = method.getCode().asDexCode();
    assertEquals(2, code.instructions.length);
    assertTrue(code.instructions[0] instanceof Const4);
    assertEquals(0, ((Const4) code.instructions[0]).B);
    assertTrue(code.instructions[1] instanceof Return);
  }

  @Test
  public void e() {
    DexEncodedMethod method = oneMethodApplication(
        "int",
        ImmutableList.of("int", "int", "int"),
        1,
        "  const v0, 0",
        "  if-nez v0, :x",
        "  const v0, 1",
        "  if-nez p0, :x",
        "  const v0, 2",
        "  if-nez p1, :x",
        "  const v0, 3",
        "  if-nez p2, :return",
        "  const v0, 4",
        "  goto :return",
        ":x",
        "  add-int v0, v0, p0",
        ":return",
        "  return v0");
    DexCode code = method.getCode().asDexCode();
    assertEquals(12, code.instructions.length);
    assertTrue(code.instructions[11] instanceof Return);
  }

  @Test
  public void f() {
    DexEncodedMethod method = oneMethodApplication(
        "int",
        Collections.singletonList("int"),
        1,
        "  const v0, 0",
        "  if-nez v0, :label_2",
        ":label_1",
        "  return v0",
        ":label_2",
        "  const v0, 1",
        "  goto :label_2");
    DexCode code = method.getCode().asDexCode();
    assertEquals(2, code.instructions.length);
    assertTrue(code.instructions[0] instanceof Const4);
    assertEquals(0, ((Const4) code.instructions[0]).B);
    assertTrue(code.instructions[1] instanceof Return);
  }

  @Test
  public void simplifyNonZeroTests() {
    class TestData {

      final int a;
      final int b;
      final boolean results[];

      TestData(int a, int b) {
        this.a = a;
        this.b = b;
        results = new boolean[6];
        results[Type.EQ.ordinal()] = a == b;
        results[Type.NE.ordinal()] = a != b;
        results[Type.LE.ordinal()] = a <= b;
        results[Type.GE.ordinal()] = a >= b;
        results[Type.LT.ordinal()] = a < b;
        results[Type.GT.ordinal()] = a > b;
      }
    }

    int[] testValues = new int[]{
        100,
        1,
        0,
        -1,
        100
    };

    List<TestData> tests = new ArrayList<>();
    for (int i = 0; i < testValues.length; i++) {
      for (int j = 0; j < testValues.length; j++) {
        tests.add(new TestData(testValues[i], testValues[j]));
      }
    }

    for (TestData test : tests) {
      for (Type type : Type.values()) {
        DexEncodedMethod method = oneMethodApplication(
            "int",
            Collections.singletonList("int"),
            2,
            "  const v0, 0x" + Integer.toHexString(test.a),
            "  const v1, 0x" + Integer.toHexString(test.b),
            "  " + ifOpcode[type.ordinal()] + " v0, v1, :label_2",
            "  const v0, 0",
            ":label_1",
            "  return v0",
            ":label_2",
            "  const v0, 1",
            "  goto :label_1");
        DexCode code = method.getCode().asDexCode();
        assertEquals(2, code.instructions.length);
        assertTrue(code.instructions[0] instanceof Const4);
        int expected = test.results[type.ordinal()] ? 1 : 0;
        assertEquals(expected, ((Const4) code.instructions[0]).B);
        assertTrue(code.instructions[1] instanceof Return);
      }
    }
  }

  public void runRewriteIfWithConstZeroTest(Type type, boolean zeroLeft, Class expected) {
    String ifInstruction;
    if (zeroLeft) {
      ifInstruction = "  " + ifOpcode[type.ordinal()] + " v0, v1, :label_2";
    } else {
      ifInstruction = "  " + ifOpcode[type.ordinal()] + " v1, v0, :label_2";
    }

    DexEncodedMethod method = oneMethodApplication(
        "int",
        Collections.singletonList("int"),
        1,
        "  const v0, 0x00",
        ifInstruction,
        "  const v0, 0",
        ":label_1",
        "  return v0",
        ":label_2",
        "  const v0, 1",
        "  goto :label_1");
    DexCode code = method.getCode().asDexCode();
    assertEquals(5, code.instructions.length);
    assertTrue(expected.isInstance(code.instructions[0]));
    assertTrue(code.instructions[4] instanceof Return);
  }

  @Test
  public void testRewriteIfWithConstZero() {
    runRewriteIfWithConstZeroTest(Type.EQ, true, IfEqz.class);
    runRewriteIfWithConstZeroTest(Type.NE, true, IfNez.class);
    runRewriteIfWithConstZeroTest(Type.LE, true, IfGez.class);
    runRewriteIfWithConstZeroTest(Type.GE, true, IfLez.class);
    runRewriteIfWithConstZeroTest(Type.LT, true, IfGtz.class);
    runRewriteIfWithConstZeroTest(Type.GT, true, IfLtz.class);

    runRewriteIfWithConstZeroTest(Type.EQ, false, IfEqz.class);
    runRewriteIfWithConstZeroTest(Type.NE, false, IfNez.class);
    runRewriteIfWithConstZeroTest(Type.LE, false, IfLez.class);
    runRewriteIfWithConstZeroTest(Type.GE, false, IfGez.class);
    runRewriteIfWithConstZeroTest(Type.LT, false, IfLtz.class);
    runRewriteIfWithConstZeroTest(Type.GT, false, IfGtz.class);
  }

  @Test
  public void x() {
    DexEncodedMethod method = oneMethodApplication(
        "Test",
        Lists.newArrayList("Test", "java.lang.String[]", "java.lang.String",
            "java.lang.String[]", "java.lang.String"),
        10,
        "          const/4             v4, 0x00  # 0",
        "          invoke-virtual      { v10 }, LTest;->a()LTest;",
        "          if-nez              v4, :label_8",
        "          move-object         v0, v4",
        "      :label_7",
        "          return-object       v0",
        "      :label_8",
        "          invoke-static       { v14 }, LTest;->a([Ljava/lang/String;)LTest;",
        "          move-result-object  v2",
        "          invoke-virtual      { v2 }, LTest;->a()Z",
        "          move-result         v0",
        "          if-nez              v0, :label_20",
        "          move-object         v0, v4",
        "          goto                :label_7",
        "      :label_20",
        "          iget-wide           v0, v2, LTest;->a:J",
        "          iget-wide           v6, v2, LTest;->b:J",
        "          invoke-virtual      { v2 }, LTest;->c()Z",
        "          move-result         v2",
        "          if-eqz              v2, :label_33",
        "          invoke-virtual      { v4 }, LTest;->a()V",
        "      :label_33",
        "          new-instance        v5, LTest;",
        "          sget-object         v2, LTest;->a:[Ljava/lang/String;",
        "          invoke-direct       { v5, v2 }, LTest;-><init>([Ljava/lang/String;)V",
        "          invoke-virtual      { v10 }, LTest;->a()LTest;",
        "          invoke-virtual      { v4, v0, v1, v6, v7 }, LTest;->a(JJ)Ljava/util/List;",
        "          move-result-object  v2",
        "          invoke-interface    { v2 }, Ljava/util/List;->iterator()Ljava/util/Iterator;",
        "          move-result-object  v6",
        "          move-wide           v2, v0",
        "      :label_52",
        "          invoke-interface    { v6 }, Ljava/util/Iterator;->hasNext()Z",
        "          move-result         v0",
        "          if-eqz              v0, :label_107",
        "          invoke-interface    { v6 }, Ljava/util/Iterator;->next()Ljava/lang/Object;",
        "          move-result-object  v0",
        "          check-cast          v0, LTest;",
        "          const-wide/16       v8, 0x0000000000000001  # 1",
        "          add-long/2addr      v2, v8",
        "          invoke-virtual      { v5 }, LTest;->newRow()LTest;",
        "          move-result-object  v1",
        "          invoke-static       { v2, v3 }, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;",
        "          move-result-object  v7",
        "          invoke-virtual      { v1, v7 }, LTest;->a(Ljava/lang/Object;)LTest;",
        "          move-result-object  v1",
        "          const-string        v7, \"add\"",
        "          invoke-virtual      { v1, v7 }, LTest;->a(Ljava/lang/Object;)LTest;",
        "          move-result-object  v1",
        "          iget-object         v7, v0, LTest;->a:Ljava/lang/String;",
        "          invoke-virtual      { v1, v7 }, LTest;->a(Ljava/lang/Object;)LTest;",
        "          move-result-object  v1",
        "          iget                v7, v0, LTest;->b:I",
        "          invoke-static       { v7 }, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;",
        "          move-result-object  v7",
        "          invoke-virtual      { v1, v7 }, LTest;->add(Ljava/lang/Object;)LTest;",
        "          move-result-object  v1",
        "          iget-object         v0, v0, LTest;->a:Ljava/lang/String;",
        "          invoke-virtual      { v1, v0 }, LTest;->add(Ljava/lang/Object;)LTest;",
        "          goto                :label_52",
        "      :label_107",
        "          iget-object         v0, v4, LTest;->a:LTest;",
        "          const-string        v1, \"text 1\"",
        "          const/4             v2, 0x00  # 0",
        "          invoke-virtual      { v0, v1, v2 }, LTest;->a(Ljava/lang/String;I)LTest;",
        "          move-result-object  v0",
        "          const-string        v1, \"text 2\"",
        "          const-string        v2, \"\"",
        "          invoke-interface    { v0, v1, v2 }, LTest;->getString(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
        "          move-result-object  v0",
        "          invoke-static       { v5, v0 }, LTest;->a(LTest;Ljava/lang/String;)LTest;",
        "          move-result-object  v0",
        "          goto                :label_7"
    );
    DexCode code = method.getCode().asDexCode();
    assertEquals(3, code.instructions.length);
    assertTrue(code.instructions[0] instanceof InvokeVirtual);
    assertTrue(code.instructions[1] instanceof Const4);
    assertEquals(0, ((Const4) code.instructions[1]).B);
    assertTrue(code.instructions[2] instanceof ReturnObject);
  }

  @Test
  public void y() {
    DexEncodedMethod method = oneMethodApplication(
        "boolean",
        Lists.newArrayList("Test", "java.lang.Object"),
        6,
        "      const-wide/16       v4, 0x0000000000000000L  # 0",
        "      const/4             v0, 0x01  # 1",
        "      const/4             v3, 0x00  # 0",
        "      const/4             v1, 0x00  # 0",
        "      if-ne               v6, v7, :label_8",
        "    :label_7",
        "      return              v0",
        "    :label_8",
        "      if-nez              v7, :label_12",
        "      move                v0, v1",
        "      goto                :label_7",
        "    :label_12",
        "      instance-of         v2, v7, LTest;",
        "      if-nez              v2, :label_18",
        "      move                v0, v1",
        "      goto                :label_7",
        "    :label_18",
        "      check-cast          v7, LTest;",
        "      cmp-long            v2, v4, v4",
        "      if-nez              v2, :label_50",
        "      invoke-static       { v3, v3 }, LTest;->a(Ljava/lang/Object;Ljava/lang/Object;)Z",
        "      move-result         v2",
        "      if-eqz              v2, :label_50",
        "      invoke-static       { v3, v3 }, LTest;->a(Ljava/lang/Object;Ljava/lang/Object;)Z",
        "      move-result         v2",
        "      if-eqz              v2, :label_50",
        "      invoke-static       { v1 }, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;",
        "      move-result-object  v2",
        "      invoke-static       { v1 }, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;",
        "      move-result-object  v3",
        "      invoke-static       { v2, v3 }, LTest;->a(Ljava/lang/Object;Ljava/lang/Object;)Z",
        "      move-result         v2",
        "      if-nez              v2, :label_7",
        "    :label_50",
        "      move                v0, v1",
        "      goto                :label_7"
    );
    DexCode code = method.getCode().asDexCode();
    // TODO(sgjesse): Maybe this test is too fragile, as it leaves quite a lot of code, so the
    // expectation might need changing with other optimizations.
    assertEquals(29, code.instructions.length);
  }
}
