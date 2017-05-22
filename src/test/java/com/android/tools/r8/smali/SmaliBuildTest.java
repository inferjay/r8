// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.smali;

import static org.junit.Assert.assertEquals;

import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.DexInspector;
import com.android.tools.r8.utils.DexInspector.ClassSubject;
import com.android.tools.r8.utils.InternalOptions;
import java.io.IOException;
import java.nio.file.Paths;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

public class SmaliBuildTest extends SmaliTestBase {

  private void checkJavaLangString(DexApplication application, boolean present) {
    DexInspector inspector = new DexInspector(application);
    ClassSubject clazz = inspector.clazz("java.lang.String");
    assertEquals(present, clazz.isPresent());
  }

  @Test
  public void buildWithoutLibrary() {
    // Build simple "Hello, world!" application.
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);
    builder.addMainMethod(
        2,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    const-string        v1, \"Hello, world!\"",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(Ljava/lang/String;)V",
        "    return-void"
    );

    InternalOptions options = new InternalOptions();
    // No libraries added - java.lang.String is not present.
    DexApplication originalApplication = buildApplication(builder, options);
    checkJavaLangString(originalApplication, false);

    DexApplication processedApplication = processApplication(originalApplication, options);
    checkJavaLangString(processedApplication, false);
  }

  @Test
  public void buildWithLibrary() throws IOException, RecognitionException {
    // Build simple "Hello, world!" application.
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);
    builder.addMainMethod(
        2,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    const-string        v1, \"Hello, world!\"",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(Ljava/lang/String;)V",
        "    return-void"
    );

    InternalOptions options = new InternalOptions();
    AndroidApp app = AndroidApp.builder()
        .addDexProgramData(builder.compile())
        .addLibraryFiles(Paths.get(ToolHelper.getDefaultAndroidJar()))
        .build();

    // Java standard library added - java.lang.String is present.
    DexApplication originalApplication = buildApplication(app, options);
    checkJavaLangString(originalApplication, true);

    DexApplication processedApplication = processApplication(originalApplication, options);
    checkJavaLangString(processedApplication, true);
  }
}
