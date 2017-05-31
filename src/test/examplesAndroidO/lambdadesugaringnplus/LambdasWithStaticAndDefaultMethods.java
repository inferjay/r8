// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package lambdadesugaringnplus;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import lambdadesugaringnplus.other.ClassWithDefaultPackagePrivate;
import lambdadesugaringnplus.other.InterfaceWithDefaultPackagePrivate;

public class LambdasWithStaticAndDefaultMethods {
  interface I {
    String iRegular();

    static String iStatic() {
      return "I::iStatic()";
    }

    default String iDefault() {
      return "I::iDefault()";
    }

    default String iDefaultOverridden() {
      return "I::iDefaultOverridden()";
    }

    default II stateless() {
      return () -> "I::stateless()";
    }

    default II stateful() {
      return () -> "I::captureThis(" + stateless().iRegular() + ")";
    }
  }

  static class C implements I {
    @Override
    public String iRegular() {
      return "C::iRegular()";
    }

    @Override
    public String iDefaultOverridden() {
      return "C::iDefaultOverridden()";
    }
  }

  interface II extends I {
    static String iStatic() {
      II ii = I::iStatic;
      return "II::iStatic(" + ((I) I::iStatic).iRegular() +
          "|" + ((II) ii::iDefaultOverridden).iRegular() +
          "|" + ((II) String::new).iRegular() +
          "|" + ((II) ii::iRegular).iRegular() + ")";
    }

    default String iDefaultOverridden() {
      return "II::iDefault(" + ((I) this::iDefault).iRegular() +
          "|" + ((II) "One-Two-Three"::intern).iRegular() +
          "|" + ((II) this::iDefault).iRegular() + ")";
    }
  }

  interface P {
    String get();
  }

  static void p(P p) {
    System.out.println(p.get());
  }

  interface X<T> {
    String foo(T t);
  }

  interface Y<T extends I> extends X<T> {
    String foo(T t);
  }

  interface Z extends Y<II> {
    String foo(II t);
  }

  interface G<T> {
    T foo(T t);
  }

  interface B38257361_I1<T extends Number> {
    default T copy(T t) {
      return t;
    }
  }

  interface B38257361_I2 extends B38257361_I1<Integer> {
    @Override
    default Integer copy(Integer t) {
      return B38257361_I1.super.copy(t);
    }
  }

  static class B38257361_C implements B38257361_I2 {
  }

  static class B38257361 {
    private B38257361_C c = new B38257361_C();

    public Integer test(Integer i) {
      return c.copy(i);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Number test(Number n) {
      return ((B38257361_I1) c).copy(n);
    }

    public static void test() {
      B38257361 l = new B38257361();
      Integer i = new Integer(1);
      if (i.equals(l.test(i))) {
        System.out.println("Check 1: OK");
      } else {
        System.out.println("Check 1: NOT OK");
      }
      if (i.equals(l.test((Number) i))) {
        System.out.println("Check 2: OK");
      } else {
        System.out.println("Check 2: NOT OK");
      }
      try {
        Double d = new Double(1);
        if (d.equals(l.test((Number) d))) {
          System.out.println("Check 3: NOT OK, classCastException expected");
        } else {
          System.out.println("Check 3: NOT OK, classCastException expected");
        }
        System.out.println("Error, ClassCastException is expected");
      } catch (ClassCastException e) {
        // Class cast into the bridge method is expected
        System.out.println("OK, ClassCastException is expected");
      }
    }
  }

  interface B38257037_I1 {
    default Number getNumber() {
      return new Integer(1);
    }
  }

  interface B38257037_I2 extends B38257037_I1 {
    @Override
    default Double getNumber() {
      return new Double(2.3);
    }
  }

  static class B38257037_C implements B38257037_I2 {
  }

  /**
   * Check that bridges are generated.
   */
  static class B38257037 {
    private B38257037_C c = new B38257037_C();

    public Double test1() {
      return c.getNumber();
    }

    public Number test2() {
      return ((B38257037_I1) c).getNumber();
    }

    public static void test() {
      B38257037 l = new B38257037();
      if (l.test1() == 2.3) {
        System.out.println("Check 1: OK");
      } else {
        System.out.println("Check 1: NOT OK");
      }
      if (l.test2().equals(new Double(2.3))) {
        System.out.println("Check 2: OK");
      } else {
        System.out.println("Check 2: NOT OK");
      }
    }
  }

  interface B38306708_I {
    class $CC{
      static void print() {
        System.out.println("$CC");
      }
    }

    default String m() {
      return "ITop.m()";
    }
  }

  static class B38306708 {
    public static void test() {
      B38306708_I.$CC.print();
    }
  }

  interface B38308515_I {
    default String m() {
      return "m instance";
    }

    static String m(B38308515_I i) {
      return "m static";
    }
  }

  static class B38308515_C implements B38308515_I {
  }

  static class B38308515 {
    static void test() {
      B38308515_C c = new B38308515_C();
      System.out.println(c.m());
      System.out.println(B38308515_I.m(c));
    }
  }

  static class B38302860 {

    @SomeAnnotation(1)
    private interface AnnotatedInterface {

      @SomeAnnotation(2)
      void annotatedAbstractMethod();

      @SomeAnnotation(3)
      default void annotatedDefaultMethod() {
      }

      @SomeAnnotation(4)
      static void annotatedStaticMethod() {
      }
    }

    @Retention(value = RetentionPolicy.RUNTIME)
    private @interface SomeAnnotation {
      int value();
    }

    private static boolean checkAnnotationValue(Annotation[] annotations, int value) {
      if (annotations.length != 1) {
        return false;
      }
      return annotations[0] instanceof SomeAnnotation
          && ((SomeAnnotation) annotations[0]).value() == value;
    }

    @SuppressWarnings("unchecked")
    static void test() throws Exception {
      if (checkAnnotationValue(AnnotatedInterface.class.getAnnotations(), 1)) {
        System.out.println("Check 1: OK");
      } else {
        System.out.println("Check 1: NOT OK");
      }

      if (checkAnnotationValue(
          AnnotatedInterface.class.getMethod("annotatedAbstractMethod").getAnnotations(), 2)) {
        System.out.println("Check 2: OK");
      } else {
        System.out.println("Check 2: NOT OK");
      }

      if (checkAnnotationValue(
          AnnotatedInterface.class.getMethod("annotatedDefaultMethod").getAnnotations(), 3)) {
        System.out.println("Check 3: OK");
      } else {
        System.out.println("Check 3: NOT OK");
      }

      if (checkAnnotationValue(
          getCompanionClassOrInterface().getMethod("annotatedStaticMethod").getAnnotations(), 4)) {
        System.out.println("Check 4: OK");
      } else {
        System.out.println("Check 4: NOT OK");
      }
    }

    private static Class getCompanionClassOrInterface() {
      try {
        return Class.forName("lambdadesugaringnplus."
            + "LambdasWithStaticAndDefaultMethods$B38302860$AnnotatedInterface-CC");
      } catch (Exception e) {
        return AnnotatedInterface.class;
      }
    }
  }

  static class B62168701 {
    interface I extends Serializable {
      String getValue();
    }

    interface J {
      static void dump() {
        I i = () -> "B62168701 -- OK";
        System.out.println(i.getValue());
      }
    }

    static void test() {
      J.dump();
    }
  }

  static void z(Z p) {
    System.out.println(p.foo(null));
  }

  static void g(G<String[]> g) {
    StringBuilder builder = new StringBuilder("{");
    String sep = "";
    for (String s : g.foo(new String[] { "Arg0", "Arg1", "Arg2" })) {
      builder.append(sep).append(s);
      sep = ", ";
    }
    builder.append("}");
    System.out.println(builder.toString());
  }

  interface SuperChain {
    default String iMain() {
      return "SuperChain::iMain()";
    }
  }

  interface SuperChainDerived extends SuperChain {
    default String iMain() {
      return "SuperChainDerived::iMain(" + SuperChain.super.iMain() + ")";
    }
  }

  interface OtherSuperChain {
    default String iMain() {
      return "OtherSuperChain::iMain()";
    }
  }

  static class ClassWithSuperChain implements SuperChainDerived, OtherSuperChain {
    public String iMain() {
      return "ClassWithSuperChain::iMain(" + SuperChainDerived.super.iMain() + ")" + iMainImpl();
    }

    public String iMainImpl() {
      return "ClassWithSuperChain::iMain(" + SuperChainDerived.super.iMain() +
          " + " + OtherSuperChain.super.iMain() + ")";
    }
  }

  public static void main(String[] args) throws Exception {
    C c = new C();
    I i = c;

    c.iRegular();
    c.iDefault();
    c.iDefaultOverridden();
    I.iStatic();
    i.iRegular();
    i.iDefault();
    i.iDefaultOverridden();

    p(i.stateless()::iRegular);
    p(i.stateful()::iRegular);

    g(a -> a);
    g(a -> {
      int size = a.length;
      for (int x = 0; x < size / 2; x++) {
        String t = a[x];
        a[x] = a[size - 1 - x];
        a[size - 1 - x] = t;
      }
      return a;
    });

    p(c::iRegular);
    p(c::iDefault);
    p(c::iDefaultOverridden);
    p(I::iStatic);
    p(i::iRegular);
    p(i::iDefault);
    p(i::iDefaultOverridden);

    II ii = i::iRegular;
    p(II::iStatic);
    p(ii::iRegular);
    p(ii::iDefault);
    p(ii::iDefaultOverridden);

    z(s -> "From Interface With Bridges");

    System.out.println(new ClassWithSuperChain().iMain());

    ClassWithDefaultPackagePrivate c2 = new ClassWithDefaultPackagePrivate();
    InterfaceWithDefaultPackagePrivate i2 = c2;

    c2.defaultFoo();
    i2.defaultFoo();
    InterfaceWithDefaultPackagePrivate.staticFoo();

    p(c2::defaultFoo);
    p(i2::defaultFoo);
    p(InterfaceWithDefaultPackagePrivate::staticFoo);
    p(c2.lambda()::foo);

    B38257361.test();
    B38257037.test();
    B38306708.test();
    B38308515.test();
    B38302860.test();
    B62168701.test();
  }
}
