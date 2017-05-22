// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

// This code is not run directly. It needs to be compiled to dex code.
// 'trycatch.dex' is what is run.

package trycatch;

class TryCatch {


  private static class Thrower {

    private boolean shouldThrow;

    public Thrower(boolean shouldThrow) {
      this.shouldThrow = shouldThrow;
    }

    public void maybeThrow() {
      if (shouldThrow) {
        throw new RuntimeException();
      }
    }
  }

  private static void throwOrCatch(Thrower thrower) {
    String firstJunk = "junk 1";
    String result = "Did not throw";
    String secondJunk = "junk 2";
    try {
      thrower.maybeThrow();
    } catch (Throwable e) {
      String tmp = secondJunk;
      secondJunk = firstJunk;
      result = "Did throw";
      firstJunk = tmp;
    }
    System.out.println(result);
    System.out.println(firstJunk);
    System.out.println(secondJunk);
  }

  private static void throwOnPositive(int i) {
    if (i > 0) {
      throw new RuntimeException();
    }
  }

  private static int loopWhileThrow(int i) {
    while (true) {
      int result = 100;
      try {
        throwOnPositive(i--);
        return result;
      } catch (Throwable e) {
      }
    }
  }

  private static void foo() {
    try {
      throw new RuntimeException();
    } catch (IllegalStateException e) {
      System.out.println("Error!");
    }
  }

  private static String tryCatchPhi(int i) {
    String result = "one";
    String otherResult = "two";
    try {
      throwOnPositive(i++);
      result = otherResult;
      throwOnPositive(i);
    } catch (Throwable e) {
      return result;
    }
    return "three";
  }

  private static void emptyMethod(int x) {
  }

  private static int regressRemoveCatchHandlers(int a, int b) {
    try {
      if (a == b) {
        emptyMethod(a);
        return 0;
      } else {
        emptyMethod(a);
        return a / b;
      }
    } catch (IllegalStateException | ArithmeticException e) {
      System.out.println("Error!");
    }
    return -1;
  }

  static final int NUM_OF_EVENTS_PER_FILE = 8192;

  public static Object regressMultiNewArray() {
    try {
      int[][] args = new int[3][NUM_OF_EVENTS_PER_FILE];
      return args;
    } catch (Exception e) {
      return null;
    }
  }

  // Create a class hierarchy which introduces a bridge method (B::x returning Object) by
  // overriding a method (A::x) with one with a narrower type.
  public static class A {

    public Object x() {
      return null;
    }
  }

  public static class B extends A {

    // This bridge method will be generated.
    // public Object x() {
    //   return result of call "String x()"
    //}

    public String x() {
      return null;
    }
  }

  static B b = new B();

  public static Object regressBridgeMethod() {
    String s;
    try {
      s = b.x();
    } catch (ClassCastException e) {
      return null;
    }
    return s;
  }

  public static void main(String[] args) {
    loopWhileThrow(-100);
    throwOrCatch(new Thrower(false));
    throwOrCatch(new Thrower(true));
    System.out.println(tryCatchPhi(-1));
    System.out.println(tryCatchPhi(0));
    System.out.println(tryCatchPhi(1));
    try {
      foo();
    } catch (RuntimeException e) {
      System.out.print("Success!");
    }
    regressRemoveCatchHandlers(1, 0);
    regressMultiNewArray();
    regressBridgeMethod();
  }
}