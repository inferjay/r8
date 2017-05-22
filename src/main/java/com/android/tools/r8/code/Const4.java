// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.code;

import com.android.tools.r8.ir.code.MoveType;
import com.android.tools.r8.ir.code.SingleConstant;
import com.android.tools.r8.ir.conversion.IRBuilder;
import com.android.tools.r8.naming.ClassNameMapper;
import com.android.tools.r8.utils.StringUtils;

public class Const4 extends Format11n implements SingleConstant {

  public static final int OPCODE = 0x12;
  public static final String NAME = "Const4";
  public static final String SMALI_NAME = "const/4";

  Const4(int high, BytecodeStream stream) {
    super(high, stream);
  }

  public Const4(int dest, int constant) {
    super(dest, constant);
  }

  public String getName() {
    return NAME;
  }

  public String getSmaliName() {
    return SMALI_NAME;
  }

  public int getOpcode() {
    return OPCODE;
  }

  @Override
  public int decodedValue() {
    return B;
  }

  public String toString(ClassNameMapper naming) {
    return formatString("v" + A + ", 0x" + StringUtils.hexString(decodedValue(), 1) +
        " (" + decodedValue() + ")");
  }

  public String toSmaliString(ClassNameMapper naming) {
    return formatSmaliString("v" + A + ", 0x" + StringUtils.hexString(decodedValue(), 2) +
        "  # " + decodedValue());
  }

  @Override
  public void buildIR(IRBuilder builder) {
    builder.addConst(MoveType.SINGLE, A, decodedValue());
  }
}
