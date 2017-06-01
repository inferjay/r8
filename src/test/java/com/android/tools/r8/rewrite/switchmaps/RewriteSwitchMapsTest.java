// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.rewrite.switchmaps;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.TestBase;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.DexInspector;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.Test;

public class RewriteSwitchMapsTest extends TestBase {

  private static final String JAR_FILE = "switchmaps.jar";
  private static final String SWITCHMAP_CLASS_NAME = "switchmaps.Switches$1";
  private static final String PG_CONFIG =
      "-keep class switchmaps.Switches { public static void main(...); } " +
          "-dontobfuscate";

  @Test
  public void checkSwitchMapsRemoved()
      throws IOException, ProguardRuleParserException, ExecutionException, CompilationException {
    AndroidApp.Builder builder = AndroidApp.builder();
    builder.addLibraryFiles(Paths.get(ToolHelper.getDefaultAndroidJar()));
    builder.addProgramFiles(Paths.get(ToolHelper.EXAMPLES_BUILD_DIR).resolve(JAR_FILE));
    AndroidApp result = compileWithR8(builder.build(), writeTextToTempFile(PG_CONFIG));
    DexInspector inspector = new DexInspector(result);
    Assert.assertFalse(inspector.clazz(SWITCHMAP_CLASS_NAME).isPresent());
  }
}
