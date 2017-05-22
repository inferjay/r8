// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

import com.android.tools.r8.code.Iput;
import com.android.tools.r8.code.IputBoolean;
import com.android.tools.r8.code.IputByte;
import com.android.tools.r8.code.IputChar;
import com.android.tools.r8.code.IputObject;
import com.android.tools.r8.code.IputShort;
import com.android.tools.r8.code.IputWide;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.ir.conversion.DexBuilder;
import java.util.List;

public class InstancePut extends FieldInstruction {

  public InstancePut(MemberType type, List<Value> values, DexField field) {
    super(type, field, null, values);
  }

  public Value value() {
    return inValues.get(0);
  }

  public Value object() {
    return inValues.get(1);
  }

  @Override
  public void buildDex(DexBuilder builder) {
    com.android.tools.r8.code.Instruction instruction;
    int valueRegister = builder.allocatedRegister(value(), getNumber());
    int objectRegister = builder.allocatedRegister(object(), getNumber());
    switch (type) {
      case SINGLE:
        instruction = new Iput(valueRegister, objectRegister, field);
        break;
      case WIDE:
        instruction = new IputWide(valueRegister, objectRegister, field);
        break;
      case OBJECT:
        instruction = new IputObject(valueRegister, objectRegister, field);
        break;
      case BOOLEAN:
        instruction = new IputBoolean(valueRegister, objectRegister, field);
        break;
      case BYTE:
        instruction = new IputByte(valueRegister, objectRegister, field);
        break;
      case CHAR:
        instruction = new IputChar(valueRegister, objectRegister, field);
        break;
      case SHORT:
        instruction = new IputShort(valueRegister, objectRegister, field);
        break;
      default:
        throw new Unreachable("Unexpected type " + type);
    }
    builder.add(this, instruction);
  }

  @Override
  public boolean instructionTypeCanThrow() {
    return true;
  }

  @Override
  public boolean identicalNonValueParts(Instruction other) {
    InstancePut o = other.asInstancePut();
    return o.field == field && o.type == type;
  }

  public int compareNonValueParts(Instruction other) {
    InstancePut o = other.asInstancePut();
    int result = field.slowCompareTo(o.field);
    if (result != 0) {
      return result;
    }
    return type.ordinal() - o.type.ordinal();
  }

  @Override
  public int maxInValueRegister() {
    return Constants.U4BIT_MAX;
  }

  @Override
  public int maxOutValueRegister() {
    assert false : "InstancePut instructions define no values.";
    return 0;
  }

  @Override
  public boolean isInstancePut() {
    return true;
  }

  @Override
  public InstancePut asInstancePut() {
    return this;
  }

  @Override
  public String toString() {
    return super.toString() + "; field: " + field.toSourceString();
  }
}
