// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.code;

public abstract class Base1Format extends Instruction {

  public Base1Format(BytecodeStream stream) {
    super(stream);
  }

  protected Base1Format() {}

  public int getSize() {
    return 1;
  }
}
