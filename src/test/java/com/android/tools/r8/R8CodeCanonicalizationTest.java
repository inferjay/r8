// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.dex.DexFileReader;
import com.android.tools.r8.dex.Segment;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


public class R8CodeCanonicalizationTest {

  private static final String SOURCE_DEX = "invokeempty/classes.dex";

  private int readNumberOfCodes(Path file) throws IOException {
    Segment[] segments = DexFileReader.parseMapFrom(file);
    for (Segment segment : segments) {
      if (segment.type == Constants.TYPE_CODE_ITEM) {
        return segment.length;
      }
    }
    return 0;
  }

  @Rule
  public TemporaryFolder temp = ToolHelper.getTemporaryFolderForTest();

  @Test
  public void testNumberOfCodeItemsUnchanged()
      throws IOException, ExecutionException, ProguardRuleParserException, CompilationException {
    int numberOfCodes = readNumberOfCodes(Paths.get(ToolHelper.EXAMPLES_BUILD_DIR + SOURCE_DEX));
    ToolHelper.runR8(ToolHelper.EXAMPLES_BUILD_DIR + SOURCE_DEX, temp.getRoot().getCanonicalPath());
    int newNumberOfCodes = readNumberOfCodes(
        Paths.get(temp.getRoot().getCanonicalPath(), "classes.dex"));
    Assert.assertEquals("Number of codeitems does not change.", numberOfCodes, newNumberOfCodes);
  }

}
