// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.android.tools.r8.R8RunArtTestsTest.CompilerUnderTest;
import com.android.tools.r8.R8RunArtTestsTest.DexTool;
import com.android.tools.r8.ToolHelper.DexVm;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.android.tools.r8.utils.JarBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

  private static final Map<String, TestCondition> outputNotIdenticalToJVMOutput =
      new ImmutableMap.Builder<String, TestCondition>()
          // Traverses stack frames that contain Art specific frames.
          .put("throwing.Throwing", TestCondition.any())
          // Early art versions incorrectly print Float.MIN_VALUE.
          .put(
              "filledarray.FilledArray",
              TestCondition.match(
                  TestCondition.runtimes(DexVm.ART_6_0_1, DexVm.ART_5_1_1, DexVm.ART_4_4_4)))
          .build();

  // For local testing on a specific Art version(s) change this set. e.g. to
  // ImmutableSet.of("default") or pass the option -Ddex_vm=<version> to the Java VM.
  private static Set<DexVm> artVersions = ToolHelper.getArtVersions();

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
    if (fileType.equals(JAR_EXTENSION)) {
      return getOriginalJarFile();
    } else {
      assert fileType.equals(DEX_EXTENSION);
      return getOriginalDexFile();
    }
  }

  public Path getOriginalJarFile() {
    return Paths.get(EXAMPLE_DIR, name + JAR_EXTENSION);
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
    String out = temp.getRoot().getCanonicalPath();
    ToolHelper.runR8(getInputFile().toString(), out);
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

    ToolHelper.ProcessResult javaResult =
        ToolHelper.runJava(ImmutableList.of(getOriginalJarFile().toString()), mainClass);
    if (javaResult.exitCode != 0) {
      fail("JVM failed for: " + mainClass);
    }

    // TODO(ager): Once we have a bot running using dalvik (version 4.4.4) we should remove
    // this explicit loop to get rid of repeated testing on the buildbots.
    for (DexVm version : artVersions) {
      if (failsOn.containsKey(version) && failsOn.get(version).contains(getTestName())) {
        thrown.expect(Throwable.class);
      }

      // Check output against Art output on original dex file.
      String output =
          ToolHelper.checkArtOutputIdentical(original, generated.toString(), mainClass, version);

      // Check output against JVM output.
      if (shouldMatchJVMOutput(version)) {
        String javaOutput = javaResult.stdout;
        assertEquals(
            "JVM and Art output differ:\n" + "JVM:\n" + javaOutput + "\nArt:\n" + output,
            output,
            javaOutput);
      }
    }
  }

  private boolean shouldMatchJVMOutput(DexVm version) {
    TestCondition condition = outputNotIdenticalToJVMOutput.get(mainClass);
    if (condition == null) {
      return true;
    }
    return !condition.test(DexTool.NONE, CompilerUnderTest.R8, version, CompilationMode.RELEASE);
  }
}
