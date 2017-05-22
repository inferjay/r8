// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import com.android.tools.r8.graph.DexAccessFlags;
import java.util.List;
import java.util.Set;

public class ProguardAssumeNoSideEffectRule extends ProguardConfigurationRule {

  public static class Builder extends ProguardClassSpecification.Builder {

    private Builder() {}

    public ProguardAssumeNoSideEffectRule build() {
      return new ProguardAssumeNoSideEffectRule(classAnnotation, classAccessFlags,
          negatedClassAccessFlags, classTypeNegated, classType, classNames, inheritanceAnnotation,
          inheritanceClassName, inheritanceIsExtends, memberRules);
    }
  }

  private ProguardAssumeNoSideEffectRule(
      ProguardTypeMatcher classAnnotation,
      DexAccessFlags classAccessFlags,
      DexAccessFlags negatedClassAccessFlags,
      boolean classTypeNegated,
      ProguardClassType classType,
      List<ProguardTypeMatcher> classNames,
      ProguardTypeMatcher inheritanceAnnotation,
      ProguardTypeMatcher inheritanceClassName,
      boolean inheritanceIsExtends,
      Set<ProguardMemberRule> memberRules) {
    super(classAnnotation, classAccessFlags, negatedClassAccessFlags, classTypeNegated, classType,
        classNames, inheritanceAnnotation, inheritanceClassName, inheritanceIsExtends, memberRules);
  }

  /**
   * Create a new empty builder.
   */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public boolean applyToLibraryClasses() {
    return true;
  }

  @Override
  String typeString() {
    return "assumenosideeffects";
  }
}
