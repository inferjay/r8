// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.utils.InternalOptions;

/**
 * Instruction reading an SSA value with attached local information.
 *
 * This instruction ensures that a value with a local is kept alive until at least this read.
 *
 * This instruction must not be considered dead until live-ranges have been computed for locals
 * after which it will be removed.
 */
public class DebugLocalRead extends Instruction {

  public DebugLocalRead(Value src) {
    super(null, src);
    assert src.getLocalInfo() != null;
  }

  public Value src() {
    return inValues.get(0);
  }

  @Override
  public boolean isDebugLocalRead() {
    return true;
  }

  @Override
  public DebugLocalRead asDebugLocalRead() {
    return this;
  }

  @Override
  public boolean identicalNonValueParts(Instruction other) {
    assert other.isDebugLocalRead();
    return true;
  }

  @Override
  public int compareNonValueParts(Instruction other) {
    assert other.isDebugLocalRead();
    return 0;
  }

  @Override
  public boolean canBeDeadCode(IRCode code, InternalOptions options) {
    return false;
  }

  @Override
  public void buildDex(DexBuilder builder) {
    throw new Unreachable();
  }

  @Override
  public int maxInValueRegister() {
    return Constants.U16BIT_MAX;
  }

  @Override
  public int maxOutValueRegister() {
    throw new Unreachable();
  }

  public void addDebugLocalStart() {
    src().addDebugLocalStart(this);
  }

  public void addDebugLocalEnd() {
    src().addDebugLocalEnd(this);
  }
}
