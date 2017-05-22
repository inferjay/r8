// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.optimize;

import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.logging.Log;
import com.android.tools.r8.optimize.InvokeSingleTargetExtractor.InvokeKind;
import java.util.IdentityHashMap;
import java.util.Map;

public class BridgeMethodAnalysis {

  private final GraphLense lense;
  private final AppInfoWithSubtyping appInfo;
  private final Map<DexMethod, DexMethod> bridgeTargetToBridgeMap = new IdentityHashMap<>();

  public BridgeMethodAnalysis(GraphLense lense, AppInfoWithSubtyping appInfo) {
    this.lense = lense;
    this.appInfo = appInfo;
  }

  public GraphLense run() {
    for (DexClass clazz : appInfo.classes()) {
      identifyBridgeMethods(clazz.virtualMethods());
      identifyBridgeMethods(clazz.directMethods());
    }
    return new BridgeLense(lense, bridgeTargetToBridgeMap);
  }

  private void identifyBridgeMethods(DexEncodedMethod[] dexEncodedMethods) {
    for (DexEncodedMethod method : dexEncodedMethods) {
      if (method.accessFlags.isBridge()) {
        InvokeSingleTargetExtractor targetExtractor = new InvokeSingleTargetExtractor();
        method.getCode().registerReachableDefinitions(targetExtractor);
        DexMethod target = targetExtractor.getTarget();
        InvokeKind kind = targetExtractor.getKind();
        if (target != null &&
            target.proto.parameters.values.length == method.method.proto.parameters.values.length) {
          assert !method.accessFlags.isPrivate() && !method.accessFlags.isConstructor();
          target = lense.lookupMethod(target, method);
          if (kind == InvokeKind.STATIC) {
            assert method.accessFlags.isStatic();
            DexEncodedMethod targetMethod = appInfo.lookupStaticTarget(target);
            if (targetMethod != null) {
              addForwarding(method, targetMethod);
            }
          } else if (kind == InvokeKind.VIRTUAL) {
            // TODO(herhut): Add support for bridges with multiple targets.
            DexEncodedMethod targetMethod = appInfo.lookupSingleVirtualTarget(target);
            if (targetMethod != null) {
              addForwarding(method, targetMethod);
            }
          }
        }
      }
    }
  }

  private void addForwarding(DexEncodedMethod method, DexEncodedMethod target) {
    // This is a single target bridge we can inline.
    if (Log.ENABLED) {
      Log.info(getClass(), "Adding bridge forwarding %s -> %s.", method.method,
          target.method);
    }
    bridgeTargetToBridgeMap.put(target.method, method.method);
    // Force the target to be inlined into the bridge.
    target.markForceInline();
  }



  private static class BridgeLense extends GraphLense {

    private final GraphLense previousLense;
    private final Map<DexMethod, DexMethod> bridgeTargetToBridgeMap;

    private BridgeLense(GraphLense previousLense,
        Map<DexMethod, DexMethod> bridgeTargetToBridgeMap) {
      this.previousLense = previousLense;
      this.bridgeTargetToBridgeMap = bridgeTargetToBridgeMap;
    }

    @Override
    public DexType lookupType(DexType type, DexEncodedMethod context) {
      return previousLense.lookupType(type, context);
    }

    @Override
    public DexMethod lookupMethod(DexMethod method, DexEncodedMethod context) {
      DexMethod previous = previousLense.lookupMethod(method, context);
      DexMethod target = bridgeTargetToBridgeMap.get(previous);
      // Do not forward calls from a bridge method to itself while the bridge method is still
      // a bridge.
      if (target == null ||
          context.accessFlags.isBridge() && target == context.method) {
        return previous;
      } else {
        return target;
      }
    }

    @Override
    public DexField lookupField(DexField field, DexEncodedMethod context) {
      return previousLense.lookupField(field, context);
    }

    @Override
    public boolean isContextFree() {
      return false;
    }
  }
}
