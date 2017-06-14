// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.code.ReturnObject;
import com.android.tools.r8.code.ReturnVoid;
import com.android.tools.r8.code.ReturnWide;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppInfo;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.ir.optimize.Inliner.Constraint;

public class Return extends JumpInstruction {

  // Need to keep track of the original return type, as a null value will have MoveType.SINGLE.
  final private MoveType returnType;

  public Return() {
    super(null);
    returnType = null;
  }

  public Return(Value value, MoveType returnType) {
    super(null, value);
    this.returnType = returnType;
  }

  public boolean isReturnVoid() {
    return inValues.size() == 0;
  }

  public MoveType getReturnType() {
    return returnType;
  }

  public Value returnValue() {
    assert !isReturnVoid();
    return inValues.get(0);
  }

  public com.android.tools.r8.code.Instruction createDexInstruction(DexBuilder builder) {
    if (isReturnVoid()) {
      return new ReturnVoid();
    } else {
      switch (returnValue().type) {
        case OBJECT:
          assert returnType == MoveType.OBJECT;
          return new ReturnObject(builder.allocatedRegister(returnValue(), getNumber()));
        case SINGLE:
          if (returnType == MoveType.OBJECT) {
            return new ReturnObject(builder.allocatedRegister(returnValue(), getNumber()));
          } else {
            assert returnType == MoveType.SINGLE;
            return new com.android.tools.r8.code.Return(builder.allocatedRegister(returnValue(), getNumber()));
          }
        case WIDE:
          assert returnType == MoveType.WIDE;
          return new ReturnWide(builder.allocatedRegister(returnValue(), getNumber()));
        default:
          throw new Unreachable();
      }
    }
  }

  @Override
  public void buildDex(DexBuilder builder) {
    builder.add(this, createDexInstruction(builder));
  }

  @Override
  public boolean identicalNonValueParts(Instruction other) {
    if (isReturnVoid()) {
      return other.asReturn().isReturnVoid();
    } else {
      return other.asReturn().returnValue().type == returnValue().type;
    }
  }

  @Override
  public int compareNonValueParts(Instruction other) {
    if (isReturnVoid()) {
      return other.asReturn().isReturnVoid() ? 0 : -1;
    } else {
      return returnValue().type.ordinal() - other.asReturn().returnValue().type.ordinal();
    }
  }

  @Override
  public int maxInValueRegister() {
    return Constants.U8BIT_MAX;
  }

  @Override
  public int maxOutValueRegister() {
    assert false : "Return defines no values.";
    return 0;
  }

  @Override
  public boolean isReturn() {
    return true;
  }

  @Override
  public Return asReturn() {
    return this;
  }

  @Override
  public Constraint inliningConstraint(AppInfo info, DexType holder) {
    return Constraint.ALWAYS;
  }
}
