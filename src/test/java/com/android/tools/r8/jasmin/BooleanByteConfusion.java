// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.jasmin;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class BooleanByteConfusion extends JasminTestBase {

  private void runTest(JasminBuilder builder, String main) throws Exception {
    String javaResult = runOnJava(builder, main);
    String artResult = runOnArt(builder, main);
    assertEquals(javaResult, artResult);
    String dxArtResult = runOnArtDx(builder, main);
    assertEquals(javaResult, dxArtResult);
  }

  @Test
  public void booleanByteConfusion() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    // public static void foo(boolean condition) {
    //   boolean[][] array = null;
    //   if (condition) {
    //     array = new boolean[][] { {true}, {false} };
    //   }
    //   if (array != null) {
    //     System.out.println(array[0][0]);
    //     System.out.println(array[1][0]);
    //   } else {
    //     System.out.println("null array");
    //   }
    // }
    clazz.addStaticMethod("foo", ImmutableList.of("Z"), "V",
        ".limit stack 10",
        ".limit locals 10",
        "  aconst_null",
        // Compiling with new versions of javac seem to always put in this check cast, but it
        // is not strictly required and some versions of javac seem to not emit it. The class
        // file is still valid and runs, so we should be able to deal with it as well.
        //
        // "  checkcast [[Z",
        //
        "  astore_1",
        "  iload_0",
        "  ifeq Target",
        "  iconst_2",
        "  anewarray [Z",
        "  dup",
        "  iconst_0",
        "  iconst_1",
        "  newarray boolean",
        "  dup",
        "  iconst_0",
        "  iconst_1",
        "  bastore",
        "  aastore",
        "  dup",
        "  iconst_1",
        "  iconst_1",
        "  newarray boolean",
        "  dup",
        "  iconst_0",
        "  iconst_0",
        "  bastore",
        "  aastore",
        "  astore_1",
        "Target:",
        "  aload_1",
        "  ifnull PrintNull",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  aload_1",
        "  iconst_0",
        "  aaload",
        "  iconst_0",
        "  baload",
        "  invokevirtual java/io/PrintStream/println(Z)V",
        "  getstatic java/lang/System.out Ljava/io/PrintStream;",
        "  aload_1",
        "  iconst_1",
        "  aaload",
        "  iconst_0",
        "  baload",
        "  invokevirtual java/io/PrintStream.println(Z)V",
        "  goto Return",
        "PrintNull:",
        "  getstatic java/lang/System.out Ljava/io/PrintStream;",
        "  ldc \"null array\"",
        "  invokevirtual java/io/PrintStream.println(Ljava/lang/String;)V",
        "Return:",
        "  return");

    // public static void main(String args[]) {
    //   foo(true);
    //   foo(false);
    // }
    clazz.addMainMethod(
        ".limit stack 2",
        ".limit locals 10",
        "  iconst_1",
        "  invokestatic Test/foo(Z)V",
        "  iconst_0",
        "  invokestatic Test/foo(Z)V",
        "  return");

    runTest(builder, clazz.name);
  }
}
