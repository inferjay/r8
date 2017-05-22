// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.rewrite.longcompare;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.D8;
import com.android.tools.r8.D8Command;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.ToolHelper.ArtCommandBuilder;
import com.android.tools.r8.utils.DexInspector;
import com.android.tools.r8.utils.DexInspector.ClassSubject;
import com.android.tools.r8.utils.DexInspector.InstructionSubject;
import com.android.tools.r8.utils.DexInspector.InvokeInstructionSubject;
import com.android.tools.r8.utils.DexInspector.MethodSubject;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests checking that Long.compare() is always rewritten into long compare instruction.
 */
public class LongCompare {

  @Rule
  public TemporaryFolder tmpOutputDir = ToolHelper.getTemporaryFolderForTest();

  void compileWithD8(Path intputPath, Path outputPath)
      throws IOException, CompilationException {
    D8.run(D8Command.builder().addProgramFiles(intputPath).setOutputPath(outputPath).build());
  }

  void runTest(Path dexFile) {
    ArtCommandBuilder builder = new ArtCommandBuilder(ToolHelper.getDexVm());
    builder.appendClasspath(dexFile.toString());
    builder.setMainClass("rewrite.LongCompare");
    try {
      String output = ToolHelper.runArt(builder);
      Assert.assertEquals("-1\n1\n-1\n0\ntrue\nfalse\nfalse\nfalse\n", output);
    } catch (IOException e) {
      Assert.fail();
    }
  }

  @Test
  public void testLongCompareIsRewritten()
      throws IOException, CompilationException, ExecutionException {
    final Path inputPath = Paths.get(ToolHelper.EXAMPLES_BUILD_DIR + "/rewrite.jar");
    Path outputPath = tmpOutputDir.newFolder().toPath();

    compileWithD8(inputPath, outputPath);

    Path dexPath = outputPath.resolve("classes.dex");

    DexInspector dexInspector = new DexInspector(dexPath);
    ClassSubject classSubject = dexInspector.clazz("rewrite.LongCompare");
    MethodSubject methodSubject = classSubject
        .method("int", "simpleCompare", Arrays.asList("long", "long"));
    // Check that exception handler is removed since it is no longer needed.
    Assert.assertFalse(methodSubject.getMethod().getCode().asDexCode().usesExceptionHandling());
    Iterator<InvokeInstructionSubject> iterator =
        methodSubject.iterateInstructions(InstructionSubject::isInvoke);
    Assert.assertFalse(iterator.hasNext());

    runTest(dexPath);
  }
}
