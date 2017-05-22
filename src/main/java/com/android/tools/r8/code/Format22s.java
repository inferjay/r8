// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.code;

import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.dex.IndexedItemCollection;
import com.android.tools.r8.graph.ObjectToOffsetMapping;
import com.android.tools.r8.naming.ClassNameMapper;
import com.android.tools.r8.utils.StringUtils;
import java.nio.ShortBuffer;

public abstract class Format22s extends Base2Format {

  public final int A;
  public final int B;
  public final int CCCC;

  // vB | vA | op | #+CCCC
  /*package*/ Format22s(int high, BytecodeStream stream) {
    super(stream);
    A = high & 0xf;
    B = (high >> 4) & 0xf;
    CCCC = readSigned16BitValue(stream);
  }

  /*package*/ Format22s(int A, int B, int CCCC) {
    assert 0 <= A && A <= Constants.U4BIT_MAX;
    assert 0 <= B && B <= Constants.U4BIT_MAX;
    assert Short.MIN_VALUE <= CCCC && CCCC <= Short.MAX_VALUE;
    this.A = A;
    this.B = B;
    this.CCCC = CCCC;
  }

  public void write(ShortBuffer dest, ObjectToOffsetMapping mapping) {
    writeFirst(B, A, dest);
    write16BitValue(CCCC, dest);
  }

  public final int hashCode() {
    return ((CCCC << 8) | (A << 4) | B) ^ getClass().hashCode();
  }

  public final boolean equals(Object other) {
    if (other == null || this.getClass() != other.getClass()) {
      return false;
    }
    Format22s o = (Format22s) other;
    return o.A == A && o.B == B && o.CCCC == CCCC;
  }

  public String toString(ClassNameMapper naming) {
    return formatString("v" + A + ", v" + B + ", #" + CCCC);
  }

  public String toSmaliString(ClassNameMapper naming) {
    return formatSmaliString(
        "v" + A + ", v" + B + ", 0x" + StringUtils.hexString(CCCC, 4) + "  # " + CCCC);
  }

  @Override
  public void collectIndexedItems(IndexedItemCollection indexedItems) {
    // No references.
  }
}
