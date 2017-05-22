// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package invoke;

public class SuperClass {
  public void super0() {
    System.out.println("super0");
  }

  public void super1(int a) {
    System.out.println("super1 " + a);
  }

  public void super2(int a, int b) {
    System.out.println("super2 " + a + " " + b);
  }

  public void super3(int a, int b, int c) {
    System.out.println("super3 " + a + " " + b + " " + c);
  }

  public void super4(int a, int b, int c, int d) {
    System.out.println("super4 " + a + " " + b + " " + c + " " + d);
  }

  public void super5(int a, int b, int c, int d, int e) {
    System.out.println("super5 " + a + " " + b + " " + c + " " + d + " " + e);
  }

  public void superRange(int a, int b, int c, int d, int e, int f) {
    System.out.println("superRange " + a + " " + b + " " + c + " " + d + " " + e + " " + f);
  }
}
