// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.jasmin;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class Regress64658224 extends JasminTestBase {

  @Test
  public void testInvalidTypeInfoFromLocals() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    clazz.addStaticMethod("foo", ImmutableList.of("I"), "V",
        ".limit stack 2",
        ".limit locals 2",
        ".var 1 is x Ljava/lang/Object; from L1 to L2",
        "  aconst_null",
        "  astore 1",
        "L1:",
        "  iload 0",
        "  ifeq L3",
        "L2:",
        "  goto L5",
        "L3:",
        "  aload 1",
        "  iconst_0",
        "  aaload",
        "  pop",
        "L5:",
        "  return");

    clazz.addMainMethod(
        ".limit stack 1",
        ".limit locals 1",
        "  ldc 2",
        "  invokestatic Test/foo(I)V",
        "  return");

    String expected = "";
    String javaResult = runOnJava(builder, clazz.name);
    assertEquals(expected, javaResult);
    String artResult = runOnArtD8(builder, clazz.name);
    assertEquals(expected, artResult);
  }
}
