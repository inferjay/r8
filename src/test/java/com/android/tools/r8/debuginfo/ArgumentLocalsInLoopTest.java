// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.debuginfo;

public class ArgumentLocalsInLoopTest {

  public int foo(int x) {
    while (true) {
      if (x <= 0) {
        return x;
      }
      --x;
    }
  }

  public static void main(String[] args) {
    System.out.print(new ArgumentLocalsInLoopTest().foo(42));
  }
}
