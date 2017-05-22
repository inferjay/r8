// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.optimize;

import com.android.tools.r8.graph.Descriptor;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.shaking.Enqueuer.AppInfoWithLiveness;
import com.google.common.collect.Sets;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

public class MemberRebindingAnalysis {
  private final AppInfoWithLiveness appInfo;
  private final GraphLense lense;
  private final GraphLense.Builder builder = GraphLense.builder();

  public MemberRebindingAnalysis(AppInfoWithLiveness appInfo, GraphLense lense) {
    assert lense.isContextFree();
    this.appInfo = appInfo;
    this.lense = lense;
  }

  private DexMethod validTargetFor(DexMethod target, DexMethod original,
      BiFunction<DexClass, DexMethod, DexEncodedMethod> lookup) {
    DexClass clazz = appInfo.definitionFor(target.getHolder());
    assert clazz != null;
    if (!clazz.isLibraryClass()) {
      return target;
    }
    DexType newHolder;
    if (clazz.isInterface()) {
      newHolder = firstLibraryClassForInterfaceTarget(target, original.getHolder(), lookup);
    } else {
      newHolder = firstLibraryClass(target.getHolder(), original.getHolder());
    }
    return appInfo.dexItemFactory.createMethod(newHolder, original.proto, original.name);
  }

  private DexField validTargetFor(DexField target, DexField original,
      BiFunction<DexClass, DexField, DexEncodedField> lookup) {
    DexClass clazz = appInfo.definitionFor(target.getHolder());
    assert clazz != null;
    if (!clazz.isLibraryClass()) {
      return target;
    }
    DexType newHolder;
    if (clazz.isInterface()) {
      newHolder = firstLibraryClassForInterfaceTarget(target, original.getHolder(), lookup);
    } else {
      newHolder = firstLibraryClass(target.getHolder(), original.getHolder());
    }
    return appInfo.dexItemFactory.createField(newHolder, original.type, original.name);
  }

  private <T> DexType firstLibraryClassForInterfaceTarget(T target, DexType current,
      BiFunction<DexClass, T, ?> lookup) {
    DexClass clazz = appInfo.definitionFor(current);
    Object potential = lookup.apply(clazz, target);
    if (potential != null) {
      // Found, return type.
      return current;
    }
    if (clazz.superType != null) {
      DexType matchingSuper = firstLibraryClassForInterfaceTarget(target, clazz.superType, lookup);
      if (matchingSuper != null) {
        // Found in supertype, return first libray class.
        return clazz.isLibraryClass() ? current : matchingSuper;
      }
    }
    for (DexType iface : clazz.interfaces.values) {
      DexType matchingIface = firstLibraryClassForInterfaceTarget(target, iface, lookup);
      if (matchingIface != null) {
        // Found in interface, return first library class.
        return clazz.isLibraryClass() ? current : matchingIface;
      }
    }
    return null;
  }

  private DexType firstLibraryClass(DexType top, DexType bottom) {
    assert appInfo.definitionFor(top).isLibraryClass();
    DexClass searchClass = appInfo.definitionFor(bottom);
    while (!searchClass.isLibraryClass()) {
      searchClass = appInfo.definitionFor(searchClass.superType);
    }
    return searchClass.type;
  }

  private boolean isNotFromLibrary(Descriptor item) {
    DexClass clazz = appInfo.definitionFor(item.getHolder());
    return clazz != null && !clazz.isLibraryClass();
  }

  private DexEncodedMethod virtualLookup(DexMethod method) {
    return appInfo.lookupVirtualDefinition(method.getHolder(), method);
  }

  private DexEncodedMethod superLookup(DexMethod method) {
    return appInfo.lookupVirtualTarget(method.getHolder(), method);
  }

  private void computeMethodRebinding(Set<DexMethod> methods,
      Function<DexMethod, DexEncodedMethod> lookupTarget,
      BiFunction<DexClass, DexMethod, DexEncodedMethod> lookupTargetOnClass) {
    for (DexMethod method : methods) {
      method = lense.lookupMethod(method, null);
      // We can safely ignore array types, as the corresponding methods are defined in a library.
      if (!method.getHolder().isClassType()) {
        continue;
      }
      DexEncodedMethod target = lookupTarget.apply(method);
      // Rebind to the lowest library class or program class.
      if (target != null && target.method != method) {
        builder.map(method, validTargetFor(target.method, method, lookupTargetOnClass));
      }
    }
  }

  private void computeFieldRebinding(Set<DexField> fields,
      BiFunction<DexType, DexField, DexEncodedField> lookup,
      BiFunction<DexClass, DexField, DexEncodedField> lookupTargetOnClass) {
    for (DexField field : fields) {
      field = lense.lookupField(field, null);
      DexEncodedField target = lookup.apply(field.getHolder(), field);
      // Rebind to the lowest library class or program class.
      if (target != null && target.field != field) {
        builder.map(field, validTargetFor(target.field, field, lookupTargetOnClass));
      }
    }
  }

  public GraphLense run() {
    computeMethodRebinding(appInfo.virtualInvokes, this::virtualLookup,
        DexClass::findVirtualTarget);
    computeMethodRebinding(appInfo.superInvokes, this::superLookup, DexClass::findVirtualTarget);
    computeMethodRebinding(appInfo.directInvokes, appInfo::lookupDirectTarget,
        DexClass::findDirectTarget);
    computeMethodRebinding(appInfo.staticInvokes, appInfo::lookupStaticTarget,
        DexClass::findDirectTarget);

    computeFieldRebinding(Sets.union(appInfo.staticFieldsRead, appInfo.staticFieldsWritten),
        appInfo::lookupStaticTarget, DexClass::findStaticTarget);
    computeFieldRebinding(Sets.union(appInfo.instanceFieldsRead, appInfo.instanceFieldsWritten),
        appInfo::lookupInstanceTarget, DexClass::findInstanceTarget);
    return builder.build(lense, appInfo.dexItemFactory);
  }
}
