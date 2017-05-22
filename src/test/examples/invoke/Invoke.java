// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

// This code is not run directly. It needs to be compiled to dex code.
// 'invoke.dex' is what is run.

package invoke;

public class Invoke extends SuperClass implements InvokeInterface {

  public static void static0() {
    System.out.println("static0");
  }

  public static void static1(int a) {
    System.out.println("static1 " + a);
  }

  public static void static2(int a, int b) {
    System.out.println("static2 " + a + " " + b);
  }

  public static void static3(int a, int b, int c) {
    System.out.println("static3 " + a + " " + b + " " + c);
  }

  public static void static4(int a, int b, int c, int d) {
    System.out.println("static4 " + a + " " + b + " " + c + " " + d);
  }

  public static void static5(int a, int b, int c, int d, int e) {
    System.out.println("static5 " + a + " " + b + " " + c + " " + d + " " + e);
  }

  public static void staticRange(int a, int b, int c, int d, int e, int f) {
    System.out.println("staticRange " + a + " " + b + " " + c + " " + d + " " + e + " " + f);
  }

  public static void staticDouble0(double a) {
    System.out.println("staticDouble0 " + a);
  }

  public static void staticDouble1(double a, double b) {
    System.out.println("staticDouble2 " + a + " " + b);
  }

  public static void staticDoubleRange(double a, double b, double c) {
    System.out.println("staticDoubleRange " + a + " " + b + " " + c);
  }

  public static void staticMethods() {
    static0();
    static1(1);
    static2(1, 2);
    static3(1, 2, 3);
    static4(1, 2, 3, 4);
    static5(1, 2, 3, 4, 5);
    staticRange(1, 2, 3, 4, 5, 6);
    staticDouble0(0.1);
    staticDouble1(0.1, 0.2);
    staticDoubleRange(0.1, 0.2, 0.3);
  }

  private void direct0() {
    System.out.println("direct0");
  }

  private void direct1(int a) {
    System.out.println("direct1 " + a);
  }

  private void direct2(int a, int b) {
    System.out.println("direct2 " + a + " " + b);
  }

  private void direct3(int a, int b, int c) {
    System.out.println("direct3 " + a + " " + b + " " + c);
  }

  private void direct4(int a, int b, int c, int d) {
    System.out.println("direct4 " + a + " " + b + " " + c + " " + d);
  }

  private void directRange(int a, int b, int c, int d, int e, int f) {
    System.out.println("directRange " + a + " " + b + " " + c + " " + d + " " + e + " " + f);
  }

  public static void directMethods() {
    Invoke instance = new Invoke();
    instance.direct0();
    instance.direct1(1);
    instance.direct2(1, 2);
    instance.direct3(1, 2, 3);
    instance.direct4(1, 2, 3, 4);
    instance.directRange(1, 2, 3, 4, 5, 6);
  }

  public void interface0() {
    System.out.println("interface0");
  }

  public void interface1(int a) {
    System.out.println("interface1 " + a);
  }

  public void interface2(int a, int b) {
    System.out.println("interface2 " + a + " " + b);
  }

  public void interface3(int a, int b, int c) {
    System.out.println("interface3 " + a + " " + b + " " + c);
  }

  public void interface4(int a, int b, int c, int d) {
    System.out.println("interface4 " + a + " " + b + " " + c + " " + d);
  }

  public void interface5(int a, int b, int c, int d, int e) {
    System.out.println("interface5 " + a + " " + b + " " + c + " " + d + " " + e);
  }

  public void interfaceRange(int a, int b, int c, int d, int e, int f) {
    System.out.println("interfaceRange " + a + " " + b + " " + c + " " + d + " " + e + " " + f);
  }

  public static void interfaceMethods(InvokeInterface i) {
    i.interface0();
    i.interface1(1);
    i.interface2(1, 2);
    i.interface3(1, 2, 3);
    i.interface4(1, 2, 3, 4);
    i.interfaceRange(1, 2, 3, 4, 5, 6);
  }

  public void virtual0() {
    System.out.println("virtual0");
  }

  public void virtual1(int a) {
    System.out.println("virtual1 " + a);
  }

  public void virtual2(int a, int b) {
    System.out.println("virtual2 " + a + " " + b);
  }

  public void virtual3(int a, int b, int c) {
    System.out.println("virtual3 " + a + " " + b + " " + c);
  }

  public void virtual4(int a, int b, int c, int d) {
    System.out.println("virtual4 " + a + " " + b + " " + c + " " + d);
  }

  public void virtual5(int a, int b, int c, int d, int e) {
    System.out.println("virtual5 " + a + " " + b + " " + c + " " + d + " " + e);
  }

  public void virtualRange(int a, int b, int c, int d, int e, int f) {
    System.out.println("virtualRange " + a + " " + b + " " + c + " " + d + " " + e + " " + f);
  }

  public static void virtualMethods() {
    Invoke instance = new Invoke();
    instance.virtual0();
    instance.virtual1(1);
    instance.virtual2(1, 2);
    instance.virtual3(1, 2, 3);
    instance.virtual4(1, 2, 3, 4);
    instance.virtualRange(1, 2, 3, 4, 5, 6);
  }

  public void super0() {
    super.super0();
  }

  public void super1(int a) {
    super.super1(a);
  }

  public void super2(int a, int b) {
    super.super2(a, b);
  }

  public void super3(int a, int b, int c) {
    super.super3(a, b, c);
  }

  public void super4(int a, int b, int c, int d) {
    super.super4(a, b, c, d);
  }

  public void super5(int a, int b, int c, int d, int e) {
    super.super5(a, b, c, d, e);
  }

  public void superRange(int a, int b, int c, int d, int e, int f) {
    super.superRange(a, b, c, d, e, f);
  }

  public static void superInvocations() {
    Invoke instance = new Invoke();
    instance.super0();
    instance.super1(1);
    instance.super2(1, 2);
    instance.super3(1, 2, 3);
    instance.super4(1, 2, 3, 4);
    instance.superRange(1, 2, 3, 4, 5, 6);
  }

  public static void rangeInvoke0(int i, int j, double d, double e, long l) {
    System.out.println("rangeInvoke0 i " + i + " j " + j + " d " + d + " e " + e + " l " + l);
  }

  public static void rangeInvoke1(double d, double e, int i, int j, long l) {
    System.out.println("rangeInvoke1 i " + i + " j " + j + " d " + d + " e " + e + " l " + l);
  }

  public static void rangeInvoke2(long l, double d, double e, int i, int j) {
    System.out.println("rangeInvoke2 i " + i + " j " + j + " d " + d + " e " + e + " l " + l);
  }

  public static void rangeInvokes() {
    int i = 0;
    int j = 2;
    double d = 42.42;
    double e = 43.43;
    long l = 0x0000000F00000000L;
    // Range invokes with shuffled argument orders.
    rangeInvoke0(i, j, d, e, l);
    rangeInvoke0(i, j, d, e, l);
    // Different order.
    rangeInvoke1(d, e, i, j, l);
    rangeInvoke1(d, e, i, j, l);
    // And different again.
    rangeInvoke2(l, d, e, i, j);
    rangeInvoke2(l, d, e, i, j);
  }

  public static void oneArgumentMethod(int i) {
    System.out.println("oneArgumentMethod " + i);
  }

  public static void twoArgumentMethod(int i, int j) {
    System.out.println("twoArgumentMethod " + i + " " + j);
  }

  public static void unusedArgument0(int i0, int i1) {
    oneArgumentMethod(i0);
  }

  public static void unusedArgument1(int i0, int i1) {
    oneArgumentMethod(i1);
  }

  public static void unusedArgumentRanged(int i0, int i1, int i2, int i3, int i4, int i5, int i6,
      int i7, int i8, int i9, int i10, int i11, int i12, int i13, int i14, int i15, int i16) {
    oneArgumentMethod(i16);
    twoArgumentMethod(i16, i9);
    twoArgumentMethod(i16, i10);
    twoArgumentMethod(i16, i11);
  }

  public static void oneDoubleArgumentMethod(double d) {
    System.out.println("oneDoubleArgumentMethod " + d);
  }

  public static void twoDoubleArgumentMethod(double d0, double d1) {
    System.out.println("twoDoubleArgumentMethod " + d0 + " " + d1);
  }

  public static void unusedDoubleArgument0(double d0, double d1) {
    oneDoubleArgumentMethod(d0);
  }

  public static void unusedDoubleArgument1(double d0, double d1) {
    oneDoubleArgumentMethod(d1);
  }

  public static void unusedDoubleArgumentRanged(double d0, double d1, double d2, double d3,
      double d4, double d5, double d6, double d7, double d8, double d9, double d10, double d11,
      double d12, double d13, double d14, double d15, double d16) {
    oneDoubleArgumentMethod(d16);
    twoDoubleArgumentMethod(d16, d9);
    twoDoubleArgumentMethod(d16, d10);
    twoDoubleArgumentMethod(d16, d11);
  }

  public static void unusedArguments() {
    unusedArgument0(0, 1);
    unusedArgument1(2, 3);
    unusedArgumentRanged(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
    unusedDoubleArgument0(1.1, 2.2);
    unusedDoubleArgument1(3.3, 4.4);
    unusedDoubleArgumentRanged(
        0.0, 1.1, 2.2, 3.3, 4.4, 5.5, 6.6, 7.7, 8.8, 9.9, 10.1, 11.2, 12.3, 13.4, 14.5, 15.6, 16.6);
  }

  public static void rangeInvokesRepeatedArgument0(
      int i0, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
    System.out.println("rangeInvokesRepeatedArgument0 " + i0 + " " + i1 + " " + i2 + " " + i3 +
        " " + i4 + " " + i5 + " " + i6 + " " + i7);
  }

  public static void rangeInvokesRepeatedArgument() {
    int i = 0;
    int j = 1;
    rangeInvokesRepeatedArgument0(i, j, i, j, i, j, i, j);
    rangeInvokesRepeatedArgument0(i, j, j, j, j, j, j, j);
  }

  public static <T> T identity(T a) {
    return a;
  }

  public static void printInt(int i) {
    System.out.println("int: " + i);
  }

  public static void printDouble(double d) {
    System.out.println("double: " + d);
  }

  public static void genericMethod() {
    System.out.println("int: " + identity(42));
    System.out.println("double: " + identity(42.42));
    printInt(identity(32));
    printDouble(identity(32.32));
  }

  public static void manyArgs(
      int i0, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10,
      int i11, int i12, int i13, int i14, int i15, int i16, int i17, int i18, int i19, int i20,
      int i21, int i22, int i23, int i24, int i25, int i26, int i27, int i28, int i29,
      int i30, int i31, int i32, int i33, int i34, int i35, int i36, int i37, int i38,
      int i39, int i40, int i41, int i42, int i43, int i44, int i45, int i46, int i47,
      int i48, int i49, int i50, int i51, int i52, int i53, int i54, int i55, int i56,
      int i57, int i58, int i59, int i60, int i61, int i62, int i63, int i64, int i65,
      int i66, int i67, int i68, int i69, int i70, int i71, int i72, int i73, int i74,
      int i75, int i76, int i77, int i78, int i79, int i80, int i81, int i82, int i83,
      int i84, int i85, int i86, int i87, int i88, int i89, int i90, int i91, int i92,
      int i93, int i94, int i95, int i96, int i97, int i98, int i99, int i100, int i101,
      int i102, int i103, int i104, int i105, int i106, int i107, int i108, int i109, int i110,
      int i111, int i112, int i113, int i114, int i115, int i116, int i117, int i118, int i119,
      int i120, int i121, int i122, int i123, int i124, int i125, int i126, int i127, int i128,
      int i129, int i130, int i131, int i132, int i133, int i134, int i135, int i136, int i137,
      int i138, int i139, int i140, int i141, int i142, int i143, int i144, int i145, int i146,
      int i147, int i148, int i149, int i150, int i151, int i152, int i153, int i154, int i155,
      int i156, int i157, int i158, int i159, int i160, int i161, int i162, int i163, int i164,
      int i165, int i166, int i167, int i168, int i169, int i170, int i171, int i172, int i173,
      int i174, int i175, int i176, int i177, int i178, int i179, int i180, int i181, int i182,
      int i183, int i184, int i185, int i186, int i187, int i188, int i189, int i190, int i191,
      int i192, int i193, int i194, int i195, int i196, int i197, int i198, int i199, int i200,
      int i201, int i202, int i203, int i204, int i205, int i206, int i207, int i208, int i209,
      int i210, int i211, int i212, int i213, int i214, int i215, int i216, int i217, int i218,
      int i219, int i220, int i221, int i222, int i223, int i224, int i225, int i226, int i227,
      int i228, int i229, int i230, int i231, int i232, int i233, int i234, int i235, int i236,
      int i237, int i238, int i239, int i240, int i241, int i242, int i243, int i244, int i245,
      int i246, int i247, int i248, int i249, int i250, int i251, int i252, int i253, int i254) {
    // This is here to defeat inlining at this point.
    System.out.println(i254 + i253);
  }

  public static void rangedNoInlining(int i0, int i1, int i2, int i3, int i4, int i5) {
    // This is here to defeat inlining at this point.
    System.out.println(i0 + i1 + i2 + i3 + i4 + i5);
  }

  public static void rangeInvokeWithManyLocals(int i, int j, int k) {
    int i0 = 0; int i1 = 1; int i2 = 2; int i3 = 3; int i4 = 4; int i5 = 5; int i6 = 6;
    int i7 = 7; int i8 = 8; int i9 = 9; int i10 = 10; int i11 = 11; int i12 = 12;
    int i13 = 13; int i14 = 14; int i15 = 15; int i16 = 16; int i17 = 17;
    int i18 = 18; int i19 = 19; int i20 = 20; int i21 = 21; int i22 = 22;
    int i23 = 23; int i24 = 24; int i25 = 25; int i26 = 26; int i27 = 27;
    int i28 = 28; int i29 = 29; int i30 = 30; int i31 = 31; int i32 = 32;
    int i33 = 33; int i34 = 34; int i35 = 35; int i36 = 36; int i37 = 37;
    int i38 = 38; int i39 = 39; int i40 = 40; int i41 = 41; int i42 = 42;
    int i43 = 43; int i44 = 44; int i45 = 45; int i46 = 46; int i47 = 47;
    int i48 = 48; int i49 = 49; int i50 = 50; int i51 = 51; int i52 = 52;
    int i53 = 53; int i54 = 54; int i55 = 55; int i56 = 56; int i57 = 57;
    int i58 = 58; int i59 = 59; int i60 = 60; int i61 = 61; int i62 = 62;
    int i63 = 63; int i64 = 64; int i65 = 65; int i66 = 66; int i67 = 67;
    int i68 = 68; int i69 = 69; int i70 = 70; int i71 = 71; int i72 = 72;
    int i73 = 73; int i74 = 74; int i75 = 75; int i76 = 76; int i77 = 77;
    int i78 = 78; int i79 = 79; int i80 = 80; int i81 = 81; int i82 = 82;
    int i83 = 83; int i84 = 84; int i85 = 85; int i86 = 86; int i87 = 87;
    int i88 = 88; int i89 = 89; int i90 = 90; int i91 = 91; int i92 = 92;
    int i93 = 93; int i94 = 94; int i95 = 95; int i96 = 96; int i97 = 97;
    int i98 = 98; int i99 = 99; int i100 = 100; int i101 = 101; int i102 = 102;
    int i103 = 103; int i104 = 104; int i105 = 105; int i106 = 106; int i107 = 107;
    int i108 = 108; int i109 = 109; int i110 = 110; int i111 = 111; int i112 = 112;
    int i113 = 113; int i114 = 114; int i115 = 115; int i116 = 116; int i117 = 117;
    int i118 = 118; int i119 = 119; int i120 = 120; int i121 = 121; int i122 = 122;
    int i123 = 123; int i124 = 124; int i125 = 125; int i126 = 126; int i127 = 127;
    int i128 = 128; int i129 = 129; int i130 = 130; int i131 = 131; int i132 = 132;
    int i133 = 133; int i134 = 134; int i135 = 135; int i136 = 136; int i137 = 137;
    int i138 = 138; int i139 = 139; int i140 = 140; int i141 = 141; int i142 = 142;
    int i143 = 143; int i144 = 144; int i145 = 145; int i146 = 146; int i147 = 147;
    int i148 = 148; int i149 = 149; int i150 = 150; int i151 = 151; int i152 = 152;
    int i153 = 153; int i154 = 154; int i155 = 155; int i156 = 156; int i157 = 157;
    int i158 = 158; int i159 = 159; int i160 = 160; int i161 = 161; int i162 = 162;
    int i163 = 163; int i164 = 164; int i165 = 165; int i166 = 166; int i167 = 167;
    int i168 = 168; int i169 = 169; int i170 = 170; int i171 = 171; int i172 = 172;
    int i173 = 173; int i174 = 174; int i175 = 175; int i176 = 176; int i177 = 177;
    int i178 = 178; int i179 = 179; int i180 = 180; int i181 = 181; int i182 = 182;
    int i183 = 183; int i184 = 184; int i185 = 185; int i186 = 186; int i187 = 187;
    int i188 = 188; int i189 = 189; int i190 = 190; int i191 = 191; int i192 = 192;
    int i193 = 193; int i194 = 194; int i195 = 195; int i196 = 196; int i197 = 197;
    int i198 = 198; int i199 = 199; int i200 = 200; int i201 = 201; int i202 = 202;
    int i203 = 203; int i204 = 204; int i205 = 205; int i206 = 206; int i207 = 207;
    int i208 = 208; int i209 = 209; int i210 = 210; int i211 = 211; int i212 = 212;
    int i213 = 213; int i214 = 214; int i215 = 215; int i216 = 216; int i217 = 217;
    int i218 = 218; int i219 = 219; int i220 = 220; int i221 = 221; int i222 = 222;
    int i223 = 223; int i224 = 224; int i225 = 225; int i226 = 226; int i227 = 227;
    int i228 = 228; int i229 = 229; int i230 = 230; int i231 = 231; int i232 = 232;
    int i233 = 233; int i234 = 234; int i235 = 235; int i236 = 236; int i237 = 237;
    int i238 = 238; int i239 = 239; int i240 = 240; int i241 = 241; int i242 = 242;
    int i243 = 243; int i244 = 244; int i245 = 245; int i246 = 246; int i247 = 247;
    int i248 = 248; int i249 = 249; int i250 = 250; int i251 = 251; int i252 = 252;
    int i253 = 253; int i254 = 254; int i255 = 255; int i256 = 256; int i257 = 257;
    int i258 = 258; int i259 = 259; int i260 = 260;
    manyArgs(
        i6, i7, i8, i9, i10, i11, i12, i13, i14, i15, i16, i17, i18, i19, i20,
        i21, i22, i23, i24, i25, i26, i27, i28, i29, i30, i31, i32, i33, i34, i35,
        i36, i37, i38, i39, i40, i41, i42, i43, i44, i45, i46, i47, i48, i49, i50,
        i51, i52, i53, i54, i55, i56, i57, i58, i59, i60, i61, i62, i63, i64, i65,
        i66, i67, i68, i69, i70, i71, i72, i73, i74, i75, i76, i77, i78, i79, i80,
        i81, i82, i83, i84, i85, i86, i87, i88, i89, i90, i91, i92, i93, i94, i95,
        i96, i97, i98, i99, i100, i101, i102, i103, i104, i105, i106, i107, i108, i109, i110,
        i111, i112, i113, i114, i115, i116, i117, i118, i119, i120, i121, i122, i123, i124, i125,
        i126, i127, i128, i129, i130, i131, i132, i133, i134, i135, i136, i137, i138, i139, i140,
        i141, i142, i143, i144, i145, i146, i147, i148, i149, i150, i151, i152, i153, i154, i155,
        i156, i157, i158, i159, i160, i161, i162, i163, i164, i165, i166, i167, i168, i169, i170,
        i171, i172, i173, i174, i175, i176, i177, i178, i179, i180, i181, i182, i183, i184, i185,
        i186, i187, i188, i189, i190, i191, i192, i193, i194, i195, i196, i197, i198, i199, i200,
        i201, i202, i203, i204, i205, i206, i207, i208, i209, i210, i211, i212, i213, i214, i215,
        i216, i217, i218, i219, i220, i221, i222, i223, i224, i225, i226, i227, i228, i229, i230,
        i231, i232, i233, i234, i235, i236, i237, i238, i239, i240, i241, i242, i243, i244, i245,
        i246, i247, i248, i249, i250, i251, i252, i253, i254, i255, i256, i257, i258, i259, i260);
    rangedNoInlining(i0, i1, i2, i3, i4, i5);
  }

  public static void main(String[] args) {
    staticMethods();
    directMethods();
    interfaceMethods(new Invoke());
    virtualMethods();
    superInvocations();
    rangeInvokes();
    unusedArguments();
    rangeInvokesRepeatedArgument();
    genericMethod();
    rangeInvokeWithManyLocals(1, 2, 3);
  }
}
