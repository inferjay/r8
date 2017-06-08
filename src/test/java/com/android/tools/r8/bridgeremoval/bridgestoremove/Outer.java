// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.bridgeremoval.bridgestoremove;

public class Outer {

  class SuperClass {
    public void method() { }
  }

  // As SuperClass is package private SubClass will have a bridge method for "method".
  public class SubClass extends SuperClass { }

  public SubClass create() {
    return new SubClass();
  }

  static class StaticSuperClass {
    public void method() { }
  }

  // As SuperClass is package private SubClass will have a bridge method for "method".
  public static class StaticSubClass extends StaticSuperClass { }
}