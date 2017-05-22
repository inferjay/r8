// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.debuginfo;

public class LocalsInSwitchTest {

  public static int noLocals(int x) {
    switch (x) {
      case 0:
        return 0;
      case 1:
        return 1;
      default:
        return noLocals(x - 1) + noLocals(x - 2);
    }
  }

  public static int tempInCase(int x) {
    int res = 0;
    for (int i = 0; i < x; ++i) {
      int rem = x - i;
      switch (rem) {
        case 1:
          return res;
        case 5:
          int tmp = res + x + i;
          res += tmp;
          break;
        case 10:
          i++;
          break;
        default:
          res += rem;
      }
      res += rem % 2;
    }
    res *= x;
    return res;
  }

  public static int initInCases(int x) {
    Integer res;
    switch (x % 3) {
      case 0:
        res = 42;
      case 1:
        res = x;
      case 2:
      default:
        res = x * x;
    }
    return res + 1;
  }

  public static void main(String[] args) {
    System.out.println(noLocals(10));
    System.out.println(tempInCase(42));
    System.out.println(initInCases(123));
  }
}
