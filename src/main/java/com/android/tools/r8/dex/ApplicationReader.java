// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.dex;

import static com.android.tools.r8.dex.Constants.ANDROID_N_API;
import static com.android.tools.r8.dex.Constants.ANDROID_N_DEX_VERSION;
import static com.android.tools.r8.dex.Constants.ANDROID_O_API;
import static com.android.tools.r8.dex.Constants.ANDROID_O_DEX_VERSION;
import static com.android.tools.r8.dex.Constants.DEFAULT_ANDROID_API;
import static com.android.tools.r8.graph.ClassKind.CLASSPATH;
import static com.android.tools.r8.graph.ClassKind.LIBRARY;
import static com.android.tools.r8.graph.ClassKind.PROGRAM;
import static com.android.tools.r8.utils.FileUtils.DEFAULT_DEX_FILENAME;

import com.android.tools.r8.ClassFileResourceProvider;
import com.android.tools.r8.Resource;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.graph.ClassKind;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexClasspathClass;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexLibraryClass;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.JarApplicationReader;
import com.android.tools.r8.graph.JarClassFileReader;
import com.android.tools.r8.naming.ProguardMapReader;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.ClassProvider;
import com.android.tools.r8.utils.ClasspathClassCollection;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.LibraryClassCollection;
import com.android.tools.r8.utils.MainDexList;
import com.android.tools.r8.utils.ThreadUtils;
import com.android.tools.r8.utils.Timing;
import com.google.common.io.Closer;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ApplicationReader {

  final InternalOptions options;
  final DexItemFactory itemFactory;
  final Timing timing;
  private final AndroidApp inputApp;

  public ApplicationReader(AndroidApp inputApp, InternalOptions options, Timing timing) {
    this.options = options;
    itemFactory = options.itemFactory;
    this.timing = timing;
    this.inputApp = inputApp;
  }

  public DexApplication read() throws IOException, ExecutionException {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      return read(executor);
    } finally {
      executor.shutdown();
    }
  }

  public final DexApplication read(ExecutorService executorService)
      throws IOException, ExecutionException {
    timing.begin("DexApplication.read");
    final DexApplication.Builder builder = new DexApplication.Builder(itemFactory, timing);
    try (Closer closer = Closer.create()) {
      List<Future<?>> futures = new ArrayList<>();
      // Still preload some of the classes, primarily for two reasons:
      // (a) class lazy loading is not supported for DEX files
      //     now and current implementation of parallel DEX file
      //     loading will be lost with on-demand class loading.
      // (b) some of the class file resources don't provide information
      //     about class descriptor.
      // TODO: try and preload less classes.
      readProguardMap(builder, executorService, futures, closer);
      readMainDexList(builder, executorService, futures, closer);
      ClassReader classReader = new ClassReader(executorService, futures, closer);
      classReader.readSources();
      ThreadUtils.awaitFutures(futures);
      classReader.initializeLazyClassCollection(builder);
    } finally {
      timing.end();
    }
    return builder.build();
  }

  private int verifyOrComputeMinApiLevel(int computedMinApiLevel, DexFile file) {
    int version = file.getDexVersion();
    if (options.minApiLevel == DEFAULT_ANDROID_API) {
      computedMinApiLevel = Math.max(computedMinApiLevel, dexVersionToMinSdk(version));
    } else if (!minApiMatchesDexVersion(version)) {
      throw new CompilationError("Dex file with version '" + version +
          "' cannot be used with min sdk level '" + options.minApiLevel + "'.");
    }
    return computedMinApiLevel;
  }

  private boolean minApiMatchesDexVersion(int version) {
    switch (version) {
      case ANDROID_O_DEX_VERSION:
        return options.minApiLevel >= ANDROID_O_API;
      case ANDROID_N_DEX_VERSION:
        return options.minApiLevel >= ANDROID_N_API;
      default:
        return true;
    }
  }

  private int dexVersionToMinSdk(int version) {
    switch (version) {
      case ANDROID_O_DEX_VERSION:
        return ANDROID_O_API;
      case ANDROID_N_DEX_VERSION:
        return ANDROID_N_API;
      default:
        return DEFAULT_ANDROID_API;
    }
  }

  private void readProguardMap(DexApplication.Builder builder, ExecutorService executorService,
      List<Future<?>> futures, Closer closer)
      throws IOException {
    // Read the Proguard mapping file in parallel with DexCode and DexProgramClass items.
    if (inputApp.hasProguardMap()) {
      futures.add(executorService.submit(() -> {
        try {
          InputStream map = inputApp.getProguardMap(closer);
          builder.setProguardMap(ProguardMapReader.mapperFromInputStream(map));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }));
    }
  }

  private void readMainDexList(DexApplication.Builder builder, ExecutorService executorService,
      List<Future<?>> futures, Closer closer)
      throws IOException {
    if (inputApp.hasMainDexList()) {
      futures.add(executorService.submit(() -> {
        try {
          InputStream input = inputApp.getMainDexList(closer);
          builder.addToMainDexList(MainDexList.parse(input, itemFactory));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }));
    }
  }

  private final class ClassReader {
    private final ExecutorService executorService;
    private final List<Future<?>> futures;
    private final Closer closer;

    // We use concurrent queues to collect classes
    // since the classes can be collected concurrently.
    private final Queue<DexProgramClass> programClasses = new ConcurrentLinkedQueue<>();
    private final Queue<DexClasspathClass> classpathClasses = new ConcurrentLinkedQueue<>();
    private final Queue<DexLibraryClass> libraryClasses = new ConcurrentLinkedQueue<>();
    // Jar application reader to share across all class readers.
    private final JarApplicationReader application = new JarApplicationReader(options);

    ClassReader(ExecutorService executorService, List<Future<?>> futures, Closer closer) {
      this.executorService = executorService;
      this.futures = futures;
      this.closer = closer;
    }

    private <T extends DexClass> void readDexSources(List<Resource> dexSources,
        ClassKind classKind, Queue<T> classes) throws IOException, ExecutionException {
      if (dexSources.size() > 0) {
        List<DexFileReader> fileReaders = new ArrayList<>(dexSources.size());
        int computedMinApiLevel = options.minApiLevel;
        for (Resource input : dexSources) {
          DexFile file = new DexFile(input.getStream(closer));
          computedMinApiLevel = verifyOrComputeMinApiLevel(computedMinApiLevel, file);
          fileReaders.add(new DexFileReader(file, classKind, itemFactory));
        }
        options.minApiLevel = computedMinApiLevel;
        for (DexFileReader reader : fileReaders) {
          DexFileReader.populateIndexTables(reader);
        }
        // Read the DexCode items and DexProgramClass items in parallel.
        for (DexFileReader reader : fileReaders) {
          futures.add(executorService.submit(() -> {
            reader.addCodeItemsTo();  // Depends on Everything for parsing.
            reader.addClassDefsTo(
                classKind.bridgeConsumer(classes::add)); // Depends on Methods, Code items etc.
          }));
        }
      }
    }

    private <T extends DexClass> void readClassSources(List<Resource> classSources,
        ClassKind classKind, Queue<T> classes) throws IOException, ExecutionException {
      JarClassFileReader reader = new JarClassFileReader(
          application, classKind.bridgeConsumer(classes::add));
      for (Resource input : classSources) {
        reader.read(DEFAULT_DEX_FILENAME, classKind, input.getStream(closer));
      }
    }

    void readSources() throws IOException, ExecutionException {
      readDexSources(inputApp.getDexProgramResources(), PROGRAM, programClasses);
      readDexSources(inputApp.getDexClasspathResources(), CLASSPATH, classpathClasses);
      readDexSources(inputApp.getDexLibraryResources(), LIBRARY, libraryClasses);
      readClassSources(inputApp.getClassProgramResources(), PROGRAM, programClasses);
      readClassSources(inputApp.getClassClasspathResources(), CLASSPATH, classpathClasses);
      readClassSources(inputApp.getClassLibraryResources(), LIBRARY, libraryClasses);
    }

    private <T extends DexClass> ClassProvider<T> buildClassProvider(ClassKind classKind,
        Queue<T> preloadedClasses, List<ClassFileResourceProvider> resourceProviders,
        JarApplicationReader reader) {
      List<ClassProvider<T>> providers = new ArrayList<>();

      // Preloaded classes.
      if (!preloadedClasses.isEmpty()) {
        providers.add(ClassProvider.forPreloadedClasses(classKind, preloadedClasses));
      }

      // Class file resource providers.
      for (ClassFileResourceProvider provider : resourceProviders) {
        providers.add(ClassProvider.forClassFileResources(classKind, provider, reader));
      }

      // Combine if needed.
      if (providers.isEmpty()) {
        return null;
      }
      return providers.size() == 1 ? providers.get(0)
          : ClassProvider.combine(classKind, providers);
    }

    void initializeLazyClassCollection(DexApplication.Builder builder) {
      // Add all program classes to the builder.
      for (DexProgramClass clazz : programClasses) {
        builder.addProgramClass(clazz.asProgramClass());
      }

      // Create classpath class collection if needed.
      ClassProvider<DexClasspathClass> classpathClassProvider = buildClassProvider(CLASSPATH,
          classpathClasses, inputApp.getClasspathResourceProviders(), application);
      if (classpathClassProvider != null) {
        builder.setClasspathClassCollection(new ClasspathClassCollection(classpathClassProvider));
      }

      // Create library class collection if needed.
      ClassProvider<DexLibraryClass> libraryClassProvider = buildClassProvider(LIBRARY,
          libraryClasses, inputApp.getLibraryResourceProviders(), application);
      if (libraryClassProvider != null) {
        builder.setLibraryClassCollection(new LibraryClassCollection(libraryClassProvider));
      }
    }
  }
}
