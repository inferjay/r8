// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.optimize;

import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.Phi;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.utils.InternalOptions;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class DeadCodeRemover {

  public static void removeDeadCode(
      IRCode code, CodeRewriter codeRewriter, InternalOptions options) {
    Queue<BasicBlock> worklist = new LinkedList<>();
    worklist.addAll(code.blocks);
    for (BasicBlock block = worklist.poll(); block != null; block = worklist.poll()) {
      removeDeadInstructions(worklist, block, options);
      removeDeadPhis(worklist, block, options);
    }
    codeRewriter.rewriteMoveResult(code);
  }

  private static void removeDeadPhis(
      Queue<BasicBlock> worklist, BasicBlock block, InternalOptions options) {
    List<Phi> toRemove = new ArrayList<>();
    for (Phi phi : block.getPhis()) {
      if (phi.isDead(options)) {
        toRemove.add(phi);
        for (Value operand : phi.getOperands()) {
          operand.removePhiUser(phi);
          if (operand.isPhi()) {
            worklist.add(operand.asPhi().getBlock());
          } else {
            worklist.add(operand.definition.getBlock());
          }
        }
      }
    }
    if (!toRemove.isEmpty()) {
      List<Phi> newPhis = new ArrayList<>(block.getPhis().size() - toRemove.size());
      int toRemoveIndex = 0;
      List<Phi> phis = block.getPhis();
      int i = 0;
      for (; i < phis.size() && toRemoveIndex < toRemove.size(); i++) {
        Phi phi = phis.get(i);
        if (phi == toRemove.get(toRemoveIndex)) {
          toRemoveIndex++;
        } else {
          newPhis.add(phi);
        }
      }
      newPhis.addAll(phis.subList(i, phis.size()));
      block.setPhis(newPhis);
    }
  }

  private static void removeDeadInstructions(
      Queue<BasicBlock> worklist, BasicBlock block, InternalOptions options) {
    InstructionListIterator iterator = block.listIterator(block.getInstructions().size());
    while (iterator.hasPrevious()) {
      Instruction current = iterator.previous();
      // Remove unused invoke results.
      if (current.isInvoke()
          && current.outValue() != null
          && current.outValue().numberOfAllUsers() == 0) {
        current.setOutValue(null);
      }
      // Never remove instructions that can have side effects.
      if (!current.canBeDeadCode(options)) {
        continue;
      }
      Value outValue = current.outValue();
      // Instructions with no out value cannot be dead code by the current definition
      // (unused out value). They typically side-effect input values or deals with control-flow.
      assert outValue != null;
      if (!outValue.isDead(options)) {
        continue;
      }
      for (Value inValue : current.inValues()) {
        if (inValue.isPhi()) {
          worklist.add(inValue.asPhi().getBlock());
        } else {
          worklist.add(inValue.definition.getBlock());
        }
      }
      Value previousLocalValue = current.getPreviousLocalValue();
      if (previousLocalValue != null) {
        if (previousLocalValue.isPhi()) {
          worklist.add(previousLocalValue.asPhi().getBlock());
        } else {
          worklist.add(previousLocalValue.definition.getBlock());
        }
      }
      // All users will be removed for this instruction. Eagerly clear them so further inspection
      // of this instruction during dead code elimination will terminate here.
      outValue.clearUsers();
      iterator.remove();
    }
  }
}
