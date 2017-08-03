// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.shaking.ProguardConfiguration;
import com.android.tools.r8.shaking.ProguardConfigurationParser;
import com.android.tools.r8.shaking.ProguardConfigurationRule;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.FileUtils;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.OutputMode;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class R8Command extends BaseCommand {

  public static class Builder extends BaseCommand.Builder<R8Command, Builder> {

    private final List<Path> mainDexRules = new ArrayList<>();
    private boolean minimalMainDex = false;
    private final List<Path> proguardConfigFiles = new ArrayList<>();
    private Optional<Boolean> treeShaking = Optional.empty();
    private Optional<Boolean> minification = Optional.empty();
    private boolean ignoreMissingClasses = false;

    private Builder() {
      super(CompilationMode.RELEASE);
    }

    private Builder(AndroidApp app) {
      super(app, CompilationMode.RELEASE);
    }

    @Override
    Builder self() {
      return this;
    }

    /**
     * Enable/disable tree shaking. This overrides any settings in proguard configuration files.
     */
    public Builder setTreeShaking(boolean useTreeShaking) {
      treeShaking = Optional.of(useTreeShaking);
      return self();
    }

    /**
     * Enable/disable minification. This overrides any settings in proguard configuration files.
     */
    public Builder setMinification(boolean useMinification) {
      minification = Optional.of(useMinification);
      return self();
    }

    /**
     * Add proguard configuration file resources for automatic main dex list calculation.
     */
    public Builder addMainDexRules(Path... paths) {
      Collections.addAll(mainDexRules, paths);
      return self();
    }

    /**
     * Add proguard configuration file resources for automatic main dex list calculation.
     */
    public Builder addMainDexRules(List<Path> paths) {
      mainDexRules.addAll(paths);
      return self();
    }

    /**
     * Request minimal main dex generated when main dex rules are used.
     *
     * The main purpose of this is to verify that the main dex rules are sufficient
     * for running on a platform without native multi dex support.
     */
    public Builder setMinimalMainDex(boolean value) {
      minimalMainDex = value;
      return self();
    }
    /**
     * Add proguard configuration file resources.
     */
    public Builder addProguardConfigurationFiles(Path... paths) {
      Collections.addAll(proguardConfigFiles, paths);
      return self();
    }

    /**
     * Add proguard configuration file resources.
     */
    public Builder addProguardConfigurationFiles(List<Path> paths) {
      proguardConfigFiles.addAll(paths);
      return self();
    }

    /**
     * Set a proguard mapping file resource.
     */
    public Builder setProguardMapFile(Path path) {
      getAppBuilder().setProguardMapFile(path);
      return self();
    }

    /**
     * Set a package distribution file resource.
     */
    public Builder setPackageDistributionFile(Path path) {
      getAppBuilder().setPackageDistributionFile(path);
      return self();
    }

    /**
     * Deprecated flag to avoid failing if classes are missing during compilation.
     *
     * <p>TODO: Make compilation safely assume this flag to be true and remove the flag.
     */
    Builder setIgnoreMissingClasses(boolean ignoreMissingClasses) {
      this.ignoreMissingClasses = ignoreMissingClasses;
      return self();
    }

    protected void validate() throws CompilationException {
      super.validate();
      if (minimalMainDex && mainDexRules.isEmpty()) {
        throw new CompilationException(
            "Option --minimal-main-dex require --main-dex-rules");
      }
    }

    @Override
    public R8Command build() throws CompilationException, IOException {
      // If printing versions ignore everything else.
      if (isPrintHelp() || isPrintVersion()) {
        return new R8Command(isPrintHelp(), isPrintVersion());
      }

      validate();
      DexItemFactory factory = new DexItemFactory();
      ImmutableList<ProguardConfigurationRule> mainDexKeepRules;
      if (this.mainDexRules.isEmpty()) {
        mainDexKeepRules = ImmutableList.of();
      } else {
        ProguardConfigurationParser parser = new ProguardConfigurationParser(factory);
        try {
          parser.parse(mainDexRules);
        } catch (ProguardRuleParserException e) {
          throw new CompilationException(e.getMessage(), e.getCause());
        }
        mainDexKeepRules = parser.getConfig().getRules();
      }
      ProguardConfiguration configuration;
      if (proguardConfigFiles.isEmpty()) {
        configuration = ProguardConfiguration.defaultConfiguration(factory);
      } else {
        ProguardConfigurationParser parser = new ProguardConfigurationParser(factory);
        try {
          parser.parse(proguardConfigFiles);
        } catch (ProguardRuleParserException e) {
          throw new CompilationException(e.getMessage(), e.getCause());
        }
        configuration = parser.getConfig();
        addProgramFiles(configuration.getInjars());
        addLibraryFiles(configuration.getLibraryjars());
      }

      boolean useTreeShaking = treeShaking.orElse(configuration.isShrinking());
      boolean useMinification = minification.orElse(configuration.isObfuscating());

      return new R8Command(
          getAppBuilder().build(),
          getOutputPath(),
          getOutputMode(),
          mainDexKeepRules,
          minimalMainDex,
          configuration,
          getMode(),
          getMinApiLevel(),
          useTreeShaking,
          useMinification,
          ignoreMissingClasses);
    }
  }

  // Internal state to verify parsing properties not enforced by the builder.
  private static class ParseState {

    CompilationMode mode = null;
  }

  static final String USAGE_MESSAGE = String.join("\n", ImmutableList.of(
      "Usage: r8 [options] <input-files>",
      " where <input-files> are any combination of dex, class, zip, jar, or apk files",
      " and options are:",
      "  --release                # Compile without debugging information (default).",
      "  --debug                  # Compile with debugging information.",
      "  --output <file>          # Output result in <file>.",
      "                           # <file> must be an existing directory or a zip file.",
      "  --lib <file>             # Add <file> as a library resource.",
      "  --min-api                # Minimum Android API level compatibility.",
      "  --pg-conf <file>         # Proguard configuration <file> (implies tree",
      "                           # shaking/minification).",
      "  --pg-map <file>          # Proguard map <file>.",
      "  --no-tree-shaking        # Force disable tree shaking of unreachable classes.",
      "  --no-minification        # Force disable minification of names.",
      "  --main-dex-rules <file>  # Proguard keep rules for classes to place in the",
      "                           # primary dex file.",
      "  --minimal-main-dex       # Only place classes specified by --main-dex-rules",
      "                           # in the primary dex file.",
      "  --version                # Print the version of r8.",
      "  --help                   # Print this message."));

  private final ImmutableList<ProguardConfigurationRule> mainDexKeepRules;
  private final boolean minimalMainDex;
  private final ProguardConfiguration proguardConfiguration;
  private final boolean useTreeShaking;
  private final boolean useMinification;
  private final boolean ignoreMissingClasses;

  public static Builder builder() {
    return new Builder();
  }

  // Internal builder to start from an existing AndroidApp.
  static Builder builder(AndroidApp app) {
    return new Builder(app);
  }

  public static Builder parse(String[] args) throws CompilationException, IOException {
    Builder builder = builder();
    parse(args, builder, new ParseState());
    return builder;
  }

  private static ParseState parse(String[] args, Builder builder, ParseState state)
      throws CompilationException, IOException {
    for (int i = 0; i < args.length; i++) {
      String arg = args[i].trim();
      if (arg.length() == 0) {
        continue;
      } else if (arg.equals("--help")) {
        builder.setPrintHelp(true);
      } else if (arg.equals("--version")) {
        builder.setPrintVersion(true);
      } else if (arg.equals("--debug")) {
        if (state.mode == CompilationMode.RELEASE) {
          throw new CompilationException("Cannot compile in both --debug and --release mode.");
        }
        state.mode = CompilationMode.DEBUG;
        builder.setMode(state.mode);
      } else if (arg.equals("--release")) {
        if (state.mode == CompilationMode.DEBUG) {
          throw new CompilationException("Cannot compile in both --debug and --release mode.");
        }
        state.mode = CompilationMode.RELEASE;
        builder.setMode(state.mode);
      } else if (arg.equals("--output")) {
        String outputPath = args[++i];
        if (builder.getOutputPath() != null) {
          throw new CompilationException(
              "Cannot output both to '"
                  + builder.getOutputPath().toString()
                  + "' and '"
                  + outputPath
                  + "'");
        }
        builder.setOutputPath(Paths.get(outputPath));
      } else if (arg.equals("--lib")) {
        builder.addLibraryFiles(Paths.get(args[++i]));
      } else if (arg.equals("--min-api")) {
        builder.setMinApiLevel(Integer.valueOf(args[++i]));
      } else if (arg.equals("--no-tree-shaking")) {
        builder.setTreeShaking(false);
      } else if (arg.equals("--no-minification")) {
        builder.setMinification(false);
      } else if (arg.equals("--main-dex-rules")) {
        builder.addMainDexRules(Paths.get(args[++i]));
      } else if (arg.equals("--minimal-main-dex")) {
        builder.setMinimalMainDex(true);
      } else if (arg.equals("--pg-conf")) {
        builder.addProguardConfigurationFiles(Paths.get(args[++i]));
      } else if (arg.equals("--pg-map")) {
        builder.setProguardMapFile(Paths.get(args[++i]));
      } else if (arg.equals("--ignore-missing-classes")) {
        builder.setIgnoreMissingClasses(true);
      } else if (arg.startsWith("@")) {
        // TODO(zerny): Replace this with pipe reading.
        String argsFile = arg.substring(1);
        try {
          List<String> linesInFile = FileUtils.readTextFile(Paths.get(argsFile));
          List<String> argsInFile = new ArrayList<>();
          for (String line : linesInFile) {
            for (String word : line.split("\\s")) {
              String trimmed = word.trim();
              if (!trimmed.isEmpty()) {
                argsInFile.add(trimmed);
              }
            }
          }
          // TODO(zerny): We need to define what CWD should be for files referenced in an args file.
          state = parse(argsInFile.toArray(new String[argsInFile.size()]), builder, state);
        } catch (IOException | CompilationException e) {
          throw new CompilationException(
              "Failed to read arguments from file " + argsFile + ": " + e.getMessage());
        }
      } else {
        if (arg.startsWith("--")) {
          throw new CompilationException("Unknown option: " + arg);
        }
        builder.addProgramFiles(Paths.get(arg));
      }
    }
    return state;
  }

  private R8Command(
      AndroidApp inputApp,
      Path outputPath,
      OutputMode outputMode,
      ImmutableList<ProguardConfigurationRule> mainDexKeepRules,
      boolean minimalMainDex,
      ProguardConfiguration proguardConfiguration,
      CompilationMode mode,
      int minApiLevel,
      boolean useTreeShaking,
      boolean useMinification,
      boolean ignoreMissingClasses) {
    super(inputApp, outputPath, outputMode, mode, minApiLevel);
    assert proguardConfiguration != null;
    assert mainDexKeepRules != null;
    assert getOutputMode() == OutputMode.Indexed : "Only regular mode is supported in R8";
    this.mainDexKeepRules = mainDexKeepRules;
    this.minimalMainDex = minimalMainDex;
    this.proguardConfiguration = proguardConfiguration;
    this.useTreeShaking = useTreeShaking;
    this.useMinification = useMinification;
    this.ignoreMissingClasses = ignoreMissingClasses;
  }

  private R8Command(boolean printHelp, boolean printVersion) {
    super(printHelp, printVersion);
    mainDexKeepRules = ImmutableList.of();
    minimalMainDex = false;
    proguardConfiguration = null;
    useTreeShaking = false;
    useMinification = false;
    ignoreMissingClasses = false;
  }

  public boolean useTreeShaking() {
    return useTreeShaking;
  }

  public boolean useMinification() {
    return useMinification;
  }

  @Override
  InternalOptions getInternalOptions() {
    InternalOptions internal = new InternalOptions(proguardConfiguration.getDexItemFactory());
    assert !internal.debug;
    internal.debug = getMode() == CompilationMode.DEBUG;
    internal.minApiLevel = getMinApiLevel();
    assert !internal.skipMinification;
    internal.skipMinification = !useMinification();
    assert internal.useTreeShaking;
    internal.useTreeShaking = useTreeShaking();
    assert !internal.printUsage;
    internal.printUsage = proguardConfiguration.isPrintUsage();
    internal.printUsageFile = proguardConfiguration.getPrintUsageFile();
    assert !internal.ignoreMissingClasses;
    internal.ignoreMissingClasses = ignoreMissingClasses;

    // TODO(zerny): Consider which other proguard options should be given flags.
    assert internal.packagePrefix.length() == 0;
    internal.packagePrefix = proguardConfiguration.getPackagePrefix();
    assert internal.allowAccessModification;
    internal.allowAccessModification = proguardConfiguration.getAllowAccessModification();
    for (String pattern : proguardConfiguration.getAttributesRemovalPatterns()) {
      internal.attributeRemoval.applyPattern(pattern);
    }
    if (proguardConfiguration.isIgnoreWarnings()) {
      internal.ignoreMissingClasses = true;
    }
    assert internal.seedsFile == null;
    if (proguardConfiguration.getSeedFile() != null) {
      internal.seedsFile = proguardConfiguration.getSeedFile();
    }
    assert !internal.verbose;
    if (proguardConfiguration.isVerbose()) {
      internal.verbose = true;
    }
    if (!proguardConfiguration.isObfuscating()) {
      internal.skipMinification = true;
    }
    internal.printSeeds |= proguardConfiguration.getPrintSeeds();
    internal.printMapping |= proguardConfiguration.isPrintingMapping();
    internal.printMappingFile = proguardConfiguration.getPrintMappingOutput();
    internal.classObfuscationDictionary = proguardConfiguration.getClassObfuscationDictionary();
    internal.obfuscationDictionary = proguardConfiguration.getObfuscationDictionary();
    internal.mainDexKeepRules = mainDexKeepRules;
    internal.minimalMainDex = minimalMainDex;
    internal.keepRules = proguardConfiguration.getRules();
    internal.dontWarnPatterns = proguardConfiguration.getDontWarnPatterns();
    internal.outputMode = getOutputMode();
    if (internal.debug) {
      // TODO(zerny): Should we support removeSwitchMaps in debug mode? b/62936642
      internal.removeSwitchMaps = false;
      // TODO(zerny): Should we support inlining in debug mode? b/62937285
      internal.inlineAccessors = false;
    }
    return internal;
  }
}
