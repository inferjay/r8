// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.naming;

import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.graph.AppInfo;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItem;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.optimize.MemberRebindingAnalysis;
import com.android.tools.r8.shaking.Enqueuer.AppInfoWithLiveness;
import com.android.tools.r8.shaking.RootSetBuilder.RootSet;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.Timing;
import com.google.common.collect.Iterables;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Minifier {

  private final AppInfoWithLiveness appInfo;
  private final RootSet rootSet;
  private final InternalOptions options;

  public Minifier(AppInfoWithLiveness appInfo, RootSet rootSet, InternalOptions options) {
    this.appInfo = appInfo;
    this.rootSet = rootSet;
    this.options = options;
  }

  public NamingLens run(Timing timing) {
    assert !options.skipMinification;
    if (!options.allowAccessModification) {
      throw new CompilationError("Minification requires allowaccessmodification.");
    }
    timing.begin("MinifyClasses");
    Map<DexType, DexString> classRenaming =
        new ClassNameMinifier(
            appInfo, rootSet, options.packagePrefix, options.classObfuscationDictionary,
            options.attributeRemoval.signature)
            .computeRenaming();
    timing.end();
    timing.begin("MinifyMethods");
    Map<DexMethod, DexString> methodRenaming =
        new MethodNameMinifier(appInfo, rootSet, options.obfuscationDictionary)
            .computeRenaming(timing);
    timing.end();
    timing.begin("MinifyFields");
    Map<DexField, DexString> fieldRenaming =
        new FieldNameMinifier(appInfo, rootSet, options.obfuscationDictionary).computeRenaming();
    timing.end();
    return new MinifiedRenaming(classRenaming, methodRenaming, fieldRenaming, appInfo);
  }

  private static class MinifiedRenaming extends NamingLens {

    private final AppInfo appInfo;
    private final Map<DexItem, DexString> renaming = new IdentityHashMap<>();

    private MinifiedRenaming(Map<DexType, DexString> classRenaming,
        Map<DexMethod, DexString> methodRenaming, Map<DexField, DexString> fieldRenaming,
        AppInfo appInfo) {
      this.appInfo = appInfo;
      renaming.putAll(classRenaming);
      renaming.putAll(methodRenaming);
      renaming.putAll(fieldRenaming);
    }

    @Override
    public DexString lookupDescriptor(DexType type) {
      return renaming.getOrDefault(type, type.descriptor);
    }

    @Override
    public DexString lookupName(DexMethod method) {
      return renaming.getOrDefault(method, method.name);
    }

    @Override
    public DexString lookupName(DexField field) {
      return renaming.getOrDefault(field, field.name);
    }

    @Override
    void forAllRenamedTypes(Consumer<DexType> consumer) {
      Iterables.filter(renaming.keySet(), DexType.class).forEach(consumer);
    }

    /**
     * Checks whether the target is precise enough to be translated,
     * <p>
     * We only track the renaming of actual definitions, Thus, if we encounter a method id that
     * does not directly point at a definition, we won't find the actual renaming. To avoid
     * dispatching on every lookup, we assume that the tree has been fully dispatched by
     * {@link MemberRebindingAnalysis}.
     * <p>
     * Library methods are excluded from this check, as those are never renamed.
     */
    @Override
    public boolean checkTargetCanBeTranslated(DexMethod item) {
      if (item.holder.isArrayType()) {
        // Array methods are never renamed, so do not bother to check.
        return true;
      }
      DexClass holder = appInfo.definitionFor(item.holder);
      if (holder == null || holder.isLibraryClass()) {
        return true;
      }
      // We don't know which invoke type this method is used for, so checks that it has been
      // rebound either way.
      DexEncodedMethod staticTarget = appInfo.lookupStaticTarget(item);
      DexEncodedMethod directTarget = appInfo.lookupDirectTarget(item);
      DexEncodedMethod virtualTarget = appInfo.lookupVirtualTarget(item.holder, item);
      DexClass staticTargetHolder =
          staticTarget != null ? appInfo.definitionFor(staticTarget.method.getHolder()) : null;
      DexClass directTargetHolder =
          directTarget != null ? appInfo.definitionFor(directTarget.method.getHolder()) : null;
      DexClass virtualTargetHolder =
          virtualTarget != null ? appInfo.definitionFor(virtualTarget.method.getHolder()) : null;
      return directTarget == null && staticTarget == null && virtualTarget == null
          || virtualTarget != null && virtualTarget.method == item
          || directTarget != null && directTarget.method == item
          || staticTarget != null && staticTarget.method == item
          || directTargetHolder != null && directTargetHolder.isLibraryClass()
          || virtualTargetHolder != null && virtualTargetHolder.isLibraryClass()
          || staticTargetHolder != null && staticTargetHolder.isLibraryClass();
    }
  }
}
