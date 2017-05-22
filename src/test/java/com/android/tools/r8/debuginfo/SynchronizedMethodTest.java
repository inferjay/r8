// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.debuginfo;

public class SynchronizedMethodTest {

  private static synchronized int syncStatic(int x) {
    if (x % 2 == 0) {
      return 42;
    }
    return -Math.abs(x);
  }

  private synchronized int syncInstance(int x) {
    if (x % 2 == 0) {
      return 42;
    }
    return -Math.abs(x);
  }

  public static void main(String[] args) {
    System.out.println(syncStatic(1234));
    System.out.println(new SynchronizedMethodTest().syncInstance(1234));
  }
}
