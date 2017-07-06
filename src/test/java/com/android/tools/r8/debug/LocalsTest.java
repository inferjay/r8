// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.debug;

import com.android.tools.r8.debug.DebugTestBase.JUnit3Wrapper.FrameInspector;
import java.util.HashMap;
import java.util.Map;
import org.apache.harmony.jpda.tests.framework.jdwp.JDWPConstants.Tag;
import org.apache.harmony.jpda.tests.framework.jdwp.Value;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests local variable information.
 */
public class LocalsTest extends DebugTestBase {

  public static final String SOURCE_FILE = "Locals.java";

  @Test
  public void testNoLocal() throws Throwable {
    final String className = "Locals";
    final String methodName = "noLocals";
    runDebugTest(className,
        breakpoint(className, methodName),
        run(),
        checkMethod(className, methodName),
        checkLine(SOURCE_FILE, 8),
        checkNoLocal(),
        stepOver(),
        checkMethod(className, methodName),
        checkLine(SOURCE_FILE, 9),
        checkNoLocal(),
        run());
  }

  @Test
  public void testUnusedLocal() throws Throwable {
    final String className = "Locals";
    final String methodName = "unusedLocals";
    runDebugTest(className,
        breakpoint(className, methodName),
        run(),
        checkMethod(className, methodName),
        checkLine(SOURCE_FILE, 12),
        checkNoLocal(),
        stepOver(),
        checkLine(SOURCE_FILE, 13),
        checkLocal("i", Value.createInt(Integer.MAX_VALUE)),
        run());
  }

  @Test
  public void testConstantLocal() throws Throwable {
    final String className = "Locals";
    final String methodName = "constantLocals";
    Value pValue = Value.createInt(10);
    Value cValue = Value.createInt(5);
    Value vValue = Value.createInt(pValue.getIntValue() + cValue.getIntValue());

    runDebugTest(className,
        breakpoint(className, methodName),
        run(),
        checkMethod(className, methodName),
        checkLine(SOURCE_FILE, 17),
        checkLocal("p", pValue),
        stepOver(),
        checkLine(SOURCE_FILE, 18),
        checkLocal("p", pValue),
        checkLocal("c", cValue),
        stepOver(),
        checkLine(SOURCE_FILE, 19),
        checkLocal("p", pValue),
        checkLocal("c", cValue),
        checkLocal("v", vValue),
        run());
  }

  @Test
  public void testConstantLocalWithUpdate() throws Throwable {
    final String className = "Locals";
    final String methodName = "constantLocals";
    Value pValue = Value.createInt(10);
    Value cValue = Value.createInt(5);
    Value newValue = Value.createInt(5);
    Value vValue = Value.createInt(pValue.getIntValue() + newValue.getIntValue());

    runDebugTest(className,
        breakpoint(className, methodName),
        run(),
        checkMethod(className, methodName),
        checkLine(SOURCE_FILE, 17),
        checkLocal("p", pValue),
        stepOver(),
        checkLine(SOURCE_FILE, 18),
        checkLocal("p", pValue),
        checkLocal("c", cValue),
        setLocal("c", newValue),
        checkLocal("c", newValue),  // we should see the updated value
        stepOver(),
        checkLine(SOURCE_FILE, 19),
        checkLocal("p", pValue),
        checkLocal("c", newValue),
        checkLocal("v", vValue),
        run());
  }

  @Test
  public void testZeroLocals() throws Throwable {
    final String className = "Locals";
    final String methodName = "zeroLocals";
    final Value newValueForI = Value.createInt(10);
    runDebugTest(className,
        breakpoint(className, methodName),
        run(),
        checkMethod(className, methodName),
        checkLine(SOURCE_FILE, 23),
        checkNoLocal(),
        stepOver(),
        checkMethod(className, methodName),
        checkLine(SOURCE_FILE, 24),
        checkLocal("i", Value.createInt(0)),
        setLocal("i", newValueForI),
        stepOver(),
        checkLine(SOURCE_FILE, 25),
        checkLocal("i", newValueForI),
        checkLocal("f", Value.createFloat(0)),
        run());
  }

  @Test
  public void testNoFlowOptimization() throws Throwable {
    final String className = "Locals";
    final String methodName = "noFlowOptimization";
    final Value oldValueForI = Value.createInt(0);
    final Value newValueForI = Value.createInt(10);
    runDebugTest(className,
        breakpoint(className, methodName),
        run(),
        checkMethod(className, methodName),
        checkLine(SOURCE_FILE, 29),
        checkNoLocal(),
        stepOver(),
        checkMethod(className, methodName),
        checkLine(SOURCE_FILE, 30),
        checkLocal("i", oldValueForI),
        setLocal("i", newValueForI),
        stepOver(),
        checkLine(SOURCE_FILE, 33),
        checkLocal("i", newValueForI),
        run());
  }

  @Test
  public void testInvokeRange() throws Throwable {
    runDebugTest("Locals",
        breakpoint("Locals", "invokeRange"),
        run(),
        inspect(state -> {
          // 1st breakpoint
          Assert.assertEquals("invokeRange", state.getMethodName());
          Assert.assertEquals(58, state.getLineNumber());
          state.checkLocal("a", Value.createInt(12));
          state.checkLocal("b", Value.createInt(11));
          state.checkLocal("c", Value.createInt(10));
          state.checkLocal("d", Value.createInt(9));
          state.checkLocal("e", Value.createInt(8));
          state.checkLocal("f", Value.createInt(7));
          state.checkLocal("g", Value.createInt(0));

          FrameInspector outerFrame = state.getFrame(1);
          for (int i = 1; i < 12; ++i) {
            outerFrame.checkLocal("i" + i, Value.createInt(i));
          }
        }),
        run(),
        inspect(state -> {
          // 2nd breakpoint
          Assert.assertEquals("invokeRange", state.getMethodName());
          Assert.assertEquals(58, state.getLineNumber());
          state.checkLocal("a", Value.createInt(6));
          state.checkLocal("b", Value.createInt(5));
          state.checkLocal("c", Value.createInt(4));
          state.checkLocal("d", Value.createInt(3));
          state.checkLocal("e", Value.createInt(2));
          state.checkLocal("f", Value.createInt(1));
          state.checkLocal("g", Value.createInt(57));

          FrameInspector outerFrame = state.getFrame(1);
          for (int i = 1; i < 12; ++i) {
            outerFrame.checkLocal("i" + i, Value.createInt(i));
          }
        }),
        run(),
        // TODO(shertz) maybe we should duplicate invokeRange to avoid this extra 'skip'.
        // Skip last breakpoint
        run());
  }

  @Test
  public void testInvokeRange2() throws Throwable {
    runDebugTest("Locals",
        breakpoint("Locals", "reverseRange"),
        run(),
        inspect(state -> {
          Assert.assertEquals("reverseRange", state.getMethodName());
          Assert.assertEquals(54, state.getLineNumber());
          state.checkLocal("a", Value.createInt(1));
          state.checkLocal("b", Value.createInt(2));
          state.checkLocal("c", Value.createInt(3));
          state.checkLocal("d", Value.createInt(4));
          state.checkLocal("e", Value.createInt(5));
          state.checkLocal("f", Value.createInt(6));
          state.checkLocal("g", Value.createInt(7));
        }),
        stepInto(),
        inspect(state -> {
          Assert.assertEquals("invokeRange", state.getMethodName());
          Assert.assertEquals(58, state.getLineNumber());
          state.checkLocal("a", Value.createInt(7));
          state.checkLocal("b", Value.createInt(6));
          state.checkLocal("c", Value.createInt(5));
          state.checkLocal("d", Value.createInt(4));
          state.checkLocal("e", Value.createInt(3));
          state.checkLocal("f", Value.createInt(2));
          state.checkLocal("g", Value.createInt(1));
        }),
        inspect(state -> {
          FrameInspector outerFrame = state.getFrame(1);
          outerFrame.checkLocal("a", Value.createInt(1));
          outerFrame.checkLocal("b", Value.createInt(2));
          outerFrame.checkLocal("c", Value.createInt(3));
          outerFrame.checkLocal("d", Value.createInt(4));
          outerFrame.checkLocal("e", Value.createInt(5));
          outerFrame.checkLocal("f", Value.createInt(6));
          outerFrame.checkLocal("g", Value.createInt(7));
        }),
        run());
  }

  @Test
  public void testLocals_MoreThan16() throws Throwable {
    final int minIndex = 1;
    final int maxIndex = 16;
    Map<String, Value> arrayLocals = new HashMap<>();
    runDebugTest("Locals",
        breakpoint("Locals", "breakpoint"),
        run(),
        inspect(state -> {
          // 1st breakpoint: all lengthOfArray[N] are set to 0
          FrameInspector outerFrame = state.getFrame(1);

          Map<String, Value> localValues = outerFrame.getLocalValues();

          for (int i = minIndex; i <= maxIndex; ++i) {
            String varName = "lengthOfArray" + i;
            Assert.assertTrue(localValues.containsKey(varName));
            Assert.assertEquals(Value.createInt(0), localValues.get(varName));
          }

          // Capture IDs of arrays.
          for (int i = minIndex; i <= maxIndex; ++i) {
            String varName = "array" + i;
            Assert.assertTrue(localValues.containsKey(varName));
            arrayLocals.put(varName, localValues.get(varName));
          }
        }),
        // Step out to reach next instructions in the tested method
        stepOut(),
        inspect(state -> {
          Assert.assertEquals("Locals.java", state.getSourceFile());
          Assert.assertEquals(107, state.getLineNumber());
          // Verify that all arrays have the same value.
          arrayLocals.forEach((name, value) -> state.checkLocal(name, value));
        }),
        // Step instruction by instruction to ensure all locals previously declared are safe.
        stepUntil(StepKind.OVER, StepLevel.INSTRUCTION, state -> {
          final String sourceFile = state.getSourceFile();
          final int lineNumber = state.getLineNumber();
          arrayLocals.forEach((name, value) -> state.checkLocal(name, value));
          // Stop when we reach the expected line.
          return lineNumber == 125 && sourceFile.equals("Locals.java");
        }),
        run());
  }

  @Test
  public void testInvokeRangeLong() throws Throwable {
    final int initialValueOfX = 21;
    final long expectedValueOfL = (long) initialValueOfX * 2;
    final int expectedValueOfX = (int) expectedValueOfL / initialValueOfX;
    runDebugTest("Locals",
        breakpoint("Locals", "invokerangeLong"),
        run(),
        inspect(state -> {
          FrameInspector outerFrame = state.getFrame(1);
          Map<String, Value> values = outerFrame.getLocalValues();
          Assert.assertTrue("No variable 'x'", values.containsKey("x"));
          Assert.assertTrue("No variable 'obj'", values.containsKey("obj"));
          Assert.assertTrue("No variable 'l'", values.containsKey("l"));

          // 'x' is an int
          Value valueOfX = values.get("x");
          Assert.assertEquals(Tag.INT_TAG, valueOfX.getTag());
          Assert.assertEquals(Value.createInt(expectedValueOfX), valueOfX);

          // 'obj' is an Object (Integer).
          Value valueOfObj = values.get("obj");
          Assert.assertEquals(Tag.OBJECT_TAG, valueOfObj.getTag());

          // 'l' is a long.
          Value valueOfL = values.get("l");
          Assert.assertEquals(Tag.LONG_TAG, valueOfL.getTag());
          Assert.assertEquals(Value.createLong(expectedValueOfL), valueOfL);
        }),
        run());
  }

}
