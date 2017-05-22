// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package lambdadesugaringnplus.other;

public class ClassWithDefaultPackagePrivate implements InterfaceWithDefaultPackagePrivate {
  public InterfaceWithDefaultPackagePrivate lambda() {
    return () -> ("lambda(" + InterfaceWithDefaultPackagePrivate.super.defaultFoo() + ")");
  }

  @Override
  public String foo() {
    throw new RuntimeException("Don't call me!");
  }
}
