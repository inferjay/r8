// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.checkdiscarded;

import com.android.tools.r8.TestBase;
import com.android.tools.r8.checkdiscarded.testclasses.Main;
import com.android.tools.r8.checkdiscarded.testclasses.UnusedClass;
import com.android.tools.r8.checkdiscarded.testclasses.UsedClass;
import com.android.tools.r8.checkdiscarded.testclasses.WillBeGone;
import com.android.tools.r8.checkdiscarded.testclasses.WillStay;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.utils.InternalOptions;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class CheckDiscardedTest extends TestBase {

  private void run(boolean obfuscate, Class annotation, boolean checkMembers, boolean shouldFail)
      throws Exception {
    List<Class> classes = ImmutableList.of(
        UnusedClass.class,
        UsedClass.class,
        Main.class);
    String proguardConfig = keepMainProguardConfiguration(Main.class, true, obfuscate)
        + checkDiscardRule(checkMembers, annotation);
    try {
      compileWithR8(classes, proguardConfig, this::noInlining);
    } catch (CompilationError e) {
      Assert.assertTrue(shouldFail);
      return;
    }
    Assert.assertFalse(shouldFail);
  }

  private void noInlining(InternalOptions options) {
    options.inlineAccessors = false;
  }

  private String checkDiscardRule(boolean member, Class annotation) {
    if (member) {
      return "-checkdiscard class * { @" + annotation.getCanonicalName() + " *; }";
    } else {
      return "-checkdiscard @" + annotation.getCanonicalName() + " class *";
    }
  }

  @Test
  public void classesAreGone() throws Exception {
    run(false, WillBeGone.class, false, false);
    run(true, WillBeGone.class, false, false);
  }

  @Test
  public void classesAreNotGone() throws Exception {
    run(false, WillStay.class, false, true);
    run(true, WillStay.class, false, true);
  }

  @Test
  public void membersAreGone() throws Exception {
    run(false, WillBeGone.class, true, false);
    run(true, WillBeGone.class, true, false);
  }

  @Test
  public void membersAreNotGone() throws Exception {
    run(false, WillStay.class, true, true);
    run(true, WillStay.class, true, true);
  }

}
