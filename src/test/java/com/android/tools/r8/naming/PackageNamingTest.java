// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.naming;

import static com.android.tools.r8.naming.ClassNameMinifier.getParentPackagePrefix;
import static com.android.tools.r8.utils.DescriptorUtils.getPackageNameFromDescriptor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.utils.ListUtils;
import com.android.tools.r8.utils.Timing;
import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;
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
public class PackageNamingTest extends NamingTestBase {

  public PackageNamingTest(
      String test,
      List<String> keepRulesFiles,
      BiConsumer<DexItemFactory, NamingLens> inspection) {
    super(test, keepRulesFiles, inspection, new Timing("PackageNamingTest"));
  }

  @Test
  public void packageNamingTest() throws Exception {
    NamingLens naming = runMinifier(ListUtils.map(keepRulesFiles, Paths::get));
    inspection.accept(dexItemFactory, naming);
  }

  @Parameters(name = "test: {0} keep: {1}")
  public static Collection<Object[]> data() {
    List<String> tests = Arrays.asList("naming044", "naming101");

    Map<String, BiConsumer<DexItemFactory, NamingLens>> inspections = new HashMap<>();
    inspections.put("naming044:keep-rules-001.txt", PackageNamingTest::test044_rule001);
    inspections.put("naming044:keep-rules-002.txt", PackageNamingTest::test044_rule002);
    inspections.put("naming044:keep-rules-003.txt", PackageNamingTest::test044_rule003);
    inspections.put("naming044:keep-rules-004.txt", PackageNamingTest::test044_rule004);
    inspections.put("naming044:keep-rules-005.txt", PackageNamingTest::test044_rule005);
    inspections.put("naming101:keep-rules-001.txt", PackageNamingTest::test101_rule001);
    inspections.put("naming101:keep-rules-002.txt", PackageNamingTest::test101_rule002);
    inspections.put("naming101:keep-rules-003.txt", PackageNamingTest::test101_rule003);
    inspections.put("naming101:keep-rules-004.txt", PackageNamingTest::test101_rule004);
    inspections.put("naming101:keep-rules-005.txt", PackageNamingTest::test101_rule005);

    return createTests(tests, inspections);
  }

  private static int countPackageDepth(String descriptor) {
    return CharMatcher.is('/').countIn(descriptor);
  }

  // repackageclasses ''
  private static void test044_rule001(DexItemFactory dexItemFactory, NamingLens naming) {
    // All classes are moved to the top-level package, hence no package separator.
    DexType b = dexItemFactory.createType("Lnaming044/B;");
    assertFalse(naming.lookupDescriptor(b).toSourceString().contains("/"));

    // Even classes in a sub-package are moved to the same top-level package.
    DexType sub = dexItemFactory.createType("Lnaming044/sub/SubB;");
    assertFalse(naming.lookupDescriptor(sub).toSourceString().contains("/"));

    // method naming044.B.m would be renamed.
    DexMethod m = dexItemFactory.createMethod(
        b, dexItemFactory.createProto(dexItemFactory.intType), "m");
    assertNotEquals("m", naming.lookupName(m).toSourceString());
  }

  // repackageclasses 'p44.x'
  private static void test044_rule002(DexItemFactory dexItemFactory, NamingLens naming) {
    // All classes are moved to a single package, so they all have the same package prefix.
    DexType a = dexItemFactory.createType("Lnaming044/A;");
    assertTrue(naming.lookupDescriptor(a).toSourceString().startsWith("Lp44/x/"));

    DexType b = dexItemFactory.createType("Lnaming044/B;");
    assertTrue(naming.lookupDescriptor(b).toSourceString().startsWith("Lp44/x/"));

    DexType sub = dexItemFactory.createType("Lnaming044/sub/SubB;");
    assertTrue(naming.lookupDescriptor(sub).toSourceString().startsWith("Lp44/x/"));
    // Even classes in a sub-package are moved to the same package.
    assertEquals(
        getPackageNameFromDescriptor(naming.lookupDescriptor(sub).toSourceString()),
        getPackageNameFromDescriptor(naming.lookupDescriptor(b).toSourceString()));
  }

  // flattenpackagehierarchy ''
  private static void test044_rule003(DexItemFactory dexItemFactory, NamingLens naming) {
    // All packages are moved to the top-level package, hence only one package separator.
    DexType b = dexItemFactory.createType("Lnaming044/B;");
    assertEquals(1, countPackageDepth(naming.lookupDescriptor(b).toSourceString()));

    // Classes in a sub-package are moved to the top-level as well, but in a different one.
    DexType sub = dexItemFactory.createType("Lnaming044/sub/SubB;");
    assertEquals(1, countPackageDepth(naming.lookupDescriptor(sub).toSourceString()));
    assertNotEquals(
        getPackageNameFromDescriptor(naming.lookupDescriptor(sub).toSourceString()),
        getPackageNameFromDescriptor(naming.lookupDescriptor(b).toSourceString()));

    // method naming044.B.m would be renamed.
    DexMethod m = dexItemFactory.createMethod(
        b, dexItemFactory.createProto(dexItemFactory.intType), "m");
    assertNotEquals("m", naming.lookupName(m).toSourceString());
  }

  // flattenpackagehierarchy 'p44.y'
  private static void test044_rule004(DexItemFactory dexItemFactory, NamingLens naming) {
    // All packages are moved to a single package.
    DexType a = dexItemFactory.createType("Lnaming044/A;");
    assertTrue(naming.lookupDescriptor(a).toSourceString().startsWith("Lp44/y/"));
    // naming004.A -> Lp44/y/a/a;
    assertEquals(3, countPackageDepth(naming.lookupDescriptor(a).toSourceString()));

    DexType b = dexItemFactory.createType("Lnaming044/B;");
    assertTrue(naming.lookupDescriptor(b).toSourceString().startsWith("Lp44/y/"));
    // naming004.B -> Lp44/y/a/b;
    assertEquals(3, countPackageDepth(naming.lookupDescriptor(b).toSourceString()));

    DexType sub = dexItemFactory.createType("Lnaming044/sub/SubB;");
    assertTrue(naming.lookupDescriptor(sub).toSourceString().startsWith("Lp44/y/"));
    // naming004.sub.SubB -> Lp44/y/b/b;
    assertEquals(3, countPackageDepth(naming.lookupDescriptor(sub).toSourceString()));

    // Classes in a sub-package should be in a different package.
    assertNotEquals(
        getPackageNameFromDescriptor(naming.lookupDescriptor(sub).toSourceString()),
        getPackageNameFromDescriptor(naming.lookupDescriptor(b).toSourceString()));
  }

  private static void test044_rule005(DexItemFactory dexItemFactory, NamingLens naming) {
    // All packages are renamed somehow. Need to check package hierarchy is consistent.
    DexType a = dexItemFactory.createType("Lnaming044/A;");
    assertEquals(1, countPackageDepth(naming.lookupDescriptor(a).toSourceString()));
    DexType b = dexItemFactory.createType("Lnaming044/B;");
    assertEquals(1, countPackageDepth(naming.lookupDescriptor(b).toSourceString()));
    assertEquals(
        getPackageNameFromDescriptor(naming.lookupDescriptor(a).toSourceString()),
        getPackageNameFromDescriptor(naming.lookupDescriptor(b).toSourceString()));

    DexType sub_a = dexItemFactory.createType("Lnaming044/sub/SubA;");
    assertEquals(2, countPackageDepth(naming.lookupDescriptor(sub_a).toSourceString()));
    DexType sub_b = dexItemFactory.createType("Lnaming044/sub/SubB;");
    assertEquals(2, countPackageDepth(naming.lookupDescriptor(sub_b).toSourceString()));
    assertEquals(
        getPackageNameFromDescriptor(naming.lookupDescriptor(sub_a).toSourceString()),
        getPackageNameFromDescriptor(naming.lookupDescriptor(sub_b).toSourceString()));

    // Lnaming044/B -> La/c --prefix--> La
    // Lnaming044/sub/SubB -> La/b/b --prefix--> La/b --prefix--> La
    assertEquals(
        getParentPackagePrefix(naming.lookupDescriptor(b).toSourceString()),
        getParentPackagePrefix(
            getParentPackagePrefix(naming.lookupDescriptor(sub_b).toSourceString())));
  }

  private static void test101_rule001(DexItemFactory dexItemFactory, NamingLens naming) {
    // All classes are moved to the top-level package, hence no package separator.
    DexType c = dexItemFactory.createType("Lnaming101/c;");
    assertFalse(naming.lookupDescriptor(c).toSourceString().contains("/"));

    DexType abc = dexItemFactory.createType("Lnaming101/a/b/c;");
    assertFalse(naming.lookupDescriptor(abc).toSourceString().contains("/"));
    assertNotEquals(
        naming.lookupDescriptor(abc).toSourceString(),
        naming.lookupDescriptor(c).toSourceString());
  }

  private static void test101_rule002(DexItemFactory dexItemFactory, NamingLens naming) {
    // Check naming101.a.a is kept due to **.a
    DexType a = dexItemFactory.createType("Lnaming101/a/a;");
    assertEquals("Lnaming101/a/a;", naming.lookupDescriptor(a).toSourceString());
    // Repackaged to naming101.a, but naming101.a.a exists to make a name conflict.
    // Thus, everything else should not be renamed to 'a',
    // except for naming101.b.a, which is also kept due to **.a
    List<String> klasses = ImmutableList.of(
        "Lnaming101/c;",
        "Lnaming101/d;",
        "Lnaming101/a/c;",
        "Lnaming101/a/b/c;",
        "Lnaming101/b/b;");
    for (String klass : klasses) {
      DexType k = dexItemFactory.createType(klass);
      String renamedName = naming.lookupDescriptor(k).toSourceString();
      assertEquals("naming101.a", getPackageNameFromDescriptor(renamedName));
      assertNotEquals("Lnaming101/a/a;", renamedName);
    }
  }

  private static void test101_rule003(DexItemFactory dexItemFactory, NamingLens naming) {
    // All packages are moved to the top-level package, hence only one package separator.
    DexType aa = dexItemFactory.createType("Lnaming101/a/a;");
    assertEquals(1, countPackageDepth(naming.lookupDescriptor(aa).toSourceString()));

    DexType ba = dexItemFactory.createType("Lnaming101/b/a;");
    assertEquals(1, countPackageDepth(naming.lookupDescriptor(ba).toSourceString()));

    assertNotEquals(
        getPackageNameFromDescriptor(naming.lookupDescriptor(aa).toSourceString()),
        getPackageNameFromDescriptor(naming.lookupDescriptor(ba).toSourceString()));
  }

  private static void test101_rule004(DexItemFactory dexItemFactory, NamingLens naming) {
    // Check naming101.a.a is kept due to **.a
    DexType a = dexItemFactory.createType("Lnaming101/a/a;");
    assertEquals("Lnaming101/a/a;", naming.lookupDescriptor(a).toSourceString());
    // Flattened to naming101, hence all other classes will be in naming101.* package.
    // Due to naming101.a.a, prefix naming101.a is already used. So, any other classes,
    // except for naming101.a.c, should not have naming101.a as package.
    List<String> klasses = ImmutableList.of(
        "Lnaming101/c;",
        "Lnaming101/d;",
        "Lnaming101/a/b/c;",
        "Lnaming101/b/a;",
        "Lnaming101/b/b;");
    for (String klass : klasses) {
      DexType k = dexItemFactory.createType(klass);
      String renamedName = naming.lookupDescriptor(k).toSourceString();
      assertNotEquals("naming101.a", getPackageNameFromDescriptor(renamedName));
    }
  }

  private static void test101_rule005(DexItemFactory dexItemFactory, NamingLens naming) {
    // All packages are renamed somehow. Need to check package hierarchy is consistent.
    DexType aa = dexItemFactory.createType("Lnaming101/a/a;");
    assertEquals(2, countPackageDepth(naming.lookupDescriptor(aa).toSourceString()));
    DexType abc = dexItemFactory.createType("Lnaming101/a/b/c;");
    assertEquals(3, countPackageDepth(naming.lookupDescriptor(abc).toSourceString()));

    // Lnaming101/a/a; -> La/a/a; --prefix--> La/a
    // Lnaming101/a/b/c; -> La/a/a/a; --prefix--> La/a/a --prefix--> La/a
    assertEquals(
        getParentPackagePrefix(naming.lookupDescriptor(aa).toSourceString()),
        getParentPackagePrefix(
            getParentPackagePrefix(naming.lookupDescriptor(abc).toSourceString())));
  }
}
