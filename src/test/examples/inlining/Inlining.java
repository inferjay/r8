// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package inlining;

class A {

  int a;

  A(int a) {
    this.a = a;
  }

  int a() {
    return a;
  }
}

class B extends A {

  B(int a) {
    super(a);
  }
}

class InlineConstructor {

  int a;

  @CheckDiscarded
  InlineConstructor(int a) {
    this.a = a;
  }

  @CheckDiscarded
  InlineConstructor(long a) {
    this((int) a);
  }

  static InlineConstructor create() {
    return new InlineConstructor(10L);
  }
}

class InlineConstructorOfInner {

  class Inner {

    int a;

    @CheckDiscarded
    Inner(int a) {
      this.a = a;
    }

    // This is not inlined, even though it is only called once, as it is only called from a
    // non-constructor, and will set a field (the outer object) before calling the other
    // constructor.
    Inner(long a) {
      this((int) a);
    }

    public Inner create() {
      return new Inner(10L);
    }
  }

  Inner inner;

  InlineConstructorOfInner() {
    inner = new Inner(10L).create();
  }
}

public class Inlining {

  private static void Assert(boolean value) {
    if (!value) {
      System.out.println("FAILURE");
    }
  }

  public static void main(String[] args) {
    // Ensure the simple methods are called at least three times, to not be inlined due to being
    // called only once or twice.
    Assert(intExpression());
    Assert(intExpression());
    Assert(intExpression());
    Assert(longExpression());
    Assert(longExpression());
    Assert(longExpression());
    Assert(doubleExpression());
    Assert(floatExpression());
    Assert(floatExpression());
    Assert(floatExpression());
    Assert(stringExpression());
    Assert(stringExpression());
    Assert(stringExpression());

    Assert(intArgumentExpression());
    Assert(intArgumentExpression());
    Assert(intArgumentExpression());
    Assert(longArgumentExpression());
    Assert(longArgumentExpression());
    Assert(longArgumentExpression());
    Assert(doubleArgumentExpression());
    Assert(doubleArgumentExpression());
    Assert(doubleArgumentExpression());
    Assert(floatArgumentExpression());
    Assert(floatArgumentExpression());
    Assert(floatArgumentExpression());
    Assert(stringArgumentExpression());
    Assert(stringArgumentExpression());
    Assert(stringArgumentExpression());

    Assert(intAddExpression());
    Assert(intAddExpression());
    Assert(intAddExpression());

    A b = new B(42);
    A a = new A(42);
    Assert(intCmpExpression(a, b));
    Assert(intCmpExpression(a, b));
    Assert(intCmpExpression(a, b));

    // This is only called once!
    Assert(onlyCalledOnce(10));

    // This is only called twice, and is quite small!
    Assert(onlyCalledTwice(1) == 2);
    Assert(onlyCalledTwice(1) == 2);

    InlineConstructor ic = InlineConstructor.create();
    Assert(ic != null);
    InlineConstructorOfInner icoi = new InlineConstructorOfInner();
    Assert(icoi != null);
  }

  private static boolean intCmpExpression(A a, A b) {
    return a.a() == b.a();
  }

  @CheckDiscarded
  private static int intConstantInline() {
    return 42;
  }

  @CheckDiscarded
  private static boolean intExpression() {
    return 42 == intConstantInline();
  }

  @CheckDiscarded
  private static long longConstantInline() {
    return 50000000000L;
  }

  @CheckDiscarded
  private static boolean longExpression() {
    return 50000000000L == longConstantInline();
  }

  @CheckDiscarded
  private static double doubleConstantInline() {
    return 42.42;
  }

  @CheckDiscarded
  private static boolean doubleExpression() {
    return 42.42 == doubleConstantInline();
  }

  @CheckDiscarded
  private static float floatConstantInline() {
    return 21.21F;
  }

  @CheckDiscarded
  private static boolean floatExpression() {
    return 21.21F == floatConstantInline();
  }

  private static String stringConstantInline() {
    return "Fisk er godt";
  }

  private static boolean stringExpression() {
    return "Fisk er godt" == stringConstantInline();
  }

  @CheckDiscarded
  private static int intArgumentInline(int a, int b, int c) {
    return b;
  }

  @CheckDiscarded
  private static boolean intArgumentExpression() {
    return 42 == intArgumentInline(-2, 42, -1);
  }

  @CheckDiscarded
  private static long longArgumentInline(long a, long b, long c) {
    return c;
  }

  @CheckDiscarded
  private static boolean longArgumentExpression() {
    return 50000000000L == longArgumentInline(-2L, -1L, 50000000000L);
  }

  @CheckDiscarded
  private static double doubleArgumentInline(double a, double b, double c) {
    return a;
  }

  @CheckDiscarded
  private static boolean doubleArgumentExpression() {
    return 42.42 == doubleArgumentInline(42.42, -2.0, -1.0);
  }

  @CheckDiscarded
  private static float floatArgumentInline(float a, float b, float c) {
    return b;
  }

  @CheckDiscarded
  private static boolean floatArgumentExpression() {
    return 21.21F == floatArgumentInline(-2.0F, 21.21F, -1.0F);
  }

  @CheckDiscarded
  private static String stringArgumentInline(String a, String b, String c) {
    return c;
  }

  private static boolean stringArgumentExpression() {
    return "Fisk er godt" == stringArgumentInline("-1", "-1", "Fisk er godt");
  }

  @CheckDiscarded
  private static int intAddInline(int a, int b) {
    return a + b;
  }

  @CheckDiscarded
  private static boolean intAddExpression() {
    return 42 == intAddInline(21, 21);
  }

  @CheckDiscarded
  private static boolean onlyCalledOnce(int count) {
    int anotherCounter = 0;
    for (int i = 0; i < count; i++) {
      anotherCounter += i;
    }
    return anotherCounter > count;
  }

  @CheckDiscarded
  private static int onlyCalledTwice(int count) {
    return count > 0 ? count + 1 : count - 1;
  }
}
