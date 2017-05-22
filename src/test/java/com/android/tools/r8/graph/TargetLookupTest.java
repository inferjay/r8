// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.android.tools.r8.smali.SmaliTestBase;
import com.android.tools.r8.utils.InternalOptions;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class TargetLookupTest extends SmaliTestBase {

  @Test
  public void lookupDirect() {
    SmaliBuilder builder = new SmaliBuilder(DEFAULT_CLASS_NAME);

    builder.addDefaultConstructor();

    builder.addMethodRaw(
        "  .method private static x()I",
        "    .locals 1",
        "    const v0, 0",
        "    return v0",
        "  .end method"
    );

    // Instance method invoking static method using invoke-direct. This does not run on Art, but
    // results in an IncompatibleClassChangeError.
    builder.addMethodRaw(
        "  .method public y()I",
        "    .locals 1",
        "    invoke-direct       {p0}, " + builder.getCurrentClassDescriptor() + "->x()I",
        "    move-result         v0",
        "    return              v0",
        "  .end method"
    );

    builder.addMainMethod(
        2,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    new-instance        v1, LTest;",
        "    invoke-direct       {v1}, " + builder.getCurrentClassDescriptor() + "-><init>()V",
        "    :try_start",
        "    invoke-virtual      {v1}, " + builder.getCurrentClassDescriptor() + "->y()I",
        "    :try_end",
        "    const-string        v1, \"ERROR\"",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(Ljava/lang/String;)V",
        "    :return",
        "    return-void",
        "    .catch Ljava/lang/IncompatibleClassChangeError; {:try_start .. :try_end} :catch",
        "    :catch",
        "    const-string        v1, \"OK\"",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(Ljava/lang/String;)V",
        "    goto :return"
    );

    DexApplication application = buildApplication(builder);
    AppInfo appInfo = new AppInfo(application);
    DexEncodedMethod method =
        getMethod(application, DEFAULT_CLASS_NAME, "int", "x", ImmutableList.of());
    assertNull(appInfo.lookupVirtualTarget(method.method.holder, method.method));
    assertNull(appInfo.lookupDirectTarget(method.method));
    assertNotNull(appInfo.lookupStaticTarget(method.method));

    assertEquals("OK", runArt(application, new InternalOptions()));
  }

  @Test
  public void lookupDirectSuper() {
    SmaliBuilder builder = new SmaliBuilder("TestSuper");

    builder.addDefaultConstructor();

    builder.addMethodRaw(
        "  .method private static x()I",
        "    .locals 1",
        "    const               v0, 0",
        "    return              v0",
        "  .end method"
    );

    builder.addClass("Test", "TestSuper");

    builder.addDefaultConstructor();

    // Instance method invoking static method in superclass using invoke-direct. This does not run
    // on Art, but results in an IncompatibleClassChangeError.
    builder.addMethodRaw(
        "  .method public y()I",
        "    .locals 1",
        "    invoke-direct       {p0}, " + builder.getCurrentClassDescriptor() + "->x()I",
        "    move-result         v0",
        "    return              v0",
        "  .end method"
    );

    builder.addMainMethod(
        2,
        "    sget-object         v0, Ljava/lang/System;->out:Ljava/io/PrintStream;",
        "    new-instance        v1, LTest;",
        "    invoke-direct       {v1}, " + builder.getCurrentClassDescriptor() + "-><init>()V",
        "    :try_start",
        "    invoke-virtual      {v1}, " + builder.getCurrentClassDescriptor() + "->y()I",
        "    :try_end",
        "    const-string        v1, \"ERROR\"",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(Ljava/lang/String;)V",
        "    :return",
        "    return-void",
        "    .catch Ljava/lang/IncompatibleClassChangeError; {:try_start .. :try_end} :catch",
        "    :catch",
        "    const-string        v1, \"OK\"",
        "    invoke-virtual      { v0, v1 }, Ljava/io/PrintStream;->print(Ljava/lang/String;)V",
        "    goto :return"
    );

    DexApplication application = buildApplication(builder);
    AppInfo appInfo = new AppInfo(application);

    DexMethod methodXOnTestSuper =
        getMethod(application, "TestSuper", "int", "x", ImmutableList.of()).method;
    DexMethod methodYOnTest =
        getMethod(application, "Test", "int", "y", ImmutableList.of()).method;

    DexType classTestSuper = methodXOnTestSuper.getHolder();
    DexType classTest = methodYOnTest.getHolder();
    DexProto methodXProto = methodXOnTestSuper.proto;
    DexString methodXName = methodXOnTestSuper.name;
    DexMethod methodXOnTest =
        application.dexItemFactory.createMethod(classTest, methodXProto, methodXName);

    assertNull(appInfo.lookupVirtualTarget(classTestSuper, methodXOnTestSuper));
    assertNull(appInfo.lookupVirtualTarget(classTest, methodXOnTestSuper));
    assertNull(appInfo.lookupVirtualTarget(classTest, methodXOnTest));

    assertNull(appInfo.lookupDirectTarget(methodXOnTestSuper));
    assertNull(appInfo.lookupDirectTarget(methodXOnTest));

    assertNotNull(appInfo.lookupStaticTarget(methodXOnTestSuper));
    assertNotNull(appInfo.lookupStaticTarget(methodXOnTest));

    assertEquals("OK", runArt(application, new InternalOptions()));
  }
}



