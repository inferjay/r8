// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.android.tools.r8.dex.ApplicationReader;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.ListUtils;
import com.android.tools.r8.utils.Timing;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import joptsimple.internal.Strings;
import org.junit.Assume;
import org.junit.rules.TemporaryFolder;

public class ToolHelper {

  public static final String BUILD_DIR = "build/";
  public static final String EXAMPLES_DIR = "src/test/examples/";
  public static final String EXAMPLES_ANDROID_O_DIR = "src/test/examplesAndroidO/";
  public static final String EXAMPLES_BUILD_DIR = BUILD_DIR + "test/examples/";
  public static final String EXAMPLES_ANDROID_N_BUILD_DIR = BUILD_DIR + "test/examplesAndroidN/";
  public static final String EXAMPLES_ANDROID_O_BUILD_DIR = BUILD_DIR + "test/examplesAndroidO/";
  public static final String SMALI_BUILD_DIR = BUILD_DIR + "test/smali/";

  public static final String LINE_SEPARATOR = System.getProperty("line.separator");

  private static final String ANDROID_JAR_PATTERN = "third_party/android_jar/lib-v%d/android.jar";
  private static final int DEFAULT_MIN_SDK = 14;

  public enum DexVm {
    ART_4_4_4("4.4.4"),
    ART_5_1_1("5.1.1"),
    ART_6_0_1("6.0.1"),
    ART_7_0_0("7.0.0"),
    ART_DEFAULT("default");

    private static final ImmutableMap<String, DexVm> SHORT_NAME_MAP =
        new ImmutableMap.Builder<String, DexVm>()
            .putAll(
                Arrays.stream(DexVm.values()).collect(
                    Collectors.toMap(a -> a.toString(), a -> a)))
            .build();

    public String toString() {
      return shortName;
    }

    public static DexVm fromShortName(String shortName) {
      return SHORT_NAME_MAP.get(shortName);
    }

    public boolean isNewerThan(DexVm other) {
      return compareTo(other) > 0;
    }

    private DexVm(String shortName) {
      this.shortName = shortName;
    }

    private final String shortName;
  }


  public abstract static class CommandBuilder {

    private List<String> options = new ArrayList<>();
    private Map<String, String> systemProperties = new LinkedHashMap<>();
    private List<String> classpaths = new ArrayList<>();
    private String mainClass;
    private List<String> programArguments = new ArrayList<>();
    private List<String> bootClassPaths = new ArrayList<>();

    public CommandBuilder appendArtOption(String option) {
      options.add(option);
      return this;
    }

    public CommandBuilder appendArtSystemProperty(String key, String value) {
      systemProperties.put(key, value);
      return this;
    }

    public CommandBuilder appendClasspath(String classpath) {
      classpaths.add(classpath);
      return this;
    }

    public CommandBuilder setMainClass(String className) {
      this.mainClass = className;
      return this;
    }

    public CommandBuilder appendProgramArgument(String option) {
      programArguments.add(option);
      return this;
    }

    public CommandBuilder appendBootClassPath(String lib) {
      bootClassPaths.add(lib);
      return this;
    }

    private List<String> command() {
      List<String> result = new ArrayList<>();
      // The art script _must_ be run with bash, bots default to /bin/dash for /bin/sh, so
      // explicitly set it;
      if (isLinux()) {
        result.add("/bin/bash");
      } else {
        result.add("tools/docker/run.sh");
      }
      result.add(getExecutable());
      for (String option : options) {
        result.add(option);
      }
      for (Map.Entry<String, String> entry : systemProperties.entrySet()) {
        StringBuilder builder = new StringBuilder("-D");
        builder.append(entry.getKey());
        builder.append("=");
        builder.append(entry.getValue());
        result.add(builder.toString());
      }
      if (!classpaths.isEmpty()) {
        result.add("-cp");
        result.add(Strings.join(classpaths, ":"));
      }
      if (!bootClassPaths.isEmpty()) {
        result.add("-Xbootclasspath:" + String.join(":", bootClassPaths));
      }
      if (mainClass != null) {
        result.add(mainClass);
      }
      for (String argument : programArguments) {
        result.add(argument);
      }
      return result;
    }

    public ProcessBuilder asProcessBuilder() {
      return new ProcessBuilder(command());
    }

    public String build() {
      return String.join(" ", command());
    }

    protected abstract String getExecutable();
  }

  public static class ArtCommandBuilder extends CommandBuilder {

    private DexVm version;

    public ArtCommandBuilder() {
    }

    public ArtCommandBuilder(DexVm version) {
      assert ART_BINARY_VERSIONS.containsKey(version);
      this.version = version;
    }

    @Override
    protected String getExecutable() {
      return version != null ? getArtBinary(version) : getArtBinary();
    }
  }

  public static class DXCommandBuilder extends CommandBuilder {

    public DXCommandBuilder() {
      appendProgramArgument("--dex");
    }

    @Override
    protected String getExecutable() {
      return DX;
    }
  }

  private static class StreamReader implements Runnable {

    private InputStream stream;
    private String result;

    public StreamReader(InputStream stream) {
      this.stream = stream;
    }

    public String getResult() {
      return result;
    }

    @Override
    public void run() {
      try {
        result = CharStreams.toString(new InputStreamReader(stream, StandardCharsets.UTF_8));
        stream.close();
      } catch (IOException e) {
        result = "Failed reading result for stream " + stream;
      }
    }
  }

  private static final String TOOLS = "tools";
  private static final Map<DexVm, String> ART_DIRS =
      ImmutableMap.of(
          DexVm.ART_DEFAULT, "art",
          DexVm.ART_7_0_0, "art-7.0.0",
          DexVm.ART_6_0_1, "art-6.0.1",
          DexVm.ART_5_1_1, "art-5.1.1",
          DexVm.ART_4_4_4, "dalvik");
  private static final Map<DexVm, String> ART_BINARY_VERSIONS =
      ImmutableMap.of(
          DexVm.ART_DEFAULT, "bin/art",
          DexVm.ART_7_0_0, "bin/art",
          DexVm.ART_6_0_1, "bin/art",
          DexVm.ART_5_1_1, "bin/art",
          DexVm.ART_4_4_4, "bin/dalvik");

  private static final Map<DexVm, String> ART_BINARY_VERSIONS_X64 =
      ImmutableMap.of(
          DexVm.ART_DEFAULT, "bin/art",
          DexVm.ART_7_0_0, "bin/art",
          DexVm.ART_6_0_1, "bin/art");
  private static final List<String> ART_BOOT_LIBS =
      ImmutableList.of(
          "core-libart-hostdex.jar",
          "core-oj-hostdex.jar",
          "apache-xml-hostdex.jar"
      );

  private static final String LIB_PATH = TOOLS + "/linux/art/lib";
  private static final String DX = TOOLS + "/linux/dx/bin/dx";
  private static final String DEX2OAT = TOOLS + "/linux/art/bin/dex2oat";
  private static final String ANGLER_DIR = TOOLS + "/linux/art/product/angler";
  private static final String ANGLER_BOOT_IMAGE = ANGLER_DIR + "/system/framework/boot.art";

  public static String getArtDir(DexVm version) {
    String dir = ART_DIRS.get(version);
    if (dir == null) {
      throw new IllegalStateException("Does not support dex vm: " + version);
    }
    if (isLinux() || isMac()) {
      // The Linux version is used on Mac, where it is run in a Docker container.
      return TOOLS + "/linux/" + dir;
    }
    fail("Unsupported platform, we currently only support mac and linux: " + getPlatform());
    return ""; //never here
  }

  public static String getArtBinary(DexVm version) {
    String binary = ART_BINARY_VERSIONS.get(version);
    if (binary == null) {
      throw new IllegalStateException("Does not support running with dex vm: " + version);
    }
    return getArtDir(version) + "/" + binary;
  }

  public static String getDefaultAndroidJar() {
    return getAndroidJar(Constants.DEFAULT_ANDROID_API);
  }

  public static String getAndroidJar(int minSdkVersion) {
    return String.format(
        ANDROID_JAR_PATTERN,
        minSdkVersion == Constants.DEFAULT_ANDROID_API ? DEFAULT_MIN_SDK : minSdkVersion);
  }

  public static Path getJdwpTestsJarPath(int minSdk) {
    if (minSdk >= Constants.ANDROID_N_API) {
      return Paths.get("third_party", "jdwp-tests", "apache-harmony-jdwp-tests-host.jar");
    } else {
      return Paths.get(ToolHelper.BUILD_DIR, "libs", "jdwp-tests-preN.jar");
    }
  }

  // For non-Linux platforms create the temporary directory in the repository root to simplify
  // running Art in a docker container
  public static TemporaryFolder getTemporaryFolderForTest() {
    return new TemporaryFolder(ToolHelper.isLinux() ? null : Paths.get("build", "tmp").toFile());
  }

  public static String getArtBinary() {
    return getArtBinary(getDexVm());
  }

  public static Set<DexVm> getArtVersions() {
    String artVersion = System.getProperty("dex_vm");
    if (artVersion != null) {
      DexVm artVersionEnum = getDexVm();
      if (!ART_BINARY_VERSIONS.containsKey(artVersionEnum)) {
        throw new RuntimeException("Unsupported Art version " + artVersion);
      }
      return ImmutableSet.of(artVersionEnum);
    } else {
      if (isLinux()) {
        return ART_BINARY_VERSIONS.keySet();
      } else {
        return ART_BINARY_VERSIONS_X64.keySet();
      }
    }
  }

  public static List<String> getArtBootLibs() {
    String prefix = getArtDir(getDexVm()) + "/";
    List<String> result = new ArrayList<>();
    ART_BOOT_LIBS.stream().forEach(x -> result.add(prefix + "framework/" + x));
    return result;
  }

  // Returns if the passed in vm to use is the default.
  public static boolean isDefaultDexVm() {
    return getDexVm() == DexVm.ART_DEFAULT;
  }

  public static DexVm getDexVm() {
    String artVersion = System.getProperty("dex_vm");
    if (artVersion == null) {
      return DexVm.ART_DEFAULT;
    } else {
      DexVm artVersionEnum = DexVm.fromShortName(artVersion);
      if (artVersionEnum == null) {
        throw new RuntimeException("Unsupported Art version " + artVersion);
      } else {
        return artVersionEnum;
      }
    }
  }

  public static int getMinApiLevelForDexVm(DexVm dexVm) {
    switch (dexVm) {
      case ART_DEFAULT:
        return Constants.ANDROID_O_API;
      case ART_7_0_0:
        return Constants.ANDROID_N_API;
      default:
        return Constants.DEFAULT_ANDROID_API;
    }
  }

  private static String getPlatform() {
    return System.getProperty("os.name");
  }

  public static boolean isLinux() {
    return getPlatform().startsWith("Linux");
  }

  public static boolean isMac() {
    return getPlatform().startsWith("Mac");
  }

  public static boolean isWindows() {
    return getPlatform().startsWith("Windows");
  }

  public static boolean artSupported() {
    if (!isLinux() && !isMac()) {
      System.err.println("Testing on your platform is not fully supported. " +
          "Art does not work on on your platform.");
      return false;
    }
    return true;
  }

  public static Path getClassPathForTests() {
    return Paths.get(BUILD_DIR, "classes", "test");
  }

  public static Path getClassFileForTestClass(Class clazz) {
    List<String> parts = Lists.newArrayList(clazz.getCanonicalName().split("\\."));
    Class enclosing = clazz;
    while (enclosing.getEnclosingClass() != null) {
      parts.set(parts.size() - 2, parts.get(parts.size() - 2) + "$" + parts.get(parts.size() - 1));
      parts.remove(parts.size() - 1);
      enclosing = clazz.getEnclosingClass();
    }
    parts.set(parts.size() - 1, parts.get(parts.size() - 1) + ".class");
    return getClassPathForTests().resolve(
        Paths.get("", parts.toArray(new String[parts.size() - 1])));
  }

  public static DexApplication buildApplication(List<String> fileNames)
      throws IOException, ExecutionException {
    return new ApplicationReader(
            AndroidApp.fromProgramFiles(ListUtils.map(fileNames, Paths::get)),
            new InternalOptions(),
            new Timing("ToolHelper buildApplication"))
        .read();
  }

  public static R8Command.Builder prepareR8CommandBuilder(AndroidApp app) {
    return R8Command.builder(app);
  }

  public static AndroidApp runR8(AndroidApp app)
      throws ExecutionException, IOException, ProguardRuleParserException, CompilationException {
    return runR8(R8Command.builder(app).build());
  }

  public static AndroidApp runR8(AndroidApp app, Path output)
      throws ExecutionException, IOException, ProguardRuleParserException, CompilationException {
    assert output != null;
    return runR8(R8Command.builder(app).setOutputPath(output).build());
  }

  public static AndroidApp runR8(AndroidApp app, Consumer<InternalOptions> optionsConsumer)
      throws ProguardRuleParserException, ExecutionException, IOException, CompilationException {
    return runR8(R8Command.builder(app).build(), optionsConsumer);
  }

  public static AndroidApp runR8(R8Command command)
      throws ProguardRuleParserException, ExecutionException, IOException {
    return runR8(command, null);
  }

  public static AndroidApp runR8(R8Command command, Consumer<InternalOptions> optionsConsumer)
      throws ProguardRuleParserException, ExecutionException, IOException {
    return runR8WithFullResult(command, optionsConsumer).androidApp;
  }

  public static CompilationResult runR8WithFullResult(
        R8Command command, Consumer<InternalOptions> optionsConsumer)
        throws ProguardRuleParserException, ExecutionException, IOException {
   // TODO(zerny): Should we really be adding the android library in ToolHelper?
    AndroidApp app = command.getInputApp();
    if (app.getClassLibraryResources().isEmpty()) {
      app =
          AndroidApp.builder(app)
              .addLibraryFiles(Paths.get(getAndroidJar(command.getMinApiLevel())))
              .build();
    }
    InternalOptions options = command.getInternalOptions();
    // TODO(zerny): Should we really be setting this in ToolHelper?
    options.ignoreMissingClasses = true;
    if (optionsConsumer != null) {
      optionsConsumer.accept(options);
    }
    CompilationResult result = R8.runForTesting(app, options);
    R8.writeOutputs(command, options, result.androidApp);
    return result;
  }

  public static AndroidApp runR8(String fileName, String out)
      throws IOException, ProguardRuleParserException, ExecutionException, CompilationException {
    return runR8(Collections.singletonList(fileName), out);
  }

  public static AndroidApp runR8(Collection<String> fileNames, String out)
      throws IOException, ProguardRuleParserException, ExecutionException, CompilationException {
    return R8.run(
        R8Command.builder()
            .addProgramFiles(ListUtils.map(fileNames, Paths::get))
            .setOutputPath(Paths.get(out))
            .setIgnoreMissingClasses(true)
            .build());
  }

  public static AndroidApp runD8(AndroidApp app) throws CompilationException, IOException {
    return runD8(app, null);
  }

  public static AndroidApp runD8(AndroidApp app, Consumer<InternalOptions> optionsConsumer)
      throws CompilationException, IOException {
    return runD8(D8Command.builder(app).build(), optionsConsumer);
  }

  public static AndroidApp runD8(D8Command command) throws IOException {
    return runD8(command, null);
  }

  public static AndroidApp runD8(D8Command command, Consumer<InternalOptions> optionsConsumer)
      throws IOException {
    InternalOptions options = command.getInternalOptions();
    if (optionsConsumer != null) {
      optionsConsumer.accept(options);
    }
    AndroidApp result = D8.runForTesting(command.getInputApp(), options).androidApp;
    if (command.getOutputPath() != null) {
      result.write(command.getOutputPath(), command.getOutputMode());
    }
    return result;
  }

  public static AndroidApp runDexer(String fileName, String outDir, String... extraArgs)
      throws IOException {
    List<String> args = new ArrayList<>();
    Collections.addAll(args, extraArgs);
    Collections.addAll(args, "--output=" + outDir + "/classes.dex", fileName);
    int result = runDX(args.toArray(new String[args.size()])).exitCode;
    return result != 0 ? null : AndroidApp.fromProgramDirectory(Paths.get(outDir));
  }

  public static ProcessResult runDX(String[] args) throws IOException {
    Assume.assumeTrue(ToolHelper.artSupported());
    DXCommandBuilder builder = new DXCommandBuilder();
    for (String arg : args) {
      builder.appendProgramArgument(arg);
    }
    return runProcess(builder.asProcessBuilder());
  }

  public static ProcessResult runJava(Class clazz) throws Exception {
    String main = clazz.getCanonicalName();
    Path path = getClassPathForTests();
    return runJava(ImmutableList.of(path.toString()), main);
  }

  public static ProcessResult runJava(List<String> classpath, String mainClass) throws IOException {
    ProcessBuilder builder = new ProcessBuilder(
        getJavaExecutable(), "-cp", String.join(":", classpath), mainClass);
    return runProcess(builder);
  }

  public static ProcessResult forkD8(Path dir, String... args)
      throws IOException, InterruptedException {
    return forkJava(dir, D8.class, args);
  }

  public static ProcessResult forkR8(Path dir, String... args)
      throws IOException, InterruptedException {
    return forkJava(dir, R8.class, ImmutableList.builder()
        .addAll(Arrays.asList(args))
        .add("--ignore-missing-classes")
        .build()
        .toArray(new String[0]));
  }

  private static ProcessResult forkJava(Path dir, Class clazz, String... args)
      throws IOException, InterruptedException {
    List<String> command = new ImmutableList.Builder<String>()
        .add(getJavaExecutable())
        .add("-cp").add(System.getProperty("java.class.path"))
        .add(clazz.getCanonicalName())
        .addAll(Arrays.asList(args))
        .build();
    return runProcess(new ProcessBuilder(command).directory(dir.toFile()));
  }

  public static String getJavaExecutable() {
    return Paths.get(System.getProperty("java.home"), "bin", "java").toString();
  }

  public static String runArtNoVerificationErrors(String file, String mainClass)
      throws IOException {
    return runArtNoVerificationErrors(Collections.singletonList(file), mainClass, null);
  }

  public static String runArtNoVerificationErrors(List<String> files, String mainClass,
      Consumer<ArtCommandBuilder> extras)
      throws IOException {
    return runArtNoVerificationErrors(files, mainClass, extras, null);
  }

  public static String runArtNoVerificationErrors(List<String> files, String mainClass,
      Consumer<ArtCommandBuilder> extras,
      DexVm version)
      throws IOException {
    ArtCommandBuilder builder =
        version != null ? new ArtCommandBuilder(version) : new ArtCommandBuilder();
    files.forEach(builder::appendClasspath);
    builder.setMainClass(mainClass);
    if (extras != null) {
      extras.accept(builder);
    }
    return runArtNoVerificationErrors(builder);
  }

  public static String runArtNoVerificationErrors(ArtCommandBuilder builder) throws IOException {
    ProcessResult result = runArtProcess(builder);
    if (result.stderr.contains("Verification error")) {
      fail("Verification error: \n" + result.stderr);
    }
    return result.stdout;
  }

  private static ProcessResult runArtProcess(ArtCommandBuilder builder) throws IOException {
    Assume.assumeTrue(ToolHelper.artSupported());
    ProcessResult result = runProcess(builder.asProcessBuilder());
    if (result.exitCode != 0) {
      fail("Unexpected art failure: '" + result.stderr + "'\n" + result.stdout);
    }
    return result;
  }

  public static String runArt(ArtCommandBuilder builder) throws IOException {
    ProcessResult result = runArtProcess(builder);
    return result.stdout;
  }

  public static String checkArtOutputIdentical(String file1, String file2, String mainClass,
      DexVm version)
      throws IOException {
    return checkArtOutputIdentical(Collections.singletonList(file1),
        Collections.singletonList(file2), mainClass, null, version);
  }

  public static String checkArtOutputIdentical(List<String> files1, List<String> files2,
      String mainClass,
      Consumer<ArtCommandBuilder> extras,
      DexVm version)
      throws IOException {
    // Run art on original.
    for (String file : files1) {
      assertTrue("file1 " + file + " must exists", Files.exists(Paths.get(file)));
    }
    String output1 = ToolHelper.runArtNoVerificationErrors(files1, mainClass, extras, version);
    // Run art on R8 processed version.
    for (String file : files2) {
      assertTrue("file2 " + file + " must exists", Files.exists(Paths.get(file)));
    }
    String output2 = ToolHelper.runArtNoVerificationErrors(files2, mainClass, extras, version);
    assertEquals(output1, output2);
    return output1;
  }

  public static void runDex2Oat(Path file, Path outFile) throws IOException {
    Assume.assumeTrue(ToolHelper.artSupported());
    assert Files.exists(file);
    assert ByteStreams.toByteArray(Files.newInputStream(file)).length > 0;
    List<String> command = new ArrayList<>();
    command.add(DEX2OAT);
    command.add("--android-root=" + ANGLER_DIR);
    command.add("--runtime-arg");
    command.add("-Xnorelocate");
    command.add("--boot-image=" + ANGLER_BOOT_IMAGE);
    command.add("--dex-file=" + file.toAbsolutePath());
    command.add("--oat-file=" + outFile.toAbsolutePath());
    command.add("--instruction-set=arm64");
    command.add("--compiler-filter=interpret-only");
    ProcessBuilder builder = new ProcessBuilder(command);
    builder.environment().put("LD_LIBRARY_PATH", LIB_PATH);
    ProcessResult result = runProcess(builder);
    if (result.exitCode != 0) {
      fail("dex2oat failed, exit code " + result.exitCode + ", stderr:\n" + result.stderr);
    }
    if (result.stderr.contains("Verification error")) {
      fail("Verification error: \n" + result.stderr);
    }
  }

  public static class ProcessResult {

    public final int exitCode;
    public final String stdout;
    public final String stderr;

    ProcessResult(int exitCode, String stdout, String stderr) {
      this.exitCode = exitCode;
      this.stdout = stdout;
      this.stderr = stderr;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("EXIT CODE: ");
      builder.append(exitCode);
      builder.append("\n");
      builder.append("STDOUT: ");
      builder.append("\n");
      builder.append(stdout);
      builder.append("\n");
      builder.append("STDERR: ");
      builder.append("\n");
      builder.append(stderr);
      builder.append("\n");
      return builder.toString();
    }
  }

  public static ProcessResult runProcess(ProcessBuilder builder) throws IOException {
    System.out.println(String.join(" ", builder.command()));
    Process p = builder.start();
    // Drain stdout and stderr so that the process does not block. Read stdout and stderr
    // in parallel to make sure that neither buffer can get filled up which will cause the
    // C program to block in a call to write.
    StreamReader stdoutReader = new StreamReader(p.getInputStream());
    StreamReader stderrReader = new StreamReader(p.getErrorStream());
    Thread stdoutThread = new Thread(stdoutReader);
    Thread stderrThread = new Thread(stderrReader);
    stdoutThread.start();
    stderrThread.start();
    try {
      p.waitFor();
      stdoutThread.join();
      stderrThread.join();
    } catch (InterruptedException e) {
      throw new RuntimeException("Execution interrupted", e);
    }
    return new ProcessResult(p.exitValue(), stdoutReader.getResult(), stderrReader.getResult());
  }

  public static AndroidApp getApp(BaseCommand command) {
    return command.getInputApp();
  }
}
