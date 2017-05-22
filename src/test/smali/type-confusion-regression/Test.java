// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

import java.util.ArrayList;
import java.util.List;

public class Test extends Throwable {
  public long[] al = { 1, 2, 3 };
  public int[] ai = null;
  public boolean b = false;
  public List a = new ArrayList();
  public long l = 1 << 53;
  public int i = 32;
  public double d = 0.123;
  public Test h = null;

  public Test(int i) {
    throw new RuntimeException("Test(i)");
  }

  public Test(int i, int j) {
    throw new RuntimeException("Test.<init>(II)");
  }

  public Test(String s) {
    throw new RuntimeException("Test.<init>(Ljava/lang/String;)");
  }

  public Test() {
    throw new RuntimeException("Test.<init>()");
  }

  public static int a(Test t) {
    throw new RuntimeException("Test.a(Test)");
  }

  public Test e() {
    throw new RuntimeException("Test.e()");
  }

  public static void main(String[] args) {
    try {
      TestObject.a(new Test(), new Test(), new Test(), new Test());
    } catch (RuntimeException e) {
      System.out.println(e);
    }
  }
}
