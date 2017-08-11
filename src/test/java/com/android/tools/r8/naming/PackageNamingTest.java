// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.naming;

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
    List<String> tests = Arrays.asList("naming044");

    Map<String, BiConsumer<DexItemFactory, NamingLens>> inspections = new HashMap<>();
    inspections.put("naming044:keep-rules-001.txt", PackageNamingTest::test044_rule001);
    inspections.put("naming044:keep-rules-002.txt", PackageNamingTest::test044_rule002);
    inspections.put("naming044:keep-rules-003.txt", PackageNamingTest::test044_rule003);
    inspections.put("naming044:keep-rules-004.txt", PackageNamingTest::test044_rule004);

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
}
