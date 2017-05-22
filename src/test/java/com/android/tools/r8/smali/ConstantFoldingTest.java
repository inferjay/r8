// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.smali;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.android.tools.r8.code.Const4;
import com.android.tools.r8.code.DivIntLit8;
import com.android.tools.r8.code.Instruction;
import com.android.tools.r8.code.RemIntLit8;
import com.android.tools.r8.code.Return;
import com.android.tools.r8.code.ReturnWide;
import com.android.tools.r8.graph.DexCode;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.ir.code.Cmp.Bias;
import com.android.tools.r8.ir.code.If.Type;
import com.android.tools.r8.ir.code.SingleConstant;
import com.android.tools.r8.ir.code.WideConstant;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class ConstantFoldingTest extends SmaliTestBase {

  public void generateBinopTest(String type, String op, List<Long> values, Long result) {
    boolean wide = type.equals("long") || type.equals("double");
    StringBuilder source = new StringBuilder();
    int factor = wide ? 2 : 1;
    for (int i = 0; i < values.size(); i++) {
      source.append("    ");
      source.append(wide ? "const-wide " : "const ");
      source.append("v" + (i * factor));
      source.append(", ");
      source.append("0x" + Long.toHexString(values.get(i)));
      source.append(wide ? "L" : "");
      source.append("\n");
    }

    for (int i = 0; i < values.size() - 1; i++) {
      source.append("    ");
      source.append(op + "-" + type + "/2addr ");
      source.append("v" + ((i + 1) * factor));
      source.append(", ");
      source.append("v" + (i * factor));
      source.append("\n");
    }

    source.append("    ");
    source.append(wide ? "return-wide " : "return ");
    source.append("v" + ((values.size() - 1) * factor));

    DexEncodedMethod method = oneMethodApplication(
        type, Collections.singletonList(type),
        values.size() * factor,
        source.toString());
    DexCode code = method.getCode().asDexCode();
    assertEquals(2, code.instructions.length);
    if (wide) {
      assertTrue(code.instructions[0] instanceof WideConstant);
      assertEquals(result.longValue(), ((WideConstant) code.instructions[0]).decodedValue());
      assertTrue(code.instructions[1] instanceof ReturnWide);
    } else {
      assertTrue(code.instructions[0] instanceof SingleConstant);
      assertEquals(
          result.longValue(), (long) ((SingleConstant) code.instructions[0]).decodedValue());
      assertTrue(code.instructions[1] instanceof Return);
    }
  }

  private long floatBits(float f) {
    return Float.floatToIntBits(f);
  }

  private long doubleBits(double d) {
    return Double.doubleToLongBits(d);
  }

  ImmutableList<Long> arguments = ImmutableList.of(1L, 2L, 3L, 4L);
  ImmutableList<Long> floatArguments = ImmutableList.of(
      floatBits(1.0f), floatBits(2.0f), floatBits(3.0f), floatBits(4.0f));
  ImmutableList<Long> doubleArguments = ImmutableList.of(
      doubleBits(1.0), doubleBits(2.0), doubleBits(3.0), doubleBits(4.0));

  @Test
  public void addFold() {
    generateBinopTest("int", "add", arguments, 10L);
    generateBinopTest("long", "add", arguments, 10L);
    generateBinopTest("float", "add", floatArguments, floatBits(10.0f));
    generateBinopTest("double", "add", doubleArguments, doubleBits(10.0));
  }

  @Test
  public void mulFold() {
    generateBinopTest("int", "mul", arguments, 24L);
    generateBinopTest("long", "mul", arguments, 24L);
    generateBinopTest("float", "mul", floatArguments, floatBits(24.0f));
    generateBinopTest("double", "mul", doubleArguments, doubleBits(24.0));
  }

  @Test
  public void subFold() {
    generateBinopTest("int", "sub", arguments.reverse(), -2L);
    generateBinopTest("long", "sub", arguments.reverse(), -2L);
    generateBinopTest("float", "sub", floatArguments.reverse(), floatBits(-2.0f));
    generateBinopTest("double", "sub", doubleArguments.reverse(), doubleBits(-2.0));
  }

  @Test
  public void divFold() {
    ImmutableList<Long> arguments = ImmutableList.of(2L, 24L, 48L, 4L);
    ImmutableList<Long> floatArguments = ImmutableList.of(
        floatBits(2.0f), floatBits(24.0f), floatBits(48.0f), floatBits(4.0f));
    ImmutableList<Long> doubleArguments = ImmutableList.of(
        doubleBits(2.0), doubleBits(24.0), doubleBits(48.0), doubleBits(4.0));

    generateBinopTest("int", "div", arguments, 1L);
    generateBinopTest("long", "div", arguments, 1L);
    generateBinopTest("float", "div", floatArguments, floatBits(1.0f));
    generateBinopTest("double", "div", doubleArguments, doubleBits(1.0));
  }


  @Test
  public void remFold() {
    ImmutableList<Long> arguments = ImmutableList.of(10L, 6L, 3L, 2L);
    ImmutableList<Long> floatArguments = ImmutableList.of(
        floatBits(10.0f), floatBits(6.0f), floatBits(3.0f), floatBits(2.0f));
    ImmutableList<Long> doubleArguments = ImmutableList.of(
        doubleBits(10.0), doubleBits(6.0), doubleBits(3.0), doubleBits(2.0));

    generateBinopTest("int", "rem", arguments, 2L);
    generateBinopTest("long", "rem", arguments, 2L);
    generateBinopTest("float", "rem", floatArguments, floatBits(2.0f));
    generateBinopTest("double", "rem", doubleArguments, doubleBits(2.0));
  }

  @Test
  public void divIntFoldDivByZero() {
    DexEncodedMethod method = oneMethodApplication(
        "int", Collections.singletonList("int"),
        2,
        "    const/4 v0, 1           ",
        "    const/4 v1, 0           ",
        "    div-int/2addr v0, v1    ",
        "    return v0\n             "
    );
    DexCode code = method.getCode().asDexCode();
    // Division by zero is not folded, but div-int/lit8 is used.
    assertEquals(3, code.instructions.length);
    assertTrue(code.instructions[0] instanceof Const4);
    assertTrue(code.instructions[1] instanceof DivIntLit8);
    assertEquals(0, ((DivIntLit8) code.instructions[1]).CC);
    assertTrue(code.instructions[2] instanceof Return);
  }

  @Test
  public void divIntFoldRemByZero() {
    DexEncodedMethod method = oneMethodApplication(
        "int", Collections.singletonList("int"),
        2,
        "    const/4 v0, 1           ",
        "    const/4 v1, 0           ",
        "    rem-int/2addr v0, v1    ",
        "    return v0\n             "
    );
    DexCode code = method.getCode().asDexCode();
    // Division by zero is not folded, but rem-int/lit8 is used.
    assertEquals(3, code.instructions.length);
    assertTrue(code.instructions[0] instanceof Const4);
    assertTrue(code.instructions[1] instanceof RemIntLit8);
    assertEquals(0, ((RemIntLit8) code.instructions[1]).CC);
    assertTrue(code.instructions[2] instanceof Return);
  }

  public void generateUnopTest(String type, String op, Long value, Long result) {
    boolean wide = type.equals("long") || type.equals("double");
    StringBuilder source = new StringBuilder();
    source.append("    ");
    source.append(wide ? "const-wide " : "const ");
    source.append("v0 , ");
    source.append("0x" + Long.toHexString(value));
    source.append(wide ? "L" : "");
    source.append("\n");

    source.append("    ");
    source.append(op + "-" + type + " v0, v0\n");

    source.append("    ");
    source.append(wide ? "return-wide v0" : "return v0");

    DexEncodedMethod method = oneMethodApplication(
        type, Collections.singletonList(type),
        wide ? 2 : 1,
        source.toString());
    DexCode code = method.getCode().asDexCode();
    assertEquals(2, code.instructions.length);
    if (wide) {
      assertTrue(code.instructions[0] instanceof WideConstant);
      assertEquals(result.longValue(), ((WideConstant) code.instructions[0]).decodedValue());
      assertTrue(code.instructions[1] instanceof ReturnWide);
    } else {
      assertTrue(code.instructions[0] instanceof SingleConstant);
      assertEquals(
          result.longValue(), (long) ((SingleConstant) code.instructions[0]).decodedValue());
      assertTrue(code.instructions[1] instanceof Return);
    }
  }

  @Test
  public void negFold() {
    generateUnopTest("int", "neg", 2L, -2L);
    generateUnopTest("int", "neg", -2L, 2L);
    generateUnopTest("long", "neg", 2L, -2L);
    generateUnopTest("long", "neg", -2L, 2L);
    generateUnopTest("float", "neg", floatBits(2.0f), floatBits(-2.0f));
    generateUnopTest("float", "neg", floatBits(-2.0f), floatBits(2.0f));
    generateUnopTest("float", "neg", floatBits(0.0f), floatBits(-0.0f));
    generateUnopTest("float", "neg", floatBits(-0.0f), floatBits(0.0f));
    generateUnopTest("double", "neg", doubleBits(2.0), doubleBits(-2.0));
    generateUnopTest("double", "neg", doubleBits(-2.0), doubleBits(2.0));
    generateUnopTest("double", "neg", doubleBits(0.0), doubleBits(-0.0));
    generateUnopTest("double", "neg", doubleBits(-0.0), doubleBits(0.0));
  }

  private void assertConstValue(int expected, Instruction insn) {
    assertTrue(insn instanceof SingleConstant);
    assertEquals(expected, ((SingleConstant) insn).decodedValue());
  }

  private void assertConstValue(long expected, Instruction insn) {
    assertTrue(insn instanceof WideConstant);
    assertEquals(expected, ((WideConstant) insn).decodedValue());
  }

  public void testLogicalOperatorsFolding(String op, int[] v) {
    int v0 = v[0];
    int v1 = v[1];
    int v2 = v[2];
    int v3 = v[3];

    int expected = 0;
    switch (op) {
      case "and":
        expected = v0 & v1 & v2 & v3;
        break;
      case "or":
        expected = v0 | v1 | v2 | v3;
        break;
      case "xor":
        expected = v0 ^ v1 ^ v2 ^ v3;
        break;
      default:
        fail("Unsupported logical binop " + op);
    }

    DexEncodedMethod method = oneMethodApplication(
        "int", Collections.singletonList("int"),
        4,
        "    const v0, " + v0,
        "    const v1, " + v1,
        "    const v2, " + v2,
        "    const v3, " + v3,
        // E.g. and-int//2addr v1, v0
        "    " + op + "-int/2addr v1, v0    ",
        "    " + op + "-int/2addr v2, v1    ",
        "    " + op + "-int/2addr v3, v2    ",
        "    return v3\n                    "
    );
    DexCode code = method.getCode().asDexCode();
    // Test that this just returns a constant.
    assertEquals(2, code.instructions.length);
    assertConstValue(expected, code.instructions[0]);
    assertTrue(code.instructions[1] instanceof Return);
  }

  @Test
  public void logicalOperatorsFolding() {
    int[][] testValues = new int[][]{
        new int[]{0x00, 0x00, 0x00, 0x00},
        new int[]{0x0b, 0x06, 0x03, 0x00},
        new int[]{0x0f, 0x07, 0x03, 0x01},
        new int[]{0x08, 0x04, 0x02, 0x01},
    };

    for (int[] values : testValues) {
      testLogicalOperatorsFolding("and", values);
      testLogicalOperatorsFolding("or", values);
      testLogicalOperatorsFolding("xor", values);
    }
  }

  private void testShiftOperatorsFolding(String op, int[] v) {
    int v0 = v[0];
    int v1 = v[1];
    int v2 = v[2];
    int v3 = v[3];

    int expected = 0;
    switch (op) {
      case "shl":
        v0 = v0 << v1;
        v0 = v0 << v2;
        v0 = v0 << v3;
        break;
      case "shr":
        v0 = v0 >> v1;
        v0 = v0 >> v2;
        v0 = v0 >> v3;
        break;
      case "ushr":
        v0 = v0 >>> v1;
        v0 = v0 >>> v2;
        v0 = v0 >>> v3;
        break;
      default:
        fail("Unsupported shift " + op);
    }
    expected = v0;

    DexEncodedMethod method = oneMethodApplication(
        "int", Collections.singletonList("int"),
        4,
        "    const v0, " + v[0],
        "    const v1, " + v[1],
        "    const v2, " + v[2],
        "    const v3, " + v[3],
        // E.g. and-int//2addr v1, v0
        "    " + op + "-int/2addr v0, v1    ",
        "    " + op + "-int/2addr v0, v2    ",
        "    " + op + "-int/2addr v0, v3    ",
        "    return v0\n                    "
    );
    DexCode code = method.getCode().asDexCode();
    // Test that this just returns a constant.
    assertEquals(2, code.instructions.length);
    assertConstValue(expected, code.instructions[0]);
    assertTrue(code.instructions[1] instanceof Return);
  }

  @Test
  public void shiftOperatorsFolding() {
    int[][] testValues = new int[][]{
        new int[]{0x01, 0x01, 0x01, 0x01},
        new int[]{0x01, 0x02, 0x03, 0x04},
        new int[]{0x7f000000, 0x01, 0x2, 0x03},
        new int[]{0x80000000, 0x01, 0x2, 0x03},
        new int[]{0xffffffff, 0x01, 0x2, 0x03},
    };

    for (int[] values : testValues) {
      testShiftOperatorsFolding("shl", values);
      testShiftOperatorsFolding("shr", values);
      testShiftOperatorsFolding("ushr", values);
    }
  }

  private void testShiftOperatorsFoldingWide(String op, long[] v) {
    long v0 = v[0];
    int v2 = (int) v[1];
    int v4 = (int) v[2];
    int v6 = (int) v[3];

    long expected = 0;
    switch (op) {
      case "shl":
        v0 = v0 << v2;
        v0 = v0 << v4;
        v0 = v0 << v6;
        break;
      case "shr":
        v0 = v0 >> v2;
        v0 = v0 >> v4;
        v0 = v0 >> v6;
        break;
      case "ushr":
        v0 = v0 >>> v2;
        v0 = v0 >>> v4;
        v0 = v0 >>> v6;
        break;
      default:
        fail("Unsupported shift " + op);
    }
    expected = v0;

    DexEncodedMethod method = oneMethodApplication(
        "long", Collections.singletonList("long"),
        5,
        "    const-wide v0, 0x" + Long.toHexString(v[0]) + "L",
        "    const v2, " + v[1],
        "    const v3, " + v[2],
        "    const v4, " + v[3],
        // E.g. and-long//2addr v1, v0
        "    " + op + "-long/2addr v0, v2    ",
        "    " + op + "-long/2addr v0, v3    ",
        "    " + op + "-long/2addr v0, v4    ",
        "    return-wide v0\n                    "
    );
    DexCode code = method.getCode().asDexCode();
    // Test that this just returns a constant.
    assertEquals(2, code.instructions.length);
    assertConstValue(expected, code.instructions[0]);
    assertTrue(code.instructions[1] instanceof ReturnWide);
  }

  @Test
  public void shiftOperatorsFoldingWide() {
    long[][] testValues = new long[][]{
        new long[]{0x01, 0x01, 0x01, 0x01},
        new long[]{0x01, 0x02, 0x03, 0x04},
        new long[]{0x7f0000000000L, 0x01, 0x2, 0x03},
        new long[]{0x800000000000L, 0x01, 0x2, 0x03},
        new long[]{0x7f00000000000000L, 0x01, 0x2, 0x03},
        new long[]{0x8000000000000000L, 0x01, 0x2, 0x03},
        new long[]{0xffffffffffffffffL, 0x01, 0x2, 0x03},
    };

    for (long[] values : testValues) {
      testShiftOperatorsFoldingWide("shl", values);
      testShiftOperatorsFoldingWide("shr", values);
      testShiftOperatorsFoldingWide("ushr", values);
    }
  }

  @Test
  public void notIntFold() {
    int[] testValues = new int[]{0, 1, 0xff, 0xffffffff, 0xff000000, 0x80000000};
    for (int value : testValues) {
      DexEncodedMethod method = oneMethodApplication(
          "int", Collections.emptyList(),
          1,
          "    const v0, " + value,
          "    not-int v0, v0",
          "    return v0"
      );
      DexCode code = method.getCode().asDexCode();
      assertEquals(2, code.instructions.length);
      assertConstValue(~value, code.instructions[0]);
      assertTrue(code.instructions[1] instanceof Return);
    }
  }

  @Test
  public void notLongFold() {
    long[] testValues = new long[]{
        0L,
        1L,
        0xffL,
        0xffffffffffffffffL,
        0x00ffffffffffffffL,
        0xff00000000000000L,
        0x8000000000000000L
    };
    for (long value : testValues) {
      DexEncodedMethod method = oneMethodApplication(
          "long", Collections.emptyList(),
          2,
          "    const-wide v0, 0x" + Long.toHexString(value) + "L",
          "    not-long v0, v0",
          "    return-wide v0"
      );
      DexCode code = method.getCode().asDexCode();
      assertEquals(2, code.instructions.length);
      assertConstValue(~value, code.instructions[0]);
      assertTrue(code.instructions[1] instanceof ReturnWide);
    }
  }

  @Test
  public void negIntFold() {
    int[] testValues = new int[]{0, 1, 0xff, 0xffffffff, 0xff000000, 0x80000000};
    for (int value : testValues) {
      DexEncodedMethod method = oneMethodApplication(
          "int", Collections.emptyList(),
          1,
          "    const v0, " + value,
          "    neg-int v0, v0",
          "    return v0"
      );
      DexCode code = method.getCode().asDexCode();
      assertEquals(2, code.instructions.length);
      assertConstValue(-value, code.instructions[0]);
      assertTrue(code.instructions[1] instanceof Return);
    }
  }

  @Test
  public void negLongFold() {
    long[] testValues = new long[]{
        0L,
        1L,
        0xffL,
        0xffffffffffffffffL,
        0x00ffffffffffffffL,
        0xff00000000000000L,
        0x8000000000000000L
    };
    for (long value : testValues) {
      DexEncodedMethod method = oneMethodApplication(
          "long", Collections.emptyList(),
          2,
          "    const-wide v0, 0x" + Long.toHexString(value) + "L",
          "    neg-long v0, v0",
          "    return-wide v0"
      );
      DexCode code = method.getCode().asDexCode();
      assertEquals(2, code.instructions.length);
      long expected = -value;
      assertConstValue(-value, code.instructions[0]);
      assertTrue(code.instructions[1] instanceof ReturnWide);
    }
  }

  @Test
  public void cmpFloatFold() {
    String[] ifOpcode = new String[6];
    ifOpcode[Type.EQ.ordinal()] = "if-eqz";
    ifOpcode[Type.NE.ordinal()] = "if-nez";
    ifOpcode[Type.LE.ordinal()] = "if-lez";
    ifOpcode[Type.GE.ordinal()] = "if-gez";
    ifOpcode[Type.LT.ordinal()] = "if-ltz";
    ifOpcode[Type.GT.ordinal()] = "if-gtz";

    class FloatTestData {

      final float a;
      final float b;
      final boolean results[];

      FloatTestData(float a, float b) {
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

    float[] testValues = new float[]{
        Float.NEGATIVE_INFINITY,
        -100.0f,
        -0.0f,
        0.0f,
        100.0f,
        Float.POSITIVE_INFINITY,
        Float.NaN
    };

    List<FloatTestData> tests = new ArrayList<>();
    for (int i = 0; i < testValues.length; i++) {
      for (int j = 0; j < testValues.length; j++) {
        tests.add(new FloatTestData(testValues[i], testValues[j]));
      }
    }

    for (FloatTestData test : tests) {
      for (Type type : Type.values()) {
        for (Bias bias : Bias.values()) {
          if (bias == Bias.NONE) {
            // Bias NONE is only for long comparison.
            continue;
          }
          // If no NaNs are involved either bias produce the same result.
          if (Float.isNaN(test.a) || Float.isNaN(test.b)) {
            // For NaN comparison only test with the bias that provide Java semantics.
            // The Java Language Specification 4.2.3. Floating-Point Types, Formats, and Values
            // says:
            //
            // The numerical comparison operators <, <=, >, and >= return false if either or both
            // operands are NaN
            if ((type == Type.GE || type == Type.GT) && bias == Bias.GT) {
              continue;
            }
            if ((type == Type.LE || type == Type.LT) && bias == Bias.LT) {
              continue;
            }
          }
          String cmpInstruction;
          if (bias == Bias.LT) {
            cmpInstruction = "    cmpl-float v0, v0, v1";
          } else {
            cmpInstruction = "    cmpg-float v0, v0, v1";
          }
          DexEncodedMethod method = oneMethodApplication(
              "int", Collections.emptyList(),
              2,
              "    const v0, 0x" + Integer.toHexString(Float.floatToRawIntBits(test.a)),
              "    const v1, 0x" + Integer.toHexString(Float.floatToRawIntBits(test.b)),
              cmpInstruction,
              "    " + ifOpcode[type.ordinal()] + " v0, :label_2",
              "    const v0, 0",
              ":label_1",
              "    return v0",
              ":label_2",
              "  const v0, 1",
              "  goto :label_1"
          );
          DexCode code = method.getCode().asDexCode();
          assertEquals(2, code.instructions.length);
          int expected = test.results[type.ordinal()] ? 1 : 0;
          assertConstValue(expected, code.instructions[0]);
          assertTrue(code.instructions[1] instanceof Return);
        }
      }
    }
  }

  @Test
  public void cmpDoubleFold() {
    String[] ifOpcode = new String[6];
    ifOpcode[Type.EQ.ordinal()] = "if-eqz";
    ifOpcode[Type.NE.ordinal()] = "if-nez";
    ifOpcode[Type.LE.ordinal()] = "if-lez";
    ifOpcode[Type.GE.ordinal()] = "if-gez";
    ifOpcode[Type.LT.ordinal()] = "if-ltz";
    ifOpcode[Type.GT.ordinal()] = "if-gtz";

    class DoubleTestData {

      final double a;
      final double b;
      final boolean results[];

      DoubleTestData(double a, double b) {
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

    double[] testValues = new double[]{
        Double.NEGATIVE_INFINITY,
        -100.0f,
        -0.0f,
        0.0f,
        100.0f,
        Double.POSITIVE_INFINITY,
        Double.NaN
    };

    List<DoubleTestData> tests = new ArrayList<>();
    for (int i = 0; i < testValues.length; i++) {
      for (int j = 0; j < testValues.length; j++) {
        tests.add(new DoubleTestData(testValues[i], testValues[j]));
      }
    }

    for (DoubleTestData test : tests) {
      for (Type type : Type.values()) {
        for (Bias bias : Bias.values()) {
          if (bias == Bias.NONE) {
            // Bias NONE is only for long comparison.
            continue;
          }
          if (Double.isNaN(test.a) || Double.isNaN(test.b)) {
            // For NaN comparison only test with the bias that provide Java semantics.
            // The Java Language Specification 4.2.3. Doubleing-Point Types, Formats, and Values
            // says:
            //
            // The numerical comparison operators <, <=, >, and >= return false if either or both
            // operands are NaN
            if ((type == Type.GE || type == Type.GT) && bias == Bias.GT) {
              continue;
            }
            if ((type == Type.LE || type == Type.LT) && bias == Bias.LT) {
              continue;
            }
          }
          String cmpInstruction;
          if (bias == Bias.LT) {
            cmpInstruction = "    cmpl-double v0, v0, v2";
          } else {
            cmpInstruction = "    cmpg-double v0, v0, v2";
          }
          DexEncodedMethod method = oneMethodApplication(
              "int", Collections.emptyList(),
              4,
              "    const-wide v0, 0x" + Long.toHexString(Double.doubleToRawLongBits(test.a)) + "L",
              "    const-wide v2, 0x" + Long.toHexString(Double.doubleToRawLongBits(test.b)) + "L",
              cmpInstruction,
              "    " + ifOpcode[type.ordinal()] + " v0, :label_2",
              "    const v0, 0",
              ":label_1",
              "    return v0",
              ":label_2",
              "  const v0, 1",
              "  goto :label_1"
          );
          DexCode code = method.getCode().asDexCode();
          assertEquals(2, code.instructions.length);
          int expected = test.results[type.ordinal()] ? 1 : 0;
          assertConstValue(expected, code.instructions[0]);
          assertTrue(code.instructions[1] instanceof Return);
        }
      }
    }
  }

  @Test
  public void cmpLongFold() {
    long[][] longValues = new long[][]{
        {Long.MIN_VALUE, 1L},
        {Long.MAX_VALUE, 1L},
        {Long.MIN_VALUE, 0L},
        {Long.MAX_VALUE, 0L},
        {Long.MIN_VALUE, -1L},
        {Long.MAX_VALUE, -1L},
    };

    for (long[] values : longValues) {
      DexEncodedMethod method = oneMethodApplication(
          "int", Collections.emptyList(),
          4,
          "    const-wide v0, 0x" + Long.toHexString(values[0]) + "L",
          "    const-wide v2, 0x" + Long.toHexString(values[1]) + "L",
          "    cmp-long v0, v0, v2",
          "    return v0"
      );
      DexCode code = method.getCode().asDexCode();
      assertEquals(2, code.instructions.length);
      assertConstValue(Long.compare(values[0], values[1]), code.instructions[0]);
      assertTrue(code.instructions[1] instanceof Return);
    }
  }
}
