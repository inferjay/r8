// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package returns;

public class  Returns {
  public static void main(String[] args) {
    doubleJunk(); // Ignored to get pop2
    longJunk();
    longFloat();
  }

  private static double doubleJunk() {
    return 42.42;
  }

  private static long longJunk() {
    long billion5 = 5000000000L;
    return billion5;
  }

  private static float longFloat() {
    float foobar = 1.0F;
    return foobar;
  }
}
