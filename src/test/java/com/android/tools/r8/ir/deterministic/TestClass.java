// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.deterministic;

public class TestClass {

  // This will be inlined, causing some of the calls below to go away providing more
  // inlining opportunities.
  public static boolean alwaysFalse() {
    return false;
  }

  public static int a0() {
    return b0();
  }

  public static int b0() {
    if (alwaysFalse()) {
      a0();
      return 0;
    }
    return 1;
  }

  public static int a1() {
    return b1();
  }

  public static int b1() {
    if (alwaysFalse()) {
      a1();
      return 0;
    }
    return 1;
  }

  public static int a2() {
    return b2();
  }

  public static int b2() {
    return c2();
  }

  public static int c2() {
    if (alwaysFalse()) {
      a2();
      return 0;
    }
    return 1;
  }

  public static int a3() {
    return b3();
  }

  public static int b3() {
    return c3();
  }

  public static int c3() {
    if (alwaysFalse()) {
      a3();
      return 0;
    }
    return 1;
  }
}
