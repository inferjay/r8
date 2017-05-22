// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

public class DebugLocalInfo {
  public final DexString name;
  public final DexType type;
  public final DexString signature;

  public DebugLocalInfo(DexString name, DexType type, DexString signature) {
    this.name = name;
    this.type = type;
    this.signature = signature;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof DebugLocalInfo)) {
      return false;
    }
    DebugLocalInfo o = (DebugLocalInfo) other;
    return name == o.name && type == o.type && signature == o.signature;
  }

  @Override
  public int hashCode() {
    int hash = 7 * name.hashCode() + 13 * type.hashCode();
    if (signature != null) {
      hash += 31 * signature.hashCode();
    }
    return hash;
  }

  @Override
  public String toString() {
    return name + ":" + type + (signature == null ? "" : signature);
  }
}
