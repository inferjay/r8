// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package shaking15;

public class Superclass {

  public void aMethod() {
    System.out.println("Called aMethod in Superclass");
  }

  public void callingSuper() {
    System.out.println("Called callingSuper in Superclass");
  }
}
