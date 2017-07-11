// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.naming.DictionaryReader;
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
    private String packagePrefix = null;
    private boolean allowAccessModification = false;
    private boolean ignoreWarnings = false;
    private boolean obfuscating = true;
    private boolean shrinking = true;
    private boolean printUsage = false;
    private Path printUsageFile;
    private boolean printMapping;
    private Path printMappingOutput;
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

    public void setPackagePrefix(String packagePrefix) {
      this.packagePrefix = packagePrefix;
    }

    public void setAllowAccessModification(boolean allowAccessModification) {
      this.allowAccessModification = allowAccessModification;
    }

    public void setIgnoreWarnings(boolean ignoreWarnings) {
      this.ignoreWarnings = ignoreWarnings;
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

    public void setPrintMappingOutput(Path file) {
      this.printMappingOutput = file;
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

    public void setPrintSeed(boolean printSeeds) {
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
          packagePrefix,
          allowAccessModification,
          ignoreWarnings,
          obfuscating,
          shrinking,
          printUsage,
          printUsageFile,
          printMapping,
          printMappingOutput,
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
  private final List<Path> injars;
  private final List<Path> libraryjars;
  private final String packagePrefix;
  private final boolean allowAccessModification;
  private final boolean ignoreWarnings;
  private final boolean obfuscating;
  private final boolean shrinking;
  private final boolean printUsage;
  private final Path printUsageFile;
  private final boolean printMapping;
  private final Path printMappingOutput;
  private final boolean verbose;
  private final List<String> attributesRemovalPatterns;
  private final ImmutableSet<ProguardTypeMatcher> dontWarnPatterns;
  protected final ImmutableList<ProguardConfigurationRule> rules;
  private final boolean printSeeds;
  private final Path seedFile;
  private final List<String> obfuscationDictionary;
  private final List<String> classObfuscationDictionary;
  private final List<String> packageObfuscationDictionary;

  private ProguardConfiguration(
      DexItemFactory factory,
      List<Path> injars,
      List<Path> libraryjars,
      String packagePrefix,
      boolean allowAccessModification,
      boolean ignoreWarnings,
      boolean obfuscating,
      boolean shrinking,
      boolean printUsage,
      Path printUsageFile,
      boolean printMapping,
      Path printMappingOutput,
      boolean verbose,
      List<String> attributesRemovalPatterns,
      Set<ProguardTypeMatcher> dontWarnPatterns,
      List<ProguardConfigurationRule> rules,
      boolean printSeeds,
      Path seedFile,
      List<String> obfuscationDictionary,
      List<String> classObfuscationDictionary,
      List<String> packageObfuscationDictionary) {
    this.dexItemFactory = factory;
    this.injars = ImmutableList.copyOf(injars);
    this.libraryjars = ImmutableList.copyOf(libraryjars);
    this.packagePrefix = packagePrefix;
    this.allowAccessModification = allowAccessModification;
    this.ignoreWarnings = ignoreWarnings;
    this.obfuscating = obfuscating;
    this.shrinking = shrinking;
    this.printUsage = printUsage;
    this.printUsageFile = printUsageFile;
    this.printMapping = printMapping;
    this.printMappingOutput = printMappingOutput;
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

  public List<Path> getInjars() {
    return injars;
  }

  public List<Path> getLibraryjars() {
    return libraryjars;
  }

  public String getPackagePrefix() {
    return packagePrefix;
  }

  public boolean getAllowAccessModification() {
    return allowAccessModification;
  }

  public boolean isPrintingMapping() {
    return printMapping;
  }

  public Path getPrintMappingOutput() {
    return printMappingOutput;
  }

  public boolean isIgnoreWarnings() {
    return ignoreWarnings;
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

  public List<String> getAttributesRemovalPatterns() {
    return attributesRemovalPatterns;
  }

  public ImmutableSet<ProguardTypeMatcher> getDontWarnPatterns() {
    return dontWarnPatterns;
  }

  public ImmutableList<ProguardConfigurationRule> getRules() {
    return rules;
  }

  public List<String> getObfuscationDictionary() {
    return obfuscationDictionary;
  }

  public List<String> getPackageObfuscationDictionary() {
    return packageObfuscationDictionary;
  }

  public List<String> getClassObfuscationDictionary() {
    return classObfuscationDictionary;
  }

  public static ProguardConfiguration defaultConfiguration(DexItemFactory dexItemFactory) {
    ProguardConfiguration config = new DefaultProguardConfiguration(dexItemFactory);
    return config;
  }

  public static class DefaultProguardConfiguration extends ProguardConfiguration {

    public DefaultProguardConfiguration(DexItemFactory factory) {
      super(factory,
          ImmutableList.of()    /* injars */,
          ImmutableList.of()    /* libraryjars */,
          ""                    /* package prefix */,
          false                 /* allowAccessModification */,
          false                 /* ignoreWarnings */,
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
          ImmutableList.of()     /* packageObfucationDictionary */);
    }

    @Override
    public boolean isDefaultConfiguration() {
      return true;
    }
  }

  public boolean getPrintSeeds() {
    return printSeeds;
  }

  public Path getSeedFile() {
    return seedFile;
  }
}
