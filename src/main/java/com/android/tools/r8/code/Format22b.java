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

public abstract class Format22b extends Base2Format {

  public final int AA;
  public final int BB;
  public final int CC;

  // vAA | op | #+CC | VBB
  /*package*/ Format22b(int high, BytecodeStream stream) {
    super(stream);
    AA = high;
    CC = readSigned8BitValue(stream);
    BB = read8BitValue(stream);
  }

  /*package*/ Format22b(int AA, int BB, int CC) {
    assert 0 <= AA && AA <= Constants.U8BIT_MAX;
    assert 0 <= BB && BB <= Constants.U8BIT_MAX;
    assert Byte.MIN_VALUE <= CC && CC <= Byte.MAX_VALUE;
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
    Format22b o = (Format22b) other;
    return o.AA == AA && o.BB == BB && o.CC == CC;
  }

  public String toString(ClassNameMapper naming) {
    return formatString("v" + AA + ", v" + BB + ", #" + CC);
  }

  public String toSmaliString(ClassNameMapper naming) {
    return formatSmaliString(
        "v" + AA + ", v" + BB + ", 0x" + StringUtils.hexString(CC, 2) + "  # " + CC);
  }

  @Override
  public void collectIndexedItems(IndexedItemCollection indexedItems) {
    // No references.
  }
}
