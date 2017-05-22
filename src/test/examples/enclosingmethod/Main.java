// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package enclosingmethod;

public class Main {
  public static void main(String... args) {
    OuterClass anInstance = new OuterClass();
    anInstance.aMethod();
    final Class[] classes = OuterClass.class.getDeclaredClasses();
    for (Class clazz : classes) {
      System.out.println("InnerClass " + clazz.getName());
    }
  }
}
