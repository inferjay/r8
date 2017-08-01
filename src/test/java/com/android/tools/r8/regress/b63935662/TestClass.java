// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.regress.b63935662;

public class TestClass {

  interface Top {
    default String name() { return "unnamed"; }
  }

  interface Left extends Top {
    default String name() { return getClass().getName(); }
  }

  interface Right extends Top {
    /* No override of default String name() */
  }

  interface Bottom extends Left, Right {}

  static class X1 implements Bottom {
    void test() {
      System.out.println(name());
    }
  }

  static class X2 implements Left, Right  {
    void test() {
      System.out.println(name());
    }
  }

  static class X3 implements Right, Left   {
    void test() {
      System.out.println(name());
    }
  }

  static class X4 implements Left, Right, Top  {
    void test() {
      System.out.println(name());
    }
  }

  static class X5 implements Right, Left, Top   {
    void test() {
      System.out.println(name());
    }
  }

  public static void main(String[] args) {
    new X1().test();
    new X2().test();
    new X3().test();
    new X4().test();
    new X5().test();
  }
}
