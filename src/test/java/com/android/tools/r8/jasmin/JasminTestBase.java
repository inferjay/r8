// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.jasmin;

import static org.junit.Assert.assertEquals;

import com.android.tools.r8.R8;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.ToolHelper.ProcessResult;
import com.android.tools.r8.graph.AppInfo;
import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.jasmin.JasminBuilder.ClassBuilder;
import com.android.tools.r8.naming.NamingLens;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.OutputMode;
import com.android.tools.r8.utils.StringUtils;
import com.google.common.collect.ImmutableList;
import jasmin.ClassFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public class JasminTestBase {

  @Rule
  public TemporaryFolder temp = ToolHelper.getTemporaryFolderForTest();

  protected ProcessResult runOnJavaRaw(JasminBuilder builder, String main) throws Exception {
    File out = temp.newFolder("classes");
    for (ClassBuilder clazz : builder.getClasses()) {
      ClassFile file = new ClassFile();
      file.readJasmin(new StringReader(clazz.toString()), clazz.name, true);
      Path path = out.toPath().resolve(clazz.name + ".class");
      Files.createDirectories(path.getParent());
      file.write(new FileOutputStream(path.toFile()));
    }
    return ToolHelper.runJava(ImmutableList.of(out.getPath()), main);
  }

  protected String runOnJava(JasminBuilder builder, String main) throws Exception {
    ProcessResult result = runOnJavaRaw(builder, main);
    if (result.exitCode != 0) {
      System.out.println("Std out:");
      System.out.println(result.stdout);
      System.out.println("Std err:");
      System.out.println(result.stderr);
      assertEquals(0, result.exitCode);
    }
    return result.stdout;
  }

  protected String runOnArt(JasminBuilder builder, String main) throws Exception {
    // TODO(zerny): Make the compiler depend on tool flag?
    return runOnArtR8(builder, main, new InternalOptions());
  }

  protected AndroidApp compileWithD8(JasminBuilder builder) throws Exception {
    return ToolHelper.runD8(builder.build());
  }

  protected String runOnArtD8(JasminBuilder builder, String main) throws Exception {
    return runOnArt(compileWithD8(builder), main);
  }

  protected String runOnArtR8(JasminBuilder builder, String main, InternalOptions options)
      throws Exception {
    DexApplication app = builder.read();
    app = process(app, options);
    AppInfo info = new AppInfo(app);
    AndroidApp outputApp =
        R8.writeApplication(
            Executors.newSingleThreadExecutor(),
            app,
            info,
            NamingLens.getIdentityLens(),
            null,
            null,
            options);
    return runOnArt(outputApp, main);
  }

  private ProcessResult runDx(JasminBuilder builder, File classes, Path dex) throws Exception {
    for (ClassBuilder clazz : builder.getClasses()) {
      ClassFile file = new ClassFile();
      file.readJasmin(new StringReader(clazz.toString()), clazz.name, true);
      file.write(new FileOutputStream(classes.toPath().resolve(clazz.name + ".class").toFile()));
    }
    List<String> args = new ArrayList<>();
    args.add("--output=" + dex.toString());
    args.add(classes.toString());
    System.out.println("running: dx " + StringUtils.join(args, " "));
    return ToolHelper.runDX(args.toArray(new String[args.size()]));
  }

  protected ProcessResult runOnArtDxRaw(JasminBuilder builder) throws Exception {
    return runDx(builder, temp.newFolder("classes_for_dx"),
        temp.getRoot().toPath().resolve("classes.dex"));
  }

  protected String runOnArtDx(JasminBuilder builder, String main) throws Exception {
    Path dex = temp.getRoot().toPath().resolve("classes.dex");
    ProcessResult result = runDx(builder, temp.newFolder("classes_for_dx"), dex);
    if (result.exitCode != 0) {
      System.out.println("Std out:");
      System.out.println(result.stdout);
      System.out.println("Std err:");
      System.out.println(result.stderr);
      assertEquals(0, result.exitCode);
    }
    return ToolHelper.runArtNoVerificationErrors(dex.toString(), main);
  }

  protected String runOnArt(AndroidApp app, String main) throws IOException {
    Path out = temp.getRoot().toPath().resolve("out.zip");
    app.writeToZip(out, OutputMode.Indexed);
    return ToolHelper.runArtNoVerificationErrors(ImmutableList.of(out.toString()), main, null);
  }

  protected static DexApplication process(DexApplication app, InternalOptions options)
      throws IOException, ProguardRuleParserException, ExecutionException {
    return new R8(options).optimize(app, new AppInfoWithSubtyping(app));
  }
}
