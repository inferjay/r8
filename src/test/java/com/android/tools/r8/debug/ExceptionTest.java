// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.debug;

import org.junit.Test;

/**
 * Tests debugging behavior with regards to exception handling
 */
public class ExceptionTest extends DebugTestBase {

  @Test
  public void testStepOnCatch() throws Throwable {
    runDebugTest("Exceptions",
        breakpoint("Exceptions", "catchException"),
        run(),
        checkLine(9), // line of the method call throwing the exception
        stepOver(),
        checkLine(10), // line of the catch declaration
        run());
  }

}
