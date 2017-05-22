// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

// This code is not run directly. It needs to be compiled to dex code.
// 'arithmetic.dex' is what is run.

package regress;

public class Regress {

  static void b33336471_int_float() {
    int i = 0;
    float f0 = 0.0f;
    float f1 = 1.0f;
    for (int j = i; j < 2; j++) {
      System.out.println("LOOP");
    }
    float f[] = new float[1];
    if (f[0] != f0) {
      System.out.println("FLOAT COMPARISON FAILED");
    }
    if (f[0] == f1) {
      System.out.println("FLOAT COMPARISON FAILED");
    }
  }

  static void b33336471_long_double() {
    long i = 0;
    double d0 = 0.0f;
    double d1 = 1.0f;
    for (long j = i; j < 2; j++) {
      System.out.println("LOOP");
    }
    double d[] = new double[1];
    if (d[0] != d0) {
      System.out.println("DOUBLE COMPARISON FAILED");
    }
    if (d[0] == d1) {
      System.out.println("DOUBLE COMPARISON FAILED");
    }
  }

  public static void main(String[] args) {
    b33336471_int_float();
    b33336471_long_double();
  }
}
