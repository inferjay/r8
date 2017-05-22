// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package assumenosideeffects4;

public class Assumenosideeffects {

  public static final int ASSUMED_VALUE_0 = 0;
  public static final int ASSUMED_VALUE_1 = 1;
  public static final long ASSUMED_VALUE_0L = 0;
  public static final long ASSUMED_VALUE_1L = 1;

  public static void main(String[] args) {
    System.out.println(method0());
    System.out.println(method1());
    System.out.println(method0L() + "L");
    System.out.println(method1L() + "L");
  }

  @CheckDiscarded
  public static int method0() {
    System.out.println("method0");
    return ASSUMED_VALUE_0;
  }

  @CheckDiscarded
  public static int method1() {
    System.out.println("method1");
    return ASSUMED_VALUE_1;
  }

  @CheckDiscarded
  public static long method0L() {
    System.out.println("method0L");
    return ASSUMED_VALUE_0L;
  }

  @CheckDiscarded
  public static long method1L() {
    System.out.println("method1L");
    return ASSUMED_VALUE_1L;
  }
}
