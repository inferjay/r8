// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.compatdx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.maindexlist.MainDexListTests;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.FileUtils;
import com.android.tools.r8.utils.OutputMode;
import com.android.tools.r8.utils.StringUtils;
import com.android.tools.r8.utils.StringUtils.BraceType;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class CompatDxTests {
  private static final String EXAMPLE_JAR_FILE1 = "build/test/examples/arithmetic.jar";
  private static final String EXAMPLE_JAR_FILE2 = "build/test/examples/barray.jar";

  private static final String NO_LOCALS = "--no-locals";
  private static final String NO_POSITIONS = "--positions=none";
  private static final String MULTIDEX = "--multi-dex";
  private static final String NUM_THREADS_5 = "--num-threads=5";

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Rule
  public TemporaryFolder temp = ToolHelper.getTemporaryFolderForTest();

  @Test
  public void noFilesTest() throws IOException {
    runDexer("--no-files");
  }

  @Test
  public void noOutputTest() throws IOException {
    runDexerWithoutOutput(NO_POSITIONS, NO_LOCALS, MULTIDEX, EXAMPLE_JAR_FILE1);
  }

  @Test
  public void singleJarInputFile() throws IOException {
    runDexer(NO_POSITIONS, NO_LOCALS, MULTIDEX, EXAMPLE_JAR_FILE1);
  }

  @Test
  public void multipleJarInputFiles() throws IOException {
    runDexer(NO_POSITIONS, NO_LOCALS, MULTIDEX, EXAMPLE_JAR_FILE1, EXAMPLE_JAR_FILE2);
  }

  @Test
  public void outputZipFile() throws IOException {
    runDexerWithOutput("foo.dex.zip", NO_POSITIONS, NO_LOCALS, MULTIDEX, EXAMPLE_JAR_FILE1);
  }

  @Test
  public void useMultipleThreads() throws IOException {
    runDexer(NUM_THREADS_5, NO_POSITIONS, NO_LOCALS, EXAMPLE_JAR_FILE1);
  }

  @Test
  public void withPositions() throws IOException {
    runDexer(NO_LOCALS, MULTIDEX, EXAMPLE_JAR_FILE1);
  }

  @Test
  public void withLocals() throws IOException {
    runDexer(NO_POSITIONS, MULTIDEX, EXAMPLE_JAR_FILE1);
  }

  @Test
  public void withoutMultidex() throws IOException {
    runDexer(NO_POSITIONS, NO_LOCALS, EXAMPLE_JAR_FILE1);
  }

  @Test
  public void writeToNamedDexFile() throws IOException {
    runDexerWithOutput("named-output.dex", EXAMPLE_JAR_FILE1);
  }

  @Test
  public void singleDexProgramFull() throws IOException, ExecutionException {
    // Generate an application that fills the whole dex file.
    AndroidApp generated =
        MainDexListTests.generateApplication(
            ImmutableList.of("A"), Constants.ANDROID_L_API, Constants.U16BIT_MAX + 1);
    Path applicationJar = temp.newFile("application.jar").toPath();
    generated.write(applicationJar, OutputMode.Indexed);
    runDexer(applicationJar.toString());
  }

  @Test
  public void singleDexProgramIsTooLarge() throws IOException, ExecutionException {
    // Generate an application that will not fit into a single dex file.
    AndroidApp generated = MainDexListTests.generateApplication(
        ImmutableList.of("A", "B"), Constants.ANDROID_L_API, Constants.U16BIT_MAX / 2 + 2);
    Path applicationJar = temp.newFile("application.jar").toPath();
    generated.write(applicationJar, OutputMode.Indexed);
    thrown.expect(CompilationError.class);
    runDexer(applicationJar.toString());
  }

  @Test
  public void keepClassesTest() throws IOException {
    runDexerWithOutput("out.zip", "--keep-classes", EXAMPLE_JAR_FILE1);
  }

  private void runDexer(String... args) throws IOException {
    runDexerWithOutput("", args);
  }

  private void runDexerWithoutOutput(String... args) throws IOException {
    runDexerWithOutput(null, args);
  }

  private Path getOutputD8() {
    return temp.getRoot().toPath().resolve("d8-out");
  }

  private Path getOutputDX() {
    return temp.getRoot().toPath().resolve("dx-out");
  }

  private void runDexerWithOutput(String out, String... args) throws IOException {
    Path d8Out = null;
    Path dxOut = null;
    if (out != null) {
      Path baseD8 = getOutputD8();
      Path baseDX = getOutputDX();
      Files.createDirectory(baseD8);
      Files.createDirectory(baseDX);
      d8Out = baseD8.resolve(out);
      dxOut = baseDX.resolve(out);
      assertNotEquals(d8Out, dxOut);
    }

    List<String> d8Args = new ArrayList<>(args.length + 2);
    d8Args.add("--dex");
    if (d8Out != null) {
      d8Args.add("--output=" + d8Out);
    }
    Collections.addAll(d8Args, args);
    System.out.println("running: d8 " + StringUtils.join(d8Args, " "));
    CompatDx.main(d8Args.toArray(new String[d8Args.size()]));

    List<String> dxArgs = new ArrayList<>(args.length + 2);
    if (dxOut != null) {
      dxArgs.add("--output=" + dxOut);
    }
    Collections.addAll(dxArgs, args);
    System.out.println("running: dx " + StringUtils.join(dxArgs, " "));
    ToolHelper.runDX(dxArgs.toArray(new String[dxArgs.size()]));

    if (out == null) {
      // Can't check output if explicitly not writing any.
      return;
    }

    List<Path> d8Files = Files.list(Files.isDirectory(d8Out) ? d8Out : d8Out.getParent())
        .sorted().collect(Collectors.toList());
    List<Path> dxFiles = Files.list(Files.isDirectory(dxOut) ? dxOut : dxOut.getParent())
        .sorted().collect(Collectors.toList());
    assertEquals("Out file names differ",
        StringUtils.join(dxFiles, "\n", BraceType.NONE, (file) ->
            file.getFileName().toString()),
        StringUtils.join(d8Files, "\n", BraceType.NONE, (file) ->
            file.getFileName().toString()));

    for (int i = 0; i < d8Files.size(); i++) {
      if (FileUtils.isArchive(d8Files.get(i))) {
        compareArchiveFiles(d8Files.get(i), dxFiles.get(i));
      }
    }
  }

  private void compareArchiveFiles(Path d8File, Path dxFile) throws IOException {
    ZipFile d8Zip = new ZipFile(d8File.toFile());
    ZipFile dxZip = new ZipFile(dxFile.toFile());
    // TODO(zerny): This should test resource containment too once supported.
    Set<String> d8Content = d8Zip.stream().map(ZipEntry::getName).collect(Collectors.toSet());
    Set<String> dxContent = dxZip.stream().map(ZipEntry::getName).collect(Collectors.toSet());
    for (String entry : d8Content) {
      assertTrue("Expected dx output to contain " + entry, dxContent.contains(entry));
    }
    for (String entry : dxContent) {
      Path path = Paths.get(entry);
      if (FileUtils.isDexFile(path) || FileUtils.isClassFile(path)) {
        assertTrue("Expected d8 output to contain " + entry, d8Content.contains(entry));
      }
    }
  }
}
