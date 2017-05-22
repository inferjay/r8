// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

public class Test {

  public boolean b = false;
  public Test a = null;

  public Test() {
  }

  public Test(int i, Test a) {
    throw new RuntimeException("Test(ILTest;)");
  }

  public int nextIndex() {
    throw new RuntimeException("nextIndex()");
  }

  public int previousIndex() {
    throw new RuntimeException("previousIndex()");
  }

  public boolean hasNext() {
    throw new RuntimeException("hasNext()");
  }

  public boolean hasPrevious() {
    throw new RuntimeException("hasPrevious()");
  }

  public static void main(String[] args) {
    try {
      new TestObject().a(new Test(), new Test());
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}
