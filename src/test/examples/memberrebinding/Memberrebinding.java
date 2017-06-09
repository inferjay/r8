// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package memberrebinding;

import memberrebinding.subpackage.PublicClass;
import memberrebindinglib.AnIndependentInterface;

public class Memberrebinding {

  public static void main(String[] args) {
    ClassAtBottomOfChain bottomInstance = new ClassAtBottomOfChain();
    bottomInstance.superCallsProperlyPropagate();
    bottomInstance.methodThatCallsSuperCallsProperlyPropagateTwo();
    bottomInstance.methodThatShadowsPrivate();
    bottomInstance.ensureAllCalled();
    System.out.println(bottomInstance.superField);
    ClassExtendsLibraryClass classExtendsLibraryClass = new ClassExtendsLibraryClass();
    classExtendsLibraryClass.methodThatAddsHelloWorld();
    classExtendsLibraryClass.methodThatAddsHelloWorldUsingAddAll();
    System.out.println(classExtendsLibraryClass.get(0));
    System.out.println(classExtendsLibraryClass.get(1));
    System.out.println(classExtendsLibraryClass.get(2));
    PublicClass instance = new PublicClass();
    instance.aMethod();
    PublicClass.aStaticMethod();
    ClassExtendsOtherLibraryClass classExtendsOther = new ClassExtendsOtherLibraryClass();
    System.out.println(classExtendsOther.aMethodThatReturnsOne());
    System.out.println(classExtendsOther.aMethodThatReturnsTwo());
    System.out.println(classExtendsOther.aMethodThatReturnsThree());
    System.out.println(classExtendsOther.aMethodThatReturnsFour());
    AnIndependentInterface iface = classExtendsOther;
    System.out.println(iface.aMethodThatReturnsTwo());
    SuperClassOfClassExtendsOtherLibraryClass superClass = classExtendsOther;
    System.out.println(superClass.aMethodThatReturnsTrue());
    System.out.println(superClass.aMethodThatReturnsFalse());
  }
}
