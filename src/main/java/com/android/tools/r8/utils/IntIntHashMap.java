// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

// Hash map based on open addressing where keys are positive basic ints and values are basic ints.
// Provides: put, get, and size.

import java.util.Arrays;

public class IntIntHashMap extends SimpleHashMap {

  private final int EMPTY_KEY = -1;

  private int[] keys;
  private int[] values;

  public IntIntHashMap() {
    super();
  }

  public IntIntHashMap(int initialCapacity) {
    super(initialCapacity);
  }

  public IntIntHashMap(int initialCapacity, double loadFactor) {
    super(initialCapacity, loadFactor);
  }

  public void put(final int key, final int value) {
    if (key < 0) {
      throw new RuntimeException("IntIntHashMap does not support negative ints as key.");
    }
    ensureCapacity();
    basePut(key, value);
  }

  public int get(final int key) {
    if (key < 0) {
      throw new RuntimeException("IntIntHashMap does not support negative ints as key.");
    }
    int count = 1;
    int index = firstProbe(key);
    while (key != keys[index]) {
      if (keys[index] == EMPTY_KEY) {
        throw new RuntimeException("IntIntHashMap get only works if key is present.");
      }
      index = nextProbe(index, count++);
    }
    return values[index];
  }

  public boolean containsKey(final int key) {
    if (key < 0) {
      throw new RuntimeException("IntIntHashMap does not support negative ints as key.");
    }
    int count = 1;
    for (int index = firstProbe(key); ; index = nextProbe(index, count++)) {
      int k = keys[index];
      if (k == EMPTY_KEY) {
        return false;
      }
      if (k == key) {
        return true;
      }
    }
  }

  private void basePut(final int key, final int value) {
    int count = 1;
    int index = firstProbe(key);
    while ((keys[index] != EMPTY_KEY) && (keys[index] != key)) {
      index = nextProbe(index, count++);
    }
    if (keys[index] != key) {
      incrementSize();
      keys[index] = key;
    }
    values[index] = value;
  }

  void resize() {
    final int[] oldKeys = keys;
    final int[] oldValues = values;
    final int oldLength = length();
    super.resize();
    // Repopulate.
    for (int index = 0; index < oldLength; index++) {
      int key = oldKeys[index];
      if (key != EMPTY_KEY) {
        basePut(key, oldValues[index]);
      }
    }
  }

  void initialize(final int length, final int limit) {
    super.initialize(length, limit);
    keys = new int[length];
    Arrays.fill(keys, EMPTY_KEY);
    values = new int[length];
  }


  @SuppressWarnings("unchecked")
  public Iterable<Integer> values() {
    return () -> Arrays.stream(keys).filter(k -> k > EMPTY_KEY).map(this::get).iterator();
  }

  @SuppressWarnings("unchecked")
  public Iterable<Integer> keys() {
    return () -> Arrays.stream(keys).filter(k -> k > EMPTY_KEY).iterator();
  }
}
