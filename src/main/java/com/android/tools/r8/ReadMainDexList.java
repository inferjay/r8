// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.naming.ClassNameMapper;
import com.android.tools.r8.naming.ProguardMapReader;
import com.android.tools.r8.utils.FileUtils;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * Utility for applying proguard map and sorting the main dex list.
 */
public class ReadMainDexList {

  private String DOT_CLASS = ".class";

  private String stripDotClass(String name) {
    return name.endsWith(DOT_CLASS) ? name.substring(0, name.length() - DOT_CLASS.length()) : name;
  }

  private String addDotClass(String name) {
    return name + DOT_CLASS;
  }

  private String deobfuscateClassName(String name, ClassNameMapper mapper) {
    if (mapper == null) {
      return name;
    }
    return mapper.deobfuscateClassName(name);
  }

  private void run(String[] args) throws Exception {
    if (args.length != 1 && args.length != 2) {
      System.out.println("Usage: command <main_dex_list> [<proguard_map>]");
      System.exit(0);
    }

    final ClassNameMapper mapper =
        args.length == 2 ? ProguardMapReader.mapperFromFile(Paths.get(args[1])) : null;

    FileUtils.readTextFile(Paths.get(args[0]))
        .stream()
        .map(this::stripDotClass)
        .map(name -> name.replace('/', '.'))
        .map(name -> deobfuscateClassName(name, mapper))
        .map(name -> name.replace('.', '/'))
        .map(this::addDotClass)
        .sorted()
        .collect(Collectors.toList())
        .forEach(System.out::println);
  }

  public static void main(String[] args) throws Exception {
    new ReadMainDexList().run(args);
  }
}
