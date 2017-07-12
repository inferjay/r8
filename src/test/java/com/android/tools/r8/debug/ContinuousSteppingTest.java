// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.debug;

import java.util.Map;
import org.apache.harmony.jpda.tests.framework.jdwp.Value;
import org.junit.Assert;
import org.junit.Test;

public class ContinuousSteppingTest extends DebugTestBase {

  @Test
  public void testArithmetic() throws Throwable {
    runContinuousTest("Arithmetic");
  }

  @Test
  public void testLocals() throws Throwable {
    runContinuousTest("Locals");
  }

  private void runContinuousTest(String debuggeeClassName) throws Throwable {
    runDebugTest(debuggeeClassName,
        breakpoint(debuggeeClassName, "main"),
        run(),
        stepUntil(StepKind.OVER, StepLevel.INSTRUCTION, debuggeeState -> {
          // Fetch local variables.
          Map<String, Value> localValues = debuggeeState.getLocalValues();
          Assert.assertNotNull(localValues);

          // Always step until we actually exit the program.
          return false;
        }));
  }

}
