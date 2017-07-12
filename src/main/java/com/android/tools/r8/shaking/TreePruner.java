// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.KeyedDexItem;
import com.android.tools.r8.graph.PresortedComparable;
import com.android.tools.r8.logging.Log;
import com.android.tools.r8.shaking.Enqueuer.AppInfoWithLiveness;
import com.android.tools.r8.utils.InternalOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TreePruner {

  private DexApplication application;
  private final AppInfoWithLiveness appInfo;
  private final InternalOptions options;

  public TreePruner(
      DexApplication application, AppInfoWithLiveness appInfo, InternalOptions options) {
    this.application = application;
    this.appInfo = appInfo;
    this.options = options;
  }

  public DexApplication run() {
    application.timing.begin("Pruning application...");
    if (options.debugKeepRules && !options.skipMinification) {
      System.out.println(
          "NOTE: Debugging keep rules on a minified build might yield broken builds, as\n" +
              "      minifcation also depends on the used keep rules. We recommend using\n" +
              "      --skip-minification.");
    }
    DexApplication result;
    try {
      result = removeUnused(application).build();
    } finally {
      application.timing.end();
    }
    return result;
  }

  private DexApplication.Builder removeUnused(DexApplication application) {
    return new DexApplication.Builder(application)
        .replaceProgramClasses(getNewProgramClasses(application.classes()));
  }

  private List<DexProgramClass> getNewProgramClasses(List<DexProgramClass> classes) {
    List<DexProgramClass> newClasses = new ArrayList<>();
    for (DexProgramClass clazz : classes) {
      if (!appInfo.liveTypes.contains(clazz.type)) {
        // The class is completely unused and we can remove it.
        if (Log.ENABLED) {
          Log.debug(getClass(), "Removing class: " + clazz);
        }
      } else {
        newClasses.add(clazz);
        if (!appInfo.instantiatedTypes.contains(clazz.type) &&
            (!options.debugKeepRules || !hasDefaultConstructor(clazz))) {
          // The class is only needed as a type but never instantiated. Make it abstract to reflect
          // this.
          if (clazz.accessFlags.isFinal()) {
            // We cannot mark this class abstract, as it is final (not supported on Android).
            // However, this might extend an abstract class and we might have removed the
            // corresponding methods in this class. This might happen if we only keep this
            // class around for its constants.
            // For now, we remove the final flag to still be able to mark it abstract.
            clazz.accessFlags.unsetFinal();
          }
          clazz.accessFlags.setAbstract();
        }
        // The class is used and must be kept. Remove the unused fields and methods from
        // the class.
        clazz.directMethods = reachableMethods(clazz.directMethods(), clazz);
        clazz.virtualMethods = reachableMethods(clazz.virtualMethods(), clazz);
        clazz.instanceFields = reachableFields(clazz.instanceFields());
        clazz.staticFields = reachableFields(clazz.staticFields());
      }
    }
    return newClasses;
  }

  private boolean hasDefaultConstructor(DexProgramClass clazz) {
    for (DexEncodedMethod method : clazz.directMethods()) {
      if (isDefaultConstructor(method)) {
        return true;
      }
    }
    return false;
  }

  private <S extends PresortedComparable<S>, T extends KeyedDexItem<S>> int firstUnreachableIndex(
      T[] items, Set<S> live) {
    for (int i = 0; i < items.length; i++) {
      if (!live.contains(items[i].getKey())) {
        return i;
      }
    }
    return -1;
  }

  private boolean isDefaultConstructor(DexEncodedMethod method) {
    return method.accessFlags.isConstructor() && !method.accessFlags.isStatic()
        && method.method.proto.parameters.isEmpty();
  }

  private DexEncodedMethod[] reachableMethods(DexEncodedMethod[] methods, DexClass clazz) {
    int firstUnreachable = firstUnreachableIndex(methods, appInfo.liveMethods);
    // Return the original array if all methods are used.
    if (firstUnreachable == -1) {
      return methods;
    }
    ArrayList<DexEncodedMethod> reachableMethods = new ArrayList<>(methods.length);
    for (int i = 0; i < firstUnreachable; i++) {
      reachableMethods.add(methods[i]);
    }
    for (int i = firstUnreachable; i < methods.length; i++) {
      if (appInfo.liveMethods.contains(methods[i].getKey())) {
        reachableMethods.add(methods[i]);
      } else if (options.debugKeepRules && isDefaultConstructor(methods[i])) {
        // Keep the method but rewrite its body, if it has one.
        reachableMethods.add(methods[i].accessFlags.isAbstract()
            ? methods[i]
            : methods[i].toMethodThatLogsError(application.dexItemFactory));
      } else if (appInfo.targetedMethods.contains(methods[i].getKey())) {
        if (Log.ENABLED) {
          Log.debug(getClass(), "Making method %s abstract.", methods[i].method);
        }
        DexEncodedMethod method = methods[i];
        // Final classes cannot be abstract, so we have to keep the method in that case.
        // Also some other kinds of methods cannot be abstract, so keep them around.
        boolean allowAbstract = clazz.accessFlags.isAbstract()
            && !method.accessFlags.isFinal()
            && !method.accessFlags.isNative()
            && !method.accessFlags.isStrict()
            && !method.accessFlags.isSynchronized();
        // By construction, private and static methods cannot be reachable but non-live.
        assert !method.accessFlags.isPrivate() && !method.accessFlags.isStatic();
        reachableMethods.add(allowAbstract
            ? methods[i].toAbstractMethod()
            : methods[i].toEmptyThrowingMethod());
      } else if (Log.ENABLED) {
        Log.debug(getClass(), "Removing method %s.", methods[i].method);
      }
    }
    return reachableMethods.toArray(new DexEncodedMethod[reachableMethods.size()]);
  }

  private DexEncodedField[] reachableFields(DexEncodedField[] fields) {
    int firstUnreachable = firstUnreachableIndex(fields, appInfo.liveFields);
    if (firstUnreachable == -1) {
      return fields;
    }
    if (Log.ENABLED) {
      Log.debug(getClass(), "Removing field: " + fields[firstUnreachable]);
    }
    ArrayList<DexEncodedField> reachableFields = new ArrayList<>(fields.length);
    for (int i = 0; i < firstUnreachable; i++) {
      reachableFields.add(fields[i]);
    }
    for (int i = firstUnreachable + 1; i < fields.length; i++) {
      if (appInfo.liveFields.contains(fields[i].getKey())) {
        reachableFields.add(fields[i]);
      } else if (Log.ENABLED) {
        Log.debug(getClass(), "Removing field: " + fields[i]);
      }
    }
    return reachableFields.toArray(new DexEncodedField[reachableFields.size()]);
  }
}
