// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package lambdadesugaring;

public class ValueAdjustments {
  interface B2i {
    int foo(Byte i);
  }

  interface BnUnB {
    Object foo(boolean z, Boolean Z, byte b, Byte B, char c, Character C, short s, Short S,
        int i, Integer I, long l, Long L, float f, Float F, double d, Double D);
  }

  interface iz {
    boolean f();
  }

  interface iZ {
    Boolean f();
  }

  interface ib {
    byte f();
  }

  interface iO {
    Object f();
  }

  interface iN {
    Number f();
  }

  interface iB {
    Byte f();
  }

  interface ic {
    char f();
  }

  interface iC {
    Character f();
  }

  interface is {
    short f();
  }

  interface iS {
    Short f();
  }

  interface ii {
    int f();
  }

  interface iI {
    Integer f();
  }

  interface ij {
    long f();
  }

  interface iJ {
    Long f();
  }

  interface if_ {
    float f();
  }

  interface iF {
    Float f();
  }

  interface id {
    double f();
  }

  interface iD {
    Double f();
  }

  private static void checkObject(StringBuffer builder) {
    builder
        .append(((iO) ValueAdjustments::z).f()).append(' ')
        .append(((iO) ValueAdjustments::Z).f()).append(' ')
        .append(((iO) ValueAdjustments::b).f()).append(' ')
        .append(((iO) ValueAdjustments::B).f()).append(' ')
        .append(((iO) ValueAdjustments::c).f()).append(' ')
        .append(((iO) ValueAdjustments::C).f()).append(' ')
        .append(((iO) ValueAdjustments::s).f()).append(' ')
        .append(((iO) ValueAdjustments::S).f()).append(' ')
        .append(((iO) ValueAdjustments::i).f()).append(' ')
        .append(((iO) ValueAdjustments::I).f()).append(' ')
        .append(((iO) ValueAdjustments::j).f()).append(' ')
        .append(((iO) ValueAdjustments::J).f()).append(' ')
        .append(((iO) ValueAdjustments::f).f()).append(' ')
        .append(((iO) ValueAdjustments::F).f()).append(' ')
        .append(((iO) ValueAdjustments::d).f()).append(' ')
        .append(((iO) ValueAdjustments::D).f()).append('\n');
  }

  private static void checkNumber(StringBuffer builder) {
    builder
        .append(((iN) ValueAdjustments::b).f()).append(' ')
        .append(((iN) ValueAdjustments::B).f()).append(' ')
        .append(((iN) ValueAdjustments::s).f()).append(' ')
        .append(((iN) ValueAdjustments::S).f()).append(' ')
        .append(((iN) ValueAdjustments::i).f()).append(' ')
        .append(((iN) ValueAdjustments::I).f()).append(' ')
        .append(((iN) ValueAdjustments::j).f()).append(' ')
        .append(((iN) ValueAdjustments::J).f()).append(' ')
        .append(((iN) ValueAdjustments::f).f()).append(' ')
        .append(((iN) ValueAdjustments::F).f()).append(' ')
        .append(((iN) ValueAdjustments::d).f()).append(' ')
        .append(((iN) ValueAdjustments::D).f()).append('\n');
  }

  private static void checkBoxes(StringBuffer builder) {
    builder
        .append(((iZ) ValueAdjustments::z).f()).append(' ')
        .append(((iB) ValueAdjustments::b).f()).append(' ')
        .append(((iC) ValueAdjustments::c).f()).append(' ')
        .append(((iS) ValueAdjustments::s).f()).append(' ')
        .append(((iI) ValueAdjustments::i).f()).append(' ')
        .append(((iJ) ValueAdjustments::j).f()).append(' ')
        .append(((iF) ValueAdjustments::f).f()).append(' ')
        .append(((iD) ValueAdjustments::d).f()).append('\n');
  }

  private static void checkDouble(StringBuffer builder) {
    builder
        .append(((id) ValueAdjustments::b).f()).append(' ')
        .append(((id) ValueAdjustments::B).f()).append(' ')
        .append(((id) ValueAdjustments::s).f()).append(' ')
        .append(((id) ValueAdjustments::S).f()).append(' ')
        .append(((id) ValueAdjustments::c).f()).append(' ')
        .append(((id) ValueAdjustments::C).f()).append(' ')
        .append(((id) ValueAdjustments::i).f()).append(' ')
        .append(((id) ValueAdjustments::I).f()).append(' ')
        .append(((id) ValueAdjustments::j).f()).append(' ')
        .append(((id) ValueAdjustments::J).f()).append(' ')
        .append(((id) ValueAdjustments::f).f()).append(' ')
        .append(((id) ValueAdjustments::F).f()).append(' ')
        .append(((id) ValueAdjustments::d).f()).append(' ')
        .append(((id) ValueAdjustments::D).f()).append('\n');
  }

  private static void checkFloat(StringBuffer builder) {
    builder
        .append(((if_) ValueAdjustments::b).f()).append(' ')
        .append(((if_) ValueAdjustments::B).f()).append(' ')
        .append(((if_) ValueAdjustments::s).f()).append(' ')
        .append(((if_) ValueAdjustments::S).f()).append(' ')
        .append(((if_) ValueAdjustments::c).f()).append(' ')
        .append(((if_) ValueAdjustments::C).f()).append(' ')
        .append(((if_) ValueAdjustments::i).f()).append(' ')
        .append(((if_) ValueAdjustments::I).f()).append(' ')
        .append(((if_) ValueAdjustments::j).f()).append(' ')
        .append(((if_) ValueAdjustments::J).f()).append(' ')
        .append(((if_) ValueAdjustments::f).f()).append(' ')
        .append(((if_) ValueAdjustments::F).f()).append('\n');
  }

  private static void checkLong(StringBuffer builder) {
    builder
        .append(((ij) ValueAdjustments::b).f()).append(' ')
        .append(((ij) ValueAdjustments::B).f()).append(' ')
        .append(((ij) ValueAdjustments::s).f()).append(' ')
        .append(((ij) ValueAdjustments::S).f()).append(' ')
        .append(((ij) ValueAdjustments::c).f()).append(' ')
        .append(((ij) ValueAdjustments::C).f()).append(' ')
        .append(((ij) ValueAdjustments::i).f()).append(' ')
        .append(((ij) ValueAdjustments::I).f()).append(' ')
        .append(((ij) ValueAdjustments::j).f()).append(' ')
        .append(((ij) ValueAdjustments::J).f()).append('\n');
  }

  private static void checkInt(StringBuffer builder) {
    builder
        .append(((ii) ValueAdjustments::b).f()).append(' ')
        .append(((ii) ValueAdjustments::B).f()).append(' ')
        .append(((ii) ValueAdjustments::s).f()).append(' ')
        .append(((ii) ValueAdjustments::S).f()).append(' ')
        .append(((ii) ValueAdjustments::c).f()).append(' ')
        .append(((ii) ValueAdjustments::C).f()).append(' ')
        .append(((ii) ValueAdjustments::i).f()).append(' ')
        .append(((ii) ValueAdjustments::I).f()).append('\n');
  }

  private static void checkShort(StringBuffer builder) {
    builder
        .append(((is) ValueAdjustments::b).f()).append(' ')
        .append(((is) ValueAdjustments::B).f()).append(' ')
        .append(((is) ValueAdjustments::s).f()).append(' ')
        .append(((is) ValueAdjustments::S).f()).append('\n');
  }

  private static void checkChar(StringBuffer builder) {
    builder
        .append(((ic) ValueAdjustments::c).f()).append(' ')
        .append(((ic) ValueAdjustments::C).f()).append('\n');
  }

  private static void checkByte(StringBuffer builder) {
    builder
        .append(((ib) ValueAdjustments::b).f()).append(' ')
        .append(((ib) ValueAdjustments::B).f()).append('\n');
  }

  private static void checkBoolean(StringBuffer builder) {
    builder
        .append(((iz) ValueAdjustments::z).f()).append(' ')
        .append(((iz) ValueAdjustments::Z).f()).append('\n');
  }

  private static void checkMisc(StringBuffer builder) {
    builder
        .append(((BnUnB) ValueAdjustments::boxingAndUnboxing).foo(true, false, (byte) 1, (byte) 2,
            (char) 33, (char) 44, (short) 5, (short) 6, 7, 8, 9, 10L, 11, 12f, 13, 14d))
        .append('\n')
        .append(((BnUnB) ValueAdjustments::boxingAndUnboxingW).foo(true, false, (byte) 1, (byte) 2,
            (char) 33, (char) 44, (short) 5, (short) 6, 7, 8, 9, 10L, 11, 12f, 13, 14d))
        .append('\n')
        .append(((B2i) (Integer::new)).foo(Byte.valueOf((byte) 44))).append('\n');
  }

  static String boxingAndUnboxing(Boolean Z, boolean z, Byte B, byte b, Character C, char c,
      Short S, short s, Integer I, int i, Long L, long l, Float F, float f, Double D, double d) {
    return "" + Z + ":" + z + ":" + B + ":" + b + ":" + C + ":" + c + ":" + S + ":" + s
        + ":" + I + ":" + i + ":" + L + ":" + l + ":" + F + ":" + f + ":" + D + ":" + d;
  }

  static String boxingAndUnboxingW(boolean Z, boolean z, double B, double b,
      double C, double c, double S, double s, double I, double i, double L, double l,
      double F, double f, double D, double d) {
    return "" + Z + ":" + z + ":" + B + ":" + b + ":" + C + ":" + c + ":" + S + ":" + s
        + ":" + I + ":" + i + ":" + L + ":" + l + ":" + F + ":" + f + ":" + D + ":" + d;
  }

  static boolean z() {
    return true;
  }

  static byte b() {
    return 8;
  }

  static char c() {
    return 'c';
  }

  static short s() {
    return 16;
  }

  static int i() {
    return 32;
  }

  static long j() {
    return 64;
  }

  static float f() {
    return 0.32f;
  }

  static double d() {
    return 0.64;
  }

  static Boolean Z() {
    return false;
  }

  static Byte B() {
    return -8;
  }

  static Character C() {
    return 'C';
  }

  static Short S() {
    return -16;
  }

  static Integer I() {
    return -32;
  }

  static Long J() {
    return -64L;
  }

  static Float F() {
    return -0.32f;
  }

  static Double D() {
    return -0.64;
  }

  public static void main(String[] args) {
    StringBuffer builder = new StringBuffer();

    checkBoolean(builder);
    checkByte(builder);
    checkChar(builder);
    checkShort(builder);
    checkInt(builder);
    checkLong(builder);
    checkFloat(builder);
    checkDouble(builder);

    checkBoxes(builder);
    checkNumber(builder);
    checkObject(builder);

    checkMisc(builder);

    System.out.println(builder.toString());
  }
}
