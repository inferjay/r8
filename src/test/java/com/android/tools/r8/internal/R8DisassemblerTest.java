// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.internal;

import static com.android.tools.r8.utils.AndroidApp.DEFAULT_PROGUARD_MAP_FILE;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.Disassemble;
import com.android.tools.r8.R8;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.android.tools.r8.utils.FileUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.runner.RunWith;

// Invoke R8 on the dex files extracted from GMSCore.apk to disassemble the dex code.
@RunWith(Theories.class)
public class R8DisassemblerTest {

  static final String APP_DIR = "third_party/gmscore/v5/";

  public void testDisassemble(boolean deobfuscate, boolean smali)
      throws IOException, ExecutionException, ProguardRuleParserException, CompilationException {
    // This test only ensures that we do not break disassembling of dex code. It does not
    // check the generated code. To make it fast, we get rid of the output.
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(new OutputStream() {
      public void write(int b) { /* ignore*/ }
    }));

    try {
      Disassemble.DisassembleCommand.Builder builder = Disassemble.DisassembleCommand.builder();
      builder.setUseSmali(smali);
      if (deobfuscate) {
        builder.setProguardMapFile(Paths.get(APP_DIR, DEFAULT_PROGUARD_MAP_FILE));
      }
      builder.addProgramFiles(
          Files.list(Paths.get(APP_DIR))
              .filter(FileUtils::isDexFile)
              .collect(Collectors.toList()));
      R8.disassemble(builder.build());
    } finally {
      // Restore System.out for good measure.
      System.setOut(originalOut);
    }
  }

  @Test
  public void test1() throws  Exception {
    testDisassemble(false, false);
  }

  @Test
  public void test2() throws  Exception {
    testDisassemble(false, true);
  }

  @Test
  public void test3() throws  Exception {
    testDisassemble(true, false);
  }

  @Test
  public void test4() throws  Exception {
    testDisassemble(true, true);
  }
}
