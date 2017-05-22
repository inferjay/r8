// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenSource;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.writer.builder.DexBuilder;
import org.jf.dexlib2.writer.io.MemoryDataStore;
import org.jf.smali.LexerErrorInterface;
import org.jf.smali.smaliFlexLexer;
import org.jf.smali.smaliParser;
import org.jf.smali.smaliTreeWalker;

// Adapted from org.jf.smali.SmaliTestUtils.
public class Smali {

  public static byte[] compile(String smaliText) throws RecognitionException, IOException {
    return compile(smaliText, 15);
  }

  public static byte[] compile(String... smaliText) throws RecognitionException, IOException {
    return compile(Arrays.asList(smaliText), 15);
  }

  public static byte[] compile(String smaliText, int apiLevel)
      throws IOException, RecognitionException {
    return compile(ImmutableList.of(smaliText), apiLevel);
  }

  public static byte[] compile(List<String> smaliTexts)
      throws RecognitionException, IOException {
    return compile(smaliTexts, 15);
  }

  public static byte[] compile(List<String> smaliTexts, int apiLevel)
      throws RecognitionException, IOException {
    DexBuilder dexBuilder = new DexBuilder(Opcodes.forApi(apiLevel));

    for (String smaliText : smaliTexts) {
      Reader reader = new StringReader(smaliText);

      LexerErrorInterface lexer = new smaliFlexLexer(reader);
      CommonTokenStream tokens = new CommonTokenStream((TokenSource) lexer);

      smaliParser parser = new smaliParser(tokens);
      parser.setVerboseErrors(true);
      parser.setAllowOdex(false);
      parser.setApiLevel(apiLevel);

      smaliParser.smali_file_return result = parser.smali_file();

      if (parser.getNumberOfSyntaxErrors() > 0 || lexer.getNumberOfSyntaxErrors() > 0) {
        throw new RuntimeException(
            "Error occured while compiling text:\n" + StringUtils.join(smaliTexts, "\n"));
      }

      CommonTree t = result.getTree();

      CommonTreeNodeStream treeStream = new CommonTreeNodeStream(t);
      treeStream.setTokenStream(tokens);

      smaliTreeWalker dexGen = new smaliTreeWalker(treeStream);
      dexGen.setApiLevel(apiLevel);
      dexGen.setVerboseErrors(true);
      dexGen.setDexBuilder(dexBuilder);
      dexGen.smali_file();

      if (dexGen.getNumberOfSyntaxErrors() > 0) {
        throw new RuntimeException("Error occured while compiling text");
      }
    }

    MemoryDataStore dataStore = new MemoryDataStore();

    dexBuilder.writeTo(dataStore);

    // TODO(sgjesse): This returns the full backingstore from MemoryDataStore, which by default
    // is 1024k bytes. Our dex file reader does not complain though.
    return dataStore.getData();
  }
}
