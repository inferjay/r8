// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.dex;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.R8Command;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ExtraFileTest {

  private static final String EXAMPLE_DIR = ToolHelper.EXAMPLES_BUILD_DIR;
  private static final String EXAMPLE_DEX = "memberrebinding/classes.dex";
  private static final String EXAMPLE_LIB = "memberrebindinglib/classes.dex";
  private static final String EXAMPLE_CLASS = "memberrebinding.Memberrebinding";
  private static final String EXAMPLE_PACKAGE_MAP = "memberrebinding/package.map";
  private static final String EXAMPLE_PROGUARD_MAP = "memberrebinding/proguard.map";

  @Rule
  public TemporaryFolder temp = ToolHelper.getTemporaryFolderForTest();

  @Test
  public void splitMemberRebindingTwoFiles()
      throws IOException, ProguardRuleParserException, ExecutionException, CompilationException {
    if (!ToolHelper.artSupported()) {
      return;
    }

    Path out = temp.getRoot().toPath();
    Path original = Paths.get(EXAMPLE_DIR, EXAMPLE_DEX);
    Path packageMap = Paths.get(ToolHelper.EXAMPLES_DIR, EXAMPLE_PACKAGE_MAP);
    Path proguardMap = Paths.get(ToolHelper.EXAMPLES_DIR, EXAMPLE_PROGUARD_MAP);
    R8Command command =
        R8Command.builder()
            .addProgramFiles(original)
            .setOutputPath(out)
            .setMinApiLevel(Constants.ANDROID_L_API) // Allow native multidex.
            .setProguardMapFile(proguardMap)
            .setPackageDistributionFile(packageMap)
            .build();
    ToolHelper.runR8(command);
    List<String> outs =
        new ArrayList<>(
            ImmutableList.of(
                out.resolve("classes.dex").toString(),
                out.resolve("classes2.dex").toString(),
                EXAMPLE_DIR + EXAMPLE_LIB));
    outs.forEach(f -> Assert.assertTrue("Failed to find file " + f, Files.exists(Paths.get(f))));
    ToolHelper.checkArtOutputIdentical(
        ImmutableList.of(original.toString(), EXAMPLE_DIR + EXAMPLE_LIB),
        outs,
        EXAMPLE_CLASS,
        null,
        null);
  }
}
