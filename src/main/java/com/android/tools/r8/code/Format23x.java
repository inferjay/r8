// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.code;

import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.dex.IndexedItemCollection;
import com.android.tools.r8.graph.ObjectToOffsetMapping;
import com.android.tools.r8.naming.ClassNameMapper;
import java.nio.ShortBuffer;

abstract class Format23x extends Base2Format {

  public final int AA;
  public final int BB;
  public final int CC;

  // vAA | op | vCC | vBB
  Format23x(int high, BytecodeStream stream) {
    super(stream);
    AA = high;
    CC = read8BitValue(stream);
    BB = read8BitValue(stream);
  }

  Format23x(int AA, int BB, int CC) {
    assert 0 <= AA && AA <= Constants.U8BIT_MAX;
    assert 0 <= BB && BB <= Constants.U8BIT_MAX;
    assert 0 <= CC && CC <= Constants.U8BIT_MAX;
    this.AA = AA;
    this.BB = BB;
    this.CC = CC;
  }

  public void write(ShortBuffer dest, ObjectToOffsetMapping mapping) {
    writeFirst(AA, dest);
    write16BitValue(combineBytes(CC, BB), dest);
  }

  public final int hashCode() {
    return ((AA << 16) | (BB << 8) | CC) ^ getClass().hashCode();
  }

  public final boolean equals(Object other) {
    if (other == null || this.getClass() != other.getClass()) {
      return false;
    }
    Format23x o = (Format23x) other;
    return o.AA == AA && o.BB == BB && o.CC == CC;
  }

  public String toString(ClassNameMapper naming) {
    return formatString("v" + AA + ", v" + BB + ", v" + CC);
  }

  public String toSmaliString(ClassNameMapper naming) {
    return formatSmaliString("v" + AA + ", v" + BB + ", v" + CC);
  }

  @Override
  public void collectIndexedItems(IndexedItemCollection indexedItems) {
    // No references.
  }
}
