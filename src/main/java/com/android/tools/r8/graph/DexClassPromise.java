// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

/**
 * Provides a way for delayed DexClass discovery.
 *
 * Provides minimal class details of the promised class and
 * provides the class when asked by calling method get().
 *
 * Note that DexClass also implements this interface, since it
 * represents a 'materialized' promise for a class.
 */
public interface DexClassPromise {
  DexType getType();

  DexClass.Origin getOrigin();

  boolean isProgramClass();

  boolean isClasspathClass();

  boolean isLibraryClass();

  DexClass get();
}
