// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Random;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class HashMapIntTest {

  static private String getString(int i) {
    String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    StringBuilder salt = new StringBuilder();
    Random rnd = new Random();
    while (salt.length() < 18) {
      int index = (int) (rnd.nextFloat() * SALTCHARS.length());
      salt.append(SALTCHARS.charAt(index));
    }
    salt.append(" ");
    salt.append(i);
    return salt.toString();
  }

  @Test(expected = RuntimeException.class)
  public void putInvalid() throws Exception {
    HashMapInt<Object> map = new HashMapInt<>();
    map.put(null, 12);
  }

  @Test
  public void put() throws Exception {
    HashMapInt<Object> map = new HashMapInt<>();
    Object key = new Object();
    int value = 0;
    map.put(key, value);
    Assert.assertTrue(map.containsKey(key));
    Assert.assertFalse(map.containsKey(new Object[0]));
    Assert.assertEquals(map.get(key), value);
    Assert.assertEquals(map.size(), 1);
  }

  @Test
  public void random() throws Exception {
    final int length = 5999;
    HashMapInt<String> map = new HashMapInt<>();
    String[] array = new String[length];
    for (int i = 0; i < length; i++) {
      array[i] = getString(i);
      map.put(array[i], i * 3);
    }
    Assert.assertEquals(length, map.size());
    for (int i = 0; i < length; i++) {
      Assert.assertEquals(map.get(array[i]), i * 3);
    }
    for (int i : map.values()) {
      Assert.assertTrue(i < length * 3 && i >= 0 && i % 3 == 0);
    }
    Assert.assertEquals(length, Iterables.size(map.values()));
    Set<String> items = ImmutableSet.copyOf(array);
    for (String s : map.keys()) {
      Assert.assertTrue(items.contains(s));
    }
    Assert.assertEquals(length, Iterables.size(map.keys()));
  }

  @Test
  public void overwrite() throws Exception {
    HashMapInt<Object> map = new HashMapInt<>();
    Object key = new Object();
    int value1 = 0;
    map.put(key, value1);
    Assert.assertEquals(map.get(key), value1);
    int value2 = -1;
    map.put(key, value2);
    Assert.assertEquals(map.get(key), value2);
  }
}
