// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import static com.android.tools.r8.D8Command.USAGE_MESSAGE;

import com.android.tools.r8.dex.ApplicationReader;
import com.android.tools.r8.dex.ApplicationWriter;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.graph.AppInfo;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.ir.conversion.IRConverter;
import com.android.tools.r8.naming.NamingLens;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.CfgPrinter;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.ThreadUtils;
import com.android.tools.r8.utils.Timing;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * The D8 dex compiler.
 *
 * <p>D8 performs modular compilation to DEX bytecode. It supports compilation of Java bytecode and
 * Android DEX bytecode to DEX bytecode including merging a mix of these input formats.
 *
 * <p>The D8 dexer API is intentionally limited and should "do the right thing" given a command. If
 * this API does not suffice please contact the D8/R8 team.
 *
 * <p>The compiler is invoked by calling {@link #run(D8Command) D8.run} with an appropriate {@link
 * D8Command}. For example:
 *
 * <pre>
 *   D8.run(D8Command.builder()
 *       .addProgramFiles(inputPathA, inputPathB)
 *       .setOutputPath(outputPath)
 *       .build());
 * </pre>
 *
 * The above reads the input files denoted by {@code inputPathA} and {@code inputPathB}, compiles
 * them to DEX bytecode (compiling from Java bytecode for such inputs and merging for DEX inputs),
 * and then writes the result to the directory or zip archive specified by {@code outputPath}.
 */
public final class D8 {

  private static final int STATUS_ERROR = 1;

  private D8() {}

  /**
   * Main API entry for the D8 dexer.
   *
   * @param command D8 command.
   * @return the compilation result.
   */
  public static D8Output run(D8Command command) throws IOException {
    InternalOptions options = command.getInternalOptions();
    CompilationResult result = runForTesting(command.getInputApp(), options);
    assert result != null;
    D8Output output = new D8Output(result.androidApp, command.getOutputMode());
    if (command.getOutputPath() != null) {
      output.write(command.getOutputPath());
    }
    return output;
  }

  /**
   * Main API entry for the D8 dexer.
   *
   * <p>The D8 dexer API is intentionally limited and should "do the right thing" given a set of
   * inputs. If the API does not suffice please contact the R8 team.
   *
   * @param command D8 command.
   * @param executor executor service from which to get threads for multi-threaded processing.
   * @return the compilation result.
   */
  public static D8Output run(D8Command command, ExecutorService executor) throws IOException {
    InternalOptions options = command.getInternalOptions();
    CompilationResult result = runForTesting(
        command.getInputApp(), options, executor);
    assert result != null;
    D8Output output = new D8Output(result.androidApp, command.getOutputMode());
    if (command.getOutputPath() != null) {
      output.write(command.getOutputPath());
    }
    return output;
  }

  private static void run(String[] args) throws IOException, CompilationException {
    D8Command.Builder builder = D8Command.parse(args);
    if (builder.getOutputPath() == null) {
      builder.setOutputPath(Paths.get("."));
    }
    D8Command command = builder.build();
    if (command.isPrintHelp()) {
      System.out.println(USAGE_MESSAGE);
      return;
    }
    if (command.isPrintVersion()) {
      System.out.println("D8 v0.0.1");
      return;
    }
    run(command);
  }

  /** Command-line entry to D8. */
  public static void main(String[] args) throws IOException {
    if (args.length == 0) {
      System.err.println(USAGE_MESSAGE);
      System.exit(STATUS_ERROR);
    }
    try {
      run(args);
    } catch (NoSuchFileException e) {
      System.err.println("File not found: " + e.getFile());
      System.exit(STATUS_ERROR);
    } catch (FileAlreadyExistsException e) {
      System.err.println("File already exists: " + e.getFile());
      System.exit(STATUS_ERROR);
    } catch (IOException e) {
      System.err.println("Failed to read or write application files: " + e.getMessage());
      System.exit(STATUS_ERROR);
    } catch (RuntimeException e) {
      System.err.println("Compilation failed with an internal error.");
      Throwable cause = e.getCause() == null ? e : e.getCause();
      cause.printStackTrace();
      System.exit(STATUS_ERROR);
    } catch (CompilationException e) {
      System.err.println("Compilation failed: " + e.getMessage());
      System.err.println(USAGE_MESSAGE);
      System.exit(STATUS_ERROR);
    }
  }

  static CompilationResult runForTesting(AndroidApp inputApp, InternalOptions options)
      throws IOException {
    ExecutorService executor = ThreadUtils.getExecutorService(options);
    try {
      return runForTesting(inputApp, options, executor);
    } finally {
      executor.shutdown();
    }
  }

  static CompilationResult runForTesting(
      AndroidApp inputApp, InternalOptions options, ExecutorService executor) throws IOException {
    try {
      assert !inputApp.hasPackageDistribution();

      // Disable global optimizations.
      options.skipMinification = true;
      options.allowAccessModification = false;
      options.inlineAccessors = false;
      options.outline.enabled = false;

      Timing timing = new Timing("DX timer");
      DexApplication app = new ApplicationReader(inputApp, options, timing).read(executor);
      AppInfo appInfo = new AppInfo(app);
      app = optimize(app, appInfo, options, timing, executor);

      // If a method filter is present don't produce output since the application is likely partial.
      if (options.hasMethodsFilter()) {
        System.out.println("Finished compilation with method filter: ");
        options.methodsFilter.forEach((m) -> System.out.println("  - " + m));
        return null;
      }

      CompilationResult output =
          new CompilationResult(
              new ApplicationWriter(app, appInfo, options, NamingLens.getIdentityLens(), null)
                .write(null, executor),
                app,
                appInfo);

      options.printWarnings();
      return output;
    } catch (ExecutionException e) {
      if (e.getCause() instanceof CompilationError) {
        throw (CompilationError) e.getCause();
      } else {
        throw new RuntimeException(e.getMessage(), e.getCause());
      }
    }
  }

  private static DexApplication optimize(
      DexApplication application, AppInfo appInfo, InternalOptions options,
      Timing timing, ExecutorService executor)
      throws IOException, ExecutionException {
    final CfgPrinter printer = options.printCfg ? new CfgPrinter() : null;

    IRConverter converter = new IRConverter(timing, application, appInfo, options, printer);
    application = converter.convertToDex(executor);

    if (options.printCfg) {
      if (options.printCfgFile == null || options.printCfgFile.isEmpty()) {
        System.out.print(printer.toString());
      } else {
        java.io.FileWriter writer = new java.io.FileWriter(options.printCfgFile);
        writer.write(printer.toString());
        writer.close();
      }
    }
    return application;
  }
}
