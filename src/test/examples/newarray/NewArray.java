// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package newarray;

class NewArray {

  static class A {
    int v0;
    int v1;
    int v2;
    int v3;
    int v4;
    int v5;
    int v6;
  }

  public static void printArray(int[] array) {
    for (int i : array) System.out.println(i);
  }

  public static void test() {
    int x0[] = new int[]{};
    int x1[] = new int[]{0};
    int x2[] = new int[]{0, 1};
    int x3[] = new int[]{0, 1, 2};
    int x4[] = new int[]{0, 1, 2, 3};
    int x5[] = new int[]{0, 1, 2, 3, 4};
    int x6[] = new int[]{0, 1, 2, 3, 4, 5};
    int x7[] = new int[]{0, 1, 2, 3, 4, 5, 6};
    int x8[] = new int[]{0, 1, 2, 3, 4, 5, 6, 7};
    int x9[] = new int[]{0, 1, 0, 3, 4, 0, 6, 7};
    printArray(x0);
    printArray(x1);
    printArray(x2);
    printArray(x3);
    printArray(x4);
    printArray(x5);
    printArray(x6);
    printArray(x7);
    printArray(x8);
    printArray(x9);
  }

  public static void testIntArgs(int v0, int v1, int v2, int v3, int v4, int v5) {
    int x0[] = new int[]{};
    int x1[] = new int[]{v0};
    int x2[] = new int[]{v0, v1};
    int x3[] = new int[]{v0, v1, v2};
    int x4[] = new int[]{v0, v1, v2, v3};
    int x5[] = new int[]{v0, v1, v2, v3, v4};
    int x6[] = new int[]{v0, v1, v2, v3, v4, v5};
    int x7[] = new int[]{v0, v1, v2, v3, v4, v5, v0, v1, v0, v4, v0};
    printArray(x0);
    printArray(x1);
    printArray(x2);
    printArray(x3);
    printArray(x4);
    printArray(x5);
    printArray(x6);
    printArray(x7);
  }

  public static void testObjectArg(A a) {
    int x0[] = new int[]{};
    int x1[] = new int[]{a.v0};
    int x2[] = new int[]{a.v0, a.v1};
    int x3[] = new int[]{a.v0, a.v1, a.v2};
    int x4[] = new int[]{a.v0, a.v1, a.v2, a.v3};
    int x5[] = new int[]{a.v0, a.v1, a.v2, a.v3, a.v4};
    int x6[] = new int[]{a.v0, a.v1, a.v2, a.v3, a.v4, a.v5};
    int x7[] = new int[]{a.v0, a.v1, a.v2, a.v3, a.v4, a.v5, a.v6};
    int x8[] = new int[]{a.v0, a.v1, a.v2, a.v0, a.v3, a.v4, a.v5, a.v6};
    printArray(x0);
    printArray(x1);
    printArray(x2);
    printArray(x3);
    printArray(x4);
    printArray(x5);
    printArray(x6);
    printArray(x7);
    printArray(x8);
  }

  public static void newMultiDimensionalArrays(int n) {
    int[][] i2 = new int[n][n];
    int[][][] i3 = new int[n][n][n];
    int[][][][] i4 = new int[n][n][n][n];
    int[][][][][] i5 = new int[n][n][n][n][n];
    int[][][][][][] i6 = new int[n][n][n][n][n][n];
    System.out.println(i2.length);
    System.out.println(i3.length);
    System.out.println(i4.length);
    System.out.println(i5.length);
    System.out.println(i6.length);
  }

  public static void newMultiDimensionalArrays2(int n1, int n2, int n3, int n4, int n5, int n6) {
    int[][] i2 = new int[n1][n2];
    System.out.println(i2.length);
    int[][][] i3 = new int[n1][n2][n3];
    System.out.println(i3.length);
    int[][][][] i4 = new int[n1][n2][n3][n4];
    System.out.println(i4.length);
    int[][][][][] i5 = new int[n1][n2][n3][n4][n5];
    System.out.println(i5.length);
    int[][][][][][] i6 = new int[n1][n2][n3][n4][n5][n6];
    System.out.println(i6.length);
    int[][][][][][] i7 = new int[n1][n2][n1][n4][n5][n1];
    System.out.println(i7.length);
  }

  public static void newMultiDimensionalArrays3(int n) {
    int[][][] i3 = new int[n][n][];
    int[][][][] i4 = new int[n][n][][];
    int[][][][][][][] i7 = new int[n][n][n][n][n][n][];
    int[][][][][][][][] i8 = new int[n][n][n][n][n][n][][];
    System.out.println(i3.length);
    System.out.println(i4.length);
    System.out.println(i7.length);
    System.out.println(i8.length);
  }

  public static void newMultiDimensionalArrays4() {
    boolean[][] a1 = new boolean[1][2];
    byte[][] a2 = new byte[3][4];
    char[][] a3 = new char[5][6];
    short[][] a4 = new short[7][8];
    long[][] a5 = new long[9][10];
    float[][] a6 = new float[11][12];
    double[][] a7 = new double[13][14];
    A[][] a8 = new A[15][16];
    System.out.println(a1[0].length);
    System.out.println(a2[0].length);
    System.out.println(a3[0].length);
    System.out.println(a4[0].length);
    System.out.println(a5[0].length);
    System.out.println(a6[0].length);
    System.out.println(a7[0].length);
    System.out.println(a8[0].length);
    System.out.println(a1[0][0]);
    System.out.println(a2[0][0]);
    System.out.println(a3[0][0]);
    System.out.println(a4[0][0]);
    System.out.println(a5[0][0]);
    System.out.println(a6[0][0]);
    System.out.println(a7[0][0]);
    System.out.println(a8[0][0]);
  }

  public static void main(String[] args) {
    test();
    testIntArgs(0, 1, 2, 3, 4, 5);
    A a = new A();
    a.v0 = 0;
    a.v1 = 1;
    a.v2 = 2;
    a.v3 = 3;
    a.v4 = 4;
    a.v5 = 5;
    a.v6 = 6;
    testObjectArg(a);
    newMultiDimensionalArrays(6);
    newMultiDimensionalArrays2(1, 2, 3, 4, 5, 6);
    newMultiDimensionalArrays3(8);
    newMultiDimensionalArrays4();
  }
}