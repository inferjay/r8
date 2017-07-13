// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import com.android.tools.r8.CompilationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileUtils {

  public static final String APK_EXTENSION = ".apk";
  public static final String CLASS_EXTENSION = ".class";
  public static final String DEX_EXTENSION = ".dex";
  public static final String JAR_EXTENSION = ".jar";
  public static final String ZIP_EXTENSION = ".zip";
  public static final String DEFAULT_DEX_FILENAME = "classes.dex";

  public static boolean isDexFile(Path path) {
    String name = path.getFileName().toString().toLowerCase();
    return name.endsWith(DEX_EXTENSION);
  }

  public static boolean isClassFile(Path path) {
    String name = path.getFileName().toString().toLowerCase();
    return name.endsWith(CLASS_EXTENSION);
  }

  public static boolean isJarFile(Path path) {
    String name = path.getFileName().toString().toLowerCase();
    return name.endsWith(JAR_EXTENSION);
  }

  public static boolean isZipFile(Path path) {
    String name = path.getFileName().toString().toLowerCase();
    return name.endsWith(ZIP_EXTENSION);
  }

  public static boolean isApkFile(Path path) {
    String name = path.getFileName().toString().toLowerCase();
    return name.endsWith(APK_EXTENSION);
  }

  public static boolean isArchive(Path path) {
    String name = path.getFileName().toString().toLowerCase();
    return name.endsWith(APK_EXTENSION)
        || name.endsWith(JAR_EXTENSION)
        || name.endsWith(ZIP_EXTENSION);
  }

  public static List<String> readTextFile(Path file) throws IOException {
    try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
      List<String> result = new ArrayList<>();
      String line;
      while ((line = reader.readLine()) != null) {
        result.add(line);
      }
      return result;
    }
  }

  public static void writeTextFile(Path file, List<String> lines) throws IOException {
    Files.write(file, lines);
  }

  public static void writeTextFile(Path file, String... lines) throws IOException {
    Files.write(file, Arrays.asList(lines));
  }

  public static Path validateOutputFile(Path path) throws CompilationException {
    if (path != null) {
      boolean isJarOrZip = isZipFile(path) || isJarFile(path);
      if (!isJarOrZip  && !(Files.exists(path) && Files.isDirectory(path))) {
        throw new CompilationException(
            "Invalid output: "
                + path +
                "\nOutput must be a .zip or .jar archive or an existing directory");
      }
    }
    return path;
  }
}
