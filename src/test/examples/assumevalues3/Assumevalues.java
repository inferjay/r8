// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package assumevalues3;

public class Assumevalues {

  public static final int ASSUMED_VALUE = 1;
  public static final long ASSUMED_VALUEL = 1;

  public static int value = 2;
  public static long valueL = 2;

  public static void main(String[] args) {
    value = 3;
    if (value == 1) {
      System.out.println("1");
    }
    if (value == 2) {
      System.out.println("2");
    }
    if (value == 3) {
      System.out.println("3");
    }

    valueL = 3;
    if (valueL == 1) {
      System.out.println("1L");
    }
    if (valueL == 2) {
      System.out.println("2L");
    }
    if (valueL == 3) {
      System.out.println("3L");
    }
  }
}
