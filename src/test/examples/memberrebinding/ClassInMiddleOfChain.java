// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package memberrebinding;

public class ClassInMiddleOfChain extends SuperClassOfAll {

  @Override
  public void superCallsProperlyPropagate() {
    // Do not! call super here to break the super-chain.
    System.out.println("Invoked superCallsProperlyPropagate on ClassInMiddleOfChain.");
  }

  @Override
  public void superCallsProperlyPropagateTwo() {
    // Do not! call super here to break the super-chain.
    System.out.println("Invoked superCallsProperlyPropagateTwo on ClassInMiddleOfChain.");
  }

  public void methodThatShadowsPrivate() {
    System.out.println("methodThatShadowsPrivate on ClassInMiddleOfChain");
  }
}
