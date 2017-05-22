// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.debug;

import com.android.tools.r8.debug.DebugTestBase.JUnit3Wrapper.Command;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class SyntheticMethodTest extends DebugTestBase {

  private void debugInnerAccessors(StepFilter stepFilter) throws Throwable {
    String debuggeeClass = "InnerAccessors";
    List<Command> commands = new ArrayList<>();
    commands.add(breakpoint("InnerAccessors$Inner", "callPrivateMethodInOuterClass"));
    commands.add(run());
    commands.add(checkLine(13));
    commands.add(stepInto(stepFilter));  // skip synthetic accessor
    if (stepFilter == NO_FILTER) {
      commands.add(stepInto(stepFilter));
    }
    commands.add(checkMethod(debuggeeClass, "privateMethod"));
    commands.add(checkLine(8));
    commands.add(run());
    runDebugTest(debuggeeClass, commands);
  }

  @Test
  public void testInnerAccessors_NoFilter() throws Throwable {
    debugInnerAccessors(NO_FILTER);
  }

  @Test
  public void testInnerAccessors_IntelliJ() throws Throwable {
    debugInnerAccessors(INTELLIJ_FILTER);
  }

}
