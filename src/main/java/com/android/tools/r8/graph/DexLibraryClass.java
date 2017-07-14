// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.Resource;
import com.android.tools.r8.dex.IndexedItemCollection;
import com.android.tools.r8.dex.MixedSectionCollection;
import com.android.tools.r8.errors.Unreachable;
import java.util.function.Supplier;

public class DexLibraryClass extends DexClass implements Supplier<DexLibraryClass> {

  public DexLibraryClass(DexType type, Resource.Kind origin, DexAccessFlags accessFlags,
      DexType superType, DexTypeList interfaces, DexString sourceFile, DexAnnotationSet annotations,
      DexEncodedField[] staticFields, DexEncodedField[] instanceFields,
      DexEncodedMethod[] directMethods, DexEncodedMethod[] virtualMethods) {
    super(sourceFile, interfaces, accessFlags, superType, type,
        staticFields, instanceFields, directMethods, virtualMethods, annotations, origin);
  }

  @Override
  public void collectIndexedItems(IndexedItemCollection indexedItems) {
    throw new Unreachable();
  }

  @Override
  public String toString() {
    return type.toString() + "(library class)";
  }

  @Override
  public String toSourceString() {
    return type.toSourceString() + "(library class)";
  }

  @Override
  public void addDependencies(MixedSectionCollection collector) {
    // Should never happen but does not harm.
    assert false;
  }

  @Override
  public boolean isLibraryClass() {
    return true;
  }

  @Override
  public DexLibraryClass asLibraryClass() {
    return this;
  }

  @Override
  public DexLibraryClass get() {
    return this;
  }
}
