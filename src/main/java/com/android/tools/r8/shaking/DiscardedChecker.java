// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexItem;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.shaking.RootSetBuilder.RootSet;
import java.util.Arrays;
import java.util.Set;

public class DiscardedChecker {

  private final Set<DexItem> checkDiscarded;
  private final DexApplication application;
  private boolean fail = false;

  public DiscardedChecker(RootSet rootSet, DexApplication application) {
    this.checkDiscarded = rootSet.checkDiscarded;
    this.application = application;
  }

  public void run() {
    for (DexProgramClass clazz : application.classes()) {
      if (checkDiscarded.contains(clazz)) {
        report(clazz);
      }
      processSubItems(clazz.directMethods());
      processSubItems(clazz.virtualMethods());
      processSubItems(clazz.staticFields());
      processSubItems(clazz.instanceFields());
    }
    if (fail) {
      throw new CompilationError("Discard checks failed.");
    }
  }

  private void processSubItems(DexItem[] items) {
    Arrays.stream(items).filter(checkDiscarded::contains).forEach(this::report);
  }

  private void report(DexItem item) {
    System.err.println("Item " + item.toSourceString() + " was not discarded.");
    fail = true;
  }
}
