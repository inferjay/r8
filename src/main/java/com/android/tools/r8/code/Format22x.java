// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.code;

import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.dex.IndexedItemCollection;
import com.android.tools.r8.graph.ObjectToOffsetMapping;
import com.android.tools.r8.naming.ClassNameMapper;
import java.nio.ShortBuffer;

abstract class Format22x extends Base2Format {

  public final int AA;
  public final int BBBB;

  // AA | op | vBBBB
  Format22x(int high, BytecodeStream stream) {
    super(stream);
    AA = high;
    BBBB = read16BitValue(stream);
  }

  Format22x(int dest, int src) {
    assert 0 <= dest && dest <= Constants.U8BIT_MAX;
    assert 0 <= src && src <= Constants.U16BIT_MAX;
    AA = dest;
    BBBB = src;
  }

  public void write(ShortBuffer dest, ObjectToOffsetMapping mapping) {
    writeFirst(AA, dest);
    write16BitValue(BBBB, dest);
  }

  public final int hashCode() {
    return ((BBBB << 8) | AA) ^ getClass().hashCode();
  }

  public final boolean equals(Object other) {
    if (other == null || this.getClass() != other.getClass()) {
      return false;
    }
    Format22x o = (Format22x) other;
    return o.AA == AA && o.BBBB == BBBB;
  }

  public String toString(ClassNameMapper naming) {
    return formatString("v" + AA + ", v" + BBBB);
  }

  public String toSmaliString(ClassNameMapper naming) {
    return formatSmaliString("v" + AA + ", v" + BBBB);
  }

  @Override
  public void collectIndexedItems(IndexedItemCollection indexedItems) {
    // No references.
  }
}
