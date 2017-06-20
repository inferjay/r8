// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.maindexlist;

import static com.android.tools.r8.utils.FileUtils.JAR_EXTENSION;
import static com.android.tools.r8.utils.FileUtils.ZIP_EXTENSION;

import com.android.tools.r8.CompilationResult;
import com.android.tools.r8.R8Command;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.utils.InternalOptions;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class MainDexTracingTest {

  private static final String EXAMPLE_BUILD_DIR = ToolHelper.EXAMPLES_BUILD_DIR;
  private static final String EXAMPLE_O_BUILD_DIR = ToolHelper.EXAMPLES_ANDROID_O_BUILD_DIR;
  private static final String EXAMPLE_SRC_DIR = ToolHelper.EXAMPLES_DIR;
  private static final String EXAMPLE_O_SRC_DIR = ToolHelper.EXAMPLES_ANDROID_O_DIR;

  @Rule public TemporaryFolder temp = ToolHelper.getTemporaryFolderForTest();

  @Test
  public void traceMainDexList001_1() throws Throwable {
    doTest(
        "traceMainDexList001_1",
        "multidex001",
        EXAMPLE_BUILD_DIR,
        Paths.get(EXAMPLE_SRC_DIR, "multidex", "main-dex-rules.txt"),
        Paths.get(EXAMPLE_SRC_DIR, "multidex001", "ref-list-1.txt"),
        14);
  }

  @Test
  public void traceMainDexList001_2() throws Throwable {
    doTest(
        "traceMainDexList001_2",
        "multidex001",
        EXAMPLE_BUILD_DIR,
        Paths.get(EXAMPLE_SRC_DIR, "multidex001", "main-dex-rules-2.txt"),
        Paths.get(EXAMPLE_SRC_DIR, "multidex001", "ref-list-2.txt"),
        14);
  }

  @Test
  public void traceMainDexList002() throws Throwable {
    doTest(
        "traceMainDexList002",
        "multidex002",
        EXAMPLE_BUILD_DIR,
        Paths.get(EXAMPLE_SRC_DIR, "multidex", "main-dex-rules.txt"),
        Paths.get(EXAMPLE_SRC_DIR, "multidex002", "ref-list-1.txt"),
        14);
  }

  @Test
  public void traceMainDexList003() throws Throwable {
    doTest(
        "traceMainDexList003",
        "multidex003",
        EXAMPLE_BUILD_DIR,
        Paths.get(EXAMPLE_SRC_DIR, "multidex", "main-dex-rules.txt"),
        Paths.get(EXAMPLE_SRC_DIR, "multidex003", "ref-list-1.txt"),
        14);
  }

  @Test
  public void traceMainDexList004() throws Throwable {
    doTest(
        "traceMainDexList004",
        "multidex004",
        EXAMPLE_O_BUILD_DIR,
        Paths.get(EXAMPLE_SRC_DIR, "multidex", "main-dex-rules.txt"),
        Paths.get(EXAMPLE_O_SRC_DIR, "multidex004", "ref-list-1.txt"),
        14);
  }

  private void doTest(
      String testName,
      String packageName,
      String buildDir,
      Path mainDexRules,
      Path expectedMainDexList,
      int minSdk)
      throws Throwable {
    doTest(
        testName,
        packageName,
        buildDir,
        mainDexRules,
        expectedMainDexList,
        minSdk,
        R8Command.builder(),
        (options) -> {
          options.inlineAccessors = false;
        });
  }

  private void doTest(
      String testName,
      String packageName,
      String buildDir,
      Path mainDexRules,
      Path expectedMainDexList,
      int minSdk,
      R8Command.Builder builder,
      Consumer<InternalOptions> optionsConsumer)
      throws Throwable {
    Path out = temp.getRoot().toPath().resolve(testName + ZIP_EXTENSION);

    Path inputJar = Paths.get(buildDir, packageName + JAR_EXTENSION);
    builder.setMinApiLevel(minSdk);
    try {
      R8Command command = builder
          .addProgramFiles(inputJar)
          .addLibraryFiles(Paths.get(EXAMPLE_BUILD_DIR, "multidexfakeframeworks" + JAR_EXTENSION),
              Paths.get(ToolHelper.getAndroidJar(minSdk)))
          .setOutputPath(out)
          .addMainDexRules(mainDexRules)
          .build();
      CompilationResult result = ToolHelper.runR8WithFullResult(command, optionsConsumer);
      List<String> resultMainDexList =
          result.dexApplication.mainDexList.stream()
          .filter(dexType -> isApplicationClass(dexType, result) != null)
          .map(dexType -> dexType.descriptor.toString())
          .collect(Collectors.toList());
      Collections.sort(resultMainDexList);
      String[] refList = new String(Files.readAllBytes(
          expectedMainDexList), StandardCharsets.UTF_8).split("\n");
      for (int i = 0; i < refList.length; i++) {
        String reference = refList[i].trim();
        String computed = resultMainDexList.get(i);
        if (reference.contains("-$$Lambda$")) {
          // For lambda classes we check that there is a lambda class for the right containing
          // class. However, we do not check the hash for the generated lambda class. The hash
          // changes for different compiler versions because different compiler versions generate
          // different lambda implementation method names.
          reference = reference.substring(0, reference.lastIndexOf('$'));
          computed = computed.substring(0, computed.lastIndexOf('$'));
        }
        Assert.assertEquals(reference, computed);
      }
    } catch (ExecutionException e) {
      throw e.getCause();
    }
  }

  private Object isApplicationClass(DexType dexType, CompilationResult result) {
    DexClass clazz = result.appInfo.definitionFor(dexType);
    return clazz != null && clazz.isProgramClass();
  }
}
