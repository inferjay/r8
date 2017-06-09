// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.memberrebinding;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.R8Command;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.dex.Constants;
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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import org.junit.Assert;
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
  private final int minApiLevel;

  @Rule
  public TemporaryFolder temp = ToolHelper.getTemporaryFolderForTest();

  public MemberRebindingTest(TestConfiguration configuration) {
    this.kind = configuration.kind;
    originalDex = configuration.getDexPath();
    if (kind == Frontend.DEX) {
      this.programFile = originalDex;
    } else {
      this.programFile = configuration.getJarPath();
    }
    this.inspection = configuration.processedInspection;
    this.originalInspection = configuration.originalInspection;
    this.minApiLevel = configuration.getMinApiLevel();
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
            .setMinApiLevel(minApiLevel)
            .build(),
        options -> options.inlineAccessors = false);
  }

  private static boolean coolInvokes(InstructionSubject instruction) {
    if (!instruction.isInvokeVirtual() && !instruction.isInvokeInterface() &&
        !instruction.isInvokeStatic()) {
      return false;
    }
    InvokeInstructionSubject invoke = (InvokeInstructionSubject) instruction;
    return !invoke.holder().is("java.io.PrintStream");
  }

  private static void inspectOriginalMain(DexInspector inspector) {
    MethodSubject main = inspector.clazz("memberrebinding.Memberrebinding")
        .method(DexInspector.MAIN);
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
    MethodSubject main = inspector.clazz("memberrebinding.Memberrebinding")
        .method(DexInspector.MAIN);
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
    assertTrue(iterator.next().holder().is("memberrebinding.subpackage.PublicClassInTheMiddle"));
    assertTrue(iterator.next().holder().is("memberrebinding.subpackage.PublicClassInTheMiddle"));
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
    MethodSubject main = inspector.clazz("memberrebinding2.Memberrebinding")
        .method(DexInspector.MAIN);
    Iterator<FieldAccessInstructionSubject> iterator =
        main.iterateInstructions(InstructionSubject::isFieldAccess);
    // Run through instance put, static put, instance get and instance get.
    for (int i = 0; i < 4; i++) {
      assertTrue(iterator.next().holder().is("memberrebinding2.ClassAtBottomOfChain"));
      assertTrue(iterator.next().holder().is("memberrebinding2.ClassAtBottomOfChain"));
      assertTrue(iterator.next().holder().is("memberrebinding2.ClassAtBottomOfChain"));
      assertTrue(iterator.next().holder().is("memberrebinding2.subpackage.PublicClass"));
    }
    assertTrue(iterator.next().holder().is("java.lang.System"));
    assertFalse(iterator.hasNext());
  }

  private static void inspectMain2(DexInspector inspector) {
    MethodSubject main = inspector.clazz("memberrebinding2.Memberrebinding")
        .method(DexInspector.MAIN);
    Iterator<FieldAccessInstructionSubject> iterator =
        main.iterateInstructions(InstructionSubject::isFieldAccess);
    // Run through instance put, static put, instance get and instance get.
    for (int i = 0; i < 4; i++) {
      assertTrue(iterator.next().holder().is("memberrebinding2.ClassAtBottomOfChain"));
      assertTrue(iterator.next().holder().is("memberrebinding2.ClassInMiddleOfChain"));
      assertTrue(iterator.next().holder().is("memberrebinding2.SuperClassOfAll"));
      assertTrue(iterator.next().holder().is("memberrebinding2.subpackage.PublicClass"));
    }
    assertTrue(iterator.next().holder().is("java.lang.System"));
    assertFalse(iterator.hasNext());
  }

  public static MethodSignature TEST =
      new MethodSignature("test", "void", new String[]{});

  private static void inspectOriginal3(DexInspector inspector) {
    MethodSubject main = inspector.clazz("memberrebinding3.Memberrebinding").method(TEST);
    Iterator<InvokeInstructionSubject> iterator =
        main.iterateInstructions(InstructionSubject::isInvoke);
    assertTrue(iterator.next().holder().is("memberrebinding3.ClassAtBottomOfChain"));
    assertTrue(iterator.next().holder().is("memberrebinding3.ClassAtBottomOfChain"));
    assertTrue(iterator.next().holder().is("memberrebinding3.ClassAtBottomOfChain"));
    assertFalse(iterator.hasNext());
  }

  private static void inspect3(DexInspector inspector) {
    MethodSubject main = inspector.clazz("memberrebinding3.Memberrebinding").method(TEST);
    Iterator<InvokeInstructionSubject> iterator =
        main.iterateInstructions(InstructionSubject::isInvoke);
    assertTrue(iterator.next().holder().is("memberrebinding3.ClassAtBottomOfChain"));
    assertTrue(iterator.next().holder().is("memberrebinding3.ClassInMiddleOfChain"));
    assertTrue(iterator.next().holder().is("memberrebinding3.SuperClassOfAll"));
    assertFalse(iterator.hasNext());
  }

  private static void inspectOriginal4(DexInspector inspector) {
    MethodSubject main = inspector.clazz("memberrebinding4.Memberrebinding").method(TEST);
    Iterator<InvokeInstructionSubject> iterator =
        main.iterateInstructions(InstructionSubject::isInvoke);
    assertTrue(iterator.next().holder().is("memberrebinding4.Memberrebinding$Inner"));
    assertTrue(iterator.next().holder().is("memberrebinding4.subpackage.PublicInterface"));
    assertFalse(iterator.hasNext());
  }

  private static void inspect4(DexInspector inspector) {
    MethodSubject main = inspector.clazz("memberrebinding4.Memberrebinding").method(TEST);
    Iterator<InvokeInstructionSubject> iterator =
        main.iterateInstructions(InstructionSubject::isInvoke);
    assertTrue(iterator.next().holder().is("memberrebinding4.Memberrebinding$Inner"));
    assertTrue(iterator.next().holder().is("memberrebinding4.subpackage.PublicInterface"));
    assertFalse(iterator.hasNext());
  }

  private static class TestConfiguration {

    private enum AndroidVersion {
      PRE_N,
      N
    }

    final String name;
    final Frontend kind;
    final AndroidVersion version;
    final Consumer<DexInspector> originalInspection;
    final Consumer<DexInspector> processedInspection;

    private TestConfiguration(String name,
        Frontend kind,
        AndroidVersion version,
        Consumer<DexInspector> originalInspection,
        Consumer<DexInspector> processedInspection) {
      this.name = name;
      this.kind = kind;
      this.version = version;
      this.originalInspection = originalInspection;
      this.processedInspection = processedInspection;
    }

    public static void add(ImmutableList.Builder<TestConfiguration> builder,
        String name,
        AndroidVersion version,
        Consumer<DexInspector> originalInspection,
        Consumer<DexInspector> processedInspection) {
      if (version == AndroidVersion.PRE_N) {
        builder.add(new TestConfiguration(name, Frontend.DEX, version, originalInspection,
            processedInspection));
      }
      builder.add(new TestConfiguration(name, Frontend.JAR, version, originalInspection,
          processedInspection));
    }

    public Path getDexPath() {
      return getBuildPath().resolve(name).resolve("classes.dex");
    }

    public Path getJarPath() {
      return getBuildPath().resolve(name + ".jar");
    }

    public Path getBuildPath() {
      switch (version) {
        case PRE_N:
          return Paths.get(ToolHelper.EXAMPLES_BUILD_DIR);
        case N:
          return Paths.get(ToolHelper.EXAMPLES_ANDROID_N_BUILD_DIR);
        default:
          Assert.fail();
          return null;
      }
    }

    public int getMinApiLevel() {
      switch (version) {
        case PRE_N:
          return Constants.DEFAULT_ANDROID_API;
        case N:
          return Constants.ANDROID_N_API;
        default:
          Assert.fail();
          return -1;
      }
    }

    public String toString() {
      return name + " " + kind;
    }
  }

  @Parameters(name = "{0}")
  public static Collection<TestConfiguration> data() {
    ImmutableList.Builder<TestConfiguration> builder = ImmutableList.builder();
    TestConfiguration.add(builder, "memberrebinding", TestConfiguration.AndroidVersion.PRE_N,
        MemberRebindingTest::inspectOriginalMain, MemberRebindingTest::inspectMain);
    TestConfiguration.add(builder, "memberrebinding2", TestConfiguration.AndroidVersion.PRE_N,
        MemberRebindingTest::inspectOriginalMain2, MemberRebindingTest::inspectMain2);
    TestConfiguration.add(builder, "memberrebinding3", TestConfiguration.AndroidVersion.PRE_N,
        MemberRebindingTest::inspectOriginal3, MemberRebindingTest::inspect3);
    TestConfiguration.add(builder, "memberrebinding4", TestConfiguration.AndroidVersion.N,
        MemberRebindingTest::inspectOriginal4, MemberRebindingTest::inspect4);
    return builder.build();
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
