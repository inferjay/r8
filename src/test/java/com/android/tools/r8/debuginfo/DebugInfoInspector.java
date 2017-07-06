// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.debuginfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.android.tools.r8.graph.DebugLocalInfo;
import com.android.tools.r8.graph.DexDebugEntry;
import com.android.tools.r8.graph.DexDebugEntryBuilder;
import com.android.tools.r8.graph.DexDebugInfo;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.naming.MemberNaming.MethodSignature;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.DescriptorUtils;
import com.android.tools.r8.utils.DexInspector;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DebugInfoInspector {

  // Method kept here to aid inspection when debugging the tests.
  private final DexEncodedMethod method;
  private final List<DexDebugEntry> entries;

  final DexDebugInfo info;

  public DebugInfoInspector(DexEncodedMethod method, DexItemFactory factory) {
    this.method = method;
    info = method.getCode().asDexCode().getDebugInfo();
    entries = new DexDebugEntryBuilder(method, factory).build();
    checkConsistentEntries();
  }

  public DebugInfoInspector(DexInspector inspector, String clazz, MethodSignature method) {
    this(inspector.clazz(clazz).method(method).getMethod(), inspector.getFactory());
  }

  public DebugInfoInspector(AndroidApp app, String clazz, MethodSignature method)
      throws IOException, ExecutionException {
    this(new DexInspector(app), clazz, method);
  }

  public void checkStartLine(int i) {
    assertEquals(i, info.startLine);
  }

  public int checkLineExists(int line) {
    int lines = checkLines(line, entry -> {});
    assertTrue(lines > 0);
    return lines;
  }

  public int checkLineHasExactLocals(int line, String... pairs) {
    int lines = checkLines(line, entry -> checkLocalsEqual(entry, pairs));
    assertTrue("Failed to find entry for line " + line, lines > 0);
    return lines;
  }

  public int checkLineHasNoLocals(int line) {
    return checkLineHasExactLocals(line);
  }

  public int checkLineHasAtLeastLocals(int line, String... pairs) {
    int lines = checkLines(line, entry -> checkLocalsDefined(entry, pairs));
    assertTrue(lines > 0);
    return lines;
  }

  public void checkNoLine(int line) {
    int lines = checkLines(line, entry -> {});
    assertEquals(0, lines);
  }

  public int checkLineHasLocal(int line, String name, String type, String... typeParameters) {
    int lines = checkLines(line, entry -> {
      checkLocalDefined(entry, name, type, typeParameters);
    });
    assertTrue(lines > 0);
    return lines;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (DexDebugEntry entry : entries) {
      builder.append(entry).append("\n");
    }
    return builder.toString();
  }

  private void checkConsistentEntries() {
    DexDebugEntry previousEntry = null;
    for (DexDebugEntry entry : entries) {
      if (previousEntry != null) {
        assertTrue("More than one entry defined for PC " + entry.address,
            entry.address > previousEntry.address);
      }
      previousEntry = entry;
    }
  }

  private int checkLines(int line, Consumer<DexDebugEntry> check) {
    int found = 0;
    for (int i = 0; i < entries.size(); i++) {
      DexDebugEntry entry = entries.get(i);
      // Matches each entry at 'line' that is not a zero-line increment.
      if (entry.line == line && (i == 0 || entries.get(i - 1).line != line)) {
        found++;
        check.accept(entry);
      }
    }
    return found;
  }

  private static DebugLocalInfo checkLocalDefined(DexDebugEntry entry, String name, String type,
      String... typeParameters) {
    DebugLocalInfo found = null;
    for (DebugLocalInfo local : entry.locals.values()) {
      if (local.name.toString().equals(name)) {
        if (found != null) {
          fail("Line " + entry.line + ". Local defined multiple times for name: " + name);
        }
        assertEquals(type, local.type.toString());
        if (typeParameters.length > 0) {
          String desc = DescriptorUtils.javaTypeToDescriptor(type);
          StringBuilder builder = new StringBuilder(desc.substring(0, desc.length() - 1));
          builder.append("<");
          for (String parameter : typeParameters) {
            builder.append(parameter);
          }
          builder.append(">;");
          assertEquals(builder.toString(), local.signature.toString());
        }
        found = local;
      }
    }
    assertNotNull("Line " + entry.line + ". Failed to find local with name: " + name, found);
    return found;
  }

  private static void checkLocalsDefined(DexDebugEntry entry, String... pairs) {
    assert pairs.length % 2 == 0;
    for (int i = 0; i < pairs.length; i += 2) {
      checkLocalDefined(entry, pairs[i], pairs[i + 1]);
    }
  }

  private static void checkLocalsEqual(DexDebugEntry entry, String[] pairs) {
    assert pairs == null || pairs.length % 2 == 0;
    int expected = pairs == null ? 0 : pairs.length / 2;
    Set<DebugLocalInfo> remaining = new HashSet<>(entry.locals.values());
    if (pairs != null) {
      for (int i = 0; i < pairs.length; i += 2) {
        DebugLocalInfo local = checkLocalDefined(entry, pairs[i], pairs[i + 1]);
        remaining.remove(local);
      }
    }
    assertEquals("Line " + entry.line + ". Found unexpected locals: " +
            String.join(",", remaining.stream().map(Object::toString).collect(Collectors.toList())),
        expected, expected + remaining.size());
  }
}
