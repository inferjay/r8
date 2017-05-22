// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package shaking10;

import shakinglib.IndirectMethodInvoker;

public class Shaking {

  public static void main(String[] args) {
    HasAFooImpl impl = new HasAFooImpl();
    IndirectMethodInvoker invoker = new IndirectMethodInvoker();
    System.out.println(invoker.invokeFoo(impl));
  }
}
