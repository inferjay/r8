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

public class IntHashMapTest {

  static private String getRandomString() {
    String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    StringBuilder salt = new StringBuilder();
    Random rnd = new Random();
    while (salt.length() < 18) {
      int index = (int) (rnd.nextFloat() * SALTCHARS.length());
      salt.append(SALTCHARS.charAt(index));
    }
    return salt.toString();
  }

  @Test(expected = RuntimeException.class)
  public void putInvalid() throws Exception {
    IntHashMap<Object> map = new IntHashMap<>();
    map.put(12, null);
  }

  @Test
  public void put() throws Exception {
    IntHashMap<Object> map = new IntHashMap<>();
    int key = 1;
    Object value = new Object();
    map.put(key, value);
    Assert.assertEquals(map.get(key), value);
    Assert.assertEquals(map.size(), 1);
    map.put(key + 1, value);
    Assert.assertEquals(map.get(key + 1), value);
    Assert.assertEquals(map.size(), 2);
    Assert.assertEquals(map.get(0), null);
    map.put(0, value);
    Assert.assertEquals(map.get(0), value);
  }

  @Test
  public void random() throws Exception {
    final int length = 5999;
    IntHashMap<String> map = new IntHashMap<>();
    String[] array = new String[length];
    for (int i = 0; i < length; i++) {
      array[i] = getRandomString();
      map.put(i, array[i]);
    }
    Assert.assertEquals(length, map.size());
    for (int i = 0; i < length; i++) {
      Assert.assertEquals(map.get(i), array[i]);
    }
    Set<String> items = ImmutableSet.copyOf(array);
    for (String s : map.values()) {
      Assert.assertTrue(items.contains(s));
    }
    Assert.assertEquals(length, Iterables.size(map.values()));
    for (int i : map.keys()) {
      Assert.assertTrue(i < length && i >= 0);
    }
    Assert.assertEquals(length, Iterables.size(map.keys()));
  }

  @Test
  public void overwrite() throws Exception {
    IntHashMap<Object> map = new IntHashMap<>();
    int key = 1;
    Object value1 = new Object();
    map.put(key, value1);
    Assert.assertEquals(map.get(key), value1);
    Object value2 = new Object[0];
    map.put(key, value2);
    Assert.assertEquals(map.get(key), value2);
  }
}
