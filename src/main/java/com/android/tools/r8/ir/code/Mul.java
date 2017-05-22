// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

import com.android.tools.r8.code.MulDouble;
import com.android.tools.r8.code.MulDouble2Addr;
import com.android.tools.r8.code.MulFloat;
import com.android.tools.r8.code.MulFloat2Addr;
import com.android.tools.r8.code.MulInt;
import com.android.tools.r8.code.MulInt2Addr;
import com.android.tools.r8.code.MulIntLit16;
import com.android.tools.r8.code.MulIntLit8;
import com.android.tools.r8.code.MulLong;
import com.android.tools.r8.code.MulLong2Addr;

public class Mul extends ArithmeticBinop {

  public Mul(NumericType type, Value dest, Value left, Value right) {
    super(type, dest, left, right);
  }

  @Override
  public boolean isCommutative() {
    return true;
  }

  public com.android.tools.r8.code.Instruction CreateInt(int dest, int left, int right) {
    return new MulInt(dest, left, right);
  }

  public com.android.tools.r8.code.Instruction CreateLong(int dest, int left, int right) {
    return new MulLong(dest, left, right);
  }

  public com.android.tools.r8.code.Instruction CreateFloat(int dest, int left, int right) {
    return new MulFloat(dest, left, right);
  }

  public com.android.tools.r8.code.Instruction CreateDouble(int dest, int left, int right) {
    return new MulDouble(dest, left, right);
  }

  public com.android.tools.r8.code.Instruction CreateInt2Addr(int left, int right) {
    return new MulInt2Addr(left, right);
  }

  public com.android.tools.r8.code.Instruction CreateLong2Addr(int left, int right) {
    return new MulLong2Addr(left, right);
  }

  public com.android.tools.r8.code.Instruction CreateFloat2Addr(int left, int right) {
    return new MulFloat2Addr(left, right);
  }

  public com.android.tools.r8.code.Instruction CreateDouble2Addr(int left, int right) {
    return new MulDouble2Addr(left, right);
  }

  public com.android.tools.r8.code.Instruction CreateIntLit8(int dest, int left, int constant) {
    return new MulIntLit8(dest, left, constant);
  }

  public com.android.tools.r8.code.Instruction CreateIntLit16(int dest, int left, int constant) {
    return new MulIntLit16(dest, left, constant);
  }

  @Override
  public boolean identicalNonValueParts(Instruction other) {
    return other.asMul().type == type;
  }

  @Override
  public int compareNonValueParts(Instruction other) {
    return type.ordinal() - other.asMul().type.ordinal();
  }

  @Override
  int foldIntegers(int left, int right) {
    return left * right;
  }

  @Override
  long foldLongs(long left, long right) {
    return left * right;
  }

  @Override
  float foldFloat(float left, float right) {
    return left * right;
  }

  @Override
  double foldDouble(double left, double right) {
    return left * right;
  }

  @Override
  public boolean isMul() {
    return true;
  }

  @Override
  public Mul asMul() {
    return this;
  }
}
