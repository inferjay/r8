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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class FieldNameMinifier {

  private final AppInfoWithSubtyping appInfo;
  private final RootSet rootSet;
  private final Map<DexField, DexString> renaming = new IdentityHashMap<>();
  private final List<String> dictionary;
  private final Map<DexType, NamingState<DexType>> states = new IdentityHashMap<>();

  public FieldNameMinifier(AppInfoWithSubtyping appInfo, RootSet rootSet, List<String> dictionary) {
    this.appInfo = appInfo;
    this.rootSet = rootSet;
    this.dictionary = dictionary;
  }

  Map<DexField, DexString> computeRenaming() {
    NamingState<DexType> rootState = NamingState.createRoot(appInfo.dexItemFactory, dictionary);
    // Reserve names in all classes first. We do this in subtyping order so we do not
    // shadow a reserved field in subclasses. While there is no concept of virtual field
    // dispatch in Java, field resolution still traverses the super type chain and external
    // code might use a subtype to reference the field.
    reserveNamesInSubtypes(appInfo.dexItemFactory.objectType, rootState);
    // Next, reserve field names in interfaces. These should only be static.
    DexType.forAllInterfaces(appInfo.dexItemFactory,
        iface -> reserveNamesInSubtypes(iface, rootState));
    // Now rename the rest.
    renameFieldsInSubtypes(appInfo.dexItemFactory.objectType);
    DexType.forAllInterfaces(appInfo.dexItemFactory, this::renameFieldsInSubtypes);
    return renaming;
  }

  private void reserveNamesInSubtypes(DexType type, NamingState<DexType> state) {
    DexClass holder = appInfo.definitionFor(type);
    if (holder == null) {
      return;
    }
    NamingState<DexType> newState = states.computeIfAbsent(type, t -> state.createChild());
    reserveFieldNames(newState, holder.instanceFields(), holder.isLibraryClass());
    reserveFieldNames(newState, holder.staticFields(), holder.isLibraryClass());
    type.forAllExtendsSubtypes(subtype -> reserveNamesInSubtypes(subtype, newState));
  }

  private void reserveFieldNames(NamingState<DexType> state, DexEncodedField[] fields,
      boolean isLibrary) {
    for (DexEncodedField encodedField : fields) {
      if (isLibrary || rootSet.noObfuscation.contains(encodedField)) {
        DexField field = encodedField.field;
        state.reserveName(field.name, field.type);
      }
    }
  }

  private void renameFieldsInSubtypes(DexType type) {
    DexClass clazz = appInfo.definitionFor(type);
    if (clazz == null) {
      return;
    }
    NamingState<DexType> state = states.get(clazz.type);
    assert state != null;
    renameFields(clazz.instanceFields(), state);
    renameFields(clazz.staticFields(), state);
    type.forAllExtendsSubtypes(this::renameFieldsInSubtypes);
  }

  private void renameFields(DexEncodedField[] fields, NamingState<DexType> state) {
    for (DexEncodedField encodedField : fields) {
      DexField field = encodedField.field;
      if (!state.isReserved(field.name, field.type)) {
        renaming.put(field, state.assignNewNameFor(field.name, field.type, false));
      }
    }
  }
}
