// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

public class Test {

  public Test(int i) {
    System.out.println("Test(" + i + ")");
  }

  public static void main(String[] args) {
    TestObject.allocate(0);
    TestObject.allocate(4);
    TestObject.allocate(10);
  }
}
