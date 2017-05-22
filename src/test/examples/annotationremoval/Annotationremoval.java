// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package annotationremoval;

public class Annotationremoval {

  public static void main(String... args) {
    OuterClass instance = new OuterClass();
    System.out.print(instance.getValueFromInner(123));
  }
}
