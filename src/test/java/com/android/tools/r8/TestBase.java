// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8;

import static org.junit.Assert.assertEquals;

import com.android.tools.r8.ToolHelper.ProcessResult;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.FileUtils;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.OutputMode;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public class TestBase {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  /**
   * Write lines of text to a temporary file.
   */
  protected Path writeTextToTempFile(String... lines) throws IOException {
    Path file = temp.newFile().toPath();
    FileUtils.writeTextFile(file, lines);
    return file;
  }

  /**
   * Build an AndroidApp with the specified test classes.
   */
  protected static AndroidApp readClasses(Class... classes) throws IOException {
    AndroidApp.Builder builder = AndroidApp.builder();
    for (Class clazz : classes) {
      builder.addProgramFiles(ToolHelper.getClassFileForTestClass(clazz));
    }
    return builder.build();
  }

  /**
   * Build an AndroidApp with the specified test classes.
   */
  protected static AndroidApp readClasses(List<Class> classes) throws IOException {
    return readClasses(classes.toArray(new Class[classes.size()]));
  }

  /**
   * Create a temporary JAR file containing the specified test classes.
   */
  protected Path jarTestClasses(Class... classes) throws IOException {
    Path jar = File.createTempFile("junit", ".jar", temp.getRoot()).toPath();
    try (JarOutputStream out = new JarOutputStream(new FileOutputStream(jar.toFile()))) {
      for (Class clazz : classes) {
        try (FileInputStream in =
            new FileInputStream(ToolHelper.getClassFileForTestClass(clazz).toFile())) {
          out.putNextEntry(new ZipEntry(clazz.getCanonicalName().replace('.', '/') + ".class"));
          ByteStreams.copy(in, out);
          out.closeEntry();
        }
      }
    }
    return jar;
  }

  /**
   * Create a temporary JAR file containing the specified test classes.
   */
  protected Path jarTestClasses(List<Class> classes) throws IOException {
    return jarTestClasses(classes.toArray(new Class[classes.size()]));
  }

  /**
   * Compile an application with R8.
   */
  protected AndroidApp compileWithR8(Class... classes)
      throws CompilationException, ProguardRuleParserException, ExecutionException, IOException {
    R8Command command = ToolHelper.prepareR8CommandBuilder(readClasses(classes)).build();
    return ToolHelper.runR8(command);
  }

  /**
   * Compile an application with R8.
   */
  protected AndroidApp compileWithR8(List<Class> classes)
      throws CompilationException, ProguardRuleParserException, ExecutionException, IOException {
    R8Command command = ToolHelper.prepareR8CommandBuilder(readClasses(classes)).build();
    return ToolHelper.runR8(command);
  }

  /**
   * Compile an application with R8.
   */
  protected AndroidApp compileWithR8(List<Class> classes, Consumer<InternalOptions> optionsConsumer)
      throws CompilationException, ProguardRuleParserException, ExecutionException, IOException {
    R8Command command = ToolHelper.prepareR8CommandBuilder(readClasses(classes)).build();
    return ToolHelper.runR8(command, optionsConsumer);
  }

  /**
   * Compile an application with R8.
   */
  protected AndroidApp compileWithR8(AndroidApp app, Consumer<InternalOptions> optionsConsumer)
      throws CompilationException, ProguardRuleParserException, ExecutionException, IOException {
    R8Command command = ToolHelper.prepareR8CommandBuilder(app).build();
    return ToolHelper.runR8(command, optionsConsumer);
  }

  /**
   * Compile an application with R8 using the supplied proguard configuration.
   */
  protected AndroidApp compileWithR8(List<Class> classes, String proguardConfig)
      throws CompilationException, ProguardRuleParserException, ExecutionException, IOException {
    return compileWithR8(readClasses(classes), writeTextToTempFile(proguardConfig));
  }

  /**
   * Compile an application with R8 using the supplied proguard configuration.
   */
  protected AndroidApp compileWithR8(
      List<Class> classes, String proguardConfig, Consumer<InternalOptions> optionsConsumer)
      throws CompilationException, ProguardRuleParserException, ExecutionException, IOException {
    return compileWithR8(readClasses(classes), proguardConfig, optionsConsumer);
  }

  /**
   * Compile an application with R8 using the supplied proguard configuration.
   */
  protected AndroidApp compileWithR8(List<Class> classes, Path proguardConfig)
      throws CompilationException, ProguardRuleParserException, ExecutionException, IOException {
    return compileWithR8(readClasses(classes), proguardConfig);
  }

  /**
   * Compile an application with R8 using the supplied proguard configuration.
   */
  protected AndroidApp compileWithR8(AndroidApp app, Path proguardConfig)
      throws CompilationException, ProguardRuleParserException, ExecutionException, IOException {
    R8Command command =
        ToolHelper.prepareR8CommandBuilder(app)
            .addProguardConfigurationFiles(proguardConfig)
            .build();
    return ToolHelper.runR8(command);
  }

  /**
   * Compile an application with R8 using the supplied proguard configuration.
   */
  protected AndroidApp compileWithR8(
      AndroidApp app, String proguardConfig, Consumer<InternalOptions> optionsConsumer)
      throws CompilationException, ProguardRuleParserException, ExecutionException, IOException {
    return compileWithR8(app, writeTextToTempFile(proguardConfig), optionsConsumer);
  }

  /**
   * Compile an application with R8 using the supplied proguard configuration.
   */
  protected AndroidApp compileWithR8(
      AndroidApp app, Path proguardConfig, Consumer<InternalOptions> optionsConsumer)
      throws CompilationException, ProguardRuleParserException, ExecutionException, IOException {
    R8Command command =
        ToolHelper.prepareR8CommandBuilder(app)
            .addProguardConfigurationFiles(proguardConfig)
            .build();
    return ToolHelper.runR8(command, optionsConsumer);
  }

  /**
   * Generate a Proguard configuration for keeping the "public static void main(String[])" method
   * of the specified class.
   */
  public String keepMainProguardConfiguration(Class clazz) {
    return "-keep public class " + clazz.getCanonicalName() + " {\n"
        + "  public static void main(java.lang.String[]);\n"
        + "}\n";
  }

  /**
   * Generate a Proguard configuration for keeping the "public static void main(String[])" method
   * of the specified class and specify if -allowaccessmodification and -dontobfuscate are added
   * as well.
   */
  public String keepMainProguardConfiguration(
      Class clazz, boolean allowaccessmodification, boolean obfuscate) {
    return keepMainProguardConfiguration(clazz)
        + (allowaccessmodification ? "-allowaccessmodification\n" : "")
        + (obfuscate ? "" : "-dontobfuscate\n");
  }

  /**
   * Run application on Art with the specified main class.
   */
  protected String runOnArt(AndroidApp app, Class mainClass) throws IOException {
    Path out = File.createTempFile("junit", ".zip", temp.getRoot()).toPath();
    app.writeToZip(out, OutputMode.Indexed);
    return ToolHelper.runArtNoVerificationErrors(
        ImmutableList.of(out.toString()), mainClass.getCanonicalName(), null);
  }

  /**
   * Run application on Art with the specified main class and provided arguments.
   */
  protected String runOnArt(AndroidApp app, Class mainClass, String... args) throws IOException {
    Path out = File.createTempFile("junit", ".zip", temp.getRoot()).toPath();
    app.writeToZip(out, OutputMode.Indexed);
    return ToolHelper.runArtNoVerificationErrors(
        ImmutableList.of(out.toString()), mainClass.getCanonicalName(),
        builder -> {
          builder.appendArtOption("-ea");
          for (String arg : args) {
            builder.appendProgramArgument(arg);
          }
        });
  }

  /**
   * Run a single class application on Java.
   */
  protected String runOnJava(Class mainClass) throws Exception {
    ProcessResult result = ToolHelper.runJava(mainClass);
    if (result.exitCode != 0) {
      System.out.println("Std out:");
      System.out.println(result.stdout);
      System.out.println("Std err:");
      System.out.println(result.stderr);
      assertEquals(0, result.exitCode);
    }
    return result.stdout;
  }
}
