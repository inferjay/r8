// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import static org.junit.Assert.assertEquals;

import com.android.tools.r8.Resource;
import java.util.Collections;
import org.junit.Test;

public class OutputModeTest {
  @Test
  public void testIndexedFileName() {
    assertEquals("classes.dex", OutputMode.Indexed.getOutputPath(null, 0));
    assertEquals("classes2.dex", OutputMode.Indexed.getOutputPath(null, 1));
  }

  @Test
  public void testFilePerClass() {
    Resource test =
        Resource.fromBytes(Resource.Kind.CLASSFILE, new byte[]{}, Collections.singleton("LTest;"));
    assertEquals("Test.dex", OutputMode.FilePerClass.getOutputPath(test, 0));
    Resource comTest =
        Resource.fromBytes(
            Resource.Kind.CLASSFILE, new byte[]{}, Collections.singleton("Lcom/Test;"));
    assertEquals("com/Test.dex", OutputMode.FilePerClass.getOutputPath(comTest, 0));
    Resource comExampleTest =
        Resource.fromBytes(
            Resource.Kind.CLASSFILE, new byte[]{}, Collections.singleton("Lcom/example/Test;"));
    assertEquals("com/example/Test.dex", OutputMode.FilePerClass.getOutputPath(comExampleTest, 0));
    assertEquals("com/example/Test.dex", OutputMode.FilePerClass.getOutputPath(comExampleTest, 1));
  }
}
