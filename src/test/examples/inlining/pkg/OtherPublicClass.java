// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package inlining.pkg;

public class OtherPublicClass {

  public static String callsMethodThatCallsPackagePrivateMethod() {
    return PublicClass.callsPackagePrivateMethod();
  }

  public static String callsMethodThatCallsProtectedMethod() {
    return PublicClass.callsProtectedMethod();
  }

  public static int callsMethodThatReadsFieldInPackagePrivateClass() {
    return PublicClass.readsPackagePrivateField();
  }

}
