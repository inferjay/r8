// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.naming;

import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.shaking.RootSetBuilder.RootSet;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.Timing;
import com.google.common.collect.ImmutableList;
import java.util.IdentityHashMap;
import java.util.Map;

class FieldNameMinifier {

  private final AppInfoWithSubtyping appInfo;
  private final RootSet rootSet;
  private final Map<DexField, DexString> renaming = new IdentityHashMap<>();
  private final ImmutableList<String> dictionary;
  private final Map<DexType, NamingState<DexType>> states = new IdentityHashMap<>();

  FieldNameMinifier(AppInfoWithSubtyping appInfo, RootSet rootSet, InternalOptions options) {
    this.appInfo = appInfo;
    this.rootSet = rootSet;
    this.dictionary = options.proguardConfiguration.getObfuscationDictionary();
  }

  Map<DexField, DexString> computeRenaming(Timing timing) {
    NamingState<DexType> rootState = NamingState.createRoot(appInfo.dexItemFactory, dictionary);
    // Reserve names in all classes first. We do this in subtyping order so we do not
    // shadow a reserved field in subclasses. While there is no concept of virtual field
    // dispatch in Java, field resolution still traverses the super type chain and external
    // code might use a subtype to reference the field.
    timing.begin("reserve-classes");
    reserveNamesInSubtypes(appInfo.dexItemFactory.objectType, rootState);
    timing.end();
    // Next, reserve field names in interfaces. These should only be static.
    timing.begin("reserve-interfaces");
    DexType.forAllInterfaces(appInfo.dexItemFactory,
        iface -> reserveNamesInSubtypes(iface, rootState));
    timing.end();
    // Now rename the rest.
    timing.begin("rename");
    renameFieldsInSubtypes(appInfo.dexItemFactory.objectType);
    DexType.forAllInterfaces(appInfo.dexItemFactory, this::renameFieldsInSubtypes);
    timing.end();
    return renaming;
  }

  private void reserveNamesInSubtypes(DexType type, NamingState<DexType> state) {
    DexClass holder = appInfo.definitionFor(type);
    if (holder == null) {
      return;
    }
    NamingState<DexType> newState = states.computeIfAbsent(type, t -> state.createChild());
    holder.forEachField(field -> reserveFieldName(field, newState, holder.isLibraryClass()));
    type.forAllExtendsSubtypes(subtype -> reserveNamesInSubtypes(subtype, newState));
  }

  private void reserveFieldName(
      DexEncodedField encodedField,
      NamingState<DexType> state,
      boolean isLibrary) {
    if (isLibrary || rootSet.noObfuscation.contains(encodedField)) {
      DexField field = encodedField.field;
      state.reserveName(field.name, field.type);
    }
  }

  private void renameFieldsInSubtypes(DexType type) {
    DexClass clazz = appInfo.definitionFor(type);
    if (clazz == null) {
      return;
    }
    NamingState<DexType> state = states.get(clazz.type);
    assert state != null;
    clazz.forEachField(field -> renameField(field, state));
    type.forAllExtendsSubtypes(this::renameFieldsInSubtypes);
  }

  private void renameField(DexEncodedField encodedField, NamingState<DexType> state) {
    DexField field = encodedField.field;
    if (!state.isReserved(field.name, field.type)) {
      renaming.put(field, state.assignNewNameFor(field.name, field.type, false));
    }
  }
}
