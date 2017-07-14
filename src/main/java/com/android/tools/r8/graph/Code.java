// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.dex.IndexedItemCollection;
import com.android.tools.r8.dex.MixedSectionCollection;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.optimize.Outliner.OutlineCode;
import com.android.tools.r8.naming.ClassNameMapper;
import com.android.tools.r8.utils.InternalOptions;

public abstract class Code extends CanonicalizedDexItem {

  public abstract IRCode buildIR(DexEncodedMethod encodedMethod, InternalOptions options);

  public abstract void registerReachableDefinitions(UseRegistry registry);

  public abstract String toString();

  public abstract String toString(DexEncodedMethod method, ClassNameMapper naming);

  public boolean isDexCode() {
    return false;
  }

  public boolean isJarCode() {
    return false;
  }

  public boolean isOutlineCode() {
    return false;
  }

  public DexCode asDexCode() {
    throw new Unreachable(getClass().getCanonicalName() + ".asDexCode()");
  }

  public JarCode asJarCode() {
    throw new Unreachable(getClass().getCanonicalName() + ".asJarCode()");
  }

  public OutlineCode asOutlineCode() {
    throw new Unreachable(getClass().getCanonicalName() + ".asOutlineCode()");
  }

  @Override
  void collectIndexedItems(IndexedItemCollection collection) {
    throw new Unreachable();
  }

  @Override
  void collectMixedSectionItems(MixedSectionCollection collection) {
    throw new Unreachable();
  }
}
