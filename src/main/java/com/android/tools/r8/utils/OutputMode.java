// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import com.android.tools.r8.Resource;

/** Defines way the output is formed. */
public enum OutputMode {
  Indexed {
    @Override
    String getFileName(Resource resource, int index) {
      return index == 0 ? "classes.dex" : ("classes" + (index + 1) + ".dex");
    }
  },
  FilePerClass {
    @Override
    String getFileName(Resource resource, int index) {
      assert resource instanceof InternalResource;
      String classDescriptor = ((InternalResource) resource).getSingleClassDescriptorOrNull();
      assert classDescriptor != null;
      assert !classDescriptor.contains(".");
      return DescriptorUtils.descriptorToJavaType(classDescriptor) + ".dex";
    }
  };

  abstract String getFileName(Resource resource, int index);
}
