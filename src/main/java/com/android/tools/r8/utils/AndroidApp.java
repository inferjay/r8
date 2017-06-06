// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import static com.android.tools.r8.utils.FileUtils.isArchive;
import static com.android.tools.r8.utils.FileUtils.isClassFile;
import static com.android.tools.r8.utils.FileUtils.isDexFile;

import com.android.tools.r8.Resource;
import com.android.tools.r8.ResourceProvider;
import com.android.tools.r8.errors.CompilationError;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Collection of program files needed for processing.
 *
 * <p>This abstraction is the main input and output container for a given application.
 */
public class AndroidApp {

  public static final String DEFAULT_PROGUARD_MAP_FILE = "proguard.map";
  public static final String DEFAULT_PROGUARD_SEEDS_FILE = "proguard.seeds";
  public static final String DEFAULT_PACKAGE_DISTRIBUTION_FILE = "package.map";

  private final ImmutableList<InternalResource> dexSources;
  private final ImmutableList<InternalResource> classSources;
  private final ImmutableList<ResourceProvider> resourceProviders;
  private final InternalResource proguardMap;
  private final InternalResource proguardSeeds;
  private final InternalResource packageDistribution;
  private final InternalResource mainDexList;

  // See factory methods and AndroidApp.Builder below.
  private AndroidApp(
      ImmutableList<InternalResource> dexSources,
      ImmutableList<InternalResource> classSources,
      ImmutableList<ResourceProvider> resourceProviders,
      InternalResource proguardMap,
      InternalResource proguardSeeds,
      InternalResource packageDistribution,
      InternalResource mainDexList) {
    this.dexSources = dexSources;
    this.classSources = classSources;
    this.resourceProviders = resourceProviders;
    this.proguardMap = proguardMap;
    this.proguardSeeds = proguardSeeds;
    this.packageDistribution = packageDistribution;
    this.mainDexList = mainDexList;
  }

  /**
   * Create a new empty builder.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Create a new builder initialized with the resources from @code{app}.
   */
  public static Builder builder(AndroidApp app) {
    return new Builder(app);
  }

  /**
   * Create an app from program files @code{files}. See also Builder::addProgramFiles.
   */
  public static AndroidApp fromProgramFiles(Path... files) throws IOException {
    return fromProgramFiles(Arrays.asList(files));
  }

  /**
   * Create an app from program files @code{files}. See also Builder::addProgramFiles.
   */
  public static AndroidApp fromProgramFiles(List<Path> files) throws IOException {
    return builder().addProgramFiles(files).build();
  }

  /**
   * Create an app from files found in @code{directory}. See also Builder::addProgramDirectory.
   */
  public static AndroidApp fromProgramDirectory(Path directory) throws IOException {
    return builder().addProgramDirectory(directory).build();
  }

  /**
   * Create an app from dex program data. See also Builder::addDexProgramData.
   */
  public static AndroidApp fromDexProgramData(byte[]... data) {
    return fromDexProgramData(Arrays.asList(data));
  }

  /**
   * Create an app from dex program data. See also Builder::addDexProgramData.
   */
  public static AndroidApp fromDexProgramData(List<byte[]> data) {
    return builder().addDexProgramData(data).build();
  }

  /**
   * Create an app from Java-bytecode program data. See also Builder::addClassProgramData.
   */
  public static AndroidApp fromClassProgramData(byte[]... data) {
    return fromClassProgramData(Arrays.asList(data));
  }

  /**
   * Create an app from Java-bytecode program data. See also Builder::addClassProgramData.
   */
  public static AndroidApp fromClassProgramData(List<byte[]> data) {
    return builder().addClassProgramData(data).build();
  }

  /** Get input streams for all dex program resources. */
  public List<InternalResource> getDexProgramResources() {
    return filter(dexSources, Resource.Kind.PROGRAM);
  }

  /** Get input streams for all Java-bytecode program resources. */
  public List<InternalResource> getClassProgramResources() {
    return filter(classSources, Resource.Kind.PROGRAM);
  }

  /** Get input streams for all dex program classpath resources. */
  public List<InternalResource> getDexClasspathResources() {
    return filter(dexSources, Resource.Kind.CLASSPATH);
  }

  /** Get input streams for all Java-bytecode classpath resources. */
  public List<InternalResource> getClassClasspathResources() {
    return filter(classSources, Resource.Kind.CLASSPATH);
  }

  /** Get input streams for all dex library resources. */
  public List<InternalResource> getDexLibraryResources() {
    return filter(dexSources, Resource.Kind.LIBRARY);
  }

  /** Get input streams for all Java-bytecode library resources. */
  public List<InternalResource> getClassLibraryResources() {
    return filter(classSources, Resource.Kind.LIBRARY);
  }

  /** Get lazy resource providers. */
  public List<ResourceProvider> getLazyResourceProviders() {
    return resourceProviders;
  }

  private List<InternalResource> filter(
      List<InternalResource> resources, Resource.Kind kind) {
    List<InternalResource> out = new ArrayList<>(resources.size());
    for (InternalResource resource : resources) {
      if (kind == resource.getKind()) {
        out.add(resource);
      }
    }
    return out;
  }

  /**
   * True if the proguard-map resource exists.
   */
  public boolean hasProguardMap() {
    return proguardMap != null;
  }

  /**
   * Get the input stream of the proguard-map resource if it exists.
   */
  public InputStream getProguardMap(Closer closer) throws IOException {
    return proguardMap == null ? null : proguardMap.getStream(closer);
  }

  /**
   * True if the proguard-seeds resource exists.
   */
  public boolean hasProguardSeeds() {
    return proguardSeeds != null;
  }

  /**
   * Get the input stream of the proguard-seeds resource if it exists.
   */
  public InputStream getProguardSeeds(Closer closer) throws IOException {
    return proguardSeeds == null ? null : proguardSeeds.getStream(closer);
  }

  /**
   * True if the package distribution resource exists.
   */
  public boolean hasPackageDistribution() {
    return packageDistribution != null;
  }

  /**
   * Get the input stream of the package distribution resource if it exists.
   */
  public InputStream getPackageDistribution(Closer closer) throws IOException {
    return packageDistribution == null ? null : packageDistribution.getStream(closer);
  }

  /**
   * True if the main dex list resource exists.
   */
  public boolean hasMainDexList() {
    return mainDexList != null;
  }

  /**
   * Get the input stream of the main dex list resource if it exists.
   */
  public InputStream getMainDexList(Closer closer) throws IOException {
    return mainDexList == null ? null : mainDexList.getStream(closer);
  }

  /**
   * Write the dex program resources and proguard resource to @code{output}.
   */
  public void write(Path output, OutputMode outputMode) throws IOException {
    write(output, outputMode, false);
  }

  /**
   * Write the dex program resources and proguard resource to @code{output}.
   */
  public void write(Path output, OutputMode outputMode, boolean overwrite) throws IOException {
    if (isArchive(output)) {
      writeToZip(output, outputMode, overwrite);
    } else {
      writeToDirectory(output, outputMode, overwrite);
    }
  }

  /**
   * Write the dex program resources and proguard resource to @code{directory}.
   */
  public void writeToDirectory(Path directory, OutputMode outputMode) throws IOException {
    writeToDirectory(directory, outputMode, false);
  }

  /**
   * Write the dex program resources and proguard resource to @code{directory}.
   */
  public void writeToDirectory(
      Path directory, OutputMode outputMode, boolean overwrite) throws IOException {
    CopyOption[] options = copyOptions(overwrite);
    try (Closer closer = Closer.create()) {
      List<InternalResource> dexProgramSources = getDexProgramResources();
      for (int i = 0; i < dexProgramSources.size(); i++) {
        Path fileName = directory.resolve(outputMode.getFileName(dexProgramSources.get(i), i));
        Files.copy(dexProgramSources.get(i).getStream(closer), fileName, options);
      }
      writeProguardMap(closer, directory, overwrite);
      writeProguardSeeds(closer, directory, overwrite);
    }
  }

  public List<byte[]> writeToMemory() throws IOException {
    List<byte[]> dex = new ArrayList<>();
    try (Closer closer = Closer.create()) {
      List<InternalResource> dexProgramSources = getDexProgramResources();
      for (int i = 0; i < dexProgramSources.size(); i++) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteStreams.copy(dexProgramSources.get(i).getStream(closer), out);
        dex.add(out.toByteArray());
      }
      // TODO(sgjesse): Add Proguard map and seeds.
    }
    return dex;
  }

  /**
   * Write the dex program resources to @code{archive} and the proguard resource as its sibling.
   */
  public void writeToZip(Path archive, OutputMode outputMode) throws IOException {
    writeToZip(archive, outputMode, false);
  }

  /**
   * Write the dex program resources to @code{archive} and the proguard resource as its sibling.
   */
  public void writeToZip(
      Path archive, OutputMode outputMode, boolean overwrite) throws IOException {
    OpenOption[] options = openOptions(overwrite);
    try (Closer closer = Closer.create()) {
      try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(archive, options))) {
        List<InternalResource> dexProgramSources = getDexProgramResources();
        for (int i = 0; i < dexProgramSources.size(); i++) {
          ZipEntry zipEntry = new ZipEntry(outputMode.getFileName(dexProgramSources.get(i), i));
          byte[] bytes = ByteStreams.toByteArray(dexProgramSources.get(i).getStream(closer));
          zipEntry.setSize(bytes.length);
          out.putNextEntry(zipEntry);
          out.write(bytes);
          out.closeEntry();
        }
      }
      // Write the proguard map to the archives containing directory.
      // TODO(zerny): How do we want to determine the output location for the extra resources?
      writeProguardMap(closer, archive.getParent(), overwrite);
      writeProguardSeeds(closer, archive.getParent(), overwrite);
    }
  }

  private void writeProguardMap(Closer closer, Path parent, boolean overwrite) throws IOException {
    InputStream input = getProguardMap(closer);
    if (input != null) {
      Files.copy(input, parent.resolve(DEFAULT_PROGUARD_MAP_FILE), copyOptions(overwrite));
    }
  }

  public void writeProguardMap(Closer closer, OutputStream out) throws IOException {
    InputStream input = getProguardMap(closer);
    assert input != null;
    out.write(ByteStreams.toByteArray(input));
  }

  private void writeProguardSeeds(Closer closer, Path parent, boolean overwrite)
      throws IOException {
    InputStream input = getProguardSeeds(closer);
    if (input != null) {
      Files.copy(input, parent.resolve(DEFAULT_PROGUARD_SEEDS_FILE), copyOptions(overwrite));
    }
  }

  private OpenOption[] openOptions(boolean overwrite) {
    return new OpenOption[]{
        overwrite ? StandardOpenOption.CREATE : StandardOpenOption.CREATE_NEW,
        StandardOpenOption.TRUNCATE_EXISTING
    };
  }

  private CopyOption[] copyOptions(boolean overwrite) {
    return overwrite
        ? new CopyOption[]{StandardCopyOption.REPLACE_EXISTING}
        : new CopyOption[]{};
  }

  /**
   * Builder interface for constructing an AndroidApp.
   */
  public static class Builder {

    private final List<InternalResource> dexSources = new ArrayList<>();
    private final List<InternalResource> classSources = new ArrayList<>();
    private final List<ResourceProvider> resourceProviders = new ArrayList<>();
    private InternalResource proguardMap;
    private InternalResource proguardSeeds;
    private InternalResource packageDistribution;
    private InternalResource mainDexList;

    // See AndroidApp::builder().
    private Builder() {
    }

    // See AndroidApp::builder(AndroidApp).
    private Builder(AndroidApp app) {
      dexSources.addAll(app.dexSources);
      classSources.addAll(app.classSources);
      resourceProviders.addAll(app.resourceProviders);
      proguardMap = app.proguardMap;
      proguardSeeds = app.proguardSeeds;
      packageDistribution = app.packageDistribution;
      mainDexList = app.mainDexList;
    }

    /**
     * Add dex program files and proguard-map file located in @code{directory}.
     *
     * <p>The program files included are the top-level files ending in .dex and the proguard-map
     * file should it exist (see @code{DEFAULT_PROGUARD_MAP_FILE} for its assumed name).
     *
     * <p>This method is mostly a utility for reading in the file content produces by some external
     * tool, eg, dx.
     *
     * @param directory Directory containing dex program files and optional proguard-map file.
     */
    public Builder addProgramDirectory(Path directory) throws IOException {
      File[] resources = directory.toFile().listFiles(file -> isDexFile(file.toPath()));
      for (File source : resources) {
        addFile(source.toPath(), Resource.Kind.PROGRAM);
      }
      File mapFile = new File(directory.toFile(), DEFAULT_PROGUARD_MAP_FILE);
      if (mapFile.exists()) {
        setProguardMapFile(mapFile.toPath());
      }
      return this;
    }

    /**
     * Add program file resources.
     */
    public Builder addProgramFiles(Path... files) throws IOException {
      return addProgramFiles(Arrays.asList(files));
    }

    /**
     * Add program file resources.
     */
    public Builder addProgramFiles(Collection<Path> files) throws IOException {
      for (Path file : files) {
        addFile(file, Resource.Kind.PROGRAM);
      }
      return this;
    }

    /**
     * Add classpath file resources.
     */
    public Builder addClasspathFiles(Path... files) throws IOException {
      return addClasspathFiles(Arrays.asList(files));
    }

    /**
     * Add classpath file resources.
     */
    public Builder addClasspathFiles(Collection<Path> files) throws IOException {
      for (Path file : files) {
        addFile(file, Resource.Kind.CLASSPATH);
      }
      return this;
    }

    /**
     * Add classpath resource provider.
     */
    public Builder addClasspathResourceProvider(ResourceProvider provider) {
      resourceProviders.add(provider);
      return this;
    }

    /**
     * Add library file resources.
     */
    public Builder addLibraryFiles(Path... files) throws IOException {
      return addLibraryFiles(Arrays.asList(files));
    }

    /**
     * Add library file resources.
     */
    public Builder addLibraryFiles(Collection<Path> files) throws IOException {
      for (Path file : files) {
        addFile(file, Resource.Kind.LIBRARY);
      }
      return this;
    }

    /**
     * Add library resource provider.
     */
    public Builder addLibraryResourceProvider(ResourceProvider provider) {
      resourceProviders.add(provider);
      return this;
    }

    /**
     * Add dex program-data with class descriptor.
     */
    public Builder addDexProgramData(byte[] data, Set<String> classDescriptors) {
      dexSources.add(InternalResource.fromBytes(Resource.Kind.PROGRAM, data, classDescriptors));
      return this;
    }

    /**
     * Add dex program-data.
     */
    public Builder addDexProgramData(byte[]... data) {
      return addDexProgramData(Arrays.asList(data));
    }

    /**
     * Add dex program-data.
     */
    public Builder addDexProgramData(Collection<byte[]> data) {
      for (byte[] datum : data) {
        dexSources.add(InternalResource.fromBytes(Resource.Kind.PROGRAM, datum));
      }
      return this;
    }

    /**
     * Add Java-bytecode program data.
     */
    public Builder addClassProgramData(byte[]... data) {
      return addClassProgramData(Arrays.asList(data));
    }

    /**
     * Add Java-bytecode program data.
     */
    public Builder addClassProgramData(Collection<byte[]> data) {
      for (byte[] datum : data) {
        classSources.add(InternalResource.fromBytes(Resource.Kind.PROGRAM, datum));
      }
      return this;
    }

    /**
     * Set proguard-map file.
     */
    public Builder setProguardMapFile(Path file) {
      proguardMap = file == null ? null : InternalResource.fromFile(null, file);
      return this;
    }

    /**
     * Set proguard-map data.
     */
    public Builder setProguardMapData(String content) {
      return setProguardMapData(content == null ? null : content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Set proguard-map data.
     */
    public Builder setProguardMapData(byte[] content) {
      proguardMap = content == null ? null : InternalResource.fromBytes(null, content);
      return this;
    }

    /**
     * Set proguard-seeds data.
     */
    public Builder setProguardSeedsData(byte[] content) {
      proguardSeeds = content == null ? null : InternalResource.fromBytes(null, content);
      return this;
    }

    /**
     * Set the package-distribution file.
     */
    public Builder setPackageDistributionFile(Path file) {
      packageDistribution = file == null ? null : InternalResource.fromFile(null, file);
      return this;
    }

    /**
     * Set the main-dex list file.
     */
    public Builder setMainDexListFile(Path file) {
      mainDexList = file == null ? null : InternalResource.fromFile(null, file);
      return this;
    }

    /**
     * Build final AndroidApp.
     */
    public AndroidApp build() {
      return new AndroidApp(
          ImmutableList.copyOf(dexSources),
          ImmutableList.copyOf(classSources),
          ImmutableList.copyOf(resourceProviders),
          proguardMap,
          proguardSeeds,
          packageDistribution,
          mainDexList);
    }

    private void addFile(Path file, Resource.Kind kind) throws IOException {
      if (!Files.exists(file)) {
        throw new FileNotFoundException("Non-existent input file: " + file);
      }
      if (isDexFile(file)) {
        dexSources.add(InternalResource.fromFile(kind, file));
      } else if (isClassFile(file)) {
        classSources.add(InternalResource.fromFile(kind, file));
      } else if (isArchive(file)) {
        addArchive(file, kind);
      } else {
        throw new CompilationError("Unsupported source file type for file: " + file);
      }
    }

    private void addArchive(Path archive, Resource.Kind kind) throws IOException {
      assert isArchive(archive);
      boolean containsDexData = false;
      boolean containsClassData = false;
      try (ZipInputStream stream = new ZipInputStream(new FileInputStream(archive.toFile()))) {
        ZipEntry entry;
        while ((entry = stream.getNextEntry()) != null) {
          Path name = Paths.get(entry.getName());
          if (isDexFile(name)) {
            containsDexData = true;
            dexSources.add(InternalResource.fromBytes(kind, ByteStreams.toByteArray(stream)));
          } else if (isClassFile(name)) {
            containsClassData = true;
            String descriptor = PreloadedResourceProvider.guessTypeDescriptor(name);
            classSources.add(InternalResource.fromBytes(
                kind, ByteStreams.toByteArray(stream), Collections.singleton(descriptor)));
          }
        }
      } catch (ZipException e) {
        throw new CompilationError(
            "Zip error while reading '" + archive + "': " + e.getMessage(), e);
      }
      if (containsDexData && containsClassData) {
        throw new CompilationError(
            "Cannot create android app from an archive '" + archive
                + "' containing both DEX and Java-bytecode content");
      }
    }
  }
}
