// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package shaking3;

@shaking3.UsedByReflection
class A implements Comparable<A>, AnInterfaceWithATag {

  @shaking3.RandomTag
  public A() {
    // Intentionally left empty.
  }

  @Override
  public String toString() {
    return "A";
  }

  public void unused() { }

  @Override
  public int compareTo(A other) {
    if (other == this) {
      return 0;
    }
    return 1;
  }
}
