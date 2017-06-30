// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.jasmin;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class FillBooleanArrayTruncation extends JasminTestBase {

  private void runTest(JasminBuilder builder, String main) throws Exception {
    String javaResult = runOnJava(builder, main);
    String artResult = runOnArt(builder, main);
    assertEquals(javaResult, artResult);
    String dxArtResult = runOnArtDx(builder, main);
    assertEquals(javaResult, dxArtResult);
  }

  @Test
  public void filledArray() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    // Corresponds to something like the following (which doesn't compile with javac):
    //
    // public static void foo() {
    //   byte[] bytes = new byte[5];
    //   bytes[0] = 257;
    //   bytes[1] = -257;
    //   bytes[2] = 88;
    //   bytes[3] = 129;
    //   bytes[4] = -129;
    //   for (int i = 0; i < bytes.length; i++) {
    //     System.out.println(bytes[i]);
    //   }
    // }
    clazz.addStaticMethod("foo", ImmutableList.of(), "V",
        ".limit stack 10",
        ".limit locals 10",
        "  iconst_5",
        "  newarray byte",
        "  astore_0",
        "  aload_0",
        "  iconst_0",
        "  sipush 257",
        "  bastore",
        "  aload_0",
        "  iconst_1",
        "  sipush -257",
        "  bastore",
        "  aload_0",
        "  iconst_2",
        "  bipush 88",
        "  bastore",
        "  aload_0",
        "  iconst_3",
        "  sipush 129",
        "  bastore",
        "  aload_0",
        "  iconst_4",
        "  sipush -129",
        "  bastore",
        "  iconst_0",
        "  istore_1",
        "PrintLoop:",
        "  iload_1",
        "  aload_0",
        "  arraylength",
        "  if_icmpge Return",
        "  getstatic  java/lang/System.out Ljava/io/PrintStream;",
        "  aload_0",
        "  iload_1",
        "  baload",
        "  invokevirtual java/io/PrintStream.println(I)V",
        "  iinc 1 1",
        "  goto          PrintLoop",
        "Return:",
        "  return");

    // public static void main(String args[]) {
    //   foo();
    // }
    clazz.addMainMethod(
        ".limit stack 2",
        ".limit locals 10",
        "  invokestatic Test/foo()V",
        "  return");

    runTest(builder, clazz.name);
  }
}
