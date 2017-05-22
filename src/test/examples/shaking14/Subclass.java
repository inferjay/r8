// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package shaking14;

public class Subclass extends Superclass {
  static int aMethod(int value) {
    return value + 42;
  }

  static double anotherMethod(double value) {
    return value + 42;
  }
}
