// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

public class Test {

  public int getId() {
    throw new RuntimeException("getId()I");
  }

  public boolean a() {
    throw new RuntimeException("a()Z");
  }

  public void a(Test t0) {
    throw new RuntimeException("a(LTest;)V");
  }

  public void a(Test t0, Test t1) {
    throw new RuntimeException("a(LTest;LTest;)V");
  }

  public static boolean b(Test t0, Test t1) {
    throw new RuntimeException("b()Z");
  }

  public Test c() {
    throw new RuntimeException("c()LTest;");
  }

  public static boolean c(Test t) {
    throw new RuntimeException("c(LTest;)Z");
  }

  public void g() {
    throw new RuntimeException("g()V");
  }

  public boolean pageScroll(int i) {
    throw new RuntimeException("pageScroll(I)Z");
  }

  public static void main(String[] args) {
    try {
      new TestObject().onClick(new Test());
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}
