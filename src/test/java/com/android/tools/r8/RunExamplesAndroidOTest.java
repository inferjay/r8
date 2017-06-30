// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8;

import static com.android.tools.r8.dex.Constants.ANDROID_K_API;
import static com.android.tools.r8.dex.Constants.ANDROID_O_API;
import static com.android.tools.r8.utils.FileUtils.JAR_EXTENSION;
import static com.android.tools.r8.utils.FileUtils.ZIP_EXTENSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.android.tools.r8.ToolHelper.DexVm;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.utils.DexInspector;
import com.android.tools.r8.utils.DexInspector.FoundClassSubject;
import com.android.tools.r8.utils.DexInspector.FoundMethodSubject;
import com.android.tools.r8.utils.DexInspector.InstructionSubject;
import com.android.tools.r8.utils.DexInspector.InvokeInstructionSubject;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.OffOrAuto;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public abstract class RunExamplesAndroidOTest<B> {
  static final String EXAMPLE_DIR = ToolHelper.EXAMPLES_ANDROID_O_BUILD_DIR;

  abstract class TestRunner {
    final String testName;
    final String packageName;
    final String mainClass;

    final List<Consumer<InternalOptions>> optionConsumers = new ArrayList<>();
    final List<Consumer<DexInspector>> dexInspectorChecks = new ArrayList<>();
    final List<UnaryOperator<B>> builderTransformations = new ArrayList<>();

    TestRunner(String testName, String packageName, String mainClass) {
      this.testName = testName;
      this.packageName = packageName;
      this.mainClass = mainClass;
    }

    TestRunner withDexCheck(Consumer<DexInspector> check) {
      dexInspectorChecks.add(check);
      return this;
    }

    TestRunner withClassCheck(Consumer<FoundClassSubject> check) {
      withDexCheck(inspector -> inspector.forAllClasses(check));
      return this;
    }

    TestRunner withMethodCheck(Consumer<FoundMethodSubject> check) {
      withClassCheck(clazz -> clazz.forAllMethods(check));
      return this;
    }

    <T extends InstructionSubject> TestRunner
    withInstructionCheck(Predicate<InstructionSubject> filter, Consumer<T> check) {
      withMethodCheck(method -> {
        if (method.isAbstract()) {
          return;
        }
        Iterator<T> iterator = method.iterateInstructions(filter);
        while (iterator.hasNext()) {
          check.accept(iterator.next());
        }
      });
      return this;
    }

    TestRunner withOptionConsumer(Consumer<InternalOptions> consumer) {
      optionConsumers.add(consumer);
      return this;
    }

    TestRunner withInterfaceMethodDesugaring(OffOrAuto behavior) {
      return withOptionConsumer(o -> o.interfaceMethodDesugaring = behavior);
    }

    TestRunner withTryWithResourcesDesugaring(OffOrAuto behavior) {
      return withOptionConsumer(o -> o.tryWithResourcesDesugaring = behavior);
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
      if (compilationErrorExpected(testName)) {
        thrown.expect(CompilationError.class);
      }

      String qualifiedMainClass = packageName + "." + mainClass;
      Path inputFile = Paths.get(EXAMPLE_DIR, packageName + JAR_EXTENSION);
      Path out = temp.getRoot().toPath().resolve(testName + ZIP_EXTENSION);

      build(inputFile, out);

      if (!ToolHelper.artSupported()) {
        return;
      }

      boolean expectedToFail = expectedToFail(testName);
      if (expectedToFail) {
        thrown.expect(Throwable.class);
      }

      if (!dexInspectorChecks.isEmpty()) {
        DexInspector inspector = new DexInspector(out);
        for (Consumer<DexInspector> check : dexInspectorChecks) {
          check.accept(inspector);
        }
      }

      String output = ToolHelper.runArtNoVerificationErrors(out.toString(), qualifiedMainClass);
      if (!expectedToFail) {
        ToolHelper.ProcessResult javaResult =
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

  private static List<String> compilationErrorExpected =
      ImmutableList.of(
          "invokepolymorphic-error-due-to-min-sdk", "invokecustom-error-due-to-min-sdk");

  private static Map<DexVm, List<String>> failsOn =
      ImmutableMap.of(
          DexVm.ART_4_4_4, ImmutableList.of(
              // API not supported
              "paramnames",
              "repeat_annotations_new_api",
              // Dex version not supported
              "invokepolymorphic",
              "invokecustom"
          ),
          DexVm.ART_5_1_1, ImmutableList.of(
              // API not supported
              "paramnames",
              "repeat_annotations_new_api",
              // Dex version not supported
              "invokepolymorphic",
              "invokecustom"
          ),
          DexVm.ART_6_0_1, ImmutableList.of(
              // API not supported
              "paramnames",
              "repeat_annotations_new_api",
              // Dex version not supported
              "invokepolymorphic",
              "invokecustom"
          ),
          DexVm.ART_7_0_0, ImmutableList.of(
              // API not supported
              "paramnames",
              // Dex version not supported
              "invokepolymorphic",
              "invokecustom"
          ),
          DexVm.ART_DEFAULT, ImmutableList.of(
          )
      );

  @Rule
  public TemporaryFolder temp = ToolHelper.getTemporaryFolderForTest();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  boolean failsOn(Map<ToolHelper.DexVm, List<String>> failsOn, String name) {
    return failsOn.containsKey(ToolHelper.getDexVm())
        && failsOn.get(ToolHelper.getDexVm()).contains(name);
  }

  boolean expectedToFail(String name) {
    return failsOn(failsOn, name);
  }

  boolean compilationErrorExpected(String testName) {
    return compilationErrorExpected.contains(testName);
  }

  @Test
  public void invokeCustom() throws Throwable {
    test("invokecustom", "invokecustom", "InvokeCustom")
        .withMinApiLevel(ANDROID_O_API)
        .run();
  }

  @Test
  public void invokeCustomErrorDueToMinSdk() throws Throwable {
    test("invokecustom-error-due-to-min-sdk", "invokecustom", "InvokeCustom")
        .withMinApiLevel(25)
        .run();
  }

  @Test
  public void invokePolymorphic() throws Throwable {
    test("invokepolymorphic", "invokepolymorphic", "InvokePolymorphic")
        .withMinApiLevel(ANDROID_O_API)
        .run();
  }

  @Test
  public void invokePolymorphicErrorDueToMinSdk() throws Throwable {
    test("invokepolymorphic-error-due-to-min-sdk", "invokepolymorphic", "InvokePolymorphic")
        .withMinApiLevel(25)
        .run();
  }

  @Test
  public void lambdaDesugaring() throws Throwable {
    test("lambdadesugaring", "lambdadesugaring", "LambdaDesugaring")
        .withMinApiLevel(ANDROID_K_API)
        .run();
  }

  @Test
  public void lambdaDesugaringNPlus() throws Throwable {
    test("lambdadesugaringnplus", "lambdadesugaringnplus", "LambdasWithStaticAndDefaultMethods")
        .withMinApiLevel(ANDROID_K_API)
        .withInterfaceMethodDesugaring(OffOrAuto.Auto)
        .run();
  }

  @Test
  public void lambdaDesugaringValueAdjustments() throws Throwable {
    test("lambdadesugaring-value-adjustments", "lambdadesugaring", "ValueAdjustments")
        .withMinApiLevel(ANDROID_K_API)
        .run();
  }

  @Test
  public void paramNames() throws Throwable {
    test("paramnames", "paramnames", "ParameterNames")
        .withMinApiLevel(26)
        .withOptionConsumer((internalOptions) -> internalOptions.allowParameterName = true)
        .run();
  }

  @Test
  public void repeatAnnotationsNewApi() throws Throwable {
    // No need to specify minSdk as repeat annotations are handled by javac and we do not have
    // to do anything to support them. The library methods to access them just have to be in
    // the system.
    test("repeat_annotations_new_api", "repeat_annotations", "RepeatAnnotationsNewApi").run();
  }

  @Test
  public void repeatAnnotations() throws Throwable {
    // No need to specify minSdk as repeat annotations are handled by javac and we do not have
    // to do anything to support them. The library methods to access them just have to be in
    // the system.
    test("repeat_annotations", "repeat_annotations", "RepeatAnnotations").run();
  }

  @Test
  public void testTryWithResources() throws Throwable {
    test("try-with-resources-simplified", "trywithresources", "TryWithResourcesNotDesugaredTests")
        .withTryWithResourcesDesugaring(OffOrAuto.Off)
        .run();
  }

  @Test
  public void testTryWithResourcesDesugared() throws Throwable {
    test("try-with-resources-simplified", "trywithresources", "TryWithResourcesDesugaredTests")
        .withTryWithResourcesDesugaring(OffOrAuto.Auto)
        .withInstructionCheck(InstructionSubject::isInvoke,
            (InvokeInstructionSubject invoke) -> {
              Assert.assertFalse(invoke.invokedMethod().name.toString().equals("addSuppressed"));
              Assert.assertFalse(invoke.invokedMethod().name.toString().equals("getSuppressed"));
            })
        .run();
  }

  abstract TestRunner test(String testName, String packageName, String mainClass);
}
