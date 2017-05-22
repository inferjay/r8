// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package interfacemethods;

public interface I2 {

  default void d1() {
    System.out.println("d1");
  }
}
