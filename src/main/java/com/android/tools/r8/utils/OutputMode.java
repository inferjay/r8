// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import com.android.tools.r8.Resource;
import java.util.Set;

/** Defines way the output is formed. */
public enum OutputMode {
  Indexed {
    @Override
    String getOutputPath(Resource resource, int index) {
      return index == 0 ? "classes.dex" : ("classes" + (index + 1) + ".dex");
    }
  },
  FilePerClass {
    @Override
    String getOutputPath(Resource resource, int index) {
      Set<String> classDescriptors = resource.getClassDescriptors();
      assert classDescriptors != null;
      assert classDescriptors.size() == 1;
      String classDescriptor = classDescriptors.iterator().next();
      assert DescriptorUtils.isClassDescriptor(classDescriptor);
      return classDescriptor.substring(1, classDescriptor.length() - 1) + ".dex";
    }
  };

  abstract String getOutputPath(Resource resource, int index);
}
