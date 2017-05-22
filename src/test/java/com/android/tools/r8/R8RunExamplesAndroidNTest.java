// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8;

import static com.android.tools.r8.utils.FileUtils.JAR_EXTENSION;
import static com.android.tools.r8.utils.FileUtils.ZIP_EXTENSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.android.tools.r8.ToolHelper.DexVm;
import com.android.tools.r8.ToolHelper.ProcessResult;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.OffOrAuto;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class R8RunExamplesAndroidNTest {

  private static final String EXAMPLE_DIR = ToolHelper.EXAMPLES_ANDROID_N_BUILD_DIR;

  private static Map<DexVm, List<String>> failsOn =
      ImmutableMap.of(
          DexVm.ART_4_4_4,
          ImmutableList.of(
              // Dex version not supported
              "staticinterfacemethods", "defaultmethods"),
          DexVm.ART_5_1_1,
          ImmutableList.of(
              // Dex version not supported
              "staticinterfacemethods", "defaultmethods"),
          DexVm.ART_6_0_1,
          ImmutableList.of(
              // Dex version not supported
              "staticinterfacemethods", "defaultmethods"),
          DexVm.ART_7_0_0,
          ImmutableList.of(),
          DexVm.ART_DEFAULT,
          ImmutableList.of());

  @Rule public TemporaryFolder temp = ToolHelper.getTemporaryFolderForTest();

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void staticInterfaceMethods() throws Throwable {
    doTest(
        "staticinterfacemethods",
        "interfacemethods",
        "StaticInterfaceMethods",
        Constants.ANDROID_N_API,
        options -> options.interfaceMethodDesugaring = OffOrAuto.Auto);
  }

  @Test
  public void staticInterfaceMethodsErrorDueToMinSdk() throws Throwable {
    thrown.expect(CompilationError.class);
    doTest(
        "staticinterfacemethods-error-due-to-min-sdk",
        "interfacemethods",
        "StaticInterfaceMethods");
  }

  @Test
  public void defaultMethods() throws Throwable {
    doTest(
        "defaultmethods",
        "interfacemethods",
        "DefaultMethods",
        Constants.ANDROID_N_API,
        options -> options.interfaceMethodDesugaring = OffOrAuto.Auto);
  }

  @Test
  public void defaultMethodsErrorDueToMinSdk() throws Throwable {
    thrown.expect(CompilationError.class);
    doTest("defaultmethods-error-due-to-min-sdk", "interfacemethods", "DefaultMethods");
  }

  private void doTest(String testName, String packageName, String className) throws Throwable {
    doTest(testName, packageName, className, R8Command.builder(), options -> {});
  }

  private void doTest(
      String testName,
      String packageName,
      String className,
      int minSdk,
      Consumer<InternalOptions> optionsConsumer)
      throws Throwable {
    doTest(
        testName,
        packageName,
        className,
        R8Command.builder().setMinApiLevel(minSdk),
        optionsConsumer);
  }

  public void doTest(
      String testName,
      String packageName,
      String className,
      R8Command.Builder builder,
      Consumer<InternalOptions> optionsConsumer)
      throws Throwable {
    String mainClass = packageName + "." + className;
    Path inputFile = Paths.get(EXAMPLE_DIR, packageName + JAR_EXTENSION);
    Path out = temp.getRoot().toPath().resolve(testName + ZIP_EXTENSION);

    try {
      ToolHelper.runR8(
          builder.addProgramFiles(inputFile).setOutputPath(out).build(), optionsConsumer);
    } catch (ExecutionException e) {
      throw e.getCause();
    }

    if (!ToolHelper.artSupported()) {
      return;
    }

    boolean expectedToFail = false;
    if (failsOn.containsKey(ToolHelper.getDexVm())
        && failsOn.get(ToolHelper.getDexVm()).contains(testName)) {
      expectedToFail = true;
      thrown.expect(Throwable.class);
    }
    String output = ToolHelper.runArtNoVerificationErrors(out.toString(), mainClass);
    if (!expectedToFail) {
      ProcessResult javaResult =
          ToolHelper.runJava(ImmutableList.of(inputFile.toString()), mainClass);
      assertEquals("JVM run failed", javaResult.exitCode, 0);
      assertTrue(
          "JVM output does not match art output.\n\tjvm: "
              + javaResult.stdout
              + "\n\tart: "
              + output,
          output.equals(javaResult.stdout));
    }
  }
}
