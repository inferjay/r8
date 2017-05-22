// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package rewrite;

import java.util.Objects;

public class RequireNonNull {

  public static void main(String[] args) {
    RequireNonNull o = new RequireNonNull();
    System.out.println(o.nonnullRemove().toString());
    System.out.println(o.nonnullRemove(o).toString());
    o.nonnullWithPhiInstruction(true, o);
    o.nonnullWithPhiInstruction(false, o);
  }

  private Object nonnullRemove() {
    return Objects.requireNonNull(this);
  }

  private Object nonnullRemove(Object o) {
    Objects.requireNonNull(o);
    return o;
  }

  private void nonnullWithPhiInstruction(boolean b, Object input) {
    Object o = null;
    if (b) {
      o = Objects.requireNonNull(input);
    }
    System.out.println(o);
  }

  @Override
  public String toString() {
    return "toString";
  }
}
