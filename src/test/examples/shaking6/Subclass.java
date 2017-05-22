// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package shaking6;

public class Subclass extends Superclass {

  public void publicMethod() {
    // Intentionally empty.
  }

  private void privateMethod() {
    // Intentionally empty.
  }

  // Public method with same name as private method in superclass.
  public void justAMethod() {
    // Intentionally empty.
  }

  public void justAMethod(int ignore) {
    // Intentionally empty.
  }

  public void justAMethod(boolean ignore) {
    // Intentionally empty.
  }

  public int justAMethod(double ignore) {
    // Intentionally empty.
    return 0;
  }

  final void aFinalMethod() {
    // Intentionally empty.
  }
}
