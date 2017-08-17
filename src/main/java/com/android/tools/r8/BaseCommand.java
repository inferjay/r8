// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.FileUtils;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.OutputMode;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

abstract class BaseCommand {

  private final boolean printHelp;
  private final boolean printVersion;

  private final AndroidApp app;
  private final Path outputPath;
  private final OutputMode outputMode;
  private final CompilationMode mode;
  private final int minApiLevel;

  BaseCommand(boolean printHelp, boolean printVersion) {
    this.printHelp = printHelp;
    this.printVersion = printVersion;
    // All other fields are initialized with stub/invalid values.
    this.app = null;
    this.outputPath = null;
    this.outputMode = OutputMode.Indexed;
    this.mode = null;
    this.minApiLevel = 0;
  }

  BaseCommand(
      AndroidApp app,
      Path outputPath,
      OutputMode outputMode,
      CompilationMode mode,
      int minApiLevel) {
    assert app != null;
    assert mode != null;
    assert minApiLevel > 0;
    this.app = app;
    this.outputPath = outputPath;
    this.outputMode = outputMode;
    this.mode = mode;
    this.minApiLevel = minApiLevel;
    // Print options are not set.
    printHelp = false;
    printVersion = false;
  }

  public boolean isPrintHelp() {
    return printHelp;
  }

  public boolean isPrintVersion() {
    return printVersion;
  }

  // Internal access to the input resources.
  AndroidApp getInputApp() {
    return app;
  }

  // Internal access to the internal options.
  abstract InternalOptions getInternalOptions();

  public Path getOutputPath() {
    return outputPath;
  }

  public CompilationMode getMode() {
    return mode;
  }

  public int getMinApiLevel() {
    return minApiLevel;
  }

  public OutputMode getOutputMode() {
    return outputMode;
  }

  abstract static class Builder<C extends BaseCommand, B extends Builder<C, B>> {

    private boolean printHelp = false;
    private boolean printVersion = false;
    private final AndroidApp.Builder app;
    private Path outputPath = null;
    private OutputMode outputMode = OutputMode.Indexed;
    private CompilationMode mode;
    private int minApiLevel = Constants.DEFAULT_ANDROID_API;

    // Internal flag used by CompatDx to ignore dex files in archives.
    protected boolean ignoreDexInArchive = false;

    protected Builder(CompilationMode mode) {
      this(AndroidApp.builder(), mode);
    }

    // Internal constructor for testing.
    Builder(AndroidApp app, CompilationMode mode) {
      this(AndroidApp.builder(app), mode);
    }

    private Builder(AndroidApp.Builder builder, CompilationMode mode) {
      assert mode != null;
      this.app = builder;
      this.mode = mode;
      app.setIgnoreDexInArchive(ignoreDexInArchive);
    }

    abstract B self();

    public abstract C build() throws CompilationException, IOException;

    // Internal accessor for the application resources.
    AndroidApp.Builder getAppBuilder() {
      return app;
    }

    /** Add program file resources. */
    public B addProgramFiles(Path... files) throws IOException {
      app.addProgramFiles(files);
      return self();
    }

    /** Add program file resources. */
    public B addProgramFiles(Collection<Path> files) throws IOException {
      app.addProgramFiles(files);
      return self();
    }

    /** Add library file resource provider. */
    public B addLibraryResourceProvider(ClassFileResourceProvider provider) {
      getAppBuilder().addLibraryResourceProvider(provider);
      return self();
    }

    /** Add library file resources. */
    public B addLibraryFiles(Path... files) throws IOException {
      app.addLibraryFiles(files);
      return self();
    }

    /** Add library file resources. */
    public B addLibraryFiles(Collection<Path> files) throws IOException {
      app.addLibraryFiles(files);
      return self();
    }

    /** Add Java-bytecode program-data. */
    public B addClassProgramData(byte[]... data) {
      app.addClassProgramData(data);
      return self();
    }

    /** Add Java-bytecode program-data. */
    public B addClassProgramData(Collection<byte[]> data) {
      app.addClassProgramData(data);
      return self();
    }

    /** Add dex program-data. */
    public B addDexProgramData(byte[]... data) {
      app.addDexProgramData(data);
      return self();
    }

    /** Add dex program-data. */
    public B addDexProgramData(Collection<byte[]> data) {
      app.addDexProgramData(data);
      return self();
    }

    /** Get current compilation mode. */
    public CompilationMode getMode() {
      return mode;
    }

    /** Set compilation mode. */
    public B setMode(CompilationMode mode) {
      assert mode != null;
      this.mode = mode;
      return self();
    }

    /** Get the output path. Null if not set. */
    public Path getOutputPath() {
      return outputPath;
    }

    /** Get the output mode. */
    public OutputMode getOutputMode() {
      return outputMode;
    }

    /** Set an output path. Must be an existing directory or a zip file. */
    public B setOutputPath(Path outputPath) {
      this.outputPath = outputPath;
      return self();
    }

    /** Set an output mode. */
    public B setOutputMode(OutputMode outputMode) {
      this.outputMode = outputMode;
      return self();
    }

    /** Get the minimum API level (aka SDK version). */
    public int getMinApiLevel() {
      return minApiLevel;
    }

    /** Set the minimum required API level (aka SDK version). */
    public B setMinApiLevel(int minApiLevel) {
      assert minApiLevel > 0;
      this.minApiLevel = minApiLevel;
      return self();
    }

    /**
     * Add main-dex list files.
     *
     * Each line in each of the files specifies one class to keep in the primary dex file
     * (<code>classes.dex</code>).
     *
     * A class is specified using the following format: "com/example/MyClass.class". That is
     * "/" as separator between package components, and a trailing ".class".
     */
    public B addMainDexListFiles(Path... files) throws IOException {
      app.addMainDexListFiles(files);
      return self();
    }

    /**
     * Add main-dex list files.
     *
     * @see #addMainDexListFiles(Path...)
     */
    public B addMainDexListFiles(Collection<Path> files) throws IOException {
      app.addMainDexListFiles(files);
      return self();
    }

    /**
     * Add main-dex classes.
     *
     * Add classes to keep in the primary dex file (<code>classes.dex</code>).
     *
     * NOTE: The name of the classes is specified using the Java fully qualified names format
     * (e.g. "com.example.MyClass"), and <i>not</i> the format used by the main-dex list file.
     */
    public B addMainDexClasses(String... classes) {
      app.addMainDexClasses(classes);
      return self();
    }

    /**
     * Add main-dex classes.
     *
     * Add classes to keep in the primary dex file (<code>classes.dex</code>).
     *
     * NOTE: The name of the classes is specified using the Java fully qualified names format
     * (e.g. "com.example.MyClass"), and <i>not</i> the format used by the main-dex list file.
     */
    public B addMainDexClasses(Collection<String> classes) {
      app.addMainDexClasses(classes);
      return self();
    }

    /** True if the print-help flag is enabled. */
    public boolean isPrintHelp() {
      return printHelp;
    }

    /** Set the value of the print-help flag. */
    public B setPrintHelp(boolean printHelp) {
      this.printHelp = printHelp;
      return self();
    }

    /** True if the print-version flag is enabled. */
    public boolean isPrintVersion() {
      return printVersion;
    }

    /** Set the value of the print-version flag. */
    public B setPrintVersion(boolean printVersion) {
      this.printVersion = printVersion;
      return self();
    }

    protected void validate() throws CompilationException {
      if (app.hasMainDexList() && outputMode == OutputMode.FilePerClass) {
        throw new CompilationException(
            "Option --main-dex-list cannot be used with --file-per-class");
      }
      FileUtils.validateOutputFile(outputPath);
    }
  }
}
