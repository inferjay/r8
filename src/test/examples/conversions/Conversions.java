// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package conversions;

public class Conversions {

  public static byte b(byte b) {
    return b;
  }

  public static char c(char c) {
    return c;
  }

  public static short s(short s) {
    return s;
  }

  public static int i() {
    return 1;
  }

  public static int i(int i) {
    return i;
  }

  public static long l() {
    return 1;
  }

  public static long l(long l) {
    return l;
  }

  public static double d() {
    return 1;
  }

  public static double d(double d) {
    return d;
  }

  public static float f() {
    return 1;
  }

  public static float f(float f) {
    return f;
  }

  public static void main(String[] args) {
    // I2L, I2F, I2D
    System.out.println(l(i()));
    System.out.println(f(i()));
    System.out.println(d(i()));
    // L2I, L2F, L2D
    System.out.println(i((int) l()));
    System.out.println(f(l()));
    System.out.println(d(l()));
    // F2I, F2L, F2D
    System.out.println(i((int) f()));
    System.out.println(l((long) f()));
    System.out.println(d(f()));
    // D2I, D2L, D2F
    System.out.println(i((int) d()));
    System.out.println(l((long) d()));
    System.out.println(f((float) d()));
    // I2B, I2C, I2S
    System.out.println(b((byte) i()));
    System.out.println(c((char) i()));
    System.out.println(s((short) i()));
  }
}
