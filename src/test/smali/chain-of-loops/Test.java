// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

public class Test {
  public int b(int i, String s) {
    throw new RuntimeException("b(ILjava/lang/String;)");
  }

  public int b(Test t) {
    throw new RuntimeException("b(LTest;)");
  }

  public static int f(int i0, int i1) {
    throw new RuntimeException("f(II)");
  }

  public static void main(String[] args) {
    try {
      new TestObject().method();
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}
