// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.regress.b63935662;

import java.util.function.BiConsumer;

public class TestFromBug {

  public interface Map<K, V> {
    default void forEach(BiConsumer<? super K, ? super V> action) {
      System.out.println("Map.forEach");
    }
  }

  public interface ConcurrentMap<K, V> extends Map<K,V> {
    @Override
    default void forEach(BiConsumer<? super K, ? super V> action) {
      System.out.println("ConcurrentMap.forEach");
    }
  }

  public static abstract class AbstractMap<K,V> implements Map<K, V> {}
  public static class ConcurrentHashMap<K,V> extends AbstractMap<K,V> implements ConcurrentMap<K,V> {}

  public static void main(String[] args) {
    new ConcurrentHashMap<String, String>().forEach(null);
  }
}