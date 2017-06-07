// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.rewrite.logarguments;

public class TestStatic {

  public static int a() {
    return 0;
  }

  public static int a(int x) {
    return x;
  }

  public static int a(int x, int y) {
    return x + y;
  }

  public static Object a(Object o) {
    return null;
  }

  public static Object a(Object o1, Object o2) {
    return null;
  }

  public static void main(String[] args) {
    a();
    a(1);
    a(1, 2);
    a(new Object());
    a(null);
    a(new Integer(0), "");
    a(null, null);
  }
}
