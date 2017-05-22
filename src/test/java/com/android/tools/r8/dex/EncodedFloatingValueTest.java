// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.dex;

import static com.android.tools.r8.dex.Constants.DEX_MAGIC_SIZE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.android.tools.r8.utils.EncodedValueUtils;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class EncodedFloatingValueTest {
  private final double value;

  public EncodedFloatingValueTest(double value) {
    this.value = value;
  }

  @Parameters(name = "{0}")
  public static Collection<Double> data() {
    return Arrays.asList(
        0.0,
        1.0,
        0.5,
        Double.longBitsToDouble(1), // Lowest bit is 1 in double
        Double.longBitsToDouble(0x10), // Bits on byte boundary are 1.
        Double.longBitsToDouble(0x08),
        Double.longBitsToDouble(4607071218809329336L),  // Test a real long (regression).
        (double) (Float.intBitsToFloat(1)), // Lowest bit is 1 in float
        (double) (Float.intBitsToFloat(0x10)), // Bits on byte boundary are 1
        (double) (Float.intBitsToFloat(0x08))
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
  public void testEncodeDecodeDouble() {
    byte[] bytes = EncodedValueUtils.encodeDouble(value);
    assertTrue(bytes.length <= Double.BYTES);
    DexFile dexFile = createDexFileWithContent(bytes);
    Assert.assertEquals(value, EncodedValueUtils.parseDouble(dexFile, bytes.length), 0.0);
  }

  @Test
  public void testEncodeDecodeFloat() {
    byte[] bytes = EncodedValueUtils.encodeFloat((float) value);
    assertTrue(bytes.length <= Float.BYTES);
    DexFile dexFile = createDexFileWithContent(bytes);
    Assert.assertEquals((float) value, EncodedValueUtils.parseFloat(dexFile, bytes.length), 0.0f);
  }

  @Test
  public void testEncodeDecodeDoubleWithDexBuffer() {
    DexOutputBuffer buffer = new DexOutputBuffer();
    int length = EncodedValueUtils.putDouble(buffer, value);
    assertTrue(length <= Double.BYTES);
    byte[] bytes = buffer.asArray();
    DexFile dexFile = createDexFileWithContent(bytes);
    assertEquals(value, EncodedValueUtils.parseDouble(dexFile, length), 0.0);
  }

  @Test
  public void testEncodeDecodeFloatWithDexBuffer() {
    DexOutputBuffer buffer = new DexOutputBuffer();
    int length = EncodedValueUtils.putFloat(buffer, (float) value);
    assertTrue(length <= Float.BYTES);
    byte[] bytes = buffer.asArray();
    DexFile dexFile = createDexFileWithContent(bytes);
    assertEquals((float) value, EncodedValueUtils.parseFloat(dexFile, length), 0.0f);
  }
}
