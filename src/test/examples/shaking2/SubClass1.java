// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package shaking2;

public class SubClass1 extends SuperClass {
  private int used;
  private int alsoUsed;
  private boolean usedBoolean;
  private byte usedByte;
  private char usedChar;
  private Object usedObject;
  private short usedShort;
  private double usedDouble;

  private int unused;

  public SubClass1(int used) {
    this.used = used;
  }

  public SubClass1(int a1, int a2, int a3, int a4, int a5, int a6, int a7, int a8) {
    this.used = a1 + a2 + a3 + a4 + a5 + a6 + a7 + a8;
  }

  @Override
  public void virtualMethod() {
    System.out.println("SubClass1::virtualMethod");
  }

  @Override
  public void virtualMethod2(int a1, int a2, int a3, int a4, int a5, int a6, int a7, int a8) {
    System.out.println("SubClass1::virtualMethod2 " + (a1 + a2 + a3 + a4 + a5 + a6 + a7 + a8));
  }

  @Override
  public void virtualMethod3() {
    super.virtualMethod3();
    System.out.println("SubClass1::virtualMethod3");
  }

  @Override
  public void virtualMethod4(int a1, int a2, int a3, int a4, int a5, int a6, int a7, int a8) {
    super.virtualMethod4(a1, a2, a3, a4, a5, a6, a7, a8);
    System.out.println("SubClass1::virtualMethod4 " + (a1 + a2 + a3 + a4 + a5 + a6 + a7 + a8));
  }

  @Override
  public void unusedVirtualMethod() {
    System.out.println("SubClass1::unusedVirtualMethod");
  }

  @Override
  public void interfaceMethod() {
    System.out.println("SubClass1::interfaceMethod");
  }

  @Override
  public void interfaceMethod2() {
    System.out.println("SubClass1::interfaceMethod2");
  }

  @Override
  public void interfaceMethod3() {
    System.out.println("SubClass1::interfaceMethod3");
  }

  @Override
  public void interfaceMethod4() {
    System.out.println("SubClass1::interfaceMethod4");
  }

  @Override
  public void interfaceMethod5(int a1, int a2, int a3, int a4, int a5, int a6, int a7, int a8) {
    System.out.println("SubClass1::interfaceMethod5 " + (a1 + a2 + a3 + a4 + a5 + a6 + a7 + a8));
  }

  @Override
  public void accessFields() {
    super.accessFields();
    System.out.println("SubClass1::fields: " + used + " " + alsoUsed + " " + usedBoolean +
        " " + usedByte + " " + usedChar + " " + usedObject + " " + usedShort + " " + usedDouble);
    used = 1;
    alsoUsed = 2;
    usedBoolean = true;
    usedByte = 3;
    usedChar = '?';
    usedObject = new Object();
    usedShort = 4;
    usedDouble = 42.42;
  }
}
