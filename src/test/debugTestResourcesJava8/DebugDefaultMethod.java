// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

public class DebugDefaultMethod {

  interface I {
    default void doSomething(String msg) {
      String name = getClass().getName();
      System.out.println(name + ": " + msg);
    }
  }

  static class DefaultImpl implements I {
  }

  static class OverrideImpl implements I {

    @Override
    public void doSomething(String msg) {
      String newMsg = "OVERRIDE" + msg;
      System.out.println(newMsg);
    }
  }

  private static void testDefaultMethod(I i) {
    i.doSomething("Test");
  }

  public static void main(String[] args) {
    testDefaultMethod(new DefaultImpl());
    testDefaultMethod(new OverrideImpl());
  }

}
