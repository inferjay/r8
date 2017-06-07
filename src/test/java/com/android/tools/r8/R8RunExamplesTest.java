// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.android.tools.r8.ToolHelper.DexVm;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.android.tools.r8.utils.DexInspector;
import com.android.tools.r8.utils.JarBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
public class R8RunExamplesTest {

  private static final String EXAMPLE_DIR = ToolHelper.EXAMPLES_BUILD_DIR;
  private static final String JAR_EXTENSION = ".jar";
  private static final String DEX_EXTENSION = ".dex";
  private static final String DEFAULT_DEX_FILENAME = "classes.dex";

  private static final String[] failsWithJar = {};

  // For local testing on a specific Art version(s) change this set. e.g. to
  // ImmutableSet.of("default") or pass the option -Ddex_vm=<version> to the Java VM.
  private static Set<DexVm> artVersions = ToolHelper.getArtVersions();

  // A set of class names for examples that might produce bigger output and thus are excluded from
  // size testing.
  private static Set<String> mayBeBigger = ImmutableSet.of(
      // Contains a reference to an extra type due to member rebinding.
      "throwing.Throwing"
  );

  @Parameters(name = "{0}{1}")
  public static Collection<String[]> data() {
    String[] tests = {
        "arithmetic.Arithmetic",
        "arrayaccess.ArrayAccess",
        "barray.BArray",
        "bridge.BridgeMethod",
        "cse.CommonSubexpressionElimination",
        "constants.Constants",
        "controlflow.ControlFlow",
        "conversions.Conversions",
        "floating_point_annotations.FloatingPointValuedAnnotationTest",
        "filledarray.FilledArray",
        "hello.Hello",
        "ifstatements.IfStatements",
        "instancevariable.InstanceVariable",
        "instanceofstring.InstanceofString",
        "invoke.Invoke",
        "jumbostring.JumboString",
        "loadconst.LoadConst",
        "newarray.NewArray",
        "regalloc.RegAlloc",
        "returns.Returns",
        "staticfield.StaticField",
        "stringbuilding.StringBuilding",
        "switches.Switches",
        "sync.Sync",
        "throwing.Throwing",
        "trivial.Trivial",
        "trycatch.TryCatch",
        "nestedtrycatches.NestedTryCatches",
        "trycatchmany.TryCatchMany",
        "invokeempty.InvokeEmpty",
        "regress.Regress",
        "regress2.Regress2",
        "regress_37726195.Regress",
        "regress_37658666.Regress",
        "regress_37875803.Regress",
        "regress_37955340.Regress",
        "regress_62300145.Regress",
        "memberrebinding2.Test",
        "memberrebinding3.Test",
        "minification.Minification",
        "enclosingmethod.Main",
        "interfaceinlining.Main",
        "switchmaps.Switches",
    };

    List<String[]> fullTestList = new ArrayList<>(tests.length * 2);
    for (String test : tests) {
      String qualified = test;
      String pkg = qualified.substring(0, qualified.lastIndexOf('.'));
      fullTestList.add(new String[]{pkg, DEX_EXTENSION, qualified});
      fullTestList.add(new String[]{pkg, JAR_EXTENSION, qualified});
    }
    return fullTestList;
  }

  @Rule
  public TemporaryFolder temp = ToolHelper.getTemporaryFolderForTest();
  private final String name;

  private final String mainClass;
  private final String fileType;
  private static Map<DexVm, List<String>> failsOn =
      ImmutableMap.of(
          DexVm.ART_4_4_4, ImmutableList.of(
              "vmdebug.dex",
              "vmdebug.jar",
              "memberrebinding2.dex", // b/38187737
              "memberrebinding2.jar" // b/38187737
          ),
          DexVm.ART_5_1_1, ImmutableList.of(
              "vmdebug.dex",
              "vmdebug.jar",
              "memberrebinding2.dex", // b/38187737
              "memberrebinding2.jar" // b/38187737
          ),
          DexVm.ART_6_0_1, ImmutableList.of(
              "vmdebug.dex",
              "vmdebug.jar",
              "memberrebinding2.dex", // b/38187737
              "memberrebinding2.jar" // b/38187737
          ),
          DexVm.ART_7_0_0, ImmutableList.of(
              "memberrebinding2.dex", // b/38187737
              "memberrebinding2.jar" // b/38187737
          ),
          DexVm.ART_DEFAULT, ImmutableList.of(
              "memberrebinding2.dex", // b/38187737
              "memberrebinding2.jar" // b/38187737
          )
      );

  public R8RunExamplesTest(String name, String fileType, String mainClass) {
    this.name = name;
    this.fileType = fileType;
    this.mainClass = mainClass;
  }

  private Path getInputFile() {
    if (fileType == JAR_EXTENSION) {
      return Paths.get(EXAMPLE_DIR, name + JAR_EXTENSION);
    } else {
      assert fileType == DEX_EXTENSION;
      return getOriginalDexFile();
    }
  }

  private Path getOriginalDexFile() {
    return Paths.get(EXAMPLE_DIR, name, DEFAULT_DEX_FILENAME);
  }

  private Path getGeneratedDexFile() throws IOException {
    return Paths.get(temp.getRoot().getCanonicalPath(), DEFAULT_DEX_FILENAME);
  }

  private String getTestName() {
    return this.name + this.fileType;
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void generateR8Version()
      throws IOException, ProguardRuleParserException, ExecutionException, CompilationException {
    if (fileType == JAR_EXTENSION && Arrays.asList(failsWithJar).contains(name)) {
      thrown.expect(Throwable.class);
    }
    String out = temp.getRoot().getCanonicalPath();
    ToolHelper.runR8(getInputFile().toString(), out);
  }

  @Test
  public void processedFileIsSmaller() throws IOException {
    if (mayBeBigger.contains(mainClass)) {
      return;
    }
    long original = Files.size(getOriginalDexFile());
    long generated = Files.size(getGeneratedDexFile());

    if (generated > original) {
      DexInspector inspectOriginal = null;
      DexInspector inspectGenerated = null;
      try {
        inspectOriginal = new DexInspector(getOriginalDexFile());
        inspectGenerated = new DexInspector(getGeneratedDexFile());
      } catch (Throwable e) {
        System.err.println("Failed to parse dex files for post-failure processing");
        e.printStackTrace();
      }
      if (inspectGenerated != null && inspectOriginal != null) {
        assertEquals("Generated file is larger than original: " + generated + " vs. " + original,
            inspectOriginal.clazz(mainClass).dumpMethods(),
            inspectGenerated.clazz(mainClass).dumpMethods());
      }
    }
    assertTrue("Generated file is larger than original: " + generated + " vs. " + original,
        generated <= original);
  }

  @Test
  public void outputIsIdentical() throws IOException, InterruptedException, ExecutionException {
    if (!ToolHelper.artSupported()) {
      return;
    }

    String original = getOriginalDexFile().toString();

    File generated;
    // Collect the generated dex files.
    File[] outputFiles =
        temp.getRoot().listFiles((File file) -> file.getName().endsWith(".dex"));
    if (outputFiles.length == 1) {
      // Just run Art on classes.dex.
      generated = outputFiles[0];
    } else {
      // Run Art on JAR file with multiple dex files.
      generated = temp.getRoot().toPath().resolve(name + ".jar").toFile();
      JarBuilder.buildJar(outputFiles, generated);
    }
    // TODO(ager): Once we have a bot running using dalvik (version 4.4.4) we should remove
    // this explicit loop to get rid of repeated testing on the buildbots.
    for (DexVm version : artVersions) {
      boolean expectedToFail = false;
      if (failsOn.containsKey(version) && failsOn.get(version).contains(getTestName())) {
        expectedToFail = true;
        thrown.expect(Throwable.class);
      }
      String output =
          ToolHelper.checkArtOutputIdentical(original, generated.toString(), mainClass, version);
    }
  }
}
