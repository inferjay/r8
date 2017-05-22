// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package lambdadesugaring.legacy;

public class Legacy {
  public static class A {
    private final String toString;

    public A(String toString) {
      this.toString = toString;
    }

    @Override
    public String toString() {
      return toString;
    }
  }

  public static class B extends A {
    public B(String toString) {
      super(toString);
    }
  }

  public static class C extends B {
    public C(String toString) {
      super(toString);
    }
  }

  public static class D extends C {
    public D(String toString) {
      super(toString);
    }
  }

  public interface BI<T> {
    T foo(T o1);
  }

  public interface BH extends BI<String> {
  }

  public interface BK<T extends A> extends BI<T> {
    T foo(T o1);
  }

  public interface BL<T extends B> extends BK<T> {
    T foo(T o1);
  }

  public interface BM extends BK<C> {
    C foo(C o1);
  }
}
