// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package shaking2;

public abstract class SuperClass implements Interface {
  public int used;

  public static void staticMethod() {
    System.out.println("SuperClass::staticMethod");
  }

  public static void staticMethod2(int a1, int a2, int a3, int a4, int a5, int a6, int a7, int a8) {
    System.out.println("SuperClass::staticMethod2: " + (a1 + a2 + a3 + a4 + a5 + a6 + a7 + a8));
  }

  public static void unusedStaticMethod() {
    System.out.println("SuperClass::unusedStaticMethod");
  }

  public void virtualMethod() {
    System.out.println("SuperClass::virtualMethod");
  }

  public void virtualMethod2(int a1, int a2, int a3, int a4, int a5, int a6, int a7, int a8) {
    System.out.println("SuperClass::virtualMethod2 " + (a1 + a2 + a3 + a4 + a5 + a6 + a7 + a8));
  }

  public void virtualMethod3() {
    System.out.println("SuperClass::virtualMethod3");
  }

  public void virtualMethod4(int a1, int a2, int a3, int a4, int a5, int a6, int a7, int a8) {
    System.out.println("SuperClass::virtualMethod4 " + (a1 + a2 + a3 + a4 + a5 + a6 + a7 + a8));
  }

  public void unusedVirtualMethod() {
    System.out.println("SuperClass::unusedVirtualMethod");
  }

  public void unusedInterfaceMethod() { System.out.println("SuperClass::unusedInterfaceMethod"); }

  public void accessFields() {
    System.out.println("SuperClass::fields: " + used);
  }
}
