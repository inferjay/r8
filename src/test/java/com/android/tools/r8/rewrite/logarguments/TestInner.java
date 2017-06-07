// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.rewrite.logarguments;

public class TestInner {

  public class Inner {

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
  }

  private Inner createInner() {
    return new Inner();
  }

  public static void main(String[] args) {
    Inner inner = new TestInner().createInner();
    inner.a();
    inner.a(1);
    inner.a(1, 2);
    inner.a(new Object());
    inner.a(null);
    inner.a(new Integer(0), "");
    inner.a(null, null);
  }
}
