// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.utils.AndroidApp;
import java.io.IOException;
import java.nio.file.Path;

/** Represents the output of a D8 compilation. */
public class D8Output extends BaseOutput {

  D8Output(AndroidApp app) {
    super(app);
  }

  @Override
  public void write(Path output) throws IOException {
    getAndroidApp().write(output);
  }
}
