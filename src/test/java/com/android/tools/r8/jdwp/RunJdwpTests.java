// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.jdwp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.CompilationMode;
import com.android.tools.r8.D8;
import com.android.tools.r8.D8Command;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.ToolHelper.DexVm;
import com.android.tools.r8.ToolHelper.ProcessResult;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Wrapper for the art JDWP tests.
 *
 * A new version of this file can be generated using ./tools/create-jdwp-tests.py.
 */
public class RunJdwpTests {

  enum Tool {
    JAVAC,
    DX,
    D8
  }

  // Run all tests (default will only run smoke tests).
  static final boolean RUN_ALL_TESTS = false;

  // Print test output for passing tests (failing tests output is always printed).
  static final boolean PRINT_STREAMS = true;

  static final String RUN_SCRIPT = "tools/run-jdwp-tests.py";
  static final String DEX_LIB = "third_party/jdwp-tests/apache-harmony-jdwp-tests-hostdex.jar";
  static final String JAR_LIB = "third_party/jdwp-tests/apache-harmony-jdwp-tests-host.jar";

  interface TestPredicate {
    boolean test(DexVm dexVm, Tool tool);
  }

  static boolean isAndroidMOrAbove(DexVm dexVm, Tool tool) {
    return dexVm.isNewerThan(DexVm.ART_5_1_1);
  }

  static boolean isAndroidNOrAbove(DexVm dexVm, Tool tool) {
    return dexVm.isNewerThan(DexVm.ART_6_0_1);
  }

  static boolean isAndroidOOrAbove(DexVm dexVm, Tool tool) {
    return dexVm.isNewerThan(DexVm.ART_7_0_0);
  }

  static boolean isLatestRuntime(DexVm dexVm, Tool tool) {
    return dexVm == DexVm.ART_DEFAULT;
  }

  static final Map<String, TestPredicate> FLAKY_TESTS =
      ImmutableMap.<String, TestPredicate>builder()
          // Build bot is failing with ART segmentation faults on the following tests. b/63317743
          .put("StackFrame.GetValues002Test", RunJdwpTests::isAndroidMOrAbove)
          .put("ObjectReference.ReferringObjectsTest", RunJdwpTests::isAndroidMOrAbove)
          .put("VirtualMachine.InstanceCountsTest", RunJdwpTests::isAndroidMOrAbove)
          .put("ReferenceType.InstancesTest", RunJdwpTests::isAndroidMOrAbove)
          .put("EventModifiers.InstanceOnlyModifierTest", RunJdwpTests::isAndroidMOrAbove)
          .build();

  static final Map<String, TestPredicate> FAILING_TESTS =
      ImmutableMap.<String, TestPredicate>builder()
          .put("ArrayReference.SetValues003Test", RunJdwpTests::isAndroidNOrAbove)
          .put("ClassType.InvokeMethodAfterMultipleThreadSuspensionTest",
              RunJdwpTests::isAndroidNOrAbove)
          .put("ClassType.InvokeMethodWithSuspensionTest", RunJdwpTests::isAndroidMOrAbove)
          .put("ClassType.NewInstanceAfterMultipleThreadSuspensionTest",
              RunJdwpTests::isAndroidNOrAbove)
          .put("ClassType.NewInstanceStringTest", RunJdwpTests::isAndroidOOrAbove)
          .put("ClassType.NewInstanceTagTest", RunJdwpTests::isAndroidNOrAbove)
          .put("ClassType.NewInstanceWithSuspensionTest", RunJdwpTests::isAndroidMOrAbove)
          .put("Events.BreakpointTest", RunJdwpTests::isAndroidMOrAbove)
          .put("Events.Breakpoint002Test", RunJdwpTests::isAndroidMOrAbove)
          .put("Events.BreakpointOnCatchTest", RunJdwpTests::isAndroidMOrAbove)
          .put("Events.ClassPrepare002Test", RunJdwpTests::isAndroidOOrAbove)
          .put("Events.CombinedExceptionEventsTest", RunJdwpTests::isAndroidMOrAbove)
          .put("Events.ExceptionCaughtTest", RunJdwpTests::isAndroidMOrAbove)
          .put("Events.ExceptionUncaughtTest", RunJdwpTests::isAndroidMOrAbove)
          .put("Events.EventWithExceptionTest", RunJdwpTests::isAndroidNOrAbove)
          .put("Events.FieldAccessTest", RunJdwpTests::isAndroidMOrAbove)
          .put("Events.FieldModificationTest", RunJdwpTests::isAndroidMOrAbove)
          .put("Events.MethodEntryTest", RunJdwpTests::isAndroidMOrAbove)
          .put("Events.MethodExitTest", RunJdwpTests::isAndroidMOrAbove)
          .put("Events.MethodExitWithReturnValueTest", RunJdwpTests::isAndroidMOrAbove)
          .put("Events.SingleStepTest", RunJdwpTests::isAndroidMOrAbove)
          .put("Events.SingleStepWithPendingExceptionTest", RunJdwpTests::isAndroidNOrAbove)
          .put("InterfaceType.InvokeMethodTest", RunJdwpTests::isAndroidNOrAbove)
          .put("Method.IsObsoleteTest", RunJdwpTests::isAndroidNOrAbove)
          .put("Method.VariableTableWithGenericTest", RunJdwpTests::isAndroidOOrAbove)
          .put("ObjectReference.InvokeMethodDefaultTest", RunJdwpTests::isAndroidNOrAbove)
          .put("ObjectReference.InvokeMethodDefault002Test", RunJdwpTests::isAndroidNOrAbove)
          .put("ObjectReference.InvokeMethodAfterMultipleThreadSuspensionTest",
              RunJdwpTests::isAndroidNOrAbove)
          .put("ObjectReference.InvokeMethodWithSuspensionTest", RunJdwpTests::isAndroidMOrAbove)
          .put("ReferenceType.GetValues006Test", RunJdwpTests::isAndroidOOrAbove)
          .put("ReferenceType.ClassLoaderTest", RunJdwpTests::isAndroidNOrAbove)
          .put("StackFrame.GetValuesTest", RunJdwpTests::isAndroidMOrAbove)
          .put("StackFrame.SetValuesTest", RunJdwpTests::isAndroidMOrAbove)
          .put("StackFrame.SetValues002Test", RunJdwpTests::isAndroidMOrAbove)
          .put("VirtualMachine.CapabilitiesNewTest", RunJdwpTests::isLatestRuntime)
          .put("VirtualMachine.ClassPathsTest", RunJdwpTests::isAndroidMOrAbove)
          .put("VirtualMachine.DisposeDuringInvokeTest", RunJdwpTests::isAndroidMOrAbove)
          .build();

  // The smoke tests are the set of tests that fail if there is no debugging info in the dex files.
  // We avoid running the remaining tests as part of the D8/R8 testing to reduce test time by >30m.
  static final Set<String> SMOKE_TESTS = ImmutableSet.of(
      "EventModifiers.InstanceOnlyModifierTest",
      "Method.VariableTableTest",
      "Method.VariableTableWithGenericTest",
      "ObjectReference.ReferringObjectsTest",
      // This test assumes specific register allocation to make sure that a temporary object
      // is unreachable at a specific point in a method. That is not guaranteed. See b/36921933.
      // Currently doesn't fail but may start failing again with a count one higher than expected.
      "ReferenceType.InstancesTest",
      "StackFrame.GetValues002Test",
      "StackFrame.GetValuesTest",
      "StackFrame.SetValues002Test",
      "StackFrame.SetValuesTest",
      "VirtualMachine.InstanceCountsTest"
  );

  private static File d8Out = null;

  @ClassRule
  public static TemporaryFolder temp = new TemporaryFolder();

  @BeforeClass
  public static void compileLibraries() throws IOException, CompilationException {
    // Selects appropriate jar according to min api level for the selected runtime.
    int minApi = ToolHelper.getMinApiLevelForDexVm(ToolHelper.getDexVm());
    Path jdwpTestsJar = ToolHelper.getJdwpTestsJarPath(minApi);

    d8Out = temp.newFolder("d8-out");
    D8.run(
        D8Command.builder()
            .addProgramFiles(jdwpTestsJar)
            .setOutputPath(d8Out.toPath())
            .setMinApiLevel(minApi)
            .setMode(CompilationMode.DEBUG)
            .build());
  }

  String getTestLib(Tool tool) {
    if (tool == Tool.JAVAC) {
      return JAR_LIB;
    }
    if (tool == Tool.DX) {
      return DEX_LIB;
    }
    assert tool == Tool.D8;
    return d8Out.toPath().resolve("classes.dex").toString();
  }

  DexVm getDexVm() {
    return ToolHelper.getDexVm();
  }

  private void skipIfNeeded(String test, Tool tool) {
    // Is it part of smoke tests ?
    if (!RUN_ALL_TESTS) {
      Assume.assumeTrue("Skipping non-smoke test " + test, SMOKE_TESTS.contains(test));
    }
    Assume.assumeTrue("Skipping flaky test " + test,
        !FLAKY_TESTS.containsKey(test) || FLAKY_TESTS.get(test).test(getDexVm(), tool));
    if (tool != Tool.JAVAC) {
      // Can we run the test on the current ART runtime ?
      Assume.assumeTrue("Skipping test " + test + " because ART is not supported",
          ToolHelper.artSupported());
    }
  }

  void runTest(String test, Tool tool) throws IOException {
    skipIfNeeded(test, tool);
    System.out.println("Running test " + test + " for tool " + tool);
    String lib = getTestLib(tool);

    List<String> command;
    if (tool == Tool.JAVAC) {
      String run = "org.junit.runner.JUnitCore";
      String pkg = "org.apache.harmony.jpda.tests.jdwp";
      command = Arrays.asList(
          ToolHelper.getJavaExecutable(),
          "-cp", System.getProperty("java.class.path") + ":" + lib,
          run, pkg + "." + test);
    } else {
      command = Arrays.asList(
          RUN_SCRIPT, "--classpath=" + lib, "--version=" + ToolHelper.getDexVm(), test);
    }
    ProcessBuilder builder = new ProcessBuilder(command);
    ProcessResult result = ToolHelper.runProcess(builder);
    if (FAILING_TESTS.containsKey(test) && !FAILING_TESTS.get(test).test(getDexVm(), tool)) {
      if (PRINT_STREAMS || result.exitCode == 0) {
        printStreams(result);
      }
      assertNotEquals("Expected test " + test + " to fail but it succeeded", 0, result.exitCode);
    } else if (PRINT_STREAMS || result.exitCode != 0) {
      printStreams(result);
      assertEquals(0, result.exitCode);
    }
  }

  private void printStreams(ProcessResult result) {
    System.out.println("Test STDOUT");
    System.out.println(result.stdout);
    System.out.println("Test STDERR");
    System.out.println(result.stderr);
  }

  @Test
  public void testArrayReference_GetValuesTest_D8() throws IOException {
    runTest("ArrayReference.GetValuesTest", Tool.D8);
  }

  @Test
  public void testArrayReference_LengthTest_D8() throws IOException {
    runTest("ArrayReference.LengthTest", Tool.D8);
  }

  @Test
  public void testArrayReference_SetValues002Test_D8() throws IOException {
    runTest("ArrayReference.SetValues002Test", Tool.D8);
  }

  @Test
  public void testArrayReference_SetValues003Test_D8() throws IOException {
    runTest("ArrayReference.SetValues003Test", Tool.D8);
  }

  @Test
  public void testArrayReference_SetValuesTest_D8() throws IOException {
    runTest("ArrayReference.SetValuesTest", Tool.D8);
  }

  @Test
  public void testArrayType_NewInstanceTest_D8() throws IOException {
    runTest("ArrayType.NewInstanceTest", Tool.D8);
  }

  @Test
  public void testClassLoaderReference_VisibleClassesTest_D8() throws IOException {
    runTest("ClassLoaderReference.VisibleClassesTest", Tool.D8);
  }

  @Test
  public void testClassObjectReference_ReflectedType002Test_D8() throws IOException {
    runTest("ClassObjectReference.ReflectedType002Test", Tool.D8);
  }

  @Test
  public void testClassObjectReference_ReflectedTypeTest_D8() throws IOException {
    runTest("ClassObjectReference.ReflectedTypeTest", Tool.D8);
  }

  @Test
  public void testClassType_InvokeMethod002Test_D8() throws IOException {
    runTest("ClassType.InvokeMethod002Test", Tool.D8);
  }

  @Test
  public void testClassType_InvokeMethod003Test_D8() throws IOException {
    runTest("ClassType.InvokeMethod003Test", Tool.D8);
  }

  @Test
  public void testClassType_InvokeMethodAfterMultipleThreadSuspensionTest_D8()
      throws IOException {
    runTest("ClassType.InvokeMethodAfterMultipleThreadSuspensionTest", Tool.D8);
  }

  @Test
  public void testClassType_InvokeMethodWithSuspensionTest_D8() throws IOException {
    runTest("ClassType.InvokeMethodWithSuspensionTest", Tool.D8);
  }

  @Test
  public void testClassType_InvokeMethodTest_D8() throws IOException {
    runTest("ClassType.InvokeMethodTest", Tool.D8);
  }

  @Test
  public void testClassType_NewInstance002Test_D8() throws IOException {
    runTest("ClassType.NewInstance002Test", Tool.D8);
  }

  @Test
  public void testClassType_NewInstanceTagTest_D8() throws IOException {
    runTest("ClassType.NewInstanceTagTest", Tool.D8);
  }

  @Test
  public void testClassType_NewInstanceAfterMultipleThreadSuspensionTest_D8()
      throws IOException {
    runTest("ClassType.NewInstanceAfterMultipleThreadSuspensionTest", Tool.D8);
  }

  @Test
  public void testClassType_NewInstanceStringTest_D8() throws IOException {
    runTest("ClassType.NewInstanceStringTest", Tool.D8);
  }

  @Test
  public void testClassType_NewInstanceTest_D8() throws IOException {
    runTest("ClassType.NewInstanceTest", Tool.D8);
  }

  @Test
  public void testClassType_NewInstanceWithSuspensionTest_D8() throws IOException {
    runTest("ClassType.NewInstanceWithSuspensionTest", Tool.D8);
  }

  @Test
  public void testClassType_SetValues002Test_D8() throws IOException {
    runTest("ClassType.SetValues002Test", Tool.D8);
  }

  @Test
  public void testClassType_SetValuesTest_D8() throws IOException {
    runTest("ClassType.SetValuesTest", Tool.D8);
  }

  @Test
  public void testClassType_SuperClassTest_D8() throws IOException {
    runTest("ClassType.SuperClassTest", Tool.D8);
  }

  @Test
  public void testDeoptimization_DeoptimizationWithExceptionHandlingTest_D8()
      throws IOException {
    runTest("Deoptimization.DeoptimizationWithExceptionHandlingTest", Tool.D8);
  }

  @Test
  public void testEventModifiers_CountModifierTest_D8() throws IOException {
    runTest("EventModifiers.CountModifierTest", Tool.D8);
  }

  @Test
  public void testEventModifiers_InstanceOnlyModifierTest_D8() throws IOException {
    runTest("EventModifiers.InstanceOnlyModifierTest", Tool.D8);
  }

  @Test
  public void testEventModifiers_ThreadOnlyModifierTest_D8() throws IOException {
    runTest("EventModifiers.ThreadOnlyModifierTest", Tool.D8);
  }

  @Test
  public void testEvents_Breakpoint002Test_D8() throws IOException {
    runTest("Events.Breakpoint002Test", Tool.D8);
  }

  @Test
  public void testEvents_BreakpointMultipleTest_D8() throws IOException {
    runTest("Events.BreakpointMultipleTest", Tool.D8);
  }

  @Test
  public void testEvents_BreakpointOnCatchTest_D8() throws IOException {
    runTest("Events.BreakpointOnCatchTest", Tool.D8);
  }

  @Test
  public void testEvents_BreakpointTest_D8() throws IOException {
    runTest("Events.BreakpointTest", Tool.D8);
  }

  @Test
  public void testEvents_ClassPrepare002Test_D8() throws IOException {
    runTest("Events.ClassPrepare002Test", Tool.D8);
  }

  @Test
  public void testEvents_ClassPrepareTest_D8() throws IOException {
    runTest("Events.ClassPrepareTest", Tool.D8);
  }

  @Test
  public void testEvents_CombinedEvents002Test_D8() throws IOException {
    runTest("Events.CombinedEvents002Test", Tool.D8);
  }

  @Test
  public void testEvents_CombinedEvents003Test_D8() throws IOException {
    runTest("Events.CombinedEvents003Test", Tool.D8);
  }

  @Test
  public void testEvents_CombinedEventsTest_D8() throws IOException {
    runTest("Events.CombinedEventsTest", Tool.D8);
  }

  @Test
  public void testEvents_CombinedExceptionEventsTest_D8() throws IOException {
    runTest("Events.CombinedExceptionEventsTest", Tool.D8);
  }

  @Test
  public void testEvents_EventWithExceptionTest_D8() throws IOException {
    runTest("Events.EventWithExceptionTest", Tool.D8);
  }

  @Test
  public void testEvents_ExceptionCaughtTest_D8() throws IOException {
    runTest("Events.ExceptionCaughtTest", Tool.D8);
  }

  @Test
  public void testEvents_ExceptionUncaughtTest_D8() throws IOException {
    runTest("Events.ExceptionUncaughtTest", Tool.D8);
  }

  @Test
  public void testEvents_ExceptionWithLocationTest_D8() throws IOException {
    runTest("Events.ExceptionWithLocationTest", Tool.D8);
  }

  @Test
  public void testEvents_FieldAccessTest_D8() throws IOException {
    runTest("Events.FieldAccessTest", Tool.D8);
  }

  @Test
  public void testEvents_FieldModification002Test_D8() throws IOException {
    runTest("Events.FieldModification002Test", Tool.D8);
  }

  @Test
  public void testEvents_FieldModificationTest_D8() throws IOException {
    runTest("Events.FieldModificationTest", Tool.D8);
  }

  @Test
  public void testEvents_FieldWithLocationTest_D8() throws IOException {
    runTest("Events.FieldWithLocationTest", Tool.D8);
  }

  @Test
  public void testEvents_MethodEntryTest_D8() throws IOException {
    runTest("Events.MethodEntryTest", Tool.D8);
  }

  @Test
  public void testEvents_MethodExitTest_D8() throws IOException {
    runTest("Events.MethodExitTest", Tool.D8);
  }

  @Test
  public void testEvents_MethodExitWithReturnValueTest_D8() throws IOException {
    runTest("Events.MethodExitWithReturnValueTest", Tool.D8);
  }

  @Test
  public void testEvents_SingleStepTest_D8() throws IOException {
    runTest("Events.SingleStepTest", Tool.D8);
  }

  @Test
  public void testEvents_SingleStepThroughReflectionTest_D8() throws IOException {
    runTest("Events.SingleStepThroughReflectionTest", Tool.D8);
  }

  @Test
  public void testEvents_SingleStepWithLocationTest_D8() throws IOException {
    runTest("Events.SingleStepWithLocationTest", Tool.D8);
  }

  @Test
  public void testEvents_SingleStepWithPendingExceptionTest_D8() throws IOException {
    runTest("Events.SingleStepWithPendingExceptionTest", Tool.D8);
  }

  @Test
  public void testEvents_ThreadEndTest_D8() throws IOException {
    runTest("Events.ThreadEndTest", Tool.D8);
  }

  @Test
  public void testEvents_ThreadStartTest_D8() throws IOException {
    runTest("Events.ThreadStartTest", Tool.D8);
  }

  @Test
  public void testEvents_VMDeath002Test_D8() throws IOException {
    runTest("Events.VMDeath002Test", Tool.D8);
  }

  @Test
  public void testEvents_VMDeathTest_D8() throws IOException {
    runTest("Events.VMDeathTest", Tool.D8);
  }

  @Test
  public void testInterfaceType_InvokeMethodTest_D8() throws IOException {
    runTest("InterfaceType.InvokeMethodTest", Tool.D8);
  }

  @Test
  public void testMethod_BytecodesTest_D8() throws IOException {
    runTest("Method.BytecodesTest", Tool.D8);
  }

  @Test
  public void testMethod_IsObsoleteTest_D8() throws IOException {
    runTest("Method.IsObsoleteTest", Tool.D8);
  }

  @Test
  public void testMethod_LineTableTest_D8() throws IOException {
    runTest("Method.LineTableTest", Tool.D8);
  }

  @Test
  public void testMethod_VariableTableTest_D8() throws IOException {
    runTest("Method.VariableTableTest", Tool.JAVAC);
  }

  @Test
  public void testMethod_VariableTableWithGenericTest_D8() throws IOException {
    runTest("Method.VariableTableWithGenericTest", Tool.D8);
  }

  @Test
  public void testMultiSession_AttachConnectorTest_D8() throws IOException {
    runTest("MultiSession.AttachConnectorTest", Tool.D8);
  }

  @Test
  public void testMultiSession_BreakpointTest_D8() throws IOException {
    runTest("MultiSession.BreakpointTest", Tool.D8);
  }

  @Test
  public void testMultiSession_ClassObjectIDTest_D8() throws IOException {
    runTest("MultiSession.ClassObjectIDTest", Tool.D8);
  }

  @Test
  public void testMultiSession_ClassPrepareTest_D8() throws IOException {
    runTest("MultiSession.ClassPrepareTest", Tool.D8);
  }

  @Test
  public void testMultiSession_EnableCollectionTest_D8() throws IOException {
    runTest("MultiSession.EnableCollectionTest", Tool.D8);
  }

  @Test
  public void testMultiSession_ExceptionTest_D8() throws IOException {
    runTest("MultiSession.ExceptionTest", Tool.D8);
  }

  @Test
  public void testMultiSession_FieldAccessTest_D8() throws IOException {
    runTest("MultiSession.FieldAccessTest", Tool.D8);
  }

  @Test
  public void testMultiSession_FieldModificationTest_D8() throws IOException {
    runTest("MultiSession.FieldModificationTest", Tool.D8);
  }

  @Test
  public void testMultiSession_ListenConnectorTest_D8() throws IOException {
    runTest("MultiSession.ListenConnectorTest", Tool.D8);
  }

  @Test
  public void testMultiSession_MethodEntryExitTest_D8() throws IOException {
    runTest("MultiSession.MethodEntryExitTest", Tool.D8);
  }

  @Test
  public void testMultiSession_RefTypeIDTest_D8() throws IOException {
    runTest("MultiSession.RefTypeIDTest", Tool.D8);
  }

  @Test
  public void testMultiSession_ResumeTest_D8() throws IOException {
    runTest("MultiSession.ResumeTest", Tool.D8);
  }

  @Test
  public void testMultiSession_SingleStepTest_D8() throws IOException {
    runTest("MultiSession.SingleStepTest", Tool.D8);
  }

  @Test
  public void testMultiSession_VMDeathTest_D8() throws IOException {
    runTest("MultiSession.VMDeathTest", Tool.D8);
  }

  @Test
  public void testObjectReference_DisableCollectionTest_D8() throws IOException {
    runTest("ObjectReference.DisableCollectionTest", Tool.D8);
  }

  @Test
  public void testObjectReference_EnableCollectionTest_D8() throws IOException {
    runTest("ObjectReference.EnableCollectionTest", Tool.D8);
  }

  @Test
  public void testObjectReference_GetValues002Test_D8() throws IOException {
    runTest("ObjectReference.GetValues002Test", Tool.D8);
  }

  @Test
  public void testObjectReference_GetValues003Test_D8() throws IOException {
    runTest("ObjectReference.GetValues003Test", Tool.D8);
  }

  @Test
  public void testObjectReference_GetValuesTest_D8() throws IOException {
    runTest("ObjectReference.GetValuesTest", Tool.D8);
  }

  @Test
  public void testObjectReference_InvokeMethod002Test_D8() throws IOException {
    runTest("ObjectReference.InvokeMethod002Test", Tool.D8);
  }

  @Test
  public void testObjectReference_InvokeMethod003Test_D8() throws IOException {
    runTest("ObjectReference.InvokeMethod003Test", Tool.D8);
  }

  @Test
  public void testObjectReference_InvokeMethodTest_D8() throws IOException {
    runTest("ObjectReference.InvokeMethodTest", Tool.D8);
  }

  @Test
  public void testObjectReference_InvokeMethodAfterMultipleThreadSuspensionTest_D8()
      throws IOException {
    runTest("ObjectReference.InvokeMethodAfterMultipleThreadSuspensionTest", Tool.D8);
  }

  @Test
  public void testObjectReference_InvokeMethodDefault002Test_D8() throws IOException {
    runTest("ObjectReference.InvokeMethodDefault002Test", Tool.D8);
  }

  @Test
  public void testObjectReference_InvokeMethodDefaultTest_D8() throws IOException {
    runTest("ObjectReference.InvokeMethodDefaultTest", Tool.D8);
  }

  @Test
  public void testObjectReference_InvokeMethodWithSuspensionTest_D8() throws IOException {
    runTest("ObjectReference.InvokeMethodWithSuspensionTest", Tool.D8);
  }

  @Test
  public void testObjectReference_IsCollectedTest_D8() throws IOException {
    runTest("ObjectReference.IsCollectedTest", Tool.D8);
  }

  @Test
  public void testObjectReference_MonitorInfoTest_D8() throws IOException {
    runTest("ObjectReference.MonitorInfoTest", Tool.D8);
  }

  @Test
  public void testObjectReference_ReferenceTypeTest_D8() throws IOException {
    runTest("ObjectReference.ReferenceTypeTest", Tool.D8);
  }

  @Test
  public void testObjectReference_ReferringObjectsTest_D8() throws IOException {
    runTest("ObjectReference.ReferringObjectsTest", Tool.D8);
  }

  @Test
  public void testObjectReference_SetValues002Test_D8() throws IOException {
    runTest("ObjectReference.SetValues002Test", Tool.D8);
  }

  @Test
  public void testObjectReference_SetValues003Test_D8() throws IOException {
    runTest("ObjectReference.SetValues003Test", Tool.D8);
  }

  @Test
  public void testObjectReference_SetValues004Test_D8() throws IOException {
    runTest("ObjectReference.SetValues004Test", Tool.D8);
  }

  @Test
  public void testObjectReference_SetValuesTest_D8() throws IOException {
    runTest("ObjectReference.SetValuesTest", Tool.D8);
  }

  @Test
  public void testReferenceType_ClassLoaderTest_D8() throws IOException {
    runTest("ReferenceType.ClassLoaderTest", Tool.D8);
  }

  @Test
  public void testReferenceType_ClassObjectTest_D8() throws IOException {
    runTest("ReferenceType.ClassObjectTest", Tool.D8);
  }

  @Test
  public void testReferenceType_ConstantPoolTest_D8() throws IOException {
    runTest("ReferenceType.ConstantPoolTest", Tool.D8);
  }

  @Test
  public void testReferenceType_FieldsTest_D8() throws IOException {
    runTest("ReferenceType.FieldsTest", Tool.D8);
  }

  @Test
  public void testReferenceType_FieldsWithGenericTest_D8() throws IOException {
    runTest("ReferenceType.FieldsWithGenericTest", Tool.D8);
  }

  @Test
  public void testReferenceType_GetValues002Test_D8() throws IOException {
    runTest("ReferenceType.GetValues002Test", Tool.D8);
  }

  @Test
  public void testReferenceType_GetValues003Test_D8() throws IOException {
    runTest("ReferenceType.GetValues003Test", Tool.D8);
  }

  @Test
  public void testReferenceType_GetValues004Test_D8() throws IOException {
    runTest("ReferenceType.GetValues004Test", Tool.D8);
  }

  @Test
  public void testReferenceType_GetValues005Test_D8() throws IOException {
    runTest("ReferenceType.GetValues005Test", Tool.D8);
  }

  @Test
  public void testReferenceType_GetValues006Test_D8() throws IOException {
    runTest("ReferenceType.GetValues006Test", Tool.D8);
  }

  @Test
  public void testReferenceType_GetValues007Test_D8() throws IOException {
    runTest("ReferenceType.GetValues007Test", Tool.D8);
  }

  @Test
  public void testReferenceType_GetValuesTest_D8() throws IOException {
    runTest("ReferenceType.GetValuesTest", Tool.D8);
  }

  @Test
  public void testReferenceType_InstancesTest_D8() throws IOException {
    runTest("ReferenceType.InstancesTest", Tool.D8);
  }

  @Test
  public void testReferenceType_InterfacesTest_D8() throws IOException {
    runTest("ReferenceType.InterfacesTest", Tool.D8);
  }

  @Test
  public void testReferenceType_MethodsTest_D8() throws IOException {
    runTest("ReferenceType.MethodsTest", Tool.D8);
  }

  @Test
  public void testReferenceType_MethodsWithGenericTest_D8() throws IOException {
    runTest("ReferenceType.MethodsWithGenericTest", Tool.D8);
  }

  @Test
  public void testReferenceType_ModifiersTest_D8() throws IOException {
    runTest("ReferenceType.ModifiersTest", Tool.D8);
  }

  @Test
  public void testReferenceType_Signature002Test_D8() throws IOException {
    runTest("ReferenceType.Signature002Test", Tool.D8);
  }

  @Test
  public void testReferenceType_SignatureTest_D8() throws IOException {
    runTest("ReferenceType.SignatureTest", Tool.D8);
  }

  @Test
  public void testReferenceType_SignatureWithGenericTest_D8() throws IOException {
    runTest("ReferenceType.SignatureWithGenericTest", Tool.D8);
  }

  @Test
  public void testReferenceType_SourceDebugExtensionTest_D8() throws IOException {
    runTest("ReferenceType.SourceDebugExtensionTest", Tool.D8);
  }

  @Test
  public void testReferenceType_SourceFileTest_D8() throws IOException {
    runTest("ReferenceType.SourceFileTest", Tool.D8);
  }

  @Test
  public void testReferenceType_StatusTest_D8() throws IOException {
    runTest("ReferenceType.StatusTest", Tool.D8);
  }

  @Test
  public void testReferenceType_SyntheticFieldsTest_D8() throws IOException {
    runTest("ReferenceType.SyntheticFieldsTest", Tool.D8);
  }

  @Test
  public void testReferenceType_SyntheticMethodsTest_D8() throws IOException {
    runTest("ReferenceType.SyntheticMethodsTest", Tool.D8);
  }

  @Test
  public void testStackFrame_GetValues002Test_D8() throws IOException {
    runTest("StackFrame.GetValues002Test", Tool.D8);
  }

  @Test
  public void testStackFrame_GetValuesTest_D8() throws IOException {
    runTest("StackFrame.GetValuesTest", Tool.D8);
  }

  @Test
  public void testStackFrame_PopFrames002Test_D8() throws IOException {
    runTest("StackFrame.PopFrames002Test", Tool.D8);
  }

  @Test
  public void testStackFrame_PopFramesTest_D8() throws IOException {
    runTest("StackFrame.PopFramesTest", Tool.D8);
  }

  @Test
  public void testStackFrame_ProxyThisObjectTest_D8() throws IOException {
    runTest("StackFrame.ProxyThisObjectTest", Tool.D8);
  }

  @Test
  public void testStackFrame_SetValues002Test_D8() throws IOException {
    runTest("StackFrame.SetValues002Test", Tool.D8);
  }

  @Test
  public void testStackFrame_SetValuesTest_D8() throws IOException {
    runTest("StackFrame.SetValuesTest", Tool.D8);
  }

  @Test
  public void testStackFrame_ThisObjectTest_D8() throws IOException {
    runTest("StackFrame.ThisObjectTest", Tool.D8);
  }

  @Test
  public void testStringReference_ValueTest_D8() throws IOException {
    runTest("StringReference.ValueTest", Tool.D8);
  }

  @Test
  public void testThreadGroupReference_ChildrenTest_D8() throws IOException {
    runTest("ThreadGroupReference.ChildrenTest", Tool.D8);
  }

  @Test
  public void testThreadGroupReference_NameTest_D8() throws IOException {
    runTest("ThreadGroupReference.NameTest", Tool.D8);
  }

  @Test
  public void testThreadGroupReference_ParentTest_D8() throws IOException {
    runTest("ThreadGroupReference.ParentTest", Tool.D8);
  }

  @Test
  public void testThreadReference_CurrentContendedMonitorTest_D8() throws IOException {
    runTest("ThreadReference.CurrentContendedMonitorTest", Tool.D8);
  }

  @Test
  public void testThreadReference_ForceEarlyReturn002Test_D8() throws IOException {
    runTest("ThreadReference.ForceEarlyReturn002Test", Tool.D8);
  }

  @Test
  public void testThreadReference_ForceEarlyReturn003Test_D8() throws IOException {
    runTest("ThreadReference.ForceEarlyReturn003Test", Tool.D8);
  }

  @Test
  public void testThreadReference_ForceEarlyReturn004Test_D8() throws IOException {
    runTest("ThreadReference.ForceEarlyReturn004Test", Tool.D8);
  }

  @Test
  public void testThreadReference_ForceEarlyReturn005Test_D8() throws IOException {
    runTest("ThreadReference.ForceEarlyReturn005Test", Tool.D8);
  }

  @Test
  public void testThreadReference_ForceEarlyReturn006Test_D8() throws IOException {
    runTest("ThreadReference.ForceEarlyReturn006Test", Tool.D8);
  }

  @Test
  public void testThreadReference_ForceEarlyReturnTest_D8() throws IOException {
    runTest("ThreadReference.ForceEarlyReturnTest", Tool.D8);
  }

  @Test
  public void testThreadReference_FrameCountTest_D8() throws IOException {
    runTest("ThreadReference.FrameCountTest", Tool.D8);
  }

  @Test
  public void testThreadReference_FramesTest_D8() throws IOException {
    runTest("ThreadReference.FramesTest", Tool.D8);
  }

  @Test
  public void testThreadReference_InterruptTest_D8() throws IOException {
    runTest("ThreadReference.InterruptTest", Tool.D8);
  }

  @Test
  public void testThreadReference_NameTest_D8() throws IOException {
    runTest("ThreadReference.NameTest", Tool.D8);
  }

  @Test
  public void testThreadReference_OwnedMonitorsStackDepthInfoTest_D8() throws IOException {
    runTest("ThreadReference.OwnedMonitorsStackDepthInfoTest", Tool.D8);
  }

  @Test
  public void testThreadReference_OwnedMonitorsTest_D8() throws IOException {
    runTest("ThreadReference.OwnedMonitorsTest", Tool.D8);
  }

  @Test
  public void testThreadReference_ResumeTest_D8() throws IOException {
    runTest("ThreadReference.ResumeTest", Tool.D8);
  }

  @Test
  public void testThreadReference_Status002Test_D8() throws IOException {
    runTest("ThreadReference.Status002Test", Tool.D8);
  }

  @Test
  public void testThreadReference_Status003Test_D8() throws IOException {
    runTest("ThreadReference.Status003Test", Tool.D8);
  }

  @Test
  public void testThreadReference_Status004Test_D8() throws IOException {
    runTest("ThreadReference.Status004Test", Tool.D8);
  }

  @Test
  public void testThreadReference_Status005Test_D8() throws IOException {
    runTest("ThreadReference.Status005Test", Tool.D8);
  }

  @Test
  public void testThreadReference_Status006Test_D8() throws IOException {
    runTest("ThreadReference.Status006Test", Tool.D8);
  }

  @Test
  public void testThreadReference_StatusTest_D8() throws IOException {
    runTest("ThreadReference.StatusTest", Tool.D8);
  }

  @Test
  public void testThreadReference_SuspendCountTest_D8() throws IOException {
    runTest("ThreadReference.SuspendCountTest", Tool.D8);
  }

  @Test
  public void testThreadReference_SuspendTest_D8() throws IOException {
    runTest("ThreadReference.SuspendTest", Tool.D8);
  }

  @Test
  public void testThreadReference_ThreadGroup002Test_D8() throws IOException {
    runTest("ThreadReference.ThreadGroup002Test", Tool.D8);
  }

  @Test
  public void testThreadReference_ThreadGroupTest_D8() throws IOException {
    runTest("ThreadReference.ThreadGroupTest", Tool.D8);
  }

  @Test
  public void testVirtualMachine_AllClassesTest_D8() throws IOException {
    runTest("VirtualMachine.AllClassesTest", Tool.D8);
  }

  @Test
  public void testVirtualMachine_AllClassesWithGenericTest_D8() throws IOException {
    runTest("VirtualMachine.AllClassesWithGenericTest", Tool.D8);
  }

  @Test
  public void testVirtualMachine_AllThreadsTest_D8() throws IOException {
    runTest("VirtualMachine.AllThreadsTest", Tool.D8);
  }

  @Test
  public void testVirtualMachine_CapabilitiesNewTest_D8() throws IOException {
    runTest("VirtualMachine.CapabilitiesNewTest", Tool.D8);
  }

  @Test
  public void testVirtualMachine_CapabilitiesTest_D8() throws IOException {
    runTest("VirtualMachine.CapabilitiesTest", Tool.D8);
  }

  @Test
  public void testVirtualMachine_ClassesBySignatureTest_D8() throws IOException {
    runTest("VirtualMachine.ClassesBySignatureTest", Tool.D8);
  }

  @Test
  public void testVirtualMachine_ClassPathsTest_D8() throws IOException {
    runTest("VirtualMachine.ClassPathsTest", Tool.D8);
  }

  @Test
  public void testVirtualMachine_CreateStringTest_D8() throws IOException {
    runTest("VirtualMachine.CreateStringTest", Tool.D8);
  }

  @Test
  public void testVirtualMachine_DisposeDuringInvokeTest_D8() throws IOException {
    runTest("VirtualMachine.DisposeDuringInvokeTest", Tool.D8);
  }

  @Test
  public void testVirtualMachine_DisposeTest_D8() throws IOException {
    runTest("VirtualMachine.DisposeTest", Tool.D8);
  }

  @Test
  public void testVirtualMachine_DisposeObjectsTest_D8() throws IOException {
    runTest("VirtualMachine.DisposeObjectsTest", Tool.D8);
  }

  @Test
  public void testVirtualMachine_ExitTest_D8() throws IOException {
    runTest("VirtualMachine.ExitTest", Tool.D8);
  }

  @Test
  public void testVirtualMachine_IDSizesTest_D8() throws IOException {
    runTest("VirtualMachine.IDSizesTest", Tool.D8);
  }

  @Test
  public void testVirtualMachine_InstanceCountsTest_D8() throws IOException {
    runTest("VirtualMachine.InstanceCountsTest", Tool.D8);
  }

  @Test
  public void testVirtualMachine_RedefineClassesTest_D8() throws IOException {
    runTest("VirtualMachine.RedefineClassesTest", Tool.D8);
  }

  @Test
  public void testVirtualMachine_Resume002Test_D8() throws IOException {
    runTest("VirtualMachine.Resume002Test", Tool.D8);
  }

  @Test
  public void testVirtualMachine_ResumeTest_D8() throws IOException {
    runTest("VirtualMachine.ResumeTest", Tool.D8);
  }

  @Test
  public void testVirtualMachine_SetDefaultStratumTest_D8() throws IOException {
    runTest("VirtualMachine.SetDefaultStratumTest", Tool.D8);
  }

  @Test
  public void testVirtualMachine_SuspendTest_D8() throws IOException {
    runTest("VirtualMachine.SuspendTest", Tool.D8);
  }

  @Test
  public void testVirtualMachine_TopLevelThreadGroupsTest_D8() throws IOException {
    runTest("VirtualMachine.TopLevelThreadGroupsTest", Tool.D8);
  }

  @Test
  public void testVirtualMachine_VersionTest_D8() throws IOException {
    runTest("VirtualMachine.VersionTest", Tool.D8);
  }
}
