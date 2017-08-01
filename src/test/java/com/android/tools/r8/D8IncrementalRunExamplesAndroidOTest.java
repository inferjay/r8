// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8;

import static com.android.tools.r8.utils.FileUtils.JAR_EXTENSION;
import static org.junit.Assert.assertEquals;

import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.errors.InternalCompilerError;
import com.android.tools.r8.errors.Unimplemented;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.OffOrAuto;
import com.android.tools.r8.utils.OutputMode;
import com.beust.jcommander.internal.Lists;
import com.google.common.io.Closer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.UnaryOperator;
import org.junit.Assert;
import org.junit.Test;

public abstract class D8IncrementalRunExamplesAndroidOTest
    extends RunExamplesAndroidOTest<D8Command.Builder> {

  abstract class D8IncrementalTestRunner extends TestRunner {

    D8IncrementalTestRunner(String testName, String packageName, String mainClass) {
      super(testName, packageName, mainClass);
    }

    @Override
    TestRunner withMinApiLevel(int minApiLevel) {
      return withBuilderTransformation(builder -> builder.setMinApiLevel(minApiLevel));
    }

    @Override
    void build(Path testJarFile, Path out) throws Throwable {
      Map<String, Resource> files = compileClassesTogether(testJarFile, null);
      mergeClassFiles(Lists.newArrayList(files.values()), out);
    }

    // Dex classes separately.
    SortedMap<String, Resource> compileClassesSeparately(Path testJarFile) throws Throwable {
      TreeMap<String, Resource> fileToResource = new TreeMap<>();
      List<String> classFiles = collectClassFiles(testJarFile);
      for (String classFile : classFiles) {
        AndroidApp app = compileClassFiles(
            testJarFile, Collections.singletonList(classFile), null, OutputMode.Indexed);
        assert app.getDexProgramResources().size() == 1;
        fileToResource.put(
            makeRelative(testJarFile, Paths.get(classFile)).toString(),
            app.getDexProgramResources().get(0));
      }
      return fileToResource;
    }

    // Dex classes in one D8 invocation.
    SortedMap<String, Resource> compileClassesTogether(
        Path testJarFile, Path output) throws Throwable {
      TreeMap<String, Resource> fileToResource = new TreeMap<>();
      List<String> classFiles = collectClassFiles(testJarFile);
      AndroidApp app = compileClassFiles(
          testJarFile, classFiles, output, OutputMode.FilePerClass);
      for (Resource resource : app.getDexProgramResources()) {
        Set<String> descriptors = resource.getClassDescriptors();
        Assert.assertNotNull(descriptors);
        Assert.assertEquals(1, descriptors.size());
        String classDescriptor = descriptors.iterator().next();
        classDescriptor = classDescriptor.substring(1, classDescriptor.length() - 1);
        String classFilePath = classDescriptor + ".class";
        if (File.separatorChar != '/') {
          classFilePath = classFilePath.replace('/', File.separatorChar);
        }
        fileToResource.put(classFilePath, resource);
      }
      return fileToResource;
    }

    private Path makeRelative(Path testJarFile, Path classFile) {
      Path regularParent =
          testJarFile.getParent().resolve(Paths.get("classes"));
      Path legacyParent = regularParent.resolve(Paths.get("..",
          regularParent.getFileName().toString() + "Legacy", "classes"));

      if (classFile.startsWith(regularParent)) {
        return regularParent.relativize(classFile);
      }
      Assert.assertTrue(classFile.startsWith(legacyParent));
      return legacyParent.relativize(classFile);
    }

    private List<String> collectClassFiles(Path testJarFile) {
      List<String> result = new ArrayList<>();
      // Collect Java 8 classes.
      collectClassFiles(getClassesRoot(testJarFile), result);
      // Collect legacy classes.
      collectClassFiles(getLegacyClassesRoot(testJarFile), result);
      Collections.sort(result);
      return result;
    }

    Path getClassesRoot(Path testJarFile) {
      Path parent = testJarFile.getParent();
      return parent.resolve(Paths.get("classes", packageName));
    }

    Path getLegacyClassesRoot(Path testJarFile) {
      Path parent = testJarFile.getParent();
      Path legacyPath = Paths.get("..",
          parent.getFileName().toString() + "Legacy", "classes", packageName);
      return parent.resolve(legacyPath);
    }

    private void collectClassFiles(Path dir, List<String> result) {
      if (Files.exists(dir)) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
          for (Path entry: stream) {
            if (Files.isDirectory(entry)) {
              collectClassFiles(entry, result);
            } else {
              result.add(entry.toString());
            }
          }
        } catch (IOException x) {
          throw new AssertionError(x);
        }
      }
    }

    AndroidApp compileClassFiles(Path testJarFile,
        List<String> inputFiles, Path output, OutputMode outputMode) throws Throwable {
      D8Command.Builder builder = D8Command.builder();
      addClasspathReference(testJarFile, builder);
      for (String inputFile : inputFiles) {
        builder = builder.addProgramFiles(Paths.get(inputFile));
      }
      for (UnaryOperator<D8Command.Builder> transformation : builderTransformations) {
        builder = transformation.apply(builder);
      }
      builder = builder.setOutputMode(outputMode);
      builder = builder.addLibraryFiles(
          Paths.get(ToolHelper.getAndroidJar(builder.getMinApiLevel())));
      if (output != null) {
        builder = builder.setOutputPath(output);
      }
      addLibraryReference(builder, Paths.get(ToolHelper.getAndroidJar(builder.getMinApiLevel())));
      D8Command command = builder.build();
      try {
        return ToolHelper.runD8(command, this::combinedOptionConsumer);
      } catch (Unimplemented | CompilationError | InternalCompilerError re) {
        throw re;
      } catch (RuntimeException re) {
        throw re.getCause() == null ? re : re.getCause();
      }
    }

    Resource mergeClassFiles(List<Resource> dexFiles, Path out) throws Throwable {
      D8Command.Builder builder = D8Command.builder();
      for (Resource dexFile : dexFiles) {
        builder.addDexProgramData(readFromResource(dexFile));
      }
      for (UnaryOperator<D8Command.Builder> transformation : builderTransformations) {
        builder = transformation.apply(builder);
      }
      if (out != null) {
        builder = builder.setOutputPath(out);
      }
      D8Command command = builder.build();
      try {
        AndroidApp app = ToolHelper.runD8(command, this::combinedOptionConsumer);
        assert app.getDexProgramResources().size() == 1;
        return app.getDexProgramResources().get(0);
      } catch (Unimplemented | CompilationError | InternalCompilerError re) {
        throw re;
      } catch (RuntimeException re) {
        throw re.getCause() == null ? re : re.getCause();
      }
    }

    abstract void addClasspathReference(
        Path testJarFile, D8Command.Builder builder) throws IOException;

    abstract void addLibraryReference(
        D8Command.Builder builder, Path location) throws IOException;
  }

  @Test
  public void dexPerClassFileNoDesugaring() throws Throwable {
    String testName = "dexPerClassFileNoDesugaring";
    String testPackage = "incremental";
    String mainClass = "IncrementallyCompiled";

    Path inputJarFile = Paths.get(EXAMPLE_DIR, testPackage + JAR_EXTENSION);

    D8IncrementalTestRunner test = test(testName, testPackage, mainClass);

    Map<String, Resource> compiledSeparately = test.compileClassesSeparately(inputJarFile);
    Map<String, Resource> compiledTogether = test.compileClassesTogether(inputJarFile, null);
    Assert.assertEquals(compiledSeparately.size(), compiledTogether.size());

    for (Map.Entry<String, Resource> entry : compiledSeparately.entrySet()) {
      Resource otherResource = compiledTogether.get(entry.getKey());
      Assert.assertNotNull(otherResource);
      Assert.assertArrayEquals(readFromResource(entry.getValue()), readFromResource(otherResource));
    }

    Resource mergedFromCompiledSeparately =
        test.mergeClassFiles(Lists.newArrayList(compiledSeparately.values()), null);
    Resource mergedFromCompiledTogether =
        test.mergeClassFiles(Lists.newArrayList(compiledTogether.values()), null);
    Assert.assertArrayEquals(
        readFromResource(mergedFromCompiledSeparately),
        readFromResource(mergedFromCompiledTogether));
  }

  @Test
  public void dexPerClassFileWithDesugaring() throws Throwable {
    String testName = "dexPerClassFileWithDesugaring";
    String testPackage = "lambdadesugaringnplus";
    String mainClass = "LambdasWithStaticAndDefaultMethods";

    Path inputJarFile = Paths.get(EXAMPLE_DIR, testPackage + JAR_EXTENSION);

    D8IncrementalTestRunner test = test(testName, testPackage, mainClass);
    test.withInterfaceMethodDesugaring(OffOrAuto.Auto);

    Resource mergedFromCompiledSeparately =
        test.mergeClassFiles(Lists.newArrayList(
            test.compileClassesSeparately(inputJarFile).values()), null);
    Resource mergedFromCompiledTogether =
        test.mergeClassFiles(Lists.newArrayList(
            test.compileClassesTogether(inputJarFile, null).values()), null);

    Assert.assertArrayEquals(
        readFromResource(mergedFromCompiledSeparately),
        readFromResource(mergedFromCompiledTogether));
  }

  @Test
  public void dexPerClassFileOutputFiles() throws Throwable {
    String testName = "dexPerClassFileNoDesugaring";
    String testPackage = "incremental";
    String mainClass = "IncrementallyCompiled";

    Path out = temp.getRoot().toPath();

    Path inputJarFile = Paths.get(EXAMPLE_DIR, testPackage + JAR_EXTENSION);

    D8IncrementalTestRunner test = test(testName, testPackage, mainClass);
    test.compileClassesTogether(inputJarFile, out);


    String[] topLevelDir = out.toFile().list();
    assert topLevelDir != null;
    assertEquals(1, topLevelDir.length);
    assertEquals("incremental", topLevelDir[0]);

    String[] dexFiles = out.resolve(topLevelDir[0]).toFile().list();
    assert dexFiles != null;
    Arrays.sort(dexFiles);

    String[] expectedFileNames = {
        "IncrementallyCompiled$A$AB.dex",
        "IncrementallyCompiled$A.dex",
        "IncrementallyCompiled$B$BA.dex",
        "IncrementallyCompiled$B.dex",
        "IncrementallyCompiled$C.dex",
        "IncrementallyCompiled.dex"
    };
    Arrays.sort(expectedFileNames);

    Assert.assertArrayEquals(expectedFileNames, dexFiles);
  }

  abstract D8IncrementalTestRunner test(String testName, String packageName, String mainClass);

  static byte[] readFromResource(Resource resource) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    byte[] buffer = new byte[16384];
    try (Closer closer = Closer.create()) {
      InputStream stream = closer.register(resource.getStream());
      int read;
      while ((read = stream.read(buffer, 0, buffer.length)) != -1) {
        output.write(buffer, 0, read);
      }
    }
    return output.toByteArray();
  }
}
