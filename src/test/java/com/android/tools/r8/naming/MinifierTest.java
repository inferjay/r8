// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.naming;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.utils.ListUtils;
import com.android.tools.r8.utils.Timing;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class MinifierTest extends NamingTestBase {

  public MinifierTest(
      String test,
      List<String> keepRulesFiles,
      BiConsumer<DexItemFactory, NamingLens> inspection) {
    super(test, keepRulesFiles, inspection, new Timing("MinifierTest"));
  }

  @Test
  public void minifierTest() throws Exception {
    NamingLens naming = runMinifier(ListUtils.map(keepRulesFiles, Paths::get));
    inspection.accept(dexItemFactory, naming);
  }

  @Parameters(name = "test: {0} keep: {1}")
  public static Collection<Object[]> data() {
    List<String> tests = Arrays.asList("naming001");

    Map<String, BiConsumer<DexItemFactory, NamingLens>> inspections = new HashMap<>();
    inspections.put("naming001:keep-rules-001.txt", MinifierTest::test001_rule001);
    inspections.put("naming001:keep-rules-002.txt", MinifierTest::test001_rule002);
    inspections.put("naming001:keep-rules-003.txt", MinifierTest::test001_rule003);
    inspections.put("naming001:keep-rules-005.txt", MinifierTest::test001_rule005);
    inspections.put("naming001:keep-rules-006.txt", MinifierTest::test001_rule006);
    inspections.put("naming001:keep-rules-014.txt", MinifierTest::test001_rule014);
    inspections.put("naming001:keep-rules-017.txt", MinifierTest::test001_rule017);

    return createTests(tests, inspections);
  }

  private static void test001_rule001(DexItemFactory dexItemFactory, NamingLens naming) {
    DexType a = dexItemFactory.createType("Lnaming001/A;");
    // class naming001.A should be kept, according to the keep rule.
    assertEquals("Lnaming001/A;", naming.lookupDescriptor(a).toSourceString());

    DexMethod m = dexItemFactory.createMethod(
        a, dexItemFactory.createProto(dexItemFactory.voidType), "m");
    // method naming001.A.m would be renamed.
    assertNotEquals("m", naming.lookupName(m).toSourceString());

    DexMethod p = dexItemFactory.createMethod(
        a, dexItemFactory.createProto(dexItemFactory.voidType), "privateFunc");
    // method naming001.A.privateFunc would be renamed.
    assertNotEquals("privateFunc", naming.lookupName(p).toSourceString());

    DexType k = dexItemFactory.createType("Lnaming001/K;");
    DexField h = dexItemFactory.createField(k, dexItemFactory.intType, "h");
    // field naming001.K.h is dead, not renamed; hence returned as same via identityLens.
    assertEquals("h", naming.lookupName(h).toSourceString());
  }

  private static void test001_rule002(DexItemFactory dexItemFactory, NamingLens naming) {
    DexType a = dexItemFactory.createType("Lnaming001/A;");
    // class naming001.A should be kept, according to the keep rule.
    assertEquals("Lnaming001/A;", naming.lookupDescriptor(a).toSourceString());

    DexMethod m = dexItemFactory.createMethod(
        a, dexItemFactory.createProto(dexItemFactory.voidType), "m");
    // method naming001.A.m would be renamed.
    assertNotEquals("m", naming.lookupName(m).toSourceString());

    DexMethod p = dexItemFactory.createMethod(
        a, dexItemFactory.createProto(dexItemFactory.voidType), "privateFunc");
    // method naming001.A.privateFunc should be kept, according to the keep rule.
    assertEquals("privateFunc", naming.lookupName(p).toSourceString());
  }

  private static void test001_rule003(DexItemFactory dexItemFactory, NamingLens naming) {
    DexType a = dexItemFactory.createType("Lnaming001/A;");
    // class naming001.A should be kept, according to the keep rule.
    assertEquals("Lnaming001/A;", naming.lookupDescriptor(a).toSourceString());

    DexMethod m = dexItemFactory.createMethod(
        a, dexItemFactory.createProto(dexItemFactory.voidType), "m");
    // method naming001.A.m should be kept, according to the keep rule.
    assertEquals("m", naming.lookupName(m).toSourceString());

    DexMethod p = dexItemFactory.createMethod(
        a, dexItemFactory.createProto(dexItemFactory.voidType), "privateFunc");
    // method naming001.A.privateFunc would be renamed.
    assertNotEquals("privateFunc", naming.lookupName(p).toSourceString());
  }

  private static void test001_rule005(DexItemFactory dexItemFactory, NamingLens naming) {
    DexType d = dexItemFactory.createType("Lnaming001/D;");
    // class naming001.D should be kept, according to the keep rule.
    assertEquals("Lnaming001/D;", naming.lookupDescriptor(d).toSourceString());

    DexMethod main = dexItemFactory.createMethod(
        d,
        dexItemFactory.createProto(dexItemFactory.voidType, dexItemFactory.stringArrayType),
        "main");
    // method naming001.D.main should be kept, according to the keep rule.
    assertEquals("main", naming.lookupName(main).toSourceString());

    DexMethod k = dexItemFactory.createMethod(
        d, dexItemFactory.createProto(dexItemFactory.voidType), "keep");
    // method naming001.D.keep would be renamed.
    assertNotEquals("keep", naming.lookupName(k).toSourceString());

    // Note that naming001.E extends naming001.D.
    DexType e = dexItemFactory.createType("Lnaming001/E;");
    DexMethod inherited_k = dexItemFactory.createMethod(
        e, dexItemFactory.createProto(dexItemFactory.voidType), "keep");
    // method naming001.E.keep should be renamed to the same name as naming001.D.keep.
    assertEquals(
        naming.lookupName(inherited_k).toSourceString(), naming.lookupName(k).toSourceString());
  }

  private static void test001_rule006(DexItemFactory dexItemFactory, NamingLens naming) {
    DexType g = dexItemFactory.createType("Lnaming001/G;");
    // class naming001.G should be kept, according to the keep rule.
    assertEquals("Lnaming001/G;", naming.lookupDescriptor(g).toSourceString());

    DexMethod main = dexItemFactory.createMethod(
        g,
        dexItemFactory.createProto(dexItemFactory.voidType, dexItemFactory.stringArrayType),
        "main");
    // method naming001.G.main should be kept, according to the keep rule.
    assertEquals("main", naming.lookupName(main).toSourceString());

    DexMethod impl_m = dexItemFactory.createMethod(
        g, dexItemFactory.createProto(dexItemFactory.voidType), "m");
    // method naming001.G.m would be renamed.
    assertNotEquals("m", naming.lookupName(impl_m).toSourceString());

    // Note that naming001.G implements H that extends I, where method m is declared.
    DexType i = dexItemFactory.createType("Lnaming001/I;");
    DexMethod def_m = dexItemFactory.createMethod(
        i, dexItemFactory.createProto(dexItemFactory.voidType), "m");
    // method naming001.I.m should be renamed to the same name as naming001.G.m.
    assertEquals(
        naming.lookupName(impl_m).toSourceString(), naming.lookupName(def_m).toSourceString());
  }

  private static void test001_rule014(DexItemFactory dexItemFactory, NamingLens naming) {
    DexType reflect = dexItemFactory.createType("Lnaming001/Reflect;");
    // class naming001.Reflect should be kept, according to the keep rule.
    assertEquals("Lnaming001/Reflect;", naming.lookupDescriptor(reflect).toSourceString());

    DexMethod keep6 = dexItemFactory.createMethod(
        reflect, dexItemFactory.createProto(dexItemFactory.voidType), "keep6");
    // method naming001.Reflect.keep6 should be kept, according to the keep rule.
    assertEquals("keep6", naming.lookupName(keep6).toSourceString());

    DexType reflect2 = dexItemFactory.createType("Lnaming001/Reflect2;");
    DexField fieldPublic = dexItemFactory.createField(
        reflect2, dexItemFactory.intType, "fieldPublic");
    // method naming001.Reflect.keep6 accesses to naming001.Reflect2.fieldPublic via reflection.
    assertEquals("fieldPublic", naming.lookupName(fieldPublic).toSourceString());
  }

  private static void test001_rule017(DexItemFactory dexItemFactory, NamingLens naming) {
    DexType k = dexItemFactory.createType("Lnaming001/K;");
    // class naming001.K should be kept, according to the keep rule.
    assertEquals("Lnaming001/K;", naming.lookupDescriptor(k).toSourceString());

    DexMethod keep = dexItemFactory.createMethod(
        k, dexItemFactory.createProto(dexItemFactory.voidType), "keep");
    // method naming001.K.keep should be kept, according to the keep rule.
    assertEquals("keep", naming.lookupName(keep).toSourceString());

    DexField i = dexItemFactory.createField(k, dexItemFactory.intType, "i");
    // field naming001.K.i
    assertEquals("i", naming.lookupName(i).toSourceString());

    DexField j = dexItemFactory.createField(k, dexItemFactory.intType, "j");
    // field naming001.K.j
    assertEquals("j", naming.lookupName(j).toSourceString());
  }

}
