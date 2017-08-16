// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.naming.DictionaryReader;
import com.android.tools.r8.utils.InternalOptions.PackageObfuscationMode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProguardConfiguration {

  public static class Builder {

    private final List<Path> injars = new ArrayList<>();
    private final List<Path> libraryjars = new ArrayList<>();
    private PackageObfuscationMode packageObfuscationMode = PackageObfuscationMode.NONE;
    private String packagePrefix = "";
    private boolean allowAccessModification = false;
    private boolean ignoreWarnings = false;
    private boolean optimize = true;
    private int optimizationPasses = 1;
    private boolean obfuscating = true;
    private boolean shrinking = true;
    private boolean printUsage = false;
    private Path printUsageFile;
    private boolean printMapping;
    private Path printMappingFile;
    private boolean verbose = false;
    private final List<String> attributesRemovalPatterns = new ArrayList<>();
    private final Set<ProguardTypeMatcher> dontWarnPatterns = new HashSet<>();
    protected final List<ProguardConfigurationRule> rules = new ArrayList<>();
    private final DexItemFactory dexItemFactory;
    private boolean printSeeds;
    private Path seedFile;
    private Path obfuscationDictionary;
    private Path classObfuscationDictionary;
    private Path packageObfuscationDictionary;

    private Builder(DexItemFactory dexItemFactory) {
      this.dexItemFactory = dexItemFactory;
    }

    public void addInjars(List<Path> injars) {
      this.injars.addAll(injars);
    }

    public void addLibraryJars(List<Path> libraryJars) {
      this.libraryjars.addAll(libraryJars);
    }

    public PackageObfuscationMode getPackageObfuscationMode() {
      return packageObfuscationMode;
    }

    public void setPackagePrefix(String packagePrefix) {
      packageObfuscationMode = PackageObfuscationMode.REPACKAGE;
      this.packagePrefix = packagePrefix;
    }

    public void setFlattenPackagePrefix(String packagePrefix) {
      packageObfuscationMode = PackageObfuscationMode.FLATTEN;
      this.packagePrefix = packagePrefix;
    }

    public void setAllowAccessModification(boolean allowAccessModification) {
      this.allowAccessModification = allowAccessModification;
    }

    public void setIgnoreWarnings(boolean ignoreWarnings) {
      this.ignoreWarnings = ignoreWarnings;
    }

    public void setOptimize(boolean optimize) {
      this.optimize = optimize;
    }

    public void setOptimizationPasses(int optimizationPasses) {
      // TODO(b/36800551): no-op until we have clear ideas about optimization passes.
      // this.optimizationPasses = optimizationPasses;
    }

    public void setObfuscating(boolean obfuscate) {
      this.obfuscating = obfuscate;
    }

    public void setShrinking(boolean shrinking) {
      this.shrinking = shrinking;
    }

    public void setPrintUsage(boolean printUsage) {
      this.printUsage = printUsage;
    }

    public void setPrintUsageFile(Path printUsageFile) {
      this.printUsageFile = printUsageFile;
    }

    public void setPrintMapping(boolean printMapping) {
      this.printMapping = printMapping;
    }

    public void setPrintMappingFile(Path file) {
      this.printMappingFile = file;
    }

    public void setVerbose(boolean verbose) {
      this.verbose = verbose;
    }

    public void addAttributeRemovalPattern(String attributesRemovalPattern) {
      this.attributesRemovalPatterns.add(attributesRemovalPattern);
    }

    public void addRule(ProguardConfigurationRule rule) {
      this.rules.add(rule);
    }

    public void addDontWarnPattern(ProguardTypeMatcher pattern) {
      dontWarnPatterns.add(pattern);
    }

    public void setSeedFile(Path seedFile) {
      this.seedFile = seedFile;
    }

    public void setPrintSeeds(boolean printSeeds) {
      this.printSeeds = printSeeds;
    }

    public void setObfuscationDictionary(Path obfuscationDictionary) {
      this.obfuscationDictionary = obfuscationDictionary;
    }

    public void setClassObfuscationDictionary(Path classObfuscationDictionary) {
      this.classObfuscationDictionary = classObfuscationDictionary;
    }

    public void setPackageObfuscationDictionary(Path packageObfuscationDictionary) {
      this.packageObfuscationDictionary = packageObfuscationDictionary;
    }

    public ProguardConfiguration build() {
      return new ProguardConfiguration(
          dexItemFactory,
          injars,
          libraryjars,
          packageObfuscationMode,
          packagePrefix,
          allowAccessModification,
          ignoreWarnings,
          optimize ? optimizationPasses : 0,
          obfuscating,
          shrinking,
          printUsage,
          printUsageFile,
          printMapping,
          printMappingFile,
          verbose,
          attributesRemovalPatterns,
          dontWarnPatterns,
          rules,
          printSeeds,
          seedFile,
          DictionaryReader.readAllNames(obfuscationDictionary),
          DictionaryReader.readAllNames(classObfuscationDictionary),
          DictionaryReader.readAllNames(packageObfuscationDictionary));
    }
  }

  private final DexItemFactory dexItemFactory;
  private final ImmutableList<Path> injars;
  private final ImmutableList<Path> libraryjars;
  private final PackageObfuscationMode packageObfuscationMode;
  private final String packagePrefix;
  private final boolean allowAccessModification;
  private final boolean ignoreWarnings;
  private final int optimizationPasses;
  private final boolean obfuscating;
  private final boolean shrinking;
  private final boolean printUsage;
  private final Path printUsageFile;
  private final boolean printMapping;
  private final Path printMappingFile;
  private final boolean verbose;
  private final ImmutableList<String> attributesRemovalPatterns;
  private final ImmutableSet<ProguardTypeMatcher> dontWarnPatterns;
  protected final ImmutableList<ProguardConfigurationRule> rules;
  private final boolean printSeeds;
  private final Path seedFile;
  private final ImmutableList<String> obfuscationDictionary;
  private final ImmutableList<String> classObfuscationDictionary;
  private final ImmutableList<String> packageObfuscationDictionary;

  private ProguardConfiguration(
      DexItemFactory factory,
      List<Path> injars,
      List<Path> libraryjars,
      PackageObfuscationMode packageObfuscationMode,
      String packagePrefix,
      boolean allowAccessModification,
      boolean ignoreWarnings,
      int optimizationPasses,
      boolean obfuscating,
      boolean shrinking,
      boolean printUsage,
      Path printUsageFile,
      boolean printMapping,
      Path printMappingFile,
      boolean verbose,
      List<String> attributesRemovalPatterns,
      Set<ProguardTypeMatcher> dontWarnPatterns,
      List<ProguardConfigurationRule> rules,
      boolean printSeeds,
      Path seedFile,
      ImmutableList<String> obfuscationDictionary,
      ImmutableList<String> classObfuscationDictionary,
      ImmutableList<String> packageObfuscationDictionary) {
    this.dexItemFactory = factory;
    this.injars = ImmutableList.copyOf(injars);
    this.libraryjars = ImmutableList.copyOf(libraryjars);
    this.packageObfuscationMode = packageObfuscationMode;
    this.packagePrefix = packagePrefix;
    this.allowAccessModification = allowAccessModification;
    this.ignoreWarnings = ignoreWarnings;
    this.optimizationPasses = optimizationPasses;
    this.obfuscating = obfuscating;
    this.shrinking = shrinking;
    this.printUsage = printUsage;
    this.printUsageFile = printUsageFile;
    this.printMapping = printMapping;
    this.printMappingFile = printMappingFile;
    this.verbose = verbose;
    this.attributesRemovalPatterns = ImmutableList.copyOf(attributesRemovalPatterns);
    this.dontWarnPatterns = ImmutableSet.copyOf(dontWarnPatterns);
    this.rules = ImmutableList.copyOf(rules);
    this.printSeeds = printSeeds;
    this.seedFile = seedFile;
    this.obfuscationDictionary = obfuscationDictionary;
    this.classObfuscationDictionary = classObfuscationDictionary;
    this.packageObfuscationDictionary = packageObfuscationDictionary;
  }

  /**
   * Create a new empty builder.
   */
  public static Builder builder(DexItemFactory dexItemFactory) {
    return new Builder(dexItemFactory);
  }

  public DexItemFactory getDexItemFactory() {
    return dexItemFactory;
  }

  public boolean isDefaultConfiguration() {
    return false;
  }

  public ImmutableList<Path> getInjars() {
    return injars;
  }

  public ImmutableList<Path> getLibraryjars() {
    return libraryjars;
  }

  public PackageObfuscationMode getPackageObfuscationMode() {
    return packageObfuscationMode;
  }

  public String getPackagePrefix() {
    return packagePrefix;
  }

  public boolean isAccessModificationAllowed() {
    return allowAccessModification;
  }

  public boolean isPrintMapping() {
    return printMapping;
  }

  public Path getPrintMappingFile() {
    return printMappingFile;
  }

  public boolean isIgnoreWarnings() {
    return ignoreWarnings;
  }

  public int getOptimizationPasses() {
    return optimizationPasses;
  }

  public boolean isObfuscating() {
    return obfuscating;
  }

  public boolean isShrinking() {
    return shrinking;
  }

  public boolean isPrintUsage() {
    return printUsage;
  }

  public Path getPrintUsageFile() {
    return printUsageFile;
  }

  public boolean isVerbose() {
    return verbose;
  }

  public ImmutableList<String> getAttributesRemovalPatterns() {
    return attributesRemovalPatterns;
  }

  public ImmutableSet<ProguardTypeMatcher> getDontWarnPatterns() {
    return dontWarnPatterns;
  }

  public ImmutableList<ProguardConfigurationRule> getRules() {
    return rules;
  }

  public ImmutableList<String> getObfuscationDictionary() {
    return obfuscationDictionary;
  }

  public ImmutableList<String> getClassObfuscationDictionary() {
    return classObfuscationDictionary;
  }

  public ImmutableList<String> getPackageObfuscationDictionary() {
    return packageObfuscationDictionary;
  }

  public static ProguardConfiguration defaultConfiguration(DexItemFactory dexItemFactory) {
    return new DefaultProguardConfiguration(dexItemFactory);
  }

  public static class DefaultProguardConfiguration extends ProguardConfiguration {

    public DefaultProguardConfiguration(DexItemFactory factory) {
      super(factory,
          ImmutableList.of()    /* injars */,
          ImmutableList.of()    /* libraryjars */,
          PackageObfuscationMode.REPACKAGE, /* TODO(b/36799686): should be NONE once implemented */
          ""                    /* package prefix */,
          false                 /* allowAccessModification */,
          false                 /* ignoreWarnings */,
          1                     /* optimizationPasses */,
          false                 /* obfuscating */,
          false                 /* shrinking */,
          false                 /* printUsage */,
          null                  /* printUsageFile */,
          false                 /* printMapping */,
          null                  /* outputMapping */,
          false                 /* verbose */,
          ImmutableList.of()    /* attributesRemovalPatterns */,
          ImmutableSet.of()     /* dontWarnPatterns */,
          ImmutableList.of(ProguardKeepRule.defaultKeepAllRule()),
          false                 /* printSeeds */,
          null                  /* seedFile */,
          ImmutableList.of()     /* obfuscationDictionary */,
          ImmutableList.of()     /* classObfuscationDictionary */,
          ImmutableList.of()     /* packageObfuscationDictionary */);
    }

    @Override
    public boolean isDefaultConfiguration() {
      return true;
    }
  }

  public boolean isPrintSeeds() {
    return printSeeds;
  }

  public Path getSeedFile() {
    return seedFile;
  }
}
