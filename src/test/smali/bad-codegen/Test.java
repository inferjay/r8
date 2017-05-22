// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

import java.util.Map;

public class Test {
  public Test a;
  public static Test P;

  public Test() {
  }

  public Test(int a, Test b, Test c, Long d, Test e, Map f) {
  }

  public Test(int i1, int i2, int i3) {
  }

  public long b() {
    return 0;
  }

  public boolean c() {
    return false;
  }

  public boolean d() {
    return false;
  }

  public static void main(String[] args) {
    Test test = new Test();
    try {
      new TestObject().a(test, test, test, test, true);
    } catch (Throwable t) {
      System.out.println(t);
    }
  }
}
