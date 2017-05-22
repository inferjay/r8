// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package bridge;

abstract class Super<T> {
  public abstract int method(T t0, T t1);
  public abstract int rangeMethod(T t0, T t1, T t2, T t3, T t4, T t5);
}

public class BridgeMethod extends Super<Integer> {

  @Override
  public int method(Integer t0, Integer t1) {
    if (t0 > t1) {
      return t0;
    }
    return t1;
  }

  @Override
  public int rangeMethod(Integer t0, Integer t1, Integer t2, Integer t3, Integer t4, Integer t5) {
    if (t0 > t1) {
      return t0;
    }
    return t1 + t2 + t3 + t4 + t5;
  }

  public static void main(String[] args) {
    Super<Integer> instance = new BridgeMethod();
    instance.method(1, 2);
    instance.method(2, 1);
    instance.rangeMethod(1, 2, 3, 4, 5, 6);
    instance.rangeMethod(2, 1, 3, 4, 5, 6);
  }
}
