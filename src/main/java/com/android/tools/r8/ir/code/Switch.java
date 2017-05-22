// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

import com.android.tools.r8.code.Nop;
import com.android.tools.r8.code.PackedSwitch;
import com.android.tools.r8.code.PackedSwitchPayload;
import com.android.tools.r8.code.SparseSwitch;
import com.android.tools.r8.code.SparseSwitchPayload;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.utils.CfgPrinter;

public class Switch extends JumpInstruction {

  public enum Type {
    PACKED, SPARSE
  }

  private final Type type;
  private final int[] keys;
  private final int[] targetBlockIndices;
  private int fallthroughBlockIndex;

  public Switch(
      Type type,
      Value value,
      int[] keys,
      int[] targetBlockIndices,
      int fallthroughBlockIndex) {
    super(null, value);
    this.type = type;
    this.keys = keys;
    this.targetBlockIndices = targetBlockIndices;
    this.fallthroughBlockIndex = fallthroughBlockIndex;
  }

  private Value value() {
    return inValues.get(0);
  }

  @Override
  public boolean isSwitch() {
    return true;
  }

  @Override
  public Switch asSwitch() {
    return this;
  }

  @Override
  public boolean identicalNonValueParts(Instruction other) {
    assert other.isSwitch();
    return false;
  }

  @Override
  public int compareNonValueParts(Instruction other) {
    assert other.isSwitch();
    return 0;
  }

  @Override
  public void buildDex(DexBuilder builder) {
    int value = builder.allocatedRegister(value(), getNumber());
    if (type == Type.PACKED) {
      builder.addSwitch(this, new PackedSwitch(value));
    } else {
      builder.addSwitch(this, new SparseSwitch(value));
    }
  }

  private int numberOfKeys() {
    return targetBlockIndices.length;
  }

  public int[] targetBlockIndices() {
    return targetBlockIndices;
  }

  @Override
  public BasicBlock fallthroughBlock() {
    return getBlock().getSuccessors().get(fallthroughBlockIndex);
  }

  public int getFallthroughBlockIndex() {
    return fallthroughBlockIndex;
  }

  public void setFallthroughBlockIndex(int i) {
    fallthroughBlockIndex = i;
  }

  public BasicBlock targetBlock(int index) {
    return getBlock().getSuccessors().get(targetBlockIndices()[index]);
  }

  @Override
  public void setFallthroughBlock(BasicBlock block) {
    getBlock().getSuccessors().set(fallthroughBlockIndex, block);
  }

  public Nop buildPayload(int[] targets) {
    if (type == Type.PACKED) {
      return new PackedSwitchPayload(numberOfKeys(), keys[0], targets);
    } else {
      return new SparseSwitchPayload(numberOfKeys(), keys, targets);
    }
  }

  @Override
  public int maxInValueRegister() {
    return Constants.U8BIT_MAX;
  }

  @Override
  public int maxOutValueRegister() {
    return Constants.U8BIT_MAX;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(
        super.toString() + " (" + (type == Type.PACKED ? "PACKED" : "SPARSE") + ")\n");
    for (int i = 0; i < numberOfKeys(); i++) {
      builder.append("          ");
      if (type == Type.PACKED) {
        builder.append(keys[0] + i);
      } else {
        builder.append(keys[i]);
      }
      builder.append(" -> ");
      builder.append(targetBlock(i).getNumber());
      builder.append("\n");
    }
    builder.append("          F -> ");
    builder.append(fallthroughBlock().getNumber());
    return builder.toString();
  }

  @Override
  public void print(CfgPrinter printer) {
    super.print(printer);
    for (int index : targetBlockIndices) {
      BasicBlock target = getBlock().getSuccessors().get(index);
      printer.append(" B").append(target.getNumber());
    }
  }
}
