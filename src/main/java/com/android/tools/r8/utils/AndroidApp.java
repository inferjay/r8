// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import static com.android.tools.r8.utils.FileUtils.isArchive;
import static com.android.tools.r8.utils.FileUtils.isClassFile;
import static com.android.tools.r8.utils.FileUtils.isDexFile;

import com.android.tools.r8.ClassFileResourceProvider;
import com.android.tools.r8.Resource;
import com.android.tools.r8.errors.CompilationError;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Collection of program files needed for processing.
 *
 * <p>This abstraction is the main input and output container for a given application.
 */
public class AndroidApp {

  public static final String DEFAULT_PROGUARD_MAP_FILE = "proguard.map";

  private final ImmutableList<Resource> programResources;
  private final ImmutableList<ClassFileResourceProvider> classpathResourceProviders;
  private final ImmutableList<ClassFileResourceProvider> libraryResourceProviders;
  private final ImmutableList<ProgramFileArchiveReader> programFileArchiveReaders;
  private final Resource deadCode;
  private final Resource proguardMap;
  private final Resource proguardSeeds;
  private final Resource packageDistribution;
  private final List<Resource> mainDexListResources;
  private final List<String> mainDexClasses;
  private final Resource mainDexListOutput;

  // See factory methods and AndroidApp.Builder below.
  private AndroidApp(
      ImmutableList<Resource> programResources,
      ImmutableList<ProgramFileArchiveReader> programFileArchiveReaders,
      ImmutableList<ClassFileResourceProvider> classpathResourceProviders,
      ImmutableList<ClassFileResourceProvider> libraryResourceProviders,
      Resource deadCode,
      Resource proguardMap,
      Resource proguardSeeds,
      Resource packageDistribution,
      List<Resource> mainDexListResources,
      List<String> mainDexClasses,
      Resource mainDexListOutput) {
    this.programResources = programResources;
    this.programFileArchiveReaders = programFileArchiveReaders;
    this.classpathResourceProviders = classpathResourceProviders;
    this.libraryResourceProviders = libraryResourceProviders;
    this.deadCode = deadCode;
    this.proguardMap = proguardMap;
    this.proguardSeeds = proguardSeeds;
    this.packageDistribution = packageDistribution;
    this.mainDexListResources = mainDexListResources;
    this.mainDexClasses = mainDexClasses;
    this.mainDexListOutput = mainDexListOutput;
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
  public List<Resource> getDexProgramResources() throws IOException {
    List<Resource> dexResources = filter(programResources, Resource.Kind.DEX);
    for (ProgramFileArchiveReader reader : programFileArchiveReaders) {
      dexResources.addAll(reader.getDexProgramResources());
    }
    return dexResources;
  }

  public List<Resource> getDexProgramResourcesForOutput() {
    assert programFileArchiveReaders.isEmpty();
    return filter(programResources, Resource.Kind.DEX);
  }

  /** Get input streams for all Java-bytecode program resources. */
  public List<Resource> getClassProgramResources() throws IOException {
    List<Resource> classResources = filter(programResources, Resource.Kind.CLASSFILE);
    for (ProgramFileArchiveReader reader : programFileArchiveReaders) {
      classResources.addAll(reader.getClassProgramResources());
    }
    return classResources;
  }

  /** Get classpath resource providers. */
  public List<ClassFileResourceProvider> getClasspathResourceProviders() {
    return classpathResourceProviders;
  }

  /** Get library resource providers. */
  public List<ClassFileResourceProvider> getLibraryResourceProviders() {
    return libraryResourceProviders;
  }

  private List<Resource> filter(List<Resource> resources, Resource.Kind kind) {
    List<Resource> out = new ArrayList<>(resources.size());
    for (Resource resource : resources) {
      if (kind == resource.kind) {
        out.add(resource);
      }
    }
    return out;
  }

  /**
   * True if the dead-code resource exists.
   */
  public boolean hasDeadCode() {
    return deadCode != null;
  }

  /**
   * Get the input stream of the dead-code resource if exists.
   */
  public InputStream getDeadCode(Closer closer) throws IOException {
    return deadCode == null ? null : closer.register(deadCode.getStream());
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
    return proguardMap == null ? null : closer.register(proguardMap.getStream());
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
    return proguardSeeds == null ? null : closer.register(proguardSeeds.getStream());
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
    return packageDistribution == null ? null : closer.register(packageDistribution.getStream());
  }

  /**
   * True if the main dex list resources exists.
   */
  public boolean hasMainDexList() {
    return !(mainDexListResources.isEmpty() && mainDexClasses.isEmpty());
  }

  /**
   * True if the main dex list resources exists.
   */
  public boolean hasMainDexListResources() {
    return !mainDexListResources.isEmpty();
  }

  /**
   * Get the main dex list resources if any.
   */
  public List<Resource> getMainDexListResources() {
    return mainDexListResources;
  }

  /**
   * Get the main dex classes if any.
   */
  public List<String> getMainDexClasses() {
    return mainDexClasses;
  }

  /**
   * True if the main dex list resource exists.
   */
  public boolean hasMainDexListOutput() {
    return mainDexListOutput != null;
  }

  /**
   * Get the main dex list output resources if any.
   */
  public InputStream getMainDexListOutput(Closer closer) throws IOException {
    return mainDexListOutput == null ? null : closer.register(mainDexListOutput.getStream());
  }

  /**
   * Write the dex program resources and proguard resource to @code{output}.
   */
  public void write(Path output, OutputMode outputMode) throws IOException {
    if (isArchive(output)) {
      writeToZip(output, outputMode);
    } else {
      writeToDirectory(output, outputMode);
    }
  }

  /**
   * Write the dex program resources and proguard resource to @code{directory}.
   */
  public void writeToDirectory(Path directory, OutputMode outputMode) throws IOException {
    if (outputMode == OutputMode.Indexed) {
      for (Path path : Files.list(directory).collect(Collectors.toList())) {
        if (isClassesDexFile(path)) {
          Files.delete(path);
        }
      }
    }
    CopyOption[] options = new CopyOption[] {StandardCopyOption.REPLACE_EXISTING};
    try (Closer closer = Closer.create()) {
      List<Resource> dexProgramSources = getDexProgramResources();
      for (int i = 0; i < dexProgramSources.size(); i++) {
        Path filePath = directory.resolve(outputMode.getOutputPath(dexProgramSources.get(i), i));
        if (!Files.exists(filePath.getParent())) {
          Files.createDirectories(filePath.getParent());
        }
        Files.copy(closer.register(dexProgramSources.get(i).getStream()), filePath, options);
      }
    }
  }

  private static boolean isClassesDexFile(Path file) {
    String name = file.getFileName().toString().toLowerCase();
    if (!name.startsWith("classes") || !name.endsWith(".dex")) {
      return false;
    }
    String numeral = name.substring("classes".length(), name.length() - ".dex".length());
    if (numeral.isEmpty()) {
      return true;
    }
    char c0 = numeral.charAt(0);
    if (numeral.length() == 1) {
      return '2' <= c0 && c0 <= '9';
    }
    if (c0 < '1' || '9' < c0) {
      return false;
    }
    for (int i = 1; i < numeral.length(); i++) {
      char c = numeral.charAt(i);
      if (c < '0' || '9' < c) {
        return false;
      }
    }
    return true;
  }

  public List<byte[]> writeToMemory() throws IOException {
    List<byte[]> dex = new ArrayList<>();
    try (Closer closer = Closer.create()) {
      List<Resource> dexProgramSources = getDexProgramResources();
      for (int i = 0; i < dexProgramSources.size(); i++) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteStreams.copy(closer.register(dexProgramSources.get(i).getStream()), out);
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
    OpenOption[] options =
        new OpenOption[] {StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};
    try (Closer closer = Closer.create()) {
      try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(archive, options))) {
        List<Resource> dexProgramSources = getDexProgramResources();
        for (int i = 0; i < dexProgramSources.size(); i++) {
          ZipEntry zipEntry = new ZipEntry(outputMode.getOutputPath(dexProgramSources.get(i), i));
          byte[] bytes =
              ByteStreams.toByteArray(closer.register(dexProgramSources.get(i).getStream()));
          zipEntry.setSize(bytes.length);
          out.putNextEntry(zipEntry);
          out.write(bytes);
          out.closeEntry();
        }
      }
    }
  }

  public void writeProguardMap(Closer closer, OutputStream out) throws IOException {
    InputStream input = getProguardMap(closer);
    assert input != null;
    out.write(ByteStreams.toByteArray(input));
  }

  public void writeProguardSeeds(Closer closer, OutputStream out) throws IOException {
    InputStream input = getProguardSeeds(closer);
    assert input != null;
    out.write(ByteStreams.toByteArray(input));
  }

  public void writeMainDexList(Closer closer, OutputStream out) throws IOException {
    InputStream input = getMainDexListOutput(closer);
    assert input != null;
    out.write(ByteStreams.toByteArray(input));
  }

  public void writeDeadCode(Closer closer, OutputStream out) throws IOException {
    InputStream input = getDeadCode(closer);
    assert input != null;
    out.write(ByteStreams.toByteArray(input));
  }

  /**
   * Builder interface for constructing an AndroidApp.
   */
  public static class Builder {

    private final List<Resource> programResources = new ArrayList<>();
    private final List<ProgramFileArchiveReader> programFileArchiveReaders = new ArrayList<>();
    private final List<ClassFileResourceProvider> classpathResourceProviders = new ArrayList<>();
    private final List<ClassFileResourceProvider> libraryResourceProviders = new ArrayList<>();
    private Resource deadCode;
    private Resource proguardMap;
    private Resource proguardSeeds;
    private Resource packageDistribution;
    private List<Resource> mainDexListResources = new ArrayList<>();
    private List<String> mainDexListClasses = new ArrayList<>();
    private Resource mainDexListOutput;

    // See AndroidApp::builder().
    private Builder() {
    }

    // See AndroidApp::builder(AndroidApp).
    private Builder(AndroidApp app) {
      programResources.addAll(app.programResources);
      programFileArchiveReaders.addAll(app.programFileArchiveReaders);
      classpathResourceProviders.addAll(app.classpathResourceProviders);
      libraryResourceProviders.addAll(app.libraryResourceProviders);
      deadCode = app.deadCode;
      proguardMap = app.proguardMap;
      proguardSeeds = app.proguardSeeds;
      packageDistribution = app.packageDistribution;
      mainDexListResources = app.mainDexListResources;
      mainDexListClasses = app.mainDexClasses;
      mainDexListOutput = app.mainDexListOutput;
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
      List<Path> resources =
          Arrays.asList(directory.toFile().listFiles(file -> isDexFile(file.toPath()))).stream()
          .map(file -> file.toPath()).collect(Collectors.toList());
      addProgramFiles(resources);
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
        addProgramFile(file);
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
        addClassProvider(file, classpathResourceProviders);
      }
      return this;
    }

    /**
     * Add classpath resource provider.
     */
    public Builder addClasspathResourceProvider(ClassFileResourceProvider provider) {
      classpathResourceProviders.add(provider);
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
        addClassProvider(file, libraryResourceProviders);
      }
      return this;
    }

    /**
     * Add library resource provider.
     */
    public Builder addLibraryResourceProvider(ClassFileResourceProvider provider) {
      libraryResourceProviders.add(provider);
      return this;
    }

    /**
     * Add dex program-data with class descriptor.
     */
    public Builder addDexProgramData(byte[] data, Set<String> classDescriptors) {
      programResources.add(
          Resource.fromBytes(Resource.Kind.DEX, data, classDescriptors));
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
        programResources.add(Resource.fromBytes(Resource.Kind.DEX, datum));
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
        programResources.add(Resource.fromBytes(Resource.Kind.CLASSFILE, datum));
      }
      return this;
    }

    /**
     * Set dead-code data.
     */
    public Builder setDeadCode(byte[] content) {
      deadCode = content == null ? null : Resource.fromBytes(null, content);
      return this;
    }

    /**
     * Set proguard-map file.
     */
    public Builder setProguardMapFile(Path file) {
      proguardMap = file == null ? null : Resource.fromFile(null, file);
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
      proguardMap = content == null ? null : Resource.fromBytes(null, content);
      return this;
    }

    /**
     * Set proguard-seeds data.
     */
    public Builder setProguardSeedsData(byte[] content) {
      proguardSeeds = content == null ? null : Resource.fromBytes(null, content);
      return this;
    }

    /**
     * Set the package-distribution file.
     */
    public Builder setPackageDistributionFile(Path file) {
      packageDistribution = file == null ? null : Resource.fromFile(null, file);
      return this;
    }

    /**
     * Add a main-dex list file.
     */
    public Builder addMainDexListFiles(Path... files) throws IOException {
      return addMainDexListFiles(Arrays.asList(files));
    }

    public Builder addMainDexListFiles(Collection<Path> files) throws IOException {
      for (Path file : files) {
        if (!Files.exists(file)) {
          throw new FileNotFoundException("Non-existent input file: " + file);
        }
        // TODO(sgjesse): Should we just read the file here? This will sacrifice the parallelism
        // in ApplicationReader where all input resources are read in parallel.
        mainDexListResources.add(Resource.fromFile(null, file));
      }
      return this;
    }


    /**
     * Add main-dex classes.
     */
    public Builder addMainDexClasses(String... classes) {
      return addMainDexClasses(Arrays.asList(classes));
    }

    /**
     * Add main-dex classes.
     */
    public Builder addMainDexClasses(Collection<String> classes) {
      mainDexListClasses.addAll(classes);
      return this;
    }

    public boolean hasMainDexList() {
      return !(mainDexListResources.isEmpty() && mainDexListClasses.isEmpty());
    }

    /**
     * Set the main-dex list output data.
     */
    public Builder setMainDexListOutputData(byte[] content) {
      mainDexListOutput = content == null ? null : Resource.fromBytes(null, content);
      return this;
    }

    /**
     * Build final AndroidApp.
     */
    public AndroidApp build() {
      return new AndroidApp(
          ImmutableList.copyOf(programResources),
          ImmutableList.copyOf(programFileArchiveReaders),
          ImmutableList.copyOf(classpathResourceProviders),
          ImmutableList.copyOf(libraryResourceProviders),
          deadCode,
          proguardMap,
          proguardSeeds,
          packageDistribution,
          mainDexListResources,
          mainDexListClasses,
          mainDexListOutput);
    }

    private void addProgramFile(Path file) throws IOException {
      if (!Files.exists(file)) {
        throw new FileNotFoundException("Non-existent input file: " + file);
      }
      if (isDexFile(file)) {
        programResources.add(Resource.fromFile(Resource.Kind.DEX, file));
      } else if (isClassFile(file)) {
        programResources.add(Resource.fromFile(Resource.Kind.CLASSFILE, file));
      } else if (isArchive(file)) {
        programFileArchiveReaders.add(new ProgramFileArchiveReader(file));
      } else {
        throw new CompilationError("Unsupported source file type for file: " + file);
      }
    }

    private void addClassProvider(Path file, List<ClassFileResourceProvider> providerList)
        throws IOException {
      if (!Files.exists(file)) {
        throw new FileNotFoundException("Non-existent input file: " + file);
      }
      if (isArchive(file)) {
        providerList.add(PreloadedClassFileProvider.fromArchive(file));
      } else if (Files.isDirectory(file) ) {
        providerList.add(DirectoryClassFileProvider.fromDirectory(file));
      } else {
        throw new CompilationError("Unsupported source file type for file: " + file);
      }
    }
  }
}
