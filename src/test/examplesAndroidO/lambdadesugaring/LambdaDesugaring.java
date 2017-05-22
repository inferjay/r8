// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package lambdadesugaring;

import java.io.Serializable;
import java.util.ArrayList;
import lambdadesugaring.legacy.Legacy;
import lambdadesugaring.other.OtherRefs;

public class LambdaDesugaring {
  interface I {
    String foo();
  }

  interface V {
    void foo();
  }

  interface VT<T> {
    void foo(T t);
  }

  interface P1<X> {
    X foo(int i);
  }

  interface I2 extends I {
  }

  interface I3 {
    String foo();
  }

  interface M1 {
  }

  interface M2 {
  }

  interface J {
    String foo(String a, int b, boolean c);
  }

  interface G {
    A foo();
  }

  interface H<T extends A> {
    T foo(T o);
  }

  interface K {
    Object foo(String a, String b, String c);
  }

  interface ObjectProvider {
    Object act();
  }

  interface S2Z {
    boolean foo(String a);
  }

  interface SS2Z {
    boolean foo(String a, String b);
  }

  interface ArrayTransformerA<T> {
    T[] transform(T[] a);
  }

  @SuppressWarnings("unchecked")
  interface ArrayTransformerB<T> {
    T[] transform(T... a);
  }

  static <T> void print(T[] a) {
    StringBuilder builder = new StringBuilder("{");
    String sep = "";
    for (T s : a) {
      builder.append(sep).append(s.toString());
      sep = ", ";
    }
    builder.append("}");
    System.out.println(builder.toString());
  }

  <T> T[] reorder(T[] a) {
    int size = a.length;
    for (int x = 0; x < size / 2; x++) {
      T t = a[x];
      a[x] = a[size - 1 - x];
      a[size - 1 - x] = t;
    }
    return a;
  }

  static void atA(ArrayTransformerA<Integer> f) {
    print(f.transform(new Integer[] { 1, 2, 3 }));
  }

  static void atB(ArrayTransformerB<String> f) {
    print(f.transform("A", "B", "C"));
  }

  public static String staticUnused() {
    return "ReleaseTests::staticUnused";
  }

  public static void testUnusedLambdas() {
    System.out.print("Before unused ... ");
    Object o = (I) LambdaDesugaring::staticUnused;
    System.out.println("after unused.");
  }

  class A {
    final String toString;

    A(String toString) {
      this.toString = toString;
    }

    @Override
    public String toString() {
      return toString;
    }
  }

  class B extends A {
    B(String toString) {
      super(toString);
    }
  }

  class C extends B {
    C(String toString) {
      super(toString);
    }
  }

  class D extends C {
    D(String toString) {
      super(toString);
    }
  }

  public static class Refs {
    public static String f(I i) {
      return i.foo();
    }

    public static void v(V v) {
      v.foo();
    }

    public static void vt(VT<String> v) {
      v.foo(null);
    }

    public static String p1(P1 p) {
      return p.foo(123).getClass().getCanonicalName();
    }

    public static String pSS2Z(SS2Z p) {
      return "" + p.foo("123", "321");
    }

    public static String pS2Z(S2Z p) {
      return "" + p.foo("123");
    }

    public static String p3(K k) {
      return k.foo("A", "B", "C").toString();
    }

    public static String g(ObjectProvider op) {
      return op.act().toString();
    }

    static class A extends OtherRefs {
      String fooInternal() {
        return "Refs::A::fooInternal()";
      }

      protected String fooProtected() {
        return "Refs::A::fooProtected()";
      }

      protected String fooProtectedOverridden() {
        return "Refs::A::fooProtectedOverridden()";
      }

      protected static String staticProtected() {
        return "Refs::A::staticProtected()";
      }

      static String staticInternal() {
        return "Refs::A::staticInternal()";
      }
    }

    public static class B extends A {
      public void test() {
        System.out.println(f(new A()::fooInternal));
        System.out.println(f(this::fooInternal));
        System.out.println(f(this::fooProtected));
        System.out.println(f(this::fooProtectedOverridden));
        System.out.println(f(this::fooPublic));
        System.out.println(f(this::fooInternal));

        System.out.println(f(super::fooProtectedOverridden));
        System.out.println(f(this::fooOtherProtected));
        System.out.println(f(this::fooOtherPublic));

        System.out.println(g(this::fooPrivate));
        System.out.println(g(new Integer(123)::toString));
        System.out.println(g(System::lineSeparator));

        System.out.println(f(A::staticInternal));
        System.out.println(f(A::staticProtected));
        System.out.println(f(B::staticPrivate));
        System.out.println(f(OtherRefs::staticOtherPublic));
        System.out.println(f(OtherRefs::staticOtherProtected));

        System.out.println(g(StringBuilder::new));
        System.out.println(g(OtherRefs.PublicInit::new));
        System.out.println(ProtectedInit.testProtected());
        System.out.println(g(ProtectedInit::new));
        System.out.println(g(InternalInit::new));
        System.out.println(PrivateInit.testPrivate());
        System.out.println(g(PrivateInit::new));

        System.out.println(p1(D[]::new));
        System.out.println(p1(Integer::new));
        System.out.println(p1(B::staticArray));

        System.out.println(pSS2Z(String::equalsIgnoreCase));
        System.out.println(pS2Z("123321"::contains));
        System.out.println(pS2Z(String::isEmpty));

        System.out.println(p3(B::fooConcat));

        v(D::new); // Discarding the return value
        vt((new ArrayList<String>())::add);

        I3 i3 = this::fooPrivate;
        System.out.println(f(i3::foo));
      }

      private static String staticPrivate() {
        return "Refs::B::staticPrivate()";
      }

      private String fooPrivate() {
        return "Refs::B::fooPrivate()";
      }

      String fooInternal() {
        return "Refs::B::fooInternal()";
      }

      public static StringBuilder fooConcat(Object... objs) {
        StringBuilder builder = new StringBuilder("Refs::B::fooConcat(");
        String sep = "";
        for (Object obj : objs) {
          builder.append(sep).append(obj.toString());
          sep = ", ";
        }
        return builder.append(")");
      }

      @Override
      protected String fooProtectedOverridden() {
        return "Refs::B::fooProtectedOverridden()";
      }

      public String fooPublic() {
        return "Refs::B::fooPublic()";
      }

      static int[] staticArray(int size) {
        return new int[size];
      }
    }

    static class D {
      D() {
        System.out.println("Refs::D::init()");
      }
    }

    public static class ProtectedInit extends OtherRefs.PublicInit {
      protected ProtectedInit() {
      }

      static String testProtected() {
        return g(ProtectedInit::new);
      }

      @Override
      public String toString() {
        return "OtherRefs::ProtectedInit::init()";
      }
    }

    static class InternalInit extends ProtectedInit {
      InternalInit() {
      }

      @Override
      public String toString() {
        return "Refs::InternalInit::init()";
      }
    }

    static class PrivateInit extends InternalInit {
      private PrivateInit() {
      }

      static String testPrivate() {
        return g(PrivateInit::new);
      }

      @Override
      public String toString() {
        return "Refs::PrivateInit::init()";
      }
    }
  }

  public void testLambdasSimple() {
    System.out.println(f(() -> "testLambdasSimple#1"));
    System.out.println(
        g((a, b, c) -> "{" + a + ":" + b + ":" + c + "}",
            "testLambdasSimple#2", 123, true));
  }

  public void testLambdasSimpleWithCaptures() {
    String s = "<stirng>";
    long l = 1234567890123456789L;
    char c = '#';

    System.out.println(
        g((x, y, z) -> "{" + s + ":" + l + ":" + c + ":" + x + ":" + y + ":" + z + "}",
            "param1", 2, false));

    I i1 = () -> "i1";
    I i2 = () -> i1.foo() + ":i2";
    I i3 = () -> i2.foo() + ":i3";
    System.out.println(f(() -> "{" + i3.foo() + ":anonymous}"));
  }

  public void testInstructionPatchingWithCatchHandlers() {
    try {
      int a = 1, b = 0;
      System.out.println(f(() -> "testInstructionPatchingWithCatchHandlers:1"));
      System.out.println(f(() -> ("does not matter " + (a / b))));
    } catch (IndexOutOfBoundsException | ArithmeticException e) {
      System.out.println("testInstructionPatchingWithCatchHandlers:Divide By Zero");
    } catch (RuntimeException re) {
      throw re;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    int changes = -1;
    try {
      if (f(() -> "").isEmpty()) {
        changes = 32;
        System.out.println(f(() -> "testInstructionPatchingWithCatchHandlers:lambda"));
        throw new RuntimeException();
      } else {
        changes = 42;
        throw new RuntimeException();
      }
    } catch (Throwable t) {
      System.out.println("testInstructionPatchingWithCatchHandlers:changes=" + changes);
    }
  }

  public void testInstanceLambdaMethods() {
    Integer i = 12345;
    System.out.println(h(() -> new A("{testInstanceLambdaMethods:" + i + "}")));
  }

  @SuppressWarnings("unchecked")
  private void testEnforcedSignatureHelper() {
    H h = ((H<B>) x -> new B("{testEnforcedSignature:" + x + "}"));
    System.out.println(h.foo(new A("A")).toString());
  }

  public void testEnforcedSignature() {
    String capture = "capture";
    System.out.println(i(x -> new B("{testEnforcedSignature:" + x + "}")));
    System.out.println(i(x -> new B("{testEnforcedSignature:" + capture + "}")));

    try {
      testEnforcedSignatureHelper();
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    atA(t -> new LambdaDesugaring().reorder(t));
    atB(t -> new LambdaDesugaring().reorder(t));
  }

  public void testMultipleInterfaces() {
    System.out.println(j((I2 & M1 & I3 & M2) () -> "{testMultipleInterfaces:1}"));

    Object o = (I2 & M1 & I3 & M2) () -> "{testMultipleInterfaces:2}";
    M1 m1 = (M1) o;
    M2 m2 = (M2) m1;
    I i = (I) m2;
    System.out.println(((I3) i).foo());

    o = (I2 & Serializable & M2) () -> "{testMultipleInterfaces:3}";
    m2 = (M2) o;
    Serializable s = (Serializable) m2;
    System.out.println(((I) s).foo());
  }

  public void testBridges() {
    k((Legacy.BH) (x -> x), "{testBridges:1}");
    k((Legacy.BK<Legacy.D> & Serializable) (x -> x), new Legacy.D("{testBridges:2}"));
    // k((Legacy.BL) (x -> x), new Legacy.B("{testBridges:3}")); crashes javac
    k((Legacy.BM) (x -> x), new Legacy.C("{testBridges:4}"));
  }

  public String f(I i) {
    return i.foo();
  }

  String g(J j, String a, int b, boolean c) {
    return j.foo(a, b, c);
  }

  String h(G g) {
    return g.foo().toString();
  }

  String i(H<B> h) {
    return h.foo(new B("i(H<B>)")).toString();
  }

  <T extends I2 & M1 & M2 & I3> String j(T l) {
    return ((I3) ((M2) ((M1) (((I2) l))))).foo();
  }

  static <T> void k(Legacy.BI<T> i, T v) {
    System.out.println(i.foo(v).toString());
  }

  static I statelessLambda() {
    return InstanceAndClassChecks::staticProvider;
  }

  static I statefulLambda() {
    return InstanceAndClassChecks.INSTANCE::instanceProvider;
  }

  static class InstanceAndClassChecks {
    static final InstanceAndClassChecks INSTANCE = new InstanceAndClassChecks();

    static void test() {
      assertSameInstance(
          InstanceAndClassChecks::staticProvider,
          InstanceAndClassChecks::staticProvider,
          "Instances must be same");
      assertSameInstance(
          InstanceAndClassChecks::staticProvider,
          statelessLambda(),
          "Instances must be same");

      assertDifferentInstance(
          INSTANCE::instanceProvider,
          INSTANCE::instanceProvider,
          "Instances must be different");
      assertDifferentInstance(
          INSTANCE::instanceProvider,
          statefulLambda(), "Instances must be different");
    }

    public static String staticProvider() {
      return "staticProvider";
    }

    public String instanceProvider() {
      return "instanceProvider";
    }

    static void assertSameInstance(I a, I b, String msg) {
      if (a != b) {
        throw new AssertionError(msg);
      }
    }

    static void assertDifferentInstance(I a, I b, String msg) {
      if (a == b) {
        throw new AssertionError(msg);
      }
    }
  }

  public static void main(String[] args) {
    LambdaDesugaring tests = new LambdaDesugaring();
    tests.testLambdasSimple();
    LambdaDesugaring.testUnusedLambdas();
    tests.testLambdasSimpleWithCaptures();
    tests.testInstructionPatchingWithCatchHandlers();
    tests.testInstanceLambdaMethods();
    tests.testEnforcedSignature();
    tests.testMultipleInterfaces();
    tests.testBridges();
    new Refs.B().test();
    if (isAndroid()) {
      InstanceAndClassChecks.test();
    }
  }

  static boolean isAndroid() {
    try {
      Class.forName("dalvik.system.VMRuntime");
      return true;
    } catch (Exception ignored) {
    }
    return false;
  }
}
