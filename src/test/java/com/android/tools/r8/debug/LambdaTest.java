// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.debug;

import org.junit.Test;

public class LambdaTest extends DebugTestBase {

  @Test
  public void testLambdaDebugging() throws Throwable {
    String debuggeeClass = "DebugLambda";
    String initialMethodName = "printInt";
    // TODO(shertz) test local variables
    runDebugTestJava8(debuggeeClass,
        breakpoint(debuggeeClass, initialMethodName),
        run(),
        checkMethod(debuggeeClass, initialMethodName),
        checkLine(12),
        stepInto(INTELLIJ_FILTER),
        checkLine(16),
        run());
  }
}
