// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.regress.b63935662;

import com.android.tools.r8.R8Command;
import com.android.tools.r8.TestBase;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.OffOrAuto;
import java.nio.file.Path;
import org.junit.Assert;
import org.junit.Test;

public class Regress63935662 extends TestBase {

  void run(AndroidApp app, Class mainClass) throws Exception {
    Path proguardConfig =
        writeTextToTempFile(keepMainProguardConfiguration(mainClass, true, false));
    R8Command command =
        ToolHelper.prepareR8CommandBuilder(app)
            .addProguardConfigurationFiles(proguardConfig)
            .setMinApiLevel(Constants.ANDROID_L_API)
            .build();
    String resultFromJava = runOnJava(mainClass);
    app = ToolHelper.runR8(command, options -> options.interfaceMethodDesugaring = OffOrAuto.Auto);
    String resultFromArt = runOnArt(app, mainClass);
    Assert.assertEquals(resultFromJava, resultFromArt);
  }

  @Test
  public void test() throws Exception {
    Class mainClass = TestClass.class;
    AndroidApp app = readClasses(
        TestClass.Top.class, TestClass.Left.class, TestClass.Right.class, TestClass.Bottom.class,
        TestClass.X1.class, TestClass.X2.class, TestClass.X3.class, TestClass.X4.class, TestClass.X5.class,
        mainClass);
    run(app, mainClass);
  }

  @Test
  public void test2() throws Exception {
    Class mainClass = TestFromBug.class;
    AndroidApp app = readClasses(
        TestFromBug.Map.class, TestFromBug.AbstractMap.class,
        TestFromBug.ConcurrentMap.class, TestFromBug.ConcurrentHashMap.class,
        mainClass);
    run(app, mainClass);
  }
}
