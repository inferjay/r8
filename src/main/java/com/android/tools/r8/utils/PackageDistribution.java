// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class PackageDistribution {

  private static final String OLDFILE_PREFIX_TEXT =
      "\n"
          + "# Below follow the original package to file mapping rules. These have not been\n"
          + "# changed by R8.\n"
          + "\n";

  private static final String APPENDED_PREFIX_TEXT =
      "# The following packages had no mapping in the supplied package file. The\n"
          + "# mapping rules provided below reflect the mapping that was used by R8. Please\n"
          + "# use this updated map moving forward to ensure stability of package placement\n"
          + "# in DEX files (and thus minimize patch size).\n"
          + "#\n"
          + "# Note that the updated package placement might not be optimal. Shifting the new\n"
          + "# packages to DEX files that contain related packages might yield smaller DEX\n"
          + "# file sizes.\n"
          + "\n";

  private static final String NEW_PACKAGE_MAP_PREFIX_TEXT =
      "# This file provides a mapping of classes to DEX files in an Android multi-dex\n"
          +"# application. It is used in conjunction with the R8 DEX file optimizer\n"
          + "# to enforce a fixed distribution of classes to DEX files.\n"
          + "#\n"
          + "# Fixing the class distribution serves two purposes:\n"
          + "#\n"
          + "# 1. Keeping classes in the same DEX file reduces the size of patches between\n"
          + "#    two versions of an application.\n"
          + "# 2. Co-locating classes with their uses can reduce DEX file size. For example,\n"
          + "#    one might want to place the helper classes for credit card processing in\n"
          + "#    the same DEX file that contains the payment related logic.\n"
          + "#\n"
          + "# Entries in this file have the following form:\n"
          + "#\n"
          + "# <packageSpec>:<file number>\n"
          + "#\n"
          + "# Where packageSpec is either the name of a package, e.g., 'com.google.foo', or\n"
          + "# a package wildcard of the form 'com.google.bar.*'. The former matches exactly\n"
          + "# the classes in the given package, whereas the latter also matches classes in\n"
          + "# subpackages. PackageSpec entries may not overlap.\n"
          + "#\n"
          + "# Empty lines and lines starting with a '#' are ignored.\n"
          + "\n";

  private static final String NO_PACKAGE_MAP_REQUIRED_TEXT =
      "\n"
          +  "# Intentionally empty, as the output only has a single DEX file.\n"
          + "\n";

  private final Map<String, Integer> map;

  private PackageDistribution(Map<String, Integer> map) {
    this.map = map;
  }

  public static PackageDistribution load(InputStream input) throws IOException {
    return read(new BufferedReader(new InputStreamReader(input)));
  }

  public static PackageDistribution load(Path path) {
    try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      return read(reader);
    } catch (IOException e) {
      throw new RuntimeException("Error reading file " + path, e);
    }
  }

  private static PackageDistribution read(BufferedReader reader) throws IOException {
    String line = null;
    try {
      Map<String, Integer> result = new HashMap<>();
      while ((line = reader.readLine()) != null) {
        if (line.length() == 0 || line.startsWith("#")) {
          continue;
        }
        String[] parts = line.split(":");
        if (parts.length != 2) {
          throw new RuntimeException("Error parsing package map line " + line);
        }
        String prefix = parts[0];
        if (result.containsKey(prefix)) {
          throw new RuntimeException("Prefix is assigned twice: " + prefix);
        }
        int file = Integer.parseInt(parts[1]);
        result.put(prefix, file);
      }
      return new PackageDistribution(result);
    } catch (NumberFormatException e) {
      throw new RuntimeException("Error parsing package map line " + line, e);
    }
  }

  public static void formatEntry(Entry<String, Integer> entry, Writer writer) throws IOException {
    writer.write(entry.getKey());
    writer.write(":");
    writer.write(entry.getValue().toString());
  }

  public static void writePackageToFileMap(
      Path target, Map<String, Integer> mappings, PackageDistribution original) throws IOException {
    BufferedWriter writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8);
    if (mappings.isEmpty()) {
      if (original != null) {
        copyOriginalPackageMap(original, writer);
      } else {
        writer.write(NEW_PACKAGE_MAP_PREFIX_TEXT);
        writer.write(NO_PACKAGE_MAP_REQUIRED_TEXT);
      }
      writer.close();
      return;
    }
    if (original == null) {
      writer.write(NEW_PACKAGE_MAP_PREFIX_TEXT);
    } else {
      writer.write(APPENDED_PREFIX_TEXT);
    }
    for (Entry<String, Integer> entry : mappings.entrySet()) {
      formatEntry(entry, writer);
      writer.newLine();
    }
    if (original != null) {
      // Copy the original
      writer.write(OLDFILE_PREFIX_TEXT);
      copyOriginalPackageMap(original, writer);
    }
    writer.close();
  }

  private static void copyOriginalPackageMap(PackageDistribution original, BufferedWriter writer)
      throws IOException {
    for (Entry<String, Integer> entry : original.map.entrySet()) {
      formatEntry(entry, writer);
      writer.newLine();
    }
  }

  public int maxReferencedIndex() {
    return map.values().stream().max(Integer::compare).orElseGet(() -> 0);
  }

  public Set<String> getFiles() {
    return map.keySet();
  }

  public int get(String file) {
    return map.getOrDefault(file, -1);
  }

  public boolean containsFile(String file) {
    return map.containsKey(file);
  }
}
