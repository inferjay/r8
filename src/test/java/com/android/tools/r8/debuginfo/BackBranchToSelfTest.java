// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.debuginfo;

public class BackBranchToSelfTest {

  public static int backBranchToSelf(boolean loop) {
    do {
      if (loop)
        continue;
    }
    while (loop);
    return 42;
  }

  public static void main(String[] args) {
    System.out.print(backBranchToSelf(false));
  }
}
