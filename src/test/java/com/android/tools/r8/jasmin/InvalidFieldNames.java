// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.jasmin;

import static junit.framework.TestCase.assertEquals;

import com.android.tools.r8.errors.CompilationError;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class InvalidFieldNames extends JasminTestBase {

  public boolean runsOnJVM;
  public String name;

  public InvalidFieldNames(String name, boolean runsOnJVM) {
    this.name = name;
    this.runsOnJVM = runsOnJVM;
  }

  private void runTest(JasminBuilder builder, String main, String expected) throws Exception {
    if (runsOnJVM) {
      String javaResult = runOnJava(builder, main);
      assertEquals(expected, javaResult);
    }
    String artResult = null;
    try {
      artResult = runOnArt(builder, main);
    } catch (CompilationError t) {
      // Ignore.
    }
    assert artResult == null : "Invalid dex class names should be rejected.";
  }

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        { new String(new int[] { 0x00a0 }, 0, 1), true },
        { new String(new int[] { 0x2000 }, 0, 1), true },
        { new String(new int[] { 0x200f }, 0, 1), true },
        { new String(new int[] { 0x2028 }, 0, 1), true },
        { new String(new int[] { 0x202f }, 0, 1), true },
        { new String(new int[] { 0xd800 }, 0, 1), true },
        { new String(new int[] { 0xdfff }, 0, 1), true },
        { new String(new int[] { 0xfff0 }, 0, 1), true },
        { new String(new int[] { 0xffff }, 0, 1), true },
        { "a/b", false },
        { "<a", false },
        { "a>", true },
        { "a<b>", true },
        { "<a>b", true }
    });
  }

  @Test
  public void invalidFieldNames() throws Exception {
    JasminBuilder builder = new JasminBuilder();
    JasminBuilder.ClassBuilder clazz = builder.addClass("Test");

    clazz.addStaticField(name, "I", "42");

    clazz.addMainMethod(
        ".limit stack 2",
        ".limit locals 1",
        "  getstatic java/lang/System/out Ljava/io/PrintStream;",
        "  getstatic Test/" + name + " I",
        "  invokevirtual java/io/PrintStream.print(I)V",
        "  return");

    runTest(builder, clazz.name, "42");
  }
}
