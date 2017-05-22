// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package regress2;

public class Regress2 {

  static class X {

    void add() {
    }
  }

  static private boolean test() {
    X x = null;
    X y = null;

    int a = 5;
    System.out.println("START");
    while (a-- > 0) {
      System.out.println("LOOP");
      int b = 0;
      switch (b) {
        case 1:
          X current = new X();
          if (x == null) {
            x = current;
          } else {
            x = null;
          }
          y.add();
          break;
        case 2:
          if (x != null) {
            x = null;
          }
          y.add();
          break;
      }
    }
    System.out.println("END");
    return true;
  }

  public static void main(String[] args) {
    test();
  }
}
