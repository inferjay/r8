// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

// This code is not run directly. It needs to be compiled to dex code.
// 'regalloc.dex' is what is run.

package regalloc;

// Various test cases that are challenging for the register allocator.
public class RegAlloc {

  public static class BoxedInteger {
    public int i;
    public BoxedInteger(int i) {
      this.i = i;
    }
  }

  // Takes as many arguments as are allowed by the Java programming language (255) and
  // does computations on them.
  public static void binaryOpUsingHighRegistersArguments(
      int d000, int d001, int d002, int d003, int d004, int d005,
      int d006, int d007, int d008, int d009, int d010, int d011,
      int d012, int d013, int d014, int d015, int d016, int d017,
      int d018, int d019, int d020, int d021, int d022, int d023,
      int d024, int d025, int d026, int d027, int d028, int d029,
      int d030, int d031, int d032, int d033, int d034, int d035,
      int d036, int d037, int d038, int d039, int d040, int d041,
      int d042, int d043, int d044, int d045, int d046, int d047,
      int d048, int d049, int d050, int d051, int d052, int d053,
      int d054, int d055, int d056, int d057, int d058, int d059,
      int d060, int d061, int d062, int d063, int d064, int d065,
      int d066, int d067, int d068, int d069, int d070, int d071,
      int d072, int d073, int d074, int d075, int d076, int d077,
      int d078, int d079, int d080, int d081, int d082, int d083,
      int d084, int d085, int d086, int d087, int d088, int d089,
      int d090, int d091, int d092, int d093, int d094, int d095,
      int d096, int d097, int d098, int d099, int d100, int d101,
      int d102, int d103, int d104, int d105, int d106, int d107,
      int d108, int d109, int d110, int d111, int d112, int d113,
      int d114, int d115, int d116, int d117, int d118, int d119,
      int d120, int d121, int d122, int d123, int d124, int d125,
      int d126, int d127, int d128, int d129, int d130, int d131,
      int d132, int d133, int d134, int d135, int d136, int d137,
      int d138, int d139, int d140, int d141, int d142, int d143,
      int d144, int d145, int d146, int d147, int d148, int d149,
      int d150, int d151, int d152, int d153, int d154, int d155,
      int d156, int d157, int d158, int d159, int d160, int d161,
      int d162, int d163, int d164, int d165, int d166, int d167,
      int d168, int d169, int d170, int d171, int d172, int d173,
      int d174, int d175, int d176, int d177, int d178, int d179,
      int d180, int d181, int d182, int d183, int d184, int d185,
      int d186, int d187, int d188, int d189, int d190, int d191,
      int d192, int d193, int d194, int d195, int d196, int d197,
      int d198, int d199, int d200, int d201, int d202, int d203,
      int d204, int d205, int d206, int d207, int d208, int d209,
      int d210, int d211, int d212, int d213, int d214, int d215,
      int d216, int d217, int d218, int d219, int d220, int d221,
      int d222, int d223, int d224, int d225, int d226, int d227,
      int d228, int d229, int d230, int d231, int d232, int d233,
      int d234, int d235, int d236, int d237, int d238, int d239,
      int d240, int d241, int d242, int d243, int d244, int d245,
      int d246, int d247, int d248, int d249, int d250, int d251,
      int d252, int d253, int d254) {
    d254 += d253;
    d253 += 4;
    d016 += 17000;
    System.out.println("binaryOpUsingHighRegistersArguments: " + d016 + " " + d253 + " " + d254);
  }

  // Takes as many arguments as are allowed by the Java programming language (255) and
  // does computations on them.
  public static void binaryDoubleOpUsingHighRegistersArguments(
      double d000, double d001, double d002, double d003, double d004, double d005,
      double d006, double d007, double d008, double d009, double d010, double d011,
      double d012, double d013, double d014, double d015, double d016, double d017,
      double d018, double d019, double d020, double d021, double d022, double d023,
      double d024, double d025, double d026, double d027, double d028, double d029,
      double d030, double d031, double d032, double d033, double d034, double d035,
      double d036, double d037, double d038, double d039, double d040, double d041,
      double d042, double d043, double d044, double d045, double d046, double d047,
      double d048, double d049, double d050, double d051, double d052, double d053,
      double d054, double d055, double d056, double d057, double d058, double d059,
      double d060, double d061, double d062, double d063, double d064, double d065,
      double d066, double d067, double d068, double d069, double d070, double d071,
      double d072, double d073, double d074, double d075, double d076, double d077,
      double d078, double d079, double d080, double d081, double d082, double d083,
      double d084, double d085, double d086, double d087, double d088, double d089,
      double d090, double d091, double d092, double d093, double d094, double d095,
      double d096, double d097, double d098, double d099, double d100, double d101,
      double d102, double d103, double d104, double d105, double d106, double d107,
      double d108, double d109, double d110, double d111, double d112, double d113,
      double d114, double d115, double d116, double d117, double d118, double d119,
      double d120, double d121, double d122, double d123, double d124, double d125,
      double d126) {
    d126 += d125;
    System.out.println("binaryDoubleOpUsingHighRegistersArguments: " + d126 + " " + d125);
  }

  public static <T> T identity(T i) {
    return i;
  }

  // Uses a lot of locals that are alive across an addition of two of them. Trivial
  // register allocation will assign too high registers for the additions.
  public static void binaryOpUsingHighRegistersLocals() {
    // Go through identity function in an attempt to make sure that the addition
    // at the end of this method is not constant folded away.
    int i000 = identity(0); int i001 = identity(1); int i002 = identity(2);
    int i003 = identity(3); int i004 = identity(4); int i005 = identity(5);
    int i006 = identity(6); int i007 = identity(7); int i008 = identity(8);
    int i009 = identity(9); int i010 = identity(10); int i011 = identity(11);
    int i012 = identity(12); int i013 = identity(13); int i014 = identity(14);
    int i015 = identity(15); int i016 = identity(16); int i017 = identity(17);
    int i018 = identity(18); int i019 = identity(19); int i020 = identity(20);
    int i021 = identity(21); int i022 = identity(22); int i023 = identity(23);
    int i024 = identity(24); int i025 = identity(25); int i026 = identity(26);
    int i027 = identity(27); int i028 = identity(28); int i029 = identity(29);
    int i030 = identity(30); int i031 = identity(31); int i032 = identity(32);
    int i033 = identity(33); int i034 = identity(34); int i035 = identity(35);
    int i036 = identity(36); int i037 = identity(37); int i038 = identity(38);
    int i039 = identity(39); int i040 = identity(40); int i041 = identity(41);
    int i042 = identity(42); int i043 = identity(43); int i044 = identity(44);
    int i045 = identity(45); int i046 = identity(46); int i047 = identity(47);
    int i048 = identity(48); int i049 = identity(49); int i050 = identity(50);
    int i051 = identity(51); int i052 = identity(52); int i053 = identity(53);
    int i054 = identity(54); int i055 = identity(55); int i056 = identity(56);
    int i057 = identity(57); int i058 = identity(58); int i059 = identity(59);
    int i060 = identity(60); int i061 = identity(61); int i062 = identity(62);
    int i063 = identity(63); int i064 = identity(64); int i065 = identity(65);
    int i066 = identity(66); int i067 = identity(67); int i068 = identity(68);
    int i069 = identity(69); int i070 = identity(70); int i071 = identity(71);
    int i072 = identity(72); int i073 = identity(73); int i074 = identity(74);
    int i075 = identity(75); int i076 = identity(76); int i077 = identity(77);
    int i078 = identity(78); int i079 = identity(79); int i080 = identity(80);
    int i081 = identity(81); int i082 = identity(82); int i083 = identity(83);
    int i084 = identity(84); int i085 = identity(85); int i086 = identity(86);
    int i087 = identity(87); int i088 = identity(88); int i089 = identity(89);
    int i090 = identity(90); int i091 = identity(91); int i092 = identity(92);
    int i093 = identity(93); int i094 = identity(94); int i095 = identity(95);
    int i096 = identity(96); int i097 = identity(97); int i098 = identity(98);
    int i099 = identity(99); int i100 = identity(100); int i101 = identity(101);
    int i102 = identity(102); int i103 = identity(103); int i104 = identity(104);
    int i105 = identity(105); int i106 = identity(106); int i107 = identity(107);
    int i108 = identity(108); int i109 = identity(109); int i110 = identity(110);
    int i111 = identity(111); int i112 = identity(112); int i113 = identity(113);
    int i114 = identity(114); int i115 = identity(115); int i116 = identity(116);
    int i117 = identity(117); int i118 = identity(118); int i119 = identity(119);
    int i120 = identity(120); int i121 = identity(121); int i122 = identity(122);
    int i123 = identity(123); int i124 = identity(124); int i125 = identity(125);
    int i126 = identity(126); int i127 = identity(127); int i128 = identity(128);
    int i129 = identity(129); int i130 = identity(130); int i131 = identity(131);
    int i132 = identity(132); int i133 = identity(133); int i134 = identity(134);
    int i135 = identity(135); int i136 = identity(136); int i137 = identity(137);
    int i138 = identity(138); int i139 = identity(139); int i140 = identity(140);
    int i141 = identity(141); int i142 = identity(142); int i143 = identity(143);
    int i144 = identity(144); int i145 = identity(145); int i146 = identity(146);
    int i147 = identity(147); int i148 = identity(148); int i149 = identity(149);
    int i150 = identity(150); int i151 = identity(151); int i152 = identity(152);
    int i153 = identity(153); int i154 = identity(154); int i155 = identity(155);
    int i156 = identity(156); int i157 = identity(157); int i158 = identity(158);
    int i159 = identity(159); int i160 = identity(160); int i161 = identity(161);
    int i162 = identity(162); int i163 = identity(163); int i164 = identity(164);
    int i165 = identity(165); int i166 = identity(166); int i167 = identity(167);
    int i168 = identity(168); int i169 = identity(169); int i170 = identity(170);
    int i171 = identity(171); int i172 = identity(172); int i173 = identity(173);
    int i174 = identity(174); int i175 = identity(175); int i176 = identity(176);
    int i177 = identity(177); int i178 = identity(178); int i179 = identity(179);
    int i180 = identity(180); int i181 = identity(181); int i182 = identity(182);
    int i183 = identity(183); int i184 = identity(184); int i185 = identity(185);
    int i186 = identity(186); int i187 = identity(187); int i188 = identity(188);
    int i189 = identity(189); int i190 = identity(190); int i191 = identity(191);
    int i192 = identity(192); int i193 = identity(193); int i194 = identity(194);
    int i195 = identity(195); int i196 = identity(196); int i197 = identity(197);
    int i198 = identity(198); int i199 = identity(199); int i200 = identity(200);
    int i201 = identity(201); int i202 = identity(202); int i203 = identity(203);
    int i204 = identity(204); int i205 = identity(205); int i206 = identity(206);
    int i207 = identity(207); int i208 = identity(208); int i209 = identity(209);
    int i210 = identity(210); int i211 = identity(211); int i212 = identity(212);
    int i213 = identity(213); int i214 = identity(214); int i215 = identity(215);
    int i216 = identity(216); int i217 = identity(217); int i218 = identity(218);
    int i219 = identity(219); int i220 = identity(220); int i221 = identity(221);
    int i222 = identity(222); int i223 = identity(223); int i224 = identity(224);
    int i225 = identity(225); int i226 = identity(226); int i227 = identity(227);
    int i228 = identity(228); int i229 = identity(229); int i230 = identity(230);
    int i231 = identity(231); int i232 = identity(232); int i233 = identity(233);
    int i234 = identity(234); int i235 = identity(235); int i236 = identity(236);
    int i237 = identity(237); int i238 = identity(238); int i239 = identity(239);
    int i240 = identity(240); int i241 = identity(241); int i242 = identity(242);
    int i243 = identity(243); int i244 = identity(244); int i245 = identity(245);
    int i246 = identity(246); int i247 = identity(247); int i248 = identity(248);
    int i249 = identity(249); int i250 = identity(250); int i251 = identity(251);
    int i252 = identity(252); int i253 = identity(253); int i254 = identity(254);
    int i255 = identity(255); int i256 = identity(256); int i257 = identity(257);
    int i258 = identity(258); int i259 = identity(259);

    int i = i259 + i259;
    System.out.println("binaryOpUsingHighRegistersLocals " + i);

    i = new BoxedInteger(42).i;
    System.out.println("instance get many registers" + i);

    int j = i000 + i001 + i002 + i003 + i004 + i005 + i006 + i007 + i008 + i009 + i010 + i011 +
        i012 + i013 + i014 + i015 + i016 + i017 + i018 + i019 + i020 + i021 + i022 + i023 +
        i024 + i025 + i026 + i027 + i028 + i029 + i030 + i031 + i032 + i033 + i034 + i035 +
        i036 + i037 + i038 + i039 + i040 + i041 + i042 + i043 + i044 + i045 + i046 + i047 +
        i048 + i049 + i050 + i051 + i052 + i053 + i054 + i055 + i056 + i057 + i058 + i059 +
        i060 + i061 + i062 + i063 + i064 + i065 + i066 + i067 + i068 + i069 + i070 + i071 +
        i072 + i073 + i074 + i075 + i076 + i077 + i078 + i079 + i080 + i081 + i082 + i083 +
        i084 + i085 + i086 + i087 + i088 + i089 + i090 + i091 + i092 + i093 + i094 + i095 +
        i096 + i097 + i098 + i099 + i100 + i101 + i102 + i103 + i104 + i105 + i106 + i107 +
        i108 + i109 + i110 + i111 + i112 + i113 + i114 + i115 + i116 + i117 + i118 + i119 +
        i120 + i121 + i122 + i123 + i124 + i125 + i126 + i127 + i128 + i129 + i130 + i131 +
        i132 + i133 + i134 + i135 + i136 + i137 + i138 + i139 + i140 + i141 + i142 + i143 +
        i144 + i145 + i146 + i147 + i148 + i149 + i150 + i151 + i152 + i153 + i154 + i155 +
        i156 + i157 + i158 + i159 + i160 + i161 + i162 + i163 + i164 + i165 + i166 + i167 +
        i168 + i169 + i170 + i171 + i172 + i173 + i174 + i175 + i176 + i177 + i178 + i179 +
        i180 + i181 + i182 + i183 + i184 + i185 + i186 + i187 + i188 + i189 + i190 + i191 +
        i192 + i193 + i194 + i195 + i196 + i197 + i198 + i199 + i200 + i201 + i202 + i203 +
        i204 + i205 + i206 + i207 + i208 + i209 + i210 + i211 + i212 + i213 + i214 + i215 +
        i216 + i217 + i218 + i219 + i220 + i221 + i222 + i223 + i224 + i225 + i226 + i227 +
        i228 + i229 + i230 + i231 + i232 + i233 + i234 + i235 + i236 + i237 + i238 + i239 +
        i240 + i241 + i242 + i243 + i244 + i245 + i246 + i247 + i248 + i249 + i250 + i251 +
        i252 + i253 + i254 + i255 + i256 + i257 + i258 + i259;
    System.out.println("sum: " + j);
  }

  // Uses a lot of locals that are alive across an addition of two of them. Trivial
  // register allocation will assign too high registers for the additions.
  public static void binaryDoubleOpUsingHighRegistersLocals() {
    // Go through identity function in an attempt to make sure that the addition
    // at the end of this method is not constant folded away.
    double i000 = identity(0.0); double i001 = identity(1.0); double i002 = identity(2.0);
    double i003 = identity(3.0); double i004 = identity(4.0); double i005 = identity(5.0);
    double i006 = identity(6.0); double i007 = identity(7.0); double i008 = identity(8.0);
    double i009 = identity(9.0); double i010 = identity(10.0); double i011 = identity(11.0);
    double i012 = identity(12.0); double i013 = identity(13.0); double i014 = identity(14.0);
    double i015 = identity(15.0); double i016 = identity(16.0); double i017 = identity(17.0);
    double i018 = identity(18.0); double i019 = identity(19.0); double i020 = identity(20.0);
    double i021 = identity(21.0); double i022 = identity(22.0); double i023 = identity(23.0);
    double i024 = identity(24.0); double i025 = identity(25.0); double i026 = identity(26.0);
    double i027 = identity(27.0); double i028 = identity(28.0); double i029 = identity(29.0);
    double i030 = identity(30.0); double i031 = identity(31.0); double i032 = identity(32.0);
    double i033 = identity(33.0); double i034 = identity(34.0); double i035 = identity(35.0);
    double i036 = identity(36.0); double i037 = identity(37.0); double i038 = identity(38.0);
    double i039 = identity(39.0); double i040 = identity(40.0); double i041 = identity(41.0);
    double i042 = identity(42.0); double i043 = identity(43.0); double i044 = identity(44.0);
    double i045 = identity(45.0); double i046 = identity(46.0); double i047 = identity(47.0);
    double i048 = identity(48.0); double i049 = identity(49.0); double i050 = identity(50.0);
    double i051 = identity(51.0); double i052 = identity(52.0); double i053 = identity(53.0);
    double i054 = identity(54.0); double i055 = identity(55.0); double i056 = identity(56.0);
    double i057 = identity(57.0); double i058 = identity(58.0); double i059 = identity(59.0);
    double i060 = identity(60.0); double i061 = identity(61.0); double i062 = identity(62.0);
    double i063 = identity(63.0); double i064 = identity(64.0); double i065 = identity(65.0);
    double i066 = identity(66.0); double i067 = identity(67.0); double i068 = identity(68.0);
    double i069 = identity(69.0); double i070 = identity(70.0); double i071 = identity(71.0);
    double i072 = identity(72.0); double i073 = identity(73.0); double i074 = identity(74.0);
    double i075 = identity(75.0); double i076 = identity(76.0); double i077 = identity(77.0);
    double i078 = identity(78.0); double i079 = identity(79.0); double i080 = identity(80.0);
    double i081 = identity(81.0); double i082 = identity(82.0); double i083 = identity(83.0);
    double i084 = identity(84.0); double i085 = identity(85.0); double i086 = identity(86.0);
    double i087 = identity(87.0); double i088 = identity(88.0); double i089 = identity(89.0);
    double i090 = identity(90.0); double i091 = identity(91.0); double i092 = identity(92.0);
    double i093 = identity(93.0); double i094 = identity(94.0); double i095 = identity(95.0);
    double i096 = identity(96.0); double i097 = identity(97.0); double i098 = identity(98.0);
    double i099 = identity(99.0); double i100 = identity(100.0); double i101 = identity(101.0);
    double i102 = identity(102.0); double i103 = identity(103.0); double i104 = identity(104.0);
    double i105 = identity(105.0); double i106 = identity(106.0); double i107 = identity(107.0);
    double i108 = identity(108.0); double i109 = identity(109.0); double i110 = identity(110.0);
    double i111 = identity(111.0); double i112 = identity(112.0); double i113 = identity(113.0);
    double i114 = identity(114.0); double i115 = identity(115.0); double i116 = identity(116.0);
    double i117 = identity(117.0); double i118 = identity(118.0); double i119 = identity(119.0);
    double i120 = identity(120.0); double i121 = identity(121.0); double i122 = identity(122.0);
    double i123 = identity(123.0); double i124 = identity(124.0); double i125 = identity(125.0);
    double i126 = identity(126.0); double i127 = identity(127.0); double i128 = identity(128.0);
    double i129 = identity(129.0); double i130 = identity(130.0); double i131 = identity(131.0);
    double i132 = identity(132.0); double i133 = identity(133.0); double i134 = identity(134.0);
    double i135 = identity(135.0); double i136 = identity(136.0); double i137 = identity(137.0);
    double i138 = identity(138.0); double i139 = identity(139.0); double i140 = identity(140.0);
    double i141 = identity(141.0); double i142 = identity(142.0); double i143 = identity(143.0);
    double i144 = identity(144.0); double i145 = identity(145.0); double i146 = identity(146.0);
    double i147 = identity(147.0); double i148 = identity(148.0); double i149 = identity(149.0);
    double i150 = identity(150.0); double i151 = identity(151.0); double i152 = identity(152.0);
    double i153 = identity(153.0); double i154 = identity(154.0); double i155 = identity(155.0);
    double i156 = identity(156.0); double i157 = identity(157.0); double i158 = identity(158.0);
    double i159 = identity(159.0); double i160 = identity(160.0); double i161 = identity(161.0);
    double i162 = identity(162.0); double i163 = identity(163.0); double i164 = identity(164.0);
    double i165 = identity(165.0); double i166 = identity(166.0); double i167 = identity(167.0);
    double i168 = identity(168.0); double i169 = identity(169.0); double i170 = identity(170.0);
    double i171 = identity(171.0); double i172 = identity(172.0); double i173 = identity(173.0);
    double i174 = identity(174.0); double i175 = identity(175.0); double i176 = identity(176.0);
    double i177 = identity(177.0); double i178 = identity(178.0); double i179 = identity(179.0);
    double i180 = identity(180.0); double i181 = identity(181.0); double i182 = identity(182.0);
    double i183 = identity(183.0); double i184 = identity(184.0); double i185 = identity(185.0);
    double i186 = identity(186.0); double i187 = identity(187.0); double i188 = identity(188.0);
    double i189 = identity(189.0); double i190 = identity(190.0); double i191 = identity(191.0);
    double i192 = identity(192.0); double i193 = identity(193.0); double i194 = identity(194.0);
    double i195 = identity(195.0); double i196 = identity(196.0); double i197 = identity(197.0);
    double i198 = identity(198.0); double i199 = identity(199.0); double i200 = identity(200.0);
    double i201 = identity(201.0); double i202 = identity(202.0); double i203 = identity(203.0);
    double i204 = identity(204.0); double i205 = identity(205.0); double i206 = identity(206.0);
    double i207 = identity(207.0); double i208 = identity(208.0); double i209 = identity(209.0);
    double i210 = identity(210.0); double i211 = identity(211.0); double i212 = identity(212.0);
    double i213 = identity(213.0); double i214 = identity(214.0); double i215 = identity(215.0);
    double i216 = identity(216.0); double i217 = identity(217.0); double i218 = identity(218.0);
    double i219 = identity(219.0); double i220 = identity(220.0); double i221 = identity(221.0);
    double i222 = identity(222.0); double i223 = identity(223.0); double i224 = identity(224.0);
    double i225 = identity(225.0); double i226 = identity(226.0); double i227 = identity(227.0);
    double i228 = identity(228.0); double i229 = identity(229.0); double i230 = identity(230.0);
    double i231 = identity(231.0); double i232 = identity(232.0); double i233 = identity(233.0);
    double i234 = identity(234.0); double i235 = identity(235.0); double i236 = identity(236.0);
    double i237 = identity(237.0); double i238 = identity(238.0); double i239 = identity(239.0);
    double i240 = identity(240.0); double i241 = identity(241.0); double i242 = identity(242.0);
    double i243 = identity(243.0); double i244 = identity(244.0); double i245 = identity(245.0);
    double i246 = identity(246.0); double i247 = identity(247.0); double i248 = identity(248.0);
    double i249 = identity(249.0); double i250 = identity(250.0); double i251 = identity(251.0);
    double i252 = identity(252.0); double i253 = identity(253.0); double i254 = identity(254.0);
    double i255 = identity(255.0); double i256 = identity(256.0); double i257 = identity(257.0);
    double i258 = identity(258.0); double i259 = identity(259.0);

    double i = i259 + i259;
    System.out.println("binaryOpUsingHighRegistersLocals " + i);

    double j = i000 + i001 + i002 + i003 + i004 + i005 + i006 + i007 + i008 + i009 + i010 + i011 +
        i012 + i013 + i014 + i015 + i016 + i017 + i018 + i019 + i020 + i021 + i022 + i023 +
        i024 + i025 + i026 + i027 + i028 + i029 + i030 + i031 + i032 + i033 + i034 + i035 +
        i036 + i037 + i038 + i039 + i040 + i041 + i042 + i043 + i044 + i045 + i046 + i047 +
        i048 + i049 + i050 + i051 + i052 + i053 + i054 + i055 + i056 + i057 + i058 + i059 +
        i060 + i061 + i062 + i063 + i064 + i065 + i066 + i067 + i068 + i069 + i070 + i071 +
        i072 + i073 + i074 + i075 + i076 + i077 + i078 + i079 + i080 + i081 + i082 + i083 +
        i084 + i085 + i086 + i087 + i088 + i089 + i090 + i091 + i092 + i093 + i094 + i095 +
        i096 + i097 + i098 + i099 + i100 + i101 + i102 + i103 + i104 + i105 + i106 + i107 +
        i108 + i109 + i110 + i111 + i112 + i113 + i114 + i115 + i116 + i117 + i118 + i119 +
        i120 + i121 + i122 + i123 + i124 + i125 + i126 + i127 + i128 + i129 + i130 + i131 +
        i132 + i133 + i134 + i135 + i136 + i137 + i138 + i139 + i140 + i141 + i142 + i143 +
        i144 + i145 + i146 + i147 + i148 + i149 + i150 + i151 + i152 + i153 + i154 + i155 +
        i156 + i157 + i158 + i159 + i160 + i161 + i162 + i163 + i164 + i165 + i166 + i167 +
        i168 + i169 + i170 + i171 + i172 + i173 + i174 + i175 + i176 + i177 + i178 + i179 +
        i180 + i181 + i182 + i183 + i184 + i185 + i186 + i187 + i188 + i189 + i190 + i191 +
        i192 + i193 + i194 + i195 + i196 + i197 + i198 + i199 + i200 + i201 + i202 + i203 +
        i204 + i205 + i206 + i207 + i208 + i209 + i210 + i211 + i212 + i213 + i214 + i215 +
        i216 + i217 + i218 + i219 + i220 + i221 + i222 + i223 + i224 + i225 + i226 + i227 +
        i228 + i229 + i230 + i231 + i232 + i233 + i234 + i235 + i236 + i237 + i238 + i239 +
        i240 + i241 + i242 + i243 + i244 + i245 + i246 + i247 + i248 + i249 + i250 + i251 +
        i252 + i253 + i254 + i255 + i256 + i257 + i258 + i259;
    System.out.println("sum: " + j);
  }

  public static void main(String[] args) {
    binaryOpUsingHighRegistersArguments(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
        12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
        24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35,
        36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
        48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59,
        60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71,
        72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83,
        84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95,
        96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107,
        108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119,
        120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131,
        132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143,
        144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155,
        156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167,
        168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179,
        180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191,
        192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203,
        204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 215,
        216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227,
        228, 229, 230, 231, 232, 233, 234, 235, 236, 237, 238, 239,
        240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 251,
        252, 253, 254);
    binaryDoubleOpUsingHighRegistersArguments(
        0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0,
        10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0,
        20.0, 21.0, 22.0, 23.0, 24.0, 25.0, 26.0, 27.0, 28.0, 29.0,
        30.0, 31.0, 32.0, 33.0, 34.0, 35.0, 36.0, 37.0, 38.0, 39.0,
        40.0, 41.0, 42.0, 43.0, 44.0, 45.0, 46.0, 47.0, 48.0, 49.0,
        50.0, 51.0, 52.0, 53.0, 54.0, 55.0, 56.0, 57.0, 58.0, 59.0,
        60.0, 61.0, 62.0, 63.0, 64.0, 65.0, 66.0, 67.0, 68.0, 69.0,
        70.0, 71.0, 72.0, 73.0, 74.0, 75.0, 76.0, 77.0, 78.0, 79.0,
        80.0, 81.0, 82.0, 83.0, 84.0, 85.0, 86.0, 87.0, 88.0, 89.0,
        90.0, 91.0, 92.0, 93.0, 94.0, 95.0, 96.0, 97.0, 98.0, 99.0,
        100.0, 101.0, 102.0, 103.0, 104.0, 105.0, 106.0, 107.0, 108.0, 109.0,
        110.0, 111.0, 112.0, 113.0, 114.0, 115.0, 116.0, 117.0, 118.0, 119.0,
        120.0, 121.0, 122.0, 123.0, 124.0, 125.0, 126.0);
    binaryOpUsingHighRegistersLocals();
    binaryDoubleOpUsingHighRegistersLocals();
  }
}
