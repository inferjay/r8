// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.optimize;

import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.DexCode;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionIterator;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.Invoke;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.code.ValueNumberGenerator;
import com.android.tools.r8.ir.conversion.CallGraph;
import com.android.tools.r8.utils.InternalOptions;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

public class Inliner {

  private static final int INLINING_INSTRUCTION_LIMIT = 5;

  protected final AppInfoWithSubtyping appInfo;
  private final InternalOptions options;

  // State for inlining methods which are known to be called twice.
  public boolean applyDoubleInlining = false;
  public final Set<DexEncodedMethod> doubleInlineCallers = Sets.newIdentityHashSet();
  public final Set<DexEncodedMethod> doubleInlineSelectedTargets = Sets.newIdentityHashSet();
  public final Map<DexEncodedMethod, DexEncodedMethod> doubleInlineeCandidates = new HashMap<>();

  public Inliner(
      AppInfoWithSubtyping appInfo, InternalOptions options) {
    this.appInfo = appInfo;
    this.options = options;
  }

  private InliningConstraint instructionAllowedForInlining(
      DexEncodedMethod method, Instruction instruction) {
    InliningConstraint result = instruction.inliningConstraint(appInfo, method.method.holder);
    if ((result == InliningConstraint.NEVER) && instruction.isDebugInstruction()) {
      return InliningConstraint.ALWAYS;
    }
    return result;
  }

  public InliningConstraint identifySimpleMethods(IRCode code, DexEncodedMethod method) {
    DexCode dex = method.getCode().asDexCode();
    // We have generated code for a method and we want to figure out whether the method is a
    // candidate for inlining. The code is the final IR after optimizations.
    if (dex.instructions.length > INLINING_INSTRUCTION_LIMIT) {
      return InliningConstraint.NEVER;
    }
    InliningConstraint result = InliningConstraint.ALWAYS;
    ListIterator<BasicBlock> iterator = code.listIterator();
    assert iterator.hasNext();
    BasicBlock block = iterator.next();
    BasicBlock nextBlock;
    do {
      nextBlock = iterator.hasNext() ? iterator.next() : null;
      InstructionListIterator it = block.listIterator();
      while (it.hasNext()) {
        Instruction instruction = it.next();
        InliningConstraint state = instructionAllowedForInlining(method, instruction);
        if (state == InliningConstraint.NEVER) {
          return InliningConstraint.NEVER;
        }
        if (state.ordinal() < result.ordinal()) {
          result = state;
        }
      }
      block = nextBlock;
    } while (block != null);
    return result;
  }

  protected boolean hasInliningAccess(DexEncodedMethod method, DexEncodedMethod target) {
    if (target.accessFlags.isPublic()) {
      return true;
    }
    DexType methodHolder = method.method.getHolder();
    DexType targetHolder = target.method.getHolder();
    if (target.accessFlags.isPrivate()) {
      return methodHolder == targetHolder;
    }
    if (target.accessFlags.isProtected() &&
        methodHolder.isSubtypeOf(targetHolder, appInfo)) {
      return true;
    }
    return methodHolder.isSamePackage(targetHolder);
  }

  public enum InliningConstraint {
    // The ordinal values are important so please do not reorder.
    NEVER,    // Never inline this.
    PRIVATE,  // Only inline this into methods with same holder.
    PACKAGE,  // Only inline this into methods with holders from same package.
    ALWAYS,   // No restrictions for inlining this.
  }

  static public class InlineAction {
    public final DexEncodedMethod target;
    public final Invoke invoke;
    public final boolean forceInline;

    public InlineAction(
        DexEncodedMethod target, Invoke invoke, boolean forceInline) {
      this.target = target;
      this.invoke = invoke;
      this.forceInline = forceInline;
    }

    public InlineAction(DexEncodedMethod target, Invoke invoke) {
      this.target = target;
      this.invoke = invoke;
      this.forceInline = target.getOptimizationInfo().forceInline();
    }

    public IRCode buildIR(ValueNumberGenerator generator, InternalOptions options) {
      assert target.isProcessed();
      assert target.getCode().isDexCode();
      return target.buildIR(generator, options);
    }
  }

 private int numberOfInstructions(IRCode code) {
    int numOfInstructions = 0;
    for (BasicBlock block : code.blocks) {
      numOfInstructions += block.getInstructions().size();
    }
    return numOfInstructions;
  }

  private boolean legalConstructorInline(DexEncodedMethod method, IRCode code) {
    // In the Java VM Specification section "4.10.2.4. Instance Initialization Methods and
    // Newly Created Objects" it says:
    //
    // Before that method invokes another instance initialization method of myClass or its direct
    // superclass on this, the only operation the method can perform on this is assigning fields
    // declared within myClass.
    //

    // Allow inlining a constructor into a constructor, as the constructor code is expected to
    // adhere to the VM specification.
    if (method.accessFlags.isConstructor()) {
      return true;
    }

    // Don't allow inlining a constructor into a non-constructor if the first use of the
    // un-initialized object is not an argument of an invoke of <init>.
    InstructionIterator iterator = code.instructionIterator();
    Instruction instruction = iterator.next();
    // A constructor always has the un-initialized object as the first argument.
    assert instruction.isArgument();
    Value unInitializedObject = instruction.outValue();
    while (iterator.hasNext()) {
      instruction = iterator.next();
      if (instruction.inValues().contains(unInitializedObject)) {
        return instruction.isInvokeDirect();
      }
    }
    assert false : "Execution should never reach this point";
    return false;
  }

  /// Computer the receiver value for the holder method.
  private Value receiverValue(DexEncodedMethod method, IRCode code) {
    // Ignore static methods.
    if (method.accessFlags.isStatic()) {
      return null;
    }
    // Find the outValue of the first argument instruction in the first block.
    return code.collectArguments().get(0);
  }

  public void performInlining(DexEncodedMethod method, IRCode code, CallGraph callGraph) {
    int instruction_allowance = 1500;
    instruction_allowance -= numberOfInstructions(code);
    if (instruction_allowance < 0) {
      return;
    }
    Value receiver = receiverValue(method, code);
    InliningOracle oracle = new InliningOracle(this, method, receiver, callGraph);

    List<BasicBlock> blocksToRemove = new ArrayList<>();
    ListIterator<BasicBlock> blockIterator = code.listIterator();
    while (blockIterator.hasNext() && (instruction_allowance >= 0)) {
      BasicBlock block = blockIterator.next();
      if (blocksToRemove.contains(block)) {
        continue;
      }
      InstructionListIterator iterator = block.listIterator();
      while (iterator.hasNext() && (instruction_allowance >= 0)) {
        Instruction current = iterator.next();
        if (current.isInvokeMethod()) {
          InvokeMethod invoke = current.asInvokeMethod();
          InlineAction result = invoke.computeInlining(oracle);
          if (result != null) {
            IRCode inlinee = result.buildIR(code.valueNumberGenerator, options);
            if (inlinee != null) {
              // TODO(sgjesse): Get rid of this additional check by improved inlining.
              if (block.hasCatchHandlers() && inlinee.getNormalExitBlock() == null) {
                continue;
              }
              // Make sure constructor inlining is legal.
              DexEncodedMethod target = appInfo.lookup(invoke.getType(), invoke.getInvokedMethod());
              if (target.accessFlags.isConstructor() && !legalConstructorInline(method, inlinee)) {
                continue;
              }
              if (invoke.isInvokeInterface()
                  || invoke.isInvokeVirtual()
                  || invoke.isInvokeDirect()) {
                // If the invoke has a receiver but the declared method holder is different
                // from the computed target holder, inlining is cancelled due to verification
                // issues.
                if (result.target.method.getHolder() != target.method.getHolder()) {
                  continue;
                }
              }
              // Inline the inlinee code in place of the invoke instruction
              // Back up before the invoke instruction.
              iterator.previous();
              instruction_allowance -= numberOfInstructions(inlinee);
              if (instruction_allowance >= 0 || result.forceInline) {
                iterator.inlineInvoke(code, inlinee, blockIterator, blocksToRemove);
              }
              // If we inlined the invoke from a bridge method, it is no longer a bridge method.
              if (method.accessFlags.isBridge()) {
                method.accessFlags.unsetSynthetic();
                method.accessFlags.unsetBridge();
              }
            }
          }
        }
      }
    }
    oracle.finish();
    code.removeBlocks(blocksToRemove);
    assert code.isConsistentSSA();
  }
}
