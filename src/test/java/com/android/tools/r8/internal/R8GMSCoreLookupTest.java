// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.internal;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import com.android.tools.r8.dex.ApplicationReader;
import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.Timing;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Before;
import org.junit.Test;

public class R8GMSCoreLookupTest {

  static final String APP_DIR = "third_party/gmscore/v5/";
  private AndroidApp app;
  private DexApplication program;
  private AppInfoWithSubtyping appInfo;

  @Before
  public void readGMSCore() throws IOException, ExecutionException {
    app = AndroidApp.fromProgramDirectory(Paths.get(APP_DIR));
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    Timing timing = new Timing("ReadGMSCore");
    program = new ApplicationReader(app, new InternalOptions(), timing).read(executorService);
    appInfo = new AppInfoWithSubtyping(program);
  }

  private void testVirtualLookup(DexProgramClass clazz, DexEncodedMethod method) {
    // Check lookup will produce the same result.
    DexMethod id = method.method;
    assertEquals(appInfo.lookupVirtualTarget(id.holder, method.method), method);

    // Check lookup targets with include method.
    Set<DexEncodedMethod> targets = appInfo.lookupVirtualTargets(method.method);
    assertTrue(targets.contains(method));
  }

  private void testInterfaceLookup(DexProgramClass clazz, DexEncodedMethod method) {
    Set<DexEncodedMethod> targets = appInfo.lookupVirtualTargets(method.method);
    assertFalse(targets.isEmpty());
  }

  private void testLookup(DexProgramClass clazz) {
    assert appInfo.subtypes(clazz.type)
        != null : "Application class must have non null subtypes.";
    if (clazz.isInterface()) {
      for (DexEncodedMethod method : clazz.virtualMethods()) {
        testInterfaceLookup(clazz, method);
      }
    } else {
      for (DexEncodedMethod method : clazz.virtualMethods()) {
        testVirtualLookup(clazz, method);
      }
    }
  }

  @Test
  public void testLookup() {
    program.classes().forEach(this::testLookup);
  }
}
