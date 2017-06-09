// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package memberrebinding4;

import memberrebinding4.subpackage.PublicInterface;

public class Memberrebinding {

  static class Inner implements PublicInterface {

  }

  public static void main(String[] args) {
    test();
  }

  public static void test() {
    new Inner().dump();
  }
}
