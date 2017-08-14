// Copyright (c) 2017, the Rex project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.d8;

import static org.junit.Assert.assertEquals;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.D8Command;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.dex.ApplicationReader;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.dex.Marker;
import com.android.tools.r8.dex.Marker.Tool;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.Timing;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

/**
 * Simple test that compiles framework.jar with D8 a number of times with
 * various number of threads available to the compiler.
 * This test also tests the hidden marker inserted into classes.dex.
 */
@RunWith( Parameterized.class )
public class D8FrameworkTest {

  private static final Path FRAMEWORK_JAR =
      Paths.get("tools/linux/art-5.1.1/product/mako/system/framework/framework.jar");

  @Rule
  public TemporaryFolder output = ToolHelper.getTemporaryFolderForTest();

  @Parameters(name = "Number of threads = {0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] { {1}, {2}, {4}, {8}, {16} });
  }

  private final int threads;

  public D8FrameworkTest(int threads) {
    this.threads = threads;
  }

  @Test
  public void compile() throws CompilationException, IOException, ExecutionException {
    D8Command command = D8Command.builder()
        .setMinApiLevel(Constants.ANDROID_N_API)
        .addProgramFiles(FRAMEWORK_JAR)
        .build();
    Marker marker = new Marker(Tool.D8)
        .put("revision", "1.0.0")
        .put("threads", threads);
    Marker selfie = Marker.parse(marker.toString());
    assert marker.equals(selfie);
    AndroidApp app = ToolHelper.runD8(command, options -> {
      options.setMarker(marker);
      options.numberOfThreads = threads;
    });
    DexApplication dexApp =
        new ApplicationReader(app, new InternalOptions(), new Timing("D8FrameworkTest")).read();
    Marker readMarker = dexApp.dexItemFactory.extractMarker();
    assertEquals(marker, readMarker);
  }
}
