// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package assumenosideeffects5;

public class Assumenosideeffects {

  public static void main(String[] args) {
    System.out.println(methodTrue());
    System.out.println(methodFalse());
  }

  @CheckDiscarded
  public static boolean methodTrue() {
    System.out.println("methodTrue");
    return true;
  }

  @CheckDiscarded
  public static boolean methodFalse() {
    System.out.println("methodFalse");
    return false;
  }
}
