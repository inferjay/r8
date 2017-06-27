// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.rewrite.assertions;

import static org.junit.Assert.assertTrue;

import com.android.tools.r8.TestBase;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.naming.MemberNaming.MethodSignature;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.DexInspector;
import com.android.tools.r8.utils.DexInspector.ClassSubject;
import com.android.tools.r8.utils.DexInspector.MethodSubject;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class RemoveAssertionsTest extends TestBase {

  @Test
  public void test() throws Exception {
    // Run with R8, but avoid inlining to really validate that the methods "condition"
    // and "<clinit>" are gone.
    Class testClass = ClassWithAssertions.class;
    AndroidApp app = compileWithR8(
        ImmutableList.of(testClass),
        keepMainProguardConfiguration(testClass, true, false),
        options -> options.inlineAccessors = false);
    DexInspector x = new DexInspector(app);

    ClassSubject clazz = x.clazz(ClassWithAssertions.class);
    assertTrue(clazz.isPresent());
    MethodSubject conditionMethod =
        clazz.method(new MethodSignature("condition", "boolean", new String[]{}));
    assertTrue(!conditionMethod.isPresent());
    MethodSubject clinit =
        clazz.method(new MethodSignature(Constants.CLASS_INITIALIZER_NAME, "void", new String[]{}));
    assertTrue(!clinit.isPresent());
  }
}
