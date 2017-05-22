// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.code;

import com.android.tools.r8.ir.code.MoveType;
import com.android.tools.r8.ir.conversion.IRBuilder;

public class Move extends Format12x {

  public static final int OPCODE = 0x1;
  public static final String NAME = "Move";
  public static final String SMALI_NAME = "move";

  Move(int high, BytecodeStream stream) {
    super(high, stream);
  }

  public Move(int dest, int src) {
    super(dest, src);
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
  public void buildIR(IRBuilder builder) {
    builder.addMove(MoveType.SINGLE, A, B);
  }
}
