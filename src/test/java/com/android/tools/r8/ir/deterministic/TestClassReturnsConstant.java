// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.deterministic;

import java.util.Random;

public class TestClassReturnsConstant {

  public static int a(int x) {
    return b(new Random().nextInt());
  }

  public static int b(int x) {
    a(x);
    return 1;
  }
}
