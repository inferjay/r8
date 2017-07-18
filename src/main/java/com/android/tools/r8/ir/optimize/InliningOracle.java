// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.optimize;

import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.InvokeDirect;
import com.android.tools.r8.ir.code.InvokeInterface;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.InvokeMethodWithReceiver;
import com.android.tools.r8.ir.code.InvokePolymorphic;
import com.android.tools.r8.ir.code.InvokeStatic;
import com.android.tools.r8.ir.code.InvokeSuper;
import com.android.tools.r8.ir.code.InvokeVirtual;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.conversion.CallGraph;
import com.android.tools.r8.ir.optimize.Inliner.InlineAction;
import com.android.tools.r8.ir.optimize.Inliner.Reason;
import com.android.tools.r8.logging.Log;

/**
 * The InliningOracle contains information needed for when inlining
 * other methods into @method.
 */
public class InliningOracle {

  final Inliner inliner;
  final DexEncodedMethod method;
  final Value receiver;
  final CallGraph callGraph;
  final private InliningInfo info;

  public InliningOracle(
      Inliner inliner,
      DexEncodedMethod method,
      Value receiver,
      CallGraph callGraph) {
    this.inliner = inliner;
    this.method = method;
    this.receiver = receiver;
    this.callGraph = callGraph;
    info = Log.ENABLED ? new InliningInfo(method) : null;
  }

  public void finish() {
    if (Log.ENABLED) {
      System.out.println(info.toString());
    }
  }

  DexEncodedMethod validateCandidate(InvokeMethod invoke) {
    DexEncodedMethod candidate = invoke.computeSingleTarget(inliner.appInfo);
    if ((candidate == null)
        || (candidate.getCode() == null)
        || inliner.appInfo.definitionFor(candidate.method.getHolder()).isLibraryClass()) {
      if (info != null) {
        info.exclude(invoke, "No inlinee");
      }
      return null;
    }
    if (method == candidate) {
      // Cannot handle recursive inlining at this point.
      // Bridge methods should never have recursive calls.
      assert !candidate.getOptimizationInfo().forceInline();
      return null;
    }

    if (candidate.accessFlags.isSynchronized()) {
      // Don't inline if target is synchronized.
      if (info != null) {
        info.exclude(invoke, "Inlinee candidate is synchronized");
      }
      return null;
    }

    if (callGraph.isBreaker(method, candidate)) {
      // Cycle breaker so abort to preserve compilation order.
      return null;
    }

    if (!inliner.hasInliningAccess(method, candidate)) {
      if (info != null) {
        info.exclude(invoke, "Inlinee candidate does not have right access flags");
      }
      return null;
    }
    return candidate;
  }

  private Reason computeInliningReason(DexEncodedMethod target) {
    if (target.getOptimizationInfo().forceInline()) {
      return Reason.FORCE;
    }
    if (callGraph.hasSingleCallSite(target)) {
      return Reason.SINGLE_CALLER;
    }
    if (isDoubleInliningTarget(target)) {
      return Reason.DUAL_CALLER;
    }
    return Reason.SIMPLE;
  }

  public InlineAction computeForInvokeWithReceiver(InvokeMethodWithReceiver invoke) {
    boolean receiverIsNeverNull = invoke.receiverIsNeverNull();
    if (!receiverIsNeverNull) {
      if (info != null) {
        info.exclude(invoke, "receiver for candidate can be null");
      }
      return null;
    }
    DexEncodedMethod target = invoke.computeSingleTarget(inliner.appInfo);
    if (target == null) {
      // Abort inlining attempt if we cannot find single target.
      if (info != null) {
        info.exclude(invoke, "could not find single target");
      }
      return null;
    }

    if (target == method) {
      // Bridge methods should never have recursive calls.
      assert !target.getOptimizationInfo().forceInline();
      return null;
    }

    if (target.getCode() == null) {
      return null;
    }

    DexClass holder = inliner.appInfo.definitionFor(target.method.getHolder());
    if (holder.isInterface()) {
      // Art978_virtual_interfaceTest correctly expects an IncompatibleClassChangeError exception at runtime.
      if (info != null) {
        info.exclude(invoke, "Do not inline target if method holder is an interface class");
      }
      return null;
    }

    if (holder.isLibraryClass()) {
      // Library functions should not be inlined.
      return null;
    }

    // Don't inline if target is synchronized.
    if (target.accessFlags.isSynchronized()) {
      if (info != null) {
        info.exclude(invoke, "target is synchronized");
      }
      return null;
    }

    Reason reason = computeInliningReason(target);
    // Determine if this should be inlined no matter how big it is.
    if (!target.isInliningCandidate(method, reason != Reason.SIMPLE)) {
      // Abort inlining attempt if the single target is not an inlining candidate.
      if (info != null) {
        info.exclude(invoke, "target is not identified for inlining");
      }
      return null;
    }

    if (callGraph.isBreaker(method, target)) {
      // Cycle breaker so abort to preserve compilation order.
      return null;
    }

    // Abort inlining attempt if method -> target access is not right.
    if (!inliner.hasInliningAccess(method, target)) {
      if (info != null) {
        info.exclude(invoke, "target does not have right access");
      }
      return null;
    }

    // Attempt to inline a candidate that is only called twice.
    if ((reason == Reason.DUAL_CALLER) && (inliner.doubleInlining(method, target) == null)) {
      if (info != null) {
        info.exclude(invoke, "target is not ready for double inlining");
      }
      return null;
    }

    if (info != null) {
      info.include(invoke.getType(), target);
    }
    return new InlineAction(target, invoke, reason);
  }

  public InlineAction computeForInvokeVirtual(InvokeVirtual invoke) {
    return computeForInvokeWithReceiver(invoke);
  }

  public InlineAction computeForInvokeInterface(InvokeInterface invoke) {
    return computeForInvokeWithReceiver(invoke);
  }

  public InlineAction computeForInvokeDirect(InvokeDirect invoke) {
    return computeForInvokeWithReceiver(invoke);
  }

  private boolean canInlineStaticInvoke(DexEncodedMethod method, DexEncodedMethod target) {
    // Only proceed with inlining a static invoke if:
    // - the holder for the target equals the holder for the method, or
    // - there is no class initializer.
    DexType targetHolder = target.method.getHolder();
    if (method.method.getHolder() == targetHolder) {
      return true;
    }
    DexClass clazz = inliner.appInfo.definitionFor(targetHolder);
    return (clazz != null) && (!clazz.hasNonTrivialClassInitializer());
  }

  private synchronized boolean isDoubleInliningTarget(DexEncodedMethod candidate) {
    // 10 is found from measuring.
    return callGraph.hasDoubleCallSite(candidate)
        && candidate.getCode().isDexCode()
        && (candidate.getCode().asDexCode().instructions.length <= 10);
  }

  public InlineAction computeForInvokeStatic(InvokeStatic invoke) {
    DexEncodedMethod candidate = validateCandidate(invoke);
    if (candidate == null) {
      return null;
    }
    Reason reason = computeInliningReason(candidate);
    // Determine if this should be inlined no matter how big it is.
    if (!candidate.isInliningCandidate(method, reason != Reason.SIMPLE)) {
      // Abort inlining attempt if the single target is not an inlining candidate.
      if (info != null) {
        info.exclude(invoke, "target is not identified for inlining");
      }
      return null;
    }

    // Abort inlining attempt if we can not guarantee class for static target has been initialized.
    if (!canInlineStaticInvoke(method, candidate)) {
      if (info != null) {
        info.exclude(invoke, "target is static but we cannot guarantee class has been initialized");
      }
      return null;
    }

    // Attempt to inline a candidate that is only called twice.
    if ((reason == Reason.DUAL_CALLER) && (inliner.doubleInlining(method, candidate) == null)) {
      if (info != null) {
        info.exclude(invoke, "target is not ready for double inlining");
      }
      return null;
    }

    if (info != null) {
      info.include(invoke.getType(), candidate);
    }
    return new InlineAction(candidate, invoke, reason);
  }

  public InlineAction computeForInvokeSuper(InvokeSuper invoke) {
    DexEncodedMethod candidate = validateCandidate(invoke);
    if (candidate == null) {
      if (info != null) {
        info.exclude(invoke, "not a valid inlining target");
      }
      return null;
    }
    if (info != null) {
      info.include(invoke.getType(), candidate);
    }
    return new InlineAction(candidate, invoke, Reason.SIMPLE);
  }

  public InlineAction computeForInvokePolymorpic(InvokePolymorphic invoke) {
    // TODO: No inlining of invoke polymorphic for now.
    if (info != null) {
      info.exclude(invoke, "inlining through invoke signature polymorpic is not supported");
    }
    return null;
  }
}
