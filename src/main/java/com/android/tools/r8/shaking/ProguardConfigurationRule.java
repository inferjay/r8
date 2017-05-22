// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.shaking;

import com.android.tools.r8.graph.DexAccessFlags;
import com.android.tools.r8.utils.StringUtils;
import java.util.List;
import java.util.Set;

public abstract class ProguardConfigurationRule extends ProguardClassSpecification {
  ProguardConfigurationRule(
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

  abstract String typeString();

  String modifierString() {
    return null;
  }

  public boolean applyToLibraryClasses() {
    return false;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(typeString());
    StringUtils.appendNonEmpty(builder, ",", modifierString(), null);
    builder.append(' ');
    builder.append(super.toString());
    return builder.toString();
  }
}
