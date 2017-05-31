// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8;

import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.errors.InternalCompilerError;
import com.android.tools.r8.errors.Unimplemented;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

public class D8IncrementalRunExamplesAndroidOTest
    extends RunExamplesAndroidOTest<D8Command.Builder> {

  class D8IncrementalTestRunner extends TestRunner {

    D8IncrementalTestRunner(String testName, String packageName, String mainClass) {
      super(testName, packageName, mainClass);
    }

    @Override
    TestRunner withMinApiLevel(int minApiLevel) {
      return withBuilderTransformation(builder -> builder.setMinApiLevel(minApiLevel));
    }

    @Override
    void build(Path testJarFile, Path out) throws Throwable {
      // Collect classes and compile separately.
      List<String> classFiles = collectClassFiles(testJarFile);
      List<String> dexFiles = new ArrayList<>();
      for (int i = 0; i < classFiles.size(); i++) {
        Path indexedOut = Paths.get(
            out.getParent().toString(), out.getFileName() + "." + i + ".zip");
        compile(testJarFile.toString(), Collections.singletonList(classFiles.get(i)), indexedOut);
        dexFiles.add(indexedOut.toString());
      }

      // When compiled add files separately, merge them.
      compile(null, dexFiles, out);
    }

    private List<String> collectClassFiles(Path testJarFile) {
      List<String> result = new ArrayList<>();
      // Collect Java 8 classes.
      Path parent = testJarFile.getParent();
      File packageDir = parent.resolve(Paths.get("classes", packageName)).toFile();
      collectClassFiles(packageDir, result);
      // Collect legacy classes.
      Path legacyPath = Paths.get("..",
          parent.getFileName().toString() + "Legacy", "classes", packageName);
      packageDir = parent.resolve(legacyPath).toFile();
      collectClassFiles(packageDir, result);
      Collections.sort(result);
      return result;
    }

    private void collectClassFiles(File dir, List<String> result) {
      File[] files = dir.listFiles();
      if (files != null) {
        for (File file : files) {
          if (file.isDirectory()) {
            collectClassFiles(file, result);
          } else {
            result.add(file.getAbsolutePath());
          }
        }
      }
    }

    private void compile(String classpath, List<String> inputFiles, Path out) throws Throwable {
      D8Command.Builder builder = D8Command.builder();
      if (classpath != null) {
        builder.addClasspathFiles(Paths.get(classpath));
      }
      for (String inputFile : inputFiles) {
        builder.addProgramFiles(Paths.get(inputFile));
      }
      for (UnaryOperator<D8Command.Builder> transformation : builderTransformations) {
        builder = transformation.apply(builder);
      }
      builder.addLibraryFiles(Paths.get(ToolHelper.getAndroidJar(builder.getMinApiLevel())));
      D8Command command = builder.setOutputPath(out).build();
      try {
        ToolHelper.runD8(command, this::combinedOptionConsumer);
      } catch (Unimplemented | CompilationError | InternalCompilerError re) {
        throw re;
      } catch (RuntimeException re) {
        throw re.getCause() == null ? re : re.getCause();
      }
    }
  }

  @Override
  TestRunner test(String testName, String packageName, String mainClass) {
    return new D8IncrementalTestRunner(testName, packageName, mainClass);
  }
}
