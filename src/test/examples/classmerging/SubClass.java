// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package classmerging;

public class SubClass extends SuperClass {

  private int field;

  public SubClass(int field) {
    this(field, field + 100);
  }

  public SubClass(int one, int other) {
    super(one);
    field = other;
  }

  public String toString() {
    return "is " + field + " " + getField();
  }
}
