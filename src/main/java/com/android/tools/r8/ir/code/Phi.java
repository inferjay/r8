// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.graph.DebugLocalInfo;
import com.android.tools.r8.ir.code.BasicBlock.EdgeType;
import com.android.tools.r8.ir.conversion.IRBuilder;
import com.android.tools.r8.utils.CfgPrinter;
import com.android.tools.r8.utils.ListUtils;
import com.android.tools.r8.utils.StringUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Phi extends Value {

  private final BasicBlock block;
  private final List<Value> operands = new ArrayList<>();

  // Trivial phis are eliminated during IR construction. When a trivial phi is eliminated
  // we need to update all references to it. A phi can be referenced from phis, instructions
  // and current definition mappings. This list contains the current definitions mappings that
  // contain this phi.
  private List<Map<Integer, Value>> definitionUsers = new ArrayList<>();

  // The computed out type is not always the same as 'this.type' because of the type
  // confusion around null and constant zero. The null object can be used in a single
  // context (if tests) and the single 0 can be used as null. A phi can therefore
  // have either of the creation types 'single' and 'object' depending on the use that
  // triggered the creation of the phi. We therefore have to delay the output type
  // computation of the phi until all operands are known.
  private MoveType outType = null;

  public Phi(int number, BasicBlock block, MoveType type, DebugLocalInfo local) {
    super(number, type, local == null ? null : new DebugInfo(local, null));
    this.block = block;
    block.addPhi(this);
  }

  @Override
  public boolean isPhi() {
    return true;
  }

  @Override
  public Phi asPhi() {
    return this;
  }

  public BasicBlock getBlock() {
    return block;
  }

  public void addOperands(IRBuilder builder, int register) {
    // Phi operands are only filled in once to complete the phi. Some phis are incomplete for a
    // period of time to break cycles. When the cycle has been resolved they are completed
    // exactly once by adding the operands.
    assert operands.isEmpty();
    boolean canBeNull = false;
    if (block.getPredecessors().size() == 0) {
      throwUndefinedValueError();
    }
    for (BasicBlock pred : block.getPredecessors()) {
      EdgeType edgeType = pred.getEdgeType(block);
      // Since this read has been delayed we must provide the local info for the value.
      Value operand = builder.readRegister(register, pred, edgeType, type, getLocalInfo());
      canBeNull |= operand.canBeNull();
      appendOperand(operand);
    }
    if (!canBeNull) {
      markNeverNull();
    }
    removeTrivialPhi();
  }

  public void addOperands(List<Value> operands) {
    // Phi operands are only filled in once to complete the phi. Some phis are incomplete for a
    // period of time to break cycles. When the cycle has been resolved they are completed
    // exactly once by adding the operands.
    assert this.operands.isEmpty();
    boolean canBeNull = false;
    if (operands.size() == 0) {
      throwUndefinedValueError();
    }
    for (Value operand : operands) {
      canBeNull |= operand.canBeNull();
      appendOperand(operand);
    }
    if (!canBeNull) {
      markNeverNull();
    }
    removeTrivialPhi();
  }

  private void throwUndefinedValueError() {
    throw new CompilationError(
        "Undefined value encountered during compilation. "
            + "This is typically caused by invalid dex input that uses a register "
            + "that is not define on all control-flow paths leading to the use.");
  }

  private void appendOperand(Value operand) {
    operands.add(operand);
    operand.addPhiUser(this);
  }

  public Value getOperand(int predIndex) {
    return operands.get(predIndex);
  }

  public List<Value> getOperands() {
    return operands;
  }

  public void removeOperand(int index) {
    operands.get(index).removePhiUser(this);
    operands.remove(index);
  }

  public void removeOperandsByIndex(List<Integer> operandsToRemove) {
    if (operandsToRemove.isEmpty()) {
      return;
    }
    List<Value> copy = new ArrayList<>(operands);
    operands.clear();
    int current = 0;
    for (int i : operandsToRemove) {
      operands.addAll(copy.subList(current, i));
      copy.get(i).removePhiUser(this);
      current = i + 1;
    }
    operands.addAll(copy.subList(current, copy.size()));
  }

  public void replace(int predIndex, Value newValue) {
    Value current = operands.get(predIndex);
    operands.set(predIndex, newValue);
    newValue.addPhiUser(this);
    current.removePhiUser(this);
  }

  // Removing the phi user from the current value leads to concurrent modification errors
  // during trivial phi elimination. It is safe to not remove the phi user from current
  // since current will be unreachable after trivial phi elimination.
  // TODO(ager): can we unify the these replace methods and avoid the concurrent modification
  // issue?
  private void replaceTrivialPhi(Value current, Value newValue) {
    for (int i = 0; i < operands.size(); i++) {
      if (operands.get(i) == current) {
        operands.set(i, newValue);
        newValue.addPhiUser(this);
      }
    }
  }

  public boolean isTrivialPhi() {
    Value same = null;
    for (Value op : operands) {
      if (op == same || op == this) {
        // Have only seen one value other than this.
        continue;
      }
      if (same != null) {
        // Merged at least two values and is therefore not trivial.
        return false;
      }
      same = op;
    }
    return true;
  }

  public void removeTrivialPhi() {
    Value same = null;
    for (Value op : operands) {
      if (op == same || op == this) {
        // Have only seen one value other than this.
        continue;
      }
      if (same != null) {
        // Merged at least two values and is therefore not trivial.
        assert !isTrivialPhi();
        return;
      }
      same = op;
    }
    assert isTrivialPhi();
    // Removing this phi, so get rid of it as a phi user from all of the operands to avoid
    // recursively getting back here with the same phi. If the phi has itself as an operand
    // that also removes the self-reference.
    for (Value op : operands) {
      op.removePhiUser(this);
    }
    // Replace this phi with the unique value in all users.
    for (Instruction user : uniqueUsers()) {
      user.replaceValue(this, same);
    }
    for (Phi user : uniquePhiUsers()) {
      user.replaceTrivialPhi(this, same);
    }
    if (debugUsers() != null) {
      for (Instruction user : debugUsers()) {
        user.replaceDebugPhi(this, same);
      }
    }
    // If IR construction is taking place, update the definition users.
    if (definitionUsers != null) {
      for (Map<Integer, Value> user : definitionUsers) {
        for (Map.Entry<Integer, Value> entry : user.entrySet()) {
          if (entry.getValue() == this) {
            entry.setValue(same);
            if (same.isPhi()) {
              same.asPhi().addDefinitionsUser(user);
            }
          }
        }
      }
    }
    // Try to simplify phi users that might now have become trivial.
    for (Phi user : uniquePhiUsers()) {
      user.removeTrivialPhi();
    }
    // Get rid of the phi itself.
    block.removePhi(this);
  }

  public String printPhi() {
    StringBuilder builder = new StringBuilder();
    builder.append("v");
    builder.append(number);
    builder.append(" <- phi");
    StringUtils.append(builder, ListUtils.map(operands, (Value operand) -> "v" + operand.number));
    return builder.toString();
  }

  public void print(CfgPrinter printer) {
    int uses = numberOfPhiUsers() + numberOfUsers();
    printer
        .print("0 ")                 // bci
        .append(uses)                // use
        .append(" v").append(number) // tid
        .append(" Phi");
    for (Value operand : operands) {
      printer.append(" v").append(operand.number);
    }
  }

  public void addDefinitionsUser(Map<Integer, Value> currentDefinitions) {
    definitionUsers.add(currentDefinitions);
  }

  public void removeDefinitionsUser(Map<Integer, Value> currentDefinitions) {
    definitionUsers.remove(currentDefinitions);
  }

  public void clearDefinitionsUsers() {
    definitionUsers = null;
  }

  private boolean isSingleConstZero(Value value) {
    return value.definition != null && value.definition.isConstNumber() &&
        value.definition.asConstNumber().isZero() &&
        value.outType() == MoveType.SINGLE;
  }

  private MoveType computeOutType(Set<Phi> active) {
    if (outType != null) {
      return outType;
    }
    active.add(this);
    // Go through non-phi operands first to determine if we have an operand that dictates the type.
    for (Value operand : operands) {
      // Since a constant zero can be either an integer or an Object (null) we skip them
      // when computing types and rely on other operands to specify the actual type.
      if (!operand.isPhi() && !isSingleConstZero(operand)) {
        return operand.outType();
      }
    }
    // We did not find a non-phi operand that dictates the type. Recurse on phi arguments.
    for (Value operand : operands) {
      if (operand.isPhi() && !active.contains(operand)) {
        MoveType phiType = operand.asPhi().computeOutType(active);
        // TODO(zerny): If we had a CONST_ZERO type element, we could often avoid going through
        // all phis. We would only have to recurse until we got a non CONST_ZERO out type.
        if (phiType != MoveType.SINGLE) {
          return phiType;
        }
      }
    }
    // All operands were the constant zero or phis with out type SINGLE and the out type is either
    // object or single depending on the use. Since all inputs have out type SINGLE it is safe to
    // return MoveType.SINGLE here.
    assert type == MoveType.SINGLE || type == MoveType.OBJECT;
    return MoveType.SINGLE;
  }

  @Override
  public MoveType outType() {
    if (outType != null) {
      return outType;
    }
    return computeOutType(new HashSet<>());
  }

  @Override
  public boolean isConstant() {
    return false;
  }

  public boolean needsRegister() {
    return true;
  }
}
