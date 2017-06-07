// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.rewrite.logarguments;

public class TestInstance {

  public int a() {
    return 0;
  }

  public int a(int x) {
    return x;
  }

  public int a(int x, int y) {
    return x + y;
  }

  public Object a(Object o) {
    return null;
  }

  public Object a(Object o1, Object o2) {
    return null;
  }

  public static void main(String[] args) {
    TestInstance test = new TestInstance();
    test.a();
    test.a(1);
    test.a(1, 2);
    test.a(new Object());
    test.a(null);
    test.a(new Integer(0), "");
    test.a(null, null);
  }
}
