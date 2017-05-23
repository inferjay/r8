// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

public class Test {

  int ignore;

  public static void main(String... args) {
    Lowest l = new Lowest();
    callVirtualOnIface(l);
    callIfaceOnVirtual(l);
  }

  private static void callVirtualOnIface(Iface i) {
    try {
      i.bar();
    } catch (IncompatibleClassChangeError e) {
      System.out.println("ICCE");
    }
  }

  private static void callIfaceOnVirtual(Super s) {
    try {
      s.foo();
    } catch (IncompatibleClassChangeError e) {
      System.out.println("ICCE");
    }
  }
}
