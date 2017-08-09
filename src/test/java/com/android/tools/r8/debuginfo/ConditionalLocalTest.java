// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.debuginfo;

public class ConditionalLocalTest {

  public void foo(int x) {
    if (x % 2 != 0) {
      Integer obj = new Integer(x + x);
      long l = obj.longValue();
      x = (int) l;
      System.out.print(obj);
    }
    return;
  }

  public static void main(String[] args) {
    new ConditionalLocalTest().foo(21);
  }
}
