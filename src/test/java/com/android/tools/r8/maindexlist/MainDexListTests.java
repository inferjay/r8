// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.maindexlist;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.D8Command;
import com.android.tools.r8.R8Command;
import com.android.tools.r8.TestBase;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.dex.ApplicationWriter;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.Code;
import com.android.tools.r8.graph.DebugLocalInfo;
import com.android.tools.r8.graph.DexAccessFlags;
import com.android.tools.r8.graph.DexAnnotationSet;
import com.android.tools.r8.graph.DexAnnotationSetRefList;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.DexTypeList;
import com.android.tools.r8.ir.code.CatchHandlers;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.conversion.IRBuilder;
import com.android.tools.r8.ir.conversion.SourceCode;
import com.android.tools.r8.ir.regalloc.LinearScanRegisterAllocator;
import com.android.tools.r8.ir.regalloc.RegisterAllocator;
import com.android.tools.r8.ir.synthetic.SynthesizedCode;
import com.android.tools.r8.jasmin.JasminBuilder;
import com.android.tools.r8.naming.NamingLens;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.DescriptorUtils;
import com.android.tools.r8.utils.DexInspector;
import com.android.tools.r8.utils.DexInspector.FoundClassSubject;
import com.android.tools.r8.utils.FileUtils;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.ListUtils;
import com.android.tools.r8.utils.MainDexList;
import com.android.tools.r8.utils.OutputMode;
import com.android.tools.r8.utils.ThreadUtils;
import com.android.tools.r8.utils.Timing;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class MainDexListTests extends TestBase {

  private static final int MAX_METHOD_COUNT = Constants.U16BIT_MAX;

  private static final List<String> TWO_LARGE_CLASSES = ImmutableList.of("A", "B");
  private static final int MANY_CLASSES_COUNT = 10000;
  private static final int MANY_CLASSES_SINGLE_DEX_METHODS_PER_CLASS = 2;
  private static final int MANY_CLASSES_MULTI_DEX_METHODS_PER_CLASS = 10;
  private static List<String> MANY_CLASSES;

  @ClassRule
  public static TemporaryFolder generatedApplicationsFolder = new TemporaryFolder();

  // Generate the test applications in a @BeforeClass method, as they are used by several tests.
  @BeforeClass
  public static void generateTestApplications() throws Throwable {
    ImmutableList.Builder<String> builder = ImmutableList.builder();
    for (int i = 0; i < MANY_CLASSES_COUNT; ++i) {
      String pkg = i % 2 == 0 ? "a" : "b";
      builder.add(pkg + ".Class" + i);
    }
    MANY_CLASSES = builder.build();

    // Generates an application with many classes, every even in one package and every odd in
    // another. Keep the number of methods low enough for single dex application.
    AndroidApp generated = generateApplication(
        MANY_CLASSES, Constants.DEFAULT_ANDROID_API, MANY_CLASSES_SINGLE_DEX_METHODS_PER_CLASS);
    generated.write(getManyClassesSingleDexAppPath(), OutputMode.Indexed);

    // Generates an application with many classes, every even in one package and every odd in
    // another. Add enough methods so the application cannot fit into one dex file.
    generated = generateApplication(
        MANY_CLASSES, Constants.ANDROID_L_API, MANY_CLASSES_MULTI_DEX_METHODS_PER_CLASS);
    generated.write(getManyClassesMultiDexAppPath(), OutputMode.Indexed);

    // Generates an application with two classes, each with the maximum possible number of methods.
    generated = generateApplication(TWO_LARGE_CLASSES, Constants.ANDROID_N_API, MAX_METHOD_COUNT);
    generated.write(getTwoLargeClassesAppPath(), OutputMode.Indexed);
  }

  private static Path getTwoLargeClassesAppPath() {
    return generatedApplicationsFolder.getRoot().toPath().resolve("two-large-classes.zip");
  }

  private static Path getManyClassesSingleDexAppPath() {
    return generatedApplicationsFolder.getRoot().toPath().resolve("many-classes-mono.zip");
  }

  private static Path getManyClassesMultiDexAppPath() {
    return generatedApplicationsFolder.getRoot().toPath().resolve("many-classes-stereo.zip");
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void checkGeneratedFileFitInSingleDexFile() {
    assertTrue(MANY_CLASSES_COUNT * MANY_CLASSES_SINGLE_DEX_METHODS_PER_CLASS <= MAX_METHOD_COUNT);
  }

  @Test
  public void checkGeneratedFileNeedsTwoDexFiles() {
    assertTrue(MANY_CLASSES_COUNT * MANY_CLASSES_MULTI_DEX_METHODS_PER_CLASS > MAX_METHOD_COUNT);
  }

  @Test
  public void putFirstClassInMainDexList() throws Throwable {
    verifyMainDexContains(TWO_LARGE_CLASSES.subList(0, 1), getTwoLargeClassesAppPath(), false);
  }

  @Test
  public void putSecondClassInMainDexList() throws Throwable {
    verifyMainDexContains(TWO_LARGE_CLASSES.subList(1, 2), getTwoLargeClassesAppPath(), false);
  }

  @Test
  public void cannotFitBothIntoMainDex() throws Throwable {
    thrown.expect(CompilationError.class);
    verifyMainDexContains(TWO_LARGE_CLASSES, getTwoLargeClassesAppPath(), false);
  }

  @Test
  public void everySecondClassInMainDex() throws Throwable {
    ImmutableList.Builder<String> mainDexBuilder = ImmutableList.builder();
    for (int i = 0; i < MANY_CLASSES.size(); i++) {
      String clazz = MANY_CLASSES.get(i);
      if (i % 3 == 0) {
        mainDexBuilder.add(clazz);
      }
    }
    verifyMainDexContains(mainDexBuilder.build(), getManyClassesSingleDexAppPath(), true);
    verifyMainDexContains(mainDexBuilder.build(), getManyClassesMultiDexAppPath(), false);
  }

  @Test
  public void singleClassInMainDex() throws Throwable {
    ImmutableList<String> mainDex = ImmutableList.of(MANY_CLASSES.get(0));
    verifyMainDexContains(mainDex, getManyClassesSingleDexAppPath(), true);
    verifyMainDexContains(mainDex, getManyClassesMultiDexAppPath(), false);
  }

  @Test
  public void allClassesInMainDex() throws Throwable {
    // Degenerated case with an app thats fit into a single dex, and where the main dex list
    // contains all classes.
    verifyMainDexContains(MANY_CLASSES, getManyClassesSingleDexAppPath(), true);
  }

  @Test
  public void cannotFitAllIntoMainDex() throws Throwable {
    thrown.expect(CompilationError.class);
    verifyMainDexContains(MANY_CLASSES, getManyClassesMultiDexAppPath(), false);
  }

  @Test
  public void validEntries() throws IOException {
    List<String> list = ImmutableList.of(
        "A.class",
        "a/b/c/D.class",
        "a/b/c/D$E.class"
    );
    DexItemFactory factory = new DexItemFactory();
    Path mainDexList = temp.getRoot().toPath().resolve("valid.txt");
    FileUtils.writeTextFile(mainDexList, list);
    Set<DexType> types = MainDexList.parse(mainDexList, factory);
    for (String entry : list) {
      assertTrue(types.contains(factory.createType("L" + entry.replace(".class", "") + ";")));
    }
  }

  @Test
  public void invalidQualifiedEntry() throws IOException {
    thrown.expect(CompilationError.class);
    DexItemFactory factory = new DexItemFactory();
    Path mainDexList = temp.getRoot().toPath().resolve("invalid.txt");
    FileUtils.writeTextFile(mainDexList, ImmutableList.of("a.b.c.D.class"));
    MainDexList.parse(mainDexList, factory);
  }

  @Test
  public void checkDeterminism() throws Exception {
    // Synthesize a dex containing a few empty classes including some in the default package.
    // Everything can fit easaly in a single dex file.
    String[] classes = {
        "A",
        "B",
        "C",
        "D",
        "E",
        "F",
        "A1",
        "A2",
        "A3",
        "A4",
        "A5",
        "maindexlist/A",
        "maindexlist/B",
        "maindexlist/C",
        "maindexlist/D",
        "maindexlist/E",
        "maindexlist/F",
        "maindexlist/A1",
        "maindexlist/A2",
        "maindexlist/A3",
        "maindexlist/A4",
        "maindexlist/A5"
    };
    JasminBuilder jasminBuilder = new JasminBuilder();
    for (String name : classes) {
      jasminBuilder.addClass(name);
    }
    Path input = temp.newFolder().toPath().resolve("input.zip");
    ToolHelper.runR8(jasminBuilder.build()).writeToZip(input, OutputMode.Indexed);

    // Prepare different main dex lists.
    ArrayList<Path> mainLists = new ArrayList<>();
    // Lets first without a main dex list.
    mainLists.add(null);

    // List with all classes.
    List<String> mainList = new ArrayList<>();
    for (int i = 0; i < classes.length; i++) {
      mainList.add(classes[i] + ".class");
    }
    addMainListFile(mainLists, mainList);

    // Full list in reverse order
    addMainListFile(mainLists, Lists.reverse(mainList));

    // Partial list without first entries (those in default package).
    mainList.clear();
    for (int i = classes.length / 2; i < classes.length; i++) {
      mainList.add(classes[i] + ".class");
    }
    addMainListFile(mainLists, mainList);

    // Same in reverese order
    addMainListFile(mainLists, Lists.reverse(mainList));

    // Mixed partial list.
    mainList.clear();
    for (int i = 0; i < classes.length; i += 2) {
      mainList.add(classes[i] + ".class");
    }
    addMainListFile(mainLists, mainList);

    // Another different mixed partial list.
    mainList.clear();
    for (int i = 1; i < classes.length; i += 2) {
      mainList.add(classes[i] + ".class");
    }
    addMainListFile(mainLists, mainList);

    // Build with all main dex lists.
    Path tmp = temp.getRoot().toPath();
    for (int i = 0; i < mainLists.size(); i++) {
      Path out = tmp.resolve(String.valueOf(i));
      Files.createDirectories(out);
      D8Command.Builder builder = D8Command.builder()
          .addProgramFiles(input)
          .setOutputPath(out);
      if (mainLists.get(i) != null) {
        builder.setMainDexListFile(mainLists.get(i));
      }
      ToolHelper.runD8(builder.build());
    }

    // Check: no secondary dex and resulting dex is always the same.
    assertFalse(Files.exists(tmp.resolve(String.valueOf(0)).resolve("classes2.dex")));
    byte[] ref = Files.readAllBytes(
        tmp.resolve(String.valueOf(0)).resolve(FileUtils.DEFAULT_DEX_FILENAME));
    for (int i = 1; i < mainLists.size(); i++) {
      assertFalse(Files.exists(tmp.resolve(String.valueOf(i)).resolve("classes2.dex")));
      byte[] checked = Files.readAllBytes(
          tmp.resolve(String.valueOf(i)).resolve(FileUtils.DEFAULT_DEX_FILENAME));
      assertArrayEquals(ref, checked);
    }
  }

  private void addMainListFile(ArrayList<Path> mainLists, List<String> content)
      throws IOException {
    Path listFile = temp.newFile().toPath();
    FileUtils.writeTextFile(listFile, content);
    mainLists.add(listFile);
  }

  private static String typeToEntry(String type) {
    return type.replace(".", "/") + FileUtils.CLASS_EXTENSION;
  }

  private void failedToFindClassInExpectedFile(Path outDir, String clazz) throws IOException {
    Files.list(outDir)
        .filter(FileUtils::isDexFile)
        .forEach(
            p -> {
              try {
                DexInspector i = new DexInspector(AndroidApp.fromProgramFiles(p));
                assertFalse("Found " + clazz + " in file " + p, i.clazz(clazz).isPresent());
              } catch (IOException | ExecutionException e) {
                e.printStackTrace();
              }
            });
    fail("Failed to find class " + clazz + "in any file...");
  }

  private void assertMainDexClass(FoundClassSubject clazz, List<String> mainDex) {
    if (!mainDex.contains(clazz.toString())) {
      StringBuilder builder = new StringBuilder();
      for (int i = 0; i < mainDex.size(); i++) {
        builder.append(i == 0 ? "[" : ", ");
        builder.append(mainDex.get(i));
      }
      builder.append("]");
      fail("Class " + clazz + " found in main dex, " +
          "only expected explicit main dex classes " + builder +" in main dex file");
    }
  }

  private void doVerifyMainDexContains(
      List<String> mainDex, Path app, boolean singleDexApp, boolean minimalMainDex)
      throws IOException, CompilationException, ExecutionException, ProguardRuleParserException {
    AndroidApp originalApp = AndroidApp.fromProgramFiles(app);
    DexInspector originalInspector = new DexInspector(originalApp);
    for (String clazz : mainDex) {
      assertTrue("Class " + clazz + " does not exist in input",
          originalInspector.clazz(clazz).isPresent());
    }
    Path outDir = temp.newFolder().toPath();
    Path mainDexList = temp.newFile().toPath();
    FileUtils.writeTextFile(mainDexList, ListUtils.map(mainDex, MainDexListTests::typeToEntry));
    R8Command command =
        R8Command.builder()
            .addProgramFiles(app)
            .setMainDexListFile(mainDexList)
            .setMinimalMainDex(minimalMainDex)
            .setOutputPath(outDir)
            .setTreeShaking(false)
            .setMinification(false)
            .build();
    ToolHelper.runR8(command);
    if (!singleDexApp && !minimalMainDex) {
      assertTrue("Output run only produced one dex file.",
          1 < Files.list(outDir).filter(FileUtils::isDexFile).count());
    }
    DexInspector inspector =
        new DexInspector(AndroidApp.fromProgramFiles(outDir.resolve("classes.dex")));
    for (String clazz : mainDex) {
      if (!inspector.clazz(clazz).isPresent()) {
        failedToFindClassInExpectedFile(outDir, clazz);
      }
    }
    if (minimalMainDex) {
      inspector.forAllClasses(clazz -> assertMainDexClass(clazz, mainDex));
    }
  }

  private void verifyMainDexContains(List<String> mainDex, Path app, boolean singleDexApp)
      throws Throwable {
    doVerifyMainDexContains(mainDex, app, singleDexApp, false);
    doVerifyMainDexContains(mainDex, app, singleDexApp, true);
  }

  public static AndroidApp generateApplication(List<String> classes, int minApi, int methodCount)
      throws IOException, ExecutionException {
    Timing timing = new Timing("MainDexListTests");
    InternalOptions options = new InternalOptions();
    options.minApiLevel = minApi;
    DexItemFactory factory = options.itemFactory;
    DexApplication.Builder builder = new DexApplication.Builder(factory, timing);
    for (String clazz : classes) {
      DexString desc = factory.createString(DescriptorUtils.javaTypeToDescriptor(clazz));
      DexType type = factory.createType(desc);
      DexEncodedMethod[] directMethods = new DexEncodedMethod[methodCount];
      for (int i = 0; i < methodCount; i++) {
        DexAccessFlags access = new DexAccessFlags();
        access.setPublic();
        access.setStatic();
        Code code = new SynthesizedCode(new ReturnVoidCode());
        DexEncodedMethod method =
            new DexEncodedMethod(
                factory.createMethod(
                    desc,
                    factory.createString("method" + i),
                    factory.voidDescriptor,
                    DexString.EMPTY_ARRAY),
                access,
                DexAnnotationSet.empty(),
                DexAnnotationSetRefList.empty(),
                code);
        IRCode ir = code.buildIR(method, options);
        RegisterAllocator allocator = new LinearScanRegisterAllocator(ir, options);
        method.setCode(ir, allocator, factory);
        directMethods[i] = method;
      }
      builder.addProgramClass(
          new DexProgramClass(
              type,
              null,
              new DexAccessFlags(),
              factory.objectType,
              DexTypeList.empty(),
              null,
              DexAnnotationSet.empty(),
              DexEncodedField.EMPTY_ARRAY,
              DexEncodedField.EMPTY_ARRAY,
              directMethods,
              DexEncodedMethod.EMPTY_ARRAY));
    }
    DexApplication application = builder.build();
    AppInfoWithSubtyping appInfo = new AppInfoWithSubtyping(application);
    ApplicationWriter writer =
        new ApplicationWriter(application, appInfo, options, NamingLens.getIdentityLens(), null);
    ExecutorService executor = ThreadUtils.getExecutorService(options);
    try {
      return writer.write(null, executor);
    } finally {
      executor.shutdown();
    }
  }

  // Code stub to generate methods with "return-void" bodies.
  private static class ReturnVoidCode implements SourceCode {

    @Override
    public int instructionCount() {
      return 1;
    }

    @Override
    public int instructionIndex(int instructionOffset) {
      return instructionOffset;
    }

    @Override
    public int instructionOffset(int instructionIndex) {
      return instructionIndex;
    }

    @Override
    public boolean needsPrelude() {
      return false;
    }

    @Override
    public DebugLocalInfo getCurrentLocal(int register) {
      return null;
    }

    @Override
    public int traceInstruction(int instructionIndex, IRBuilder builder) {
      return instructionIndex;
    }

    @Override
    public void closedCurrentBlockWithFallthrough(int fallthroughInstructionIndex) {
      throw new Unreachable();
    }

    @Override
    public void closedCurrentBlock() {
      // Intentionally empty.
    }

    @Override
    public void setUp() {
      // Intentionally empty.
    }

    @Override
    public void clear() {
      // Intentionally empty.
    }

    @Override
    public void buildPrelude(IRBuilder builder) {
      // Intentionally empty.
    }

    @Override
    public void buildInstruction(IRBuilder builder, int instructionIndex) {
      assert instructionIndex == 0;
      builder.addReturn();
    }

    @Override
    public void buildPostlude(IRBuilder builder) {
      // Intentionally empty.
    }

    @Override
    public void resolveAndBuildSwitch(
        int value, int fallthroughOffset, int payloadOffset, IRBuilder builder) {
      throw new Unreachable();
    }

    @Override
    public void resolveAndBuildNewArrayFilledData(
        int arrayRef, int payloadOffset, IRBuilder builder) {
      throw new Unreachable();
    }

    @Override
    public CatchHandlers<Integer> getCurrentCatchHandlers() {
      return null;
    }

    @Override
    public boolean verifyRegister(int register) {
      throw new Unreachable();
    }

    @Override
    public boolean verifyCurrentInstructionCanThrow() {
      throw new Unreachable();
    }

    @Override
    public boolean verifyLocalInScope(DebugLocalInfo local) {
      throw new Unreachable();
    }
  }
}
