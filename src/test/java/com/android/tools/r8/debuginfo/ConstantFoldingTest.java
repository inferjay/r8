// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.debuginfo;

public class ConstantFoldingTest {

  public static int foo(int x) {
    int res = 2;
    {
      int tmp = res + 19;
      res *= tmp;
    }
    return res / 2 + x;
  }

  public static void main(String[] args) {
    System.out.print(foo(21));
  }
}
