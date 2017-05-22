// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

// Hash map based on open addressing where keys are basic ints and values are Objects.
// Provides: put, get, and size.

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.IntStream;

public class IntHashMap<T> extends SimpleHashMap {

  private int[] keys;
  private Object[] values;

  public IntHashMap() {
    super();
  }

  public IntHashMap(int initialCapacity) {
    super(initialCapacity);
  }

  public IntHashMap(int initialCapacity, double loadFactor) {
    super(initialCapacity, loadFactor);
  }

  public void put(final int key, final T value) {
    if (value == null) {
      throw new RuntimeException("IntHashMap does not support null as value.");
    }
    ensureCapacity();
    basePut(key, value);
  }

  public T get(final int key) {
    int count = 1;
    int index = firstProbe(computeHash(key));
    // Note that unused entries in keys is 0.
    // That means we only need to check for value != null when key == 0.
    while ((keys[index] != key) && (values[index] != null)) {
      index = nextProbe(index, count++);
    }
    assert (keys[index] == key) || (values[index] == null);
    @SuppressWarnings("unchecked")
    T result = (T) values[index];
    return result;
  }

  private void basePut(final int key, final Object value) {
    assert value != null;
    int count = 1;
    int index = firstProbe(computeHash(key));
    while ((values[index] != null) && (keys[index] != key)) {
      index = nextProbe(index, count++);
    }
    if (values[index] == null) {
      keys[index] = key;
      incrementSize();
    }
    values[index] = value;
    assert value.equals(get(key));
  }

  void resize() {
    final int[] oldKeys = keys;
    final Object[] oldValues = values;
    final int oldLength = length();
    super.resize();
    // Repopulate.
    for (int index = 0; index < oldLength; index++) {
      Object value = oldValues[index];
      if (value != null) {
        basePut(oldKeys[index], value);
      }
    }
  }

  void initialize(final int length, final int limit) {
    super.initialize(length, limit);
    keys = new int[length];
    values = new Object[length];
  }

  @SuppressWarnings("unchecked")
  public Iterable<T> values() {
    return () -> (Iterator<T>) Arrays.stream(values).filter(Objects::nonNull).iterator();
  }

  public Iterable<Integer> keys() {
    if (get(0) != null) {
      return () -> IntStream.concat(IntStream.of(0), Arrays.stream(keys).filter(i -> i != 0))
          .iterator();
    }
    return () -> Arrays.stream(keys).filter(i -> i != 0 || get(i) != null).distinct().iterator();
  }

  // Thomas Wang, Integer Hash Functions.
  // http://www.concentric.net/~Ttwang/tech/inthash.htm
  private static int computeHash(final int key) {
    int hash = key;
    hash = ~hash + (hash << 15);  // hash = (hash << 15) - hash - 1;
    hash = hash ^ (hash >> 12);
    hash = hash + (hash << 2);
    hash = hash ^ (hash >> 4);
    hash = hash * 2057;  // hash = (hash + (hash << 3)) + (hash << 11);
    hash = hash ^ (hash >> 16);
    return hash & 0x3fffffff;
  }
}
