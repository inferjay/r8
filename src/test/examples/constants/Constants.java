// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

// This code is not run directly. It needs to be compiled to dex code.
// 'constants.dex' is what is run.
package constants;

class Constants {

  public static void printInt(int x) {
    System.out.print(x);
  }

  public static void printLong(long x) {
    System.out.print(x);
  }

  public static void testConst4() {
    printInt(-8);
    printInt(-1);
    printInt(0);
    printInt(1);
    printInt(7);
  }

  public static void testConst16() {
    printInt(Short.MIN_VALUE);
    printInt(-9);
    printInt(8);
    printInt(Short.MAX_VALUE);
  }

  public static void testConstHigh16() {
    printInt(0xffff0000);
    printInt(0xf0000000);
    printInt(0x0f000000);
    printInt(0x00f00000);
    printInt(0x000f0000);
    printInt(0x80000000);
    printInt(0x00010000);
  }

  public static void testConst() {
    printInt(Short.MIN_VALUE - 1);
    printInt(Short.MAX_VALUE + 1);

    printInt(0xffff0001);
    printInt(0xf0000001);
    printInt(0x0f000001);
    printInt(0x00f00001);
    printInt(0x000f0001);
    printInt(0x80000001);
    printInt(0x00010001);
  }

  public static void testConstWide16() {
    printLong((long) Short.MIN_VALUE);
    printLong(-1L);
    printLong(0L);
    printLong(1L);
    printLong((long) Short.MAX_VALUE);
  }

  public static void testConstWide32() {
    printLong((long) Short.MIN_VALUE - 1);
    printLong((long) Integer.MIN_VALUE);
    printLong((long) Integer.MAX_VALUE);
    printLong((long) Short.MAX_VALUE + 1);
  }

  public static void testConstWideHigh16() {
    printLong(0xffff000000000000L);
    printLong(0xf000000000000000L);
    printLong(0x0f00000000000000L);
    printLong(0x00f0000000000000L);
    printLong(0x000f000000000000L);
    printLong(0x8000000000000000L);
    printLong(0x0001000000000000L);
    printLong(0x7fff000000000000L);
  }

  public static void testConstWide() {
    printLong((long) Integer.MIN_VALUE - 1);
    printLong((long) Integer.MAX_VALUE + 1);

    printLong(0xffff7fffffffffffL);
    printLong(0xffff000000000001L);
    printLong(0xf000000000000001L);
    printLong(0x0f00000000000001L);
    printLong(0x00f0000000000001L);
    printLong(0x000f000000000001L);
    printLong(0x8000000000000001L);
    printLong(0x0001000000000001L);
    printLong(0x7fffffffffffffffL);
    printLong(0x7fff000000000001L);
  }

  public static void main(String[] args) {
    testConst4();
    testConst16();
    testConstHigh16();
    testConst();

    testConstWide16();
    testConstWide32();
    testConstWideHigh16();
    testConstWide();
  }
}
