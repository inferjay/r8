// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package memberrebinding;

import java.util.ArrayList;
import java.util.Arrays;

public class ClassExtendsLibraryClass extends ArrayList<String> {

  public void methodThatAddsHelloWorld() {
    // call add on this type, so it gets rebound.
    add("hello");
    // call it on list to make the methodid available.
    addOnArrayList(this, "world");
  }

  private static <T> void addOnArrayList(ArrayList<T> list, T item) {
    list.add(item);
  }

  public void methodThatAddsHelloWorldUsingAddAll() {
    // call this only on this type, so that it cannot be rebound to the interface.
    String[] words = new String[]{"hello", "world"};
    addAll(Arrays.asList(words));
  }

}
