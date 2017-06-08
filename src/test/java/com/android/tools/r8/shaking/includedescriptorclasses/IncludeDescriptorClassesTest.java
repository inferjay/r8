// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.shaking.includedescriptorclasses;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.android.tools.r8.TestBase;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.utils.DexInspector;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.Test;

public class IncludeDescriptorClassesTest extends TestBase {

  private static String PROGUARD = "third_party/proguard/proguard5.2.1/bin/proguard.sh";

  private Path runProguard(Path inJar, Path config) throws IOException {
    Path outJar = File.createTempFile("junit", ".jar", temp.getRoot()).toPath();
    List<String> command = new ArrayList<>();
    command.add(PROGUARD);
    command.add("-forceprocessing");  // Proguard just checks the creation time on the in/out jars.
    command.add("-injars");
    command.add(inJar.toString());
    command.add("-libraryjars");
    command.add(ToolHelper.getDefaultAndroidJar());
    command.add("@" + config);
    command.add("-outjar");
    command.add(outJar.toString());
    command.add("-printmapping");
    ProcessBuilder builder = new ProcessBuilder(command);
    ToolHelper.ProcessResult result = ToolHelper.runProcess(builder);
    if (result.exitCode != 0) {
      fail("Proguard failed, exit code " + result.exitCode + ", stderr:\n" + result.stderr);
    }
    return outJar;
  }

  private Set<String> readJarClasses(Path jar) throws IOException {
    Set<String> result = new HashSet<>();
    try (ZipInputStream in = new ZipInputStream(new FileInputStream(jar.toFile()))) {
      ZipEntry entry = in.getNextEntry();
      while (entry != null) {
        String name = entry.getName();
        if (name.endsWith(".class")) {
          result.add(name.substring(0, name.length() - ".class".length()).replace('/', '.'));
        }
        entry = in.getNextEntry();
      }
    }
    return result;
  }

  private class Result {
    final DexInspector inspector;
    final Set<String> classesAfterProguard;

    Result(DexInspector inspector, Set<String> classesAfterProguard) {
      this.inspector = inspector;
      this.classesAfterProguard = classesAfterProguard;
    }

    void assertKept(Class clazz) {
      assertTrue(inspector.clazz(clazz.getCanonicalName()).isPresent());
      assertFalse(inspector.clazz(clazz.getCanonicalName()).isRenamed());
      if (classesAfterProguard != null) {
        assertTrue(classesAfterProguard.contains(clazz.getCanonicalName()));
      }
    }

    void assertRemoved(Class clazz) {
      assertFalse(inspector.clazz(clazz.getCanonicalName()).isPresent());
      // TODO(sgjesse): Also check that it was not just renamed...
      if (classesAfterProguard != null) {
        assertFalse(classesAfterProguard.contains(clazz.getCanonicalName()));
      }
    }

    void assertRenamed(Class clazz) {
      assertTrue(inspector.clazz(clazz.getCanonicalName()).isPresent());
      assertTrue(inspector.clazz(clazz.getCanonicalName()).isRenamed());
      // TODO(sgjesse): Also check that it was actually renamed...
      if (classesAfterProguard != null) {
        assertFalse(classesAfterProguard.contains(clazz.getCanonicalName()));
      }
    }

  }

  private List<Class> applicationClasses = ImmutableList.of(
      ClassWithNativeMethods.class, NativeArgumentType.class, NativeReturnType.class,
      StaticFieldType.class, InstanceFieldType.class);
  private List<Class> mainClasses = ImmutableList.of(
      MainCallMethod1.class, MainCallMethod2.class, MainCallMethod3.class);

  Result runTest(Class mainClass, Path proguardConfig) throws Exception {
    List<Class> classes = new ArrayList<>(applicationClasses);
    classes.add(mainClass);

    DexInspector inspector = new DexInspector(compileWithR8(classes, proguardConfig));

    Set<String> classesAfterProguard = null;
    // Actually running Proguard should only be during development.
    if (false) {
      Path proguardedJar = runProguard(jarTestClasses(classes), proguardConfig);
      classesAfterProguard = readJarClasses(proguardedJar);
    }

    return new Result(inspector, classesAfterProguard);
  }

  @Test
  public void testNoIncludesDescriptorClasses() throws Exception {
    for (Class mainClass : mainClasses) {
      List<Class> allClasses = new ArrayList<>(applicationClasses);
      allClasses.add(mainClass);

      Path proguardConfig = writeTextToTempFile(
          keepMainProguardConfiguration(mainClass),
          "-keepclasseswithmembers class * {   ",
          "  <fields>;                         ",
          "  native <methods>;                 ",
          "}                                   ",
          "-allowaccessmodification            "
      );

      Result result = runTest(mainClass, proguardConfig);

      // Without includedescriptorclasses return type argument type and field type are removed.
      result.assertKept(ClassWithNativeMethods.class);
      result.assertRemoved(NativeArgumentType.class);
      result.assertRemoved(NativeReturnType.class);
      result.assertRemoved(InstanceFieldType.class);
      result.assertRemoved(StaticFieldType.class);
    }
  }

  @Test
  public void testKeepClassesWithMembers() throws Exception {
    for (Class mainClass : mainClasses) {
      Path proguardConfig = writeTextToTempFile(
          keepMainProguardConfiguration(mainClass),
          "-keepclasseswithmembers,includedescriptorclasses class * {  ",
          "  <fields>;                                                 ",
          "  native <methods>;                                         ",
          "}                                                           ",
          "-allowaccessmodification                                    "
      );

      Result result = runTest(mainClass, proguardConfig);

      // With includedescriptorclasses return type, argument type ad field type are not renamed.
      result.assertKept(ClassWithNativeMethods.class);
      result.assertKept(NativeArgumentType.class);
      result.assertKept(NativeReturnType.class);
      result.assertKept(InstanceFieldType.class);
      result.assertKept(StaticFieldType.class);
    }
  }

  @Test
  public void testKeepClassMembers() throws Exception {
    for (Class mainClass : mainClasses) {
      Path proguardConfig = writeTextToTempFile(
          keepMainProguardConfiguration(mainClass),
          "-keepclassmembers,includedescriptorclasses class * {  ",
          "  <fields>;                                           ",
          "  native <methods>;                                   ",
          "}                                                     ",
          "-allowaccessmodification                              "
      );

      Result result = runTest(mainClass, proguardConfig);

      // With includedescriptorclasses return type and argument type are not renamed.
      result.assertRenamed(ClassWithNativeMethods.class);
      result.assertKept(NativeArgumentType.class);
      result.assertKept(NativeReturnType.class);
      result.assertKept(InstanceFieldType.class);
      result.assertKept(StaticFieldType.class);
    }
  }

    @Test
    public void testKeepClassMemberNames() throws Exception {
      for (Class mainClass : mainClasses) {
        Path proguardConfig = writeTextToTempFile(
            keepMainProguardConfiguration(mainClass),
            // same as -keepclassmembers,allowshrinking,includedescriptorclasses
            "-keepclassmembernames,includedescriptorclasses class * {  ",
            "  <fields>;                                               ",
            "  native <methods>;                                       ",
            "}                                                         ",
            "-allowaccessmodification                                  "
        );

        Result result = runTest(mainClass, proguardConfig);

        boolean useNativeArgumentType =
            mainClass == MainCallMethod1.class || mainClass == MainCallMethod3.class;
        boolean useNativeReturnType =
            mainClass == MainCallMethod2.class || mainClass == MainCallMethod3.class;

        result.assertRenamed(ClassWithNativeMethods.class);
        if (useNativeArgumentType) {
          result.assertKept(NativeArgumentType.class);
        } else {
          result.assertRemoved(NativeArgumentType.class);
        }

        if (useNativeReturnType) {
          result.assertKept(NativeReturnType.class);
        } else {
          result.assertRemoved(NativeReturnType.class);
        }

        result.assertRemoved(InstanceFieldType.class);
        result.assertRemoved(StaticFieldType.class);
      }
    }
}
