// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.DebugLocalInfo;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.StringUtils;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;

public class DebugLocalsChange extends Instruction {

  private final Int2ReferenceMap<DebugLocalInfo> ending;
  private final Int2ReferenceMap<DebugLocalInfo> starting;

  public DebugLocalsChange(
      Int2ReferenceMap<DebugLocalInfo> ending, Int2ReferenceMap<DebugLocalInfo> starting) {
    super(null);
    this.ending = ending;
    this.starting = starting;
  }

  public Int2ReferenceMap<DebugLocalInfo> getEnding() {
    return ending;
  }

  public Int2ReferenceMap<DebugLocalInfo> getStarting() {
    return starting;
  }

  @Override
  public boolean isDebugLocalsChange() {
    return true;
  }

  @Override
  public DebugLocalsChange asDebugLocalsChange() {
    return this;
  }

  @Override
  public void buildDex(DexBuilder builder) {
    builder.addNop(this);
  }

  @Override
  public boolean identicalNonValueParts(Instruction other) {
    assert other.isDebugLocalsChange();
    return false;
  }

  @Override
  public int compareNonValueParts(Instruction other) {
    assert other.isDebugLocalsChange();
    return 0;
  }

  @Override
  public int maxInValueRegister() {
    throw new Unreachable();
  }

  @Override
  public int maxOutValueRegister() {
    throw new Unreachable();
  }

  @Override
  public boolean canBeDeadCode(IRCode code, InternalOptions options) {
    return false;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(super.toString());
    builder.append("ending: ");
    StringUtils.append(builder, ending.int2ReferenceEntrySet());
    builder.append(", starting: ");
    StringUtils.append(builder, starting.int2ReferenceEntrySet());
    return builder.toString();
  }
}
