// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.errors.Unreachable;

public enum ConstType {
  INT,
  LONG,
  FLOAT,
  DOUBLE,
  OBJECT,
  INT_OR_FLOAT,
  LONG_OR_DOUBLE;

  public static ConstType fromNumericType(NumericType type) {
    switch (type) {
      case BYTE:
      case CHAR:
      case SHORT:
      case INT:
        return INT;
      case LONG:
        return LONG;
      case FLOAT:
        return FLOAT;
      case DOUBLE:
        return DOUBLE;
      default:
        throw new Unreachable("Invalid numeric type '" + type + "'");
    }
  }

  public static ConstType fromMoveType(MoveType moveType) {
    switch (moveType) {
      case SINGLE:
        return INT_OR_FLOAT;
      case WIDE:
        return LONG_OR_DOUBLE;
      case OBJECT:
        // Currently constants never have type OBJECT even when it is the null object.
        return INT;
      default:
        throw new Unreachable("Invalid move type '" + moveType + "'");
    }
  }
}
