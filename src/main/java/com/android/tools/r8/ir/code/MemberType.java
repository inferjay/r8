// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

import com.android.tools.r8.errors.InternalCompilerError;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.DexType;

public enum MemberType {
  SINGLE,
  WIDE,
  OBJECT,
  BOOLEAN,
  BYTE,
  CHAR,
  SHORT;

  public static MoveType moveTypeFor(MemberType type) {
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
    return null;
  }

  public static MemberType fromTypeDescriptorChar(char descriptor) {
    switch (descriptor) {
      case 'L':  // object
      case '[':  // array
        return MemberType.OBJECT;
      case 'Z':  // boolean
        return MemberType.BOOLEAN;
      case 'B':  // byte
        return MemberType.BYTE;
      case 'S':  // short
        return MemberType.SHORT;
      case 'C':  // char
        return MemberType.CHAR;
      case 'I':  // int
      case 'F':  // float
        return MemberType.SINGLE;
      case 'J':  // long
      case 'D':  // double
        return MemberType.WIDE;
      case 'V':
        throw new InternalCompilerError("No member type for void type.");
      default:
        throw new Unreachable("Invalid descriptor char '" + descriptor + "'");
    }
  }

  public static MemberType fromDexType(DexType type) {
    return fromTypeDescriptorChar((char) type.descriptor.content[0]);
  }
}
