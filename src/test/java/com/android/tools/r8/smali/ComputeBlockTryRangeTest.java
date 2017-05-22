// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.smali;

import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.utils.InternalOptions;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class ComputeBlockTryRangeTest extends SmaliTestBase {

  @Test
  public void jumpIntoTryRange() {

    SmaliBuilder builder = new SmaliBuilder("Test");

    builder.addStaticMethod(
        "void", "main", Arrays.asList("java.lang.String[]"), 2,
        "  sget-object v1, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "  const v0, 1",
        "  invoke-static {v0}, LTest;->method(I)I",
        "  move-result v0",
        "  invoke-virtual {v1, v0}, Ljava/io/PrintStream;->println(I)V",
        "  const v0, 0",
        "  invoke-static {v0}, LTest;->method(I)I",
        "  move-result v0",
        "  invoke-virtual {v1, v0}, Ljava/io/PrintStream;->println(I)V",
        "  return-void");

    MethodSignature methodSig = builder.addStaticMethod(
        "int", "method", Collections.singletonList("int"), 1,
        "  const v0, 42",
        "  goto :in_try",
        ":try_start",
        ":dead_code",
        "  const v0, 0",
        ":in_try",
        "  div-int/2addr v0, v1",
        "  return v0",
        ":try_end",
        "  .catch Ljava/io/IOException; {:try_start .. :try_end} :dead_code",
        "  .catch Ljava/lang/Throwable; {:try_start .. :try_end} :return_half",
        ":return_half",
        "  const v1, 2",
        "  goto :in_try"
    );

    InternalOptions options = new InternalOptions();
    DexApplication originalApplication = buildApplication(builder, options);
    DexApplication processedApplication = processApplication(originalApplication, options);

    DexEncodedMethod method = getMethod(processedApplication, methodSig);
    assert method.getCode().asDexCode().tries.length > 0;
  }
}
