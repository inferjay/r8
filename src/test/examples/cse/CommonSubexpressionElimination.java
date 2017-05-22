// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

// This code is not run directly. It needs to be compiled to dex code.
// 'arithmetic.dex' is what is run.

package cse;

public class CommonSubexpressionElimination {

  public static int divNoCatch(int a, int b, int c) {
    int d = c / (a - b);
    System.out.println(d);
    return c / (a - b);
  }

  public static int divNoCatch2(int a, int b, int c) {
    int d = c / (a - b);
    int e = c / (a - b);
    System.out.println(d + " " + e);
    return c / (a - b);
  }

  public static int divCatch(int a, int b, int c) {
    try {
      int d = c / (a - b);
      System.out.println(d);
      return d;
    } catch (Throwable t) {
      return c / (a - b);
    }
  }

  public static int divCatch2(int a, int b, int c) {
    try {
      int d = c / (a - b);
      int e = c / (a - b);
      System.out.println(d + " " + e);
      return d;
    } catch (Throwable t) {
      return c / (a - b);
    }
  }

  public static String divCatchCatch(int a, int b, int c) {
    try {
      int d = c / (a - b);
      System.out.println(d);
      return "X";
    } catch (Throwable t) {
      try {
        return "" + c / (a - b);
      } catch (Throwable t2) {
        return "A";
      }
    }
  }

  public static String divCatchSharedCatchHandler(int a, int b, int c) {
    try {
      int d = c / (a - b);
      System.out.println(d);
      if (a == 0) {
        int e = c / (a - b);
        System.out.println(e);
      } else {
        int f = c / (a - b);
        System.out.println(f);
      }
      return "X";
    } catch (Throwable t) {
      return "B";
    }
  }


  public static void main(String[] args) {
    System.out.println(divNoCatch(1, 0, 1));
    System.out.println(divNoCatch2(1, 0, 2));
    System.out.println(divCatch(1, 0, 3));
    System.out.println(divCatch2(1, 0, 4));

    System.out.println(divCatchCatch(0, 0, 1));
    System.out.println(divCatchSharedCatchHandler(0, 0, 1));

    try {
      divNoCatch(0, 0, 1);
      throw new RuntimeException("UNEXPECTED");
    } catch (ArithmeticException e) {
      // Expected "divide by zero".
    }

    try {
      divNoCatch2(0, 0, 1);
      throw new RuntimeException("UNEXPECTED");
    } catch (ArithmeticException e) {
      // Expected "divide by zero".
    }

    try {
      divCatch(0, 0, 1);
      throw new RuntimeException("UNEXPECTED");
    } catch (ArithmeticException e) {
      // Expected "divide by zero".
    }

    try {
      divCatch2(0, 0, 1);
      throw new RuntimeException("UNEXPECTED");
    } catch (ArithmeticException e) {
      // Expected "divide by zero".
    }
  }
}
