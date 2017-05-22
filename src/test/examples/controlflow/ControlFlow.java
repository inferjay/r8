// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

// This code is not run directly. It needs to be compiled to dex code.
// 'controlflow.dex' is what is run.

package controlflow;

public class ControlFlow {

  public static void simpleIf(boolean b) {
    String s = "Hep!";
    if (b) {
      s = "Fisk";
    } else {
      s = "Hest";
    }
    System.out.println(s);
  }

  public static void simpleIfMoreValues(boolean b) {
    int i = 0;
    double d = 0.0;
    String s = "Hep!";
    if (b) {
      i = 1;
      d = 1.1;
      s = "Fisk";
      b = false;
    } else {
      i = 2;
      d = 2.2;
      s = "Hest";
    }
    if (i == 1) {
      b = true;
    }
    System.out.println(s + " " + i + " " + d + " " + b);
  }

  public static void simpleIfFallthrough(boolean b) {
    String s = "Hep!";
    if (b) {
      s = "Fisk";
    }
    System.out.println(s);
  }

  public static void sequenceOfIfs(int i) {
    if (i < 10) {
      System.out.println("10");
    }
    if (i < 5) {
      System.out.println("5");
    }
    if (i < 2) {
      System.out.println("2");
    }
  }

  public static void nestedIfs(int i) {
    if (i < 10) {
      System.out.println("10");
      if (i < 5) {
        System.out.println("5");
        if (i < 2) {
          System.out.println("2");
        }
      }
    }
  }

  public static void simpleLoop(int count) {
    System.out.println("simpleLoop");
    for (int i = 0; i < count; i++) {
      System.out.println("count: " + i);
    }
  }

  public static void main(String[] args) {
    simpleIf(true);
    simpleIf(false);
    simpleIfMoreValues(true);
    simpleIfMoreValues(false);
    simpleIfFallthrough(true);
    simpleIfFallthrough(false);
    sequenceOfIfs(10);
    sequenceOfIfs(9);
    sequenceOfIfs(4);
    sequenceOfIfs(1);
    nestedIfs(10);
    nestedIfs(9);
    nestedIfs(4);
    nestedIfs(1);
    simpleLoop(0);
    simpleLoop(1);
    simpleLoop(10);
  }
}
