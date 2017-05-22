// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

public class Arithmetic {

  public static void main(String[] args) {
    bitwiseInts(12345, 54321);
  }

  public static void bitwiseInts(int x, int y) {
    System.out.println(x & y);
    System.out.println(x | y);
    System.out.println(x ^ y);
    System.out.println(~x);
  }

}
