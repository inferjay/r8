// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package instancevariable;

class InstanceVariable {
  public int foo = 42;
  public int bar;
  public InstanceVariable parent;

  private int privateFoo;
  private int privateBar = -1;
  private InstanceVariable privateParent;

  public InstanceVariable() {}

  public InstanceVariable(int bar) {
    this.bar = bar;
    privateFoo = 1;
    privateBar = 43;
    parent = new InstanceVariable();
    privateParent = new InstanceVariable();
  }

  public int sumAll() {
    return privateFoo + privateBar + bar + foo + parent.foo + privateParent.privateBar;
  }

  public static void main(String[] args) {
    InstanceVariable instanceVariable = new InstanceVariable(17);
    System.out.println(instanceVariable.sumAll() + "=144");
  }
}
