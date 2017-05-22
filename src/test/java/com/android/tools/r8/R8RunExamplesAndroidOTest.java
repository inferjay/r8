// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8;

import com.android.tools.r8.ToolHelper.DexVm;
import com.android.tools.r8.dex.Constants;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.UnaryOperator;
import org.junit.Test;

public class R8RunExamplesAndroidOTest extends RunExamplesAndroidOTest<R8Command.Builder> {

  private static Map<DexVm, List<String>> alsoFailsOn =
      ImmutableMap.of(
          DexVm.ART_4_4_4, ImmutableList.of(
              "invokecustom-with-shrinking"
          ),
          DexVm.ART_5_1_1, ImmutableList.of(
              "invokecustom-with-shrinking"
          ),
          DexVm.ART_6_0_1, ImmutableList.of(
              "invokecustom-with-shrinking"
          ),
          DexVm.ART_7_0_0, ImmutableList.of(
              "invokecustom-with-shrinking"
          ),
          DexVm.ART_DEFAULT, ImmutableList.of(
          )
      );

  @Test
  public void invokeCustomWithShrinking() throws Throwable {
    test("invokecustom-with-shrinking", "invokecustom", "InvokeCustom")
        .withMinApiLevel(Constants.ANDROID_O_API)
        .withBuilderTransformation(builder ->
            builder.addProguardConfigurationFiles(
                Paths.get(ToolHelper.EXAMPLES_ANDROID_O_DIR, "invokecustom/keep-rules.txt")))
        .run();
  }

  class R8TestRunner extends TestRunner {

    R8TestRunner(String testName, String packageName, String mainClass) {
      super(testName, packageName, mainClass);
    }

    @Override
    TestRunner withMinApiLevel(int minApiLevel) {
      return withBuilderTransformation(builder -> builder.setMinApiLevel(minApiLevel));
    }

    @Override
    void build(Path inputFile, Path out) throws Throwable {
      try {
        R8Command.Builder builder = R8Command.builder();
        for (UnaryOperator<R8Command.Builder> transformation : builderTransformations) {
          builder = transformation.apply(builder);
        }
        R8Command command = builder.addProgramFiles(inputFile).setOutputPath(out).build();
        ToolHelper.runR8(command, this::combinedOptionConsumer);
      } catch (ExecutionException e) {
        throw e.getCause();
      }
    }
  }

  @Override
  TestRunner test(String testName, String packageName, String mainClass) {
    return new R8TestRunner(testName, packageName, mainClass);
  }

  @Override
  boolean expectedToFail(String name) {
    return super.expectedToFail(name) || failsOn(alsoFailsOn, name);
  }
}
