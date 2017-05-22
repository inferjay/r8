// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir;

import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.ValueNumberGenerator;
import com.android.tools.r8.smali.SmaliTestBase;
import com.android.tools.r8.utils.InternalOptions;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.ListIterator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class BasicBlockIteratorTest extends SmaliTestBase {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  /**
   * Simple test IR, which has three blocks:
   *
   * First block: Argument instructions
   * Second block: Add instruction
   * Third block: Return instruction
   *
   */
  IRCode simpleCode() {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    String returnType = "int";
    List<String> parameters = ImmutableList.of("int", "int");
    MethodSignature signature = builder.addStaticMethod(
        returnType,
        DEFAULT_METHOD_NAME,
        parameters,
        1,
        "    add-int             v0, p0, p1",
        "    return              p0"
    );

    InternalOptions options = new InternalOptions();
    DexApplication application = buildApplication(builder, options);

    // Build the code, and split the code into three blocks.
    ValueNumberGenerator valueNumberGenerator = new ValueNumberGenerator();
    DexEncodedMethod method = getMethod(application, signature);
    IRCode code = method.buildIR(valueNumberGenerator, new InternalOptions());
    ListIterator<BasicBlock> blocks = code.listIterator();
    InstructionListIterator iter = blocks.next().listIterator();
    iter.nextUntil(i -> !i.isArgument());
    iter.previous();
    iter.split(1, code, blocks);
    return code;
  }

  @Test
  public void removeBeforeNext() {
    IRCode code = simpleCode();

    ListIterator<BasicBlock> blocks = code.listIterator();
    thrown.expect(IllegalStateException.class);
    blocks.remove();
  }

  @Test
  public void removeTwice() {
    IRCode code = simpleCode();

    ListIterator<BasicBlock> blocks = code.listIterator();
    blocks.next();
    blocks.next();
    blocks.remove();
    thrown.expect(IllegalStateException.class);
    blocks.remove();
  }
}
