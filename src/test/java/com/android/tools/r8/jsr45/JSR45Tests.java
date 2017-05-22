// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.jsr45;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.D8;
import com.android.tools.r8.D8Command;
import com.android.tools.r8.R8;
import com.android.tools.r8.R8Command;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.graph.DexAnnotationElement;
import com.android.tools.r8.graph.DexValue.DexValueString;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.DexInspector;
import com.android.tools.r8.utils.DexInspector.AnnotationSubject;
import com.android.tools.r8.utils.DexInspector.ClassSubject;
import com.google.common.io.Closer;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class JSR45Tests {

  private static final String DEFAULT_MAP_FILENAME = "proguard.map";
  private static final Path INPUT_PATH =
      Paths.get("src/test/java/com/android/tools/r8/jsr45/HelloKt.class");
  private static final Path DONT_SHRINK_DONT_OBFUSCATE_CONFIG =
      Paths.get("src/test/java/com/android/tools/r8/jsr45/keep-rules-1.txt");
  private static final Path DONT_SHRINK_CONFIG =
      Paths.get("src/test/java/com/android/tools/r8/jsr45/keep-rules-2.txt");
  private static final Path SHRINK_KEEP_CONFIG =
      Paths.get("src/test/java/com/android/tools/r8/jsr45/keep-rules-3.txt");
  private static final Path SHRINK_NO_KEEP_CONFIG =
      Paths.get("src/test/java/com/android/tools/r8/jsr45/keep-rules-4.txt");

  @Rule
  public TemporaryFolder tmpOutputDir = ToolHelper.getTemporaryFolderForTest();

  void compileWithD8(Path intputPath, Path outputPath) throws IOException, CompilationException {
    D8.run(
        D8Command.builder()
            .setMinApiLevel(Constants.ANDROID_O_API)
            .addProgramFiles(intputPath)
            .setOutputPath(outputPath)
            .build());
  }

  void compileWithR8(Path inputPath, Path outputPath, Path keepRulesPath)
      throws IOException, CompilationException, ExecutionException, ProguardRuleParserException {
    AndroidApp androidApp =
        R8.run(
            R8Command.builder()
                .addProgramFiles(inputPath)
                .addLibraryFiles(Paths.get(ToolHelper.getDefaultAndroidJar()))
                .setOutputPath(outputPath)
                .addProguardConfigurationFiles(keepRulesPath)
                .build());
    if (androidApp.hasProguardMap()) {
      try (Closer closer = Closer.create()) {
        androidApp.writeProguardMap(closer, new FileOutputStream(
            Paths.get(tmpOutputDir.getRoot().getCanonicalPath(), DEFAULT_MAP_FILENAME).toFile()));
      }
    }
  }

  static class ReadSourceDebugExtensionAttribute extends ClassVisitor {

    private ReadSourceDebugExtensionAttribute(int api, ClassVisitor cv) {
      super(api, cv);
    }

    private String debugSourceExtension = null;

    @Override
    public void visitSource(String source, String debug) {
      debugSourceExtension = debug;
      super.visitSource(source, debug);
    }
  }

  @Test
  public void testSourceDebugExtensionWithD8()
      throws IOException, CompilationException, ExecutionException {
    Path outputPath = tmpOutputDir.newFolder().toPath();

    compileWithD8(INPUT_PATH, outputPath);

    checkAnnotationContent(INPUT_PATH, outputPath);
  }

  /**
   * Check that when dontshrink and dontobfuscate is used the annotation is transmitted.
   */
  @Test
  public void testSourceDebugExtensionWithShriking1()
      throws IOException, CompilationException, ExecutionException, ProguardRuleParserException {
    Path outputPath = tmpOutputDir.newFolder().toPath();
    compileWithR8(INPUT_PATH, outputPath, DONT_SHRINK_DONT_OBFUSCATE_CONFIG);
    checkAnnotationContent(INPUT_PATH, outputPath);
  }

  /**
   * Check that when dontshrink is used the annotation is not removed due to obfuscation.
   */
  @Test
  public void testSourceDebugExtensionWithShrinking2()
      throws IOException, CompilationException, ExecutionException, ProguardRuleParserException {
    Path outputPath = tmpOutputDir.newFolder().toPath();
    compileWithR8(INPUT_PATH, outputPath, DONT_SHRINK_CONFIG);
    checkAnnotationContent(INPUT_PATH, outputPath);
  }

  /**
   * Check that the annotation is transmitted when shrinking is enabled with a keepattribute option.
   */
  @Test
  public void testSourceDebugExtensionWithShrinking3()
      throws IOException, CompilationException, ExecutionException, ProguardRuleParserException {
    Path outputPath = tmpOutputDir.newFolder().toPath();

    compileWithR8(INPUT_PATH, outputPath, SHRINK_KEEP_CONFIG);

    checkAnnotationContent(INPUT_PATH, outputPath);
  }

  /**
   * Check that the annotation is removed when shriking is enabled and that there is not
   * keepattributes option.
   */
  @Test
  public void testSourceDebugExtensionWithShrinking4()
      throws IOException, CompilationException, ExecutionException, ProguardRuleParserException {
    Path outputPath = tmpOutputDir.newFolder().toPath();

    compileWithR8(INPUT_PATH, outputPath, SHRINK_NO_KEEP_CONFIG);

    DexInspector dexInspector =
        new DexInspector(outputPath.resolve("classes.dex"), getGeneratedProguardMap());
    ClassSubject classSubject = dexInspector.clazz("HelloKt");
    AnnotationSubject annotationSubject =
        classSubject.annotation("dalvik.annotation.SourceDebugExtension");
    Assert.assertFalse(annotationSubject.isPresent());
  }

  private void checkAnnotationContent(Path inputPath, Path outputPath)
      throws IOException, ExecutionException {
    ClassReader classReader = new ClassReader(new FileInputStream(inputPath.toFile()));
    ReadSourceDebugExtensionAttribute sourceDebugExtensionReader =
        new ReadSourceDebugExtensionAttribute(Opcodes.ASM5, null);
    classReader.accept(sourceDebugExtensionReader, 0);

    DexInspector dexInspector =
        new DexInspector(outputPath.resolve("classes.dex"), getGeneratedProguardMap());
    ClassSubject classSubject = dexInspector.clazz("HelloKt");

    AnnotationSubject annotationSubject =
        classSubject.annotation("dalvik.annotation.SourceDebugExtension");
    Assert.assertTrue(annotationSubject.isPresent());
    DexAnnotationElement[] annotationElement = annotationSubject.getAnnotation().elements;
    Assert.assertNotNull(annotationElement);
    Assert.assertTrue(annotationElement.length == 1);
    Assert.assertEquals("value", annotationElement[0].name.toString());
    Assert.assertTrue(annotationElement[0].value instanceof DexValueString);
    Assert.assertEquals(
        sourceDebugExtensionReader.debugSourceExtension,
        ((DexValueString) annotationElement[0].value).value.toSourceString());
  }

  private String getGeneratedProguardMap() throws IOException {
    Path mapFile = Paths.get(tmpOutputDir.getRoot().getCanonicalPath(), DEFAULT_MAP_FILENAME);
    if (Files.exists(mapFile)) {
      return mapFile.toAbsolutePath().toString();
    }
    return null;
  }
}
