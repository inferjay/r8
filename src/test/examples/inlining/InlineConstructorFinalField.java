// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package inlining;

public class InlineConstructorFinalField {

  public final int number;

  @CheckDiscarded
  InlineConstructorFinalField(int value) {
    number = value;
  }

  // This will not be inlined, as it sets a final field.
  InlineConstructorFinalField() {
    this(42);
  }

  public String toString() {
    return "value: " + number;
  }
}
