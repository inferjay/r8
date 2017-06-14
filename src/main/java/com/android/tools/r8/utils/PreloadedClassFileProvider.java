// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import static com.android.tools.r8.utils.FileUtils.CLASS_EXTENSION;
import static com.android.tools.r8.utils.FileUtils.isArchive;
import static com.android.tools.r8.utils.FileUtils.isClassFile;

import com.android.tools.r8.ClassFileResourceProvider;
import com.android.tools.r8.Resource;
import com.android.tools.r8.errors.CompilationError;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

/** Lazy Java class file resource provider based on preloaded/prebuilt context. */
public final class PreloadedClassFileProvider implements ClassFileResourceProvider {
  private final Map<String, byte[]> content;

  private PreloadedClassFileProvider(Map<String, byte[]> content) {
    this.content = content;
  }

  @Override
  public Set<String> getClassDescriptors() {
    return Sets.newHashSet(content.keySet());
  }

  @Override
  public Resource getResource(String descriptor) {
    byte[] bytes = content.get(descriptor);
    if (bytes == null) {
      return null;
    }
    return Resource.fromBytes(Resource.Kind.CLASSFILE, bytes, Collections.singleton(descriptor));
  }

  /** Create preloaded content resource provider from archive file. */
  public static ClassFileResourceProvider fromArchive(Path archive) throws IOException {
    assert isArchive(archive);
    Builder builder = builder();
    try (ZipInputStream stream = new ZipInputStream(new FileInputStream(archive.toFile()))) {
      ZipEntry entry;
      while ((entry = stream.getNextEntry()) != null) {
        String name = entry.getName();
        if (isClassFile(Paths.get(name))) {
          builder.addResource(guessTypeDescriptor(name), ByteStreams.toByteArray(stream));
        }
      }
    } catch (ZipException e) {
      throw new CompilationError(
          "Zip error while reading '" + archive + "': " + e.getMessage(), e);
    }

    return builder.build();
  }

  // Guess class descriptor from location of the class file.
  static String guessTypeDescriptor(Path name) {
    return guessTypeDescriptor(name.toString());
  }

  // Guess class descriptor from location of the class file.
  private static String guessTypeDescriptor(String name) {
    assert name != null;
    assert name.endsWith(CLASS_EXTENSION) :
        "Name " + name + " must have " + CLASS_EXTENSION + " suffix";
    String fileName =
            File.separatorChar == '/' ? name.toString() :
                    name.toString().replace(File.separatorChar, '/');
    String descriptor = fileName.substring(0, fileName.length() - CLASS_EXTENSION.length());
    if (descriptor.contains(".")) {
      throw new CompilationError("Unexpected file name in the archive: " + fileName);
    }
    return 'L' + descriptor + ';';
  }

  @Override
  public String toString() {
    return content.size() + " preloaded resources";
  }

  /** Create a new empty builder. */
  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private Map<String, byte[]> content = new HashMap<>();

    private Builder() {
    }

    public Builder addResource(String descriptor, byte[] bytes) {
      assert content != null;
      assert descriptor != null;
      assert bytes != null;
      assert !content.containsKey(descriptor);
      content.put(descriptor, bytes);
      return this;
    }

    public PreloadedClassFileProvider build() {
      assert content != null;
      PreloadedClassFileProvider provider = new PreloadedClassFileProvider(content);
      content = null;
      return provider;
    }
  }
}
