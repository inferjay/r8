// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

public abstract class UseRegistry {

  public abstract boolean registerInvokeVirtual(DexMethod method);

  public abstract boolean registerInvokeDirect(DexMethod method);

  public abstract boolean registerInvokeStatic(DexMethod method);

  public abstract boolean registerInvokeInterface(DexMethod method);

  public abstract boolean registerInvokeSuper(DexMethod method);

  public abstract boolean registerInstanceFieldWrite(DexField field);

  public abstract boolean registerInstanceFieldRead(DexField field);

  public abstract boolean registerNewInstance(DexType type);

  public abstract boolean registerStaticFieldRead(DexField field);

  public abstract boolean registerStaticFieldWrite(DexField field);

  public abstract boolean registerTypeReference(DexType type);
}
