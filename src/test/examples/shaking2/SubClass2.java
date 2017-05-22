// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package shaking2;

public class SubClass2 extends SuperClass {
  private int used;
  private int unused;
  private int unusedToo;

  public SubClass2(int used) {
    this.used = used;
  }

  @Override
  public void virtualMethod() {
    System.out.println("SubClass2::virtualMethod");
  }

  @Override
  public void virtualMethod2(int a1, int a2, int a3, int a4, int a5, int a6, int a7, int a8) {
    System.out.println("SubClass2::virtualMethod2 " + (a1 + a2 + a3 + a4 + a5 + a6 + a7 + a8));
  }

  @Override
  public void virtualMethod3() {
    super.virtualMethod3();
    System.out.println("SubClass2::virtualMethod3");
  }

  @Override
  public void virtualMethod4(int a1, int a2, int a3, int a4, int a5, int a6, int a7, int a8) {
    super.virtualMethod4(a1, a2, a3, a4, a5, a6, a7, a8);
    System.out.println("SubClass2::virtualMethod4 " + (a1 + a2 + a3 + a4 + a5 + a6 + a7 + a8));
  }

  @Override
  public void interfaceMethod() {
    System.out.println("SubClass2::interfaceMethod");
  }

  @Override
  public void interfaceMethod2() {
    System.out.println("SubClass2::interfaceMethod2");
  }

  @Override
  public void interfaceMethod3() {
    System.out.println("SubClass2::interfaceMethod3");
  }

  @Override
  public void interfaceMethod4() {
    System.out.println("SubClass2::interfaceMethod4");
  }

  @Override
  public void interfaceMethod5(int a1, int a2, int a3, int a4, int a5, int a6, int a7, int a8) {
    System.out.println("SubClass1::interfaceMethod5 " + (a1 + a2 + a3 + a4 + a5 + a6 + a7 + a8));
  }
}
