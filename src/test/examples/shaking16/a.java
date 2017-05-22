// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package shaking16;

public class a extends b {
  
  public int a;
  public int b;
  
  public a(int a, int b) {
    super(a -1, b + 1);
    this.a = a;
    this.b = b;
  }

  @Override
  public void a() {
    System.out.println("Called a in Subclass " + a + ", " + b);
  }

  @Override
  public void b() {
    System.out.println("Called b in Subclass");
    super.b();
    super.a();
  }
}
