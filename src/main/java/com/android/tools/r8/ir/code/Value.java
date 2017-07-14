// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.graph.DebugLocalInfo;
import com.android.tools.r8.ir.regalloc.LiveIntervals;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.LongInterval;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Value {

  /**
   * Immutable view of the debug info associated with an SSA value.
   *
   * Used during IR building and to construct replacement values.
   */
  public static class DebugInfo {
    private final DebugLocalInfo local;
    private final Value previousLocalValue;

    public DebugInfo(DebugLocalInfo local, Value previousLocalValue) {
      assert local != null;
      this.local = local;
      this.previousLocalValue = previousLocalValue;
    }
  }

  // Actual internal data for the debug information of locals.
  // This is wrapped in a class to avoid multiple pointers in the value structure.
  private static class DebugData {
    final DebugLocalInfo local;
    Value previousLocalValue;
    Set<Instruction> debugUsers = new HashSet<>();
    List<Instruction> localStarts = new ArrayList<>();
    List<Instruction> localEnds = new ArrayList<>();

    DebugData(DebugInfo info) {
      this(info.local, info.previousLocalValue);
    }

    DebugData(DebugLocalInfo local, Value previousLocalValue) {
      assert previousLocalValue == null || !previousLocalValue.isUninitializedLocal();
      this.local = local;
      this.previousLocalValue = previousLocalValue;
    }
  }

  public static final Value UNDEFINED = new Value(-1, MoveType.OBJECT, null);

  protected final int number;
  protected final MoveType type;
  public Instruction definition = null;
  private LinkedList<Instruction> users = new LinkedList<>();
  private Set<Instruction> uniqueUsers = null;
  private LinkedList<Phi> phiUsers = new LinkedList<>();
  private Set<Phi> uniquePhiUsers = null;
  private Value nextConsecutive = null;
  private Value previousConsecutive = null;
  private LiveIntervals liveIntervals;
  private int needsRegister = -1;
  private boolean neverNull = false;
  private boolean isThis = false;
  private boolean isArgument = false;
  private LongInterval valueRange;
  private final DebugData debugData;

  public Value(int number, MoveType type, DebugInfo debugInfo) {
    this.number = number;
    this.type = type;
    this.debugData = debugInfo == null ? null : new DebugData(debugInfo);
  }

  public boolean isFixedRegisterValue() {
    return false;
  }

  public FixedRegisterValue asFixedRegisterValue() {
    return null;
  }

  public int getNumber() {
    return number;
  }

  public int requiredRegisters() {
    return type.requiredRegisters();
  }

  public DebugInfo getDebugInfo() {
    return debugData == null ? null : new DebugInfo(debugData.local, debugData.previousLocalValue);
  }

  public DebugLocalInfo getLocalInfo() {
    return debugData == null ? null : debugData.local;
  }

  public Value getPreviousLocalValue() {
    return debugData == null ? null : debugData.previousLocalValue;
  }

  public void replacePreviousLocalValue(Value value) {
    if (value == null || value.isUninitializedLocal()) {
      debugData.previousLocalValue = null;
    } else {
      debugData.previousLocalValue = value;
    }
  }

  public List<Instruction> getDebugLocalStarts() {
    return debugData.localStarts;
  }

  public List<Instruction> getDebugLocalEnds() {
    return debugData.localEnds;
  }

  public void addDebugLocalStart(Instruction start) {
    assert start != null;
    debugData.localStarts.add(start);
  }

  public void addDebugLocalEnd(Instruction end) {
    assert end != null;
    debugData.localEnds.add(end);
  }

  public void linkTo(Value other) {
    assert nextConsecutive == null || nextConsecutive == other;
    assert other.previousConsecutive == null || other.previousConsecutive == this;
    other.previousConsecutive = this;
    nextConsecutive = other;
  }

  public void replaceLink(Value newArgument) {
    assert isLinked();
    if (previousConsecutive != null) {
      previousConsecutive.nextConsecutive = newArgument;
      newArgument.previousConsecutive = previousConsecutive;
      previousConsecutive = null;
    }
    if (nextConsecutive != null) {
      nextConsecutive.previousConsecutive = newArgument;
      newArgument.nextConsecutive = nextConsecutive;
      nextConsecutive = null;
    }
  }

  public boolean isLinked() {
    return nextConsecutive != null || previousConsecutive != null;
  }

  public Value getStartOfConsecutive() {
    Value current = this;
    while (current.getPreviousConsecutive() != null) {
      current = current.getPreviousConsecutive();
    }
    return current;
  }

  public Value getNextConsecutive() {
    return nextConsecutive;
  }

  public Value getPreviousConsecutive() {
    return previousConsecutive;
  }

  public Set<Instruction> uniqueUsers() {
    if (uniqueUsers != null) {
      return uniqueUsers;
    }
    return uniqueUsers = ImmutableSet.copyOf(users);
  }

  public Set<Phi> uniquePhiUsers() {
    if (uniquePhiUsers != null) {
      return uniquePhiUsers;
    }
    return uniquePhiUsers = ImmutableSet.copyOf(phiUsers);
  }

  public Set<Instruction> debugUsers() {
    if (debugData == null) {
      return null;
    }
    return Collections.unmodifiableSet(debugData.debugUsers);
  }

  public int numberOfUsers() {
    int size = users.size();
    if (size <= 1) {
      return size;
    }
    return uniqueUsers().size();
  }

  public int numberOfPhiUsers() {
    int size = phiUsers.size();
    if (size <= 1) {
      return size;
    }
    return uniquePhiUsers().size();
  }

  public int numberOfDebugUsers() {
    return debugData == null ? 0 : debugData.debugUsers.size();
  }

  public int numberOfAllUsers() {
    return numberOfUsers() + numberOfPhiUsers() + numberOfDebugUsers();
  }

  public void addUser(Instruction user) {
    users.add(user);
    uniqueUsers = null;
  }

  public void removeUser(Instruction user) {
    users.remove(user);
    uniqueUsers = null;
  }

  public void clearUsers() {
    users.clear();
    uniqueUsers = null;
    phiUsers.clear();
    uniquePhiUsers = null;
    if (debugData != null) {
      debugData.debugUsers.clear();
    }
  }

  public void addPhiUser(Phi user) {
    phiUsers.add(user);
    uniquePhiUsers = null;
  }

  public void removePhiUser(Phi user) {
    phiUsers.remove(user);
    uniquePhiUsers = null;
  }

  public void addDebugUser(Instruction user) {
    if (isUninitializedLocal()) {
      return;
    }
    debugData.debugUsers.add(user);
  }

  public boolean isUninitializedLocal() {
    return definition != null && definition.isDebugLocalUninitialized();
  }

  public boolean isInitializedLocal() {
    return !isUninitializedLocal();
  }

  public void removeDebugUser(Instruction user) {
    debugData.debugUsers.remove(user);
  }

  public boolean hasUsersInfo() {
    return users != null;
  }

  public void clearUsersInfo() {
    users = null;
    uniqueUsers = null;
    phiUsers = null;
    uniquePhiUsers = null;
    if (debugData != null) {
      debugData.debugUsers = null;
    }
  }

  public void replaceUsers(Value newValue) {
    if (this == newValue) {
      return;
    }
    for (Instruction user : uniqueUsers()) {
      user.inValues.replaceAll(v -> {
        if (v == this) {
          newValue.addUser(user);
          return newValue;
        }
        return v;
      });
    }
    for (Phi user : uniquePhiUsers()) {
      user.getOperands().replaceAll(v -> {
        if (v == this) {
          newValue.addPhiUser(user);
          return newValue;
        }
        return v;
      });
    }
    if (debugData != null) {
      for (Instruction user : debugUsers()) {
        user.getDebugValues().replaceAll(v -> {
          if (v == this) {
            newValue.addDebugUser(user);
            return newValue;
          }
          return v;
        });
        if (user.getPreviousLocalValue() == this) {
          newValue.addDebugUser(user);
          user.replacePreviousLocalValue(newValue);
        }
      }
    }
    clearUsers();
  }

  public void setLiveIntervals(LiveIntervals intervals) {
    assert liveIntervals == null;
    liveIntervals = intervals;
  }

  public LiveIntervals getLiveIntervals() {
    return liveIntervals;
  }

  public boolean needsRegister() {
    assert needsRegister >= 0;
    assert !hasUsersInfo() || (needsRegister > 0) == internalComputeNeedsRegister();
    return needsRegister > 0;
  }

  public void setNeedsRegister(boolean value) {
    assert needsRegister == -1 || (needsRegister > 0) == value;
    needsRegister = value ? 1 : 0;
  }

  public void computeNeedsRegister() {
    assert needsRegister < 0;
    setNeedsRegister(internalComputeNeedsRegister());
  }

  public boolean internalComputeNeedsRegister() {
    if (!isConstant()) {
      return true;
    }
    if (numberOfPhiUsers() > 0) {
      return true;
    }
    for (Instruction user : uniqueUsers()) {
      if (user.needsValueInRegister(this)) {
        return true;
      }
    }
    return false;
  }

  public boolean hasRegisterConstraint() {
    for (Instruction instruction : uniqueUsers()) {
      if (instruction.maxInValueRegister() != Constants.U16BIT_MAX) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    return number;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("v");
    builder.append(number);
    boolean isConstant = definition != null && definition.isConstNumber();
    boolean hasLocalInfo = getLocalInfo() != null;
    if (isConstant || hasLocalInfo) {
      builder.append("(");
      if (isConstant) {
        ConstNumber constNumber = getConstInstruction().asConstNumber();
        if (constNumber.outType() == MoveType.SINGLE) {
          builder.append((int) constNumber.getRawValue());
        } else {
          builder.append(constNumber.getRawValue());
        }
      }
      if (isConstant && hasLocalInfo) {
        builder.append(", ");
      }
      if (hasLocalInfo) {
        builder.append(getLocalInfo());
      }
      builder.append(")");
    }
    if (valueRange != null) {
      builder.append(valueRange);
    }
    return builder.toString();
  }

  public MoveType outType() {
    return type;
  }

  public ConstInstruction getConstInstruction() {
    assert isConstant();
    return definition.getOutConstantConstInstruction();
  }

  public boolean isConstant() {
    return definition.isOutConstant() && getLocalInfo() == null;
  }

  public boolean isPhi() {
    return false;
  }

  public Phi asPhi() {
    return null;
  }

  public void markNeverNull() {
    assert !neverNull;
    neverNull = true;
  }

  /**
   * Returns whether this value is know to never be <code>null</code>.
   */
  public boolean isNeverNull() {
    return neverNull;
  }

  public boolean canBeNull() {
    return !neverNull;
  }

  public void markAsArgument() {
    assert !isArgument;
    assert !isThis;
    isArgument = true;
  }

  public boolean isArgument() {
    return isArgument;
  }

  public void markAsThis() {
    assert isArgument;
    assert !isThis;
    isThis = true;
    markNeverNull();
  }

  /**
   * Returns whether this value is known to be the receiver (this argument) in a method body.
   * <p>
   * For a receiver value {@link #isNeverNull()} is guarenteed to be <code>true</code> as well.
   */
  public boolean isThis() {
    return isThis;
  }

  public void setValueRange(LongInterval range) {
    valueRange = range;
  }

  public boolean hasValueRange() {
    return valueRange != null || isConstant();
  }

  public boolean isValueInRange(int value) {
    if (isConstant()) {
      return value == getConstInstruction().asConstNumber().getIntValue();
    } else {
      return valueRange != null && valueRange.containsValue(value);
    }
  }

  public LongInterval getValueRange() {
    if (isConstant()) {
      if (type == MoveType.SINGLE) {
        int value = getConstInstruction().asConstNumber().getIntValue();
        return new LongInterval(value, value);
      } else {
        assert type == MoveType.WIDE;
        long value = getConstInstruction().asConstNumber().getLongValue();
        return new LongInterval(value, value);
      }
    } else {
      return valueRange;
    }
  }

  public boolean isDead(InternalOptions options) {
    // Totally unused values are trivially dead.
    return numberOfAllUsers() == 0 || isDead(new HashSet<>(), options);
  }

  protected boolean isDead(Set<Value> active, InternalOptions options) {
    // If the value has debug users we cannot eliminate it since it represents a value in a local
    // variable that should be visible in the debugger.
    if (numberOfDebugUsers() != 0) {
      return false;
    }
    // This is a candidate for a dead value. Guard against looping by adding it to the set of
    // currently active values.
    active.add(this);
    for (Instruction instruction : uniqueUsers()) {
      if (!instruction.canBeDeadCode(null, options)) {
        return false;
      }
      Value outValue = instruction.outValue();
      // Instructions with no out value cannot be dead code by the current definition
      // (unused out value). They typically side-effect input values or deals with control-flow.
      assert outValue != null;
      if (!active.contains(outValue) && !outValue.isDead(active, options)) {
        return false;
      }
    }
    for (Phi phi : uniquePhiUsers()) {
      if (!active.contains(phi) && !phi.isDead(active, options)) {
        return false;
      }
    }
    return true;
  }
}
