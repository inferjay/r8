// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.dex;

import com.android.tools.r8.code.Const4;
import com.android.tools.r8.code.ConstString;
import com.android.tools.r8.code.Goto32;
import com.android.tools.r8.code.IfEq;
import com.android.tools.r8.code.IfEqz;
import com.android.tools.r8.code.IfNe;
import com.android.tools.r8.code.IfNez;
import com.android.tools.r8.code.Instruction;
import com.android.tools.r8.code.ReturnVoid;
import com.android.tools.r8.graph.DexAccessFlags;
import com.android.tools.r8.graph.DexCode;
import com.android.tools.r8.graph.DexCode.Try;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.naming.NamingLens;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class JumboStringProcessing {

  @Test
  public void branching() {
    DexItemFactory factory = new DexItemFactory();
    DexString string = factory.createString("turn into jumbo");
    factory.sort(NamingLens.getIdentityLens());
    Instruction[] instructions = buildInstructions(string, false);
    DexCode code = jumboStringProcess(factory, string, instructions);
    Instruction[] rewrittenInstructions = code.instructions;
    assert rewrittenInstructions[1] instanceof IfEq;
    IfEq condition = (IfEq) rewrittenInstructions[1];
    assert condition.getOffset() + condition.CCCC == rewrittenInstructions[3].getOffset();
    assert rewrittenInstructions[2] instanceof Goto32;
    Goto32 jump = (Goto32) rewrittenInstructions[2];
    Instruction lastInstruction = rewrittenInstructions[rewrittenInstructions.length - 1];
    assert jump.getOffset() + jump.AAAAAAAA == lastInstruction.getOffset();
  }

  @Test
  public void branching2() {
    DexItemFactory factory = new DexItemFactory();
    DexString string = factory.createString("turn into jumbo");
    factory.sort(NamingLens.getIdentityLens());
    Instruction[] instructions = buildInstructions(string, true);
    DexCode code = jumboStringProcess(factory, string, instructions);
    Instruction[] rewrittenInstructions = code.instructions;
    assert rewrittenInstructions[1] instanceof IfEqz;
    IfEqz condition = (IfEqz) rewrittenInstructions[1];
    assert condition.getOffset() + condition.BBBB == rewrittenInstructions[3].getOffset();
    assert rewrittenInstructions[2] instanceof Goto32;
    Goto32 jump = (Goto32) rewrittenInstructions[2];
    Instruction lastInstruction = rewrittenInstructions[rewrittenInstructions.length - 1];
    assert jump.getOffset() + jump.AAAAAAAA == lastInstruction.getOffset();
  }

  private Instruction[] buildInstructions(DexString string, boolean zeroCondition) {
    List<Instruction> instructions = new ArrayList<>();
    int offset = 0;
    Instruction instr = new Const4(0, 0);
    instr.setOffset(offset);
    instructions.add(instr);
    offset += instr.getSize();
    int lastInstructionOffset = 15000 * 2 + 2 + offset;
    if (zeroCondition) {
      instr = new IfNez(0, lastInstructionOffset - offset);
    } else {
      instr = new IfNe(0, 0, lastInstructionOffset - offset);
    }
    instr.setOffset(offset);
    instructions.add(instr);
    offset += instr.getSize();
    for (int i = 0; i < 15000; i++) {
      instr = new ConstString(0, string);
      instr.setOffset(offset);
      instructions.add(instr);
      offset += instr.getSize();
    }
    instr = new ReturnVoid();
    instr.setOffset(offset);
    instructions.add(instr);
    assert instr.getOffset() == lastInstructionOffset;
    return instructions.toArray(new Instruction[instructions.size()]);
  }

  private DexCode jumboStringProcess(
      DexItemFactory factory, DexString string, Instruction[] instructions) {
    DexCode code = new DexCode(
        1,
        0,
        0,
        instructions,
        new Try[0],
        null,
        null,
        null);
    DexAccessFlags flags = new DexAccessFlags(0);
    flags.setPublic();
    DexEncodedMethod method = new DexEncodedMethod(null, flags, null, null, code);
    new JumboStringRewriter(method, string, factory).rewrite();
    return method.getCode().asDexCode();
  }
}
