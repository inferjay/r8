// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.rewrite.staticvalues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.smali.SmaliTestBase;
import com.android.tools.r8.utils.DexInspector;
import com.android.tools.r8.utils.DexInspector.MethodSubject;
import com.android.tools.r8.utils.InternalOptions;
import org.junit.Test;

public class StaticValuesTest extends SmaliTestBase {

  @Test
  public void testAllTypes() {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    builder.addStaticField("booleanField", "Z");
    builder.addStaticField("byteField", "B");
    builder.addStaticField("shortField", "S");
    builder.addStaticField("intField", "I");
    builder.addStaticField("longField", "J");
    builder.addStaticField("floatField", "F");
    builder.addStaticField("doubleField", "D");
    builder.addStaticField("charField", "C");
    builder.addStaticField("stringField", "Ljava/lang/String;");

    builder.addStaticInitializer(
        2,
        "const               v0, 1",
        "sput-byte           v0, LTest;->booleanField:Z",
        "sput-byte           v0, LTest;->byteField:B",
        "const               v0, 2",
        "sput-short          v0, LTest;->shortField:S",
        "const               v0, 3",
        "sput                v0, LTest;->intField:I",
        "const-wide          v0, 4",
        "sput-wide           v0, LTest;->longField:J",
        "const               v0, 0x40a00000",  // 5.0.
        "sput                v0, LTest;->floatField:F",
        "const-wide          v0, 0x4018000000000000L",  // 6.0.
        "sput-wide           v0, LTest;->doubleField:D",
        "const               v0, 0x37",  // ASCII 7.
        "sput-char           v0, LTest;->charField:C",
        "const-string        v0, \"8\"",
        "sput-object         v0, LTest;->stringField:Ljava/lang/String;",
        "return-void"
    );
    builder.addMainMethod(
        3,
        "sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "sget-boolean        v1, LTest;->booleanField:Z",
        "invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(Z)V",
        "sget-byte           v1, LTest;->byteField:B",
        "invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(I)V",
        "sget-short          v1, LTest;->shortField:S",
        "invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(I)V",
        "sget                v1, LTest;->intField:I",
        "invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(I)V",
        "sget-wide           v1, LTest;->longField:J",
        "invoke-virtual      { v0, v1, v2 }, Ljava/io/PrintStream;->println(J)V",
        "sget                v1, LTest;->floatField:F",
        "invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(F)V",
        "sget-wide           v1, LTest;->doubleField:D",
        "invoke-virtual      { v0, v1, v2 }, Ljava/io/PrintStream;->println(D)V",
        "sget-char           v1, LTest;->charField:C",
        "invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(C)V",
        "sget-object         v1, LTest;->stringField:Ljava/lang/String;",
        "invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(Ljava/lang/String;)V",
        "return-void"
    );

    InternalOptions options = new InternalOptions();
    DexApplication originalApplication = buildApplication(builder, options);
    DexApplication processedApplication = processApplication(originalApplication, options);

    DexInspector inspector = new DexInspector(processedApplication);
    assertFalse(inspector.clazz("Test").clinit().isPresent());

    String result = runArt(processedApplication, options);

    assertEquals("true\n1\n2\n3\n4\n5.0\n6.0\n7\n8\n", result);
  }

  @Test
  public void getBeforePut() {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    builder.addStaticField("field1", "I", "1");
    builder.addStaticField("field2", "I", "2");

    builder.addStaticInitializer(
        1,
        "sget                v0, LTest;->field1:I",
        "sput                v0, LTest;->field2:I",
        "const               v0, 0",
        "sput                v0, LTest;->field1:I",
        "return-void"
    );
    builder.addMainMethod(
        2,
        "sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "sget                v1, LTest;->field1:I",
        "invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(I)V",
        "sget                v1, LTest;->field2:I",
        "invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(I)V",
        "return-void"
    );

    InternalOptions options = new InternalOptions();
    DexApplication originalApplication = buildApplication(builder, options);
    DexApplication processedApplication = processApplication(originalApplication, options);

    DexInspector inspector = new DexInspector(processedApplication);
    MethodSubject clinit = inspector.clazz("Test").clinit();
    // Nothing changed in the class initializer.
    assertEquals(5, clinit.getMethod().getCode().asDexCode().instructions.length);

    String result = runArt(processedApplication, options);

    assertEquals("0\n1\n", result);
  }

  @Test
  public void testNull() {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    builder.addStaticField("stringField", "Ljava/lang/String;", "Hello");
    builder.addStaticField("arrayField", "[I");
    builder.addStaticField("arrayField2", "[[[[I");

    builder.addStaticInitializer(
        2,
        "const               v0, 0",
        "sput-object         v0, LTest;->stringField:Ljava/lang/String;",
        "sput-object         v0, LTest;->arrayField:[I",
        "sput-object         v0, LTest;->arrayField2:[[[[I",
        "return-void"
    );
    builder.addMainMethod(
        3,
        "sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "sget-object         v1, LTest;->stringField:Ljava/lang/String;",
        "invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(Ljava/lang/String;)V",
        "sget-object         v1, LTest;->arrayField:[I",
        "invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(Ljava/lang/Object;)V",
        "sget-object         v1, LTest;->arrayField2:[[[[I",
        "invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(Ljava/lang/Object;)V",
        "return-void"
    );

    InternalOptions options = new InternalOptions();
    DexApplication originalApplication = buildApplication(builder, options);
    DexApplication processedApplication = processApplication(originalApplication, options);

    DexInspector inspector = new DexInspector(processedApplication);
    assertFalse(inspector.clazz("Test").clinit().isPresent());

    String result = runArt(processedApplication, options);

    assertEquals("null\nnull\nnull\n", result);
  }

  @Test
  public void testString() {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    builder.addStaticField("stringField1", "Ljava/lang/String;", "Hello");
    builder.addStaticField("stringField2", "Ljava/lang/String;", "Hello");
    builder.addStaticField("stringField3", "Ljava/lang/String;", "Hello");

    builder.addStaticInitializer(
        2,
        "const-string        v0, \"Value1\"",
        "sput-object         v0, LTest;->stringField1:Ljava/lang/String;",
        "const-string        v0, \"Value2\"",
        "sput-object         v0, LTest;->stringField2:Ljava/lang/String;",
        "sput-object         v0, LTest;->stringField3:Ljava/lang/String;",
        "return-void"
    );
    builder.addMainMethod(
        3,
        "sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "sget-object         v1, LTest;->stringField1:Ljava/lang/String;",
        "invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(Ljava/lang/String;)V",
        "sget-object         v1, LTest;->stringField2:Ljava/lang/String;",
        "invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(Ljava/lang/String;)V",
        "sget-object         v1, LTest;->stringField3:Ljava/lang/String;",
        "invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(Ljava/lang/String;)V",
        "return-void"
    );

    InternalOptions options = new InternalOptions();
    DexApplication originalApplication = buildApplication(builder, options);
    DexApplication processedApplication = processApplication(originalApplication, options);

    DexInspector inspector = new DexInspector(processedApplication);
    assertFalse(inspector.clazz("Test").clinit().isPresent());

    String result = runArt(processedApplication, options);

    assertEquals("Value1\nValue2\nValue2\n", result);
  }

  @Test
  public void testInitializationToOwnClassName() {
    String className = "org.example.Test";
    SmaliBuilder builder = new SmaliBuilder(className);

    builder.addStaticField("name1", "Ljava/lang/String;");
    builder.addStaticField("name2", "Ljava/lang/String;");
    builder.addStaticField("name3", "Ljava/lang/String;");
    builder.addStaticField("simpleName1", "Ljava/lang/String;");
    builder.addStaticField("simpleName2", "Ljava/lang/String;");
    builder.addStaticField("simpleName3", "Ljava/lang/String;");

    String descriptor = builder.getCurrentClassDescriptor();

    builder.addStaticInitializer(
        3,
        "const-class         v0, " + descriptor,
        "invoke-virtual      { v0 }, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;",
        "move-result-object  v0",
        "sput-object         v0, " + descriptor + "->simpleName1:Ljava/lang/String;",
        "const-class         v0, " + descriptor,
        "invoke-virtual      { v0 }, Ljava/lang/Class;->getName()Ljava/lang/String;",
        "move-result-object  v0",
        "sput-object         v0, " + descriptor + "->name1:Ljava/lang/String;",
        "const-class         v0, " + descriptor,
        "invoke-virtual      { v0 }, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;",
        "move-result-object  v1",
        "invoke-virtual      { v0 }, Ljava/lang/Class;->getName()Ljava/lang/String;",
        "move-result-object  v2",
        "sput-object         v1, " + descriptor + "->simpleName2:Ljava/lang/String;",
        "sput-object         v1, " + descriptor + "->simpleName3:Ljava/lang/String;",
        "sput-object         v2, " + descriptor + "->name2:Ljava/lang/String;",
        "sput-object         v2, " + descriptor + "->name3:Ljava/lang/String;",
        "return-void"
    );
    builder.addMainMethod(
        3,
        "sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "sget-object         v1, " + descriptor + "->simpleName1:Ljava/lang/String;",
        "invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(Ljava/lang/String;)V",
        "sget-object         v1, " + descriptor + "->name1:Ljava/lang/String;",
        "invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(Ljava/lang/String;)V",
        "sget-object         v1, " + descriptor + "->simpleName2:Ljava/lang/String;",
        "invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(Ljava/lang/String;)V",
        "sget-object         v1, " + descriptor + "->name2:Ljava/lang/String;",
        "invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(Ljava/lang/String;)V",
        "sget-object         v1, " + descriptor + "->simpleName3:Ljava/lang/String;",
        "invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(Ljava/lang/String;)V",
        "sget-object         v1, " + descriptor + "->name3:Ljava/lang/String;",
        "invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(Ljava/lang/String;)V",
        "return-void"
    );

    InternalOptions options = new InternalOptions();
    DexApplication originalApplication = buildApplication(builder, options);
    DexApplication processedApplication = processApplication(originalApplication, options);

    DexInspector inspector = new DexInspector(processedApplication);
    assertTrue(inspector.clazz(className).isPresent());
    assertFalse(inspector.clazz(className).clinit().isPresent());

    String result = runArt(processedApplication, options, className);

    assertEquals(
        "Test\n" + className + "\nTest\n" + className + "\nTest\n"  + className + "\n", result);
  }

  @Test
  public void testInitializationToOtherClassName() {
    String className = "org.example.Test";
    SmaliBuilder builder = new SmaliBuilder(className);

    builder.addStaticField("simpleName", "Ljava/lang/String;");
    builder.addStaticField("name", "Ljava/lang/String;");

    String descriptor = builder.getCurrentClassDescriptor();

    builder.addStaticInitializer(
        3,
        "const-class         v0, Lorg/example/Test2;",
        "invoke-virtual      { v0 }, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;",
        "move-result-object  v0",
        "sput-object         v0, " + descriptor + "->simpleName:Ljava/lang/String;",
        "const-class         v0, Lorg/example/Test2;",
        "invoke-virtual      { v0 }, Ljava/lang/Class;->getName()Ljava/lang/String;",
        "move-result-object  v0",
        "sput-object         v0, " + descriptor + "->name:Ljava/lang/String;",
        "return-void"
    );
    builder.addMainMethod(
        3,
        "sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "sget-object         v1, " + descriptor + "->simpleName:Ljava/lang/String;",
        "invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(Ljava/lang/String;)V",
        "sget-object         v1, " + descriptor + "->name:Ljava/lang/String;",
        "invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(Ljava/lang/String;)V",
        "return-void"
    );

    builder.addClass("org.example.Test2");

    InternalOptions options = new InternalOptions();
    DexApplication originalApplication = buildApplication(builder, options);
    DexApplication processedApplication = processApplication(originalApplication, options);

    DexInspector inspector = new DexInspector(processedApplication);
    assertTrue(inspector.clazz(className).isPresent());
    assertTrue(inspector.clazz(className).clinit().isPresent());

    String result = runArt(processedApplication, options, className);

    assertEquals("Test2\norg.example.Test2\n", result);
  }

  @Test
  public void fieldOnOtherClass() {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    builder.addStaticInitializer(
        1,
        "const               v0, 2",
        "sput                v0, LOther;->field:I",
        "return-void"
    );
    builder.addMainMethod(
        2,
        "sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "sget                v1, LOther;->field:I",
        "invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->println(I)V",
        "return-void"
    );

    builder.addClass("Other");
    builder.addStaticField("field", "I", "1");

    InternalOptions options = new InternalOptions();
    DexApplication originalApplication = buildApplication(builder, options);
    DexApplication processedApplication = processApplication(originalApplication, options);

    DexInspector inspector = new DexInspector(processedApplication);
    MethodSubject clinit = inspector.clazz("Test").clinit();
    // Nothing changed in the class initializer.
    assertEquals(3, clinit.getMethod().getCode().asDexCode().instructions.length);

    String result = runArt(processedApplication, options);

    assertEquals("2\n", result);
  }

}
