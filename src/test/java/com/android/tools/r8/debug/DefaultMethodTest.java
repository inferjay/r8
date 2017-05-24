// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.debug;

import com.android.tools.r8.debug.DebugTestBase.JUnit3Wrapper.Command;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class DefaultMethodTest extends DebugTestBase {

  @Test
  public void testDefaultMethod() throws Throwable {
    String debuggeeClass = "DebugDefaultMethod";
    String parameterName = "msg";
    String localVariableName = "name";

    List<Command> commands = new ArrayList<>();
    commands.add(breakpoint(debuggeeClass, "testDefaultMethod"));
    commands.add(run());
    commands.add(checkMethod(debuggeeClass, "testDefaultMethod"));
    commands.add(checkLine(27));
    if (!supportsDefaultMethod()) {
      // We desugared default method. This means we're going to step through an extra (forward)
      // method first.
      commands.add(stepInto());
    }
    commands.add(stepInto());
    commands.add(checkLocal(parameterName));
    commands.add(stepOver());
    commands.add(checkLocal(parameterName));
    commands.add(checkLocal(localVariableName));
    // TODO(shertz) check current method name ?
    commands.add(run());
    commands.add(run()  /* resume after 2nd breakpoint */);

    runDebugTestJava8(debuggeeClass, commands);
  }

  @Test
  public void testOverrideDefaultMethod() throws Throwable {
    String debuggeeClass = "DebugDefaultMethod";
    String parameterName = "msg";
    String localVariableName = "newMsg";

    List<Command> commands = new ArrayList<>();
    commands.add(breakpoint(debuggeeClass, "testDefaultMethod"));
    commands.add(run());
    commands.add(run() /* resume after 1st breakpoint */);
    commands.add(checkMethod(debuggeeClass, "testDefaultMethod"));
    commands.add(checkLine(27));
    commands.add(stepInto());
    commands.add(checkMethod("DebugDefaultMethod$OverrideImpl", "doSomething"));
    commands.add(checkLocal(parameterName));
    commands.add(stepOver());
    commands.add(checkLocal(parameterName));
    commands.add(checkLocal(localVariableName));
    commands.add(run());

    runDebugTestJava8(debuggeeClass, commands);
  }

}
