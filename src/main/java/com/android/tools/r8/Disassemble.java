// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.android.tools.r8.shaking.ProguardRuleParserException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class Disassemble {
  public static void main(String[] args)
      throws IOException, ProguardRuleParserException, CompilationException, ExecutionException {
    List<Path> files = Arrays.stream(args).map(s -> Paths.get(s)).collect(Collectors.toList());
    R8Command command = R8Command.builder().addProgramFiles(files).build();
    R8.disassemble(command);
  }
}
