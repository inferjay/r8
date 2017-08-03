// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.desugar;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.D8Command;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.utils.OffOrAuto;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class BasicTestDependenciesDesugaringTest {

  private static final String CLASSPATH_SEPARATOR = File.pathSeparator;

  private static final String[] allLibs;
  static {
    try {
      allLibs =
          Files.readAllLines(Paths.get(ToolHelper.BUILD_DIR, "generated", "supportlibraries.txt"))
          .toArray(new String[0]);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  private static Set<String> knownIssues = Sets.newHashSet(new String[]{
      "espresso-core-3.0.0.jar",
      "hamcrest-integration-1.3.jar",
      "hamcrest-library-1.3.jar",
      "junit-4.12.jar",
      "support-core-ui-25.4.0.jar",
      "support-media-compat-25.4.0.jar",
      "support-fragment-25.4.0.jar",
      "support-compat-25.4.0.jar"
  });

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Parameters(name = "{0}")
  public static Collection<String[]> data() {
    int libCount = allLibs.length;
    Collection<String[]> datas = new ArrayList<String[]>(libCount);
    for (int i = 0; i < libCount; i++) {
      StringBuilder classpath = new StringBuilder();
      for (int j = 0; j < libCount; j++) {
        if (j != i) {
          classpath.append(allLibs[j]).append(CLASSPATH_SEPARATOR);
        }
      }
      datas.add(new String[] {new File(allLibs[i]).getName(), allLibs[i], classpath.toString()});
    }
    return datas;
  }

  private String name;
  private Path toCompile;
  private List<Path> classpath;

  public  BasicTestDependenciesDesugaringTest(String name, String toCompile, String classpath) {
    this.name = name;
    this.toCompile = Paths.get(toCompile);
    this.classpath = Arrays.asList(classpath.split(CLASSPATH_SEPARATOR)).stream()
        .map(string -> Paths.get(string)).collect(Collectors.toList());
  }

  @Test
  public void testCompile() throws IOException, CompilationException {
    if (knownIssues.contains(name)) {
      thrown.expect(CompilationError.class);
    }
    ToolHelper.runD8(
        D8Command.builder().addClasspathFiles(classpath)
        .addProgramFiles(toCompile)
        .addLibraryFiles(Paths.get(ToolHelper.getAndroidJar(Constants.ANDROID_K_API)))
        .setMinApiLevel(Constants.ANDROID_K_API)
        .build(),
        options -> options.interfaceMethodDesugaring = OffOrAuto.Auto);
  }
}
