// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package regress_37658666;

class Float {
  int cmp(float a, float b) {
    if (a > b)
      return 1;
    if (a == b)
      return 0;
    return -1;
  }
}

public class Regress {

  public static boolean check(int i, int j) {
    return i == j;
  }

  public static void main(String[] args) {
    Float f = new Float();
    System.out.println(check(0, f.cmp(+0f, -0f)));
  }
}