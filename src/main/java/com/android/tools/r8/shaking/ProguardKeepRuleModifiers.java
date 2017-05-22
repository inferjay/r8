// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

public class ProguardKeepRuleModifiers {
  public static class Builder {
    public boolean allowsShrinking = false;
    public boolean allowsOptimization = false;
    public boolean allowsObfuscation = false;
    public boolean whyAreYouKeeping = false;
    public boolean includeDescriptorClasses = false;
    public boolean keepPackageNames = false;
    public boolean checkDiscarded = false;

    void setFlagsToHaveNoEffect() {
      allowsShrinking = true;
      allowsOptimization = true;
      allowsObfuscation = true;
      whyAreYouKeeping = false;
      includeDescriptorClasses = false;
      keepPackageNames = false;
    }

    private Builder() {}

    ProguardKeepRuleModifiers build() {
      return new ProguardKeepRuleModifiers(allowsShrinking, allowsOptimization, allowsObfuscation,
          whyAreYouKeeping, includeDescriptorClasses, keepPackageNames, checkDiscarded);
    }
  }

  public final boolean allowsShrinking;
  public final boolean allowsOptimization;
  public final boolean allowsObfuscation;
  public final boolean whyAreYouKeeping;
  public final boolean includeDescriptorClasses;
  public final boolean keepPackageNames;
  public final boolean checkDiscarded;

  private ProguardKeepRuleModifiers(
      boolean allowsShrinking,
      boolean allowsOptimization,
      boolean allowsObfuscation,
      boolean whyAreYouKeeping,
      boolean includeDescriptorClasses,
      boolean keepPackageNames,
      boolean checkDiscarded) {
    this.allowsShrinking = allowsShrinking;
    this.allowsOptimization = allowsOptimization;
    this.allowsObfuscation = allowsObfuscation;
    this.whyAreYouKeeping = whyAreYouKeeping;
    this.includeDescriptorClasses = includeDescriptorClasses;
    this.keepPackageNames = keepPackageNames;
    this.checkDiscarded = checkDiscarded;
  }
  /**
   * Create a new empty builder.
   */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ProguardKeepRuleModifiers)) {
      return false;
    }
    ProguardKeepRuleModifiers that = (ProguardKeepRuleModifiers) o;

    return allowsShrinking == that.allowsShrinking
        && allowsOptimization == that.allowsOptimization
        && allowsObfuscation == that.allowsObfuscation
        && includeDescriptorClasses == that.includeDescriptorClasses
        && keepPackageNames == that.keepPackageNames;
  }

  @Override
  public int hashCode() {
    return (allowsShrinking ? 1 : 0)
        | (allowsOptimization ? 2 : 0)
        | (allowsObfuscation ? 4 : 0)
        | (whyAreYouKeeping ? 8 : 0)
        | (includeDescriptorClasses ? 16 : 0)
        | (keepPackageNames ? 32 : 0);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    appendWithComma(builder, allowsObfuscation, "allowobfuscation");
    appendWithComma(builder, allowsShrinking, "allowshrinking");
    appendWithComma(builder, allowsOptimization, "allowoptimization");
    appendWithComma(builder, whyAreYouKeeping, "whyareyoukeeping");
    appendWithComma(builder, includeDescriptorClasses, "includedescriptorclasses");
    appendWithComma(builder, keepPackageNames, "keeppackagenames");
    return builder.toString();
  }

  private void appendWithComma(StringBuilder builder, boolean predicate,
      String text) {
    if (!predicate) {
      return;
    }
    if (builder.length() != 0) {
      builder.append(',');
    }
    builder.append(text);
  }
}
