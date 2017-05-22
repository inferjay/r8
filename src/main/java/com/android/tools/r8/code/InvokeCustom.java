// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.code;

import com.android.tools.r8.graph.DexCallSite;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexMethodHandle;
import com.android.tools.r8.graph.DexValue;
import com.android.tools.r8.graph.DexValue.DexValueMethodHandle;
import com.android.tools.r8.graph.DexValue.DexValueType;
import com.android.tools.r8.graph.IndexedDexItem;
import com.android.tools.r8.graph.OffsetToObjectMapping;
import com.android.tools.r8.graph.UseRegistry;
import com.android.tools.r8.ir.conversion.IRBuilder;

public class InvokeCustom extends Format35c {

  public static final int OPCODE = 0xfc;
  public static final String NAME = "InvokeCustom";
  public static final String SMALI_NAME = "invoke-custom";

  InvokeCustom(int high, BytecodeStream stream, OffsetToObjectMapping mapping) {
    super(high, stream, mapping.getCallSiteMap());
  }

  public InvokeCustom(int A, IndexedDexItem BBBB, int C, int D, int E, int F, int G) {
    super(A, BBBB, C, D, E, F, G);
  }

  public String getName() {
    return NAME;
  }

  public String getSmaliName() {
    return SMALI_NAME;
  }

  public int getOpcode() {
    return OPCODE;
  }

  @Override
  public void registerUse(UseRegistry registry) {
    registerCallSite(registry, getCallSite());
  }

  @Override
  public DexCallSite getCallSite() {
    return (DexCallSite) BBBB;
  }

  @Override
  public void buildIR(IRBuilder builder) {
    builder.addInvokeCustomRegisters(getCallSite(), A, new int[]{C, D, E, F, G});
  }

  @Override
  public boolean canThrow() {
    return true;
  }

  static void registerCallSite(UseRegistry registry, DexCallSite callSite) {
    InvokeCustom.registerMethodHandle(registry, callSite.bootstrapMethod);

    // Register bootstrap method arguments, only Type and MethodHandle need to be register.
    for (DexValue arg : callSite.bootstrapArgs) {
      if (arg instanceof DexValueType) {
        registry.registerTypeReference(((DexValueType) arg).value);
      } else if (arg instanceof DexValueMethodHandle) {
        InvokeCustom.registerMethodHandle(registry, ((DexValueMethodHandle) arg).value);
      }
    }
  }

  static void registerMethodHandle(UseRegistry registry, DexMethodHandle methodHandle) {
    switch (methodHandle.type) {
      case INSTANCE_GET:
        registry.registerInstanceFieldRead(methodHandle.asField());
        break;
      case INSTANCE_PUT:
        registry.registerInstanceFieldWrite(methodHandle.asField());
        break;
      case STATIC_GET:
        registry.registerStaticFieldRead(methodHandle.asField());
        break;
      case STATIC_PUT:
        registry.registerStaticFieldWrite(methodHandle.asField());
        break;
      case INVOKE_INSTANCE:
        registry.registerInvokeVirtual(methodHandle.asMethod());
        break;
      case INVOKE_STATIC:
        registry.registerInvokeStatic(methodHandle.asMethod());
        break;
      case INVOKE_CONSTRUCTOR:
        DexMethod method = methodHandle.asMethod();
        registry.registerNewInstance(method.getHolder());
        registry.registerInvokeDirect(method);
        break;
      case INVOKE_INTERFACE:
        registry.registerInvokeInterface(methodHandle.asMethod());
        break;
      case INVOKE_SUPER:
        registry.registerInvokeSuper(methodHandle.asMethod());
        break;
      default:
        throw new AssertionError();
    }
  }
}
