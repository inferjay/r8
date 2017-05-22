// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import static com.android.tools.r8.utils.DescriptorUtils.JAVA_PACKAGE_SEPARATOR;
import static com.android.tools.r8.utils.FileUtils.CLASS_EXTENSION;

import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexType;
import com.google.common.collect.Sets;
import com.google.common.io.Closer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class MainDexList {

  public static Set<DexType> parse(Path path, DexItemFactory itemFactory) throws IOException {
    try (Closer closer = Closer.create()) {
      return parse(closer.register(Files.newInputStream(path)), itemFactory);
    }
  }

  public static Set<DexType> parse(InputStream input, DexItemFactory itemFactory) {
    Set<DexType> result = Sets.newIdentityHashSet();
    try {
      BufferedReader file =
          new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
      String line;
      while ((line = file.readLine()) != null) {
        if (!line.endsWith(CLASS_EXTENSION)) {
          throw new CompilationError("Illegal main-dex-list entry '" + line + "'.");
        }
        String name = line.substring(0, line.length() - CLASS_EXTENSION.length());
        if (name.contains("" + JAVA_PACKAGE_SEPARATOR)) {
          throw new CompilationError("Illegal main-dex-list entry '" + line + "'.");
        }
        String descriptor = "L" + name + ";";
        result.add(itemFactory.createType(descriptor));
      }
    } catch (IOException e) {
      throw new CompilationError("Cannot load main-dex-list.");
    }
    return result;
  }
}
