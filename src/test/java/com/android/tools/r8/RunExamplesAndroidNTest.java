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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public abstract class RunExamplesAndroidNTest<B> {

  private static final String EXAMPLE_DIR = ToolHelper.EXAMPLES_ANDROID_N_BUILD_DIR;

  abstract class TestRunner {
    final String testName;
    final String packageName;
    final String mainClass;

    final List<Consumer<InternalOptions>> optionConsumers = new ArrayList<>();
    final List<UnaryOperator<B>> builderTransformations = new ArrayList<>();

    TestRunner(String testName, String packageName, String mainClass) {
      this.testName = testName;
      this.packageName = packageName;
      this.mainClass = mainClass;
    }

    TestRunner withOptionConsumer(Consumer<InternalOptions> consumer) {
      optionConsumers.add(consumer);
      return this;
    }

    TestRunner withInterfaceMethodDesugaring(OffOrAuto behavior) {
      return withOptionConsumer(o -> o.interfaceMethodDesugaring = behavior);
    }

    TestRunner withBuilderTransformation(UnaryOperator<B> builderTransformation) {
      builderTransformations.add(builderTransformation);
      return this;
    }

    void combinedOptionConsumer(InternalOptions options) {
      for (Consumer<InternalOptions> consumer : optionConsumers) {
        consumer.accept(options);
      }
    }

    void run() throws Throwable {
      String qualifiedMainClass = packageName + "." + mainClass;
      Path inputFile = Paths.get(EXAMPLE_DIR, packageName + JAR_EXTENSION);
      Path out = temp.getRoot().toPath().resolve(testName + ZIP_EXTENSION);

      build(inputFile, out);

      if (!ToolHelper.artSupported()) {
        return;
      }

      boolean expectedToFail = false;
      if (failsOn.containsKey(ToolHelper.getDexVm())
          && failsOn.get(ToolHelper.getDexVm()).contains(testName)) {
        expectedToFail = true;
        thrown.expect(Throwable.class);
      }
      String output = ToolHelper.runArtNoVerificationErrors(out.toString(), qualifiedMainClass);
      if (!expectedToFail) {
        ProcessResult javaResult =
            ToolHelper.runJava(ImmutableList.of(inputFile.toString()), qualifiedMainClass);
        assertEquals("JVM run failed", javaResult.exitCode, 0);
        assertTrue(
            "JVM output does not match art output.\n\tjvm: "
                + javaResult.stdout
                + "\n\tart: "
                + output,
            output.equals(javaResult.stdout));
      }
    }

    abstract TestRunner withMinApiLevel(int minApiLevel);

    abstract void build(Path inputFile, Path out) throws Throwable;
  }

  private static Map<DexVm, List<String>> failsOn =
      ImmutableMap.of(
          DexVm.ART_4_4_4,
          ImmutableList.of(),
          DexVm.ART_5_1_1,
          ImmutableList.of(),
          DexVm.ART_6_0_1,
          ImmutableList.of(),
          DexVm.ART_7_0_0,
          ImmutableList.of(),
          DexVm.ART_DEFAULT,
          ImmutableList.of());

  @Rule public TemporaryFolder temp = ToolHelper.getTemporaryFolderForTest();

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void staticInterfaceMethods() throws Throwable {
    test("staticinterfacemethods", "interfacemethods", "StaticInterfaceMethods")
        .withMinApiLevel(Constants.ANDROID_K_API)
        .withInterfaceMethodDesugaring(OffOrAuto.Auto)
        .run();
  }

  @Test
  public void staticInterfaceMethodsErrorDueToMinSdk() throws Throwable {
    thrown.expect(CompilationError.class);
    test("staticinterfacemethods-error-due-to-min-sdk", "interfacemethods",
        "StaticInterfaceMethods")
        .run();
  }

  @Test
  public void defaultMethods() throws Throwable {
    test("defaultmethods", "interfacemethods", "DefaultMethods")
        .withMinApiLevel(Constants.ANDROID_K_API)
        .withInterfaceMethodDesugaring(OffOrAuto.Auto)
        .run();
  }

  @Test
  public void defaultMethodsErrorDueToMinSdk() throws Throwable {
    thrown.expect(CompilationError.class);
    test("defaultmethods-error-due-to-min-sdk", "interfacemethods",
        "DefaultMethods")
        .run();
  }

  abstract TestRunner test(String testName, String packageName, String mainClass);
}
