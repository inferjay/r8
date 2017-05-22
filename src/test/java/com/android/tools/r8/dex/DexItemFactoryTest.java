// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.dex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.android.tools.r8.graph.DexItem;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import org.junit.Test;

public class DexItemFactoryTest {

  @Test
  public void commonItems() {
    DexItemFactory factory = new DexItemFactory();

    Object[] data = new Object[]{
        "B", factory.byteDescriptor, factory.byteType,
        "C", factory.charDescriptor, factory.charType,
        "D", factory.doubleDescriptor, factory.doubleType,
        "F", factory.floatDescriptor, factory.floatType,
        "I", factory.intDescriptor, factory.intType,
        "J", factory.longDescriptor, factory.longType,
        "S", factory.shortDescriptor, factory.shortType,
        "V", factory.voidDescriptor, factory.voidType,
        "Z", factory.booleanDescriptor, factory.booleanType,
        "Ljava/lang/String;", factory.stringDescriptor, factory.stringType,
        "Ljava/lang/Object;", factory.objectDescriptor, factory.objectType,
    };

    for (int i = 0; i < data.length; i += 3) {
      DexString string1 = factory.createString((String) data[i]);
      DexString string2 = factory.createString((String) data[i]);
      DexItem type1 = factory.createType(string1);
      DexItem type2 = factory.createType(string2);
      DexItem expectedDexString = (DexString) data[i + 1];
      DexItem expectedDexType = (DexType) data[i + 2];

      assertSame(expectedDexString, string1);
      assertSame(expectedDexString, string2);
      assertSame(expectedDexType, type1);
      assertSame(expectedDexType, type2);
    }
  }

  @Test
  public void getPrimitiveTypeName() {
    DexItemFactory factory = new DexItemFactory();
    assertEquals("boolean", factory.booleanType.getName());
    assertEquals("byte", factory.byteType.getName());
    assertEquals("short", factory.shortType.getName());
    assertEquals("char", factory.charType.getName());
    assertEquals("int", factory.intType.getName());
    assertEquals("float", factory.floatType.getName());
    assertEquals("long", factory.longType.getName());
    assertEquals("double", factory.doubleType.getName());
  }
}
