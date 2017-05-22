// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

public class Test {
  boolean returnBoolean() {
    System.out.println("returnBoolean");
    return true;
  }

  boolean returnTheOtherBoolean(Test a) {
    System.out.println("returnTheOtherBoolean");
    return a.returnBoolean();
  }

  public static void main(String[] args) {
    new TestObject().a(new Test(), 42);
  }
}
