// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

import com.android.tools.r8.errors.InternalCompilerError;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.DexType;

public enum MoveType {
  SINGLE,
  WIDE,
  OBJECT;

  public int requiredRegisters() {
    return this == MoveType.WIDE ? 2 : 1;
  }

  public static MoveType fromMemberType(MemberType type) {
    switch (type) {
      case BOOLEAN:
      case BYTE:
      case CHAR:
      case SHORT:
      case SINGLE:
        return MoveType.SINGLE;
      case WIDE:
        return MoveType.WIDE;
      case OBJECT:
        return MoveType.OBJECT;
    }
    assert false;
    return null;
  }

  public static MoveType fromTypeDescriptorChar(char descriptor) {
    switch (descriptor) {
      case 'L':  // object
      case '[':  // array
        return MoveType.OBJECT;
      case 'Z':  // boolean
      case 'B':  // byte
      case 'S':  // short
      case 'C':  // char
      case 'I':  // int
      case 'F':  // float
        return MoveType.SINGLE;
      case 'J':  // long
      case 'D':  // double
        return MoveType.WIDE;
      case 'V':
        throw new InternalCompilerError("No move type for void type.");
      default:
        throw new Unreachable("Invalid descriptor char '" + descriptor + "'");
    }
  }

  public static MoveType fromDexType(DexType type) {
    return fromTypeDescriptorChar((char) type.descriptor.content[0]);
  }

  public static MoveType fromConstType(ConstType type) {
    switch (type) {
      case INT:
      case FLOAT:
      case INT_OR_FLOAT:
        return MoveType.SINGLE;
      case OBJECT:
        return MoveType.OBJECT;
      case LONG:
      case DOUBLE:
      case LONG_OR_DOUBLE:
        return MoveType.WIDE;
      default:
        throw new Unreachable("Invalid const type '" + type + "'");
    }
  }

  public static MoveType fromNumericType(NumericType type) {
    switch (type) {
      case BYTE:
      case CHAR:
      case SHORT:
      case INT:
      case FLOAT:
        return MoveType.SINGLE;
      case LONG:
      case DOUBLE:
        return MoveType.WIDE;
      default:
        throw new Unreachable("Invalid numeric type '" + type + "'");
    }
  }
}
