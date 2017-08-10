// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.naming;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

    return createTests(tests, inspections);
  }

  private static void test044_rule001(DexItemFactory dexItemFactory, NamingLens naming) {
    DexType b = dexItemFactory.createType("Lnaming044/B;");
    // All classes are moved to the top-level package, hence no package separator.
    assertFalse(naming.lookupDescriptor(b).toSourceString().contains("/"));

    DexMethod m = dexItemFactory.createMethod(
        b, dexItemFactory.createProto(dexItemFactory.intType), "m");
    // method naming044.B.m would be renamed.
    assertNotEquals("m", naming.lookupName(m).toSourceString());
  }

  private static void test044_rule002(DexItemFactory dexItemFactory, NamingLens naming) {
    DexType a = dexItemFactory.createType("Lnaming044/A;");
    assertTrue(naming.lookupDescriptor(a).toSourceString().startsWith("Lp44/x/"));

    DexType b = dexItemFactory.createType("Lnaming044/B;");
    assertTrue(naming.lookupDescriptor(b).toSourceString().startsWith("Lp44/x/"));
  }
}
