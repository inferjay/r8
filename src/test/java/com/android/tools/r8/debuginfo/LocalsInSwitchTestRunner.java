// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.debuginfo;

import static org.junit.Assert.assertEquals;

import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.utils.AndroidApp;
import org.junit.Test;

public class LocalsInSwitchTestRunner extends DebugInfoTestBase {

  @Test
  public void testLocalsInSwitch() throws Exception {
    Class clazz = LocalsInSwitchTest.class;

    AndroidApp d8App = compileWithD8(clazz);
    AndroidApp dxApp = getDxCompiledSources();

    String expected = "55" + ToolHelper.LINE_SEPARATOR + "1862" + ToolHelper.LINE_SEPARATOR
            + "15130" + ToolHelper.LINE_SEPARATOR;
    assertEquals(expected, runOnJava(clazz));
    assertEquals(expected, runOnArt(d8App, clazz.getCanonicalName()));
    assertEquals(expected, runOnArt(dxApp, clazz.getCanonicalName()));

    checkNoLocals(inspectMethod(d8App, clazz, "int", "noLocals", "int"));
    checkNoLocals(inspectMethod(dxApp, clazz, "int", "noLocals", "int"));

    checkTempInCase(inspectMethod(d8App, clazz, "int", "tempInCase", "int"), false);
    checkTempInCase(inspectMethod(dxApp, clazz, "int", "tempInCase", "int"), true);

    checkInitInCases(inspectMethod(d8App, clazz, "int", "initInCases", "int"));
    checkInitInCases(inspectMethod(dxApp, clazz, "int", "initInCases", "int"));
  }

  private void checkNoLocals(DebugInfoInspector info) {
    info.checkStartLine(9);
    info.checkLineHasExactLocals(9, "x", "int");
    info.checkLineHasExactLocals(11, "x", "int");
    info.checkLineHasExactLocals(13, "x", "int");
    info.checkLineHasExactLocals(15, "x", "int");
  }

  private void checkTempInCase(DebugInfoInspector tempInCase, boolean dx) {
    // int res =
    tempInCase.checkStartLine(20);
    tempInCase.checkLineHasExactLocals(20, "x", "int");
    // for (int i = ...
    // The local 'i' is visible on the back edges, but not on the initial entry.
    tempInCase.checkLineHasAtLeastLocals(21, "x", "int", "res", "int");
    //   int rem =
    tempInCase.checkLineHasExactLocals(22, "x", "int", "res", "int", "i", "int");
    //   switch (rem) {
    if (!dx) {
      // DX contains several entries for 23, one of which does not define 'rem'. Go figure...
      tempInCase.checkLineHasExactLocals(23, "x", "int", "res", "int", "i", "int", "rem", "int");
    }
    //   case 0:
    tempInCase.checkNoLine(24);
    //     return res
    if (!dx) {
      // DX does not produce a position at the return statement. Good stuff.
      tempInCase.checkLineHasExactLocals(25, "x", "int", "res", "int", "i", "int", "rem", "int");
    }
    //   case 5:
    tempInCase.checkNoLine(26);
    //     int tmp =
    tempInCase.checkLineHasExactLocals(27, "x", "int", "res", "int", "i", "int", "rem", "int");
    //     res += tmp
    tempInCase.checkLineHasExactLocals(28,
        "x", "int", "res", "int", "i", "int", "rem", "int", "tmp", "int");
    //     break;
    tempInCase.checkLineHasExactLocals(29,
        "x", "int", "res", "int", "i", "int", "rem", "int", "tmp", "int");
    //   case 10:
    tempInCase.checkNoLine(30);
    //     i++
    tempInCase.checkLineHasExactLocals(31, "x", "int", "res", "int", "i", "int", "rem", "int");
    //     break;
    tempInCase.checkLineHasExactLocals(32, "x", "int", "res", "int", "i", "int", "rem", "int");
    //   default:
    tempInCase.checkNoLine(33);
    //     res += rem;
    tempInCase.checkLineHasExactLocals(34, "x", "int", "res", "int", "i", "int", "rem", "int");
    //   }
    tempInCase.checkNoLine(35);
    //   res += rem % 2;
    tempInCase.checkLineHasExactLocals(36, "x", "int", "res", "int", "i", "int", "rem", "int");
    // }
    tempInCase.checkNoLine(37);
    // res *= x;
    if (!dx) {
      // DX fails to end the scope of "i" after the loop.
      tempInCase.checkLineHasExactLocals(38, "x", "int", "res", "int");
    }
    // return res;
    if (!dx) {
      tempInCase.checkLineHasExactLocals(39, "x", "int", "res", "int");
    }
  }

  private void checkInitInCases(DebugInfoInspector info) {
    info.checkNoLine(43); // No line on uninitialized local declaration.
    info.checkStartLine(44);
    info.checkLineHasExactLocals(44, "x", "int"); // Local "res" is still not visible in the case.
    info.checkLineHasExactLocals(46, "x", "int"); // Ditto.
    info.checkLineHasExactLocals(48, "x", "int"); // Ditto.
    info.checkLineHasExactLocals(51, "x", "int"); // Ditto.
    info.checkLineHasExactLocals(53, "x", "int", "res", "java.lang.Integer");
  }
}
