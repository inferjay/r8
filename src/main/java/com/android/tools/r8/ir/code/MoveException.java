// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.ir.optimize.Inliner.Constraint;
import com.android.tools.r8.utils.InternalOptions;

public class MoveException extends Instruction {

  private DebugPosition position = null;

  public MoveException(Value dest) {
    super(dest);
  }

  public Value dest() {
    return outValue;
  }

  public DebugPosition getPosition() {
    return position;
  }

  public void setPosition(DebugPosition position) {
    this.position = position;
  }

  @Override
  public void buildDex(DexBuilder builder) {
    int dest = builder.allocatedRegister(dest(), getNumber());
    builder.add(this, new com.android.tools.r8.code.MoveException(dest));
  }

  @Override
  public int maxInValueRegister() {
    assert false : "MoveException has no register arguments.";
    return 0;
  }

  @Override
  public int maxOutValueRegister() {
    return Constants.U8BIT_MAX;
  }

  @Override
  public boolean identicalNonValueParts(Instruction other) {
    assert other.isMoveException();
    return true;
  }

  @Override
  public int compareNonValueParts(Instruction other) {
    assert other.isMoveException();
    return 0;
  }

  @Override
  public boolean isMoveException() {
    return true;
  }

  @Override
  public MoveException asMoveException() {
    return this;
  }

  @Override
  public boolean canBeDeadCode(IRCode code, InternalOptions options) {
    return !options.debug;
  }

  @Override
  public String toString() {
    if (position != null) {
      StringBuilder builder = new StringBuilder(super.toString());
      builder.append("(DebugPosition ");
      position.printLineInfo(builder);
      builder.append(')');
      return builder.toString();
    }
    return super.toString();
  }

  @Override
  public Constraint inliningConstraint(AppInfoWithSubtyping info, DexType holder) {
    // TODO(64432527): Revisit this constraint.
    return Constraint.NEVER;
  }
}
