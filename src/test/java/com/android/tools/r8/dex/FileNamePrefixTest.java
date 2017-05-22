// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.dex;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class FileNamePrefixTest {

  private final List<String> files;
  private final String expectedPrefix;

  public FileNamePrefixTest(String[] files, String expectedPrefix) {
    this.files = ImmutableList.copyOf(files);
    this.expectedPrefix = expectedPrefix;
  }

  @Parameters(name = "{index}: {1}")
  public static Collection<Object[]> data() {
    return ImmutableList.of(
        new Object[]{new String[]{"classes.dex", "classes2.dex"}, "classes"},
        new Object[]{new String[]{"Classes.dex", "Classes2.dex"}, "Classes"},
        new Object[]{new String[]{"classes.dex", "classes2.dix"}, null},
        new Object[]{new String[]{"classes.dex", "classes2.xdex"}, null},
        new Object[]{new String[]{"classes.dex", "classes1.dex"}, null},
        new Object[]{new String[]{"classes.dex", "fields2.dex"}, null},
        new Object[]{new String[]{"classes.dex", "classes2.dex", "classes4.dex"}, null}
    );
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void computePrefix() {
    if (expectedPrefix == null) {
      thrown.expect(RuntimeException.class);
    }
    String computedPrefix = VirtualFile.deriveCommonPrefixAndSanityCheck(files);
    if (expectedPrefix != null) {
      Assert.assertEquals(expectedPrefix, computedPrefix);
    }
  }
}
