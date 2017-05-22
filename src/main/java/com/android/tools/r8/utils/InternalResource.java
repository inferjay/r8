// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.utils;

import com.android.tools.r8.Resource;
import com.google.common.io.Closer;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/** Internal resource class that is not intended for use from the outside.
 *
 * <p>This is only here to hide the creation and class descriptor methods
 * from the javadoc for the D8 API. If we decide to expose those methods
 * later the split between Resource and InternalResource can be removed.
 */
public abstract class InternalResource implements Resource {

  private final Kind kind;

  private InternalResource(Kind kind) {
    this.kind = kind;
  }

  /** Returns class descriptor if known, otherwise returns null. */
  public abstract String getClassDescriptor();

  /** Get the kind of the resource. */
  public Kind getKind() {
    return kind;
  }

  /** Create an application resource for a given file and kind. */
  public static InternalResource fromFile(Kind kind, Path file) {
    return new FileResource(kind, file);
  }

  /** Create an application resource for a given content and kind. */
  public static InternalResource fromBytes(Kind kind, byte[] bytes) {
    return fromBytes(kind, bytes, null);
  }

  /** Create an application resource for a given content, kind and type descriptor. */
  public static InternalResource fromBytes(Kind kind, byte[] bytes, String typeDescriptor) {
    return new ByteResource(kind, bytes, typeDescriptor);
  }

  /** File based application resource. */
  private static class FileResource extends InternalResource {
    final Path file;

    FileResource(Kind kind, Path file) {
      super(kind);
      assert file != null;
      this.file = file;
    }

    @Override
    public String getClassDescriptor() {
      return null;
    }

    @Override
    public InputStream getStream(Closer closer) throws IOException {
      return closer.register(new FileInputStream(file.toFile()));
    }
  }

  /** Byte content based application resource. */
  private static class ByteResource extends InternalResource {
    final String classDescriptor;
    final byte[] bytes;

    ByteResource(Kind kind, byte[] bytes, String classDescriptor) {
      super(kind);
      assert bytes != null;
      this.classDescriptor = classDescriptor;
      this.bytes = bytes;
    }

    @Override
    public String getClassDescriptor() {
      return classDescriptor;
    }

    @Override
    public InputStream getStream(Closer closer) throws IOException {
      // Note: closing a byte-array input stream is a no-op.
      return new ByteArrayInputStream(bytes);
    }
  }
}
