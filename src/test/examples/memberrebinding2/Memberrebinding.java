// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package memberrebinding2;

import memberrebinding2.subpackage.PublicClass;

public class Memberrebinding {

  public static void main(String[] args) {
    ClassAtBottomOfChain bottomInstance = new ClassAtBottomOfChain();
    PublicClass instance = new PublicClass();

    int x = 0;

    bottomInstance.bottomField = 1;
    bottomInstance.middleField = 2;
    bottomInstance.superField = 3;
    instance.field = 4;

    bottomInstance.staticBottomField = 5;
    bottomInstance.staticMiddleField = 6;
    bottomInstance.staticSuperField = 7;
    instance.staticField = 8;

    x += bottomInstance.bottomField;
    x += bottomInstance.middleField;
    x += bottomInstance.superField;
    x += instance.field;

    x += bottomInstance.staticBottomField;
    x += bottomInstance.staticMiddleField;
    x += bottomInstance.staticSuperField;
    x += instance.staticField;

    System.out.println(x);
  }
}
