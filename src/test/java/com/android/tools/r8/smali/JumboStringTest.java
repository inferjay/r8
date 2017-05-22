// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.smali;

import static org.junit.Assert.assertEquals;

import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.StringUtils;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class JumboStringTest extends SmaliTestBase {

  @Test
  public void test() {
    StringBuilder builder = new StringBuilder();
    StringBuilder expectedBuilder = new StringBuilder();
    builder.append("    new-instance         v0, Ljava/lang/StringBuilder;\n");
    builder.append("    invoke-direct        { v0 }, Ljava/lang/StringBuilder;-><init>()V\n");
    for (int i = 0; i <= 0xffff + 2; i++) {
      String prefixed = StringUtils.zeroPrefix(i, 5);
      expectedBuilder.append(prefixed);
      expectedBuilder.append("\n");
      builder.append("  const-string         v1, \"" + prefixed + "\\n\"\n");
      builder.append(
          "  invoke-virtual       { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;\n");
    }
    builder.append(
        "    invoke-virtual       { v0 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;\n");
    builder.append("    move-result-object   v0\n");
    builder.append("    return-object               v0\n");

    SmaliBuilder smaliBuilder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    MethodSignature signature = smaliBuilder.addStaticMethod(
        "java.lang.String",
        DEFAULT_METHOD_NAME,
        ImmutableList.of(),
        2,
        builder.toString()
    );

    smaliBuilder.addMainMethod(
        2,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    invoke-static       {}, LTest;->method()Ljava/lang/String;",
        "    move-result-object  v1",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(Ljava/lang/String;)V",
        "    return-void"
    );

    InternalOptions options = new InternalOptions();
    DexApplication originalApplication = buildApplication(smaliBuilder, options);
    DexApplication processedApplication = processApplication(originalApplication, options);
    String result = runArt(processedApplication, options);

    assertEquals(expectedBuilder.toString(), result);
  }
}
