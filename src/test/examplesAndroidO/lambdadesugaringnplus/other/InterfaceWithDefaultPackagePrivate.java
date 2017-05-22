// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package lambdadesugaringnplus.other;

class PackagePrivate {
  @Override
  public String toString() {
    return "PackagePrivate::toString()";
  }
}

public interface InterfaceWithDefaultPackagePrivate {
  String foo();

  default String defaultFoo() {
    return "defaultFoo: " + new PackagePrivate().toString();
  }

  static String staticFoo() {
    return "staticFoo: " + new PackagePrivate().toString();
  }
}
