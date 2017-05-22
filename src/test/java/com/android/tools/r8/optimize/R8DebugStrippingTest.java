// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.optimize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.R8Command;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.graph.DexCode;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.naming.ClassNameMapper;
import com.android.tools.r8.naming.ClassNaming;
import com.android.tools.r8.naming.MemberNaming;
import com.android.tools.r8.naming.MemberNaming.InlineInformation;
import com.android.tools.r8.naming.MemberNaming.MethodSignature;
import com.android.tools.r8.naming.MemberNaming.Signature;
import com.android.tools.r8.naming.ProguardMapReader;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.DexInspector;
import com.android.tools.r8.utils.DexInspector.ClassSubject;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.Closer;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class R8DebugStrippingTest {

  private static final String ROOT = ToolHelper.EXAMPLES_BUILD_DIR;
  private static final String EXAMPLE_DEX = "throwing/classes.dex";
  private static final String EXAMPLE_MAP = "throwing/throwing.map";
  private static final String EXAMPLE_CLASS = "throwing.Throwing";
  private static final String EXAMPLE_JAVA = "Throwing.java";

  private static final String MAIN_NAME = "main";
  private static final String[] MAIN_PARAMETERS = new String[]{"java.lang.String[]"};
  private static final String VOID_RETURN = "void";

  private static final String OTHER_NAME = "throwInAFunctionThatIsNotInlinedAndCalledTwice";
  private static final String[] NO_PARAMETERS = new String[0];
  private static final String INT_RETURN = "int";

  private static final String THIRD_NAME = "aFunctionThatCallsAnInlinedMethodThatThrows";
  private static final String[] LIST_PARAMETER = new String[]{"java.util.List"};

  private static final String FORTH_NAME = "anotherFunctionThatCallsAnInlinedMethodThatThrows";
  private static final String[] STRING_PARAMETER = new String[]{"java.lang.String"};

  private static final Map<String, Signature> SIGNATURE_MAP = ImmutableMap.of(
      MAIN_NAME, new MethodSignature(MAIN_NAME, VOID_RETURN, MAIN_PARAMETERS),
      OTHER_NAME, new MethodSignature(OTHER_NAME, INT_RETURN, NO_PARAMETERS),
      THIRD_NAME, new MethodSignature(THIRD_NAME, INT_RETURN, LIST_PARAMETER),
      FORTH_NAME, new MethodSignature(FORTH_NAME, INT_RETURN, STRING_PARAMETER)
  );

  private ClassNameMapper mapper;

  @Parameter(0)
  public boolean compressRanges;

  @Rule
  public TemporaryFolder temp = ToolHelper.getTemporaryFolderForTest();

  @Before
  public void loadRangeInformation() throws IOException {
    mapper = ProguardMapReader.mapperFromFile(Paths.get(ROOT, EXAMPLE_MAP));
  }

  @Parameters(name = "compressLineNumers={0}")
  public static Object[] parameters() {
    return new Object[]{true, false};
  }

  @Test
  public void testStackTraces()
      throws IOException, ProguardRuleParserException, ExecutionException, CompilationException {

    // Temporary directory for R8 output.
    Path out = temp.getRoot().toPath();

    R8Command command =
        R8Command.builder()
            .addProgramFiles(Paths.get(ROOT, EXAMPLE_DEX))
            .setOutputPath(out)
            .setProguardMapFile(Paths.get(ROOT, EXAMPLE_MAP))
            .build();

    // Generate R8 processed version.
    AndroidApp result =
        ToolHelper.runR8(command, (options) -> options.skipDebugLineNumberOpt = !compressRanges);

    ClassNameMapper classNameMapper;
    try (Closer closer = Closer.create()) {
      classNameMapper = ProguardMapReader.mapperFromInputStream(result.getProguardMap(closer));
    }
    if (compressRanges) {
      classNameMapper.forAllClassNamings(this::ensureRangesAreUniquePerClass);
    }

    if (!ToolHelper.artSupported()) {
      return;
    }
    // Run art on original.
    String originalOutput =
        ToolHelper.runArtNoVerificationErrors(ROOT + EXAMPLE_DEX, EXAMPLE_CLASS);
    // Run art on R8 processed version.
    String otherOutput =
        ToolHelper.runArtNoVerificationErrors(out + "/classes.dex", EXAMPLE_CLASS);
    // Check that exceptions are in same range
    assertStacktracesMatchRanges(originalOutput, otherOutput, classNameMapper);

    // Check that we have debug information in all the places required.
    DexInspector inspector = new DexInspector(out.resolve("classes.dex"));
    BiMap<String, String> obfuscationMap
        = classNameMapper.getObfuscatedToOriginalMapping().inverse();
    ClassSubject overloaded = inspector.clazz(obfuscationMap.get("throwing.Overloaded"));
    assertTrue(overloaded.isPresent());
    ensureDebugInfosExist(overloaded);
  }

  private void ensureDebugInfosExist(ClassSubject overloaded) {
    final Map<DexString, Boolean> hasDebugInfo = Maps.newIdentityHashMap();
    overloaded.forAllMethods(method -> {
          if (!method.isAbstract()) {
            DexCode code = method.getMethod().getCode().asDexCode();
            DexString name = method.getMethod().method.name;
            Boolean previous = hasDebugInfo.get(name);
            boolean current = code.getDebugInfo() != null;
            // If we have seen one before, it should be the same as now.
            assertTrue(previous == null || (previous == current));
            hasDebugInfo.put(name, current);
          }
        }
    );
  }

  private void ensureRangesAreUniquePerClass(ClassNaming naming) {
    final Map<String, Set<Integer>> rangeMap = new HashMap<>();
    naming.forAllMemberNaming(memberNaming -> {
      if (memberNaming.isMethodNaming()) {
        if (memberNaming.topLevelRange != MemberNaming.fakeZeroRange) {
          int startLine = memberNaming.topLevelRange.from;
          Set<Integer> used = rangeMap
              .computeIfAbsent(memberNaming.getRenamedName(), any -> new HashSet<>());
          assertFalse(used.contains(startLine));
          used.add(startLine);
        }
      }
    });
  }

  private String extractRangeIndex(String line, ClassNameMapper mapper) {
    int position = line.lastIndexOf(EXAMPLE_JAVA);
    assertNotSame("Malformed stackframe: " + line, -1, position);
    String numberPart = line.substring(position + EXAMPLE_JAVA.length() + 1, line.lastIndexOf(')'));
    int number = Integer.parseInt(numberPart);
    // Search the signature map for all signatures that actually match. We do this by first looking
    // up the renamed signature and then checking whether it is contained in the line. We prepend
    // the class name to make sure we do not match random characters in the line.
    for (Entry<String, Signature> entry : SIGNATURE_MAP.entrySet()) {
      MemberNaming naming = mapper.getClassNaming(EXAMPLE_CLASS)
          .lookupByOriginalSignature(entry.getValue());
      if (!line.contains(EXAMPLE_CLASS + "." + naming.getRenamedName())) {
        continue;
      }
      if (naming.topLevelRange.contains(number)) {
        return entry.getKey() + ":" + 0;
      }
      int rangeNo = 1;
      for (InlineInformation inlineInformation : naming.inlineInformation) {
        if (inlineInformation.inlinedRange.contains(number)) {
          return entry.getKey() + ":" + rangeNo;
        }
        rangeNo++;
      }
    }
    fail("Number not in any range " + number);
    return null;
  }

  private MemberNaming selectRanges(String line, ClassNameMapper mapper) {
    Signature signature;
    for (Entry<String, Signature> entry : SIGNATURE_MAP.entrySet()) {
      if (line.contains(entry.getKey())) {
        return mapper.getClassNaming(EXAMPLE_CLASS).lookup(entry.getValue());
      }
    }
    Assert.fail("unknown method in line " + line);
    return null;
  }

  private void assertStacktracesMatchRanges(String before, String after,
      ClassNameMapper newMapper) {
    String[] beforeLines = before.split("\n");
    String[] afterLines = after.split("\n");
    assertEquals("Output length differs", beforeLines.length,
        afterLines.length);
    for (int i = 0; i < beforeLines.length; i++) {
      if (!beforeLines[i].startsWith("FRAME:")) {
        continue;
      }
      String beforeLine = beforeLines[i];
      String expected = extractRangeIndex(beforeLine, mapper);
      String afterLine = afterLines[i];
      String generated = extractRangeIndex(afterLine, newMapper);
      assertEquals("Ranges match", expected, generated);
    }
  }
}
