// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package naming001;

public class A {

  A() {
  }

  A(int i) {
  }

  static {
    C.m();
  }

  void m() {
  }

  @SuppressWarnings("unused")
  private void privateFunc() {
  }
}

