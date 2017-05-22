// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package classmerging;

class Outer {

  /**
   * This class is package private to trigger the generation of bridge methods
   * for the visibility change of methods from public subtypes.
   */
  class SuperClass {

    public String method() {
      return "Method in SuperClass.";
    }
  }

  public class SubClass extends SuperClass {
    // Intentionally left empty.
  }

  public SubClass getInstance() {
    return new SubClass();
  }
}
