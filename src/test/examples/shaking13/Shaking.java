// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package shaking13;

import shakinglib.LibraryClass;

public class Shaking {

  private static void fieldTest() {
    AClassWithFields instance = new AClassWithFields();
    instance.intField = 1;
    AClassWithFields.staticIntField = 2;

    LibraryClass.staticIntField = 3;
    LibraryClass libraryInstance = new LibraryClass();
    libraryInstance.intField = 4;
  }

  public static void main(String[] args) {
    fieldTest();
  }
}
