// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.dex;

import com.android.tools.r8.dex.VirtualFile.FilePerClassDistributor;
import com.android.tools.r8.dex.VirtualFile.FillFilesDistributor;
import com.android.tools.r8.dex.VirtualFile.PackageMapDistributor;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.graph.AppInfo;
import com.android.tools.r8.graph.DexAnnotation;
import com.android.tools.r8.graph.DexAnnotationDirectory;
import com.android.tools.r8.graph.DexAnnotationSet;
import com.android.tools.r8.graph.DexAnnotationSetRefList;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexCode;
import com.android.tools.r8.graph.DexDebugInfo;
import com.android.tools.r8.graph.DexEncodedArray;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.DexTypeList;
import com.android.tools.r8.graph.DexValue;
import com.android.tools.r8.naming.MinifiedNameMapPrinter;
import com.android.tools.r8.naming.NamingLens;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.DescriptorUtils;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.OutputMode;
import com.android.tools.r8.utils.PackageDistribution;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class ApplicationWriter {

  public final DexApplication application;
  public final AppInfo appInfo;
  public final NamingLens namingLens;
  public final byte[] proguardSeedsData;
  public final InternalOptions options;

  private static class SortAnnotations extends MixedSectionCollection {

    @Override
    public boolean add(DexAnnotationSet dexAnnotationSet) {
      // Annotation sets are sorted by annotation types.
      dexAnnotationSet.sort();
      return true;
    }

    @Override
    public boolean add(DexAnnotation annotation) {
      // The elements of encoded annotation must be sorted by name.
      annotation.annotation.sort();
      return true;
    }

    @Override
    public boolean add(DexEncodedArray dexEncodedArray) {
      // Dex values must potentially be sorted, eg, for DexValueAnnotation.
      for (DexValue value : dexEncodedArray.values) {
        value.sort();
      }
      return true;
    }

    @Override
    public boolean add(DexProgramClass dexClassData) {
      return true;
    }

    @Override
    public boolean add(DexCode dexCode) {
      return true;
    }

    @Override
    public boolean add(DexDebugInfo dexDebugInfo) {
      return true;
    }

    @Override
    public boolean add(DexTypeList dexTypeList) {
      return true;
    }

    @Override
    public boolean add(DexAnnotationSetRefList annotationSetRefList) {
      return true;
    }

    @Override
    public boolean setAnnotationsDirectoryForClass(DexProgramClass clazz,
        DexAnnotationDirectory annotationDirectory) {
      return true;
    }
  }

  public ApplicationWriter(
      DexApplication application,
      AppInfo appInfo,
      InternalOptions options,
      NamingLens namingLens,
      byte[] proguardSeedsData) {
    assert application != null;
    this.application = application;
    this.appInfo = appInfo;
    assert options != null;
    this.options = options;
    this.namingLens = namingLens;
    this.proguardSeedsData = proguardSeedsData;
  }

  public AndroidApp write(PackageDistribution packageDistribution, ExecutorService executorService)
      throws IOException, ExecutionException {
    application.timing.begin("DexApplication.write");
    try {
      application.dexItemFactory.sort(namingLens);
      SortAnnotations sortAnnotations = new SortAnnotations();
      application.classes().forEach((clazz) -> clazz.addDependencies(sortAnnotations));

      // Distribute classes into dex files.
      VirtualFile.Distributor distributor = null;
      if (options.outputMode == OutputMode.FilePerClass) {
        assert packageDistribution == null :
            "Cannot combine package distribution definition with file-per-class option.";
        distributor = new FilePerClassDistributor(this);
      } else if (options.minApiLevel < Constants.ANDROID_L_API
            && options.mainDexKeepRules.isEmpty()
            && application.mainDexList.isEmpty()) {
        if (packageDistribution != null) {
          throw new CompilationError("Cannot apply package distribution. Multidex is not"
              + " supported with API level " + options.minApiLevel +"."
              + " For API level < " + Constants.ANDROID_L_API + ", main dex classes list or"
              + " rules must be specified.");
        }
        distributor = new VirtualFile.MonoDexDistributor(this);
      } else if (packageDistribution != null) {
        assert !options.minimalMainDex :
            "Cannot combine package distribution definition with minimal-main-dex option.";
        distributor = new PackageMapDistributor(this, packageDistribution, executorService);
      } else {
        distributor = new FillFilesDistributor(this, options.minimalMainDex);
      }
      Map<Integer, VirtualFile> newFiles = distributor.run();

      // Write the dex files and the Proguard mapping file in parallel. Use a linked hash map
      // as the order matters when addDexProgramData is called below.
      LinkedHashMap<VirtualFile, Future<byte[]>> dexDataFutures = new LinkedHashMap<>();
      for (int i = 0; i < newFiles.size(); i++) {
        VirtualFile newFile = newFiles.get(i);
        assert newFile.getId() == i;
        if (!newFile.isEmpty()) {
          dexDataFutures.put(newFile, executorService.submit(() -> writeDexFile(newFile)));
        }
      }

      // Wait for all the spawned futures to terminate.
      AndroidApp.Builder builder = AndroidApp.builder();
      try {
        for (Map.Entry<VirtualFile, Future<byte[]>> entry : dexDataFutures.entrySet()) {
          builder.addDexProgramData(entry.getValue().get(), entry.getKey().getClassDescriptors());
        }
      } catch (InterruptedException e) {
        throw new RuntimeException("Interrupted while waiting for future.", e);
      }
      // Write the proguard map file after writing the dex files, as the map writer traverses
      // the DexProgramClass structures, which are destructively updated during dex file writing.
      byte[] proguardMapResult = writeProguardMapFile();
      if (proguardMapResult != null) {
        builder.setProguardMapData(proguardMapResult);
      }
      if (proguardSeedsData != null) {
        builder.setProguardSeedsData(proguardSeedsData);
      }
      byte[] mainDexList = writeMainDexList();
      if (mainDexList != null) {
        builder.setMainDexListData(mainDexList);
      }
      return builder.build();
    } finally {
      application.timing.end();
    }
  }

  private byte[] writeDexFile(VirtualFile vfile) {
    FileWriter fileWriter =
        new FileWriter(
            vfile.computeMapping(application), application, appInfo, options, namingLens);
    // The file writer now knows the indexes of the fixed sections including strings.
    fileWriter.rewriteCodeWithJumboStrings(vfile.classes());
    // Collect the non-fixed sections.
    fileWriter.collect();
    // Generate and write the bytes.
    return fileWriter.generate();
  }

  private byte[] writeProguardMapFile() throws IOException {
    // TODO(herhut): Should writing of the proguard-map file be split like this?
    if (!namingLens.isIdentityLens()) {
      MinifiedNameMapPrinter printer = new MinifiedNameMapPrinter(application, namingLens);
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      PrintStream stream = new PrintStream(bytes);
      printer.write(stream);
      stream.flush();
      return bytes.toByteArray();
    } else if (application.getProguardMap() != null) {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      Writer writer = new PrintWriter(bytes);
      application.getProguardMap().write(writer, !options.skipDebugLineNumberOpt);
      writer.flush();
      return bytes.toByteArray();
    }
    return null;
  }

  private String mapMainDexListName(DexType type) {
    return DescriptorUtils.descriptorToJavaType(namingLens.lookupDescriptor(type).toString())
        .replace('.', '/') + ".class";
  }

  private byte[] writeMainDexList() throws IOException {
    if (application.mainDexList.isEmpty()) {
      return null;
    }
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter(bytes);
    application.mainDexList.forEach(
        type -> writer.println(mapMainDexListName(type))
    );
    writer.flush();
    return bytes.toByteArray();
  }
}
