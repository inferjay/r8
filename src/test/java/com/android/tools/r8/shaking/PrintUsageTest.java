// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import static com.android.tools.r8.shaking.TreeShakingTest.getTestOptionalParameter;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.R8Command;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.shaking.PrintUsageTest.PrintUsageInspector.ClassSubject;
import com.android.tools.r8.utils.ListUtils;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PrintUsageTest {
  private static final String ANDROID_JAR = ToolHelper.getDefaultAndroidJar();
  private static final String PRINT_USAGE_FILE_SUFFIX = "-print-usage.txt";

  private final String test;
  private final String programFile;
  private final List<String> keepRulesFiles;
  private final Consumer<PrintUsageInspector> inspection;

  @Rule
  public TemporaryFolder temp = ToolHelper.getTemporaryFolderForTest();

  public PrintUsageTest(
      String test,
      List<String> keepRulesFiles,
      Consumer<PrintUsageInspector> inspection) {
    this.test = test;
    this.programFile = ToolHelper.EXAMPLES_BUILD_DIR + test + ".jar";
    this.keepRulesFiles = keepRulesFiles;
    this.inspection = inspection;
  }

  @Before
  public void runR8andGetPrintUsage()
      throws IOException, ProguardRuleParserException, ExecutionException, CompilationException {
    Path out = temp.getRoot().toPath();
    R8Command command =
        R8Command.builder()
            .setOutputPath(out)
            .addProgramFiles(Paths.get(programFile))
            .addProguardConfigurationFiles(ListUtils.map(keepRulesFiles, Paths::get))
            .addProguardConfigurationConsumer(builder -> {
              builder.setPrintUsage(true);
              builder.setPrintUsageFile(out.resolve(test + PRINT_USAGE_FILE_SUFFIX));
            })
            .addLibraryFiles(Paths.get(ANDROID_JAR))
            .build();
    ToolHelper.runR8(command, options -> {
      // Disable inlining to make this test not depend on inlining decisions.
      options.inlineAccessors = false;
    });
  }

  @Test
  public void printUsageTest() throws IOException, ExecutionException {
    Path out = temp.getRoot().toPath();
    Path printUsageFile = out.resolve(test + PRINT_USAGE_FILE_SUFFIX);
    if (inspection != null) {
      PrintUsageInspector inspector = new PrintUsageInspector(printUsageFile);
      inspection.accept(inspector);
    }
  }

  @Parameters(name = "test: {0} keep: {1}")
  public static Collection<Object[]> data() {
    List<String> tests = Arrays.asList(
        "shaking1", "shaking2", "shaking4", "shaking8", "shaking9", "shaking12");

    Map<String, Consumer<PrintUsageInspector>> inspections = new HashMap<>();
    inspections.put("shaking1:keep-rules-printusage.txt", PrintUsageTest::inspectShaking1);
    inspections.put("shaking2:keep-rules-printusage.txt", PrintUsageTest::inspectShaking2);
    inspections.put("shaking4:keep-rules-printusage.txt", PrintUsageTest::inspectShaking4);
    inspections.put("shaking8:keep-rules-printusage.txt", PrintUsageTest::inspectShaking8);
    inspections.put("shaking9:keep-rules-printusage.txt", PrintUsageTest::inspectShaking9);
    inspections.put("shaking12:keep-rules-printusage.txt", PrintUsageTest::inspectShaking12);

    List<Object[]> testCases = new ArrayList<>();
    Set<String> usedInspections = new HashSet<>();
    for (String test : tests) {
      File[] keepFiles = new File(ToolHelper.EXAMPLES_DIR + test)
          .listFiles(file -> file.isFile() && file.getName().endsWith(".txt"));
      for (File keepFile : keepFiles) {
        String keepName = keepFile.getName();
        Consumer<PrintUsageInspector> inspection =
            getTestOptionalParameter(inspections, usedInspections, test, keepName);
        if (inspection != null) {
          testCases.add(new Object[]{test, ImmutableList.of(keepFile.getPath()), inspection});
        }
      }
    }
    assert usedInspections.size() == inspections.size();
    return testCases;
  }

  private static void inspectShaking1(PrintUsageInspector inspector) {
    assertTrue(inspector.clazz("shaking1.Unused").isPresent());
    assertTrue(inspector.clazz("shaking1.Used").isPresent());
    ClassSubject used = inspector.clazz("shaking1.Used").get();
    assertTrue(used.method("void", "<clinit>", ImmutableList.of()));
  }

  private static void inspectShaking2(PrintUsageInspector inspector) {
    Optional<ClassSubject> staticFields = inspector.clazz("shaking2.StaticFields");
    assertTrue(staticFields.isPresent());
    assertTrue(staticFields.get().field("int", "completelyUnused"));
    assertTrue(staticFields.get().field("int", "unused"));
    Optional<ClassSubject> subClass1 = inspector.clazz("shaking2.SubClass1");
    assertTrue(subClass1.isPresent());
    assertTrue(subClass1.get().method("void", "unusedVirtualMethod", Collections.emptyList()));
    Optional<ClassSubject> superClass = inspector.clazz("shaking2.SuperClass");
    assertTrue(superClass.isPresent());
    assertTrue(superClass.get().method("void", "unusedStaticMethod", Collections.emptyList()));
  }

  private static void inspectShaking4(PrintUsageInspector inspector) {
    assertTrue(inspector.clazz("shaking4.Interface").isPresent());
  }

  private static void inspectShaking8(PrintUsageInspector inspector) {
    Optional<ClassSubject> thing = inspector.clazz("shaking8.Thing");
    assertTrue(thing.isPresent());
    assertTrue(thing.get().field("int", "aField"));
    assertFalse(inspector.clazz("shaking8.OtherThing").isPresent());
    assertTrue(inspector.clazz("shaking8.YetAnotherThing").isPresent());
  }

  private static void inspectShaking9(PrintUsageInspector inspector) {
    Optional<ClassSubject> superClass = inspector.clazz("shaking9.Superclass");
    assertFalse(superClass.isPresent());
    Optional<ClassSubject> subClass = inspector.clazz("shaking9.Subclass");
    assertTrue(subClass.isPresent());
    assertTrue(subClass.get().method("void", "aMethod", Collections.emptyList()));
    assertFalse(subClass.get().method("void", "<init>", Collections.emptyList()));
  }

  private static void inspectShaking12(PrintUsageInspector inspector) {
    assertFalse(inspector.clazz("shaking12.PeopleClass").isPresent());
    Optional<ClassSubject> animal = inspector.clazz("shaking12.AnimalClass");
    assertTrue(animal.isPresent());
    assertTrue(animal.get().method("java.lang.String", "getName", Collections.emptyList()));
  }

  static class PrintUsageInspector {
    private Map<String, ClassSubject> printedUsage;

    PrintUsageInspector(Path printUsageFile) throws IOException {
      printedUsage = new HashMap<>();
      try (Stream<String> lines = Files.lines(printUsageFile)) {
        lines.forEach(line -> {
          if (line.startsWith("    ")) {
            if (line.contains("(") && line.contains(")")) {
              readMethod(line);
            } else {
              readField(line);
            }
          } else {
            readClazz(line);
          }
        });
      }
    }

    private ClassSubject lastClazz = null;

    private void readClazz(String line) {
      if (printedUsage.containsKey(line)) {
        lastClazz = printedUsage.get(line);
      } else {
        lastClazz = new ClassSubject();
        printedUsage.put(line, lastClazz);
      }
    }

    private void readMethod(String line) {
      assert lastClazz != null;
      lastClazz.putMethod(line);
    }

    private void readField(String line) {
      assert lastClazz != null;
      lastClazz.putField(line);
    }

    public Optional<ClassSubject> clazz(String name) {
      if (printedUsage.containsKey(name)) {
        return Optional.of(printedUsage.get(name));
      }
      return Optional.empty();
    }

    static class ClassSubject {
      private Set<String> methods;
      private Set<String> fields;

      public ClassSubject() {
        methods = new HashSet<>();
        fields = new HashSet<>();
      }

      void putMethod(String line) {
        String[] tokens = line.split(" ");
        assert tokens.length >= 2;
        methods.add(tokens[tokens.length - 2] + " " + tokens[tokens.length - 1]);
      }

      void putField(String line) {
        String[] tokens = line.split(" ");
        assert tokens.length >= 2;
        fields.add(tokens[tokens.length - 2] + " " + tokens[tokens.length - 1]);
      }

      public boolean method(String returnType, String name, List<String> parameters) {
        StringBuilder builder = new StringBuilder();
        builder.append(returnType).append(" ").append(name);
        builder.append("(");
        for (int i = 0; i < parameters.size(); i++) {
          if (i != 0) {
            builder.append(",");
          }
          builder.append(parameters.get(i));
        }
        builder.append(")");
        return methods.contains(builder.toString());
      }

      public boolean field(String type, String name) {
        return fields.contains(type + " " + name);
      }
    }
  }
}
