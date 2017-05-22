// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package minification;

public class Minification {

  public static void main(String[] args) {
    SubClassA subClassA = new SubClassA();
    exerciseA(subClassA);
    SubSubClassAB subSubClassAB = new SubSubClassAB();
    exerciseA(subSubClassAB);
    exerciseB(subSubClassAB);
    SubClassB subClassB = new SubClassB();
    exerciseB(subClassB);
    SubClassC subClassC = new SubClassC();
    exerciseB(subClassC);
    exerciseC(subClassC);
    ClassD classD = new ClassD();
    exerciseD(classD);
  }

  private static void exerciseA(InterfaceA thing) {
    thing.functionFromIntToInt(thing.uniqueLittleMethodInA());
  }

  private static void exerciseB(InterfaceB thing) {
    thing.functionFromIntToInt(thing.uniqueLittleMethodInB());
  }

  private static void exerciseC(InterfaceC thing) {
    thing.functionFromIntToInt(thing.uniqueLittleMethodInC());
  }

  private static void exerciseD(InterfaceD thing) {
    thing.anotherFunctionFromIntToInt(42);
    thing.functionFromIntToInt(42);
  }
}
