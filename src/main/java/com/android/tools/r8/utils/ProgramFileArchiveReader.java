// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import static com.android.tools.r8.utils.FileUtils.isArchive;
import static com.android.tools.r8.utils.FileUtils.isClassFile;
import static com.android.tools.r8.utils.FileUtils.isDexFile;

import com.android.tools.r8.Resource;
import com.android.tools.r8.errors.CompilationError;
import com.google.common.io.ByteStreams;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

class ProgramFileArchiveReader {

  private final Path archive;
  private boolean ignoreDexInArchive;
  private List<Resource> dexResources = null;
  private List<Resource> classResources = null;

  ProgramFileArchiveReader(Path archive, boolean ignoreDexInArchive) {
    this.archive = archive;
    this.ignoreDexInArchive = ignoreDexInArchive;
  }

  private void readArchive() throws IOException {
    assert isArchive(archive);
    dexResources = new ArrayList<>();
    classResources = new ArrayList<>();
    try (ZipInputStream stream = new ZipInputStream(new FileInputStream(archive.toFile()))) {
      ZipEntry entry;
      while ((entry = stream.getNextEntry()) != null) {
        Path name = Paths.get(entry.getName());
        if (isDexFile(name)) {
          if (!ignoreDexInArchive) {
            Resource resource =
                new OneShotByteResource(Resource.Kind.DEX, ByteStreams.toByteArray(stream), null);
            dexResources.add(resource);
          }
        } else if (isClassFile(name)) {
          String descriptor = PreloadedClassFileProvider.guessTypeDescriptor(name);
          Resource resource = new OneShotByteResource(Resource.Kind.CLASSFILE,
              ByteStreams.toByteArray(stream), Collections.singleton(descriptor));
          classResources.add(resource);
        }
      }
    } catch (ZipException e) {
      throw new CompilationError(
          "Zip error while reading '" + archive + "': " + e.getMessage(), e);
    }
    if (!dexResources.isEmpty() && !classResources.isEmpty()) {
      throw new CompilationError(
          "Cannot create android app from an archive '" + archive
              + "' containing both DEX and Java-bytecode content");
    }
  }

  public Collection<Resource> getDexProgramResources() throws IOException {
    if (dexResources == null) {
      readArchive();
    }
    List<Resource> result = dexResources;
    dexResources = null;
    return result;
  }

  public Collection<Resource> getClassProgramResources() throws IOException {
    if (classResources == null) {
      readArchive();
    }
    List<Resource> result = classResources;
    classResources = null;
    return result;
  }
}
