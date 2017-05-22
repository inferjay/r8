// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.smali;

import static org.junit.Assert.assertTrue;

import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.utils.InternalOptions;
import org.junit.Test;

public class Regress38014736 extends SmaliTestBase {

  @Test
  public void handlerRangeStartingOnMoveResult() {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    builder.addMainMethod(
        2,
        "new-instance        v1, Ljava/lang/Integer;",
        "const/4             v0, 0x00",
        "invoke-direct       { v1, v0 }, Ljava/lang/Integer;-><init>(I)V",
        "invoke-virtual      { v1 }, Ljava/lang/Integer;->intValue()I",
        "move-result         v1",
        "invoke-static       { v1 }, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;",
        "move-result-object  v1",
        "invoke-virtual      { v1 }, Ljava/lang/Integer;->intValue()I",
        ":try_start", // The try block starts on the move result instruction.
        "move-result         v1",
        "sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(I)V",
        "const/4             v1, 0x00  # 0",
        "invoke-static       { v1 }, "
            + "Ljava/lang/Integer;->valueOf(Ljava/lang/String;)Ljava/lang/Integer;",
        "move-result-object  v1",
        "invoke-virtual      { v1 }, Ljava/lang/Integer;->intValue()I",
        ":try_end", // The try block ends on the move result instruction.
        "move-result         v1",
        "sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(I)V",
        ".catch Ljava/lang/NumberFormatException; {:try_start .. :try_end} :catch_block",
        "return-void",
        ":catch_block",
        "move-exception      v1",
        "sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(Ljava/lang/Object;)V",
        "return-void");

    InternalOptions options = new InternalOptions();
    DexApplication originalApplication = buildApplication(builder, options);
    DexApplication processedApplication = processApplication(originalApplication, options);
    String result = runArt(processedApplication, options);
    // The art runtime changed the way exceptions are printed. Therefore, we only check
    // for the type of the exception and that the message mentions null.
    assertTrue(result.startsWith("0\njava.lang.NumberFormatException:"));
    assertTrue(result.contains("null"));
  }
}
