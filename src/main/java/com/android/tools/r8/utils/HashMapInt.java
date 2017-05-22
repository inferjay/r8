// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

// Hash map based on open addressing where keys are Objects and values are basic ints.
// Provides: put, get, and size.

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public class HashMapInt<T> extends SimpleHashMap {

  private Object[] keys;
  private int[] values;

  public HashMapInt() {
    super();
  }

  public HashMapInt(int initialCapacity) {
    super(initialCapacity);
  }

  public HashMapInt(int initialCapacity, double loadFactor) {
    super(initialCapacity, loadFactor);
  }

  public void put(final T key, final int value) {
    if (key == null) {
      throw new RuntimeException("HashMapInt does not support null as key.");
    }
    ensureCapacity();
    basePut(key, value);
  }

  boolean equals(Object one, Object other) {
    return one.equals(other);
  }

  public int get(final T key) {
    if (key == null) {
      throw new RuntimeException("HashMapInt does not support null as key.");
    }
    int count = 1;
    int index = firstProbe(key);
    while (!equals(key, keys[index])) {
      if (keys[index] == null) {
        throw new RuntimeException("HashMapInt get only works if key is present.");
      }
      index = nextProbe(index, count++);
    }
    return values[index];
  }

  int firstProbe(T key) {
    return firstProbe(key.hashCode());
  }

  public boolean containsKey(final T key) {
    if (key == null) {
      throw new RuntimeException("HashMapInt does not support null as key.");
    }
    int count = 1;
    for (int index = firstProbe(key); ; index = nextProbe(index, count++)) {
      Object k = keys[index];
      if (k == null) {
        return false;
      }
      if (equals(k, key)) {
        return true;
      }
    }
  }

  private void basePut(final T key, final int value) {
    int count = 1;
    int index = firstProbe(key);
    while ((keys[index] != null) && (keys[index] != key)) {
      index = nextProbe(index, count++);
    }
    if (keys[index] == null) {
      keys[index] = key;
      incrementSize();
    }
    values[index] = value;
  }

  @SuppressWarnings("unchecked")
  public Iterable<Integer> values() {
    return () -> Arrays.stream(keys).filter(Objects::nonNull).map((key) -> get((T) key)).iterator();
  }

  @SuppressWarnings("unchecked")
  public Iterable<T> keys() {
    return () -> (Iterator<T>) Arrays.stream(keys).filter(Objects::nonNull).iterator();
  }

  @SuppressWarnings("unchecked")
  void resize() {
    final Object[] oldKeys = keys;
    final int[] oldValues = values;
    final int oldLength = length();
    super.resize();
    // Repopulate.
    for (int index = 0; index < oldLength; index++) {
      T key = (T) oldKeys[index];
      if (key != null) {
        basePut(key, oldValues[index]);
      }
    }
  }

  void initialize(final int length, final int limit) {
    super.initialize(length, limit);
    keys = new Object[length];
    values = new int[length];
  }
}
