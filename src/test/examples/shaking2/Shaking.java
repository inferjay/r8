// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package shaking2;

public class Shaking {
  public static void callInterfaceMethod(Interface object) {
    object.interfaceMethod4();
    object.interfaceMethod5(1, 2, 3, 4, 5, 6, 7, 8);
  }

  public static void callAsSuperClassAndInterface(SuperClass object) {
    object.interfaceMethod();
    object.interfaceMethod2();
    object.interfaceMethod3();
    object.virtualMethod();
    object.virtualMethod2(1, 2, 3, 4, 5, 6, 7, 8);
    object.accessFields();
    callInterfaceMethod(object);
  }

  public static void accessStaticFields() {
    System.out.println("StaticFields::used: " + StaticFields.used);
    System.out.println("StaitcFields::read" +
        " " + StaticFields.readInt +
        " " + StaticFields.readBoolean+
        " " + StaticFields.readByte +
        " " + StaticFields.readChar +
        " " + StaticFields.readObject +
        " " + StaticFields.readShort +
        " " + StaticFields.readDouble);
    StaticFields.writeInt = 1;
    StaticFields.writeBoolean = true;
    StaticFields.writeByte = 2;
    StaticFields.writeChar = 3;
    StaticFields.writeObject = new Object();
    StaticFields.writeShort = 3;
    StaticFields.writeDouble = 3.3;
  }

  public static void main(String[] args) {
    accessStaticFields();
    SuperClass.staticMethod();
    SuperClass.staticMethod2(1, 2, 3, 4, 5, 6, 7, 8);
    SubClass1 instance1 = new SubClass1(1);
    callAsSuperClassAndInterface(instance1);
    instance1.virtualMethod3();
    instance1.virtualMethod4(1, 2, 3, 4, 5, 6, 7, 8);
    callAsSuperClassAndInterface(new SubClass1(1, 2, 3, 4, 5, 6, 7, 8));
    SubClass2 instance2 = new SubClass2(2);
    callAsSuperClassAndInterface(instance2);
    instance2.virtualMethod3();
    instance2.virtualMethod4(1, 2, 3, 4, 5, 6, 7, 8);
  }
}
