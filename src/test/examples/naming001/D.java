// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package naming001;

public class D {
  public void keep() {
    System.out.println();
  }

  public static void main(String[] args) {
    D d = new E();
    d.keep();
  }

  public static void main2(String[] args) {
    D d = new D();
    d.keep();
  }
}

