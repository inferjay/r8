// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.regalloc.LinearScanRegisterAllocator;
import com.android.tools.r8.utils.CfgPrinter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.stream.Collectors;

public class IRCode {

  public final DexEncodedMethod method;

  public LinkedList<BasicBlock> blocks;
  public final ValueNumberGenerator valueNumberGenerator;

  private BasicBlock normalExitBlock;
  private boolean numbered = false;
  private int nextInstructionNumber = 0;

  public IRCode(
      DexEncodedMethod method,
      LinkedList<BasicBlock> blocks,
      BasicBlock normalExitBlock,
      ValueNumberGenerator valueNumberGenerator) {
    this.method = method;
    this.blocks = blocks;
    this.normalExitBlock = normalExitBlock;
    this.valueNumberGenerator = valueNumberGenerator;
  }

  private void ensureBlockNumbering() {
    if (!numbered) {
      numbered = true;
      BasicBlock[] sorted = topologicallySortedBlocks();
      for (int i = 0; i < sorted.length; i++) {
        sorted[i].setNumber(i);
      }
    }
  }

  @Override
  public String toString() {
    ensureBlockNumbering();
    StringBuilder builder = new StringBuilder();
    builder.append("blocks:\n");
    for (BasicBlock block : blocks) {
      builder.append(block.toDetailedString());
      builder.append("\n");
    }
    return builder.toString();
  }

  public void clearMarks() {
    for (BasicBlock block : blocks) {
      block.clearMark();
    }
  }

  public void removeMarkedBlocks() {
    ListIterator<BasicBlock> blockIterator = listIterator();
    while (blockIterator.hasNext()) {
      BasicBlock block = blockIterator.next();
      if (block.isMarked()) {
        blockIterator.remove();
        if (block == normalExitBlock) {
          normalExitBlock = null;
        }
      }
    }
  }

  public void removeBlocks(List<BasicBlock> blocksToRemove) {
    blocks.removeAll(blocksToRemove);
    if (blocksToRemove.contains(normalExitBlock)) {
      normalExitBlock = null;
    }
  }

  /**
   * Compute quasi topologically sorted list of the basic blocks using depth first search.
   *
   * TODO(ager): We probably want to compute strongly connected components and topologically
   * sort strongly connected components instead. However, this is much better than having
   * no sorting.
   */
  public BasicBlock[] topologicallySortedBlocks() {
    return topologicallySortedBlocks(Collections.emptyList());
  }

  public BasicBlock[] topologicallySortedBlocks(List<BasicBlock> blocksToIgnore) {
    clearMarks();
    int reachableBlocks = blocks.size() - blocksToIgnore.size();
    BasicBlock[] sorted = new BasicBlock[reachableBlocks];
    BasicBlock entryBlock = blocks.getFirst();
    int index = depthFirstSorting(entryBlock, sorted, reachableBlocks - 1);
    assert index == -1;
    return sorted;
  }

  private int depthFirstSorting(BasicBlock block, BasicBlock[] sorted, int index) {
    if (!block.isMarked()) {
      block.mark();
      for (BasicBlock succ : block.getSuccessors()) {
        index = depthFirstSorting(succ, sorted, index);
      }
      assert sorted[index] == null;
      sorted[index] = block;
      return index - 1;
    }
    return index;
  }

  public void print(CfgPrinter printer) {
    ensureBlockNumbering();
    for (BasicBlock block : blocks) {
      block.print(printer);
    }
  }

  public boolean isConsistentSSA() {
    assert isConsistentGraph() && consistentDefUseChains() && validThrowingInstructions();
    return true;
  }

  public boolean isConsistentGraph() {
    assert consistentBlockNumbering();
    assert consistentPredecessorSuccessors();
    assert consistentCatchHandlers();
    assert consistentBlockInstructions();
    assert normalExitBlock == null || normalExitBlock.exit().isReturn();
    return true;
  }

  private boolean consistentDefUseChains() {
    Set<Value> values = new HashSet<>();

    for (BasicBlock block : blocks) {
      int predecessorCount = block.getPredecessors().size();
      // Check that all phi uses are consistent.
      for (Phi phi : block.getPhis()) {
        assert phi.getOperands().size() == predecessorCount;
        values.add(phi);
        for (Value value : phi.getOperands()) {
          values.add(value);
          if (value.isPhi()) {
            Phi phiOperand = value.asPhi();
            assert phiOperand.getBlock().getPhis().contains(phiOperand);
            assert phiOperand.uniquePhiUsers().contains(phi);
          } else {
            Instruction definition = value.definition;
            assert definition.outValue() == value;
          }
        }
      }
      for (Instruction instruction : block.getInstructions()) {
        assert instruction.getBlock() == block;
        Value outValue = instruction.outValue();
        if (outValue != null) {
          values.add(outValue);
          assert outValue.definition == instruction;
          Value previousLocalValue = outValue.getPreviousLocalValue();
          if (previousLocalValue != null) {
            values.add(previousLocalValue);
            assert previousLocalValue.debugUsers().contains(instruction);
          }
        }
        for (Value value : instruction.inValues()) {
          values.add(value);
          assert value.uniqueUsers().contains(instruction);
          if (value.isPhi()) {
            Phi phi = value.asPhi();
            assert phi.getBlock().getPhis().contains(phi);
          } else {
            Instruction definition = value.definition;
            assert definition.outValue() == value;
          }
        }
      }
    }

    for (Value value : values) {
      assert consistentValueUses(value);
    }

    return true;
  }

  private boolean consistentValueUses(Value value) {
    for (Instruction user : value.uniqueUsers()) {
      assert user.inValues().contains(value);
    }
    for (Phi phiUser : value.uniquePhiUsers()) {
      assert phiUser.getOperands().contains(value);
      assert phiUser.getBlock().getPhis().contains(phiUser);
    }
    if (value.debugUsers() != null) {
      for (Instruction debugUser : value.debugUsers()) {
        assert debugUser.getPreviousLocalValue() == value
            || debugUser.getDebugValues().contains(value);
      }
    }
    return true;
  }

  private boolean consistentPredecessorSuccessors() {
    for (BasicBlock block : blocks) {
      // Check that all successors are distinct.
      assert new HashSet<>(block.getSuccessors()).size() == block.getSuccessors().size();
      for (BasicBlock succ : block.getSuccessors()) {
        // Check that successors are in the block list.
        assert blocks.contains(succ);
        // Check that successors have this block as a predecessor.
        assert succ.getPredecessors().contains(block);
      }
      // Check that all predecessors are distinct.
      assert new HashSet<>(block.getPredecessors()).size() == block.getPredecessors().size();
      for (BasicBlock pred : block.getPredecessors()) {
        // Check that predecessors are in the block list.
        assert blocks.contains(pred);
        // Check that predecessors have this block as a successor.
        assert pred.getSuccessors().contains(block);
      }
    }
    return true;
  }

  private boolean consistentCatchHandlers() {
    for (BasicBlock block : blocks) {
      // Check that catch handlers are always the first successors of a block.
      if (block.hasCatchHandlers()) {
        assert block.exit().isGoto() || block.exit().isThrow();
        CatchHandlers<Integer> catchHandlers = block.getCatchHandlersWithSuccessorIndexes();
        // If there is a catch-all guard it must be the last.
        List<DexType> guards = catchHandlers.getGuards();
        int lastGuardIndex = guards.size() - 1;
        for (int i = 0; i < guards.size(); i++) {
          assert guards.get(i) != DexItemFactory.catchAllType || i == lastGuardIndex;
        }
        // Check that all successors except maybe the last are catch successors.
        List<Integer> sortedHandlerIndices = new ArrayList<>(catchHandlers.getAllTargets());
        sortedHandlerIndices.sort(Comparator.naturalOrder());
        int firstIndex = sortedHandlerIndices.get(0);
        int lastIndex = sortedHandlerIndices.get(sortedHandlerIndices.size() - 1);
        assert firstIndex == 0;
        assert lastIndex < sortedHandlerIndices.size();
        int lastSuccessorIndex = block.getSuccessors().size() - 1;
        assert lastIndex == lastSuccessorIndex  // All successors are catch successors.
            || lastIndex == lastSuccessorIndex - 1; // All but one successors are catch successors.
        assert lastIndex == lastSuccessorIndex || !block.exit().isThrow();
      }
    }
    return true;
  }

  public boolean consistentBlockNumbering() {
    return blocks.stream()
        .collect(Collectors.groupingBy(BasicBlock::getNumber, Collectors.counting()))
        .entrySet().stream().noneMatch((bb2count) -> bb2count.getValue() > 1);
  }

  private boolean consistentBlockInstructions() {
    for (BasicBlock block : blocks) {
      for (Instruction instruction : block.getInstructions()) {
        assert instruction.getBlock() == block;
      }
    }
    return true;
  }

  private boolean validThrowingInstructions() {
    for (BasicBlock block : blocks) {
      if (block.hasCatchHandlers()) {
        boolean seenThrowing = false;
        for (Instruction instruction : block.getInstructions()) {
          if (instruction.instructionTypeCanThrow()) {
            assert !seenThrowing;
            seenThrowing = true;
            continue;
          }
          // After the throwing instruction only debug instructions an the final jump
          // instruction is allowed.
          // TODO(ager): For now allow const instructions due to the way consts are pushed
          // towards their use
          if (seenThrowing) {
            assert instruction.isDebugInstruction()
                || instruction.isJumpInstruction()
                || instruction.isConstInstruction()
                || instruction.isNewArrayFilledData();
          }
        }
      }
    }
    return true;
  }

  public InstructionIterator instructionIterator() {
    return new IRCodeInstructionsIterator(this);
  }

  void setNormalExitBlock(BasicBlock block) {
    normalExitBlock = block;
  }

  public BasicBlock getNormalExitBlock() {
    return normalExitBlock;
  }

  public ListIterator<BasicBlock> listIterator() {
    return new BasicBlockIterator(this);
  }

  public ListIterator<BasicBlock> listIterator(int index) {
    return new BasicBlockIterator(this, index);
  }

  public BasicBlock[] numberInstructions() {
    BasicBlock[] blocks = topologicallySortedBlocks();
    for (BasicBlock block : blocks) {
      for (Instruction instruction : block.getInstructions()) {
        instruction.setNumber(nextInstructionNumber);
        nextInstructionNumber += LinearScanRegisterAllocator.INSTRUCTION_NUMBER_DELTA;
      }
    }
    return blocks;
  }

  public int numberRemainingInstructions() {
    InstructionIterator it = instructionIterator();
    while (it.hasNext()) {
      Instruction i = it.next();
      if (i.getNumber() == -1) {
        i.setNumber(nextInstructionNumber);
        nextInstructionNumber += LinearScanRegisterAllocator.INSTRUCTION_NUMBER_DELTA;
      }
    }
    return nextInstructionNumber;
  }

  public int getNextInstructionNumber() {
    return nextInstructionNumber;
  }

  public List<Value> collectArguments() {
    final List<Value> arguments = new ArrayList<>();
    Iterator<Instruction> iterator = blocks.get(0).iterator();
    while (iterator.hasNext()) {
      Instruction instruction = iterator.next();
      if (instruction.isArgument()) {
        arguments.add(instruction.asArgument().outValue());
      }
    }
    assert arguments.size()
        == method.method.proto.parameters.values.length + (method.accessFlags.isStatic() ? 0 : 1);
    return arguments;
  }

  public Value createValue(MoveType moveType, Value.DebugInfo debugInfo) {
    return new Value(valueNumberGenerator.next(), moveType, debugInfo);
  }

  public Value createValue(MoveType moveType) {
    return createValue(moveType, null);
  }

  public ConstNumber createIntConstant(int value) {
    return new ConstNumber(ConstType.INT, createValue(MoveType.SINGLE), value);
  }

  public ConstNumber createTrue() {
    return new ConstNumber(ConstType.INT, createValue(MoveType.SINGLE), 1);
  }

  public ConstNumber createFalse() {
    return new ConstNumber(ConstType.INT, createValue(MoveType.SINGLE), 0);
  }

  public final int getHighestBlockNumber() {
    return blocks.stream().max(Comparator.comparingInt(BasicBlock::getNumber)).get().getNumber();
  }
}
