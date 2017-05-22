// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package rewrite;

public class LongCompare {

  public static int simpleCompare(long l1, long l2) {
    try {
      return Long.compare(l1, l2);
    } catch (Throwable t) {
      System.out.println(t);
    }
    return 2;
  }

  public static long getValue1() {
    return 123456789L;
  }

  public static long getValue2() {
    return 0;
  }

  public static boolean complexCompare(long l1, long l2) {
    return Long.compare(getValue1(), l1) == 0 && Long.compare(l2, getValue2()) > 0;
  }

  public static void main(String[] args) {
    System.out.println(simpleCompare(123456789L, 987654321L));
    System.out.println(simpleCompare(Long.MAX_VALUE, 0L));
    System.out.println(simpleCompare(Long.MIN_VALUE, 0L));
    System.out.println(simpleCompare(Long.MAX_VALUE, Long.MAX_VALUE));

    System.out.println(complexCompare(123456789L, 1));
    System.out.println(complexCompare(123456789L, -1));
    System.out.println(complexCompare(1234567890L, 1));
    System.out.println(complexCompare(1234567890L, -1));
  }
}
