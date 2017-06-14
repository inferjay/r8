// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8;

import com.android.tools.r8.ToolHelper.ProcessResult;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.android.tools.r8.utils.FileUtils;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class R8EntryPointTests extends TestBase {

  private static final String MAPPING = "mapping.txt";
  private static final String SEEDS = "seeds.txt";
  private static final Path INPUT_JAR =
      Paths.get(ToolHelper.EXAMPLES_BUILD_DIR, "minification" + FileUtils.JAR_EXTENSION);
  private static final Path PROGUARD_FLAGS =
      Paths.get(ToolHelper.EXAMPLES_DIR, "minification",  "keep-rules.txt");

  private Path testFlags;

  @Before
  public void setup() throws IOException {
    testFlags = temp.newFile("local.flags").toPath();
    FileUtils.writeTextFile(testFlags, ImmutableList.of(
        "-printseeds " + SEEDS,
        "-printmapping " + MAPPING));
  }

  @Test
  public void testRun1Dir() throws IOException, CompilationException, ProguardRuleParserException {
    Path out = temp.newFolder("outdex").toPath();
    R8.run(getCommand(out));
    Assert.assertTrue(Files.isRegularFile(out.resolve(FileUtils.DEFAULT_DEX_FILENAME)));
    Assert.assertTrue(Files.isRegularFile(testFlags.getParent().resolve(MAPPING)));
    Assert.assertTrue(Files.isRegularFile(testFlags.getParent().resolve(SEEDS)));
  }

  @Test
  public void testRun1Zip() throws IOException, CompilationException, ProguardRuleParserException {
    Path out = temp.newFolder("outdex").toPath().resolve("dex.zip");
    R8.run(getCommand(out));
    Assert.assertTrue(Files.isRegularFile(out));
    Assert.assertTrue(Files.isRegularFile(testFlags.getParent().resolve(MAPPING)));
    Assert.assertTrue(Files.isRegularFile(testFlags.getParent().resolve(SEEDS)));
  }

  @Test
  public void testRun2Dir() throws IOException, CompilationException, ProguardRuleParserException {
    Path out = temp.newFolder("outdex").toPath();
    ExecutorService executor = Executors.newWorkStealingPool(2);
    try {
      R8.run(getCommand(out), executor);
    } finally {
      executor.shutdown();
    }
    Assert.assertTrue(Files.isRegularFile(out.resolve(FileUtils.DEFAULT_DEX_FILENAME)));
    Assert.assertTrue(Files.isRegularFile(testFlags.getParent().resolve(MAPPING)));
    Assert.assertTrue(Files.isRegularFile(testFlags.getParent().resolve(SEEDS)));
  }

  @Test
  public void testRun2Zip() throws IOException, CompilationException, ProguardRuleParserException {
    Path out = temp.newFolder("outdex").toPath().resolve("dex.zip");
    ExecutorService executor = Executors.newWorkStealingPool(2);
    try {
      R8.run(getCommand(out), executor);
    } finally {
      executor.shutdown();
    }
    Assert.assertTrue(Files.isRegularFile(out));
    Assert.assertTrue(Files.isRegularFile(testFlags.getParent().resolve(MAPPING)));
    Assert.assertTrue(Files.isRegularFile(testFlags.getParent().resolve(SEEDS)));
  }

  @Test
  public void testMainDir() throws IOException, InterruptedException {
    Path out = temp.newFolder("outdex").toPath();
    ProcessResult r8 = ToolHelper.forkR8(Paths.get("."),
        "--lib", ToolHelper.getDefaultAndroidJar(),
        "--output", out.toString(),
        "--pg-conf", PROGUARD_FLAGS.toString(),
        "--pg-conf", testFlags.toString(),
        INPUT_JAR.toString());
    Assert.assertEquals(0, r8.exitCode);
    Assert.assertTrue(Files.isRegularFile(out.resolve(FileUtils.DEFAULT_DEX_FILENAME)));
    Assert.assertTrue(Files.isRegularFile(testFlags.getParent().resolve(MAPPING)));
    Assert.assertTrue(Files.isRegularFile(testFlags.getParent().resolve(SEEDS)));
  }

  @Test
  public void testMainZip() throws IOException, InterruptedException {
    Path out = temp.newFolder("outdex").toPath().resolve("dex.zip");
    ProcessResult r8 = ToolHelper.forkR8(Paths.get("."),
        "--lib", ToolHelper.getDefaultAndroidJar(),
        "--output", out.toString(),
        "--pg-conf", PROGUARD_FLAGS.toString(),
        "--pg-conf", testFlags.toString(),
        INPUT_JAR.toString());
    Assert.assertEquals(0, r8.exitCode);
    Assert.assertTrue(Files.isRegularFile(out));
    Assert.assertTrue(Files.isRegularFile(testFlags.getParent().resolve(MAPPING)));
    Assert.assertTrue(Files.isRegularFile(testFlags.getParent().resolve(SEEDS)));
  }

  private R8Command getCommand(Path out) throws CompilationException, IOException {
    return R8Command.builder()
      .addLibraryFiles(Paths.get(ToolHelper.getDefaultAndroidJar()))
      .addProgramFiles(INPUT_JAR)
      .setOutputPath(out)
      .addProguardConfigurationFiles(PROGUARD_FLAGS, testFlags)
      .build();
  }
}
