// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.debuginfo;

public class LocalsAtThrowTest {

  public static int localsAtThrow(int x) {
    int a = 1;
    int b = 2;
    switch (x % 3) {
      case 1:
        throw new RuntimeException();
      case 2:
        return a + b;
    }
    return 42;
  }

  public static void main(String[] args) {
    System.out.print(localsAtThrow(11));
  }
}
