// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.optimize;

import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.DexCode;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.DominatorTree;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionIterator;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.Invoke;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.code.ValueNumberGenerator;
import com.android.tools.r8.ir.conversion.CallGraph;
import com.android.tools.r8.ir.conversion.IRConverter;
import com.android.tools.r8.ir.conversion.LensCodeRewriter;
import com.android.tools.r8.ir.conversion.OptimizationFeedback;
import com.android.tools.r8.logging.Log;
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
  private final GraphLense graphLense;
  private final InternalOptions options;

  // State for inlining methods which are known to be called twice.
  private boolean applyDoubleInlining = false;
  private final Set<DexEncodedMethod> doubleInlineCallers = Sets.newIdentityHashSet();
  private final Set<DexEncodedMethod> doubleInlineSelectedTargets = Sets.newIdentityHashSet();
  private final Map<DexEncodedMethod, DexEncodedMethod> doubleInlineeCandidates = new HashMap<>();

  public Inliner(AppInfoWithSubtyping appInfo, GraphLense graphLense, InternalOptions options) {
    this.appInfo = appInfo;
    this.graphLense = graphLense;
    this.options = options;
  }

  private Constraint instructionAllowedForInlining(
      DexEncodedMethod method, Instruction instruction) {
    Constraint result = instruction.inliningConstraint(appInfo, method.method.holder);
    if ((result == Constraint.NEVER) && instruction.isDebugInstruction()) {
      return Constraint.ALWAYS;
    }
    return result;
  }

  public Constraint identifySimpleMethods(IRCode code, DexEncodedMethod method) {
    DexCode dex = method.getCode().asDexCode();
    // We have generated code for a method and we want to figure out whether the method is a
    // candidate for inlining. The code is the final IR after optimizations.
    if (dex.instructions.length > INLINING_INSTRUCTION_LIMIT) {
      return Constraint.NEVER;
    }
    Constraint result = Constraint.ALWAYS;
    ListIterator<BasicBlock> iterator = code.listIterator();
    assert iterator.hasNext();
    BasicBlock block = iterator.next();
    BasicBlock nextBlock;
    do {
      nextBlock = iterator.hasNext() ? iterator.next() : null;
      InstructionListIterator it = block.listIterator();
      while (it.hasNext()) {
        Instruction instruction = it.next();
        Constraint state = instructionAllowedForInlining(method, instruction);
        if (state == Constraint.NEVER) {
          return Constraint.NEVER;
        }
        if (state.ordinal() < result.ordinal()) {
          result = state;
        }
      }
      block = nextBlock;
    } while (block != null);
    return result;
  }

  boolean hasInliningAccess(DexEncodedMethod method, DexEncodedMethod target) {
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

  synchronized DexEncodedMethod doubleInlining(DexEncodedMethod method,
      DexEncodedMethod target) {
    if (!applyDoubleInlining) {
      if (doubleInlineeCandidates.containsKey(target)) {
        // Both calls can be inlined.
        doubleInlineCallers.add(doubleInlineeCandidates.get(target));
        doubleInlineCallers.add(method);
        doubleInlineSelectedTargets.add(target);
      } else {
        // First call can be inlined.
        doubleInlineeCandidates.put(target, method);
      }
      // Just preparing for double inlining.
      return null;
    } else {
      // Don't perform the actual inlining if this was not selected.
      if (!doubleInlineSelectedTargets.contains(target)) {
        return null;
      }
    }
    return target;
  }

  public synchronized void processDoubleInlineCallers(IRConverter converter,
      OptimizationFeedback feedback) {
    if (doubleInlineCallers.size() > 0) {
      applyDoubleInlining = true;
      for (DexEncodedMethod method : doubleInlineCallers) {
        converter.processMethod(method, feedback, Outliner::noProcessing);
        assert method.isProcessed();
      }
    }
  }

  public enum Constraint {
    // The ordinal values are important so please do not reorder.
    NEVER,    // Never inline this.
    PRIVATE,  // Only inline this into methods with same holder.
    PACKAGE,  // Only inline this into methods with holders from same package.
    ALWAYS,   // No restrictions for inlining this.
  }

  public enum Reason {
    FORCE,         // Inlinee is marked for forced inlining (bridge method or renamed constructor).
    SINGLE_CALLER, // Inlinee has precisely one caller.
    DUAL_CALLER,   // Inlinee has precisely two callers.
    SIMPLE,        // Inlinee has simple code suitable for inlining.
  }

  static public class InlineAction {

    public final DexEncodedMethod target;
    public final Invoke invoke;
    public final Reason reason;

    public InlineAction(DexEncodedMethod target, Invoke invoke, Reason reason) {
      this.target = target;
      this.invoke = invoke;
      this.reason = reason;
    }

    public boolean forceInline() {
      return reason != Reason.SIMPLE;
    }

    public IRCode buildIR(ValueNumberGenerator generator, AppInfoWithSubtyping appInfo,
        GraphLense graphLense, InternalOptions options) {
      if (target.isProcessed()) {
        assert target.getCode().isDexCode();
        return target.buildIR(generator, options);
      } else {
        // Build the IR for a yet not processed method, and perform minimal IR processing.
        IRCode code;
        if (target.getCode().isJarCode()) {
          code = target.getCode().asJarCode().buildIR(target, generator, options);
        } else {
          code = target.getCode().asDexCode().buildIR(target, generator, options);
        }
        new LensCodeRewriter(graphLense, appInfo).rewrite(code, target);
        return code;
      }
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
        return instruction.isInvokeDirect()
            && appInfo.dexItemFactory
            .isConstructor(instruction.asInvokeDirect().getInvokedMethod());
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
    computeReceiverMustBeNonNull(code);
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
            DexEncodedMethod target = appInfo.lookup(invoke.getType(), invoke.getInvokedMethod());
            if (target == null) {
              // The declared target cannot be found so skip inlining.
              continue;
            }
            boolean forceInline = result.reason == Reason.FORCE;
            if (!target.isProcessed() && !forceInline) {
              // Do not inline code that was not processed unless we have to force inline.
              continue;
            }
            IRCode inlinee = result
                .buildIR(code.valueNumberGenerator, appInfo, graphLense, options);
            if (inlinee != null) {
              // TODO(sgjesse): Get rid of this additional check by improved inlining.
              if (block.hasCatchHandlers() && inlinee.getNormalExitBlock() == null) {
                continue;
              }
              if (callGraph.isBreaker(method, target)) {
                // Make sure we don't inline a call graph breaker.
                continue;
              }
              // If this code did not go through the full pipeline, apply inlining to make sure
              // that force inline targets get processed.
              if (!target.isProcessed()) {
                assert forceInline;
                if (Log.ENABLED) {
                  Log.verbose(getClass(), "Forcing extra inline on " + target.toSourceString());
                }
                performInlining(target, inlinee, callGraph);
              }
              // Make sure constructor inlining is legal.
              if (target.accessFlags.isConstructor() && !legalConstructorInline(method, inlinee)) {
                continue;
              }
              // Ensure the container is compatible with the target.
             if (!forceInline
                 && !result.target.isPublicInlining()
                 && (method.method.getHolder() != result.target.method.getHolder())) {
                continue;
              }
              DexType downcast = null;
              if (invoke.isInvokeMethodWithReceiver()) {
                // If the invoke has a receiver but the declared method holder is different
                // from the computed target holder, inlining requires a downcast of the receiver.
                if (result.target.method.getHolder() != target.method.getHolder()) {
                  downcast = result.target.method.getHolder();
                }
              }
              // Inline the inlinee code in place of the invoke instruction
              // Back up before the invoke instruction.
              iterator.previous();
              instruction_allowance -= numberOfInstructions(inlinee);
              if (instruction_allowance >= 0 || result.forceInline()) {
                iterator.inlineInvoke(code, inlinee, blockIterator, blocksToRemove, downcast);
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

  // Determine whether the receiver of an invocation is guaranteed to be non-null based on
  // the dominator tree. If a method call is dominated by another method call with the same
  // receiver, the receiver most be non-null when we reach the dominated call.
  //
  // We bail out for exception handling. If an invoke is covered by a try block we cannot use
  // dominance to determine that the receiver is non-null at a dominated call:
  //
  // Object o;
  // try {
  //   o.m();
  // } catch (NullPointerException e) {
  //   o.f();  // Dominated by other call with receiver o, but o is null.
  // }
  //
  private void computeReceiverMustBeNonNull(IRCode code) {
    DominatorTree dominatorTree = new DominatorTree(code);
    InstructionIterator it = code.instructionIterator();
    while (it.hasNext()) {
      Instruction instruction = it.next();
      if (instruction.isInvokeMethodWithReceiver()) {
        Value receiverValue = instruction.inValues().get(0);
        for (Instruction user : receiverValue.uniqueUsers()) {
          if (user.isInvokeMethodWithReceiver() &&
              user.inValues().get(0) == receiverValue &&
              !user.getBlock().hasCatchHandlers() &&
              dominatorTree.strictlyDominatedBy(instruction.getBlock(), user.getBlock())) {
            instruction.asInvokeMethodWithReceiver().setIsDominatedByCallWithSameReceiver();
            break;
          }
        }
      }
    }
  }
}
