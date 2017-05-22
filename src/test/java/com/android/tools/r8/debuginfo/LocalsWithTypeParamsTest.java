// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.debuginfo;

class A<T> {
  int foo(B<T, String> b) {
    B<String, String> otherB = new B<String, String>();
    return (b.foo() + otherB.foo()) / 2;
  }
}

class B<U, V> {
  int foo() {
    return 42;
  }
}

public class LocalsWithTypeParamsTest {

  public static void main(String[] args) {
    System.out.print(new A<Class>().foo(new B<Class, String>()));
  }
}
