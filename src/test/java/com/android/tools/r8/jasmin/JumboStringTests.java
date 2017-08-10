// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.jasmin;

import static org.junit.Assert.assertEquals;

import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.debuginfo.DebugInfoInspector;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.naming.MemberNaming.MethodSignature;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.DexInspector;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import org.junit.Test;

public class JumboStringTests extends JasminTestBase {

  // String constants are split into several class files to ensure both the constant-pool and
  // instruction count are below the class-file limits.
  private static int CLASSES_COUNT = 10;
  private static int MIN_STRING_COUNT = Constants.FIRST_JUMBO_INDEX + 1;
  private static int EXTRA_STRINGS_PER_CLASSES_COUNT = MIN_STRING_COUNT % CLASSES_COUNT;
  private static int STRINGS_PER_CLASSES_COUNT =
      EXTRA_STRINGS_PER_CLASSES_COUNT + MIN_STRING_COUNT / CLASSES_COUNT;

  @Test
  public void test() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    LinkedHashMap<String, MethodSignature> classes = new LinkedHashMap<>(CLASSES_COUNT);
    for (int i = 0; i < CLASSES_COUNT; i++) {
      JasminBuilder.ClassBuilder clazz = builder.addClass("Test" + i);
      List<String> lines = new ArrayList<>(STRINGS_PER_CLASSES_COUNT + 100);
      lines.addAll(
          ImmutableList.of(
              ".limit locals 3",
              ".limit stack 4",
              ".var 0 is this LTest; from L0 to L2",
              ".var 1 is i I from L0 to L2",
              ".var 2 is strings [Ljava/lang/String; from L1 to L2",
              "L0:",
              ".line 1",
              "  ldc " + STRINGS_PER_CLASSES_COUNT,
              "  anewarray java/lang/String",
              "  astore 2",
              "L1:",
              ".line 2"));
      for (int j = 0; j < STRINGS_PER_CLASSES_COUNT; j++) {
        lines.add("  aload 2");
        lines.add("  ldc " + j);
        lines.add("  ldc \"string" + i + "_" + j + "\"");
        lines.add("  aastore");
      }
      lines.addAll(
          ImmutableList.of(
              "L2:",
              "  .line 3",
              "  aload 2",
              "  iload 1",
              "  aaload",
              "  checkcast java/lang/String",
              "  areturn"));
      MethodSignature foo =
          clazz.addVirtualMethod(
              "foo", ImmutableList.of("I"), "Ljava/lang/String;", lines.toArray(new String[0]));
      classes.put(clazz.name, foo);
    }

    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");
    clazz.addMainMethod(
        ".limit stack 3",
        ".limit locals 1",
        "  new Test0",
        "  dup",
        "  invokespecial Test0/<init>()V",
        "  ldc 42",
        "  invokevirtual Test0/foo(I)Ljava/lang/String;",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  swap",
        "  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V",
        "  return");

    String expected = "string0_42";
    assertEquals(expected, runOnJava(builder, clazz.name));

    AndroidApp jasminApp = builder.build();
    AndroidApp d8App = ToolHelper.runD8(jasminApp);
    assertEquals(expected, runOnArt(d8App, clazz.name));

    DexInspector inspector = new DexInspector(d8App);
    for (Entry<String, MethodSignature> entry : classes.entrySet()) {
      DebugInfoInspector info = new DebugInfoInspector(inspector, entry.getKey(), entry.getValue());
      info.checkStartLine(1);
      // If jumbo-string processing fails to keep debug info, some methods will have lost 'i' here.
      info.checkLineHasExactLocals(1, "this", entry.getKey(), "i", "int");
      info.checkLineHasExactLocals(
          2, "this", entry.getKey(), "i", "int", "strings", "java.lang.String[]");
    }
  }
}
