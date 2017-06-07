// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.rewrite.logarguments;

import static org.junit.Assert.assertEquals;

import com.android.tools.r8.TestBase;
import com.android.tools.r8.utils.AndroidApp;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class LogArgumentsTest extends TestBase {

  private int occourences(String match, String value) {
    int count = 0;
    int startIndex = 0;
    while (true) {
      int index = value.indexOf(match, startIndex);
      if (index > 0) {
        count++;
      } else {
        return count;
      }
      startIndex = index + match.length();
    }
  }

  @Test
  public void testStatic() throws Exception {
    String qualifiedMethodName = "com.android.tools.r8.rewrite.logarguments.TestStatic.a";
    AndroidApp app = compileWithR8(
        readClasses(TestStatic.class),
        options -> options.logArgumentsFilter = ImmutableList.of(qualifiedMethodName));
    String result = runOnArt(app, TestStatic.class);
    assertEquals(7, occourences(qualifiedMethodName, result));
    assertEquals(3, occourences("(primitive)", result));
    assertEquals(3, occourences("(null)", result));
    assertEquals(1, occourences("java.lang.Object", result));
    assertEquals(1, occourences("java.lang.Integer", result));
    assertEquals(1, occourences("java.lang.String", result));
  }

  @Test
  public void testInstance() throws Exception {
    String qualifiedMethodName = "com.android.tools.r8.rewrite.logarguments.TestInstance.a";
    AndroidApp app = compileWithR8(
        readClasses(TestInstance.class),
        options -> options.logArgumentsFilter = ImmutableList.of(qualifiedMethodName));
    String result = runOnArt(app, TestInstance.class);
    assertEquals(7, occourences(qualifiedMethodName, result));
    assertEquals(7, occourences(
        "class com.android.tools.r8.rewrite.logarguments.TestInstance", result));
    assertEquals(3, occourences("(primitive)", result));
    assertEquals(3, occourences("(null)", result));
    assertEquals(1, occourences("java.lang.Object", result));
    assertEquals(1, occourences("java.lang.Integer", result));
    assertEquals(1, occourences("java.lang.String", result));
  }

  @Test
  public void testInner() throws Exception {
    String qualifiedMethodName = "com.android.tools.r8.rewrite.logarguments.TestInner$Inner.a";
    AndroidApp app = compileWithR8(
        readClasses(TestInner.class, TestInner.Inner.class),
        options -> options.logArgumentsFilter = ImmutableList.of(qualifiedMethodName));
    String result = runOnArt(app, TestInner.class);
    assertEquals(7, occourences(qualifiedMethodName, result));
    assertEquals(7, occourences(
        "class com.android.tools.r8.rewrite.logarguments.TestInner$Inner", result));
    assertEquals(3, occourences("(primitive)", result));
    assertEquals(3, occourences("(null)", result));
    assertEquals(1, occourences("java.lang.Object", result));
    assertEquals(1, occourences("java.lang.Integer", result));
    assertEquals(1, occourences("java.lang.String", result));
  }
}
