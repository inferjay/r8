// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.debuginfo;

public class ScopedExceptionsTest {

  private static int scopedExceptions() {
    try {
      throwNPE();
    }
    catch (NullPointerException e) {}
    catch (Throwable e) {
      System.out.println("Unexpected...");
    }
    return 42;
  }

  private static void throwNPE() {
    throw new NullPointerException();
  }

  public static void main(String[] args) {
    System.out.print(scopedExceptions());
  }
}
