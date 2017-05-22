// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.desugar;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.Code;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.UseRegistry;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.conversion.IRBuilder;
import com.android.tools.r8.ir.conversion.SourceCode;
import com.android.tools.r8.naming.ClassNameMapper;
import com.android.tools.r8.utils.InternalOptions;

public final class SynthesizedCode extends Code {
  private final SourceCode sourceCode;

  public SynthesizedCode(SourceCode sourceCode) {
    this.sourceCode = sourceCode;
  }

  @Override
  public final IRCode buildIR(DexEncodedMethod encodedMethod, InternalOptions options) {
    return new IRBuilder(encodedMethod, sourceCode, options).build();
  }

  @Override
  public final String toString() {
    return toString(null);
  }

  @Override
  public final void registerReachableDefinitions(UseRegistry registry) {
    throw new Unreachable();
  }

  @Override
  protected final int computeHashCode() {
    return sourceCode.hashCode();
  }

  @Override
  protected final boolean computeEquals(Object other) {
    return other instanceof SynthesizedCode &&
        this.sourceCode.equals(((SynthesizedCode) other).sourceCode);
  }

  @Override
  public final String toString(ClassNameMapper naming) {
    return "SynthesizedCode: " + sourceCode.toString();
  }
}
