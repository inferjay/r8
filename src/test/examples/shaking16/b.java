// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package shaking16;

public class b {
  
  public int a;
  public int b;
  
  public b(int a, int b) {
    this.a = a;
    this.b = b;
  }

  public void a() {
    System.out.println("Called a in Superclass" + a + ", " + b);
  }

  public void b() {
    System.out.println("Called b in Superclass");
  }
}
