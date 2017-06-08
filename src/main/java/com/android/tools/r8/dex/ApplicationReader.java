// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.dex;

import static com.android.tools.r8.dex.Constants.ANDROID_N_API;
import static com.android.tools.r8.dex.Constants.ANDROID_N_DEX_VERSION;
import static com.android.tools.r8.dex.Constants.ANDROID_O_API;
import static com.android.tools.r8.dex.Constants.ANDROID_O_DEX_VERSION;
import static com.android.tools.r8.dex.Constants.DEFAULT_ANDROID_API;
import static com.android.tools.r8.utils.FileUtils.DEFAULT_DEX_FILENAME;

import com.android.tools.r8.Resource;
import com.android.tools.r8.ResourceProvider;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.graph.ClassKind;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.JarApplicationReader;
import com.android.tools.r8.graph.JarClassFileReader;
import com.android.tools.r8.naming.ProguardMapReader;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.LazyClassCollection;
import com.android.tools.r8.utils.MainDexList;
import com.android.tools.r8.utils.ThreadUtils;
import com.android.tools.r8.utils.Timing;
import com.google.common.io.Closer;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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
      readProguardMap(builder, executorService, futures, closer);
      readMainDexList(builder, executorService, futures, closer);
      readDexSources(builder, executorService, futures, closer);
      readClassSources(builder, closer);
      initializeLazyClassCollection(builder);
      ThreadUtils.awaitFutures(futures);
    } catch (ExecutionException e) {
      // If the reading failed with a valid compilation error, rethrow the unwrapped exception.
      Throwable cause = e.getCause();
      if (cause != null && cause instanceof CompilationError) {
        throw (CompilationError) cause;
      }
      throw e;
    } finally {
      timing.end();
    }
    return builder.build();
  }

  private void readClassSources(DexApplication.Builder builder, Closer closer)
      throws IOException, ExecutionException {
    JarApplicationReader application = new JarApplicationReader(options);
    JarClassFileReader reader = new JarClassFileReader(
        application, builder::addClassIgnoringLibraryDuplicates);
    for (Resource input : inputApp.getClassProgramResources()) {
      reader.read(DEFAULT_DEX_FILENAME, ClassKind.PROGRAM, input.getStream(closer));
    }
    for (Resource input : inputApp.getClassClasspathResources()) {
      reader.read(DEFAULT_DEX_FILENAME, ClassKind.CLASSPATH, input.getStream(closer));
    }
    for (Resource input : inputApp.getClassLibraryResources()) {
      reader.read(DEFAULT_DEX_FILENAME, ClassKind.LIBRARY, input.getStream(closer));
    }
  }

  private void initializeLazyClassCollection(DexApplication.Builder builder) {
    List<ResourceProvider> classpathProviders = inputApp.getClasspathResourceProviders();
    List<ResourceProvider> libraryProviders = inputApp.getLibraryResourceProviders();
    if (!classpathProviders.isEmpty() || !libraryProviders.isEmpty()) {
      builder.setLazyClassCollection(new LazyClassCollection(
          new JarApplicationReader(options), classpathProviders, libraryProviders));
    }
  }

  private void readDexSources(DexApplication.Builder builder, ExecutorService executorService,
      List<Future<?>> futures, Closer closer)
      throws IOException, ExecutionException {
    List<Resource> dexProgramSources = inputApp.getDexProgramResources();
    List<Resource> dexClasspathSources = inputApp.getDexClasspathResources();
    List<Resource> dexLibrarySources = inputApp.getDexLibraryResources();
    int numberOfFiles = dexProgramSources.size()
        + dexLibrarySources.size() + dexClasspathSources.size();
    if (numberOfFiles > 0) {
      List<DexFileReader> fileReaders = new ArrayList<>(numberOfFiles);
      int computedMinApiLevel = options.minApiLevel;
      for (Resource input : dexProgramSources) {
        DexFile file = new DexFile(input.getStream(closer));
        computedMinApiLevel = verifyOrComputeMinApiLevel(computedMinApiLevel, file);
        fileReaders.add(new DexFileReader(file, ClassKind.PROGRAM, itemFactory));
      }
      for (Resource input : dexClasspathSources) {
        DexFile file = new DexFile(input.getStream(closer));
        fileReaders.add(new DexFileReader(file, ClassKind.CLASSPATH, itemFactory));
      }
      for (Resource input : dexLibrarySources) {
        DexFile file = new DexFile(input.getStream(closer));
        computedMinApiLevel = verifyOrComputeMinApiLevel(computedMinApiLevel, file);
        fileReaders.add(new DexFileReader(file, ClassKind.LIBRARY, itemFactory));
      }
      options.minApiLevel = computedMinApiLevel;
      for (DexFileReader reader : fileReaders) {
        DexFileReader.populateIndexTables(reader);
      }
      // Read the DexCode items and DexProgramClass items in parallel.
      for (DexFileReader reader : fileReaders) {
        futures.add(executorService.submit(() -> {
          reader.addCodeItemsTo();  // Depends on Everything for parsing.
          reader.addClassDefsTo(builder::addClass);  // Depends on Methods, Code items etc.
        }));
      }
    }
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
}
