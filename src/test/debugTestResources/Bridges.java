// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

public class Bridges {

  interface GenericInterface<T> {

    void get(T t);
  }

  static class StringImpl implements GenericInterface<String> {

    @Override
    public void get(String s) {
      System.out.println(s);
    }
  }

  public static void testGenericBridge(GenericInterface<String> obj) {
    obj.get("Foo");
  }

  public static void main(String[] args) {
    testGenericBridge(new StringImpl());
  }
}