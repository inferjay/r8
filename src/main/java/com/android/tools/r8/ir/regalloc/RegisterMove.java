// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.regalloc;

import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.MoveType;
import java.util.Map;
import java.util.Set;

// Register moves used by the spilling register allocator. These are used both for spill and
// for phi moves and they are moves between actual registers represented by their register number.
public class RegisterMove {
  MoveType type;
  int dst;
  int src;
  Instruction definition;

  public RegisterMove(int dst, int src, MoveType type) {
    this.dst = dst;
    this.src = src;
    this.type = type;
  }

  public RegisterMove(int dst, MoveType type, Instruction definition) {
    this.dst = dst;
    this.src = LinearScanRegisterAllocator.NO_REGISTER;
    this.type = type;
    assert definition.isConstInstruction();
    this.definition = definition;
  }


  private boolean writes(int register) {
    if (type == MoveType.WIDE && (dst + 1) == register) {
      return true;
    }
    return dst == register;
  }

  public boolean isBlocked(Set<RegisterMove> moveSet, Map<Integer, Integer> valueMap) {
    for (RegisterMove move : moveSet) {
      if (move.src == LinearScanRegisterAllocator.NO_REGISTER) {
        continue;
      }
      if (move != this) {
        if (writes(valueMap.get(move.src))) {
          return true;
        }
        if (move.type == MoveType.WIDE) {
          if (writes(valueMap.get(move.src) + 1)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    return src + dst * 3 + type.hashCode() * 5 + (definition == null ? 0 : definition.hashCode());
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof RegisterMove)) {
      return false;
    }
    RegisterMove o = (RegisterMove) other;
    return o.src == src && o.dst == dst && o.type == type && o.definition == definition;
  }
}
