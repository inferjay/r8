// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.dex;

import static com.android.tools.r8.dex.Constants.DEX_MAGIC_SIZE;

import com.android.tools.r8.utils.LebUtils;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test for encoding and decoding Leb128
 */
@RunWith(Parameterized.class)
public class Leb128Test {
  private final int value;

  public Leb128Test(int value) {
    this.value = value;
  }

  @Parameters(name = "{0}")
  public static Collection<Integer> data() {
    return Arrays.asList(
      0,
      0x3f, // Uses 6 bits
      0x7f, // Uses 7 bits
      0xff, // Uses 8 bits
      0x3ff, // Uses 14 bits
      0x407f, // Uses 15 bits with 7 consecutive 0s.
      Integer.MIN_VALUE, // 10...0 pattern
      0xffffffc0, // 1..1000000
      0xffffff80, // 1..10000000
      0xffffff00, // 1..100000000
      0xffffc07f, // Uses 15 bits with 7 consecutive 0s.
      Integer.MIN_VALUE + 1
    );
  }

  // Create a DexFile with correct file magic followed by the argument bytes. Positions the
  // DexFile after the file magic.
  private DexFile createDexFileWithContent(byte[] bytes) {
    DexOutputBuffer buffer = new DexOutputBuffer();
    buffer.putBytes(Constants.DEX_FILE_MAGIC_PREFIX);
    buffer.putBytes(Constants.ANDROID_PRE_N_DEX_VERSION_BYTES);
    buffer.putByte(Constants.DEX_FILE_MAGIC_SUFFIX);
    buffer.putBytes(bytes);
    DexFile dexFile = new DexFile(buffer.asArray());
    dexFile.position(DEX_MAGIC_SIZE);
    return dexFile;
  }

  @Test
  public void encodeDecodeLeb128TestWithDexBuffer() {
    if (value < 0) {
      return;
    }
    DexOutputBuffer buffer = new DexOutputBuffer();
    LebUtils.putUleb128(buffer, value);
    Assert.assertEquals(buffer.position(), LebUtils.sizeAsUleb128(value));
    DexFile file = createDexFileWithContent(buffer.asArray());
    Assert.assertEquals(value, LebUtils.parseUleb128(file));
  }

  @Test
  public void encodeDecodeLeb128Test() {
    if (value < 0) {
      return;
    }
    byte[] encoded = LebUtils.encodeUleb128(value);
    Assert.assertEquals(encoded.length, LebUtils.sizeAsUleb128(value));
    DexFile file = createDexFileWithContent(encoded);
    Assert.assertEquals(value, LebUtils.parseUleb128(file));
  }

  @Test
  public void encodeDecodeSLeb128TestWithDexBuffer() {
    DexOutputBuffer buffer = new DexOutputBuffer();
    LebUtils.putSleb128(buffer, value);
    Assert.assertEquals(buffer.position(), LebUtils.sizeAsSleb128(value));
    DexFile file = createDexFileWithContent(buffer.asArray());
    Assert.assertEquals(value, LebUtils.parseSleb128(file));
  }


  @Test
  public void encodeDecodeSLeb128Test() {
    byte[] encoded = LebUtils.encodeSleb128(value);
    Assert.assertEquals(encoded.length, LebUtils.sizeAsSleb128(value));
    DexFile file = createDexFileWithContent(encoded);
    Assert.assertEquals(value, LebUtils.parseSleb128(file));
  }
}
