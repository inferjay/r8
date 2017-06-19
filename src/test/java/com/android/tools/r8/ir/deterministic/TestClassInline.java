// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.deterministic;

public class TestClassInline {

  public static boolean alwaysFalse() {
    return false;
  }

  public static int a() {
    return b();
  }

  public static int b() {
    if (alwaysFalse()) {
      a();
      return 0;
    }
    return 1;
  }
}
