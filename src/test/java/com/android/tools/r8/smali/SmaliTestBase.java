// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.smali;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.android.tools.r8.R8;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.dex.ApplicationReader;
import com.android.tools.r8.graph.AppInfo;
import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.ValueNumberGenerator;
import com.android.tools.r8.ir.conversion.IRConverter;
import com.android.tools.r8.naming.NamingLens;
import com.android.tools.r8.shaking.ProguardRuleParserException;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.DescriptorUtils;
import com.android.tools.r8.utils.DexInspector;
import com.android.tools.r8.utils.DexInspector.ClassSubject;
import com.android.tools.r8.utils.DexInspector.MethodSubject;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.OutputMode;
import com.android.tools.r8.utils.Smali;
import com.android.tools.r8.utils.Timing;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import org.antlr.runtime.RecognitionException;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public class SmaliTestBase {

  public static final String DEFAULT_CLASS_NAME = "Test";
  public static final String DEFAULT_MAIN_CLASS_NAME = DEFAULT_CLASS_NAME;
  public static final String DEFAULT_METHOD_NAME = "method";

  @Rule
  public TemporaryFolder temp = ToolHelper.getTemporaryFolderForTest();

  public static class MethodSignature {

    public final String clazz;
    public final String name;
    public final String returnType;
    public final List<String> parameterTypes;

    public MethodSignature(String clazz, String name, String returnType,
        List<String> parameterTypes) {
      this.clazz = clazz;
      this.name = name;
      this.returnType = returnType;
      this.parameterTypes = parameterTypes;
    }
  }

  public static class SmaliBuilder {

    abstract class Builder {
      String name;
      String superName;
      List<String> implementedInterfaces;
      List<String> source = new ArrayList<>();

      Builder(String name, String superName, List<String> implementedInterfaces) {
        this.name = name;
        this.superName = superName;
        this.implementedInterfaces = implementedInterfaces;
      }

      protected void appendSuper(StringBuilder builder) {
        builder.append(".super ");
        builder.append(DescriptorUtils.javaTypeToDescriptor(superName));
        builder.append("\n");
      }

      protected void appendImplementedInterfaces(StringBuilder builder) {
        for (String implementedInterface : implementedInterfaces) {
          builder.append(".implements ");
          builder.append(DescriptorUtils.javaTypeToDescriptor(implementedInterface));
          builder.append("\n");
        }
      }

      protected void writeSource(StringBuilder builder) {
        for (String sourceLine : source) {
          builder.append(sourceLine);
          builder.append("\n");
        }
      }
    }

    public class ClassBuilder extends Builder {

      ClassBuilder(String name, String superName, List<String> implementedInterfaces) {
        super(name, superName, implementedInterfaces);
      }

      public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(".class public ");
        builder.append(DescriptorUtils.javaTypeToDescriptor(name));
        builder.append("\n");
        appendSuper(builder);
        appendImplementedInterfaces(builder);
        builder.append("\n");
        writeSource(builder);
        return builder.toString();
      }
    }

    public class InterfaceBuilder extends Builder {

      InterfaceBuilder(String name, String superName) {
        super(name, superName, ImmutableList.of());
      }

      public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(".class public interface abstract ");
        builder.append(DescriptorUtils.javaTypeToDescriptor(name));
        builder.append("\n");
        appendSuper(builder);
        appendImplementedInterfaces(builder);
        builder.append("\n");
        writeSource(builder);
        return builder.toString();
      }
    }

    private String currentClassName;
    private final Map<String, Builder> classes = new HashMap<>();

    public SmaliBuilder() {
      // No default class.
    }

    public SmaliBuilder(String name) {
      addClass(name);
    }

    public SmaliBuilder(String name, String superName) {
      addClass(name, superName);
    }

    private List<String> getSource(String clazz) {
      return classes.get(clazz).source;
    }

    public String getCurrentClassName() {
      return currentClassName;
    }

    public String getCurrentClassDescriptor() {
      return DescriptorUtils.javaTypeToDescriptor(currentClassName);
    }

    public void addClass(String name) {
      addClass(name, "java.lang.Object");
    }

    public void addClass(String name, String superName) {
      addClass(name, superName, ImmutableList.of());
    }

    public void addClass(String name, String superName, List<String> implementedInterfaces) {
      assert !classes.containsKey(name);
      currentClassName = name;
      classes.put(name, new ClassBuilder(name, superName, implementedInterfaces));
    }

    public void addInterface(String name) {
      addInterface(name, "java.lang.Object");
    }

    public void addInterface(String name, String superName) {
      assert !classes.containsKey(name);
      currentClassName = name;
      classes.put(name, new InterfaceBuilder(name, superName));
    }

    public void addDefaultConstructor() {
      String superDescriptor =
          DescriptorUtils.javaTypeToDescriptor(classes.get(currentClassName).superName);
      addMethodRaw(
          "  .method public constructor <init>()V",
          "    .locals 0",
          "    invoke-direct {p0}, " + superDescriptor + "-><init>()V",
          "    return-void",
          "  .end method"
      );
    }

    public MethodSignature addStaticMethod(String returnType, String name, List<String> parameters,
        int locals, String... instructions) {
      StringBuilder builder = new StringBuilder();
      for (String instruction : instructions) {
        builder.append(instruction);
        builder.append("\n");
      }
      return addStaticMethod(returnType, name, parameters, locals, builder.toString());
    }

    public MethodSignature addStaticMethod(String returnType, String name, List<String> parameters,
        int locals, String code) {
      StringBuilder builder = new StringBuilder();
      builder.append(".method public static ");
      builder.append(name);
      builder.append("(");
      for (String parameter : parameters) {
        builder.append(DescriptorUtils.javaTypeToDescriptor(parameter));
      }
      builder.append(")");
      builder.append(DescriptorUtils.javaTypeToDescriptor(returnType));
      builder.append("\n");
      builder.append(".locals ");
      builder.append(locals);
      builder.append("\n\n");
      builder.append(code);
      builder.append(".end method");
      getSource(currentClassName).add(builder.toString());
      return new MethodSignature(currentClassName, name, returnType, parameters);
    }

    public MethodSignature addAbstractMethod(
        String returnType, String name, List<String> parameters) {
      StringBuilder builder = new StringBuilder();
      builder.append(".method public abstract ");
      builder.append(name);
      builder.append("(");
      for (String parameter : parameters) {
        builder.append(DescriptorUtils.javaTypeToDescriptor(parameter));
      }
      builder.append(")");
      builder.append(DescriptorUtils.javaTypeToDescriptor(returnType));
      builder.append("\n");
      builder.append(".end method");
      getSource(currentClassName).add(builder.toString());
      return new MethodSignature(currentClassName, name, returnType, parameters);
    }

    public MethodSignature addMainMethod(int locals, String... instructions) {
      return addStaticMethod(
          "void", "main", Collections.singletonList("java.lang.String[]"), locals, instructions);
    }

    public void addMethodRaw(String... source) {
      StringBuilder builder = new StringBuilder();
      for (String line : source) {
        builder.append(line);
        builder.append("\n");
      }
      getSource(currentClassName).add(builder.toString());
    }

    public List<String> build() {
      List<String> result = new ArrayList<>(classes.size());
      for (String clazz : classes.keySet()) {
        Builder classBuilder = classes.get(clazz);
        result.add(classBuilder.toString());
      }
      return result;
    }

    public byte[] compile() throws IOException, RecognitionException {
      return Smali.compile(build());
    }

    @Override
    public String toString() {
      return String.join("\n\n", build());
    }
  }

  public class TestApplication {

    public final DexApplication application;
    public final DexEncodedMethod method;
    public final IRCode code;
    public final List<IRCode> additionalCode;
    public final ValueNumberGenerator valueNumberGenerator;
    public final InternalOptions options;

    public TestApplication(
        DexApplication application,
        DexEncodedMethod method,
        IRCode code,
        ValueNumberGenerator valueNumberGenerator,
        InternalOptions options) {
      this(application, method, code, null, valueNumberGenerator, options);
    }

    public TestApplication(
        DexApplication application,
        DexEncodedMethod method,
        IRCode code,
        List<IRCode> additionalCode,
        ValueNumberGenerator valueNumberGenerator,
        InternalOptions options) {
      this.application = application;
      this.method = method;
      this.code = code;
      this.additionalCode = additionalCode;
      this.valueNumberGenerator = valueNumberGenerator;
      this.options = options;
    }

    public int countArgumentInstructions() {
      int count = 0;
      ListIterator<Instruction> iterator = code.blocks.get(0).listIterator();
      while (iterator.next().isArgument()) {
        count++;
      }
      return count;
    }

    public InstructionListIterator listIteratorAt(BasicBlock block, int index) {
      InstructionListIterator iterator = block.listIterator();
      for (int i = 0; i < index; i++) {
        iterator.next();
      }
      return iterator;
    }

    public String run() {
      AppInfo appInfo = new AppInfo(application);
      IRConverter converter = new IRConverter(application, appInfo, options);
      converter.replaceCodeForTesting(method, code);
      return runArt(application, options);
    }
  }

  protected DexApplication buildApplication(SmaliBuilder builder) {
    return buildApplication(builder, new InternalOptions());
  }

  protected DexApplication buildApplication(SmaliBuilder builder, InternalOptions options) {
    try {
      return buildApplication(AndroidApp.fromDexProgramData(builder.compile()), options);
    } catch (IOException | RecognitionException e) {
      throw new RuntimeException(e);
    }
  }

  protected DexApplication buildApplicationWithAndroidJar(
      SmaliBuilder builder, InternalOptions options) {
    try {
      AndroidApp input = AndroidApp.builder()
          .addDexProgramData(builder.compile())
          .addLibraryFiles(Paths.get(ToolHelper.getDefaultAndroidJar()))
          .build();
      return buildApplication(input, options);
    } catch (IOException | RecognitionException e) {
      throw new RuntimeException(e);
    }
  }

  protected DexApplication buildApplication(AndroidApp input, InternalOptions options) {
    try {
      options.itemFactory.resetSortedIndices();
      return new ApplicationReader(input, options, new Timing("SmaliTest")).read();
    } catch (IOException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  protected DexApplication processApplication(DexApplication application, InternalOptions options) {
    try {
      R8 r8 = new R8(options);
      return r8.optimize(application, new AppInfoWithSubtyping(application));
    } catch (IOException | ProguardRuleParserException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  protected DexClass getClass(DexApplication application, String className) {
    DexInspector inspector = new DexInspector(application);
    ClassSubject clazz = inspector.clazz(className);
    assertTrue(clazz.isPresent());
    return clazz.getDexClass();
  }

  protected DexClass getClass(DexApplication application, MethodSignature signature) {
    return getClass(application, signature.clazz);
  }

  protected DexEncodedMethod getMethod(DexApplication application, String className,
      String returnType, String methodName, List<String> parameters) {
    DexInspector inspector = new DexInspector(application);
    ClassSubject clazz = inspector.clazz(className);
    assertTrue(clazz.isPresent());
    MethodSubject method = clazz.method(returnType, methodName, parameters);
    assertTrue(method.isPresent());
    return method.getMethod();
  }

  protected DexEncodedMethod getMethod(DexApplication application, MethodSignature signature) {
    return getMethod(application,
        signature.clazz, signature.returnType, signature.name, signature.parameterTypes);
  }

  /**
   * Create an application with one method, and processed that application using R8.
   *
   * Returns the processed method for inspection.
   *
   * @param returnType the return type for the method
   * @param parameters the parameter types for the method
   * @param locals number of locals needed for the application
   * @param instructions instructions for the method
   * @return the processed method for inspection
   */
  public DexApplication singleMethodApplication(String returnType, List<String> parameters,
      int locals, String... instructions) {
    // Build a one class method.
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);
    builder.addStaticMethod(returnType, DEFAULT_METHOD_NAME, parameters, locals, instructions);

    // Read the one class method as an application.
    DexApplication application = buildApplication(builder);
    assertEquals(1, Iterables.size(application.classes()));
    return application;
  }

  /**
   * Create an application with one method, and processed that application using R8.
   *
   * Returns the processed method for inspection.
   *
   * @param returnType the return type for the method
   * @param parameters the parameter types for the method
   * @param locals number of locals needed for the application
   * @param instructions instructions for the method
   * @return the processed method for inspection
   */
  public DexEncodedMethod oneMethodApplication(String returnType, List<String> parameters,
      int locals, String... instructions) {
    InternalOptions options = new InternalOptions();

    // Build a one class application.
    DexApplication application = singleMethodApplication(
        returnType, parameters, locals, instructions);

    // Process the application with R8.
    DexApplication processdApplication = processApplication(application, options);
    assertEquals(1, Iterables.size(processdApplication.classes()));

    // Return the processed method for inspection.
    return getMethod(
        processdApplication, DEFAULT_CLASS_NAME, returnType, DEFAULT_METHOD_NAME, parameters);
  }

  public String runArt(DexApplication application, InternalOptions options) {
    try {
      AndroidApp app = writeDex(application, options);
      Path out = temp.getRoot().toPath().resolve("run-art-input.zip");
      // TODO(sgjesse): Pass in a unique temp directory for each run.
      app.writeToZip(out, OutputMode.Indexed);
      return ToolHelper.runArtNoVerificationErrors(out.toString(), DEFAULT_MAIN_CLASS_NAME);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void runDex2Oat(DexApplication application, InternalOptions options) {
    try {
      AndroidApp app = writeDex(application, options);
      Path dexOut = temp.getRoot().toPath().resolve("run-dex2oat-input.zip");
      Path oatFile = temp.getRoot().toPath().resolve("oat-file");
      app.writeToZip(dexOut, OutputMode.Indexed);
      ToolHelper.runDex2Oat(dexOut, oatFile);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public AndroidApp writeDex(DexApplication application, InternalOptions options) {
    AppInfo appInfo = new AppInfo(application);
    try {
      return R8.writeApplication(
          Executors.newSingleThreadExecutor(),
          application,
          appInfo,
          NamingLens.getIdentityLens(),
          null,
          null,
          options);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

}
