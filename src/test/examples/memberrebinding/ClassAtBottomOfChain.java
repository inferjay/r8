// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package memberrebinding;

public class ClassAtBottomOfChain extends EssentiallyEmptyClass {

  @Override
  public void superCallsProperlyPropagate() {
    System.out.println("Try invoke on super, aka ClassInMiddleOfChain");
    super.superCallsProperlyPropagate();
  }

  public void methodThatCallsSuperCallsProperlyPropagateTwo() {
    // Invoke the method on the superclass even though this class does not override it.
    super.superCallsProperlyPropagateTwo();
  }

  // Method with same name but different signature to test lookup.
  public void methodThatShadowsPrivate(int ignore) {

  }
}
