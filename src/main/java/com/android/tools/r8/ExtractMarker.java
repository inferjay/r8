// Copyright (c) 2017, the Rex project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import com.google.common.collect.ImmutableList;

import com.android.tools.r8.dex.ApplicationReader;
import com.android.tools.r8.dex.Marker;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.OutputMode;
import com.android.tools.r8.utils.Timing;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

public class ExtractMarker {
  private static class Command extends BaseCommand {

    public static class Builder
        extends BaseCommand.Builder<ExtractMarker.Command, ExtractMarker.Command.Builder> {

      private Builder() {
        super(CompilationMode.RELEASE);
      }

      @Override
      ExtractMarker.Command.Builder self() {
        return this;
      }

      @Override
      public ExtractMarker.Command build() throws CompilationException, IOException {
        // If printing versions ignore everything else.
        if (isPrintHelp()) {
          return new ExtractMarker.Command(isPrintHelp());
        }
        validate();
        return new ExtractMarker.Command(
            getAppBuilder().build(), getOutputPath(), getOutputMode(), getMode(), getMinApiLevel());
      }
    }

    static final String USAGE_MESSAGE = String.join("\n", ImmutableList.of(
        "Usage: extractmarker [options] <input-files>",
        " where <input-files> are dex files",
        "  --version               # Print the version of r8.",
        "  --help                  # Print this message."));

    public static ExtractMarker.Command.Builder builder() {
      return new ExtractMarker.Command.Builder();
    }

    public static ExtractMarker.Command.Builder parse(String[] args)
        throws CompilationException, IOException {
      ExtractMarker.Command.Builder builder = builder();
      parse(args, builder);
      return builder;
    }

    private static void parse(String[] args, ExtractMarker.Command.Builder builder)
        throws CompilationException, IOException {
      for (int i = 0; i < args.length; i++) {
        String arg = args[i].trim();
        if (arg.length() == 0) {
          continue;
        } else if (arg.equals("--help")) {
          builder.setPrintHelp(true);
        } else {
          if (arg.startsWith("--")) {
            throw new CompilationException("Unknown option: " + arg);
          }
          builder.addProgramFiles(Paths.get(arg));
        }
      }
    }

    private Command(
        AndroidApp inputApp,
        Path outputPath,
        OutputMode outputMode,
        CompilationMode mode,
        int minApiLevel) {
      super(inputApp, outputPath, outputMode, mode, minApiLevel);
    }

    private Command(boolean printHelp) {
      super(printHelp, false);
    }

    @Override
    InternalOptions getInternalOptions() {
      return new InternalOptions();
    }
  }

  public static void main(String[] args)
      throws IOException, ProguardRuleParserException, CompilationException, ExecutionException {
    ExtractMarker.Command.Builder builder = ExtractMarker.Command.parse(args);
    ExtractMarker.Command command = builder.build();
    if (command.isPrintHelp()) {
      System.out.println(ExtractMarker.Command.USAGE_MESSAGE);
      return;
    }
    AndroidApp app = command.getInputApp();
    DexApplication dexApp =
        new ApplicationReader(app, new InternalOptions(), new Timing("ExtractMarker")).read();
    Marker readMarker = dexApp.dexItemFactory.extractMarker();
    if (readMarker == null) {
      System.out.println("D8/R8 marker not found.");
      System.exit(1);
    } else {
      System.out.println(readMarker.toString());
      System.exit(0);
    }
  }
}
