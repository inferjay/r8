// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.smali;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Phi;
import com.android.tools.r8.ir.code.Return;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.utils.InternalOptions;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

/**
 * Regression test to ensure that we do not ignore the exceptional / on-throw value of a
 * throwing instruction in the special case where the exceptional edge and the normal edge target
 * the same block.
 */
public class CatchSuccessorFallthroughTest extends SmaliTestBase {

  @Test
  public void catchSuccessorFallthroughTest() {

    SmaliBuilder builder = new SmaliBuilder("Test");

    builder.addStaticMethod("int", "maybeThrow", Arrays.asList("int"), 0,
        "  if-eqz v0, :throw",
        "  const v0, 42",
        "  return v0",
        ":throw",
        "  div-int/2addr v0, v0",
        "  return v0");

    MethodSignature methodSig = builder.addStaticMethod(
        "int", "method", Collections.singletonList("int"), 0,
        ":try_start",
        "  invoke-static {v0}, LTest;->maybeThrow(I)I",
        "  move-result v0",
        ":try_end",
        "  .catch Ljava/lang/Throwable; {:try_start .. :try_end} :return",
        ":return",
        "  return v0"
    );

    builder.addStaticMethod(
        "void", "main", Arrays.asList("java.lang.String[]"), 2,
        "  sget-object v1, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "  const v0, 1",
        "  invoke-static {v0}, LTest;->method(I)I",
        "  move-result v0",
        "  invoke-virtual {v1, v0}, Ljava/io/PrintStream;->println(I)V",
        "  const v0, 0",
        "  invoke-static {v0}, LTest;->method(I)I",
        "  move-result v0",
        "  invoke-virtual {v1, v0}, Ljava/io/PrintStream;->println(I)V",
        "  return-void");

    InternalOptions options = new InternalOptions();
    DexApplication originalApplication = buildApplication(builder, options);


    DexEncodedMethod method = getMethod(originalApplication, methodSig);
    // Get the IR pre-optimization.
    IRCode code = method.buildIR(options);

    // Find the exit block and assert that the value is a phi merging the exceptional edge
    // with the normal edge.
    boolean hasReturn = false;
    for (BasicBlock block : code.blocks) {
      if (block.exit() instanceof Return) {
        // Find the return block.
        // Check it has one phi with two operands / two predecessors.
        Return ret = block.exit().asReturn();
        assertEquals(2, block.getPredecessors().size());
        assertEquals(1, block.getPhis().size());
        assertEquals(ret.returnValue(), block.getPhis().get(0));
        // Next we find and check that the phi values come from the expected predecessor.
        boolean hasNormalPredecessor = false;
        boolean hasExceptionalPredecessor = false;
        Phi phi = block.getPhis().get(0);
        for (Value operand : phi.getOperands()) {
          BasicBlock defBlock = operand.definition.getBlock();
          if (defBlock.canThrow()) {
            // Found the invoke instruction / block.
            assertEquals(2, defBlock.getSuccessors().size());
            assertTrue(
                defBlock.getInstructions().get(defBlock.getInstructions().size() - 2).isInvoke());
            for (BasicBlock returnPredecessor : block.getPredecessors()) {
              if (defBlock.hasCatchSuccessor(returnPredecessor)) {
                hasExceptionalPredecessor = true;
              } else if (defBlock == returnPredecessor) {
                // Normal flow goes to return.
                hasNormalPredecessor = true;
              } else if (defBlock.getSuccessors().contains(returnPredecessor)) {
                // Normal flow goes to return after an edge split.
                assertTrue(returnPredecessor.isTrivialGoto());
                hasNormalPredecessor = true;
              }
            }
          }
        }
        assertTrue(hasNormalPredecessor);
        assertTrue(hasExceptionalPredecessor);
        hasReturn = true;
      }
    }
    assertTrue(hasReturn);
  }
}
