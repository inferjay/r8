// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

public class Locals {

  private static void noLocals() {
    System.out.println("There's no local here");
  }

  private static void unusedLocals() {
    int i = Integer.MAX_VALUE;
    System.out.println("Not using local variable");
  }

  private static void constantLocals(int p) {
    int c = 5;
    int v = c + p;
    System.out.println("c=" + c + ", v=" + v);
  }

  private static void zeroLocals() {
    int i = 0;
    float f = 0.0f;
    System.out.println("zeroLocals");
  }

  private static void noFlowOptimization() {
    int i = 0;
    if (i == 0) {
      System.out.println("i == 0");
    } else {
      System.out.println("i != 0");
    }
  }

  private static void manyLocals() {
    int i1 = 1;
    int i2 = 2;
    int i3 = 3;
    int i4 = 4;
    int i5 = 5;
    int i6 = 6;
    int i7 = 7;
    int i8 = 8;
    int i9 = 9;
    int i10 = 10;
    int i11 = 11;
    int i12 = 12;
    invokeRange(i6, i5, i4, i3, i2, i1, invokeRange(i12, i11, i10, i9, i8, i7, 0));
  }

  private static int reverseRange(int a, int b, int c, int d, int e, int f, int g) {
    return invokeRange(g, f, e, d, c, b, a);
  }

  private static int invokeRange(int a, int b, int c, int d, int e, int f, int g) {
    System.out.println(a + b + c + d + e + f + g);
    return a + b + c + d + e + f + g;
  }

  private void lotsOfArrayLength() {
    int lengthOfArray1 = 0;
    int lengthOfArray2 = 0;
    int lengthOfArray3 = 0;
    int lengthOfArray4 = 0;
    int lengthOfArray5 = 0;
    int lengthOfArray6 = 0;
    int lengthOfArray7 = 0;
    int lengthOfArray8 = 0;
    int lengthOfArray9 = 0;
    int lengthOfArray10 = 0;
    int lengthOfArray11 = 0;
    int lengthOfArray12 = 0;
    int lengthOfArray13 = 0;
    int lengthOfArray14 = 0;
    int lengthOfArray15 = 0;
    int lengthOfArray16 = 0;

    // These statements are compiled into new-array in DEX which stores the result in a 4bit
    // register (0..15).
    boolean[] array1 = new boolean[1];
    byte[] array2 = new byte[1];
    char[] array3 = new char[1];
    short[] array4 = new short[1];
    int[] array5 = new int[1];
    long[] array6 = new long[1];
    float[] array7 = new float[1];
    double[] array8 = new double[1];
    Object[] array9 = new Object[1];
    String[] array10 = new String[1];
    String[] array11 = new String[1];
    String[] array12 = new String[1];
    String[] array13 = new String[1];
    String[] array14 = new String[1];
    String[] array15 = new String[1];
    String[] array16 = new String[1];

    // 1st breakpoint to capture the IDs of each array.
    breakpoint();

    // Breakpoint at line below. In DEX, the array-length instruction expects a 4bit register
    // (0..15). By creating >16 locals, we should cause an intermediate move instruction to
    // copy/move a high register (>= 16) into a lower register (< 16).
    // A test should step instruction by instruction and make sure all locals have the correct
    // value.
    lengthOfArray1 = array1.length;
    lengthOfArray2 = array2.length;
    lengthOfArray3 = array3.length;
    lengthOfArray4 = array4.length;
    lengthOfArray5 = array5.length;
    lengthOfArray6 = array6.length;
    lengthOfArray7 = array7.length;
    lengthOfArray8 = array8.length;
    lengthOfArray9 = array9.length;
    lengthOfArray10 = array10.length;
    lengthOfArray11 = array11.length;
    lengthOfArray12 = array12.length;
    lengthOfArray13 = array13.length;
    lengthOfArray14 = array14.length;
    lengthOfArray15 = array15.length;
    lengthOfArray16 = array16.length;

    // Use all locals
    System.out.println(array1);
    System.out.println(array2);
    System.out.println(array3);
    System.out.println(array4);
    System.out.println(array5);
    System.out.println(array6);
    System.out.println(array7);
    System.out.println(array8);
    System.out.println(array9);
    System.out.println(array10);
    System.out.println(array11);
    System.out.println(array12);
    System.out.println(array13);
    System.out.println(array14);
    System.out.println(array15);
    System.out.println(array16);

    System.out.println(lengthOfArray1);
    System.out.println(lengthOfArray2);
    System.out.println(lengthOfArray3);
    System.out.println(lengthOfArray4);
    System.out.println(lengthOfArray5);
    System.out.println(lengthOfArray6);
    System.out.println(lengthOfArray7);
    System.out.println(lengthOfArray8);
    System.out.println(lengthOfArray9);
    System.out.println(lengthOfArray10);
    System.out.println(lengthOfArray11);
    System.out.println(lengthOfArray12);
    System.out.println(lengthOfArray13);
    System.out.println(lengthOfArray14);
    System.out.println(lengthOfArray15);
    System.out.println(lengthOfArray16);
  }

  // Utility method to set a breakpoint and inspect the stack.
  private static void breakpoint() {
  }

  public void foo(int x) {
    Integer obj = new Integer(x + x);
    long l = obj.longValue();
    try {
      l = obj.longValue();
      x = (int) l / x;
      invokerangeLong(l, l, l, l, l, l);
      sout(x);
    } catch (ArithmeticException e) {
      sout(l);
    } catch (RuntimeException e) {
      sout(l); // We should not attempt to read the previous definition of 'e' here or below.
    } catch (Throwable e) {
      sout(l);
    }
  }

  private void sout(long l) {
    System.out.print(l);
  }

  private void invokerangeLong(long a, long b, long c, long d, long e, long f) {
    if (a != d) {
      throw new RuntimeException("unexpected");
    }
  }

  public static void main(String[] args) {
    noLocals();
    unusedLocals();
    constantLocals(10);
    zeroLocals();
    noFlowOptimization();
    manyLocals();
    reverseRange(1,2,3,4,5,6,7);
    new Locals().lotsOfArrayLength();
    new Locals().foo(21);
  }

}
