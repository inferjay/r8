// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.naming;

import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.shaking.Enqueuer.AppInfoWithLiveness;
import com.android.tools.r8.shaking.RootSetBuilder.RootSet;
import com.android.tools.r8.utils.StringUtils;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClassNameMinifier {

  /**
   * We ban classes from these prefixes from minification. This is needed as some classes
   * in the android sdk are given a @hide annotation, which will remove them from the
   * sdk we tree-shake and minify against. Thus, the class will not be available and hence
   * we won't find out that it is a library class.
   * To save space, we by default minify classes we do not have an implementation for.
   */
  private static final Set<String> BANNED_CLASS_PREFIXES = ImmutableSet
      .of("Ljava", "Landroid", "Ldalvik");

  private final AppInfoWithLiveness appInfo;
  private final RootSet rootSet;
  private final String packagePrefix;
  private final Set<DexString> usedTypeNames = Sets.newIdentityHashSet();

  private final Map<DexType, DexString> renaming = Maps.newIdentityHashMap();
  private final Map<String, NamingState> states = new HashMap<>();
  final List<String> dictionary;

  public ClassNameMinifier(AppInfoWithLiveness appInfo, RootSet rootSet, String packagePrefix,
      List<String> dictionary) {
    this.appInfo = appInfo;
    this.rootSet = rootSet;
    this.packagePrefix = packagePrefix;
    this.dictionary = dictionary;
  }

  public Map<DexType, DexString> computeRenaming() {
    // Collect names we have to keep.
    for (DexClass clazz : appInfo.classes()) {
      if (rootSet.noObfuscation.contains(clazz)) {
        assert !renaming.containsKey(clazz.type);
        renaming.put(clazz.type, clazz.type.descriptor);
        usedTypeNames.add(clazz.type.descriptor);
      }
    }
    for (DexClass clazz : appInfo.classes()) {
      if (!renaming.containsKey(clazz.type)) {
        String packageName = getPackageNameFor(clazz);
        NamingState state = getStateFor(packageName);
        renaming.put(clazz.type, state.nextTypeName());
      }
    }
    renameTypesInProtosOf(appInfo.staticInvokes);
    renameTypesInProtosOf(appInfo.superInvokes);
    renameTypesInProtosOf(appInfo.directInvokes);
    renameTypesInProtosOf(appInfo.virtualInvokes);
    appInfo.dexItemFactory.forAllTypes(this::renameArrayTypeIfNeeded);

    return Collections.unmodifiableMap(renaming);
  }

  private String getPackageNameFor(DexClass clazz) {
    if (packagePrefix == null || rootSet.keepPackageName.contains(clazz)) {
      return clazz.type.getPackageDescriptor();
    } else {
      return packagePrefix;
    }
  }

  private NamingState getStateFor(String packageName) {
    return states.computeIfAbsent(packageName, NamingState::new);
  }

  private void renameTypesInProtosOf(Iterable<DexMethod> methods) {
    for (DexMethod method : methods) {
      renameTypeWithoutClassDefinition(method.proto.returnType);
      for (DexType type : method.proto.parameters.values) {
        renameTypeWithoutClassDefinition(type);
      }
    }
  }

  private void renameTypeWithoutClassDefinition(DexType type) {
    if (type.isArrayType()) {
      type = type.toBaseType(appInfo.dexItemFactory);
    }
    if (type.isClassType() && !renaming.containsKey(type)) {
      DexClass clazz = appInfo.definitionFor(type);
      if (clazz == null || !clazz.isLibraryClass()) {
        if (!classIsBannedFromRenaming(type)) {
          String packageName = packagePrefix == null ? type.getPackageDescriptor() : packagePrefix;
          NamingState state = getStateFor(packageName);
          renaming.put(type, state.nextTypeName());
        }
      }
    }
  }

  private boolean classIsBannedFromRenaming(DexType type) {
    String desc = type.toDescriptorString();
    int index = desc.indexOf('/');
    String prefix = desc.substring(0, index);
    return index != -1 && BANNED_CLASS_PREFIXES.contains(prefix);
  }

  private void renameArrayTypeIfNeeded(DexType type) {
    if (type.isArrayType()) {
      DexType base = type.toBaseType(appInfo.dexItemFactory);
      DexString value = renaming.get(base);
      if (value != null) {
        int dimensions = type.descriptor.numberOfLeadingSquareBrackets();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < dimensions; i++) {
          builder.append('[');
        }
        builder.append(value.toString());
        DexString descriptor = appInfo.dexItemFactory.createString(builder.toString());
        renaming.put(type, descriptor);
      }
    }
  }

  private class NamingState {

    private final char[] packagePrefix;
    private int typeCounter = 1;
    private Iterator<String> dictionaryIterator;

    NamingState(String packageName) {
      this.packagePrefix = ("L" + packageName + (packageName.isEmpty() ? "" : "/")).toCharArray();
      this.dictionaryIterator = dictionary.iterator();
    }

    public char[] getPackagePrefix() {
      return packagePrefix;
    }

    protected String nextSuggestedName() {
      StringBuilder nextName = new StringBuilder();
      if (dictionaryIterator.hasNext()) {
        nextName.append(getPackagePrefix()).append(dictionaryIterator.next()).append(';');
        return nextName.toString();
      } else {
        return StringUtils.numberToIdentifier(packagePrefix, typeCounter++, true);
      }
    }

    private DexString nextTypeName() {
      DexString candidate;
      do {
        candidate = appInfo.dexItemFactory.createString(nextSuggestedName());
      } while (usedTypeNames.contains(candidate));
      return candidate;
    }
  }
}
