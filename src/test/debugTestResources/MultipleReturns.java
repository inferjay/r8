// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

public class MultipleReturns {

  public static void main(String[] args) {
    int resultIfTrue = multipleReturns(true);
    int resultIfFalse = multipleReturns(false);
    System.out.println("resultIfTrue=" + resultIfTrue);
    System.out.println("resultIfFalse=" + resultIfFalse);
  }

  public static int multipleReturns(boolean condition) {
    if (condition) {
      return Integer.MAX_VALUE;
    } else {
      return Integer.MIN_VALUE;
    }
  }


}
