// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package invokecustom;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

interface J {

  default void targetMethodTest8() {
    System.out.println("targetMethodTest8 from J");
  }

  default void targetMethodTest7() {
    System.out.println("targetMethodTest7 from J");
  }

  default void targetMethodTest6() {
    System.out.println("targetMethodTest6 from J");
  }
}

interface I extends J {
  void targetMethodTest8();

  default void targetMethodTest6() {
    System.out.println("targetMethodTest6 from I");
  }

  default void targetMethodTest9() {
    System.out.println("targetMethodTest9 from I");
  }

  default void targetMethodTest10() {
    System.out.println("targetMethodTest10 from I");
  }
}

abstract class Super {
  public void targetMethodTest5() {
    System.out.println("targetMethodTest5 from Super");
  }

  abstract void targetMethodTest10();
}

public class InvokeCustom extends Super implements I {

  private static String staticField1 = "StaticField1";

  private String instanceField1 = "instanceField1";

  private static void targetMethodTest1() {
    System.out.println("Hello World!");
  }

  private static void targetMethodTest2(boolean z, byte b, char c, short s, int i, float f, long l,
      double d, String str) {
    System.out.println(z);
    System.out.println(b);
    System.out.println(c);
    System.out.println(s);
    System.out.println(i);
    System.out.println(f);
    System.out.println(l);
    System.out.println(d);
    System.out.println(str);
  }

  private static void targetMethodTest3() {
  }

  public static CallSite bsmLookupStatic(MethodHandles.Lookup caller, String name, MethodType type)
      throws NoSuchMethodException, IllegalAccessException {
    final MethodHandles.Lookup lookup = MethodHandles.lookup();
    final MethodHandle targetMH = lookup.findStatic(lookup.lookupClass(), name, type);
    return new ConstantCallSite(targetMH.asType(type));
  }

  public static CallSite bsmLookupStaticWithExtraArgs(
      MethodHandles.Lookup caller, String name, MethodType type, int i, long l, float f, double d)
      throws NoSuchMethodException, IllegalAccessException {
    System.out.println(i);
    System.out.println(l);
    System.out.println(f);
    System.out.println(d);
    final MethodHandles.Lookup lookup = MethodHandles.lookup();
    final MethodHandle targetMH = lookup.findStatic(lookup.lookupClass(), name, type);
    return new ConstantCallSite(targetMH.asType(type));
  }

  @Override
  public void targetMethodTest5() {
    System.out.println("targetMethodTest5 from InvokeCustom");
  }

  private static void targetMethodTest4() {
    System.out.println("targetMethodTest4");
  }

  public static CallSite bsmCreateCallSite(
      MethodHandles.Lookup caller, String name, MethodType type, MethodHandle mh)
      throws Throwable {
    // Using mh to create the call site fails when run on Art. See b/36957105 for details.
    final MethodHandle targetMH = MethodHandles.lookup().findSpecial(Super.class,
                "targetMethodTest5", MethodType.methodType(void.class), InvokeCustom.class);
    return new ConstantCallSite(targetMH);
  }

  public static CallSite bsmCreateCallCallingtargetMethodTest6(
      MethodHandles.Lookup caller, String name, MethodType type, MethodHandle mh)
      throws Throwable {
    // Using mh to create the call site fails when run on Art. See b/36957105 for details.
    final MethodHandle targetMH =
        MethodHandles.lookup().findVirtual(
            I.class, "targetMethodTest6", MethodType.methodType(void.class));
    return new ConstantCallSite(targetMH);
  }

  public static CallSite bsmCreateCallCallingtargetMethodTest7(
      MethodHandles.Lookup caller, String name, MethodType type, MethodHandle mh)
      throws Throwable {
    // Using mh to create the call site fails when run on Art. See b/36957105 for details.
    final MethodHandle targetMH =
        MethodHandles.lookup().findVirtual(
            J.class, "targetMethodTest7", MethodType.methodType(void.class));
    return new ConstantCallSite(targetMH);
  }

  public void targetMethodTest8() {
    System.out.println("targetMethodTest8 from InvokeCustom");
  }

  public static CallSite bsmCreateCallCallingtargetMethodTest8(
      MethodHandles.Lookup caller, String name, MethodType type, MethodHandle mh)
      throws Throwable {
    // Using mh to create the call site fails when run on Art. See b/36957105 for details.
    final MethodHandle targetMH =
        MethodHandles.lookup().findVirtual(
            J.class, "targetMethodTest8", MethodType.methodType(void.class));
    return new ConstantCallSite(targetMH);
  }

  public static CallSite bsmCreateCallCallingtargetMethodTest9(
      MethodHandles.Lookup caller, String name, MethodType type, MethodHandle mh)
      throws Throwable {
    // Using mh to create the call site fails when run on Art. See b/36957105 for details.
    final MethodHandle targetMH =
        MethodHandles.lookup().findVirtual(
            InvokeCustom.class, "targetMethodTest9", MethodType.methodType(void.class));
    return new ConstantCallSite(targetMH);
  }

  public void targetMethodTest10() {
    System.out.println("targetMethodTest10 from InvokeCustom");
  }

  public static CallSite bsmCreateCallCallingtargetMethodTest10(
      MethodHandles.Lookup caller, String name, MethodType type, MethodHandle mh)
      throws Throwable {
    // Using mh to create the call site fails when run on Art. See b/36957105 for details.
    final MethodHandle targetMH =
        MethodHandles.lookup().findVirtual(
            InvokeCustom.class, "targetMethodTest10", MethodType.methodType(void.class));
    return new ConstantCallSite(targetMH);
  }

  public static CallSite bsmCreateCallCallingtargetMethod(
      MethodHandles.Lookup caller, String name, MethodType type, MethodHandle mh)
      throws Throwable {
    return new ConstantCallSite(mh);
  }
}
