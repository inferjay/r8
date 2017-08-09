// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.maindexlist;

import static org.junit.Assert.assertEquals;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.R8Command;
import com.android.tools.r8.TestBase;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.utils.FileUtils;
import com.google.common.collect.ImmutableList;
import java.nio.file.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MainDexListOutputTest extends TestBase {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testNoMainDex() throws Exception {
    thrown.expect(CompilationException.class);
    Path mainDexListOutput = temp.getRoot().toPath().resolve("main-dex-output.txt");
    R8Command command =
        ToolHelper.prepareR8CommandBuilder(readClasses(HelloWorldMain.class))
            .setMainDexListOutputPath(mainDexListOutput)
            .build();
    ToolHelper.runR8(command);
  }

  @Test
  public void testWithMainDex() throws Exception {
    Path mainDexRules = writeTextToTempFile(keepMainProguardConfiguration(HelloWorldMain.class));
    Path mainDexListOutput = temp.getRoot().toPath().resolve("main-dex-output.txt");
    R8Command command =
        ToolHelper.prepareR8CommandBuilder(readClasses(HelloWorldMain.class))
            .addMainDexRules(mainDexRules)
            .setMainDexListOutputPath(mainDexListOutput)
            .build();
    ToolHelper.runR8(command);
    // Main dex list with the single class.
    assertEquals(
        ImmutableList.of(HelloWorldMain.class.getTypeName().replace('.', '/') + ".class"),
        FileUtils.readTextFile(mainDexListOutput));
  }
}
