// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.memberrebinding;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.R8;
import com.android.tools.r8.R8Command;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.naming.MemberNaming.MethodSignature;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.android.tools.r8.utils.DexInspector;
import com.android.tools.r8.utils.DexInspector.FieldAccessInstructionSubject;
import com.android.tools.r8.utils.DexInspector.InstructionSubject;
import com.android.tools.r8.utils.DexInspector.InvokeInstructionSubject;
import com.android.tools.r8.utils.DexInspector.MethodSubject;
import com.android.tools.r8.utils.ListUtils;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class MemberRebindingTest {

  private static final String ANDROID_JAR = ToolHelper.getDefaultAndroidJar();
  private static final List<String> JAR_LIBRARIES = ImmutableList
      .of(ANDROID_JAR, ToolHelper.EXAMPLES_BUILD_DIR + "memberrebindinglib.jar");
  private static final List<String> DEX_LIBRARIES = ImmutableList
      .of(ANDROID_JAR, ToolHelper.EXAMPLES_BUILD_DIR + "memberrebindinglib/classes.dex");

  private enum Frontend {
    DEX, JAR;

    @Override
    public String toString() {
      return this == DEX ? ".dex" : ".jar";
    }
  }

  private final Frontend kind;
  private final Path originalDex;
  private final Path programFile;
  private final Consumer<DexInspector> inspection;
  private final Consumer<DexInspector> originalInspection;

  @Rule
  public TemporaryFolder temp = ToolHelper.getTemporaryFolderForTest();

  public MemberRebindingTest(
      String test, Frontend kind,
      Consumer<DexInspector> inspection,
      Consumer<DexInspector> originalInspection) {
    this.kind = kind;
    originalDex = Paths.get(ToolHelper.EXAMPLES_BUILD_DIR + test + "/classes.dex");
    if (kind == Frontend.DEX) {
      this.programFile = originalDex;
    } else {
      this.programFile = Paths.get(ToolHelper.EXAMPLES_BUILD_DIR + test + ".jar");
    }
    this.inspection = inspection;
    this.originalInspection = originalInspection;
  }

  @Before
  public void runR8()
      throws IOException, ProguardRuleParserException, ExecutionException, CompilationException {
    // Generate R8 processed version without library option.
    String out = temp.getRoot().getCanonicalPath();
    List<String> libs = kind == Frontend.DEX ? DEX_LIBRARIES : JAR_LIBRARIES;
    // NOTE: It is important to turn off inlining to ensure
    // dex inspection of invokes is predictable.
    ToolHelper.runR8(
        R8Command.builder()
            .setOutputPath(Paths.get(out))
            .addProgramFiles(programFile)
            .addLibraryFiles(ListUtils.map(libs, Paths::get))
            .build(),
        options -> options.inlineAccessors = false);
  }

  private static boolean coolInvokes(InstructionSubject instruction) {
    if (!instruction.isInvokeVirtual() && !instruction.isInvokeInterface()) {
      return false;
    }
    InvokeInstructionSubject invoke = (InvokeInstructionSubject) instruction;
    return !invoke.holder().is("java.io.PrintStream");
  }

  private static void inspectOriginalMain(DexInspector inspector) {
    MethodSubject main = inspector.clazz("memberrebinding.Test").method(DexInspector.MAIN);
    Iterator<InvokeInstructionSubject> iterator =
        main.iterateInstructions(MemberRebindingTest::coolInvokes);
    assertTrue(iterator.next().holder().is("memberrebinding.ClassAtBottomOfChain"));
    assertTrue(iterator.next().holder().is("memberrebinding.ClassAtBottomOfChain"));
    assertTrue(iterator.next().holder().is("memberrebinding.ClassAtBottomOfChain"));
    assertTrue(iterator.next().holder().is("memberrebinding.ClassAtBottomOfChain"));
    assertTrue(iterator.next().holder().is("memberrebinding.ClassExtendsLibraryClass"));
    assertTrue(iterator.next().holder().is("memberrebinding.ClassExtendsLibraryClass"));
    assertTrue(iterator.next().holder().is("memberrebinding.ClassExtendsLibraryClass"));
    assertTrue(iterator.next().holder().is("memberrebinding.ClassExtendsLibraryClass"));
    assertTrue(iterator.next().holder().is("memberrebinding.ClassExtendsLibraryClass"));
    assertTrue(iterator.next().holder().is("memberrebinding.subpackage.PublicClass"));
    assertTrue(iterator.next().holder().is("memberrebinding.ClassExtendsOtherLibraryClass"));
    assertTrue(iterator.next().holder().is("memberrebinding.ClassExtendsOtherLibraryClass"));
    assertTrue(iterator.next().holder().is("memberrebinding.ClassExtendsOtherLibraryClass"));
    assertTrue(iterator.next().holder().is("memberrebinding.ClassExtendsOtherLibraryClass"));
    assertTrue(iterator.next().holder().is("memberrebindinglib.AnIndependentInterface"));
    assertTrue(
        iterator.next().holder().is("memberrebinding.SuperClassOfClassExtendsOtherLibraryClass"));
    assertTrue(
        iterator.next().holder().is("memberrebinding.SuperClassOfClassExtendsOtherLibraryClass"));
    assertFalse(iterator.hasNext());
  }

  private static void inspectMain(DexInspector inspector) {
    MethodSubject main = inspector.clazz("memberrebinding.Test").method(DexInspector.MAIN);
    Iterator<InvokeInstructionSubject> iterator =
        main.iterateInstructions(MemberRebindingTest::coolInvokes);
    assertTrue(iterator.next().holder().is("memberrebinding.ClassAtBottomOfChain"));
    assertTrue(iterator.next().holder().is("memberrebinding.ClassAtBottomOfChain"));
    assertTrue(iterator.next().holder().is("memberrebinding.ClassInMiddleOfChain"));
    assertTrue(iterator.next().holder().is("memberrebinding.SuperClassOfAll"));
    assertTrue(iterator.next().holder().is("memberrebinding.ClassExtendsLibraryClass"));
    assertTrue(iterator.next().holder().is("memberrebinding.ClassExtendsLibraryClass"));
    // For the next three - test that we re-bind to library methods (holder is java.util.ArrayList).
    assertTrue(iterator.next().holder().is("java.util.ArrayList"));
    assertTrue(iterator.next().holder().is("java.util.ArrayList"));
    assertTrue(iterator.next().holder().is("java.util.ArrayList"));
    assertTrue(iterator.next().holder().is("memberrebinding.subpackage.PackagePrivateClass"));
    // For the next three - test that we re-bind to the lowest library class.
    assertTrue(iterator.next().holder().is("memberrebindinglib.SubClass"));
    assertTrue(iterator.next().holder().is("memberrebindinglib.SubClass"));
    assertTrue(iterator.next().holder().is("memberrebindinglib.SubClass"));
    // The next one is already precise.
    assertTrue(
        iterator.next().holder().is("memberrebinding.SuperClassOfClassExtendsOtherLibraryClass"));
    // Some dispatches on interfaces.
    assertTrue(iterator.next().holder().is("memberrebindinglib.AnIndependentInterface"));
    assertTrue(iterator.next().holder().is("memberrebindinglib.SubClass"));
    assertTrue(iterator.next().holder().is("memberrebindinglib.ImplementedInProgramClass"));
    assertFalse(iterator.hasNext());
  }

  private static void inspectOriginalMain2(DexInspector inspector) {
    MethodSubject main = inspector.clazz("memberrebinding2.Test").method(DexInspector.MAIN);
    Iterator<FieldAccessInstructionSubject> iterator =
        main.iterateInstructions(InstructionSubject::isFieldAccess);
    // Run through instance put, static put, instance get and instance get.
    for (int i = 0; i < 4; i++) {
      assertTrue(iterator.next().holder().is("memberrebinding2.ClassAtBottomOfChain"));
      assertTrue(iterator.next().holder().is("memberrebinding2.ClassAtBottomOfChain"));
      assertTrue(iterator.next().holder().is("memberrebinding2.ClassAtBottomOfChain"));
      assertTrue(iterator.next().holder().is("memberrebinding2.subpackage.PublicClass"));
    }
  }

  private static void inspectMain2(DexInspector inspector) {
    MethodSubject main = inspector.clazz("memberrebinding2.Test").method(DexInspector.MAIN);
    Iterator<FieldAccessInstructionSubject> iterator =
        main.iterateInstructions(InstructionSubject::isFieldAccess);
    // Run through instance put, static put, instance get and instance get.
    for (int i = 0; i < 4; i++) {
      assertTrue(iterator.next().holder().is("memberrebinding2.ClassAtBottomOfChain"));
      assertTrue(iterator.next().holder().is("memberrebinding2.ClassInMiddleOfChain"));
      assertTrue(iterator.next().holder().is("memberrebinding2.SuperClassOfAll"));
      assertTrue(iterator.next().holder().is("memberrebinding2.subpackage.PackagePrivateClass"));
    }
  }

  public static MethodSignature TEST =
      new MethodSignature("test", "void", new String[]{});

  private static void inspectOriginal3(DexInspector inspector) {
    MethodSubject main = inspector.clazz("memberrebinding3.Test").method(TEST);
    Iterator<InvokeInstructionSubject> iterator =
        main.iterateInstructions(InstructionSubject::isInvoke);
    assertTrue(iterator.next().holder().is("memberrebinding3.ClassAtBottomOfChain"));
    assertTrue(iterator.next().holder().is("memberrebinding3.ClassAtBottomOfChain"));
    assertTrue(iterator.next().holder().is("memberrebinding3.ClassAtBottomOfChain"));
  }

  private static void inspect3(DexInspector inspector) {
    MethodSubject main = inspector.clazz("memberrebinding3.Test").method(TEST);
    Iterator<InvokeInstructionSubject> iterator =
        main.iterateInstructions(InstructionSubject::isInvoke);
    assertTrue(iterator.next().holder().is("memberrebinding3.ClassAtBottomOfChain"));
    assertTrue(iterator.next().holder().is("memberrebinding3.ClassInMiddleOfChain"));
    assertTrue(iterator.next().holder().is("memberrebinding3.SuperClassOfAll"));
  }

  @Parameters(name = "{0}{1}")
  public static Collection<Object[]> data() {
    List<String> tests =
        ImmutableList.of("memberrebinding", "memberrebinding2", "memberrebinding3");

    Map<String, List<Consumer<DexInspector>>> inspections = new HashMap<>();
    inspections.put("memberrebinding", ImmutableList.of(
        MemberRebindingTest::inspectMain,
        MemberRebindingTest::inspectOriginalMain));
    inspections.put("memberrebinding2", ImmutableList.of(
        MemberRebindingTest::inspectMain2,
        MemberRebindingTest::inspectOriginalMain2));
    inspections.put("memberrebinding3", ImmutableList.of(
        MemberRebindingTest::inspect3,
        MemberRebindingTest::inspectOriginal3));

    List<Object[]> testCases = new ArrayList<>();
    Set<String> usedInspections = new HashSet<>();

    for (String test : tests) {
      List<Consumer<DexInspector>> inspection = inspections.get(test);
      assert inspection != null;
      usedInspections.add(test);
      testCases.add(new Object[]{test, Frontend.JAR, inspection.get(0), inspection.get(1)});
      testCases.add(new Object[]{test, Frontend.DEX, inspection.get(0), inspection.get(1)});
    }

    assert usedInspections.size() == inspections.size();

    return testCases;
  }

  @Test
  public void memberRebindingTest() throws IOException, InterruptedException, ExecutionException {
    if (!ToolHelper.artSupported()) {
      return;
    }
    String out = temp.getRoot().getCanonicalPath();
    Path processed = Paths.get(out, "classes.dex");

    if (kind == Frontend.DEX) {
      DexInspector inspector = new DexInspector(originalDex);
      originalInspection.accept(inspector);
    }

    DexInspector inspector = new DexInspector(processed);
    inspection.accept(inspector);

    // We don't run Art, as the test R8RunExamplesTest already does that.
    // ToolHelper.checkArtOutputIdentical(originalDex, processed, mainClass, null);
  }
}
