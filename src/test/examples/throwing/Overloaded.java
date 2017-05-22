// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package throwing;

import java.util.List;

public abstract class Overloaded {

  public int aMethod(int x) {
    return 0;
  }

  public int conflictingMethod(int x) {
    return 0;
  }

  public abstract int bMethod(double x);

  public int conflictingMethod(double x) {
    return 0;
  }

  public int cMethod(boolean x) {
    return 0;
  }

  public int conflictingMethod(boolean x) {
    return 0;
  }

  public int anotherConflict(boolean x) {
    return 0;
  }

  public int unique(List x) {
    return 0;
  }
}
