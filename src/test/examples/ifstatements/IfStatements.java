// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package ifstatements;

class IfStatements {

  public static void ifNull(Object a) {
    if (a != null) {
      System.out.println("sisnotnull");
    }
    if (a == null) {
      System.out.println("sisnull");
    }
  }

  public static void ifCond(int x) {
    if (x == 0) {
      System.out.println("ifCond x == 0");
    }
    if (x != 0) {
      System.out.println("ifCond x != 0");
    }
    if (x < 0) {
      System.out.println("ifCond x < 0");
    }
    if (x >= 0) {
      System.out.println("ifCond x >= 0");
    }
    if (x > 0) {
      System.out.println("ifCond x > 0");
    }
    if (x <= 0) {
      System.out.println("ifCond x <= 0");
    }
  }

  public static void ifIcmp(int x, int y) {
    if (x == y) {
      System.out.println("ifIcmp x == y");
    }
    if (x != y) {
      System.out.println("ifIcmp x != y");
    }
    if (x < y) {
      System.out.println("ifIcmp x < y");
    }
    if (x >= y) {
      System.out.println("ifIcmp x >= y");
    }
    if (x > y) {
      System.out.println("ifIcmp x > y");
    }
    if (x <= y) {
      System.out.println("ifIcmp x <= y");
    }
  }

  public static void ifAcmp(Object a, Object b) {
    if (a == b) {
      System.out.println("ifAcmp a == b");
    }
    if (a != b) {
      System.out.println("ifAcmp a != b");
    }
  }

  public static void main(String[] args) {
    Object a = new Object();
    ifNull(a);
    ifNull(null);
    ifCond(-1);
    ifCond(0);
    ifCond(1);
    ifIcmp(-1, 1);
    ifIcmp(0, 0);
    ifIcmp(1, -1);
    ifAcmp(a, a);
    ifAcmp(a, null);
  }
}
