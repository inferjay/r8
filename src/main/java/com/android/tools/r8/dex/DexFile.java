// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.dex;

import static com.android.tools.r8.dex.Constants.DEX_FILE_MAGIC_PREFIX;

import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.utils.LebUtils;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DexFile {

  final String name;
  private final ByteBuffer buffer;
  private final int version;

  DexFile(String name) throws IOException {
    this.name = name;
    Path path = Paths.get(name);
    buffer = ByteBuffer.wrap(Files.readAllBytes(path));
    version = parseMagic(buffer);
  }

  public DexFile(InputStream input) throws IOException {
    // TODO(zerny): Remove dependencies on file names.
    name = "input-stream.dex";
    buffer = ByteBuffer.wrap(ByteStreams.toByteArray(input));
    version = parseMagic(buffer);
  }

  /**
   * Returns a File that contains the bytes provided as argument. Used for testing.
   *
   * @param bytes contents of the file
   */
  DexFile(byte[] bytes) {
    this.name = "mockfile.dex";
    buffer = ByteBuffer.wrap(bytes);
    version = parseMagic(buffer);
  }

  // Parse the magic header and determine the dex file version.
  private int parseMagic(ByteBuffer buffer) {
    int index = 0;
    for (byte prefixByte : DEX_FILE_MAGIC_PREFIX) {
      if (buffer.get(index++) != prefixByte) {
        throw new CompilationError("Dex file has invalid header: " + name);
      }
    }
    if (buffer.get(index++) != '0' || buffer.get(index++) != '3') {
      throw new CompilationError("Dex file has invalid version number: " + name);
    }
    byte versionByte = buffer.get(index++);
    int version;
    switch (versionByte) {
      case '8':
        version = 38;
        break;
      case '7':
        version = 37;
        break;
      case '5':
        version = 35;
        break;
      default:
        throw new CompilationError("Dex file has invalid version number: " + name);
    }
    if (buffer.get(index++) != '\0') {
      throw new CompilationError("Dex file has invalid header: " + name);
    }
    return version;
  }

  int getDexVersion() {
    return version;
  }

  byte[] getByteArray(int size) {
    byte[] result = new byte[size];
    buffer.get(result);
    return result;
  }

  int getUleb128() {
    return LebUtils.parseUleb128(this);
  }

  int getSleb128() {
    return LebUtils.parseSleb128(this);
  }

  int getUleb128p1() {
    return getUleb128() - 1;
  }

  int getUint() {
    int result = buffer.getInt();
    assert result >= 0;  // Ensure the java int didn't overflow.
    return result;
  }

  int getUshort() {
    int result = buffer.getShort() & 0xffff;
    assert result >= 0;  // Ensure we have a non-negative number.
    return result;
  }

  short getShort() {
    return buffer.getShort();
  }

  int getUint(int offset) {
    int result = buffer.getInt(offset);
    assert result >= 0;  // Ensure the java int didn't overflow.
    return result;
  }

  public int getInt() {
    return buffer.getInt();
  }

  void setByteOrder() {
    // Make sure we set the right endian for reading.
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    int endian = buffer.getInt(Constants.ENDIAN_TAG_OFFSET);
    if (endian == Constants.REVERSE_ENDIAN_CONSTANT) {
      buffer.order(ByteOrder.BIG_ENDIAN);
    } else {
      assert endian == Constants.ENDIAN_CONSTANT;
    }
  }

  int position() {
    return buffer.position();
  }

  void position(int position) {
    buffer.position(position);
  }

  void align(int alignment) {
    assert (alignment & (alignment - 1)) == 0;   // Check alignment is power of 2.
    int p = buffer.position();
    p += (alignment - (p % alignment)) & (alignment - 1);
    buffer.position(p);
  }

  public byte get() {
    return buffer.get();
  }

  int getUbyte() {
    int result = buffer.get() & 0xff;
    assert result >= 0;  // Ensure we have a non-negative result.
    return result;
  }

  int end() {
    return buffer.capacity();
  }
}
