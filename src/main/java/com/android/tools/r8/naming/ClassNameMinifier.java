// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.naming;

import com.android.tools.r8.graph.DexAnnotation;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.DexValue;
import com.android.tools.r8.graph.DexValue.DexValueType;
import com.android.tools.r8.shaking.Enqueuer.AppInfoWithLiveness;
import com.android.tools.r8.shaking.RootSetBuilder.RootSet;
import com.android.tools.r8.utils.DescriptorUtils;
import com.android.tools.r8.utils.StringUtils;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClassNameMinifier {

  private final AppInfoWithLiveness appInfo;
  private final RootSet rootSet;
  private final String packagePrefix;
  private final Set<DexString> usedTypeNames = Sets.newIdentityHashSet();

  private final Map<DexType, DexString> renaming = Maps.newIdentityHashMap();
  private final Map<String, NamingState> states = new HashMap<>();
  private final List<String> dictionary;
  private final boolean keepInnerClassStructure;

  public ClassNameMinifier(AppInfoWithLiveness appInfo, RootSet rootSet, String packagePrefix,
      List<String> dictionary, boolean keepInnerClassStructure) {
    this.appInfo = appInfo;
    this.rootSet = rootSet;
    this.packagePrefix = packagePrefix;
    this.dictionary = dictionary;
    this.keepInnerClassStructure = keepInnerClassStructure;
  }

  public Map<DexType, DexString> computeRenaming() {
    Iterable<DexProgramClass> classes = appInfo.classes();
    // Collect names we have to keep.
    for (DexClass clazz : appInfo.classes()) {
      if (rootSet.noObfuscation.contains(clazz)) {
        assert !renaming.containsKey(clazz.type);
        registerClassAsUsed(clazz.type);
      }
    }
    for (DexClass clazz : appInfo.classes()) {
      if (!renaming.containsKey(clazz.type)) {
        DexString renamed = computeName(clazz);
        renaming.put(clazz.type, renamed);
      }
    }
    appInfo.dexItemFactory.forAllTypes(this::renameArrayTypeIfNeeded);

    return Collections.unmodifiableMap(renaming);
  }

  /**
   * Registers the given type as used.
   * <p>
   * When {@link #keepInnerClassStructure} is true, keeping the name of an inner class will
   * automatically also keep the name of the outer class, as otherwise the structure would be
   * invalidated.
   */
  private void registerClassAsUsed(DexType type) {
    renaming.put(type, type.descriptor);
    usedTypeNames.add(type.descriptor);
    if (keepInnerClassStructure) {
      DexType outerClass = getOutClassForType(type);
      if (outerClass != null) {
        if (!renaming.containsKey(outerClass)) {
          // The outer class was not previously kept. We have to do this now.
          registerClassAsUsed(outerClass);
        }
      }
    }
  }

  private DexType getOutClassForType(DexType type) {
    DexClass clazz = appInfo.definitionFor(type);
    if (clazz == null) {
      return null;
    }
    DexAnnotation annotation =
        clazz.annotations.getFirstMatching(appInfo.dexItemFactory.annotationEnclosingClass);
    if (annotation != null) {
      assert annotation.annotation.elements.length == 1;
      DexValue value = annotation.annotation.elements[0].value;
      return ((DexValueType) value).value;
    }
    // We do not need to preserve the names for local or anonymous classes, as they do not result
    // in a member type declaration and hence cannot be referenced as nested classes in
    // method signatures.
    // See https://docs.oracle.com/javase/specs/jls/se7/html/jls-8.html#jls-8.5.
    return null;
  }

  private DexString computeName(DexClass clazz) {
    NamingState state = null;
    if (keepInnerClassStructure) {
      // When keeping the nesting structure of inner classes, we have to insert the name
      // of the outer class for the $ prefix.
      DexType outerClass = getOutClassForType(clazz.type);
      if (outerClass != null) {
        state = getStateForOuterClass(outerClass);
      }
    }
    if (state == null) {
      String packageName = getPackageNameFor(clazz);
      state = getStateFor(packageName);
    }
    return state.nextTypeName();
  }

  private String getPackageNameFor(DexClass clazz) {
    if ((packagePrefix == null) || rootSet.keepPackageName.contains(clazz)) {
      return clazz.type.getPackageDescriptor();
    } else {
      return packagePrefix;
    }
  }

  private NamingState getStateFor(String packageName) {
    return states.computeIfAbsent(packageName, NamingState::new);
  }

  private NamingState getStateForOuterClass(DexType outer) {
    String prefix = DescriptorUtils
        .getClassBinaryNameFromDescriptor(outer.toDescriptorString());
    return states.computeIfAbsent(prefix, k -> {
      // Create a naming state with this classes renaming as prefix.
      DexString renamed = renaming.get(outer);
      if (renamed == null) {
        // The outer class has not been renamed yet, so rename the outer class first.
        DexClass outerClass = appInfo.definitionFor(outer);
        if (outerClass == null) {
          renamed = outer.descriptor;
        } else {
          renamed = computeName(outerClass);
          renaming.put(outer, renamed);
        }
      }
      String binaryName = DescriptorUtils.getClassBinaryNameFromDescriptor(renamed.toString());
      return new NamingState(binaryName, "$");
    });
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
    private final String separator;
    private int typeCounter = 1;
    private Iterator<String> dictionaryIterator;

    NamingState(String packageName) {
      this(packageName, "/");
    }

    NamingState(String packageName, String separator) {
      this.packagePrefix = ("L" + packageName + (packageName.isEmpty() ? "" : separator))
          .toCharArray();
      this.separator = separator;
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
