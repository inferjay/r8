// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

public class Test extends Throwable {
  public byte[] a;

  public Test(String s) {
    throw new RuntimeException("Test(Ljava/lang/String;");
  }

  public Test(int a, Test b) {
    throw new RuntimeException("Test(ILTest;)");
  }

  public Test() {
  }

  public long c() {
    throw new RuntimeException("Test.c()");
  }

  public Object valueAt(int i) {
    throw new RuntimeException("Test.valueAt(I)");
  }

  public void b(int i) {
    throw new RuntimeException("Test.b(I)");
  }

  public void b(byte[] a, int b, int c) {
    throw new RuntimeException("Test.b([BII)");
  }

  public void a() {
    throw new RuntimeException("Test.a()");
  }

  public void a(Test a) {
    throw new RuntimeException("Test.a(LTest;)");
  }

  public void a(Test a, int i) {
    throw new RuntimeException("Test.a(LTest;I)");
  }

  public boolean a(byte[] a, int b, int c, boolean d) {
    throw new RuntimeException("Test.a");
  }

  public void a(long a, int b, int c, int d, byte[] e) {
    throw new RuntimeException("Test.a(JIII[B)");
  }

  public void c(int i) {
    throw new RuntimeException("Test.c(I)");
  }

  public int n() {
    throw new RuntimeException("Test.n()");
  }

  public int size() {
    throw new RuntimeException("Test.size");
  }

  public static void main(String[] args) {
    try {
      new TestObject().a(new Test(), new Test());
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}
