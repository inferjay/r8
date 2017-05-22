// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.dex;

import static org.junit.Assert.assertEquals;

import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexString;
import org.junit.Test;

public class DexStringTest {

  @Test
  public void testEncodingLength() {
    DexItemFactory factory = new DexItemFactory();
    checkEncodedLength(factory.createString("\u0000"), 2);
    checkEncodedLength(factory.createString("\u0001"), 1);
    checkEncodedLength(factory.createString("\u007f"), 1);
    checkEncodedLength(factory.createString("\u0080"), 2);
    checkEncodedLength(factory.createString("\u07ff"), 2);
    checkEncodedLength(factory.createString("\u0800"), 3);
    checkEncodedLength(factory.createString("\uffff"), 3);
    checkEncodedLength(factory.createString("\ud800\udc00"), 6);
    checkEncodedLength(factory.createString("\udbff\udfff"), 6);
  }

  @Test
  public void testCompare() {
    DexItemFactory factory = new DexItemFactory();

    // Test strings in lexicographic order.
    DexString[] strings = {
        factory.createString(""),
        factory.createString("\u0000"),
        factory.createString("\u0001"),
        factory.createString("\u0060a"),  // 'a' is 0x61.
        factory.createString("a"),
        factory.createString("a\u0000a"),
        factory.createString("a\u0001a"),
        factory.createString("a\u0060a"),  // 'a' is 0x61.
        factory.createString("aa"),
        factory.createString("aaa"),
        factory.createString("a\u007f"),  // U+007f is the last code point with one UTF-8 bytes.
        factory.createString("a\u007fa"),
        factory.createString("a\u0080"),  // U+0080 is the first code point with two UTF-8 bytes.
        factory.createString("a\u0080a"),
        factory.createString("a\u07ff"),  // U+07ff is the last code point with two UTF-8 bytes.
        factory.createString("a\u07ffa"),
        factory.createString("a\u0800"),  // U+0800 is the first code point with three UTF-8 bytes.
        factory.createString("a\u0800a"),
        factory.createString("a\u0801"),
        factory.createString("a\u0801a"),
        factory.createString("a\ud800\udc00a"),  // Surrogate pair for U+010000. Sorts per UTF-16.
        factory.createString("a\udbff\udfffa"),  // Surrogate pair for U+10ffff. Sorts per UTF-16.
        factory.createString("a\uffffa"),
        factory.createString("\u007f"),  // U+007f is the last code point with one UTF-8 bytes.
        factory.createString("\u0080"),  // U+0080 is the first code point with two UTF-8 bytes.
        factory.createString("\u07ff"),  // U+07ff is the last code point with two UTF-8 bytes.
        factory.createString("\u0800"),  // U+0800 is the first code point with three UTF-8 bytes.
        factory.createString("\u0801"),  // U+0800 is the first code point with three UTF-8 bytes.
        factory.createString("\ud800\udc00"),  // Surrogate pair for U+010000. Sorts per UTF-16.
        factory.createString("\udbff\udfff"),  // Surrogate pair for U+10ffff. Sorts per UTF-16.
        factory.createString("\uffff"),
    };


    for (int i = 0; i < strings.length; i++) {
      for (int j = 0; j < strings.length; j++) {
        int expected = Integer.signum(i - j);
        check(expected, strings[i], strings[j]);
        check(-expected, strings[j], strings[i]);
      }
    }
  }

  private void check(int expected, DexString s1, DexString s2) {
    assertEquals(s1.dump() + " " + s2.dump(),
        expected, Integer.signum(s1.toString().compareTo(s2.toString())));
    assertEquals(s1.dump() + " " + s2.dump(),
        expected, Integer.signum(s1.slowCompareTo(s2)));
  }

  private void checkEncodedLength(DexString s, int encodedLength) {
    // The terminating zero is not part of the encoding,
    int length = s.content.length;
    assertEquals(0, s.content[length - 1]);
    assertEquals(encodedLength, length - 1);
  }
}
