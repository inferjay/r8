// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8;

import com.google.common.io.Closer;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Set;

/** Represents application resources. */
public abstract class Resource {
  /** Kind of the resource describing the resource content. */
  public enum Kind {
    DEX, CLASSFILE
  }

  private Resource(Kind kind) {
    this.kind = kind;
  }

  /** Kind of the resource. */
  public final Kind kind;

  /** Create an application resource for a given file. */
  public static Resource fromFile(Kind kind, Path file) {
    return new FileResource(kind, file);
  }

  /** Create an application resource for a given content. */
  public static Resource fromBytes(Kind kind, byte[] bytes) {
    return fromBytes(kind, bytes, null);
  }

  /** Create an application resource for a given content and type descriptor. */
  public static Resource fromBytes(Kind kind, byte[] bytes, Set<String> typeDescriptors) {
    return new ByteResource(kind, bytes, typeDescriptors);
  }

  /**
   * Returns the set of class descriptors for classes represented
   * by the resource if known, or `null' otherwise.
   */
  public abstract Set<String> getClassDescriptors();

  /** Get the resource as a stream. */
  public abstract InputStream getStream(Closer closer) throws IOException;

  /** File based application resource. */
  private static class FileResource extends Resource {
    final Path file;

    FileResource(Kind kind, Path file) {
      super(kind);
      assert file != null;
      this.file = file;
    }

    @Override
    public Set<String> getClassDescriptors() {
      return null;
    }

    @Override
    public InputStream getStream(Closer closer) throws IOException {
      return closer.register(new FileInputStream(file.toFile()));
    }
  }

  /** Byte content based application resource. */
  private static class ByteResource extends Resource {
    final Set<String> classDescriptors;
    final byte[] bytes;

    ByteResource(Kind kind, byte[] bytes, Set<String> classDescriptors) {
      super(kind);
      assert bytes != null;
      this.classDescriptors = classDescriptors;
      this.bytes = bytes;
    }

    @Override
    public Set<String> getClassDescriptors() {
      return classDescriptors;
    }

    @Override
    public InputStream getStream(Closer closer) throws IOException {
      // Note: closing a byte-array input stream is a no-op.
      return new ByteArrayInputStream(bytes);
    }
  }
}
