// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.R8Command;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.android.tools.r8.utils.DexInspector.ClassSubject;
import com.android.tools.r8.utils.DexInspector.MethodSubject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class R8InliningTest {

  private static final String JAR_EXTENSION = ".jar";
  private static final String DEFAULT_DEX_FILENAME = "classes.dex";
  private static final String DEFAULT_MAP_FILENAME = "proguard.map";

  @Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{{"Inlining"}});
  }

  @Rule
  public TemporaryFolder temp = ToolHelper.getTemporaryFolderForTest();
  private final String name;
  private final String keepRulesFile;

  public R8InliningTest(String name) {
    this.name = name.toLowerCase();
    this.keepRulesFile = ToolHelper.EXAMPLES_DIR + this.name + "/keep-rules.txt";
  }

  private Path getInputFile() {
    return Paths.get(ToolHelper.EXAMPLES_BUILD_DIR, name + JAR_EXTENSION);
  }

  private Path getOriginalDexFile() {
    return Paths.get(ToolHelper.EXAMPLES_BUILD_DIR, name, DEFAULT_DEX_FILENAME);
  }

  private Path getGeneratedDexFile() throws IOException {
    return Paths.get(temp.getRoot().getCanonicalPath(), DEFAULT_DEX_FILENAME);
  }

  private String getGeneratedProguardMap() throws IOException {
    Path mapFile = Paths.get(temp.getRoot().getCanonicalPath(), DEFAULT_MAP_FILENAME);
    if (Files.exists(mapFile)) {
      return mapFile.toAbsolutePath().toString();
    }
    return null;
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void generateR8Version()
      throws IOException, ProguardRuleParserException, ExecutionException, CompilationException {
    Path out = temp.getRoot().toPath();
    R8Command command =
        R8Command.builder()
            .addProgramFiles(getInputFile())
            .setOutputPath(out)
            .addProguardConfigurationFiles(Paths.get(keepRulesFile))
            .build();
    ToolHelper.runR8(command);
    ToolHelper.runArtNoVerificationErrors(out + "/classes.dex", "inlining.Inlining");
  }

  private void checkAbsent(ClassSubject clazz, String name) {
    assertTrue(clazz.isPresent());
    MethodSubject method = clazz.method("boolean", name, Collections.emptyList());
    assertFalse(method.isPresent());
  }

  private void dump(DexEncodedMethod method) {
    System.out.println(method);
    System.out.println(method.codeToString());
  }

  private void dump(Path path, String title) throws Throwable {
    System.out.println(title + ":");
    DexInspector inspector = new DexInspector(path.toAbsolutePath());
    inspector.clazz("inlining.Inlining").forAllMethods(m -> dump(m.getMethod()));
    System.out.println(title + " size: " + Files.size(path));
  }

  @Test
  public void checkNoInvokes() throws Throwable {
    DexInspector inspector = new DexInspector(getGeneratedDexFile().toAbsolutePath(),
        getGeneratedProguardMap());
    ClassSubject clazz = inspector.clazz("inlining.Inlining");
    // Simple constant inlining.
    checkAbsent(clazz, "longExpression");
    checkAbsent(clazz, "intExpression");
    checkAbsent(clazz, "doubleExpression");
    checkAbsent(clazz, "floatExpression");
    // Simple return argument inlining.
    checkAbsent(clazz, "longArgumentExpression");
    checkAbsent(clazz, "intArgumentExpression");
    checkAbsent(clazz, "doubleArgumentExpression");
    checkAbsent(clazz, "floatArgumentExpression");
  }

  @Test
  public void processedFileIsSmaller() throws Throwable {
    long original = Files.size(getOriginalDexFile());
    long generated = Files.size(getGeneratedDexFile());
    final boolean ALWAYS_DUMP = false;  // Used for debugging.
    if (ALWAYS_DUMP || generated > original) {
      dump(getOriginalDexFile(), "Original");
      dump(getGeneratedDexFile(), "Generated");
    }
    assertTrue("Inlining failed to reduce size", original > generated);
  }
}
