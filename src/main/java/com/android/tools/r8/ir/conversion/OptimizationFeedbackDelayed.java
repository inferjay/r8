// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.conversion;

import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.ir.optimize.Inliner.Constraint;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OptimizationFeedbackDelayed implements OptimizationFeedback {

  private Reference2IntMap<DexEncodedMethod> returnsArgument = new Reference2IntOpenHashMap<>();
  private Reference2LongMap<DexEncodedMethod> returnsConstant = new Reference2LongOpenHashMap<>();
  private Set<DexEncodedMethod> neverReturnsNull = Sets.newIdentityHashSet();
  private Map<DexEncodedMethod, Constraint> inliningConstraints = Maps.newIdentityHashMap();

  @Override
  synchronized public void methodReturnsArgument(DexEncodedMethod method, int argument) {
    if (method.getOptimizationInfo().returnsArgument()) {
      assert method.getOptimizationInfo().getReturnedArgument() == argument;
      return;
    }
    assert !returnsArgument.containsKey(method);
    returnsArgument.put(method, argument);
  }

  @Override
  synchronized public void methodReturnsConstant(DexEncodedMethod method, long value) {
    if (method.getOptimizationInfo().returnsConstant()) {
      assert method.getOptimizationInfo().getReturnedConstant() == value;
      return;
    }
    assert !returnsConstant.containsKey(method);
    returnsConstant.put(method, value);
  }

  @Override
  synchronized public void methodNeverReturnsNull(DexEncodedMethod method) {
    if (method.getOptimizationInfo().neverReturnsNull()) {
      return;
    }
    assert !neverReturnsNull.contains(method);
    neverReturnsNull.add(method);
  }

  @Override
  public void markProcessed(DexEncodedMethod method, Constraint state) {
    if (state == Constraint.NEVER) {
      assert method.cannotInline();
      method.markProcessed(state);
    } else {
      inliningConstraints.put(method, state);
    }
  }

  private <T> boolean setsOverlap(Set<T> set1, Set<T> set2) {
    for (T element : set1) {
      if (set2.contains(element)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Apply the optimization feedback.
   *
   * Returns the methods from the passed in list that could be affected by applying the
   * optimization feedback.
   */
  public List<DexEncodedMethod> applyAndClear(
      List<DexEncodedMethod> processed, CallGraph.Leaves leaves) {
    returnsArgument.forEach(DexEncodedMethod::markReturnsArgument);
    returnsConstant.forEach(DexEncodedMethod::markReturnsConstant);
    neverReturnsNull.forEach(DexEncodedMethod::markNeverReturnsNull);

    // Collect all methods affected by the optimization feedback applied.
    Set<DexEncodedMethod> all = Sets.newIdentityHashSet();
    all.addAll(returnsArgument.keySet());
    all.addAll(returnsConstant.keySet());
    all.addAll(neverReturnsNull);
    inliningConstraints.forEach((method, constraint) -> {
      boolean changed = method.markProcessed(constraint);
      if (changed) {
        all.add(method);
      }
    });

    // Collect the processed methods which could be affected by the applied optimization feedback.
    List<DexEncodedMethod> result = new ArrayList<>();
    for (DexEncodedMethod method : processed) {
      Set<DexEncodedMethod> calls = leaves.getCycleBreakingCalls().get(method);
      if (setsOverlap(calls, all)) {
        result.add(method);
      }
    }

    // Clear the collected optimization feedback.
    returnsArgument.clear();
    returnsConstant.clear();
    neverReturnsNull.clear();
    inliningConstraints.clear();

    return result;
  }
}
