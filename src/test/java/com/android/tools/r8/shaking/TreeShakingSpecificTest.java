// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import static com.android.tools.r8.ToolHelper.EXAMPLES_BUILD_DIR;
import static com.android.tools.r8.ToolHelper.EXAMPLES_DIR;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.R8;
import com.android.tools.r8.R8Command;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.errors.CompilationError;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class TreeShakingSpecificTest {
  private static final String VALID_PROGUARD_DIR = "src/test/proguard/valid/";

  @Rule
  public TemporaryFolder temp = ToolHelper.getTemporaryFolderForTest();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testIgnoreWarnings()
      throws IOException, ProguardRuleParserException, ExecutionException, CompilationException {
    // Generate R8 processed version without library option.
    Path out = temp.getRoot().toPath();
    String test = "shaking2";
    Path originalDex = Paths.get(EXAMPLES_BUILD_DIR, test, "classes.dex");
    Path keepRules = Paths.get(EXAMPLES_DIR, test, "keep-rules.txt");
    Path ignoreWarnings = Paths.get(VALID_PROGUARD_DIR, "ignorewarnings.flags");
    R8.run(
        R8Command.builder()
            .addProgramFiles(originalDex)
            .setOutputPath(out)
            .addProguardConfigurationFiles(keepRules, ignoreWarnings)
            .build());
  }

  @Test
  public void testMissingLibrary()
      throws IOException, ProguardRuleParserException, ExecutionException, CompilationException {
    // Generate R8 processed version without library option.
    Path out = temp.getRoot().toPath();
    String test = "shaking2";
    Path originalDex = Paths.get(EXAMPLES_BUILD_DIR, test, "classes.dex");
    Path keepRules = Paths.get(EXAMPLES_DIR, test, "keep-rules.txt");
    thrown.expect(CompilationError.class);
    thrown.expectMessage("Shrinking can't be performed because some library classes are missing");
    R8.run(
        R8Command.builder()
            .addProgramFiles(originalDex)
            .setOutputPath(out)
            .addProguardConfigurationFiles(keepRules)
            .build());
  }

  @Test
  public void testPrintMapping()
      throws IOException, ProguardRuleParserException, ExecutionException, CompilationException {
    // Generate R8 processed version without library option.
    String test = "shaking1";
    Path out = temp.getRoot().toPath();
    Path originalDex = Paths.get(EXAMPLES_BUILD_DIR, test, "classes.dex");
    Path keepRules = Paths.get(EXAMPLES_DIR, test, "keep-rules.txt");

    // Create a flags file in temp dir requesting dump of the mapping.
    // The mapping file will be created alongside the flags file in temp dir.
    Path printMapping = out.resolve("printmapping.flags");
    try (PrintStream mapping = new PrintStream(printMapping.toFile())) {
      mapping.println("-printmapping mapping.txt");
    }

    ToolHelper.runR8(
        R8Command.builder()
            .addProgramFiles(originalDex)
            .setOutputPath(out)
            .addProguardConfigurationFiles(keepRules, printMapping)
            .build());
    Path outputmapping = out.resolve("mapping.txt");
    String actualMapping;
    actualMapping = new String(Files.readAllBytes(outputmapping), StandardCharsets.UTF_8);
    String refMapping = new String(Files.readAllBytes(
        Paths.get(EXAMPLES_DIR, "shaking1", "print-mapping.ref")), StandardCharsets.UTF_8);
    Assert.assertEquals(sorted(refMapping), sorted(actualMapping));
  }

  private static String sorted(String str) {
    return new BufferedReader(new StringReader(str))
        .lines().sorted().collect(Collectors.joining("\n"));
  }
}
