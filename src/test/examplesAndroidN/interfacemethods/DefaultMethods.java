// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package interfacemethods;

import interfacemethods.p1.I4;

public class DefaultMethods {

  interface I3 {
    default int getValue() {
      return 1;
    }

  }

  static class C3 {
    public int getValue() {
      return 2;
    }
  }

  static class C4 extends C3 implements I3 {
  }

  static class C5 implements I4 {
  }

  public static void main(String[] args) {
    new C2().d1();
    System.out.println(new C4().getValue());
    new C5().dump();
  }
}
