// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package shaking8;

public class YetAnotherThing {

  private final int anInt;

  public YetAnotherThing(int anInt) {
    this.anInt = anInt;
  }

  @Override
  public String toString() {
    return "YetAnotherThing(" + anInt + ")";
  }
}
