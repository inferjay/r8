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

  private final int[] keys;
  private final int[] targetBlockIndices;
  private int fallthroughBlockIndex;

  public Switch(
      Value value,
      int[] keys,
      int[] targetBlockIndices,
      int fallthroughBlockIndex) {
    super(null, value);
    this.keys = keys;
    this.targetBlockIndices = targetBlockIndices;
    this.fallthroughBlockIndex = fallthroughBlockIndex;
    assert valid();
  }

  private boolean valid() {
    assert keys.length <= Constants.U16BIT_MAX;
    // Keys must be acceding, and cannot target the fallthrough.
    assert keys.length == targetBlockIndices.length;
    for (int i = 1; i < keys.length - 1; i++) {
      assert keys[i - 1] < keys[i];
      assert targetBlockIndices[i] != fallthroughBlockIndex;
    }
    assert targetBlockIndices[keys.length - 1] != fallthroughBlockIndex;
    return true;
  }

  public Value value() {
    return inValues.get(0);
  }

  // Number of targets if this switch is emitted as a packed switch.
  private long numberOfTargetsIfPacked() {
    return ((long) keys[keys.length - 1]) - ((long) keys[0]) + 1;
  }

  private boolean canBePacked() {
    // The size of a switch payload is stored in an ushort in the Dex file.
    return numberOfTargetsIfPacked() <= Constants.U16BIT_MAX;
  }

  // Number of targets if this switch is emitted as a packed switch.
  private int numberOfTargetsForPacked() {
    assert canBePacked();
    return (int) numberOfTargetsIfPacked();
  }

  // Size of the switch payload if emitted as packed (in code units).
  private long packedPayloadSize() {
    return (numberOfTargetsForPacked() * 2) + 4;
  }

  // Size of the switch payload if emitted as sparse (in code units).
  private long sparsePayloadSize() {
    return (keys.length * 4) + 2;
  }

  private boolean emitPacked() {
    return canBePacked() && packedPayloadSize() <= sparsePayloadSize();
  }

  public int getFirstKey() {
    return keys[0];
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
    if (emitPacked()) {
      builder.addSwitch(this, new PackedSwitch(value));
    } else {
      builder.addSwitch(this, new SparseSwitch(value));
    }
  }

  public int numberOfKeys() {
    return keys.length;
  }

  public int getKey(int index) {
    return keys[index];
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

  public Nop buildPayload(int[] targets, int fallthroughTarget) {
    assert keys.length == targets.length;
    if (emitPacked()) {
      int targetsCount = numberOfTargetsForPacked();
      if (targets.length == targetsCount) {
        // All targets are already present.
        return new PackedSwitchPayload(getFirstKey(), targets);
      } else {
        // Generate the list of targets for all key values. Set the target for keys not present
        // to the fallthrough.
        int[] packedTargets = new int[targetsCount];
        int originalIndex = 0;
        for (int i = 0; i < targetsCount; i++) {
          int key = getFirstKey() + i;
          if (keys[originalIndex] == key) {
            packedTargets[i] = targets[originalIndex];
            originalIndex++;
          } else {
            packedTargets[i] = fallthroughTarget;
          }
        }
        assert originalIndex == keys.length;
        return new PackedSwitchPayload(getFirstKey(), packedTargets);
      }
    } else {
      assert numberOfKeys() == keys.length;
      return new SparseSwitchPayload(keys, targets);
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
    StringBuilder builder = new StringBuilder(super.toString()+ "\n");
    for (int i = 0; i < numberOfKeys(); i++) {
      builder.append("          ");
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
