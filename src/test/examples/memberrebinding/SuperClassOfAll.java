// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

// This code is not run directly. It needs to be compiled to dex code.
// 'memberrebinging.dex' is what is run.

package memberrebinding;

public class SuperClassOfAll {

  public int superField;

  public void superCallsProperlyPropagate() {
    System.out.println("Invoked superCallsProperlyPropagate on SuperClassOfAll.");
  }

  public void superCallsProperlyPropagateTwo() {
    System.out.println("Invoked superCallsProperlyPropagateTwo on SuperClassOfAll.");
  }

  private void methodThatShadowsPrivate() {
    System.out.println("methodThatShadowsPrivate on SuperClassOfAll");
  }

  public void ensureAllCalled() {
    methodThatShadowsPrivate();
  }
}