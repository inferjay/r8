// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import static com.android.tools.r8.R8Command.USAGE_MESSAGE;

import com.android.tools.r8.dex.ApplicationReader;
import com.android.tools.r8.dex.ApplicationWriter;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.dex.Marker;
import com.android.tools.r8.dex.Marker.Tool;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.errors.MainDexError;
import com.android.tools.r8.graph.AppInfo;
import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.ClassAndMemberPublicizer;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.ir.conversion.IRConverter;
import com.android.tools.r8.ir.optimize.EnumOrdinalMapCollector;
import com.android.tools.r8.ir.optimize.SwitchMapCollector;
import com.android.tools.r8.naming.Minifier;
import com.android.tools.r8.naming.NamingLens;
import com.android.tools.r8.optimize.BridgeMethodAnalysis;
import com.android.tools.r8.optimize.DebugStripper;
import com.android.tools.r8.optimize.MemberRebindingAnalysis;
import com.android.tools.r8.optimize.VisibilityBridgeRemover;
import com.android.tools.r8.shaking.AbstractMethodRemover;
import com.android.tools.r8.shaking.AnnotationRemover;
import com.android.tools.r8.shaking.DiscardedChecker;
import com.android.tools.r8.shaking.Enqueuer;
import com.android.tools.r8.shaking.MainDexListBuilder;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.android.tools.r8.shaking.ProguardTypeMatcher;
import com.android.tools.r8.shaking.ProguardTypeMatcher.MatchSpecificType;
import com.android.tools.r8.shaking.ReasonPrinter;
import com.android.tools.r8.shaking.RootSetBuilder;
import com.android.tools.r8.shaking.RootSetBuilder.RootSet;
import com.android.tools.r8.shaking.SimpleClassMerger;
import com.android.tools.r8.shaking.TreePruner;
import com.android.tools.r8.shaking.protolite.ProtoLiteExtension;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.CfgPrinter;
import com.android.tools.r8.utils.FileUtils;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.PackageDistribution;
import com.android.tools.r8.utils.ThreadUtils;
import com.android.tools.r8.utils.Timing;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class R8 {

  private final Timing timing = new Timing("R8");
  private final InternalOptions options;

  private R8(InternalOptions options) {
    this.options = options;
    options.itemFactory.resetSortedIndices();
  }

  // Compute the marker to be placed in the main dex file.
  private static Marker getMarker(InternalOptions options) {
    if (options.customizedMarker != null) {
      return options.customizedMarker;
    }
    return new Marker(Tool.R8);
  }

  public static AndroidApp writeApplication(
      ExecutorService executorService,
      DexApplication application,
      AppInfo appInfo,
      byte[] deadCode,
      NamingLens namingLens,
      byte[] proguardSeedsData,
      PackageDistribution packageDistribution,
      InternalOptions options)
      throws ExecutionException {
    try {
      Marker marker = getMarker(options);
      return new ApplicationWriter(
          application, appInfo, options, marker, deadCode, namingLens, proguardSeedsData)
          .write(packageDistribution, executorService);
    } catch (IOException e) {
      throw new RuntimeException("Cannot write dex application", e);
    }
  }

  static DexApplication optimize(
      DexApplication application,
      AppInfoWithSubtyping appInfo,
      InternalOptions options)
      throws ProguardRuleParserException, ExecutionException, IOException {
    return new R8(options).optimize(application, appInfo);
  }

  private DexApplication optimize(DexApplication application, AppInfoWithSubtyping appInfo)
      throws IOException, ProguardRuleParserException, ExecutionException {
    return optimize(application, appInfo, GraphLense.getIdentityLense(),
        Executors.newSingleThreadExecutor());
  }

  private DexApplication optimize(
      DexApplication application,
      AppInfoWithSubtyping appInfo,
      GraphLense graphLense,
      ExecutorService executorService)
      throws IOException, ProguardRuleParserException, ExecutionException {
    final CfgPrinter printer = options.printCfg ? new CfgPrinter() : null;

    timing.begin("Create IR");
    try {
      IRConverter converter = new IRConverter(
          timing, application, appInfo, options, printer, graphLense);
      application = converter.optimize(executorService);
    } finally {
      timing.end();
    }

    if (!options.skipDebugInfoOpt && (application.getProguardMap() != null)) {
      try {
        timing.begin("DebugStripper");
        DebugStripper stripper =
            new DebugStripper(application.getProguardMap(), options, appInfo.dexItemFactory);
        application.classes().forEach(stripper::processClass);
      } finally {
        timing.end();
      }
    }

    if (options.printCfg) {
      if (options.printCfgFile == null || options.printCfgFile.isEmpty()) {
        System.out.print(printer.toString());
      } else {
        try (OutputStreamWriter writer = new OutputStreamWriter(
            new FileOutputStream(options.printCfgFile),
            StandardCharsets.UTF_8)) {
          writer.write(printer.toString());
        }
      }
    }
    return application;
  }

  private Set<DexType> filterMissingClasses(Set<DexType> missingClasses,
      Set<ProguardTypeMatcher> dontWarnPatterns) {
    Set<DexType> result = new HashSet<>(missingClasses);
    for (ProguardTypeMatcher matcher : dontWarnPatterns) {
      if (matcher instanceof MatchSpecificType) {
        result.remove(((MatchSpecificType) matcher).type);
      } else {
        result.removeIf(matcher::matches);
      }
    }
    return result;
  }

  public static void disassemble(Disassemble.DisassembleCommand command)
      throws IOException, ExecutionException {
    Path output = command.getOutputPath();
    AndroidApp app = command.getInputApp();
    InternalOptions options = command.getInternalOptions();
    ExecutorService executor = ThreadUtils.getExecutorService(options);
    Timing timing = new Timing("disassemble");
    try {
      DexApplication application = new ApplicationReader(app, options, timing).read(executor);
      if (options.useSmaliSyntax) {
        if (output != null) {
          Files.createDirectories(output);
          try (PrintStream ps = new PrintStream(
              Files.newOutputStream(output.resolve("classes.smali")))) {
            application.smali(options, ps);
          }
        } else {
          application.smali(options, System.out);
        }
      } else {
        application.disassemble(output, options);
      }
    } finally {
      executor.shutdown();
    }
  }

  static CompilationResult runForTesting(AndroidApp app, InternalOptions options)
      throws ProguardRuleParserException, IOException {
    ExecutorService executor = ThreadUtils.getExecutorService(options);
    try {
      return runForTesting(app, options, executor);
    } finally {
      executor.shutdown();
    }
  }

  private static CompilationResult runForTesting(
      AndroidApp app,
      InternalOptions options,
      ExecutorService executor)
      throws ProguardRuleParserException, IOException {
    return new R8(options).run(app, executor);
  }

  private CompilationResult run(AndroidApp inputApp, ExecutorService executorService)
      throws IOException, ProguardRuleParserException {
    if (options.quiet) {
      System.setOut(new PrintStream(ByteStreams.nullOutputStream()));
    }
    try {
      if (options.minApiLevel >= Constants.ANDROID_O_API
          && !options.mainDexKeepRules.isEmpty()) {
        throw new CompilationError("Automatic main dex list is not supported when compiling for"
            + " android O and later (--min-api " + Constants.ANDROID_O_API + ")");
      }
      DexApplication application =
          new ApplicationReader(inputApp, options, timing).read(executorService);

      AppInfoWithSubtyping appInfo = new AppInfoWithSubtyping(application);
      RootSet rootSet;
      byte[] proguardSeedsData = null;
      timing.begin("Strip unused code");
      try {
        Set<DexType> missingClasses = appInfo.getMissingClasses();
        missingClasses = filterMissingClasses(missingClasses, options.dontWarnPatterns);
        if (!missingClasses.isEmpty()) {
          System.err.println();
          System.err.println("WARNING, some classes are missing:");
          missingClasses.forEach(clazz -> {
            System.err.println(" - " + clazz.toSourceString());
          });
          if (!options.ignoreMissingClasses) {
            throw new CompilationError(
                "Shrinking can't be performed because some library classes are missing.");
          }
        }
        rootSet = new RootSetBuilder(application, appInfo, options.keepRules).run(executorService);
        Enqueuer enqueuer = new Enqueuer(appInfo);
        enqueuer.addExtension(new ProtoLiteExtension(appInfo));
        appInfo = enqueuer.traceApplication(rootSet, timing);
        if (options.printSeeds) {
          ByteArrayOutputStream bytes = new ByteArrayOutputStream();
          PrintStream out = new PrintStream(bytes);
          RootSetBuilder.writeSeeds(appInfo.withLiveness().pinnedItems, out);
          out.flush();
          proguardSeedsData = bytes.toByteArray();
        }
        if (options.useTreeShaking) {
          application = new TreePruner(application, appInfo.withLiveness(), options).run();
          // Recompute the subtyping information.
          appInfo = appInfo.withLiveness().prunedCopyFrom(application);
          new AbstractMethodRemover(appInfo).run();
          new AnnotationRemover(appInfo.withLiveness(), options).run();
        }
      } finally {
        timing.end();
      }

      if (options.allowAccessModification) {
        ClassAndMemberPublicizer.run(application);
        // We can now remove visibility bridges. Note that we do not need to update the
        // invoke-targets here, as the existing invokes will simply dispatch to the now
        // visible super-method. MemberRebinding, if run, will then dispatch it correctly.
        application = new VisibilityBridgeRemover(appInfo, application).run();
      }

      GraphLense graphLense = GraphLense.getIdentityLense();

      if (appInfo.hasLiveness()) {
        graphLense = new MemberRebindingAnalysis(appInfo.withLiveness(), graphLense).run();
        // Class merging requires inlining.
        if (!options.skipClassMerging && options.inlineAccessors) {
          timing.begin("ClassMerger");
          graphLense = new SimpleClassMerger(application, appInfo.withLiveness(), graphLense,
              timing).run();
          timing.end();
        }
        appInfo = appInfo.withLiveness().prunedCopyFrom(application);
        appInfo = appInfo.withLiveness().rewrittenWithLense(graphLense);
        // Collect switch maps and ordinals maps.
        new SwitchMapCollector(appInfo.withLiveness(), options).run();
        new EnumOrdinalMapCollector(appInfo.withLiveness(), options).run();
      }

      graphLense = new BridgeMethodAnalysis(graphLense, appInfo.withSubtyping()).run();

      application = optimize(application, appInfo, graphLense, executorService);

      if (!options.mainDexKeepRules.isEmpty()) {
        appInfo = new AppInfoWithSubtyping(application);
        Enqueuer enqueuer = new Enqueuer(appInfo);
        // Lets find classes which may have code executed before secondary dex files installation.
        RootSet mainDexRootSet =
            new RootSetBuilder(application, appInfo, options.mainDexKeepRules).run(executorService);
        Set<DexType> mainDexBaseClasses = enqueuer.traceMainDex(mainDexRootSet, timing);

        // Calculate the automatic main dex list according to legacy multidex constraints.
        // Add those classes to an eventual manual list of classes.
        application = new DexApplication.Builder(application)
            .addToMainDexList(new MainDexListBuilder(mainDexBaseClasses, application).run())
            .build();
      }

      appInfo = new AppInfoWithSubtyping(application);

      if (options.useTreeShaking || !options.skipMinification) {
        timing.begin("Post optimization code stripping");
        try {
          Enqueuer enqueuer = new Enqueuer(appInfo);
          appInfo = enqueuer.traceApplication(rootSet, timing);
          if (options.useTreeShaking) {
            application = new TreePruner(application, appInfo.withLiveness(), options).run();
            appInfo = appInfo.withLiveness().prunedCopyFrom(application);
            // Print reasons on the application after pruning, so that we reflect the actual result.
            ReasonPrinter reasonPrinter = enqueuer.getReasonPrinter(rootSet.reasonAsked);
            reasonPrinter.run(application);
          }
        } finally {
          timing.end();
        }
      }

      // Only perform discard-checking if tree-shaking is turned on.
      if (options.useTreeShaking && !rootSet.checkDiscarded.isEmpty()) {
        new DiscardedChecker(rootSet, application).run();
      }

      timing.begin("Minification");
      // If we did not have keep rules, everything will be marked as keep, so no minification
      // will happen. Just avoid the overhead.
      NamingLens namingLens =
          options.skipMinification
              ? NamingLens.getIdentityLens()
              : new Minifier(appInfo.withLiveness(), rootSet, options).run(timing);
      timing.end();

      // If a method filter is present don't produce output since the application is likely partial.
      if (options.hasMethodsFilter()) {
        System.out.println("Finished compilation with method filter: ");
        options.methodsFilter.forEach((m) -> System.out.println("  - " + m));
        return null;
      }

      PackageDistribution packageDistribution = null;
      if (inputApp.hasPackageDistribution()) {
        try (Closer closer = Closer.create()) {
          packageDistribution = PackageDistribution.load(inputApp.getPackageDistribution(closer));
        }
      }

      // Generate the resulting application resources.
      AndroidApp androidApp =
          writeApplication(
              executorService,
              application,
              appInfo,
              application.deadCode,
              namingLens,
              proguardSeedsData,
              packageDistribution,
              options);

      options.printWarnings();
      return new CompilationResult(androidApp, application, appInfo);
    } catch (MainDexError mainDexError) {
      throw new CompilationError(mainDexError.getMessageForR8());
    } catch (ExecutionException e) {
      if (e.getCause() instanceof CompilationError) {
        throw (CompilationError) e.getCause();
      }
      throw new RuntimeException(e.getMessage(), e.getCause());
    } finally {
      // Dump timings.
      if (options.printTimes) {
        timing.report();
      }
    }
  }

  /**
   * Main API entry for the R8 compiler.
   *
   * <p>The R8 API is intentionally limited and should "do the right thing" given a command. If this
   * API does not suffice please contact the R8 team.
   *
   * @param command R8 command.
   * @return the compilation result.
   */
  public static AndroidApp run(R8Command command)
      throws IOException, CompilationException, ProguardRuleParserException {
    ExecutorService executorService = ThreadUtils.getExecutorService(command.getInternalOptions());
    try {
      return run(command, executorService);
    } finally {
      executorService.shutdown();
    }
  }

  static void writeOutputs(R8Command command, InternalOptions options, AndroidApp outputApp)
      throws IOException {
    if (command.getOutputPath() != null) {
      outputApp.write(command.getOutputPath(), options.outputMode);
    }

    if (options.printMapping && !options.skipMinification) {
      assert outputApp.hasProguardMap();
      try (Closer closer = Closer.create()) {
        OutputStream mapOut = FileUtils.openPathWithDefault(
            closer,
            options.printMappingFile,
            System.out,
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        outputApp.writeProguardMap(closer, mapOut);
      }
    }
    if (options.printSeeds) {
      assert outputApp.hasProguardSeeds();
      try (Closer closer = Closer.create()) {
        OutputStream seedsOut = FileUtils.openPathWithDefault(
            closer,
            options.seedsFile,
            System.out,
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        outputApp.writeProguardSeeds(closer, seedsOut);
      }
    }
    if (outputApp.hasMainDexListOutput()) {
      try (Closer closer = Closer.create()) {
        OutputStream mainDexOut =
            FileUtils.openPathWithDefault(
                closer,
                options.printMainDexListFile,
                System.out,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        outputApp.writeMainDexList(closer, mainDexOut);
      }
    }
    if (options.printUsage && outputApp.hasDeadCode()) {
      try (Closer closer = Closer.create()) {
        OutputStream deadCodeOut = FileUtils.openPathWithDefault(
            closer,
            options.printUsageFile,
            System.out,
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        outputApp.writeDeadCode(closer, deadCodeOut);
      }
    }
  }

  /**
   * Main API entry for the R8 compiler.
   *
   * <p>The R8 API is intentionally limited and should "do the right thing" given a command. If this
   * API does not suffice please contact the R8 team.
   *
   * @param command R8 command.
   * @param executor executor service from which to get threads for multi-threaded processing.
   * @return the compilation result.
   */
  public static AndroidApp run(R8Command command, ExecutorService executor)
      throws IOException, CompilationException, ProguardRuleParserException {
    InternalOptions options = command.getInternalOptions();
    AndroidApp outputApp =
        runForTesting(command.getInputApp(), options, executor).androidApp;
    writeOutputs(command, options, outputApp);
    return outputApp;
  }

  private static void run(String[] args)
      throws IOException, ProguardRuleParserException, CompilationException {
    R8Command.Builder builder = R8Command.parse(args);
    if (builder.getOutputPath() == null) {
      builder.setOutputPath(Paths.get("."));
    }
    R8Command command = builder.build();
    if (command.isPrintHelp()) {
      System.out.println(USAGE_MESSAGE);
      return;
    }
    if (command.isPrintVersion()) {
      System.out.println("R8 v0.0.1");
      return;
    }
    run(command);
  }

  public static void main(String[] args) {
    try {
      run(args);
    } catch (NoSuchFileException e) {
      System.err.println("File not found: " + e.getFile());
      System.exit(1);
    } catch (FileAlreadyExistsException e) {
      System.err.println("File already exists: " + e.getFile());
    } catch (IOException e) {
      System.err.println("Failed to read or write Android app: " + e.getMessage());
      System.exit(1);
    } catch (ProguardRuleParserException e) {
      System.err.println("Failed parsing proguard keep rules: " + e.getMessage());
      System.exit(1);
    } catch (RuntimeException e) {
      System.err.println("Compilation failed with an internal error.");
      Throwable cause = e.getCause() == null ? e : e.getCause();
      cause.printStackTrace();
      System.exit(1);
    } catch (CompilationException e) {
      System.err.println("Compilation failed: " + e.getMessage());
      System.err.println(USAGE_MESSAGE);
      System.exit(1);
    }
  }
}
