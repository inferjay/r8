// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.maindexlist;

import static org.junit.Assert.assertEquals;

import com.android.tools.r8.R8Command;
import com.android.tools.r8.TestBase;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.utils.FileUtils;
import com.google.common.collect.ImmutableList;
import java.nio.file.Path;
import org.junit.Test;

public class MainDexListOutputTest extends TestBase {
  @Test
  public void testNoMainDex() throws Exception {
    Path mainDexList = temp.newFile().toPath();
    compileWithR8(ImmutableList.of(HelloWorldMain.class),
        options -> {
          options.printMainDexList = true;
          options.printMainDexListFile = mainDexList;
        });
    // Empty main dex list.
    assertEquals(0, FileUtils.readTextFile(mainDexList).size());
  }

  @Test
  public void testWithMainDex() throws Exception {
    Path mainDexRules = writeTextToTempFile(keepMainProguardConfiguration(HelloWorldMain.class));
    R8Command command =
        ToolHelper.prepareR8CommandBuilder(readClasses(HelloWorldMain.class))
            .addMainDexRules(mainDexRules)
            .build();
    Path mainDexList = temp.newFile().toPath();
    ToolHelper.runR8(command,
        options -> {
          options.printMainDexList = true;
          options.printMainDexListFile = mainDexList;
        });
    // Main dex list with the single class.
    assertEquals(
        ImmutableList.of(HelloWorldMain.class.getTypeName().replace('.', '/') + ".class"),
        FileUtils.readTextFile(mainDexList));
  }
}
