// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package invokepolymorphic;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

class Data {
}

public class InvokePolymorphic {

  public String buildString(Integer i1, int i2, String s) {
    return (i1 == null ? "N" : "!N") + "-" + i2 + "-" + s;
  }

  public void testInvokePolymorphic() {
    MethodType mt = MethodType.methodType(String.class, Integer.class, int.class, String.class);
    MethodHandles.Lookup lk = MethodHandles.lookup();

    try {
      MethodHandle mh = lk.findVirtual(getClass(), "buildString", mt);
      System.out.println(mh.invoke(this, null, 1, "string"));
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public String buildString(
      byte b, char c, short s, float f, double d, long l, Integer i1, int i2, String str) {
    return b + "-" + c + "-" + s + "-" + f + "-" + d + "-" + l + "-" + (i1 == null ? "N" : "!N")
        + "-" + i2 + "-" + str;
  }

  public void testInvokePolymorphicRange() {
    MethodType mt = MethodType.methodType(String.class, byte.class, char.class, short.class,
        float.class, double.class, long.class, Integer.class, int.class, String.class);
    MethodHandles.Lookup lk = MethodHandles.lookup();

    try {
      MethodHandle mh = lk.findVirtual(getClass(), "buildString", mt);
      System.out.println(
          mh.invoke(this, (byte) 2, 'a', (short) 0xFFFF, 1.1f, 2.24d, 12345678L, null,
              1, "string"));
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public static void testWithAllTypes(
      boolean z, char a, short b, int c, long d, float e, double f, String g, Object h) {
    System.out.println(z);
    System.out.println(a);
    System.out.println(b);
    System.out.println(c);
    System.out.println(d);
    System.out.println(e);
    System.out.println(f);
    System.out.println(g);
    System.out.println(h);
  }

  public void testInvokePolymorphicWithAllTypes() {
    try {
      MethodHandle mth =
          MethodHandles.lookup()
              .findStatic(
                  InvokePolymorphic.class,
                  "testWithAllTypes",
                  MethodType.methodType(
                      void.class, boolean.class, char.class, short.class, int.class, long.class,
                      float.class, double.class, String.class, Object.class));
      mth.invokeExact(false,'h', (short) 56, 72, Integer.MAX_VALUE + 42l,
          0.56f, 100.0d, "hello", (Object) "goodbye");
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public MethodHandle testInvokePolymorphicWithConstructor() {
    MethodHandle mh = null;
    MethodType mt = MethodType.methodType(void.class);
    MethodHandles.Lookup lk = MethodHandles.lookup();

    try {
      mh = lk.findConstructor(Data.class, mt);
      System.out.println(mh.invoke().getClass() == Data.class);
    } catch (Throwable t) {
      t.printStackTrace();
    }

    return mh;
  }

  public static void main(String[] args) {
    InvokePolymorphic invokePolymorphic = new InvokePolymorphic();
    invokePolymorphic.testInvokePolymorphic();
    invokePolymorphic.testInvokePolymorphicRange();
    invokePolymorphic.testInvokePolymorphicWithAllTypes();
    invokePolymorphic.testInvokePolymorphicWithConstructor();
  }
}
