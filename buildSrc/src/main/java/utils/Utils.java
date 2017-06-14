// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package utils;

import java.io.File;

public class Utils {
  public static String toolsDir() {
    String osName = System.getProperty("os.name");
    if (osName.equals("Mac OS X")) {
      return "mac";
    } else if (osName.contains("Windows")) {
      return "windows";
    } else {
      return "linux";
    }
  }

  public static File dexMergerExecutable() {
    String executableName = Utils.toolsDir().equals("windows") ? "dexmerger.bat" : "dexmerger";
    return new File("tools/" + Utils.toolsDir() + "/dx/bin/" + executableName);
  }
}