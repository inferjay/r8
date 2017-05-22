// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

public class Test {

  public Object b;
  public Test d;
  public Test e;
  public Test g;
  public Test f;

  public void a() { throw new RuntimeException("Test.a()"); }
  public void a(long p1) { throw new RuntimeException("Test.a(long)"); }
  public void a(long p1, boolean p2) { throw new RuntimeException("Test.a(long, boolean)"); }
  public Test a(java.io.DataInputStream p1) { throw new RuntimeException("Test.a(input-stream)"); }
  public Test a(byte[] p1, Test p2) { throw new RuntimeException("Test.a(B[], Test)"); }
  public java.io.File b() { throw new RuntimeException("Test.b()"); }
  public Test bW_() { throw new RuntimeException("Test.bW_()"); }
  public long c() { throw new RuntimeException("Test.d()"); }
  public void c(boolean p1) { throw new RuntimeException("Test.c(boolean)"); }
  public void c(java.nio.ByteBuffer p1) { throw new RuntimeException("Test.c(byte-buf)"); }
  public byte[] cB() { throw new RuntimeException("Test.cB()"); }
  public long d() { throw new RuntimeException("Test.d()"); }
  public void d(boolean p1) { throw new RuntimeException("Test.d(boolean)"); }
  public Test e() { throw new RuntimeException("Test.e()"); }
  public void eV() { throw new RuntimeException("Test.eV()"); }

  public static void main(String[] args) {
    try {
      new TestObject().f();
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}
