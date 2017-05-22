// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.naming;

import com.android.tools.r8.ToolHelper;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;

public class ProguardMapReaderTest {

  public static final String ROOT = ToolHelper.EXAMPLES_BUILD_DIR;
  public static final String EXAMPLE_MAP = "throwing/throwing.map";

  public static final String EXAMPLE_MAP_WITH_PACKAGE_INFO =
      "dagger.android.package-info -> dagger.android.package-info\n";

  @Test
  public void parseThrowingMap() throws IOException {
    ProguardMapReader.mapperFromFile(Paths.get(ROOT, EXAMPLE_MAP));
  }

  @Test
  public void roundTripTest() throws IOException {
    ClassNameMapper firstMapper = ProguardMapReader.mapperFromFile(Paths.get(ROOT, EXAMPLE_MAP));
    ClassNameMapper secondMapper = ProguardMapReader.mapperFromString(firstMapper.toString());
    Assert.assertEquals(firstMapper, secondMapper);
  }

  @Test
  public void parseMapWithPackageInfo() throws IOException {
    ClassNameMapper mapper = ProguardMapReader.mapperFromString(EXAMPLE_MAP_WITH_PACKAGE_INFO);
    Assert.assertTrue(mapper.getObfuscatedToOriginalMapping().isEmpty());
  }
}
