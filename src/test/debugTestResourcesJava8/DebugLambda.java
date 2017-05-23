// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

public class DebugLambda {

  interface I {
    int getInt();
  }

  private static void printInt(I i) {
    System.out.println(i.getInt());
  }

  public static void testLambda(int i, int j) {
    printInt(() -> i + j);
  }

  public static void main(String[] args) {
    DebugLambda.testLambda(5, 10);
  }

}
