// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package assumenosideeffects1;

public class Assumenosideeffects {

  public static void main(String[] args) {
    noSideEffectVoid();
    noSideEffectInt();
  }

  @CheckDiscarded
  public static void noSideEffectVoid() {
    System.out.println("noSideEffectVoid");
  }

  @CheckDiscarded
  public static int noSideEffectInt() {
    System.out.println("noSideEffectInt");
    return 0;
  }
}
