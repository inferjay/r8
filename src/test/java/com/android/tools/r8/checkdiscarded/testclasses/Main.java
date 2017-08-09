// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.checkdiscarded.testclasses;

@WillStay
public class Main {

  public static void main(String... args) {
    UsedClass usedClass = new UsedClass();
    System.out.println(usedClass.hello());
  }
}
