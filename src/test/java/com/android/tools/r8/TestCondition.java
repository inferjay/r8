// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.R8RunArtTestsTest.CompilerUnderTest;
import com.android.tools.r8.R8RunArtTestsTest.DexTool;
import com.android.tools.r8.ToolHelper.DexVm;
import java.util.Arrays;
import java.util.EnumSet;

public class TestCondition {

  static class ToolSet {

    final EnumSet<DexTool> set;

    public ToolSet(EnumSet<DexTool> set) {
      this.set = set;
    }
  }

  static class CompilerSet {

    final EnumSet<CompilerUnderTest> set;

    public CompilerSet(EnumSet<CompilerUnderTest> set) {
      this.set = set;
    }
  }

  static class RuntimeSet {

    final EnumSet<DexVm> set;

    public RuntimeSet(EnumSet<DexVm> set) {
      this.set = set;
    }
  }

  static class CompilationModeSet {

    final EnumSet<CompilationMode> set;

    public CompilationModeSet(EnumSet<CompilationMode> set) {
      this.set = set;
    }
  }

  public static final CompilerSet D8_COMPILER = compilers(CompilerUnderTest.D8);
  // R8_COMPILER refers to R8 both in the standalone setting and after D8
  // R8_NOT_AFTER_D8_COMPILER and R8_AFTER_D8_COMPILER refers to the standalone and the combined
  // settings, respectively
  public static final CompilerSet R8_COMPILER =
      compilers(CompilerUnderTest.R8, CompilerUnderTest.R8_AFTER_D8);
  public static final CompilerSet R8_AFTER_D8_COMPILER = compilers(CompilerUnderTest.R8_AFTER_D8);
  public static final CompilerSet R8_NOT_AFTER_D8_COMPILER = compilers(CompilerUnderTest.R8);

  public static final CompilationModeSet DEBUG_MODE =
      new CompilationModeSet(EnumSet.of(CompilationMode.DEBUG));
  public static final CompilationModeSet RELEASE_MODE =
      new CompilationModeSet(EnumSet.of(CompilationMode.RELEASE));

  private static final ToolSet ANY_TOOL = new ToolSet(EnumSet.allOf(DexTool.class));
  private static final CompilerSet ANY_COMPILER =
      new CompilerSet(EnumSet.allOf(CompilerUnderTest.class));
  private static final RuntimeSet ANY_RUNTIME = new RuntimeSet(EnumSet.allOf(DexVm.class));
  private static final CompilationModeSet ANY_MODE =
      new CompilationModeSet(EnumSet.allOf(CompilationMode.class));

  private final EnumSet<DexTool> dexTools;
  private final EnumSet<CompilerUnderTest> compilers;
  private final EnumSet<DexVm> dexVms;
  private final EnumSet<CompilationMode> compilationModes;

  public TestCondition(
      EnumSet<DexTool> dexTools,
      EnumSet<CompilerUnderTest> compilers,
      EnumSet<DexVm> dexVms,
      EnumSet<CompilationMode> compilationModes) {
    this.dexTools = dexTools;
    this.compilers = compilers;
    this.dexVms = dexVms;
    this.compilationModes = compilationModes;
  }

  public static ToolSet tools(DexTool... tools) {
    assert tools.length > 0;
    return new ToolSet(EnumSet.copyOf(Arrays.asList(tools)));
  }

  public static CompilerSet compilers(CompilerUnderTest... compilers) {
    assert compilers.length > 0;
    return new CompilerSet(EnumSet.copyOf(Arrays.asList(compilers)));
  }

  public static RuntimeSet runtimes(DexVm... runtimes) {
    assert runtimes.length > 0;
    return new RuntimeSet(EnumSet.copyOf(Arrays.asList(runtimes)));
  }

  public static TestCondition match(
      ToolSet tools,
      CompilerSet compilers,
      RuntimeSet runtimes,
      CompilationModeSet compilationModes) {
    return new TestCondition(tools.set, compilers.set, runtimes.set, compilationModes.set);
  }

  public static TestCondition match(ToolSet tools, CompilerSet compilers, RuntimeSet runtimes) {
    return match(tools, compilers, runtimes, TestCondition.ANY_MODE);
  }

  public static TestCondition any() {
    return match(TestCondition.ANY_TOOL, TestCondition.ANY_COMPILER, TestCondition.ANY_RUNTIME);
  }

  public static TestCondition match(ToolSet tools) {
    return match(tools, TestCondition.ANY_COMPILER, TestCondition.ANY_RUNTIME);
  }

  public static TestCondition match(ToolSet tools, CompilerSet compilers) {
    return match(tools, compilers, TestCondition.ANY_RUNTIME);
  }

  public static TestCondition match(ToolSet tools, RuntimeSet runtimes) {
    return match(tools, TestCondition.ANY_COMPILER, runtimes);
  }

  public static TestCondition match(CompilerSet compilers) {
    return match(TestCondition.ANY_TOOL, compilers, TestCondition.ANY_RUNTIME);
  }

  public static TestCondition match(CompilerSet compilers, CompilationModeSet compilationModes) {
    return match(TestCondition.ANY_TOOL, compilers, TestCondition.ANY_RUNTIME, compilationModes);
  }

  public static TestCondition match(CompilerSet compilers, RuntimeSet runtimes) {
    return match(TestCondition.ANY_TOOL, compilers, runtimes);
  }

  public static TestCondition match(RuntimeSet runtimes) {
    return match(TestCondition.ANY_TOOL, TestCondition.ANY_COMPILER, runtimes);
  }

  public boolean test(
      DexTool dexTool,
      CompilerUnderTest compilerUnderTest,
      DexVm dexVm,
      CompilationMode compilationMode) {
    return dexTools.contains(dexTool)
        && compilers.contains(compilerUnderTest)
        && dexVms.contains(dexVm)
        && compilationModes.contains(compilationMode);
  }
}
