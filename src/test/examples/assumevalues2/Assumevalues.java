// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package assumevalues2;

public class Assumevalues {

  public static int value = 2;
  public static long valueL = 2;

  public static void main(String[] args) {
    value = 1;
    if (value == 1) {
      method1();
    }
    value = 2;
    if (value == 2) {
      method2();
    }
    value = 3;
    if (value == 3) {
      method3();
    }
    value = 4;
    if (value == 4) {
      method4();
    }

    valueL = 1;
    if (valueL == 1) {
      method1L();
    }
    valueL = 2;
    if (valueL == 2) {
      method2L();
    }
    valueL = 3;
    if (valueL == 3) {
      method3L();
    }
    valueL = 4;
    if (valueL == 4) {
      method4L();
    }
  }

  @CheckDiscarded
  public static void method1() {
    System.out.println("1");
  }

  public static void method2() {
    System.out.println("2");
  }

  public static void method3() {
    System.out.println("3");
  }

  @CheckDiscarded
  public static void method4() {
    System.out.println("4");
  }

  @CheckDiscarded
  public static void method1L() {
    System.out.println("1L");
  }

  public static void method2L() {
    System.out.println("2L");
  }

  public static void method3L() {
    System.out.println("3L");
  }

  @CheckDiscarded
  public static void method4L() {
    System.out.println("4L");
  }
}
