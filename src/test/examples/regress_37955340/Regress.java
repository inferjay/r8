// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package regress_37955340;

public class Regress {

  static class A {

    int x(int a) {
      return a;
    }
  }

  static class B extends A {

    int x(int a) {
      return 2;
    }
  }

  public static void main(String[] args) {
    A a = new B();
    System.out.println(a.x(1));
  }
}
