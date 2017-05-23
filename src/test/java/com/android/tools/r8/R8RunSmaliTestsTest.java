// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import static org.junit.Assert.assertEquals;

import com.android.tools.r8.ToolHelper.DexVm;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class R8RunSmaliTestsTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static final String SMALI_DIR = ToolHelper.SMALI_BUILD_DIR;

  // Tests where the original smali code fails on Art, but runs after R8 processing.
  private static Map<DexVm, List<String>> originalFailingOnArtVersions = ImmutableMap.of(
      DexVm.ART_5_1_1, ImmutableList.of(
          "sparse-switch",
          "regression/33846227"
      )
  );

  // Tests where the original smali code runs on Art, but fails after R8 processing
  private static Map<String, List<String>> failingOnArtVersions = ImmutableMap.of(
      // This list is currently empty!
  );

  @Rule
  public TemporaryFolder temp = ToolHelper.getTemporaryFolderForTest();

  @Parameters(name = "{0}")
  public static Collection<String[]> data() {
    return Arrays.asList(new String[][]{
        {"arithmetic", "-1\n3\n2\n3\n3.0\n1\n0\n-131580\n-131580\n2\n4\n-2\n"},
        {"controlflow", "2\n1\n2\n1\n2\n1\n2\n1\n2\n1\n2\n1\n2\n"},
        {"fibonacci", "55\n55\n55\n55\n"},
        {"fill-array-data", "[1, 2, 3][4, 5, 6]"},
        {"filled-new-array", "[1, 2, 3][4, 5, 6][1, 2, 3, 4, 5, 6][6, 5, 4, 3, 2, 1]"},
        {"packed-switch", "12345"},
        {"sparse-switch", "12345"},
        {"unreachable-code-1", "777"},
        {"multiple-returns", "TFtf\n1\n4611686018427387904\ntrue\nfalse\n"},
        {"try-catch", ""},
        {"phi-removal-regression", "returnBoolean\n"},
        {"overlapping-long-registers", "-9151314442816847872\n-9151314442816319488\n"},
        {"type-confusion-regression", "java.lang.RuntimeException: Test.<init>()\n"},
        {"type-confusion-regression2",
            "java.lang.NullPointerException: Attempt to read from null array\n"},
        {"type-confusion-regression3",
            "java.lang.NullPointerException: Attempt to read from field 'byte[] Test.a'" +
                " on a null object reference\n"},
        {"type-confusion-regression4", ""},
        {"type-confusion-regression5", "java.lang.RuntimeException: getId()I\n"},
        {"chain-of-loops", "java.lang.RuntimeException: f(II)\n"},
        {"new-instance-and-init", "Test(0)\nTest(0)\nTest(0)\n"},
        {"bad-codegen",
            "java.lang.NullPointerException: Attempt to read from field " +
                "'Test Test.a' on a null object reference\n"},
        {"merge-blocks-regression", "java.lang.NullPointerException: Attempt to invoke virtual"
            + " method 'Test Test.bW_()' on a null object reference\n"},
        {"self-is-catch-block", "100\n-1\n"},
        {"infinite-loop", ""},
        {"regression/33336471",
            "START\n0\n2\nLOOP\n1\n2\nLOOP\n2\n2\nDONE\n" +
                "START\n0\n2\nLOOP\n1\n2\nLOOP\n2\n2\nDONE\n"},
        {"regression/33846227", ""},
        {"illegal-invokes", "ICCE\nICCE\n"},
    });
  }

  private String directoryName;
  private String dexFileName;
  private String expectedOutput;

  public R8RunSmaliTestsTest(String name, String expectedOutput) {
    this.directoryName = name;
    this.dexFileName = name.substring(name.lastIndexOf('/') + 1) + ".dex";
    this.expectedOutput = expectedOutput;
  }

  @Test
  public void SmaliTest()
      throws IOException, ProguardRuleParserException, ExecutionException, CompilationException {
    File originalDexFile = Paths.get(SMALI_DIR, directoryName, dexFileName).toFile();
    String outputPath = temp.getRoot().getCanonicalPath();
    ToolHelper.runR8(originalDexFile.getCanonicalPath(), outputPath);

    if (!ToolHelper.artSupported()) {
      return;
    }

    String mainClass = "Test";
    String generated = outputPath + "/classes.dex";
    String output;

    // If the original smali code fails on the target VM, only run the code produced by R8.
    if (originalFailingOnArtVersions.containsKey(ToolHelper.getDexVm())
        && originalFailingOnArtVersions.get(ToolHelper.getDexVm()).contains(directoryName)) {
      output = ToolHelper.runArtNoVerificationErrors(generated, mainClass);
    } else {
      if (failingOnArtVersions.containsKey(ToolHelper.getDexVm())
          && failingOnArtVersions.get(ToolHelper.getDexVm()).contains(directoryName)) {
        thrown.expect(Throwable.class);
      }
      output =
          ToolHelper
              .checkArtOutputIdentical(originalDexFile.toString(), generated, mainClass, null);
    }
    assertEquals(expectedOutput, output);
  }
}
