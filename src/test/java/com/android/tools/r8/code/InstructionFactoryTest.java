// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.code;

import static org.junit.Assert.assertTrue;

import com.android.tools.r8.graph.OffsetToObjectMapping;
import java.nio.ByteBuffer;
import org.junit.Test;

/**
 * Tests for the InstructionFactory.
 */
public class InstructionFactoryTest {

  @Test
  public void emptyBuffer() {
    ByteBuffer emptyBuffer = ByteBuffer.allocate(0);
    InstructionFactory factory = new InstructionFactory();
    Instruction[] instructions =
        factory.readSequenceFrom(emptyBuffer, 0, 0, new OffsetToObjectMapping());
    assertTrue(instructions.length == 0);
  }
}
