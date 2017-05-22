// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.code;

import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.dex.IndexedItemCollection;
import com.android.tools.r8.graph.IndexedDexItem;
import com.android.tools.r8.graph.ObjectToOffsetMapping;
import com.android.tools.r8.naming.ClassNameMapper;
import java.nio.ShortBuffer;
import java.util.function.BiPredicate;

abstract class Format22c extends Base2Format {

  public final int A;
  public final int B;
  public IndexedDexItem CCCC;

  // vB | vA | op | [type|field]@CCCC
  /*package*/ Format22c(int high, BytecodeStream stream, IndexedDexItem[] map) {
    super(stream);
    A = high & 0xf;
    B = (high >> 4) & 0xf;
    CCCC = map[read16BitValue(stream)];
  }

  /*package*/ Format22c(int A, int B, IndexedDexItem CCCC) {
    assert 0 <= A && A <= Constants.U4BIT_MAX;
    assert 0 <= B && B <= Constants.U4BIT_MAX;
    this.A = A;
    this.B = B;
    this.CCCC = CCCC;
  }

  public void write(ShortBuffer dest, ObjectToOffsetMapping mapping) {
    writeFirst(B, A, dest);
    write16BitReference(CCCC, dest, mapping);
  }

  public final int hashCode() {
    return ((CCCC.hashCode() << 8) | (A << 4) | B) ^ getClass().hashCode();
  }

  public final boolean equals(Object other) {
    if (other == null || this.getClass() != other.getClass()) {
      return false;
    }
    Format22c o = (Format22c) other;
    return o.A == A && o.B == B && o.CCCC.equals(CCCC);
  }

  public String toString(ClassNameMapper naming) {
    return formatString(
        "v" + A + ", v" + B + ", " + (naming == null ? CCCC : naming.originalNameOf(CCCC)));
  }

  public String toSmaliString(ClassNameMapper naming) {
    // TODO(sgjesse): Add support for smali name mapping.
    return formatSmaliString("v" + A + ", v" + B + ", " + CCCC.toSmaliString());
  }

  @Override
  public void collectIndexedItems(IndexedItemCollection indexedItems) {
    CCCC.collectIndexedItems(indexedItems);
  }

  @Override
  public boolean equals(Instruction other, BiPredicate<IndexedDexItem, IndexedDexItem> equality) {
    if (other == null || this.getClass() != other.getClass()) {
      return false;
    }
    Format22c o = (Format22c) other;
    return o.A == A && o.B == B && equality.test(CCCC, o.CCCC);
  }
}
