// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package shaking2;

public interface Interface extends SuperInterface1, SuperInterface2 {
  public void unusedInterfaceMethod();
  public void interfaceMethod();
  public void interfaceMethod4();
  public void interfaceMethod5(int a1, int a2, int a3, int a4, int a5, int a6, int a7, int a8);
}
