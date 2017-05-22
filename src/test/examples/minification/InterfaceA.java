// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package minification;

public interface InterfaceA {

  /**
   * This method is also defined in {@link InterfaceB} and {@link InterfaceD}.
   */
  int functionFromIntToInt(int arg);

  /**
   * This signature is only defined here.
   */
  int uniqueLittleMethodInA();
}
