// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package minification;

public class SubSubClassAB extends SubClassA implements InterfaceB {

  @Override
  public int functionFromIntToInt(int arg) {
    System.out.println("SubSubClassAB:functionFromIntToInt");
    return super.functionFromIntToInt(arg);
  }

  @Override
  public int uniqueLittleMethodInB() {
    System.out.println("SubSubClassAB:uniqueLittleMethodInB");
    return 0;
  }
}
