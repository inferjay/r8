// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package inlining.pkg;

import inlining.CheckDiscarded;

public class PublicClass {

  protected static String protectedMethod() {
    return "Hello";
  }

  @CheckDiscarded
  static String callsProtectedMethod() {
    return protectedMethod();
  }

  @CheckDiscarded
  static String callsProtectedMethod2() {
    return protectedMethod();
  }

  public static String callsProtectedMethod3() {
    return protectedMethod();
  }

  static String packagePrivateMethod() {
    return "World";
  }

  @CheckDiscarded
  static int readsPackagePrivateField() {
    return PackagePrivateClass.aField;
  }

  public static int alsoReadsPackagePrivateField() {
    return PackagePrivateClass.aField;
  }

  @CheckDiscarded
  public static String callsPackagePrivateMethod() {
    return packagePrivateMethod();
  }

  public static String alsoCallsPackagePrivateMethod() {
    return packagePrivateMethod();
  }

  public static void callMeToPreventInling() {
    // Call it three times so it does not get inlined.
    packagePrivateMethod();
    packagePrivateMethod();
    packagePrivateMethod();
    protectedMethod();
    protectedMethod();
    protectedMethod();
  }
}
