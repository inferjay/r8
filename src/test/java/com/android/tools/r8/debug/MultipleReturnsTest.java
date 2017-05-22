// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.debug;

import org.junit.Test;

/**
 * Tests debugging of method with multiple return statements.
 */
public class MultipleReturnsTest extends DebugTestBase {

  @Test
  public void testMultipleReturns() throws Throwable {
    runDebugTest("MultipleReturns",
        breakpoint("MultipleReturns", "multipleReturns"),
        run(),
        stepOver(),
        checkLine(16), // this should be the 1st return statement
        run(),
        stepOver(),
        checkLine(18), // this should be the 2nd return statement
        run());
  }
}
