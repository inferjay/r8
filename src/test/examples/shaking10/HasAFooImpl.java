// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package shaking10;

import shakinglib.HasAFoo;
import shakinglib.HasAGetter;

public class HasAFooImpl implements HasAFoo {

  @Override
  public HasAGetter foo() {
    return new ReturnsOne();
  }

  public int bar() {
    return 0;
  }

  private static class ReturnsOne implements HasAGetter {

    @Override
    public int getIt() {
      return 1;
    }
  }
}
