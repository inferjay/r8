// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.code.Const;
import com.android.tools.r8.code.Const16;
import com.android.tools.r8.code.Const4;
import com.android.tools.r8.code.ConstHigh16;
import com.android.tools.r8.code.ConstWide;
import com.android.tools.r8.code.ConstWide16;
import com.android.tools.r8.code.ConstWide32;
import com.android.tools.r8.code.ConstWideHigh16;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.graph.AppInfo;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.ir.optimize.Inliner.Constraint;
import com.android.tools.r8.utils.NumberUtils;

public class ConstNumber extends ConstInstruction {

  public final ConstType type;
  private final long value;

  public ConstNumber(ConstType type, Value dest, long value) {
    super(dest);
    // We create const numbers after register allocation for rematerialization of values. Those
    // are all for fixed register values. All other values that are used as the destination for
    // const number instructions should be marked as constants.
    assert dest.isFixedRegisterValue() || dest.definition.isConstNumber();
    assert type != ConstType.OBJECT;
    this.type = type;
    this.value = value;
  }

  public static ConstNumber copyOf(IRCode code, ConstNumber original) {
    Value newValue =
        new Value(
            code.valueNumberGenerator.next(),
            original.outType(),
            original.getDebugInfo());
    return new ConstNumber(original.type, newValue, original.getRawValue());
  }

  private boolean preciseTypeUnknown() {
    return type == ConstType.INT_OR_FLOAT || type == ConstType.LONG_OR_DOUBLE;
  }

  public Value dest() {
    return outValue;
  }

  public int getIntValue() {
    assert type == ConstType.INT || type == ConstType.INT_OR_FLOAT;
    return (int) value;
  }

  public long getLongValue() {
    assert type == ConstType.LONG || type == ConstType.LONG_OR_DOUBLE;
    return value;
  }

  public float getFloatValue() {
    assert type == ConstType.FLOAT || type == ConstType.INT_OR_FLOAT;
    return Float.intBitsToFloat((int) value);
  }

  public double getDoubleValue() {
    assert type == ConstType.DOUBLE || type == ConstType.LONG_OR_DOUBLE;
    return Double.longBitsToDouble(value);
  }

  public long getRawValue() {
    return value;
  }

  public boolean isZero() {
    return value == 0;
  }

  public boolean isIntegerNegativeOne(NumericType type) {
    assert type == NumericType.INT || type == NumericType.LONG;
    if (type == NumericType.INT) {
      return getIntValue() == -1;
    }
    return getLongValue() == -1;
  }

  @Override
  public void buildDex(DexBuilder builder) {
    if (!dest().needsRegister()) {
      builder.addNop(this);
      return;
    }

    int register = builder.allocatedRegister(dest(), getNumber());
    if (MoveType.fromConstType(type) == MoveType.SINGLE) {
      assert NumberUtils.is32Bit(value);
      if ((register & 0xf) == register && NumberUtils.is4Bit(value)) {
        builder.add(this, new Const4(register, (int) value));
      } else if (NumberUtils.is16Bit(value)) {
        builder.add(this, new Const16(register, (int) value));
      } else if ((value & 0x0000ffffL) == 0) {
        builder.add(this, new ConstHigh16(register, ((int) value) >>> 16));
      } else {
        builder.add(this, new Const(register, (int) value));
      }
    } else {
      assert MoveType.fromConstType(type) == MoveType.WIDE;
      if (NumberUtils.is16Bit(value)) {
        builder.add(this, new ConstWide16(register, (int) value));
      } else if ((value & 0x0000ffffffffffffL) == 0) {
        builder.add(this, new ConstWideHigh16(register, (int) (value >>> 48)));
      } else if (NumberUtils.is32Bit(value)) {
        builder.add(this, new ConstWide32(register, (int) value));
      } else {
        builder.add(this, new ConstWide(register, value));
      }
    }
  }

  @Override
  public int maxInValueRegister() {
    assert false : "Const has no register arguments.";
    return 0;
  }

  @Override
  public int maxOutValueRegister() {
    return Constants.U8BIT_MAX;
  }

  @Override
  public String toString() {
    return super.toString() + " " + value + " (" + type + ")";
  }

  @Override
  public boolean identicalNonValueParts(Instruction other) {
    if (preciseTypeUnknown()) {
      return false;
    }
    ConstNumber o = other.asConstNumber();
    return o.type == type && o.value == value;
  }

  @Override
  public int compareNonValueParts(Instruction other) {
    ConstNumber o = other.asConstNumber();
    int result;
    result = type.ordinal() - o.type.ordinal();
    if (result != 0) {
      return result;
    }
    return Long.signum(value - o.value);
  }

  public boolean is8Bit() {
    return NumberUtils.is8Bit(value);
  }

  public boolean negativeIs8Bit() {
    return NumberUtils.negativeIs8Bit(value);
  }

  public boolean is16Bit() {
    return NumberUtils.is16Bit(value);
  }

  public boolean negativeIs16Bit() {
    return NumberUtils.negativeIs16Bit(value);
  }

  @Override
  public boolean isOutConstant() {
    return true;
  }

  @Override
  public boolean isConstNumber() {
    return true;
  }

  @Override
  public ConstNumber asConstNumber() {
    return this;
  }

  @Override
  public Constraint inliningConstraint(AppInfo info, DexType holder) {
    return Constraint.ALWAYS;
  }
}
