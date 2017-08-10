// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package naming001;

public class Reflect2 {
  public volatile int fieldPublic;

  private volatile int fieldPrivate;

  public volatile long fieldLong;

  private volatile long fieldLong2;

  volatile long fieldLong3;

  protected volatile long fieldLong4;

  public volatile A a;

  public volatile B b;

  private volatile Object c;

  private void calledMethod() {
  }

  public void m(A a) {
  }

  private void privateMethod(B b) {
  }

  class A {
  }

  class B {
  }
}

