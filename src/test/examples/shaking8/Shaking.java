// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package shaking8;

import java.util.Arrays;

public class Shaking {

  public static void main(String[] args) {
    Thing[] empty = new Thing[0];
    OtherThing[] one = {new OtherThing(1)};
    callCloneOnArray(null);
    System.out.println(Arrays.toString(empty));
    System.out.println(Arrays.toString(one));
  }

  private static void callCloneOnArray(YetAnotherThing[] array) {
    if (array != null) {
      array.clone();
    }
  }
}
