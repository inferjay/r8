// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.deterministic;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.R8Command;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.ir.conversion.CallGraph;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.android.tools.r8.smali.SmaliTestBase;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.DexInspector;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.Test;

public class DeterministicProcessingTest extends SmaliTestBase {
  public List<DexEncodedMethod> shuffle(List<DexEncodedMethod> methods, CallGraph.Leaves leaves) {
    Collections.shuffle(methods);
    return methods;
  }

  // This test will process the code a number of times each time shuffling the order in which
  // the methods are processed. It does not do a exhaustive probing of all permutations, so if
  // this fails it might not fail consistently, but under all circumstances a failure does reveal
  // non-determinism in the IR-processing.
  @Test
  public void shuffleOrderTest()
      throws IOException, ExecutionException, ProguardRuleParserException, CompilationException {
    final int ITERATIONS = 25;
    R8Command.Builder builder =
        R8Command.builder()
            .addProgramFiles(ToolHelper.getClassFileForTestClass(TestClass.class))
            .addLibraryFiles(Paths.get(ToolHelper.getDefaultAndroidJar()));
    byte[] expectedDex = null;
    for (int i = 0; i < ITERATIONS; i++) {
      AndroidApp result =
          ToolHelper.runR8(
              builder.build(), options -> {
                // For this test just do random shuffle.
                options.testing.irOrdering = this::shuffle;
                // Only use one thread to process to process in the order decided by the callback.
                options.numberOfThreads = 1;
              });
      List<byte[]> dex = result.writeToMemory();
      assertEquals(1, dex.size());
      if (i == 0) {
        assert expectedDex == null;
        expectedDex = dex.get(0);
      } else {
        assertArrayEquals(expectedDex, dex.get(0));
      }
    }
  }

  // Global variables used by the shuffler callback.
  int iteration = 0;
  Class testClass = null;

  public List<DexEncodedMethod> permutationsOfTwo(
      List<DexEncodedMethod> methods, CallGraph.Leaves leaves) {
    if (!leaves.brokeCycles()) {
      return methods;
    }
    methods.sort(Comparator.comparing(DexEncodedMethod::qualifiedName));
    assertEquals(2, methods.size());
    String className = testClass.getTypeName();
    // Check that we are permutating the expected methods.
    assertEquals(className + ".a", methods.get(0).qualifiedName());
    assertEquals(className + ".b", methods.get(1).qualifiedName());
    if (iteration == 1) {
      Collections.swap(methods, 0, 1);
    }
    return methods;
  }

  public void runTest(Class clazz, boolean inline) throws Exception {
    final int ITERATIONS = 2;
    testClass = clazz;
    R8Command.Builder builder =
        R8Command.builder()
            .addProgramFiles(ToolHelper.getClassFileForTestClass(clazz))
            .addLibraryFiles(Paths.get(ToolHelper.getDefaultAndroidJar()));
    List<byte[]> results = new ArrayList<>();
    for (iteration = 0; iteration < ITERATIONS; iteration++) {
      AndroidApp result =
          ToolHelper.runR8(
              builder.build(), options -> {
                options.inlineAccessors = inline;
                // Callback to determine IR processing order.
                options.testing.irOrdering = this::permutationsOfTwo;
                // Only use one thread to process to process in the order decided by the callback.
                options.numberOfThreads = 1;
              });
      List<byte[]> dex = result.writeToMemory();
      DexInspector x = new DexInspector(result);
      assertEquals(1, dex.size());
      results.add(dex.get(0));
    }
    for (int i = 0; i < ITERATIONS - 1; i++) {
      assertArrayEquals(results.get(i), results.get(i + 1));
    }
  }

  @Test
  public void testReturnArguments() throws Exception {
    runTest(TestClassReturnsArgument.class, false);
  }

  @Test
  public void testReturnConstant() throws Exception {
    runTest(TestClassReturnsConstant.class, false);
  }

  @Test
  public void testInline() throws Exception {
    runTest(TestClassInline.class, true);
  }
}
