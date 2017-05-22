// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import com.google.common.collect.Iterables;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class IntIntHashMapTest {

  @Test(expected = RuntimeException.class)
  public void putInvalid() throws Exception {
    IntIntHashMap map = new IntIntHashMap();
    map.put(-1, 12);
  }

  @Test
  public void put() throws Exception {
    IntIntHashMap map = new IntIntHashMap();
    int key = 22;
    int value = 0;
    map.put(key, value);
    Assert.assertTrue(map.containsKey(key));
    Assert.assertFalse(map.containsKey(33));
    Assert.assertEquals(map.get(key), value);
    Assert.assertEquals(map.size(), 1);
  }

  @Test
  public void random() throws Exception {
    final int length = 5999;
    IntIntHashMap map = new IntIntHashMap();
    Random rnd = new Random();
    Set<Integer> seen = new LinkedHashSet<>();
    String[] array = new String[length];
    for (int i = 0; i < length; i++) {
      int next;
      do {
        next = rnd.nextInt(4 * length);
      } while (seen.contains(next));
      seen.add(next);
      map.put(next, i * 3);
    }
    Assert.assertEquals(seen.size(), map.size());
    Iterator<Integer> it = seen.iterator();
    for (int i = 0; i < length; i++) {
      Assert.assertEquals(map.get(it.next()), i * 3);
    }
    for (int i : map.values()) {
      Assert.assertTrue(i < length * 3 && i >= 0 && i % 3 == 0);
    }
    Assert.assertEquals(length, Iterables.size(map.values()));
    for (Integer key : map.keys()) {
      Assert.assertTrue(seen.contains(key));
    }
    Assert.assertEquals(seen.size(), Iterables.size(map.keys()));
  }

  @Test
  public void overwrite() throws Exception {
    IntIntHashMap map = new IntIntHashMap();
    int key = 42;
    int value1 = 0;
    map.put(key, value1);
    Assert.assertEquals(map.get(key), value1);
    int value2 = -1;
    map.put(key, value2);
    Assert.assertEquals(map.get(key), value2);
  }
}
