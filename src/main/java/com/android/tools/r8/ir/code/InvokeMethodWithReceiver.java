// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.graph.DexMethod;
import java.util.List;

public abstract class InvokeMethodWithReceiver extends InvokeMethod {

  private boolean isDominatedByCallWithSameReceiver = false;

  InvokeMethodWithReceiver(DexMethod target, Value result, List<Value> arguments) {
    super(target, result, arguments);
  }

  public void setIsDominatedByCallWithSameReceiver() {
    isDominatedByCallWithSameReceiver = true;
  }

  public boolean receiverIsNeverNull() {
    return isDominatedByCallWithSameReceiver || arguments().get(0).isNeverNull();
  }

  @Override
  public boolean isInvokeMethodWithReceiver() {
    return true;
  }

  @Override
  public InvokeMethodWithReceiver asInvokeMethodWithReceiver() {
    return this;
  }
}
