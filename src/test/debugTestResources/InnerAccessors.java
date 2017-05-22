// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

public class InnerAccessors {

  private static void privateMethod() {
    System.out.println("I'm a private method");
  }

  static class Inner {
    public void callPrivateMethodInOuterClass() {
      privateMethod();
    }
  }

  public static void main(String[] args) {
    new Inner().callPrivateMethodInOuterClass();
  }

}
