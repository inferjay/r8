// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package shaking14;

public class Shaking {

  public static void main(String[] args) {
    SubSubClass instance = new SubSubClass();
    System.out.println(Subclass.aMethod(99));
    System.out.println(instance.anotherMethod(100));
  }
}
