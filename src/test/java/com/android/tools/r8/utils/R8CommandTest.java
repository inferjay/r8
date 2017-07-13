// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import static com.android.tools.r8.ToolHelper.EXAMPLES_BUILD_DIR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.CompilationMode;
import com.android.tools.r8.R8Command;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.ToolHelper.ProcessResult;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class R8CommandTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Rule
  public TemporaryFolder temp = ToolHelper.getTemporaryFolderForTest();

  @Test
  public void emptyCommand() throws Throwable {
    verifyEmptyCommand(R8Command.builder().build());
    verifyEmptyCommand(parse());
    verifyEmptyCommand(parse(""));
    verifyEmptyCommand(parse("", ""));
    verifyEmptyCommand(parse(" "));
    verifyEmptyCommand(parse(" ", " "));
    verifyEmptyCommand(parse("\t"));
    verifyEmptyCommand(parse("\t", "\t"));
  }

  private void verifyEmptyCommand(R8Command command) {
    assertEquals(0, ToolHelper.getApp(command).getDexProgramResources().size());
    assertEquals(0, ToolHelper.getApp(command).getClassProgramResources().size());
    assertEquals(0, ToolHelper.getApp(command).getDexLibraryResources().size());
    assertEquals(0, ToolHelper.getApp(command).getClassLibraryResources().size());
    assertFalse(ToolHelper.getApp(command).hasMainDexList());
    assertFalse(ToolHelper.getApp(command).hasProguardMap());
    assertFalse(ToolHelper.getApp(command).hasProguardSeeds());
    assertFalse(ToolHelper.getApp(command).hasPackageDistribution());
    assertNull(command.getOutputPath());
    assertFalse(command.useMinification());
    assertFalse(command.useTreeShaking());
    assertEquals(CompilationMode.RELEASE, command.getMode());
  }

  @Test
  public void defaultOutIsCwd() throws IOException, InterruptedException {
    Path working = temp.getRoot().toPath();
    Path input = Paths.get(EXAMPLES_BUILD_DIR, "arithmetic.jar").toAbsolutePath();
    Path output = working.resolve("classes.dex");
    assertFalse(Files.exists(output));
    ProcessResult result = ToolHelper.forkR8(working, input.toString());
    assertEquals("R8 run failed: " + result.stderr, 0, result.exitCode);
    assertTrue(Files.exists(output));
  }

  @Test
  public void validOutputPath() throws Throwable {
    Path existingDir = temp.getRoot().toPath();
    Path nonExistingZip = existingDir.resolve("a-non-existing-archive.zip");
    assertEquals(
        existingDir,
        R8Command.builder().setOutputPath(existingDir).build().getOutputPath());
    assertEquals(
        nonExistingZip,
        R8Command.builder().setOutputPath(nonExistingZip).build().getOutputPath());
    assertEquals(
        existingDir,
        parse("--output", existingDir.toString()).getOutputPath());
    assertEquals(
        nonExistingZip,
        parse("--output", nonExistingZip.toString()).getOutputPath());
  }

  @Test
  public void existingOutputDirWithDexFiles() throws Throwable {
    Path existingDir = temp.newFolder().toPath();
    List<Path> classesFiles = ImmutableList.of(
        existingDir.resolve("classes.dex"),
        existingDir.resolve("classes2.dex"),
        existingDir.resolve("Classes3.dex"), // ignore case.
        existingDir.resolve("classes10.dex"),
        existingDir.resolve("classes999.dex"));
    List<Path> otherFiles = ImmutableList.of(
        existingDir.resolve("classes0.dex"),
        existingDir.resolve("classes1.dex"),
        existingDir.resolve("classes010.dex"),
        existingDir.resolve("classesN.dex"),
        existingDir.resolve("other.dex"));
    for (Path file : classesFiles) {
      Files.createFile(file);
      assertTrue(Files.exists(file));
    }
    for (Path file : otherFiles) {
      Files.createFile(file);
      assertTrue(Files.exists(file));
    }
    Path input = Paths.get(EXAMPLES_BUILD_DIR, "arithmetic.jar");
    ProcessResult result =
        ToolHelper.forkR8(Paths.get("."), input.toString(), "--output", existingDir.toString());
    assertEquals(0, result.exitCode);
    assertTrue(Files.exists(classesFiles.get(0)));
    for (int i = 1; i < classesFiles.size(); i++) {
      Path file = classesFiles.get(i);
      assertFalse("Expected stale file to be gone: " + file, Files.exists(file));
    }
    for (Path file : otherFiles) {
      assertTrue("Expected non-classes file to remain: " + file, Files.exists(file));
    }
  }

  @Test
  public void nonExistingOutputDir() throws Throwable {
    thrown.expect(CompilationException.class);
    Path nonExistingDir = temp.getRoot().toPath().resolve("a/path/that/does/not/exist");
    R8Command.builder().setOutputPath(nonExistingDir).build();
  }

  @Test
  public void existingOutputZip() throws Throwable {
    Path existingZip = temp.newFile("an-existing-archive.zip").toPath();
    R8Command.builder().setOutputPath(existingZip).build();
  }

  @Test
  public void invalidOutputFileType() throws Throwable {
    thrown.expect(CompilationException.class);
    Path invalidType = temp.getRoot().toPath().resolve("an-invalid-output-file-type.foobar");
    R8Command.builder().setOutputPath(invalidType).build();
  }

  @Test
  public void nonExistingOutputDirParse() throws Throwable {
    thrown.expect(CompilationException.class);
    Path nonExistingDir = temp.getRoot().toPath().resolve("a/path/that/does/not/exist");
    parse("--output", nonExistingDir.toString());
  }

  @Test
  public void existingOutputZipParse() throws Throwable {
    Path existingZip = temp.newFile("an-existing-archive.zip").toPath();
    parse("--output", existingZip.toString());
  }

  @Test
  public void invalidOutputFileTypeParse() throws Throwable {
    thrown.expect(CompilationException.class);
    Path invalidType = temp.getRoot().toPath().resolve("an-invalid-output-file-type.foobar");
    parse("--output", invalidType.toString());
  }

  @Test
  public void argumentsInFile() throws Throwable {
    Path inputFile = temp.newFile("foobar.dex").toPath();
    Path pgConfFile = temp.newFile("pgconf.config").toPath();
    Path argsFile = temp.newFile("more-args.txt").toPath();
    FileUtils.writeTextFile(argsFile, ImmutableList.of(
        "--debug --no-minification",
        "--pg-conf " + pgConfFile,
        inputFile.toString()
    ));
    R8Command command = parse("@" + argsFile.toString());
    assertEquals(CompilationMode.DEBUG, command.getMode());
    assertFalse(command.useMinification());
    assertTrue(command.useTreeShaking());
    assertEquals(1, ToolHelper.getApp(command).getDexProgramResources().size());
  }

  @Test
  public void nonExistingOutputJar() throws Throwable {
    Path nonExistingJar = temp.getRoot().toPath().resolve("non-existing-archive.jar");
    R8Command.builder().setOutputPath(nonExistingJar).build();
  }

  private R8Command parse(String... args)
      throws CompilationException, ProguardRuleParserException, IOException {
    return R8Command.parse(args).build();
  }
}
