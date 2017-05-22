// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.compatdx;

import static com.android.tools.r8.utils.FileUtils.isApkFile;
import static com.android.tools.r8.utils.FileUtils.isClassFile;
import static com.android.tools.r8.utils.FileUtils.isDexFile;
import static com.android.tools.r8.utils.FileUtils.isJarFile;
import static com.android.tools.r8.utils.FileUtils.isZipFile;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.CompilationMode;
import com.android.tools.r8.D8;
import com.android.tools.r8.D8Command;
import com.android.tools.r8.D8Output;
import com.android.tools.r8.compatdx.CompatDx.DxCompatOptions.DxUsageMessage;
import com.android.tools.r8.compatdx.CompatDx.DxCompatOptions.PositionInfo;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.errors.Unimplemented;
import com.android.tools.r8.logging.Log;
import com.android.tools.r8.utils.FileUtils;
import com.android.tools.r8.utils.ThreadUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Closer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 * Dx compatibility interface for d8.
 *
 * This should become a mostly drop-in replacement for uses of the DX dexer (eg, dx --dex ...).
 */
public class CompatDx {

  private static final String USAGE_HEADER = "Usage: compatdx [options] <input files>";

  /**
   * Compatibility options parsing for the DX --dex sub-command.
   */
  public static class DxCompatOptions {
    // Final values after parsing.
    // Note: These are ordered by their occurrence in "dx --help"
    public final boolean help;
    public final boolean debug;
    public final boolean verbose;
    public final PositionInfo positions;
    public final boolean noLocals;
    public final boolean noOptimize;
    public final boolean statistics;
    public final String optimizeList;
    public final String noOptimizeList;
    public final boolean noStrict;
    public final boolean keepClasses;
    public final String output;
    public final String dumpTo;
    public final int dumpWidth;
    public final String dumpMethod;
    public final boolean verboseDump;
    public final boolean noFiles;
    public final boolean coreLibrary;
    public final int numThreads;
    public final boolean incremental;
    public final boolean forceJumbo;
    public final boolean noWarning;
    public final boolean multiDex;
    public final String mainDexList;
    public final boolean minimalMainDex;
    public final int minApiLevel;
    public final String inputList;
    public final ImmutableList<String> inputs;
    // Undocumented option
    public final int maxIndexNumber;

    private static final String FILE_ARG = "file";
    private static final String NUM_ARG = "number";
    private static final String METHOD_ARG = "method";

    public enum PositionInfo {
      NONE, IMPORTANT, LINES
    }

    // Exception thrown on invalid dx compat usage.
    public static class DxUsageMessage extends Exception {
      public final String message;

      public DxUsageMessage() {
        this("");
      }

      DxUsageMessage(String message) {
        this.message = message;
      }

      void printHelpOn(PrintStream sink) throws IOException {
        sink.println(message);
      }
    }

    // Exception thrown on options parse error.
    private static class DxParseError extends DxUsageMessage {
      private final OptionParser parser;

      private DxParseError(OptionParser parser) {
        this.parser = parser;
      }

      @Override
      public void printHelpOn(PrintStream sink) throws IOException {
        parser.printHelpOn(sink);
      }
    }

    // Parsing specification.
    private static class Spec {
      final OptionParser parser;

      // Note: These are ordered by their occurrence in "dx --help"
      final OptionSpec<Void> debug;
      final OptionSpec<Void> verbose;
      final OptionSpec<String> positions;
      final OptionSpec<Void> noLocals;
      final OptionSpec<Void> noOptimize;
      final OptionSpec<Void> statistics;
      final OptionSpec<String> optimizeList;
      final OptionSpec<String> noOptimizeList;
      final OptionSpec<Void> noStrict;
      final OptionSpec<Void> keepClasses;
      final OptionSpec<String> output;
      final OptionSpec<String> dumpTo;
      final OptionSpec<Integer> dumpWidth;
      final OptionSpec<String> dumpMethod;
      final OptionSpec<Void> verboseDump;
      final OptionSpec<Void> noFiles;
      final OptionSpec<Void> coreLibrary;
      final OptionSpec<Integer> numThreads;
      final OptionSpec<Void> incremental;
      final OptionSpec<Void> forceJumbo;
      final OptionSpec<Void> noWarning;
      final OptionSpec<Void> multiDex;
      final OptionSpec<String> mainDexList;
      final OptionSpec<Void> minimalMainDex;
      final OptionSpec<Integer> minApiLevel;
      final OptionSpec<String> inputList;
      final OptionSpec<String> inputs;
      final OptionSpec<Void> help;
      final OptionSpec<Integer> maxIndexNumber;

      Spec() {
        parser = new OptionParser();
        parser.accepts("dex");
        debug = parser.accepts("debug", "Print debug information");
        verbose = parser.accepts("verbose", "Print verbose information");
        positions = parser
            .accepts("positions",
                "What source-position information to keep. One of: none, lines, important")
            .withOptionalArg()
            .describedAs("keep")
            .defaultsTo("lines");
        noLocals = parser.accepts("no-locals", "Don't keep local variable information");
        statistics = parser.accepts("statistics", "Print statistics information");
        noOptimize = parser.accepts("no-optimize", "Don't optimize");
        optimizeList = parser
            .accepts("optimize-list", "File listing methods to optimize")
            .withRequiredArg()
            .describedAs(FILE_ARG);
        noOptimizeList = parser
            .accepts("no-optimize-list", "File listing methods not to optimize")
            .withRequiredArg()
            .describedAs(FILE_ARG);
        noStrict = parser.accepts("no-strict", "Disable strict file/class name checks");
        keepClasses = parser.accepts("keep-classes", "Keep input class files in in output jar");
        output = parser
            .accepts("output", "Output file or directory")
            .withRequiredArg()
            .describedAs(FILE_ARG);
        dumpTo = parser
            .accepts("dump-to", "File to dump information to")
            .withRequiredArg()
            .describedAs(FILE_ARG);
        dumpWidth = parser
            .accepts("dump-width", "Max width for columns in dump output")
            .withRequiredArg()
            .ofType(Integer.class)
            .defaultsTo(0)
            .describedAs(NUM_ARG);
        dumpMethod = parser
            .accepts("dump-method", "Method to dump information for")
            .withRequiredArg()
            .describedAs(METHOD_ARG);
        verboseDump = parser.accepts("verbose-dump", "Dump verbose information");
        noFiles = parser.accepts("no-files", "Don't fail if given no files");
        coreLibrary = parser.accepts("core-library", "Construct a core library");
        numThreads = parser
            .accepts("num-threads", "Number of threads to run with")
            .withRequiredArg()
            .ofType(Integer.class)
            .defaultsTo(1)
            .describedAs(NUM_ARG);
        incremental = parser.accepts("incremental", "Merge result with the output if it exists");
        forceJumbo = parser.accepts("force-jumbo", "Force use of string-jumbo instructions");
        noWarning = parser.accepts("no-warning", "Suppress warnings");
        maxIndexNumber = parser.accepts("set-max-idx-number",
            "Undocumented: Set maximal index number to use in a dex file.")
            .withRequiredArg()
            .ofType(Integer.class)
            .defaultsTo(0)
            .describedAs("Maximum index");
        minimalMainDex = parser.accepts("minimal-main-dex", "Produce smallest possible main dex");
        mainDexList = parser
            .accepts("main-dex-list", "File listing classes that must be in the main dex file")
            .withRequiredArg()
            .describedAs(FILE_ARG);
        multiDex = parser.accepts("multi-dex", "Allow generation of multi-dex")
            .requiredIf(noStrict, minimalMainDex, mainDexList, maxIndexNumber);
        minApiLevel = parser
            .accepts("min-sdk-version", "Minimum Android API level compatibility.")
            .withRequiredArg().ofType(Integer.class);
        inputList = parser
            .accepts("input-list", "File listing input files")
            .withRequiredArg()
            .describedAs(FILE_ARG);
        inputs = parser.nonOptions("Input files");
        help = parser.accepts("help", "Print this message").forHelp();
      }
    }

    private DxCompatOptions(OptionSet options, Spec spec) throws DxParseError {
      help = options.has(spec.help);
      debug = options.has(spec.debug);
      verbose = options.has(spec.verbose);
      if (options.has(spec.positions)) {
        switch (options.valueOf(spec.positions)) {
          case "none":
            positions = PositionInfo.NONE;
            break;
          case "important":
            positions = PositionInfo.IMPORTANT;
            break;
          case "lines":
            positions = PositionInfo.LINES;
            break;
          default:
            // Should be checked by parser.
            throw new DxParseError(spec.parser);
        }
      } else {
        positions = PositionInfo.LINES;
      }
      noLocals = options.has(spec.noLocals);
      noOptimize = options.has(spec.noOptimize);
      statistics = options.has(spec.statistics);
      optimizeList = options.valueOf(spec.optimizeList);
      noOptimizeList = options.valueOf(spec.noOptimizeList);
      noStrict = options.has(spec.noStrict);
      keepClasses = options.has(spec.keepClasses);
      output = options.valueOf(spec.output);
      dumpTo = options.valueOf(spec.dumpTo);
      dumpWidth = options.valueOf(spec.dumpWidth);
      dumpMethod = options.valueOf(spec.dumpMethod);
      verboseDump = options.has(spec.verboseDump);
      noFiles = options.has(spec.noFiles);
      coreLibrary = options.has(spec.coreLibrary);
      numThreads = options.valueOf(spec.numThreads);
      incremental = options.has(spec.incremental);
      forceJumbo = options.has(spec.forceJumbo);
      noWarning = options.has(spec.noWarning);
      multiDex = options.has(spec.multiDex);
      mainDexList = options.valueOf(spec.mainDexList);
      minimalMainDex = options.has(spec.minimalMainDex);
      minApiLevel = options.has(spec.minApiLevel)
              ? options.valueOf(spec.minApiLevel)
              : Constants.DEFAULT_ANDROID_API;
      inputList = options.valueOf(spec.inputList);
      inputs = ImmutableList.copyOf(options.valuesOf(spec.inputs));
      maxIndexNumber = options.valueOf(spec.maxIndexNumber);
    }

    public static DxCompatOptions parse(String[] args) throws DxParseError {
      Spec spec = new Spec();
      return new DxCompatOptions(spec.parser.parse(args), spec);
    }
  }

  public static void main(String[] args) throws IOException {
    try {
      run(args);
    } catch (CompilationException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    } catch (DxUsageMessage e) {
      System.err.println(USAGE_HEADER);
      e.printHelpOn(System.err);
      System.exit(1);
    }
  }

  private static void run(String[] args) throws DxUsageMessage, IOException, CompilationException {
    DxCompatOptions dexArgs = DxCompatOptions.parse(args);
    if (dexArgs.help) {
      printHelpOn(System.out);
      return;
    }
    CompilationMode mode = CompilationMode.RELEASE;
    Path output = null;
    List<Path> inputs = new ArrayList<>();
    boolean singleDexFile = !dexArgs.multiDex;
    Path mainDexList = null;
    int numberOfThreads = 1;

    for (String path : dexArgs.inputs) {
      processPath(new File(path), inputs);
    }
    if (inputs.isEmpty()) {
      if (dexArgs.noFiles) {
        return;
      }
      throw new DxUsageMessage("No input files specified");
    }

    if (!Log.ENABLED && (!dexArgs.noWarning || dexArgs.debug || dexArgs.verbose)) {
      System.out.println("Warning: logging is not enabled for this build.");
    }

    if (dexArgs.verboseDump) {
      throw new Unimplemented("verbose dump file not yet supported");
    }

    if (dexArgs.dumpMethod != null) {
      throw new Unimplemented("method-dump not yet supported");
    }

    if (dexArgs.output != null) {
      output = Paths.get(dexArgs.output);
      if (FileUtils.isDexFile(output)) {
        if (!singleDexFile) {
          throw new DxUsageMessage("Cannot output to a single dex-file when running with multidex");
        }
      } else if (!FileUtils.isArchive(output)
          && (!output.toFile().exists() || !output.toFile().isDirectory())) {
        throw new DxUsageMessage("Unsupported output file or output directory does not exist. "
            + "Output must be a directory or a file of type dex, apk, jar or zip.");
      }
    }

    if (dexArgs.dumpTo != null) {
      throw new Unimplemented("dump-to file not yet supported");
    }

    if (dexArgs.keepClasses) {
      throw new Unimplemented("keeping classes in jar not yet supported");
    }

    if (dexArgs.positions != PositionInfo.NONE) {
      mode = CompilationMode.DEBUG;
    }

    if (!dexArgs.noLocals) {
      mode = CompilationMode.DEBUG;
    }

    if (dexArgs.incremental) {
      throw new Unimplemented("incremental merge not supported yet");
    }

    if (dexArgs.forceJumbo) {
      throw new Unimplemented("force jumbo not yet supported");
    }

    if (dexArgs.noOptimize) {
      System.out.println("Warning: no support for not optimizing yet");
    }

    if (dexArgs.optimizeList != null) {
      throw new Unimplemented("no support for optimize-method list");
    }

    if (dexArgs.noOptimizeList != null) {
      throw new Unimplemented("no support for dont-optimize-method list");
    }

    if (dexArgs.statistics) {
      throw new Unimplemented("no support for printing statistics yet");
    }

    if (dexArgs.numThreads > 1) {
      numberOfThreads = dexArgs.numThreads;
    }

    if (dexArgs.mainDexList != null) {
      mainDexList = Paths.get(dexArgs.mainDexList);
    }

    if (dexArgs.noStrict) {
      System.out.println("Warning: conservative main-dex list not yet supported");
    }

    if (dexArgs.minimalMainDex) {
      System.out.println("Warning: minimal main-dex support is not yet supported");
    }

    if (dexArgs.maxIndexNumber != 0) {
      System.out.println("Warning: internal maximum-index setting is not supported");
    }

    if (numberOfThreads < 1) {
      throw new DxUsageMessage("Invalid numThreads value of " + numberOfThreads);
    }
    ExecutorService executor = ThreadUtils.getExecutorService(numberOfThreads);
    D8Output result;
    try {
       result = D8.run(
          D8Command.builder()
              .addProgramFiles(inputs)
              .setMode(mode)
              .setMinApiLevel(dexArgs.minApiLevel)
              .setMainDexListFile(mainDexList)
              .build());
    } finally {
      executor.shutdown();
    }

    if (output == null) {
      return;
    }

    if (singleDexFile) {
      if (result.getDexResources().size() > 1) {
        throw new CompilationError(
            "Compilation result could not fit into a single dex file. "
                + "Reduce the input-program size or run with --multi-dex enabled");
      }
      if (isDexFile(output)) {
        try (Closer closer = Closer.create()) {
          InputStream stream = result.getDexResources().get(0).getStream(closer);
          Files.copy(stream, output, StandardCopyOption.REPLACE_EXISTING);
        }
        return;
      }
    }

    result.write(output);
  }

  static void printHelpOn(PrintStream sink) throws IOException {
    sink.println(USAGE_HEADER);
    new DxCompatOptions.Spec().parser.printHelpOn(sink);
  }

  private static void processPath(File file, List<Path> files) {
    if (!file.exists()) {
      throw new CompilationError("File does not exist: " + file);
    }
    if (file.isDirectory()) {
      processDirectory(file, files);
      return;
    }
    Path path = file.toPath();
    if (isZipFile(path) || isJarFile(path) || isClassFile(path)) {
      files.add(path);
      return;
    }
    if (isApkFile(path)) {
      throw new Unimplemented("apk files not yet supported");
    }
  }

  private static void processDirectory(File directory, List<Path> files) {
    assert directory.exists();
    for (File file : directory.listFiles()) {
      processPath(file, files);
    }
  }
}
