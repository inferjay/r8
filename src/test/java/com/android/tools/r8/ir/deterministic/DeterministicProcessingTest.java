// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.deterministic;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.R8Command;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.android.tools.r8.smali.SmaliTestBase;
import com.android.tools.r8.utils.AndroidApp;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.Test;

public class DeterministicProcessingTest extends SmaliTestBase {

  @Test
  public void test()
      throws IOException, ExecutionException, ProguardRuleParserException, CompilationException {
    final int ITERATIONS = 10;
    R8Command.Builder builder =
        R8Command.builder()
            .addProgramFiles(ToolHelper.getClassFileForTestClass(TestClass.class))
            .addLibraryFiles(Paths.get(ToolHelper.getDefaultAndroidJar()));
    List<byte[]> results = new ArrayList<>();
    for (int i = 0; i < ITERATIONS; i++) {
      AndroidApp result =
          ToolHelper.runR8(
              builder.build(), options -> options.testing.randomizeCallGraphLeaves = true);
      List<byte[]> dex = result.writeToMemory();
      assertEquals(1, dex.size());
      results.add(dex.get(0));
      System.out.println(dex.get(0).length);
    }
    for (int i = 0; i < ITERATIONS - 1; i++) {
      assertArrayEquals(results.get(i), results.get(i + 1));
    }
  }
}
