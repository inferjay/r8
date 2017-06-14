// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.graph.AppInfo;
import com.android.tools.r8.graph.DexAccessFlags;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.optimize.Inliner.Constraint;
import java.util.List;

abstract class FieldInstruction extends Instruction {

  protected final MemberType type;
  protected final DexField field;

  protected FieldInstruction(MemberType type, DexField field, Value dest, Value object) {
    super(dest, object);
    assert type != null;
    assert field != null;
    this.type = type;
    this.field = field;
  }

  protected FieldInstruction(MemberType type, DexField field, Value dest, List<Value> values) {
    super(dest, values);
    assert type != null;
    assert field != null;
    this.type = type;
    this.field = field;
  }

  public MemberType getType() {
    return type;
  }

  public DexField getField() {
    return field;
  }

  @Override
  public Constraint inliningConstraint(AppInfo info, DexType holder) {
    // Resolve the field if possible and decide whether the instruction can inlined.
    DexType fieldHolder = field.getHolder();
    DexEncodedField target = info.lookupInstanceTarget(fieldHolder, field);
    DexClass fieldClass = info.definitionFor(fieldHolder);
    if ((target != null) && (fieldClass != null) && !fieldClass.isLibraryClass()) {
      DexAccessFlags flags = target.accessFlags;
      if (flags.isPublic()) {
        return Constraint.ALWAYS;
      }
      if (flags.isPrivate() && (fieldHolder == holder)) {
        return Constraint.PRIVATE;
      }
      if (flags.isProtected() && (fieldHolder.isSamePackage(holder))) {
        return Constraint.PACKAGE;
      }
    }
    return Constraint.NEVER;
  }
}
