// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import static com.android.tools.r8.utils.FileUtils.CLASS_EXTENSION;

import com.android.tools.r8.Resource;
import com.android.tools.r8.ResourceProvider;
import java.io.File;
import java.nio.file.Path;

/**
 * Lazy resource provider based on filesystem directory content.
 *
 * NOTE: only handles classfile resources.
 */
public final class DirectoryResourceProvider implements ResourceProvider {
  private final Path root;

  private DirectoryResourceProvider(Path root) {
    this.root = root;
  }

  @Override
  public Resource getResource(String descriptor) {
    assert DescriptorUtils.isClassDescriptor(descriptor);

    // Build expected file path based on type descriptor.
    String classBinaryName = DescriptorUtils.getClassBinaryNameFromDescriptor(descriptor);
    Path filePath = root.resolve(classBinaryName + CLASS_EXTENSION);
    File file = filePath.toFile();

    return (file.exists() && !file.isDirectory())
        ? Resource.fromFile(Resource.Kind.CLASSFILE, filePath) : null;
  }

  /** Create resource provider from directory path. */
  public static ResourceProvider fromDirectory(Path dir) {
    return new DirectoryResourceProvider(dir.toAbsolutePath());
  }
}
