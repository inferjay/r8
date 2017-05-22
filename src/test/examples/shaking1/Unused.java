// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package shaking1;

public class Unused {

  public static int aStaticFieldThatIsNotUsedButKept = 42;

  public static void unusedStaticMethod() {
    System.out.println("unusedStaticMethod");
  }

  public void unusedInstanceMethod() {
    System.out.println("unusedInstanceMethod");
  }

  public String aMethodThatIsNotUsedButKept() {
    return "Unused string";
  }
}
