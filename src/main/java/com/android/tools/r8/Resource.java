// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.google.common.io.Closer;
import java.io.IOException;
import java.io.InputStream;

/** Represents application resources. */
public interface Resource {

  /** Application resource kind. */
  enum Kind {
    PROGRAM, CLASSPATH, LIBRARY
  }

  /** Get the kind of the resource. */
  Kind getKind();

  /** Get the resource as a stream. */
  InputStream getStream(Closer closer) throws IOException;
}
