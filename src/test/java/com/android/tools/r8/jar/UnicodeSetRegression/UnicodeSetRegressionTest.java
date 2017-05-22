// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.jar.UnicodeSetRegression;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.ArtErrorParser;
import com.android.tools.r8.utils.ArtErrorParser.ArtErrorInfo;
import com.android.tools.r8.utils.ArtErrorParser.ArtErrorParserException;
import com.android.tools.r8.utils.DexInspector;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.ComparisonFailure;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class UnicodeSetRegressionTest {

  private static final String JAR_FILE =
      "src/test/java/com/android/tools/r8/jar/UnicodeSetRegression/UnicodeSet.jar";

  @Rule
  public TemporaryFolder temp = ToolHelper.getTemporaryFolderForTest();

  private AndroidApp dexFromDX() throws IOException {
    return ToolHelper.runDexer(JAR_FILE, temp.newFolder("dx-dex").getPath());
  }

  @Test
  public void testUnicodeSetFromDex()
      throws ExecutionException, IOException, ProguardRuleParserException, CompilationException {
    Path combinedInput = temp.getRoot().toPath().resolve("all.zip");
    Path oatFile = temp.getRoot().toPath().resolve("all.oat");
    ToolHelper.runR8(dexFromDX(), combinedInput);
    ToolHelper.runDex2Oat(combinedInput, oatFile);
  }

  @Test
  public void testUnicodeSetFromJar()
      throws ExecutionException, IOException, ProguardRuleParserException, CompilationException {
    Path combinedInput = temp.getRoot().toPath().resolve("all.zip");
    Path oatFile = temp.getRoot().toPath().resolve("all.oat");
    AndroidApp result = ToolHelper.runR8(JAR_FILE, combinedInput.toString());
    try {
      ToolHelper.runDex2Oat(combinedInput, oatFile);
    } catch (AssertionError e) {
      AndroidApp fromDexApp = ToolHelper.runR8(dexFromDX());
      DexInspector fromDex = new DexInspector(fromDexApp);
      DexInspector fromJar = new DexInspector(result);
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
            "REFERENCE\n" + error.dump(fromDex, false) + "\nEND REFERENCE",
            "PROCESSED\n" + error.dump(fromJar, true) + "\nEND PROCESSED").toString());
      }
      ArtErrorInfo error = errors.get(errors.size() - 1);
      throw new ComparisonFailure(error.getMessage(),
          "REFERENCE\n" + error.dump(fromDex, false) + "\nEND REFERENCE",
          "PROCESSED\n" + error.dump(fromJar, true) + "\nEND PROCESSED");
    }
  }
}
