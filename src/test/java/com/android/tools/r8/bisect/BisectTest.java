// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.bisect;

import static org.junit.Assert.assertEquals;

import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.bisect.BisectOptions.Result;
import com.android.tools.r8.dex.ApplicationReader;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.smali.SmaliTestBase.MethodSignature;
import com.android.tools.r8.smali.SmaliTestBase.SmaliBuilder;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.DexInspector;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.Timing;
import com.google.common.collect.ImmutableList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class BisectTest {

  private final String[] CLASSES = {"A", "B", "C", "D", "E", "F", "G", "H"};
  private final String ERRONEOUS_CLASS = "F";
  private final String ERRONEOUS_METHOD = "foo";
  private final String VALID_METHOD = "bar";

  // Set during build to more easily inspect later.
  private MethodSignature erroneousMethodSignature = null;

  @Rule
  public TemporaryFolder temp = ToolHelper.getTemporaryFolderForTest();

  @Test
  public void bisect() throws Exception {
    InternalOptions options = new InternalOptions();
    Timing timing = new Timing("bisect-test");

    // Build "good" application with no method in "F".
    SmaliBuilder builderGood = new SmaliBuilder();
    for (String clazz : CLASSES) {
      builderGood.addClass(clazz);
      builderGood.addStaticMethod(
          "void", VALID_METHOD, ImmutableList.of(), 0, "return-void");
    }
    AndroidApp inputGood = AndroidApp.fromDexProgramData(builderGood.compile());
    DexApplication appGood = new ApplicationReader(inputGood, options, timing).read();

    // Build "bad" application with a method "foo" in "F".
    SmaliBuilder builderBad = new SmaliBuilder();
    for (String clazz : CLASSES) {
      builderBad.addClass(clazz);
      if (clazz.equals(ERRONEOUS_CLASS)) {
        erroneousMethodSignature = builderBad.addStaticMethod(
            "void", ERRONEOUS_METHOD, ImmutableList.of(), 0, "return-void");
      } else {
        builderBad.addStaticMethod(
            "void", VALID_METHOD, ImmutableList.of(), 0, "return-void");
      }
    }
    AndroidApp inputBad = AndroidApp.fromDexProgramData(builderBad.compile());
    DexApplication appBad = new ApplicationReader(inputBad, options, timing).read();

    ExecutorService executor = Executors.newWorkStealingPool();
    try {
      BisectState state = new BisectState(appGood, appBad, null);
      DexProgramClass clazz = Bisect.run(state, this::command, temp.newFolder().toPath(), executor);
      System.out.println("Found bad class: " + clazz);
      assertEquals(clazz.type.toString(), ERRONEOUS_CLASS);
    } finally {
      executor.shutdown();
    }
  }

  private Result command(DexApplication application) {
    DexInspector inspector = new DexInspector(application);
    if (inspector
        .clazz(ERRONEOUS_CLASS)
        .method(erroneousMethodSignature.returnType,
            erroneousMethodSignature.name,
            erroneousMethodSignature.parameterTypes)
        .isPresent()) {
      return Result.BAD;
    }
    return Result.GOOD;
  }
}
