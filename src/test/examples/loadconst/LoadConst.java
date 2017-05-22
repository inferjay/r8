// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package loadconst;

public class LoadConst {

  public static void main(String[] args) {
    // ldc int
    System.out.print(0x7fffffff);
    // ldc float
    System.out.print(1234.5);
    // ldc string
    System.out.println("foobar");
    // ldc const class
    System.out.println(LoadConst.class);
  }
}
