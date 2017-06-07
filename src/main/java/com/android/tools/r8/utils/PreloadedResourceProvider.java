// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import static com.android.tools.r8.utils.FileUtils.CLASS_EXTENSION;
import static com.android.tools.r8.utils.FileUtils.isArchive;
import static com.android.tools.r8.utils.FileUtils.isClassFile;

import com.android.tools.r8.Resource;
import com.android.tools.r8.ResourceProvider;
import com.android.tools.r8.errors.CompilationError;
import com.google.common.io.ByteStreams;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

/**
 * Lazy resource provider based on preloaded/prebuilt context.
 *
 * NOTE: only handles classfile resources.
 */
public final class PreloadedResourceProvider implements ResourceProvider {
  private final Map<String, byte[]> content;

  private PreloadedResourceProvider(Map<String, byte[]> content) {
    this.content = content;
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
  public static ResourceProvider fromArchive(Path archive) throws IOException {
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
    String descriptor = name.substring(0, name.length() - CLASS_EXTENSION.length());
    if (descriptor.contains(".")) {
      throw new CompilationError("Unexpected file name in the archive: " + name);
    }
    return 'L' + descriptor + ';';
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

    public PreloadedResourceProvider build() {
      assert content != null;
      PreloadedResourceProvider provider = new PreloadedResourceProvider(content);
      content = null;
      return provider;
    }
  }
}
