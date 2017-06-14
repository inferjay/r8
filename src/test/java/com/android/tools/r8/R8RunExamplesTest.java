// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import static com.android.tools.r8.TestCondition.R8_COMPILER;
import static com.android.tools.r8.TestCondition.match;
import static com.android.tools.r8.utils.FileUtils.JAR_EXTENSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.android.tools.r8.R8RunArtTestsTest.CompilerUnderTest;
import com.android.tools.r8.R8RunArtTestsTest.DexTool;
import com.android.tools.r8.ToolHelper.DexVm;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.android.tools.r8.utils.FileUtils;
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

  // For local testing on a specific Art version(s) change this set. e.g. to
  // ImmutableSet.of(DexVm.ART_DEFAULT) or pass the option -Ddex_vm=<version> to the Java VM.
  private static final Set<DexVm> artVersions = ToolHelper.getArtVersions();

  // Tests failing to run.
  private static final Map<String, TestCondition> failingRun =
      new ImmutableMap.Builder<String, TestCondition>()
          .put("memberrebinding2.Test", match(R8_COMPILER)) // b/38187737
          .build();

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

  @Parameters(name = "{0}_{1}_{2}_{3}")
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
        "memberrebinding2.Memberrebinding",
        "memberrebinding3.Memberrebinding",
        "minification.Minification",
        "enclosingmethod.Main",
        "interfaceinlining.Main",
        "switchmaps.Switches",
    };

    List<String[]> fullTestList = new ArrayList<>(tests.length * 2);
    for (String test : tests) {
      fullTestList.add(makeTest(DexTool.NONE, CompilerUnderTest.D8, CompilationMode.DEBUG, test));
      fullTestList.add(makeTest(DexTool.NONE, CompilerUnderTest.R8, CompilationMode.RELEASE, test));
      fullTestList.add(makeTest(DexTool.DX, CompilerUnderTest.R8, CompilationMode.RELEASE, test));
    }
    return fullTestList;
  }

  private static String[] makeTest(
      DexTool tool, CompilerUnderTest compiler, CompilationMode mode, String clazz) {
    String pkg = clazz.substring(0, clazz.lastIndexOf('.'));
    return new String[]{pkg, tool.name(), compiler.name(), mode.name(), clazz};
  }

  @Rule
  public TemporaryFolder temp = ToolHelper.getTemporaryFolderForTest();

  private final DexTool tool;
  private final CompilerUnderTest compiler;
  private final CompilationMode mode;
  private final String pkg;
  private final String mainClass;

  public R8RunExamplesTest(
      String pkg,
      String tool,
      String compiler,
      String mode,
      String mainClass) {
    this.pkg = pkg;
    this.tool = DexTool.valueOf(tool);
    this.compiler = CompilerUnderTest.valueOf(compiler);
    this.mode = CompilationMode.valueOf(mode);
    this.mainClass = mainClass;
  }

  private Path getInputFile() {
    if (tool == DexTool.NONE) {
      return getOriginalJarFile();
    }
    assert tool == DexTool.DX;
    return getOriginalDexFile();
  }

  public Path getOriginalJarFile() {
    return Paths.get(EXAMPLE_DIR, pkg + JAR_EXTENSION);
  }

  private Path getOriginalDexFile() {
    return Paths.get(EXAMPLE_DIR, pkg, FileUtils.DEFAULT_DEX_FILENAME);
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void compile()
      throws IOException, ProguardRuleParserException, ExecutionException, CompilationException {
    Path out = temp.getRoot().toPath();
    switch (compiler) {
      case D8: {
        ToolHelper.runD8(D8Command.builder()
            .addProgramFiles(getInputFile())
            .setOutputPath(out)
            .setMode(mode)
            .build());
        break;
      }
      case R8: {
        ToolHelper.runR8(R8Command.builder()
            .addProgramFiles(getInputFile())
            .setOutputPath(out)
            .setMode(mode)
            .build());
        break;
      }
      default:
        throw new Unreachable();
    }
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
      generated = temp.getRoot().toPath().resolve(pkg + ".jar").toFile();
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
      TestCondition condition = failingRun.get(mainClass);
      if (condition != null && condition.test(tool, compiler, version, mode)) {
        thrown.expect(Throwable.class);
      } else {
        thrown = ExpectedException.none();
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
    return condition == null || !condition.test(tool, compiler, version, mode);
  }
}
