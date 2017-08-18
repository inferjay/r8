// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.debug;

import org.junit.Assert;
import org.junit.Test;

public class KotlinTest extends DebugTestBase {

  @Test
  public void testKotlinApp() throws Throwable {
    runDebugTestKotlin("KotlinApp",
        breakpoint("KotlinApp$Companion", "main"),
        run(),
        inspect(s -> {
          Assert.assertEquals("KotlinApp.kt", s.getSourceFile());
          Assert.assertEquals(8, s.getLineNumber());
          s.checkLocal("this");
          s.checkLocal("args");
        }),
        stepOver(),
        inspect(s -> {
          Assert.assertEquals(9, s.getLineNumber());
          s.checkLocal("this");
          s.checkLocal("args");
        }),
        stepOver(),
        inspect(s -> {
          Assert.assertEquals(10, s.getLineNumber());
          s.checkLocal("this");
          s.checkLocal("args");
          s.checkLocal("instance");
        }),
        run());
  }

}
