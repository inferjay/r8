// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class IdentityHashMapIntTest {

  @Test(expected = RuntimeException.class)
  public void putInvalid() throws Exception {
    IdentityHashMapInt<Object> map = new IdentityHashMapInt<>();
    map.put(null, 12);
  }

  @Test
  public void put() throws Exception {
    IdentityHashMapInt<Object> map = new IdentityHashMapInt<>();
    Object key = new AllEqual();
    int value = 0;
    map.put(key, value);
    Assert.assertTrue(map.containsKey(key));
    Assert.assertFalse(map.containsKey(new Object()));
    Assert.assertFalse(map.containsKey(new AllEqual()));
    Assert.assertEquals(map.get(key), value);
    Assert.assertEquals(map.size(), 1);
  }

  @Test
  public void random() throws Exception {
    final int length = 5999;
    IdentityHashMapInt<Object> map = new IdentityHashMapInt<>();
    AllEqual[] array = new AllEqual[length];
    for (int i = 0; i < length; i++) {
      array[i] = new AllEqual();
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
    Set<Object> items = Sets.newIdentityHashSet();
    Collections.addAll(items, array);
    for (Object o : map.keys()) {
      Assert.assertTrue(items.contains(o));
    }
    Assert.assertEquals(length, Iterables.size(map.keys()));
  }

  @Test
  public void overwrite() throws Exception {
    IdentityHashMapInt<Object> map = new IdentityHashMapInt<>();
    Object key = new Object();
    int value1 = 0;
    map.put(key, value1);
    Assert.assertEquals(map.get(key), value1);
    int value2 = -1;
    map.put(key, value2);
    Assert.assertEquals(map.get(key), value2);
  }

  private static class AllEqual {

    @Override
    public boolean equals(Object o) {
      return true;
    }
  }
}
