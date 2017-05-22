// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

public class IdentityHashMapInt<T> extends HashMapInt<T> {

  public IdentityHashMapInt() {
    super();
  }

  public IdentityHashMapInt(int initialCapacity) {
    super(initialCapacity);
  }

  public IdentityHashMapInt(int initialCapacity, double loadFactor) {
    super(initialCapacity, loadFactor);
  }

  @Override
  int firstProbe(T key) {
    return firstProbe(System.identityHashCode(key));
  }

  @Override
  boolean equals(Object one, Object other) {
    return one == other;
  }
}
