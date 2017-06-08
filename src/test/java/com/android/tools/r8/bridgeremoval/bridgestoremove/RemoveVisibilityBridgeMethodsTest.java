// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.bridgeremoval.bridgestoremove;

import static org.junit.Assert.assertFalse;

import com.android.tools.r8.TestBase;
import com.android.tools.r8.utils.DexInspector;
import com.google.common.collect.ImmutableList;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.Test;

public class RemoveVisibilityBridgeMethodsTest extends TestBase {

  private void run(boolean obfuscate) throws Exception {
    List<Class> classes = ImmutableList.of(
        Outer.class,
        Main.class);
    String proguardConfig = keepMainProguardConfiguration(Main.class, true, obfuscate);
    DexInspector inspector = new DexInspector(compileWithR8(classes, proguardConfig));

    List<Method> removedMethods = ImmutableList.of(
        Outer.SubClass.class.getMethod("method"),
        Outer.StaticSubClass.class.getMethod("method"));

    removedMethods.forEach(method -> assertFalse(inspector.method(method).isPresent()));
  }

  @Test
  public void testWithObfuscation() throws Exception {
    run(true);
  }

  @Test
  public void testWithoutObfuscation() throws Exception {
    run(false);
  }
}
