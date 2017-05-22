// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.smali;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.android.tools.r8.code.Const4;
import com.android.tools.r8.code.ConstString;
import com.android.tools.r8.code.ConstWide;
import com.android.tools.r8.code.ConstWideHigh16;
import com.android.tools.r8.code.DivInt;
import com.android.tools.r8.code.InvokeStatic;
import com.android.tools.r8.code.InvokeVirtual;
import com.android.tools.r8.code.MoveResult;
import com.android.tools.r8.code.MoveResultWide;
import com.android.tools.r8.code.Return;
import com.android.tools.r8.code.ReturnObject;
import com.android.tools.r8.code.ReturnVoid;
import com.android.tools.r8.code.ReturnWide;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexCode;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.utils.DexInspector;
import com.android.tools.r8.utils.DexInspector.ClassSubject;
import com.android.tools.r8.utils.DexInspector.MethodSubject;
import com.android.tools.r8.utils.InternalOptions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

public class OutlineTest extends SmaliTestBase {

  DexEncodedMethod getInvokedMethod(DexApplication application, InvokeStatic invoke) {
    DexInspector inspector = new DexInspector(application);
    ClassSubject clazz = inspector.clazz(invoke.getMethod().holder.toSourceString());
    assertTrue(clazz.isPresent());
    DexMethod invokedMethod = invoke.getMethod();
    invokedMethod.proto.returnType.toSourceString();
    MethodSubject method = clazz.method(
        invokedMethod.proto.returnType.toSourceString(),
        invokedMethod.name.toString(),
        Arrays.stream(invokedMethod.proto.parameters.values)
            .map(p -> p.toSourceString())
            .collect(Collectors.toList()));
    assertTrue(method.isPresent());
    return method.getMethod();
  }

  String firstOutlineMethodName(InternalOptions options) {
    StringBuilder builder = new StringBuilder(options.outline.className);
    builder.append('.');
    builder.append(options.outline.methodPrefix);
    builder.append("0");
    return builder.toString();
  }

  MethodSignature firstOutlineMethodSignature(
      String returnType, List<String> parameterTypes, InternalOptions options) {
    return new MethodSignature(
        options.outline.className, options.outline.methodPrefix + "0", returnType, parameterTypes);
  }

  boolean isOutlineMethodName(InternalOptions options, String qualifiedName) {
    StringBuilder builder = new StringBuilder(options.outline.className);
    builder.append('.');
    builder.append(options.outline.methodPrefix);
    return qualifiedName.indexOf(builder.toString()) == 0;
  }

  @Test
  public void a() {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    String returnType = "java.lang.String";
    List<String> parameters = Collections.singletonList("java.lang.StringBuilder");
    MethodSignature signature = builder.addStaticMethod(
        returnType,
        DEFAULT_METHOD_NAME,
        parameters,
        2,
        "    move                v0, p0",
        "    const-string        v1, \"Test\"",
        "    invoke-virtual      { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;",
        "    move-result-object  v0",
        "    return-object       v0"
    );

    builder.addMainMethod(
        2,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    new-instance        v1, Ljava/lang/StringBuilder;",
        "    invoke-direct       { v1 }, Ljava/lang/StringBuilder;-><init>()V",
        "    invoke-static       { v1 }, LTest;->method(Ljava/lang/StringBuilder;)Ljava/lang/String;",
        "    move-result-object  v1",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(Ljava/lang/String;)V",
        "    return-void"
    );

    for (int i = 2; i < 6; i++) {
      InternalOptions options = new InternalOptions();
      options.outline.threshold = 1;
      options.outline.minSize = i;
      options.outline.maxSize = i;

      DexApplication originalApplication = buildApplication(builder, options);
      DexApplication processedApplication = processApplication(originalApplication, options);
      assertEquals(2, Iterables.size(processedApplication.classes()));

      // Return the processed method for inspection.
      DexEncodedMethod method = getMethod(processedApplication, signature);

      DexCode code = method.getCode().asDexCode();
      assertTrue(code.instructions[0] instanceof ConstString);
      assertTrue(code.instructions[1] instanceof InvokeStatic);
      InvokeStatic invoke = (InvokeStatic) code.instructions[1];
      assertTrue(isOutlineMethodName(options, invoke.getMethod().qualifiedName()));

      // Run code and check result.
      String result = runArt(processedApplication, options);
      assertEquals("TestTestTestTest", result);
    }
  }

  @Test
  public void b() {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    String returnType = "java.lang.String";
    List<String> parameters = Collections.singletonList("java.lang.StringBuilder");
    MethodSignature signature = builder.addStaticMethod(
        returnType,
        DEFAULT_METHOD_NAME,
        parameters,
        2,
        "    move                v0, p0",
        "    const-string        v1, \"Test1\"",
        "    invoke-virtual      { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    const-string        v1, \"Test2\"",
        "    invoke-virtual      { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    const-string        v1, \"Test3\"",
        "    invoke-virtual      { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    const-string        v1, \"Test4\"",
        "    invoke-virtual      { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;",
        "    move-result-object  v0",
        "    return-object       v0"
    );

    builder.addMainMethod(
        2,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    new-instance        v1, Ljava/lang/StringBuilder;",
        "    invoke-direct       { v1 }, Ljava/lang/StringBuilder;-><init>()V",
        "    invoke-static       { v1 }, LTest;->method(Ljava/lang/StringBuilder;)Ljava/lang/String;",
        "    move-result-object  v1",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(Ljava/lang/String;)V",
        "    return-void"
    );

    for (int i = 2; i < 6; i++) {
      InternalOptions options = new InternalOptions();
      options.outline.threshold = 1;
      options.outline.minSize = i;
      options.outline.maxSize = i;

      DexApplication originalApplication = buildApplication(builder, options);
      DexApplication processedApplication = processApplication(originalApplication, options);
      assertEquals(2, Iterables.size(processedApplication.classes()));

      // Return the processed method for inspection.
      DexEncodedMethod method = getMethod(processedApplication, signature);

      DexCode code = method.getCode().asDexCode();

      // Up to 4 const instructions before the invoke of the outline.
      int firstOutlineInvoke = Math.min(i, 4);
      for (int j = 0; j < firstOutlineInvoke; j++) {
        assertTrue(code.instructions[j] instanceof ConstString);
      }
      assertTrue(code.instructions[firstOutlineInvoke] instanceof InvokeStatic);
      InvokeStatic invoke = (InvokeStatic) code.instructions[firstOutlineInvoke];
      assertTrue(isOutlineMethodName(options, invoke.getMethod().qualifiedName()));

      // Run code and check result.
      String result = runArt(processedApplication, options);
      assertEquals("Test1Test2Test3Test4", result);
    }
  }

  @Test
  public void c() {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    // Method with const instructions after the outline.
    String returnType = "int";
    List<String> parameters = Collections.singletonList("java.lang.StringBuilder");
    MethodSignature signature = builder.addStaticMethod(
        returnType,
        DEFAULT_METHOD_NAME,
        parameters,
        2,
        "    move                v0, p0",
        "    const-string        v1, \"Test\"",
        "    invoke-virtual      { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;",
        "    move-result-object  v0",
        "    const               v0, 0",
        "    const               v1, 1",
        "    add-int             v1, v1, v0",
        "    return              v1"
    );

    builder.addMainMethod(
        2,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    new-instance        v1, Ljava/lang/StringBuilder;",
        "    invoke-direct       { v1 }, Ljava/lang/StringBuilder;-><init>()V",
        "    invoke-static       { v1 }, LTest;->method(Ljava/lang/StringBuilder;)I",
        "    move-result         v1",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(I)V",
        "    return-void"
    );

    InternalOptions options = new InternalOptions();
    options.outline.threshold = 1;
    DexApplication originalApplication = buildApplication(builder, options);
    DexApplication processedApplication = processApplication(originalApplication, options);
    assertEquals(2, Iterables.size(processedApplication.classes()));

    // Return the processed method for inspection.
    DexEncodedMethod method = getMethod(processedApplication, signature);

    DexCode code = method.getCode().asDexCode();
    assertTrue(code.instructions[0] instanceof ConstString);
    assertTrue(code.instructions[1] instanceof InvokeStatic);
    InvokeStatic invoke = (InvokeStatic) code.instructions[1];
    assertTrue(isOutlineMethodName(options, invoke.getMethod().qualifiedName()));

    // Run code and check result.
    String result = runArt(processedApplication, options);
    assertEquals("1", result);
  }

  @Test
  public void d() {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    // Method with mixed use of arguments and locals.
    String returnType = "java.lang.String";
    List<String> parameters = ImmutableList.of(
        "java.lang.StringBuilder", "java.lang.String", "java.lang.String");
    MethodSignature signature = builder.addStaticMethod(
        returnType,
        DEFAULT_METHOD_NAME,
        parameters,
        2,
        "    invoke-virtual      { p0, p1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  p0",
        "    const-string        v0, \"Test1\"",
        "    invoke-virtual      { p0, v0 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  p0",
        "    invoke-virtual      { p0, p2 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  p0",
        "    const-string        v1, \"Test2\"",
        "    invoke-virtual      { p0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  p0",
        "    const-string        v1, \"Test3\"",
        "    invoke-virtual      { p0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  p0",
        "    invoke-virtual      { p0 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;",
        "    move-result-object  v1",
        "    return-object  v1"
    );

    builder.addMainMethod(
        4,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    new-instance        v1, Ljava/lang/StringBuilder;",
        "    invoke-direct       { v1 }, Ljava/lang/StringBuilder;-><init>()V",
        "    const-string        v2, \"TestX\"",
        "    const-string        v3, \"TestY\"",
        "    invoke-static       { v1, v2, v3 }, LTest;->method(Ljava/lang/StringBuilder;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
        "    move-result-object  v1",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(Ljava/lang/String;)V",
        "    return-void"
    );

    InternalOptions options = new InternalOptions();
    options.outline.threshold = 1;
    DexApplication originalApplication = buildApplication(builder, options);
    DexApplication processedApplication = processApplication(originalApplication, options);
    assertEquals(2, Iterables.size(processedApplication.classes()));

    // Return the processed method for inspection.
    DexEncodedMethod method = getMethod(processedApplication, signature);

    DexCode code = method.getCode().asDexCode();
    assertTrue(code.instructions[0] instanceof ConstString);
    assertTrue(code.instructions[1] instanceof ConstString);
    assertTrue(code.instructions[2] instanceof InvokeStatic);
    InvokeStatic invoke = (InvokeStatic) code.instructions[2];
    assertTrue(isOutlineMethodName(options, invoke.getMethod().qualifiedName()));

    // Run code and check result.
    String result = runArt(processedApplication, options);
    assertEquals("TestXTest1TestYTest2Test3", result);
  }

  @Test
  public void longArguments() throws Throwable {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    String returnType = "java.lang.String";
    List<String> parameters = Collections.singletonList("java.lang.StringBuilder");
    MethodSignature signature = builder.addStaticMethod(
        returnType,
        DEFAULT_METHOD_NAME,
        parameters,
        3,
        "    move                v0, p0",
        "    const-wide          v1, 0x7fffffff00000000L",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/lang/StringBuilder;->append(J)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/lang/StringBuilder;->append(J)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/lang/StringBuilder;->append(J)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/lang/StringBuilder;->append(J)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;",
        "    move-result-object  v0",
        "    return-object       v0"
    );

    builder.addMainMethod(
        2,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    new-instance        v1, Ljava/lang/StringBuilder;",
        "    invoke-direct       { v1 }, Ljava/lang/StringBuilder;-><init>()V",
        "    invoke-static       { v1 }, LTest;->method(Ljava/lang/StringBuilder;)Ljava/lang/String;",
        "    move-result-object  v1",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(Ljava/lang/String;)V",
        "    return-void"
    );

    for (int i = 2; i < 4; i++) {
      InternalOptions options = new InternalOptions();
      options.outline.threshold = 1;
      options.outline.minSize = i;
      options.outline.maxSize = i;

      DexApplication originalApplication = buildApplicationWithAndroidJar(builder, options);
      DexApplication processedApplication = processApplication(originalApplication, options);
      assertEquals(2, Iterables.size(processedApplication.classes()));

      // Return the processed method for inspection.
      DexEncodedMethod method = getMethod(processedApplication, signature);

      DexCode code = method.getCode().asDexCode();
      assertTrue(code.instructions[0] instanceof ConstWide);
      if (i < 3) {
        assertTrue(code.instructions[1] instanceof InvokeStatic);
        InvokeStatic invoke = (InvokeStatic) code.instructions[1];
        assertTrue(isOutlineMethodName(options, invoke.getMethod().qualifiedName()));
      } else {
        assertTrue(code.instructions[1] instanceof InvokeVirtual);
        assertTrue(code.instructions[2] instanceof InvokeVirtual);
        assertTrue(code.instructions[3] instanceof InvokeStatic);
        InvokeStatic invoke = (InvokeStatic) code.instructions[3];
        assertTrue(isOutlineMethodName(options, invoke.getMethod().qualifiedName()));
      }

      // Run code and check result.
      String result = runArt(processedApplication, options);
      StringBuilder resultBuilder = new StringBuilder();
      for (int j = 0; j < 4; j++) {
        resultBuilder.append(0x7fffffff00000000L);
      }
      assertEquals(resultBuilder.toString(), result);
    }
  }

  @Test
  public void doubleArguments() {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    String returnType = "java.lang.String";
    List<String> parameters = Collections.singletonList("java.lang.StringBuilder");
    MethodSignature signature = builder.addStaticMethod(
        returnType,
        DEFAULT_METHOD_NAME,
        parameters,
        3,
        "    move                v0, p0",
        "    const-wide          v1, 0x3ff0000000000000L",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/lang/StringBuilder;->append(D)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/lang/StringBuilder;->append(D)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/lang/StringBuilder;->append(D)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/lang/StringBuilder;->append(D)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;",
        "    move-result-object  v0",
        "    return-object       v0"
    );

    builder.addMainMethod(
        2,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    new-instance        v1, Ljava/lang/StringBuilder;",
        "    invoke-direct       { v1 }, Ljava/lang/StringBuilder;-><init>()V",
        "    invoke-static       { v1 }, LTest;->method(Ljava/lang/StringBuilder;)Ljava/lang/String;",
        "    move-result-object  v1",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(Ljava/lang/String;)V",
        "    return-void"
    );

    for (int i = 2; i < 4; i++) {
      InternalOptions options = new InternalOptions();
      options.outline.threshold = 1;
      options.outline.minSize = i;
      options.outline.maxSize = i;

      DexApplication originalApplication = buildApplicationWithAndroidJar(builder, options);
      DexApplication processedApplication = processApplication(originalApplication, options);
      assertEquals(2, Iterables.size(processedApplication.classes()));

      // Return the processed method for inspection.
      DexEncodedMethod method = getMethod(processedApplication, signature);

      DexCode code = method.getCode().asDexCode();
      assertTrue(code.instructions[0] instanceof ConstWideHigh16);
      if (i < 3) {
        assertTrue(code.instructions[1] instanceof InvokeStatic);
        InvokeStatic invoke = (InvokeStatic) code.instructions[1];
        assertTrue(isOutlineMethodName(options, invoke.getMethod().qualifiedName()));
      } else {
        assertTrue(code.instructions[1] instanceof InvokeVirtual);
        assertTrue(code.instructions[2] instanceof InvokeVirtual);
        assertTrue(code.instructions[3] instanceof InvokeStatic);
        InvokeStatic invoke = (InvokeStatic) code.instructions[3];
        assertTrue(isOutlineMethodName(options, invoke.getMethod().qualifiedName()));
      }

      // Run code and check result.
      String result = runArt(processedApplication, options);
      StringBuilder resultBuilder = new StringBuilder();
      for (int j = 0; j < 4; j++) {
        resultBuilder.append(1.0d);
      }
      assertEquals(resultBuilder.toString(), result);
    }
  }

  @Test
  public void invokeStatic() {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    String returnType = "void";
    List<String> parameters = ImmutableList.of("java.lang.StringBuilder", "int");
    MethodSignature signature = builder.addStaticMethod(
        returnType,
        DEFAULT_METHOD_NAME,
        parameters,
        1,
        "    invoke-virtual      { p0, p1 }, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { p0, p1 }, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    return-void"
    );

    MethodSignature mainSignature = builder.addMainMethod(
        2,
        "    new-instance        v0, Ljava/lang/StringBuilder;",
        "    invoke-direct       { v0 }, Ljava/lang/StringBuilder;-><init>()V",
        "    const/4             v1, 0x1",
        "    invoke-static       { v0, v1 }, LTest;->method(Ljava/lang/StringBuilder;I)V",
        "    const/4             v1, 0x2",
        "    invoke-static       { v0, v1 }, LTest;->method(Ljava/lang/StringBuilder;I)V",
        "    invoke-virtual      { v0 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;",
        "    move-result-object  v1",
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(Ljava/lang/String;)V",
        "    return-void"
    );

    for (int i = 2; i < 6; i++) {
      InternalOptions options = new InternalOptions();
      options.outline.threshold = 1;
      options.outline.minSize = i;
      options.outline.maxSize = i;

      DexApplication originalApplication = buildApplicationWithAndroidJar(builder, options);
      DexApplication processedApplication = processApplication(originalApplication, options);
      assertEquals(2, Iterables.size(processedApplication.classes()));

      // Return the processed main method for inspection.
      DexEncodedMethod mainMethod = getMethod(processedApplication, mainSignature);
      DexCode mainCode = mainMethod.getCode().asDexCode();

      if (i == 2 || i == 3) {
        assert mainCode.instructions.length == 10;
      } else if (i == 4) {
        assert mainCode.instructions.length == 9;
      } else {
        assert i == 5;
        assert mainCode.instructions.length == 7;
      }
      if (i == 2) {
        InvokeStatic invoke = (InvokeStatic) mainCode.instructions[4];
        assertTrue(isOutlineMethodName(options, invoke.getMethod().qualifiedName()));
      } else if (i == 3) {
        InvokeStatic invoke = (InvokeStatic) mainCode.instructions[1];
        assertTrue(isOutlineMethodName(options, invoke.getMethod().qualifiedName()));
      } else {
        assert i == 4 || i == 5;
        InvokeStatic invoke = (InvokeStatic) mainCode.instructions[2];
        assertTrue(isOutlineMethodName(options, invoke.getMethod().qualifiedName()));
      }

      // Run code and check result.
      String result = runArt(processedApplication, options);
      assertEquals("1122", result);
    }
  }

  @Test
  public void constructor() throws IOException, RecognitionException {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    MethodSignature signature1 = builder.addStaticMethod(
        "java.lang.String",
        "method1",
        Collections.emptyList(),
        3,
        "    new-instance        v0, Ljava/lang/StringBuilder;",
        "    invoke-direct       { v0 }, Ljava/lang/StringBuilder;-><init>()V",
        "    const-string        v1, \"Test1\"",
        "    invoke-virtual      { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;",
        "    move-result-object  v0",
        "    return-object       v0"
    );

    MethodSignature signature2 = builder.addStaticMethod(
        "java.lang.String",
        "method2",
        Collections.emptyList(),
        3,
        "    const/4             v1, 7",
        "    new-instance        v0, Ljava/lang/StringBuilder;",
        "    invoke-direct       { v0, v1 }, Ljava/lang/StringBuilder;-><init>(I)V",
        "    const-string        v1, \"Test2\"",
        "    invoke-virtual      { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;",
        "    move-result-object  v0",
        "    return-object       v0"
    );

    MethodSignature mainSignature = builder.addMainMethod(
        2,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    invoke-static       {}, LTest;->method1()Ljava/lang/String;",
        "    move-result-object  v1",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(Ljava/lang/String;)V",
        "    invoke-static       {}, LTest;->method2()Ljava/lang/String;",
        "    move-result-object  v1",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(Ljava/lang/String;)V",
        "    return-void"
    );

    InternalOptions options = new InternalOptions();
    options.outline.threshold = 1;
    options.outline.minSize = 7;
    options.outline.maxSize = 7;

    DexApplication originalApplication = buildApplicationWithAndroidJar(builder, options);
    DexApplication processedApplication = processApplication(originalApplication, options);
    assertEquals(2, Iterables.size(processedApplication.classes()));

    DexCode code1 = getMethod(processedApplication, signature1).getCode().asDexCode();
    assertEquals(4, code1.instructions.length);
    assertTrue(code1.instructions[1] instanceof InvokeStatic);
    InvokeStatic invoke1 = (InvokeStatic) code1.instructions[1];
    assertTrue(isOutlineMethodName(options, invoke1.getMethod().qualifiedName()));

    DexCode code2 = getMethod(processedApplication, signature2).getCode().asDexCode();
    assertEquals(5, code2.instructions.length);
    assertTrue(code2.instructions[2] instanceof InvokeStatic);
    InvokeStatic invoke2 = (InvokeStatic) code2.instructions[2];
    assertTrue(isOutlineMethodName(options, invoke1.getMethod().qualifiedName()));

    // Run code and check result.
    String result = runArt(processedApplication, options);
    assertEquals("Test1Test1Test1Test1Test2Test2Test2Test2", result);
  }

  @Test
  public void constructorDontSplitNewInstanceAndInit() {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    MethodSignature signature = builder.addStaticMethod(
        "java.lang.String",
        DEFAULT_METHOD_NAME,
        ImmutableList.of("java.lang.StringBuilder"),
        2,
        "    const-string        v0, \"Test1\"",
        "    invoke-virtual      { p0, v0 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  p0",
        "    invoke-virtual      { p0, v0 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  p0",
        "    new-instance        v1, Ljava/lang/StringBuilder;",
        "    invoke-direct       { v1 }, Ljava/lang/StringBuilder;-><init>()V",
        "    const-string        v0, \"Test2\"",
        "    invoke-virtual      { v1, v0 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  v1",
        "    invoke-virtual      { v1, v0 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  v1",
        "    invoke-virtual      { v1 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;",
        "    move-result-object  v0",
        "    return-object       v0"
    );

    MethodSignature mainSignature = builder.addMainMethod(
        2,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    new-instance        v1, Ljava/lang/StringBuilder;",
        "    invoke-direct       { v1 }, Ljava/lang/StringBuilder;-><init>()V",
        "    invoke-static       { v1 }, LTest;->method(Ljava/lang/StringBuilder;)Ljava/lang/String;",
        "    move-result-object  v1",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(Ljava/lang/String;)V",
        "    return-void"
    );

    for (int i = 2; i < 8; i++) {
      InternalOptions options = new InternalOptions();
      options.outline.threshold = 1;
      options.outline.minSize = i;
      options.outline.maxSize = i;

      DexApplication originalApplication = buildApplicationWithAndroidJar(builder, options);
      DexApplication processedApplication = processApplication(originalApplication, options);
      assertEquals(2, Iterables.size(processedApplication.classes()));

      DexCode code = getMethod(processedApplication, signature).getCode().asDexCode();
      InvokeStatic invoke;
      int outlineInstructionIndex;
      switch (i) {
        case 2:
        case 4:
          outlineInstructionIndex = 1;
          break;
        case 3:
          outlineInstructionIndex = 4;
          break;
        default:
          outlineInstructionIndex = 2;
      }
      invoke = (InvokeStatic) code.instructions[outlineInstructionIndex];
      assertTrue(isOutlineMethodName(options, invoke.getMethod().qualifiedName()));

      // Run code and check result.
      String result = runArt(processedApplication, options);
      assertEquals("Test2Test2", result);
    }
  }

  @Test
  public void outlineWithoutArguments() {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    MethodSignature signature1 = builder.addStaticMethod(
        "java.lang.String",
        DEFAULT_METHOD_NAME,
        Collections.emptyList(),
        1,
        "    new-instance        v0, Ljava/lang/StringBuilder;",
        "    invoke-direct       { v0 }, Ljava/lang/StringBuilder;-><init>()V",
        "    invoke-virtual      { v0 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;",
        "    move-result-object  v0",
        "    return-object       v0"
    );

    MethodSignature mainSignature = builder.addMainMethod(
        2,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    invoke-static       {}, LTest;->method()Ljava/lang/String;",
        "    move-result-object  v1",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(Ljava/lang/String;)V",
        "    return-void"
    );

    InternalOptions options = new InternalOptions();
    options.outline.threshold = 1;
    options.outline.minSize = 3;
    options.outline.maxSize = 3;

    DexApplication originalApplication = buildApplicationWithAndroidJar(builder, options);
    DexApplication processedApplication = processApplication(originalApplication, options);
    assertEquals(2, Iterables.size(processedApplication.classes()));

    DexCode code = getMethod(processedApplication, signature1).getCode().asDexCode();
    InvokeStatic invoke;
    assertTrue(code.instructions[0] instanceof InvokeStatic);
    invoke = (InvokeStatic) code.instructions[0];
    assertTrue(isOutlineMethodName(options, invoke.getMethod().qualifiedName()));

    // Run code and check result.
    String result = runArt(processedApplication, options);
    assertEquals("", result);
  }

  @Test
  public void outlineDifferentReturnType() {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    List<String> parameters = Collections.singletonList("java.lang.StringBuilder");

    // The naming of the methods in this test is important. The method name that don't use the
    // output from StringBuilder.toString must sort before the method name that does.
    String returnType1 = "void";
    MethodSignature signature1 = builder.addStaticMethod(
        returnType1,
        "method1",
        parameters,
        2,
        "    move                v0, p0",
        "    const-string        v1, \"Test\"",
        "    invoke-virtual      { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;",
        "    return-void"
    );

    String returnType2 = "java.lang.String";
    MethodSignature signature2 = builder.addStaticMethod(
        returnType2,
        "method2",
        parameters,
        2,
        "    move                v0, p0",
        "    const-string        v1, \"Test\"",
        "    invoke-virtual      { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;",
        "    move-result-object  v0",
        "    return-object       v0"
    );

    builder.addMainMethod(
        3,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    new-instance        v1, Ljava/lang/StringBuilder;",
        "    invoke-direct       { v1 }, Ljava/lang/StringBuilder;-><init>()V",
        "    invoke-static       { v1 }, LTest;->method1(Ljava/lang/StringBuilder;)V",
        "    invoke-static       { v1 }, LTest;->method2(Ljava/lang/StringBuilder;)Ljava/lang/String;",
        "    move-result-object  v2",
        "    invoke-virtual      { v0, v2 }, Ljava/io/PrintStream;->print(Ljava/lang/String;)V",
        "    return-void"
    );

    InternalOptions options = new InternalOptions();
    options.outline.threshold = 1;
    options.outline.minSize = 3;
    options.outline.maxSize = 3;

    DexApplication originalApplication = buildApplicationWithAndroidJar(builder, options);
    DexApplication processedApplication = processApplication(originalApplication, options);
    assertEquals(2, Iterables.size(processedApplication.classes()));

    // Check that three outlining methods was created.
    DexInspector inspector = new DexInspector(processedApplication);
    ClassSubject clazz = inspector.clazz(options.outline.className);
    assertTrue(clazz.isPresent());
    assertEquals(3, clazz.getDexClass().directMethods.length);
    // Collect the return types of the putlines for the body of method1 and method2.
    List<DexType> r = new ArrayList<>();
    for (int i = 0; i < clazz.getDexClass().directMethods.length; i++) {
      if (clazz.getDexClass().directMethods[i].getCode().asDexCode().instructions[0]
          instanceof InvokeVirtual) {
        r.add(clazz.getDexClass().directMethods[i].method.proto.returnType);
      }
    }
    assert r.size() == 2;
    DexType r1 = r.get(0);
    DexType r2 = r.get(1);
    DexItemFactory factory = processedApplication.dexItemFactory;
    assertTrue(r1 == factory.voidType && r2 == factory.stringType ||
        r1 == factory.stringType && r2 == factory.voidType);

    // Run the code.
    String result = runArt(processedApplication, options);
    assertEquals("TestTestTestTest", result);
  }

  @Test
  public void outlineMultipleTimes() {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    String returnType = "java.lang.String";
    List<String> parameters = Collections.singletonList("java.lang.StringBuilder");
    MethodSignature signature = builder.addStaticMethod(
        returnType,
        DEFAULT_METHOD_NAME,
        parameters,
        2,
        "    move                v0, p0",
        "    const-string        v1, \"Test\"",
        "    invoke-virtual      { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "    move-result-object  v0",
        "    invoke-virtual      { v0 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;",
        "    move-result-object  v0",
        "    return-object       v0"
    );

    builder.addMainMethod(
        2,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    new-instance        v1, Ljava/lang/StringBuilder;",
        "    invoke-direct       { v1 }, Ljava/lang/StringBuilder;-><init>()V",
        "    invoke-static       { v1 }, LTest;->method(Ljava/lang/StringBuilder;)Ljava/lang/String;",
        "    move-result-object  v1",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(Ljava/lang/String;)V",
        "    return-void"
    );

    InternalOptions options = new InternalOptions();
    options.outline.threshold = 1;
    options.outline.minSize = 3;
    options.outline.maxSize = 3;

    DexApplication originalApplication = buildApplicationWithAndroidJar(builder, options);
    DexApplication processedApplication = processApplication(originalApplication, options);
    assertEquals(2, Iterables.size(processedApplication.classes()));

    final int count = 10;
    // Process the application several times. Each time will outline the previous outline.
    for (int i = 0; i < count; i++) {
      // Build a new application with the Outliner class.
      DexApplication.Builder appBuilder =
          new DexApplication.Builder(processedApplication);
      originalApplication = appBuilder.build();
      processedApplication = processApplication(originalApplication, options);
      assertEquals(i + 3, Iterables.size(processedApplication.classes()));
    }

    // Process the application several times. No more outlining as threshold has been raised.
    options.outline.threshold = 2;
    for (int i = 0; i < count; i++) {
      // Build a new application with the Outliner class.
      DexApplication.Builder appBuilder =
          new DexApplication.Builder(processedApplication);
      originalApplication = appBuilder.build();
      processedApplication = processApplication(originalApplication, options);
      assertEquals(count - 1 + 3, Iterables.size(processedApplication.classes()));
    }

    // Run the application with several levels of outlining.
    String result = runArt(processedApplication, options);
    assertEquals("TestTestTestTest", result);
  }

  @Test
  public void outlineReturnLong() {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    MethodSignature signature = builder.addStaticMethod(
        "long",
        DEFAULT_METHOD_NAME,
        ImmutableList.of("int"),
        2,
        "    new-instance        v0, Ljava/util/GregorianCalendar;",
        "    invoke-direct       { v0 }, Ljava/util/GregorianCalendar;-><init>()V",
        "    invoke-virtual      { v0, p0, p0 }, Ljava/util/Calendar;->set(II)V",
        "    invoke-virtual      { v0, p0, p0 }, Ljava/util/Calendar;->set(II)V",
        "    invoke-virtual      { v0 }, Ljava/util/Calendar;->getTimeInMillis()J",
        "    move-result-wide    v0",
        "    return-wide         v0"
    );

    builder.addMainMethod(
        3,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    const/4             v1, 0",
        "    invoke-static       { v1 }, LTest;->method(I)J",
        "    move-result-wide    v1",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/io/PrintStream;->print(J)V",
        "    return-void"
    );

    InternalOptions options = new InternalOptions();
    options.outline.threshold = 1;
    options.outline.minSize = 5;
    options.outline.maxSize = 5;

    DexApplication originalApplication = buildApplicationWithAndroidJar(builder, options);
    DexApplication processedApplication = processApplication(originalApplication, options);
    assertEquals(2, Iterables.size(processedApplication.classes()));

    // Return the processed method for inspection.
    DexEncodedMethod method = getMethod(processedApplication, signature);
    // The calls to set, set and getTimeInMillis was outlined.
    DexCode code = method.getCode().asDexCode();
    assertEquals(3, code.instructions.length);
    assertTrue(code.instructions[0] instanceof InvokeStatic);
    assertTrue(code.instructions[1] instanceof MoveResultWide);
    assertTrue(code.instructions[2] instanceof ReturnWide);
    InvokeStatic invoke = (InvokeStatic) code.instructions[0];
    assertEquals(firstOutlineMethodName(options), invoke.getMethod().qualifiedName());

    // Run the code and expect a parsable long.
    String result = runArt(processedApplication, options);
    Long.parseLong(result);
  }

  @Test
  public void outlineArrayType() {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    builder.addStaticMethod(
        "void",
        "addToList",
        ImmutableList.of("java.util.List", "int[]"),
        0,
        "    invoke-interface    { p0, p1 }, Ljava/util/List;->add(Ljava/lang/Object;)Z",
        "    return-void "
    );

    MethodSignature signature = builder.addStaticMethod(
        "int[]",
        DEFAULT_METHOD_NAME,
        ImmutableList.of("int[]", "int[]"),
        1,
        "    new-instance        v0, Ljava/util/ArrayList;",
        "    invoke-direct       { v0 }, Ljava/util/ArrayList;-><init>()V",
        "    invoke-static       { v0, p0 }, LTest;->addToList(Ljava/util/List;[I)V",
        "    invoke-static       { v0, p1 }, LTest;->addToList(Ljava/util/List;[I)V",
        "    return-object       p0"
    );

    builder.addMainMethod(
        3,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    const/4             v1, 0",
        "    invoke-static       { v1, v1 }, LTest;->method([I[I)[I",
        "    move-result-object  v1",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(Ljava/lang/Object;)V",
        "    return-void"
    );

    InternalOptions options = new InternalOptions();
    options.outline.threshold = 1;
    options.outline.minSize = 4;
    options.outline.maxSize = 4;

    DexApplication originalApplication = buildApplicationWithAndroidJar(builder, options);
    DexApplication processedApplication = processApplication(originalApplication, options);
    assertEquals(2, Iterables.size(processedApplication.classes()));

    // Return the processed method for inspection.
    DexEncodedMethod method = getMethod(processedApplication, signature);
    DexCode code = method.getCode().asDexCode();
    assertEquals(2, code.instructions.length);
    assertTrue(code.instructions[0] instanceof InvokeStatic);
    assertTrue(code.instructions[1] instanceof ReturnObject);
    InvokeStatic invoke = (InvokeStatic) code.instructions[0];
    assertEquals(firstOutlineMethodName(options), invoke.getMethod().qualifiedName());

    // Run code and check result.
    String result = runArt(processedApplication, options);
    assertEquals("null", result);
  }

  @Test
  public void outlineArithmeticBinop() {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    builder.addStaticMethod(
        "void",
        "addToList",
        ImmutableList.of("java.util.List", "int[]"),
        0,
        "    invoke-interface    { p0, p1 }, Ljava/util/List;->add(Ljava/lang/Object;)Z",
        "    return-void "
    );

    MethodSignature signature1 = builder.addStaticMethod(
        "int",
        "method1",
        ImmutableList.of("int", "int"),
        1,
        "    add-int             v0, v1, v2",
        "    sub-int             v0, v0, v1",
        "    mul-int             v0, v2, v0",
        "    div-int             v0, v0, v1",
        "    return              v0"
    );

    MethodSignature signature2 = builder.addStaticMethod(
        "int",
        "method2",
        ImmutableList.of("int", "int"),
        1,
        "    add-int             v0, v2, v1",
        "    sub-int             v0, v0, v1",
        "    mul-int             v0, v0, v2",
        "    div-int             v0, v0, v1",
        "    return              v0"
    );

    builder.addMainMethod(
        4,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    const/4             v1, 1",
        "    const/4             v2, 2",
        "    invoke-static       { v1, v2 }, LTest;->method1(II)I",
        "    move-result         v4",
        "    invoke-virtual      { v0, v4 }, Ljava/io/PrintStream;->print(I)V",
        "    invoke-static       { v1, v2 }, LTest;->method2(II)I",
        "    move-result         v4",
        "    invoke-virtual      { v0, v4 }, Ljava/io/PrintStream;->print(I)V",
        "    return-void"
    );

    InternalOptions options = new InternalOptions();
    options.outline.threshold = 1;
    options.outline.minSize = 4;
    options.outline.maxSize = 4;

    DexApplication originalApplication = buildApplicationWithAndroidJar(builder, options);
    DexApplication processedApplication = processApplication(originalApplication, options);
    assertEquals(2, Iterables.size(processedApplication.classes()));

    // Return the processed method for inspection.
    DexEncodedMethod method1 = getMethod(processedApplication, signature1);
    DexCode code1 = method1.getCode().asDexCode();
    assertEquals(3, code1.instructions.length);
    assertTrue(code1.instructions[0] instanceof InvokeStatic);
    assertTrue(code1.instructions[1] instanceof MoveResult);
    assertTrue(code1.instructions[2] instanceof Return);
    InvokeStatic invoke1 = (InvokeStatic) code1.instructions[0];
    assertTrue(isOutlineMethodName(options, invoke1.getMethod().qualifiedName()));

    DexEncodedMethod method2 = getMethod(processedApplication, signature2);
    DexCode code2 = method2.getCode().asDexCode();
    assertTrue(code2.instructions[0] instanceof InvokeStatic);
    InvokeStatic invoke2 = (InvokeStatic) code2.instructions[0];
    assertEquals(invoke1.getMethod().qualifiedName(), invoke2.getMethod().qualifiedName());

    // Run code and check result.
    String result = runArt(processedApplication, options);
    assertEquals("44", result);
  }

  @Test
  public void outlineWithHandler() {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    builder.addStaticMethod(
        "void",
        "addToList",
        ImmutableList.of("java.util.List", "int[]"),
        0,
        "    invoke-interface    { p0, p1 }, Ljava/util/List;->add(Ljava/lang/Object;)Z",
        "    return-void "
    );

    MethodSignature signature = builder.addStaticMethod(
        "int",
        "method",
        ImmutableList.of("int", "int"),
        1,
        "    :try_start",
        // Throwing instruction to ensure the handler range does not get collapsed.
        "    div-int             v0, v1, v2",
        "    add-int             v0, v1, v2",
        "    sub-int             v0, v0, v1",
        "    mul-int             v0, v2, v0",
        "    div-int             v0, v0, v1",
        "    :try_end",
        "    :return",
        "    return              v0",
        "    .catch Ljava/lang/ArithmeticException; {:try_start .. :try_end} :catch",
        "    :catch",
        "    const/4             v0, -1",
        "    goto :return"
    );

    builder.addMainMethod(
        4,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    const/4             v1, 1",
        "    const/4             v2, 2",
        "    invoke-static       { v1, v2 }, LTest;->method(II)I",
        "    move-result         v4",
        "    invoke-virtual      { v0, v4 }, Ljava/io/PrintStream;->print(I)V",
        "    return-void"
    );

    InternalOptions options = new InternalOptions();
    options.outline.threshold = 1;
    options.outline.minSize = 3;  // Outline add, sub and mul.
    options.outline.maxSize = 3;

    DexApplication originalApplication = buildApplicationWithAndroidJar(builder, options);
    DexApplication processedApplication = processApplication(originalApplication, options);
    assertEquals(2, Iterables.size(processedApplication.classes()));

    // Return the processed method for inspection.
    DexEncodedMethod method = getMethod(processedApplication, signature);
    DexCode code = method.getCode().asDexCode();
    assertEquals(7, code.instructions.length);
    assertTrue(code.instructions[0] instanceof DivInt);
    assertTrue(code.instructions[1] instanceof InvokeStatic);
    assertTrue(code.instructions[2] instanceof MoveResult);
    assertTrue(code.instructions[3] instanceof DivInt);
    assertTrue(code.instructions[4] instanceof Return);
    assertTrue(code.instructions[5] instanceof Const4);
    assertTrue(code.instructions[6] instanceof Return);
    InvokeStatic invoke = (InvokeStatic) code.instructions[1];
    assertTrue(isOutlineMethodName(options, invoke.getMethod().qualifiedName()));

    // Run code and check result.
    String result = runArt(processedApplication, options);
    assertEquals("4", result);
  }

  @Test
  public void outlineUnusedOutValue() {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    // The result from neither the div-int is never used.
    MethodSignature signature = builder.addStaticMethod(
        "void",
        DEFAULT_METHOD_NAME,
        ImmutableList.of("int", "int"),
        1,
        "    div-int             v0, p0, p1",
        "    new-instance        v0, Ljava/lang/StringBuilder;",
        "    invoke-direct       { v0 }, Ljava/lang/StringBuilder;-><init>()V",
        "    return-void"
    );

    builder.addMainMethod(
        2,
        "    const               v0, 1",
        "    const               v1, 2",
        "    invoke-static       { v0, v1 }, LTest;->method(II)V",
        "    return-void"
    );

    InternalOptions options = new InternalOptions();
    options.outline.threshold = 1;
    options.outline.minSize = 3;
    options.outline.maxSize = 3;

    DexApplication originalApplication = buildApplicationWithAndroidJar(builder, options);
    DexApplication processedApplication = processApplication(originalApplication, options);
    assertEquals(2, Iterables.size(processedApplication.classes()));

    // Return the processed method for inspection.
    DexEncodedMethod method = getMethod(processedApplication, signature);
    DexCode code = method.getCode().asDexCode();
    assertEquals(2, code.instructions.length);
    assertTrue(code.instructions[0] instanceof InvokeStatic);
    assertTrue(code.instructions[1] instanceof ReturnVoid);
    InvokeStatic invoke = (InvokeStatic) code.instructions[0];
    assertEquals(firstOutlineMethodName(options), invoke.getMethod().qualifiedName());

    // Run code and check result.
    String result = runArt(processedApplication, options);
    assertEquals("", result);
  }

  @Test
  public void outlineUnusedNewInstanceOutValue() {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    // The result from the new-instance instructions are never used (<init> is not even called).
    MethodSignature signature = builder.addStaticMethod(
        "void",
        DEFAULT_METHOD_NAME,
        ImmutableList.of(),
        1,
        "    new-instance        v0, Ljava/lang/StringBuilder;",
        "    new-instance        v0, Ljava/lang/StringBuilder;",
        "    new-instance        v0, Ljava/lang/StringBuilder;",
        "    return-void"
    );

    builder.addMainMethod(
        0,
        "    invoke-static       { }, LTest;->method()V",
        "    return-void"
    );

    InternalOptions options = new InternalOptions();
    options.outline.threshold = 1;
    options.outline.minSize = 3;
    options.outline.maxSize = 3;

    DexApplication originalApplication = buildApplicationWithAndroidJar(builder, options);
    DexApplication processedApplication = processApplication(originalApplication, options);
    assertEquals(2, Iterables.size(processedApplication.classes()));

    // Return the processed method for inspection.
    DexEncodedMethod method = getMethod(processedApplication, signature);
    DexCode code = method.getCode().asDexCode();
    assertEquals(2, code.instructions.length);
    assertTrue(code.instructions[0] instanceof InvokeStatic);
    assertTrue(code.instructions[1] instanceof ReturnVoid);
    InvokeStatic invoke = (InvokeStatic) code.instructions[0];
    assertEquals(firstOutlineMethodName(options), invoke.getMethod().qualifiedName());

    // Run code and check result.
    String result = runArt(processedApplication, options);
    assertEquals("", result);
  }

  @Test
  public void regress33733666() {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    builder.addStaticMethod(
        "void",
        DEFAULT_METHOD_NAME,
        Collections.emptyList(),
        4,
        "    new-instance        v0, Ljava/util/Hashtable;",
        "    invoke-direct       { v0 }, Ljava/util/Hashtable;-><init>()V",
        "    const-string        v1, \"http://schemas.google.com/g/2005#home\"",
        "    new-instance        v2, Ljava/lang/Byte;",
        "    const/4             v3, 0x01  # 1",
        "    invoke-direct       { v2, v3 }, Ljava/lang/Byte;-><init>(B)V",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/util/Hashtable;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        "    const-string        v1, \" http://schemas.google.com/g/2005#work\"",
        "    new-instance        v2, Ljava/lang/Byte;",
        "    const/4             v3, 0x02  # 2",
        "    invoke-direct       { v2, v3 }, Ljava/lang/Byte;-><init>(B)V",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/util/Hashtable;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        "    const-string        v1, \"http://schemas.google.com/g/2005#other\"",
        "    new-instance        v2, Ljava/lang/Byte;",
        "    const/4             v3, 0x03  # 3",
        "    invoke-direct       { v2, v3 }, Ljava/lang/Byte;-><init>(B)V",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/util/Hashtable;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        "    const-string        v1, \"http://schemas.google.com/g/2005#primary\"",
        "    const/4             v2, 0x04  # 4",
        "    invoke-static       { v2 }, Ljava/lang/Byte;->valueOf(B)Ljava/lang/Byte;",
        "    move-result-object  v2",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/util/Hashtable;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        "    sput-object         v0, LA;->A:Ljava/util/Hashtable;",
        "    invoke-static       { v0 }, LA;->a(Ljava/util/Hashtable;)Ljava/util/Hashtable;",
        "    move-result-object  v0",
        "    sput-object         v0, LA;->B:Ljava/util/Hashtable;",
        "    new-instance        v0, Ljava/util/Hashtable;",
        "    invoke-direct       { v0 }, Ljava/util/Hashtable;-><init>()V",
        "    const-string        v1, \"http://schemas.google.com/g/2005#home\"",
        "    new-instance        v2, Ljava/lang/Byte;",
        "    const/4             v3, 0x02  # 2",
        "    invoke-direct       { v2, v3 }, Ljava/lang/Byte;-><init>(B)V",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/util/Hashtable;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        "    const-string        v1, \"http://schemas.google.com/g/2005#mobile\"",
        "    new-instance        v2, Ljava/lang/Byte;",
        "    const/4             v3, 0x01  # 1",
        "    invoke-direct       { v2, v3 }, Ljava/lang/Byte;-><init>(B)V",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/util/Hashtable;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        "    const-string        v1, \"http://schemas.google.com/g/2005#pager\"",
        "    new-instance        v2, Ljava/lang/Byte;",
        "    const/4             v3, 0x06  # 6",
        "    invoke-direct       { v2, v3 }, Ljava/lang/Byte;-><init>(B)V",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/util/Hashtable;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        "    const-string        v1, \"http://schemas.google.com/g/2005#work\"",
        "    new-instance        v2, Ljava/lang/Byte;",
        "    const/4             v3, 0x03  # 3",
        "    invoke-direct       { v2, v3 }, Ljava/lang/Byte;-><init>(B)V",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/util/Hashtable;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        "    const-string        v1, \"http://schemas.google.com/g/2005#home_fax\"",
        "    new-instance        v2, Ljava/lang/Byte;",
        "    const/4             v3, 0x05  # 5",
        "    invoke-direct       { v2, v3 }, Ljava/lang/Byte;-><init>(B)V",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/util/Hashtable;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        "    const-string        v1, \"http://schemas.google.com/g/2005#work_fax\"",
        "    new-instance        v2, Ljava/lang/Byte;",
        "    const/4             v3, 0x04  # 4",
        "    invoke-direct       { v2, v3 }, Ljava/lang/Byte;-><init>(B)V",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/util/Hashtable;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        "    const-string        v1, \"http://schemas.google.com/g/2005#other\"",
        "    new-instance        v2, Ljava/lang/Byte;",
        "    const/4             v3, 0x07  # 7",
        "    invoke-direct       { v2, v3 }, Ljava/lang/Byte;-><init>(B)V",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/util/Hashtable;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        "    sput-object         v0, LA;->C:Ljava/util/Hashtable;",
        "    invoke-static       { v0 }, LA;->a(Ljava/util/Hashtable;)Ljava/util/Hashtable;",
        "    move-result-object  v0",
        "    sput-object         v0, LA;->D:Ljava/util/Hashtable;",
        "    new-instance        v0, Ljava/util/Hashtable;",
        "    invoke-direct       { v0 }, Ljava/util/Hashtable;-><init>()V",
        "    const-string        v1, \"http://schemas.google.com/g/2005#home\"",
        "    new-instance        v2, Ljava/lang/Byte;",
        "    const/4             v3, 0x01  # 1",
        "    invoke-direct       { v2, v3 }, Ljava/lang/Byte;-><init>(B)V",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/util/Hashtable;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        "    const-string        v1, \"http://schemas.google.com/g/2005#work\"",
        "    new-instance        v2, Ljava/lang/Byte;",
        "    const/4             v3, 0x02  # 2",
        "    invoke-direct       { v2, v3 }, Ljava/lang/Byte;-><init>(B)V",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/util/Hashtable;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        "    const-string        v1, \"http://schemas.google.com/g/2005#other\"",
        "    new-instance        v2, Ljava/lang/Byte;",
        "    const/4             v3, 0x03  # 3",
        "    invoke-direct       { v2, v3 }, Ljava/lang/Byte;-><init>(B)V",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/util/Hashtable;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        "    sput-object         v0, LA;->E:Ljava/util/Hashtable;",
        "    invoke-static       { v0 }, LA;->a(Ljava/util/Hashtable;)Ljava/util/Hashtable;",
        "    move-result-object  v0",
        "    sput-object         v0, LA;->F:Ljava/util/Hashtable;",
        "    new-instance        v0, Ljava/util/Hashtable;",
        "    invoke-direct       { v0 }, Ljava/util/Hashtable;-><init>()V",
        "    const-string        v1, \"http://schemas.google.com/g/2005#home\"",
        "    new-instance        v2, Ljava/lang/Byte;",
        "    const/4             v3, 0x01  # 1",
        "    invoke-direct       { v2, v3 }, Ljava/lang/Byte;-><init>(B)V",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/util/Hashtable;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        "    const-string        v1, \"http://schemas.google.com/g/2005#work\"",
        "    new-instance        v2, Ljava/lang/Byte;",
        "    const/4             v3, 0x02  # 2",
        "    invoke-direct       { v2, v3 }, Ljava/lang/Byte;-><init>(B)V",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/util/Hashtable;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        "    const-string        v1, \"http://schemas.google.com/g/2005#other\"",
        "    new-instance        v2, Ljava/lang/Byte;",
        "    const/4             v3, 0x03  # 3",
        "    invoke-direct       { v2, v3 }, Ljava/lang/Byte;-><init>(B)V",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/util/Hashtable;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        "    sput-object         v0, LA;->G:Ljava/util/Hashtable;",
        "    invoke-static       { v0 }, LA;->a(Ljava/util/Hashtable;)Ljava/util/Hashtable;",
        "    move-result-object  v0",
        "    sput-object         v0, LA;->H:Ljava/util/Hashtable;",
        "    new-instance        v0, Ljava/util/Hashtable;",
        "    invoke-direct       { v0 }, Ljava/util/Hashtable;-><init>()V",
        "    const-string        v1, \"http://schemas.google.com/g/2005#work\"",
        "    new-instance        v2, Ljava/lang/Byte;",
        "    const/4             v3, 0x01  # 1",
        "    invoke-direct       { v2, v3 }, Ljava/lang/Byte;-><init>(B)V",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/util/Hashtable;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        "    const-string        v1, \"http://schemas.google.com/g/2005#other\"",
        "    new-instance        v2, Ljava/lang/Byte;",
        "    const/4             v3, 0x02  # 2",
        "    invoke-direct       { v2, v3 }, Ljava/lang/Byte;-><init>(B)V",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/util/Hashtable;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        "    sput-object         v0, LA;->I:Ljava/util/Hashtable;",
        "    invoke-static       { v0 }, LA;->a(Ljava/util/Hashtable;)Ljava/util/Hashtable;",
        "    move-result-object  v0",
        "    sput-object         v0, LA;->J:Ljava/util/Hashtable;",
        "    new-instance        v0, Ljava/util/Hashtable;",
        "    invoke-direct       { v0 }, Ljava/util/Hashtable;-><init>()V",
        "    const-string        v1, \"http://schemas.google.com/g/2005#AIM\"",
        "    new-instance        v2, Ljava/lang/Byte;",
        "    const/4             v3, 0x02  # 2",
        "    invoke-direct       { v2, v3 }, Ljava/lang/Byte;-><init>(B)V",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/util/Hashtable;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        "    const-string        v1, \"http://schemas.google.com/g/2005#MSN\"",
        "    new-instance        v2, Ljava/lang/Byte;",
        "    const/4             v3, 0x03  # 3",
        "    invoke-direct       { v2, v3 }, Ljava/lang/Byte;-><init>(B)V",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/util/Hashtable;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        "    const-string        v1, \"http://schemas.google.com/g/2005#YAHOO\"",
        "    new-instance        v2, Ljava/lang/Byte;",
        "    const/4             v3, 0x04  # 4",
        "    invoke-direct       { v2, v3 }, Ljava/lang/Byte;-><init>(B)V",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/util/Hashtable;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        "    const-string        v1, \"http://schemas.google.com/g/2005#SKYPE\"",
        "    new-instance        v2, Ljava/lang/Byte;",
        "    const/4             v3, 0x05  # 5",
        "    invoke-direct       { v2, v3 }, Ljava/lang/Byte;-><init>(B)V",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/util/Hashtable;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        "    const-string        v1, \"http://schemas.google.com/g/2005#QQ\"",
        "    new-instance        v2, Ljava/lang/Byte;",
        "    const/4             v3, 0x06  # 6",
        "    invoke-direct       { v2, v3 }, Ljava/lang/Byte;-><init>(B)V",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/util/Hashtable;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        "    const-string        v1, \"http://schemas.google.com/g/2005#GOOGLE_TALK\"",
        "    new-instance        v2, Ljava/lang/Byte;",
        "    const/4             v3, 0x07  # 7",
        "    invoke-direct       { v2, v3 }, Ljava/lang/Byte;-><init>(B)V",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/util/Hashtable;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        "    const-string        v1, \"http://schemas.google.com/g/2005#ICQ\"",
        "    new-instance        v2, Ljava/lang/Byte;",
        "    const/16            v3, 0x0008  # 8",
        "    invoke-direct       { v2, v3 }, Ljava/lang/Byte;-><init>(B)V",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/util/Hashtable;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        "    const-string        v1, \"http://schemas.google.com/g/2005#JABBER\"",
        "    new-instance        v2, Ljava/lang/Byte;",
        "    const/16            v3, 0x0009  # 9",
        "    invoke-direct       { v2, v3 }, Ljava/lang/Byte;-><init>(B)V",
        "    invoke-virtual      { v0, v1, v2 }, Ljava/util/Hashtable;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        "    sput-object         v0, LA;->K:Ljava/util/Hashtable;",
        "    invoke-static       { v0 }, LA;->a(Ljava/util/Hashtable;)Ljava/util/Hashtable;",
        "    move-result-object  v0",
        "    sput-object         v0, LA;->L:Ljava/util/Hashtable;",
        "    return-void"
    );

    InternalOptions options = new InternalOptions();
    options.outline.threshold = 2;

    DexApplication originalApplication = buildApplicationWithAndroidJar(builder, options);
    DexApplication processedApplication = processApplication(originalApplication, options);
    assertEquals(2, Iterables.size(processedApplication.classes()));

    // Verify the code.
    runDex2Oat(processedApplication, options);
  }
}
