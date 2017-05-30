// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package trywithresources;

public class TryWithResourcesDesugaredTests extends TryWithResources {
  private boolean isAndroid() {
    try {
      Class.forName("dalvik.system.VMRuntime");
      return true;
    } catch (Exception ignored) {
    }
    return false;
  }

  @Override
  boolean desugaredCodeRunningOnJvm() {
    return !isAndroid();
  }

  public static void main(String[] args) throws Exception {
    new TryWithResourcesDesugaredTests().test();
  }
}
