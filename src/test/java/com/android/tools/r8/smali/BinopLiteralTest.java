// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.smali;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.android.tools.r8.code.Const16;
import com.android.tools.r8.code.Format22b;
import com.android.tools.r8.code.Format22s;
import com.android.tools.r8.code.Return;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.graph.DexCode;
import com.android.tools.r8.graph.DexEncodedMethod;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class BinopLiteralTest extends SmaliTestBase {

  int[] lit8Values = new int[]{
      Constants.S8BIT_MIN,
      Constants.S8BIT_MIN + 1,
      Constants.S4BIT_MIN - 1,
      Constants.S4BIT_MIN,
      Constants.S4BIT_MIN + 1,
      1,
      0,
      1,
      Constants.S4BIT_MAX - 1,
      Constants.S4BIT_MAX,
      Constants.S4BIT_MAX + 1,
      Constants.S8BIT_MAX - 1,
      Constants.S8BIT_MAX,
  };

  int[] lit16Values = new int[]{
      Short.MIN_VALUE,
      Short.MIN_VALUE + 1,
      Constants.S8BIT_MIN - 1,
      Constants.S8BIT_MAX + 1,
      Short.MAX_VALUE - 1,
      Short.MAX_VALUE,
  };

  @Test
  public void lit8PassthroughTest() {
    List<String> lit8Binops = Arrays.asList(
        "add", "rsub", "mul", "div", "rem", "and", "or", "xor", "shl", "shr", "ushr"
    );

    for (String binop : lit8Binops) {
      for (int lit8Value : lit8Values) {
        DexEncodedMethod method = oneMethodApplication(
            "int", Collections.singletonList("int"),
            0,
            // E.g. add-int/lit8 p0, p0, -128
            "    " + binop + "-int/lit8 p0, p0, " + lit8Value,
            "    return p0"
        );
        DexCode code = method.getCode().asDexCode();
        assertEquals(2, code.instructions.length);
        assertTrue(code.instructions[0] instanceof Format22b);
        assertEquals(lit8Value, ((Format22b) code.instructions[0]).CC);
        assertTrue(code.instructions[1] instanceof Return);
      }
    }
  }

  @Test
  public void lit16PassthroughTest() {
    List<String> lit16Binops = Arrays.asList(
        "add", "rsub", "mul", "div", "rem", "and", "or", "xor"
    );

    for (String binop : lit16Binops) {
      for (int lit16Value : lit16Values) {
        String lit16Postfix = !binop.equals("rsub") ? "/lit16" : "";
        DexEncodedMethod method = oneMethodApplication(
            "int", Collections.singletonList("int"),
            0,
            // E.g. add-int/lit16 p0, p0, -32768
            "    " + binop + "-int" + lit16Postfix + " p0, p0, " + lit16Value,
            "    return p0"
        );
        DexCode code = method.getCode().asDexCode();
        assertEquals(2, code.instructions.length);
        assertTrue(code.instructions[0] instanceof Format22s);
        assertEquals(lit16Value, ((Format22s) code.instructions[0]).CCCC);
        assertTrue(code.instructions[1] instanceof Return);
      }
    }
  }

  @Test
  public void lit16NotSupported() {
    String[] lit8OnlyBinops = new String[]{
        "shl", "shr", "ushr",
    };
    for (String binop : lit8OnlyBinops) {
      for (int lit16Value : lit16Values) {
        DexEncodedMethod method = oneMethodApplication(
            "int", Collections.singletonList("int"),
            1,
            "    const/16 v0, " + lit16Value,
            "    " + binop + "-int/2addr p0, v0    ",
            "    return p0"
        );
        DexCode code = method.getCode().asDexCode();
        assertEquals(3, code.instructions.length);
        assertTrue(code.instructions[0] instanceof Const16);
        assertEquals(lit16Value, ((Const16) code.instructions[0]).BBBB);
        assertTrue(code.instructions[2] instanceof Return);
      }
    }
  }
}
