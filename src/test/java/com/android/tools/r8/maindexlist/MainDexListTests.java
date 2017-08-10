// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.maindexlist;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.CompilationMode;
import com.android.tools.r8.D8Command;
import com.android.tools.r8.R8Command;
import com.android.tools.r8.TestBase;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.dex.ApplicationWriter;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.errors.MainDexError;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

  private static Path getManyClassesForceMultiDexAppPath() {
    return generatedApplicationsFolder.getRoot().toPath().resolve("many-classes-stereo-forced.zip");
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
    try {
      verifyMainDexContains(TWO_LARGE_CLASSES, getTwoLargeClassesAppPath(), false);
      fail("Expect to fail, for there are too many classes for the main-dex list.");
    } catch (CompilationError e) {
      // Make sure {@link MonoDexDistributor} was _not_ used.
      assertFalse(e.getMessage().contains("single dex file"));
      // Make sure what exceeds the limit is the number of methods.
      assertTrue(e.getMessage().contains("# methods: "
          + String.valueOf(TWO_LARGE_CLASSES.size() * MAX_METHOD_COUNT)));
    }
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
    try {
      verifyMainDexContains(MANY_CLASSES, getManyClassesMultiDexAppPath(), false);
      fail("Expect to fail, for there are too many classes for the main-dex list.");
    } catch (CompilationError e) {
      // Make sure {@link MonoDexDistributor} was _not_ used.
      assertFalse(e.getMessage().contains("single dex file"));
      // Make sure what exceeds the limit is the number of methods.
      assertTrue(e.getMessage().contains("# methods: "
          + String.valueOf(MANY_CLASSES_COUNT * MANY_CLASSES_MULTI_DEX_METHODS_PER_CLASS)));
    }
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
      DexType type = factory.createType("L" + entry.replace(".class", "") + ";");
      assertTrue(types.contains(type));
      assertSame(type, MainDexList.parse(entry, factory));
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

  private Path runD8WithMainDexList(
      CompilationMode mode, Path input, List<String> mainDexClasses, boolean useFile)
      throws Exception {
    Path testDir = temp.newFolder().toPath();
    Path listFile = testDir.resolve("main-dex-list.txt");
    if (mainDexClasses != null && useFile) {
      FileUtils.writeTextFile(
          listFile,
          mainDexClasses
              .stream()
              .map(clazz -> clazz.replace('.', '/') + ".class")
              .collect(Collectors.toList()));
    }

    D8Command.Builder builder = D8Command.builder()
        .addProgramFiles(input)
        .setMode(mode)
        .setOutputPath(testDir);
    if (mainDexClasses != null) {
      if (useFile) {
        builder.addMainDexListFiles(listFile);
      } else {
        builder.addMainDexClasses(mainDexClasses);
      }
    }
    ToolHelper.runD8(builder.build());
    return testDir;
  }

  private void runDeterministicTest(Path input, List<String> mainDexClasses, boolean allClasses)
      throws Exception {
    // Run test in debug and release mode for minimal vs. non-minimal main-dex.
    for (CompilationMode mode : CompilationMode.values()) {

      // Build with all different main dex lists.
      List<Path> testDirs = new ArrayList<>();
      if (allClasses) {
        // If all classes are passed add a run without a main-dex list as well.
        testDirs.add(runD8WithMainDexList(mode, input, null, true));
      }
      testDirs.add(runD8WithMainDexList(mode, input, mainDexClasses, true));
      testDirs.add(runD8WithMainDexList(mode, input, mainDexClasses, false));
      if (mainDexClasses != null) {
        testDirs.add(runD8WithMainDexList(mode, input, Lists.reverse(mainDexClasses), true));
        testDirs.add(runD8WithMainDexList(mode, input, Lists.reverse(mainDexClasses), false));
      }

      byte[] ref = null;
      byte[] ref2 = null;
      for (Path testDir : testDirs) {
        Path primaryDexFile = testDir.resolve(FileUtils.DEFAULT_DEX_FILENAME);
        Path secondaryDexFile = testDir.resolve("classes2.dex");
        assertTrue(Files.exists(primaryDexFile));
        boolean hasSecondaryDexFile = !allClasses && mode == CompilationMode.DEBUG;
        assertEquals(hasSecondaryDexFile, Files.exists(testDir.resolve(secondaryDexFile)));
        byte[] content = Files.readAllBytes(primaryDexFile);
        if (ref == null) {
          ref = content;
        } else {
          assertArrayEquals(ref, content);
        }
        if (hasSecondaryDexFile) {
          content = Files.readAllBytes(primaryDexFile);
          if (ref2 == null) {
            ref2 = content;
          } else {
            assertArrayEquals(ref2, content);
          }
        }
      }
    }
  }

  @Test
  public void deterministicTest() throws Exception {
    // Synthesize a dex containing a few empty classes including some in the default package.
    // Everything can fit easily in a single dex file.
    ImmutableList<String> classes = new ImmutableList.Builder<String>()
        .add("A")
        .add("B")
        .add("C")
        .add("D")
        .add("E")
        .add("F")
        .add("A1")
        .add("A2")
        .add("A3")
        .add("A4")
        .add("A5")
        .add("maindexlist.A")
        .add("maindexlist.B")
        .add("maindexlist.C")
        .add("maindexlist.D")
        .add("maindexlist.E")
        .add("maindexlist.F")
        .add("maindexlist.A1")
        .add("maindexlist.A2")
        .add("maindexlist.A3")
        .add("maindexlist.A4")
        .add("maindexlist.A5")
        .build();

    JasminBuilder jasminBuilder = new JasminBuilder();
    for (String name : classes) {
      jasminBuilder.addClass(name);
    }
    Path input = temp.newFolder().toPath().resolve("input.zip");
    ToolHelper.runR8(jasminBuilder.build()).writeToZip(input, OutputMode.Indexed);

    // Test with empty main dex list.
    runDeterministicTest(input, null, true);

    // Test with main-dex list with all classes.
    runDeterministicTest(input, classes, true);

    // Test with main-dex list with first and second half of the classes.
    List<List<String>> partitions = Lists.partition(classes, classes.size() / 2);
    runDeterministicTest(input, partitions.get(0), false);
    runDeterministicTest(input, partitions.get(1), false);

    // Test with main-dex list with every second of the classes.
    runDeterministicTest(input,
        IntStream.range(0, classes.size())
            .filter(n -> n % 2 == 0)
            .mapToObj(classes::get)
            .collect(Collectors.toList()), false);
    runDeterministicTest(input,
        IntStream.range(0, classes.size())
            .filter(n -> n % 2 == 1)
            .mapToObj(classes::get)
            .collect(Collectors.toList()), false);
  }

  @Test
  public void checkIntermediateMultiDex() throws Exception {
    // Generates an application with many classes, every even in one package and every odd in
    // another. Add enough methods so the application cannot fit into one dex file.
    // Notice that this one allows multidex while using lower API.
    AndroidApp generated = generateApplication(
        MANY_CLASSES, Constants.ANDROID_K_API, true, MANY_CLASSES_MULTI_DEX_METHODS_PER_CLASS);
    generated.write(getManyClassesForceMultiDexAppPath(), OutputMode.Indexed);
    // Make sure the generated app indeed has multiple dex files.
    assertTrue(generated.getDexProgramResources().size() > 1);
  }

  @Test
  public void testMultiDexFailDueToMinApi() throws Exception {
    // Generates an application with many classes, every even in one package and every odd in
    // another. Add enough methods so the application cannot fit into one dex file.
    // Notice that this one fails due to the min API.
    try {
      generateApplication(
          MANY_CLASSES, Constants.ANDROID_K_API, false, MANY_CLASSES_MULTI_DEX_METHODS_PER_CLASS);
      fail("Expect to fail, for there are many classes while multidex is not enabled.");
    } catch (MainDexError e) {
      // Make sure {@link MonoDexDistributor} was used.
      assertTrue(e.getMessage().contains("single dex file"));
      // Make sure what exceeds the limit is the number of methods.
      assertTrue(e.getMessage().contains("# methods: "
          + String.valueOf(MANY_CLASSES_COUNT * MANY_CLASSES_MULTI_DEX_METHODS_PER_CLASS)));
    }
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

  private enum MultiDexTestMode {
    SINGLE_FILE,
    MULTIPLE_FILES,
    STRINGS,
    FILES_AND_STRINGS
  }

  private void doVerifyMainDexContains(
      List<String> mainDex, Path app, boolean singleDexApp, boolean minimalMainDex,
      MultiDexTestMode testMode)
      throws IOException, CompilationException, ExecutionException, ProguardRuleParserException {
    AndroidApp originalApp = AndroidApp.fromProgramFiles(app);
    DexInspector originalInspector = new DexInspector(originalApp);
    for (String clazz : mainDex) {
      assertTrue("Class " + clazz + " does not exist in input",
          originalInspector.clazz(clazz).isPresent());
    }
    Path outDir = temp.newFolder().toPath();
    R8Command.Builder builder =
        R8Command.builder()
            .addProgramFiles(app)
            .setMode(minimalMainDex && mainDex.size() > 0
                ? CompilationMode.DEBUG : CompilationMode.RELEASE)
            .setOutputPath(outDir)
            .setTreeShaking(false)
            .setMinification(false);

    switch (testMode) {
      case SINGLE_FILE:
        Path mainDexList = temp.newFile().toPath();
        FileUtils.writeTextFile(mainDexList, ListUtils.map(mainDex, MainDexListTests::typeToEntry));
        builder.addMainDexListFiles(mainDexList);
        break;
      case MULTIPLE_FILES: {
        // Partion the main dex list into several files.
        List<List<String>> partitions = Lists.partition(mainDex, Math.max(mainDex.size() / 3, 1));
        List<Path> mainDexListFiles = new ArrayList<>();
        for (List<String> partition : partitions) {
          Path partialMainDexList = temp.newFile().toPath();
          FileUtils.writeTextFile(partialMainDexList,
              ListUtils.map(partition, MainDexListTests::typeToEntry));
          mainDexListFiles.add(partialMainDexList);
        }
        builder.addMainDexListFiles(mainDexListFiles);
        break;
      }
      case STRINGS:
        builder.addMainDexClasses(mainDex);
        break;
      case FILES_AND_STRINGS: {
        // Partion the main dex list add some parts through files and the other parts using strings.
        List<List<String>> partitions = Lists.partition(mainDex, Math.max(mainDex.size() / 3, 1));
        List<Path> mainDexListFiles = new ArrayList<>();
        for (int i = 0; i < partitions.size(); i++) {
          List<String> partition = partitions.get(i);
          if (i % 2 == 0) {
            Path partialMainDexList = temp.newFile().toPath();
            FileUtils.writeTextFile(partialMainDexList,
                ListUtils.map(partition, MainDexListTests::typeToEntry));
            mainDexListFiles.add(partialMainDexList);
          } else {
            builder.addMainDexClasses(mainDex);
          }
        }
        builder.addMainDexListFiles(mainDexListFiles);
        break;
      }
    }

    ToolHelper.runR8(builder.build());
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
    for (MultiDexTestMode multiDexTestMode : MultiDexTestMode.values()) {
      doVerifyMainDexContains(mainDex, app, singleDexApp, false, multiDexTestMode);
      doVerifyMainDexContains(mainDex, app, singleDexApp, true, multiDexTestMode);
    }
  }

  public static AndroidApp generateApplication(List<String> classes, int minApi, int methodCount)
      throws IOException, ExecutionException {
    return generateApplication(classes, minApi, false, methodCount);
  }

  private static AndroidApp generateApplication(
      List<String> classes, int minApi, boolean intermediate, int methodCount)
      throws IOException, ExecutionException {
    Timing timing = new Timing("MainDexListTests");
    InternalOptions options = new InternalOptions();
    options.minApiLevel = minApi;
    options.intermediate = intermediate;
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
    ApplicationWriter writer = new ApplicationWriter(
        application, appInfo, options, null, NamingLens.getIdentityLens(), null);
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
