// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package assumenosideeffects3;

public class Assumenosideeffects {

  public static void main(String[] args) {
    System.out.println(method0());
    System.out.println(method1());
    System.out.println(method0L() + "L");
    System.out.println(method1L() + "L");
  }

  @CheckDiscarded
  public static int method0() {
    return 0;
  }

  @CheckDiscarded
  public static int method1() {
    return 1;
  }

  @CheckDiscarded
  public static long method0L() {
    return 0;
  }

  @CheckDiscarded
  public static long method1L() {
    return 1;
  }
}
