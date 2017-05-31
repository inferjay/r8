// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.shaking.includedescriptorclasses;

public class ClassWithNativeMethods {
  public static StaticFieldType staticField;
  public InstanceFieldType instanceField;
  public native void method1(NativeArgumentType a);
  public native NativeReturnType method2();
  public native NativeReturnType method3(NativeArgumentType a);
}
