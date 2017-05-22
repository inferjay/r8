// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import com.google.common.io.ByteStreams;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class JarBuilder {
  public static void buildJar(File[] files, File jarFile) throws IOException {
    JarOutputStream target = new JarOutputStream(new FileOutputStream(jarFile));
    for (File file : files) {
      // Only use the file name in the JAR entry (classes.dex, classes2.dex, ...)
      JarEntry entry = new JarEntry(file.getName());
      entry.setTime(file.lastModified());
      target.putNextEntry(entry);
      InputStream in = new BufferedInputStream(new FileInputStream(file));
      ByteStreams.copy(in, target);
      in.close();
      target.closeEntry();
    }
    target.close();
  }
}
