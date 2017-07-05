// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.internal;

import static org.junit.Assert.assertTrue;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.CompilationMode;
import com.android.tools.r8.D8Command;
import com.android.tools.r8.R8Command;
import com.android.tools.r8.R8RunArtTestsTest.CompilerUnderTest;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.ArtErrorParser;
import com.android.tools.r8.utils.ArtErrorParser.ArtErrorInfo;
import com.android.tools.r8.utils.ArtErrorParser.ArtErrorParserException;
import com.android.tools.r8.utils.DexInspector;
import com.android.tools.r8.utils.ListUtils;
import com.android.tools.r8.utils.OutputMode;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.ComparisonFailure;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public abstract class CompilationTestBase {

  @Rule
  public TemporaryFolder temp = ToolHelper.getTemporaryFolderForTest();

  public AndroidApp runAndCheckVerification(
      CompilerUnderTest compiler,
      CompilationMode mode,
      String referenceApk,
      String pgMap,
      String pgConf,
      String... inputs)
      throws ExecutionException, IOException, ProguardRuleParserException, CompilationException {
    return runAndCheckVerification(
        compiler, mode, referenceApk, pgMap, pgConf, Arrays.asList(inputs));
  }

  public AndroidApp runAndCheckVerification(D8Command command, String referenceApk)
      throws IOException, ExecutionException {
    return checkVerification(ToolHelper.runD8(command), referenceApk);
  }

  public AndroidApp runAndCheckVerification(
      CompilerUnderTest compiler,
      CompilationMode mode,
      String referenceApk,
      String pgMap,
      String pgConf,
      List<String> inputs)
      throws ExecutionException, IOException, ProguardRuleParserException, CompilationException {
    assertTrue(referenceApk == null || new File(referenceApk).exists());
    AndroidApp outputApp;
    if (compiler == CompilerUnderTest.R8) {
      R8Command.Builder builder = R8Command.builder();
      builder.addProgramFiles(ListUtils.map(inputs, Paths::get));
      if (pgMap != null) {
        builder.setProguardMapFile(Paths.get(pgMap));
      }
      if (pgConf != null) {
        builder.addProguardConfigurationFiles(Paths.get(pgConf));
      }
      builder.setMode(mode);
      outputApp = ToolHelper.runR8(builder.build(),
                  options -> {
                    options.printSeeds = false;
                    options.minApiLevel = Constants.ANDROID_L_API;
                  });
    } else {
      assert compiler == CompilerUnderTest.D8;
      outputApp =
          ToolHelper.runD8(
              D8Command.builder()
                  .addProgramFiles(ListUtils.map(inputs, Paths::get))
                  .setMode(mode)
                  .setMinApiLevel(Constants.ANDROID_L_API)
                  .build());
    }
    return checkVerification(outputApp, referenceApk);
  }

  public AndroidApp checkVerification(AndroidApp outputApp, String referenceApk)
      throws IOException, ExecutionException {
    Path out = temp.getRoot().toPath().resolve("all.zip");
    Path oatFile = temp.getRoot().toPath().resolve("all.oat");
    outputApp.writeToZip(out, OutputMode.Indexed);
    try {
      ToolHelper.runDex2Oat(out, oatFile);
      return outputApp;
    } catch (AssertionError e) {
      if (referenceApk == null) {
        throw e;
      }
      DexInspector theirs = new DexInspector(Paths.get(referenceApk));
      DexInspector ours = new DexInspector(out);
      List<ArtErrorInfo> errors;
      try {
        errors = ArtErrorParser.parse(e.getMessage());
      } catch (ArtErrorParserException parserException) {
        System.err.println(parserException.toString());
        throw e;
      }
      if (errors.isEmpty()) {
        throw e;
      }
      for (ArtErrorInfo error : errors.subList(0, errors.size() - 1)) {
        System.err.println(new ComparisonFailure(error.getMessage(),
            "REFERENCE\n" + error.dump(theirs, false) + "\nEND REFERENCE",
            "PROCESSED\n" + error.dump(ours, true) + "\nEND PROCESSED").toString());
      }
      ArtErrorInfo error = errors.get(errors.size() - 1);
      throw new ComparisonFailure(error.getMessage(),
          "REFERENCE\n" + error.dump(theirs, false) + "\nEND REFERENCE",
          "PROCESSED\n" + error.dump(ours, true) + "\nEND PROCESSED");
    }
  }
}
