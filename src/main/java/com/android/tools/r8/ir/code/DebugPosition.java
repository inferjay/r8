// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.DebugLocalInfo;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.StringUtils;
import com.google.common.collect.ImmutableMap;

public class DebugPosition extends Instruction {

  public final int line;
  public final DexString file;
  private ImmutableMap<Integer, DebugLocalInfo> locals;

  public DebugPosition(int line, DexString file) {
    super(null);
    this.line = line;
    this.file = file;
  }

  @Override
  public boolean isDebugPosition() {
    return true;
  }

  @Override
  public DebugPosition asDebugPosition() {
    return this;
  }

  @Override
  public void buildDex(DexBuilder builder) {
    builder.addDebugPosition(this);
  }

  @Override
  public boolean identicalNonValueParts(Instruction other) {
    assert other.isDebugPosition();
    return false;
  }

  @Override
  public int compareNonValueParts(Instruction other) {
    assert other.isDebugPosition();
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
  public boolean canBeDeadCode(InternalOptions options) {
    return false;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(super.toString());
    if (file != null) {
      builder.append(file).append(":");
    }
    builder.append(line);
    if (locals != null && !locals.isEmpty()) {
      builder.append(", locals: ");
      StringUtils.append(builder, locals.values());
    }
    return builder.toString();
  }

  public void setLocals(ImmutableMap<Integer, DebugLocalInfo> locals) {
    this.locals = locals;
  }

  public ImmutableMap<Integer, DebugLocalInfo> getLocals() {
    return locals;
  }
}
