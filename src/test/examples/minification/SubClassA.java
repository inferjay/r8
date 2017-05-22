// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package minification;

public class SubClassA extends SuperClassA {

  @Override
  public int functionFromIntToInt(int arg) {
    System.out.println("SubClassA:functionFromIntToInt(A)");
    return arg;
  }

  @Override
  public int uniqueLittleMethodInA() {
    System.out.println("SubClassA:uniqueLittleMethodInA");
    return 0;
  }
}
