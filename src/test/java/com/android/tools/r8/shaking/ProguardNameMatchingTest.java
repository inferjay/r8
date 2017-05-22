// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.shaking.ProguardTypeMatcher.ClassOrType;
import com.android.tools.r8.utils.DescriptorUtils;
import org.junit.Test;

public class ProguardNameMatchingTest {
  private static final String[] BASIC_TYPES = {
      "char", "byte", "short", "int", "long", "float", "double", "boolean"
  };

  private static final DexItemFactory dexItemFactory = new DexItemFactory();

  private static boolean matchTypeName(String pattern, String typeName,
      DexItemFactory dexItemFactory) {
    return ProguardTypeMatcher.create(pattern, ClassOrType.TYPE, dexItemFactory)
        .matches(dexItemFactory.createType(DescriptorUtils.javaTypeToDescriptor(typeName)));
  }

  private static boolean matchClassName(String pattern, String className,
      DexItemFactory dexItemFactory) {
    return ProguardTypeMatcher.create(pattern, ClassOrType.CLASS, dexItemFactory)
        .matches(dexItemFactory.createType(DescriptorUtils.javaTypeToDescriptor(className)));
  }

  @Test
  public void matchClassNames() {
    assertTrue(matchClassName("**", "", dexItemFactory));
    assertTrue(matchClassName("**", "a", dexItemFactory));
    assertTrue(matchClassName("*", "", dexItemFactory));
    assertTrue(matchClassName("*", "a", dexItemFactory));
    assertFalse(matchClassName("?", "", dexItemFactory));
    assertTrue(matchClassName("?", "a", dexItemFactory));

    assertTrue(matchClassName("**", "java.lang.Object", dexItemFactory));
    assertFalse(
        matchClassName("ja*", "java.lang.Object", dexItemFactory));
    assertTrue(
        matchClassName("ja**", "java.lang.Object", dexItemFactory));
    assertTrue(matchClassName("ja**ject", "java.lang.Object",
        dexItemFactory));
    // Oddly, the proguard specification for this makes a lonely * synonymous with **.
    assertTrue(matchClassName("*", "java.lang.Object", dexItemFactory));
    assertFalse(matchClassName("ja*ject", "java.lang.Object",
        dexItemFactory));

    assertTrue(matchClassName("java.*g.O*", "java.lang.Object",
        dexItemFactory));
    assertTrue(matchClassName("java.*g.O?je?t", "java.lang.Object",
        dexItemFactory));
    assertFalse(matchClassName("java.*g.O?je?t?", "java.lang.Object",
        dexItemFactory));
    assertFalse(matchClassName("java?lang.Object", "java.lang.Object",
        dexItemFactory));
    assertTrue(matchClassName("*a*.*a**", "java.lang.Object",
        dexItemFactory));
    assertTrue(matchClassName("*a**a**", "java.lang.Object",
        dexItemFactory));
  }

  private void assertMatchesBasicTypes(String pattern) {
    for (String type : BASIC_TYPES) {
      assertTrue(matchTypeName(pattern, type, dexItemFactory));
    }
  }

  private void assertDoesNotMatchBasicTypes(String pattern) {
    for (String type : BASIC_TYPES) {
      assertFalse(matchTypeName(pattern, type, dexItemFactory));
    }
  }

  @Test
  public void matchTypeNames() {
    assertTrue(matchTypeName("**", "java.lang.Object", dexItemFactory));
    assertDoesNotMatchBasicTypes("**");
    assertDoesNotMatchBasicTypes("*");
    assertFalse(
        matchTypeName("**", "java.lang.Object[]", dexItemFactory));
    assertFalse(
        matchTypeName("**z", "java.lang.Object", dexItemFactory));
    assertFalse(matchTypeName("java.**", "java.lang.Object[]",
        dexItemFactory));
    assertTrue(
        matchTypeName("***", "java.lang.Object[]", dexItemFactory));
    assertTrue(matchTypeName("***", "java.lang.Object[][]",
        dexItemFactory));
    assertFalse(matchTypeName("%", "java.lang.Object", dexItemFactory));
    assertTrue(matchTypeName("**", "a", dexItemFactory));
    assertTrue(matchTypeName("**[]", "java.lang.Object[]",
        dexItemFactory));
    assertFalse(matchTypeName("**[]", "java.lang.Object[][]",
        dexItemFactory));
    assertTrue(matchTypeName("*", "abc", dexItemFactory));
    assertMatchesBasicTypes("***");
    assertMatchesBasicTypes("%");
  }

  @Test
  public void matchFieldOrMethodNames() {
    assertTrue(ProguardNameMatcher.matchFieldOrMethodName("*", ""));
    assertTrue(ProguardNameMatcher.matchFieldOrMethodName("*", "get"));
    assertTrue(ProguardNameMatcher.matchFieldOrMethodName("get*", "get"));
    assertTrue(ProguardNameMatcher.matchFieldOrMethodName("get*", "getObject"));
    assertTrue(ProguardNameMatcher.matchFieldOrMethodName("*t", "get"));
    assertTrue(ProguardNameMatcher.matchFieldOrMethodName("g*t*", "getObject"));
    assertTrue(
        ProguardNameMatcher.matchFieldOrMethodName("g*t***************", "getObject"));
    assertFalse(ProguardNameMatcher.matchFieldOrMethodName("get*y", "getObject"));
    assertFalse(ProguardNameMatcher.matchFieldOrMethodName("getObject?", "getObject"));
    assertTrue(ProguardNameMatcher.matchFieldOrMethodName("getObject?", "getObject1"));
    assertTrue(ProguardNameMatcher.matchFieldOrMethodName("getObject?", "getObject5"));
 }
}
