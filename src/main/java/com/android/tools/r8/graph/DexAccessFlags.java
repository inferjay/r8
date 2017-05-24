// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.dex.Constants;

public class DexAccessFlags {

  private static final String[] ACC_NAMES = {
      "public",
      "private",
      "protected",
      "static",
      "final",
      "synchronized",
      "volatile(bridge)",
      "transient(varargs)",
      "native",
      "interface",
      "abstract",
      "strictfp",
      "synthetic",
      "annotation",
      "enum",
      "<unused>",
      "<init>",
      "synchronized",
  };

  private int flags;

  public DexAccessFlags(int flags) {
    this.flags = flags;
  }

  public DexAccessFlags(int... flags) {
    this(combineFlags(flags));
  }

  private static int combineFlags(int[] flags) {
    int combined = 0;
    for (int flag : flags) {
      combined |= flag;
    }
    return combined;
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof DexAccessFlags) {
      return flags == ((DexAccessFlags) other).flags;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return get();
  }

  public int get() {
    return flags;
  }

  public boolean containsAllOf(DexAccessFlags other) {
    return (flags & other.get()) == other.get();
  }

  public boolean containsNoneOf(DexAccessFlags other) {
    return (flags & other.get()) == 0;
  }

  public boolean isPublic() {
    return isSet(Constants.ACC_PUBLIC);
  }

  public void setPublic() {
    set(Constants.ACC_PUBLIC);
  }

  public void unsetPublic() {
    unset(Constants.ACC_PUBLIC);
  }

  public boolean isPrivate() {
    return isSet(Constants.ACC_PRIVATE);
  }

  public void setPrivate() {
    set(Constants.ACC_PRIVATE);
  }

  public void unsetPrivate() {
    unset(Constants.ACC_PRIVATE);
  }

  public boolean isProtected() {
    return isSet(Constants.ACC_PROTECTED);
  }

  public void setProtected() {
    set(Constants.ACC_PROTECTED);
  }

  public void unsetProtected() {
    unset(Constants.ACC_PROTECTED);
  }

  public boolean isStatic() {
    return isSet(Constants.ACC_STATIC);
  }

  public void setStatic() {
    set(Constants.ACC_STATIC);
  }

  public boolean isFinal() {
    return isSet(Constants.ACC_FINAL);
  }

  public void setFinal() {
    set(Constants.ACC_FINAL);
  }

  public void unsetFinal() {
    unset(Constants.ACC_FINAL);
  }

  public boolean isSynchronized() {
    return isSet(Constants.ACC_SYNCHRONIZED);
  }

  public void setSynchronized() {
    set(Constants.ACC_SYNCHRONIZED);
  }

  public void unsetSynchronized() {
    unset(Constants.ACC_SYNCHRONIZED);
  }

  public boolean isVolatile() {
    return isSet(Constants.ACC_VOLATILE);
  }

  public void setVolatile() {
    set(Constants.ACC_VOLATILE);
  }

  public boolean isBridge() {
    return isSet(Constants.ACC_BRIDGE);
  }

  public void setBridge() {
    set(Constants.ACC_BRIDGE);
  }

  public void unsetBridge() {
    unset(Constants.ACC_BRIDGE);
  }

  public boolean isTransient() {
    return isSet(Constants.ACC_TRANSIENT);
  }

  public void setTransient() {
    set(Constants.ACC_TRANSIENT);
  }

  public boolean isVarargs() {
    return isSet(Constants.ACC_VARARGS);
  }

  public void setVarargs() {
    set(Constants.ACC_VARARGS);
  }

  public boolean isNative() {
    return isSet(Constants.ACC_NATIVE);
  }

  public void setNative() {
    set(Constants.ACC_NATIVE);
  }

  public boolean isInterface() {
    return isSet(Constants.ACC_INTERFACE);
  }

  public void setInterface() {
    set(Constants.ACC_INTERFACE);
  }

  public void unsetInterface() {
    unset(Constants.ACC_INTERFACE);
  }

  public boolean isAbstract() {
    return isSet(Constants.ACC_ABSTRACT);
  }

  public void setAbstract() {
    set(Constants.ACC_ABSTRACT);
  }

  public void unsetAbstract() {
    unset(Constants.ACC_ABSTRACT);
  }

  public boolean isStrict() {
    return isSet(Constants.ACC_STRICT);
  }

  public void setStrict() {
    set(Constants.ACC_STRICT);
  }

  public boolean isSynthetic() {
    return isSet(Constants.ACC_SYNTHETIC);
  }

  public void setSynthetic() {
    set(Constants.ACC_SYNTHETIC);
  }

  public void unsetSynthetic() {
    unset(Constants.ACC_SYNTHETIC);
  }

  public boolean isAnnotation() {
    return isSet(Constants.ACC_ANNOTATION);
  }

  public void setAnnotation() {
    set(Constants.ACC_ANNOTATION);
  }

  public boolean isEnum() {
    return isSet(Constants.ACC_ENUM);
  }

  public void setEnum() {
    set(Constants.ACC_ENUM);
  }

  public boolean isConstructor() {
    return isSet(Constants.ACC_CONSTRUCTOR);
  }

  public void setConstructor() {
    set(Constants.ACC_CONSTRUCTOR);
  }

  public void unsetConstructor() {
    unset(Constants.ACC_CONSTRUCTOR);
  }

  public boolean isDeclaredSynchronized() {
    return isSet(Constants.ACC_DECLARED_SYNCHRONIZED);
  }

  public void setDeclaredSynchronized() {
    set(Constants.ACC_DECLARED_SYNCHRONIZED);
  }

  public void promoteNonPrivateToPublic() {
    if (!isPrivate()) {
      flags &= ~Constants.ACC_PROTECTED;
      flags |= Constants.ACC_PUBLIC;
    }
  }

  public void promoteToPublic() {
    flags &= ~Constants.ACC_PROTECTED & ~Constants.ACC_PRIVATE;
    flags |= Constants.ACC_PUBLIC;
  }

  private boolean isSet(int flag) {
    return (flags & flag) != 0;
  }

  private void set(int flag) {
    flags |= flag;
  }

  private void unset(int flag) {
    flags &= ~flag;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    int flags = this.flags;
    flags &= ~Constants.ACC_CONSTRUCTOR;  // Don't include the constructor flag in the string.
    for (int i = 0; i < ACC_NAMES.length && flags != 0; i++, flags >>= 1) {
      if ((flags & 0x1) != 0) {
        if (builder.length() > 0) {
          builder.append(' ');
        }
        builder.append(ACC_NAMES[i]);
      }
    }
    assert flags == 0;
    return builder.toString();
  }

  public String toSmaliString() {
    return toString();
  }
}
