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

  public static void main(String[] args) {
    noLocals();
    unusedLocals();
    constantLocals(10);
    zeroLocals();
    noFlowOptimization();
  }

}
