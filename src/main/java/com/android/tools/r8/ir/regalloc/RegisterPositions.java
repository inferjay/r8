// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.regalloc;

import java.util.Arrays;
import java.util.BitSet;

/**
 * Simple mapping from a register to an int value.
 * <p>
 * The backing for the mapping grows as needed up to a given limit. If no mapping exists for
 * a register number the value is assumed to be Integer.MAX_VALUE.
 */

public class RegisterPositions {
  private static final int INITIAL_SIZE = 16;
  private int limit;
  private int[] backing;
  private BitSet registerHoldsConstant;

  public RegisterPositions(int limit) {
    this.limit = limit;
    backing = new int[INITIAL_SIZE];
    for (int i = 0; i < INITIAL_SIZE; i++) {
      backing[i] = Integer.MAX_VALUE;
    }
    registerHoldsConstant = new BitSet(limit);
  }

  public boolean holdsConstant(int index) {
    return registerHoldsConstant.get(index);
  }

  public void set(int index, int value, boolean holdsConstant) {
    if (index >= backing.length) {
      grow(index + 1);
    }
    backing[index] = value;
    registerHoldsConstant.set(index, holdsConstant);
  }

  public int get(int index) {
    if (index < backing.length) {
      return backing[index];
    }
    assert index < limit;
    return Integer.MAX_VALUE;
  }

  public void grow(int minSize) {
    int size = backing.length;
    while (size < minSize) {
      size *= 2;
    }
    size = Math.min(size, limit);
    int oldSize = backing.length;
    backing = Arrays.copyOf(backing, size);
    for (int i = oldSize; i < size; i++) {
      backing[i] = Integer.MAX_VALUE;
    }
  }
}
