// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package filledarray;

import java.util.Arrays;

public class FilledArray {
  private static boolean[] booleans = new boolean[] { true, true, false, false };
  private static byte[] bytes = new byte[] {
      0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, -19, -20, -96,
      Byte.MAX_VALUE, Byte.MIN_VALUE };
  private static char[] chars = new char[] {
      Character.MAX_VALUE, 'a', 'b', 'c', 'd', Character.MIN_VALUE };
  private static int[] ints = new int[] { Integer.MAX_VALUE, 0, -42, 42, Integer.MIN_VALUE };
  private static short[] shorts = new short[] { Short.MAX_VALUE, 0, -42, 42, Short.MIN_VALUE };
  private static long[] longs = new long[] {
      Long.MAX_VALUE, 0x1234123412341234L, -0x1234123412341234L, Long.MIN_VALUE };
  private static float[] floats = new float[] {
      Float.MAX_VALUE, 23.23F, -43.123F, Float.MIN_VALUE, Float.MIN_NORMAL };
  private static double[] doubles = new double[] {
      Double.MAX_VALUE, 123123123.123, -43333.123, Double.MIN_VALUE, Double.MIN_NORMAL };


  public static void filledArrays() {
    boolean[] localBooleans = new boolean[] { true, true, false, false };
    localBooleans[0] = false;
    byte[] localBytes = new byte[] { 21, 22, -23 };
    char[] localChars = new char[] { 'a', 'b', 'c', 'd' };
    int[] localInts = new int[] { Integer.MAX_VALUE, 0, -42, 42, Integer.MIN_VALUE };
    short[] localShorts = new short[] { Short.MAX_VALUE, 0, -42, 42, Short.MIN_VALUE };
    long[] localLongs= new long[] { 0x1234432112341234L, -0x1234123412344321L };
    localLongs[1] = localLongs[1] + 2;
    float[] localFloats = new float[] { 23.23F, -43.123F };
    double[] localDoubles = new double[] { 123123123.123, -43333.123 };
    System.out.println("booleans");
    for (int i = 0; i < booleans.length; i++) {
      System.out.println(booleans[i]);
    }
    for (int i = 0; i < localBooleans.length; i++) {
      System.out.println(localBooleans[i]);
    }
    System.out.println("bytes");
    for (int i = 0; i < bytes.length; i++) {
      System.out.println(bytes[i]);
    }
    for (int i = 0; i < localBytes.length; i++) {
      System.out.println(localBytes[i]);
    }
    System.out.println("chars");
    for (int i = 0; i < chars.length; i++) {
      System.out.println(chars[i]);
    }
    for (int i = 0; i < localChars.length; i++) {
      System.out.println(localChars[i]);
    }
    System.out.println("ints");
    for (int i = 0; i < ints.length; i++) {
      System.out.println(ints[i]);
    }
    for (int i = 0; i < localInts.length; i++) {
      System.out.println(localInts[i]);
    }
    System.out.println("shorts");
    for (int i = 0; i < shorts.length; i++) {
      System.out.println(shorts[i]);
    }
    for (int i = 0; i < localShorts.length; i++) {
      System.out.println(localShorts[i]);
    }
    System.out.println("longs");
    for (int i = 0; i < longs.length; i++) {
      System.out.println(longs[i]);
    }
    for (int i = 0; i < localLongs.length; i++) {
      System.out.println(localLongs[i]);
    }
    System.out.println("floats");
    for (int i = 0; i < floats.length; i++) {
      System.out.println(floats[i]);
    }
    for (int i = 0; i < localFloats.length; i++) {
      System.out.println(localFloats[i]);
    }
    System.out.println("doubles");
    for (int i = 0; i < doubles.length; i++) {
      System.out.println(doubles[i]);
    }
    for (int i = 0; i < localDoubles.length; i++) {
      System.out.println(localDoubles[i]);
    }
  }

  public static void filledArraysExceptions(int divisor) {
    try {
      // Array creations that can be turned into fill-array-data.
      int[] ints = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
      int[] ints2 = new int[5];
      ints2[0] = 0;
      ints2[1] = 1;
      ints2[2] = 2;
      ints2[3] = 3;
      ints2[4] = 4;
      int i = ints[1] / divisor;
      System.out.println("i = " + i);
      System.out.println("ints = " + Arrays.toString(ints));
      System.out.println("ints2 = " + Arrays.toString(ints2));
    } catch (Throwable t) {
      System.out.println("Exception: " + t.getClass().toString());
    }

    try {
      // Array creation that cannot be turned into fill-array-data because an exception would
      // cause the initialization sequence to be interrupted.
      int[] ints = new int[5];
      ints[0] = 0;
      ints[1] = 1;
      ints[2] = 2;
      ints[3] = 3;
      int i = 7 / divisor;
      ints[4] = 4;
      System.out.println("i = " + i);
      System.out.println("ints = " + Arrays.toString(ints));
    } catch (Throwable t) {
      System.out.println("Exception: " + t.getClass().toString());
    }
  }

  public static void main(String[] args) {
    filledArrays();
    filledArraysExceptions(1);
    filledArraysExceptions(0);
  }
}
