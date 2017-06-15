// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.jasmin;

import com.android.tools.r8.dex.ApplicationReader;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.naming.MemberNaming.FieldSignature;
import com.android.tools.r8.naming.MemberNaming.MethodSignature;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.DescriptorUtils;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.StringUtils;
import com.android.tools.r8.utils.StringUtils.BraceType;
import com.android.tools.r8.utils.Timing;
import com.google.common.collect.ImmutableList;
import jasmin.ClassFile;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class JasminBuilder {

  public static class ClassBuilder {
    public final String name;
    private final List<String> methods = new ArrayList<>();
    private final List<String> fields = new ArrayList<>();
    private boolean makeInit = false;

    public ClassBuilder(String name) {
      this.name = name;
    }

    public MethodSignature addVirtualMethod(
        String name,
        List<String> argumentTypes,
        String returnType,
        String... lines) {
      makeInit = true;
      return addMethod("public", name, argumentTypes, returnType, lines);
    }

    public MethodSignature addPrivateVirtualMethod(
        String name,
        List<String> argumentTypes,
        String returnType,
        String... lines) {
      makeInit = true;
      return addMethod("private", name, argumentTypes, returnType, lines);
    }

    public MethodSignature addStaticMethod(
        String name,
        List<String> argumentTypes,
        String returnType,
        String... lines) {
      return addMethod("public static", name, argumentTypes, returnType, lines);
    }

    public MethodSignature addMainMethod(String... lines) {
      return addStaticMethod("main", ImmutableList.of("[Ljava/lang/String;"), "V", lines);
    }

    private MethodSignature addMethod(
        String access,
        String name,
        List<String> argumentTypes,
        String returnType,
        String... lines) {
      StringBuilder builder = new StringBuilder();
      builder.append(".method ").append(access).append(" ").append(name)
          .append(StringUtils.join(argumentTypes, "", BraceType.PARENS))
          .append(returnType).append("\n");
      for (String line : lines) {
        builder.append(line).append("\n");
      }
      builder.append(".end method\n");
      methods.add(builder.toString());

      String returnJavaType = DescriptorUtils.descriptorToJavaType(returnType);
      String[] argumentJavaTypes = new String[argumentTypes.size()];
      for (int i = 0; i < argumentTypes.size(); i++) {
        argumentJavaTypes[i] = DescriptorUtils.descriptorToJavaType(argumentTypes.get(i));
      }
      return new MethodSignature(name, returnJavaType, argumentJavaTypes);
    }

    public FieldSignature addStaticField(String name, String type, String value) {
      fields.add(
          ".field public static " + name + " " + type + (value != null ? (" = " + value) : ""));
      return new FieldSignature(name, type);
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append(".source ").append(name).append(".j\n");
      builder.append(".class public ").append(name).append("\n");
      builder.append(".super java/lang/Object\n");
      if (makeInit) {
        builder
            .append(".method public <init>()V\n")
            .append(".limit locals 1\n")
            .append(".limit stack 1\n")
            .append("  aload 0\n")
            .append("  invokespecial java/lang/Object/<init>()V\n")
            .append("  return\n")
            .append(".end method\n");
      }
      for (String field : fields) {
        builder.append(field).append("\n");
      }
      for (String method : methods) {
        builder.append(method).append("\n");
      }
      return builder.toString();
    }
  }

  private final List<ClassBuilder> classes = new ArrayList<>();

  public JasminBuilder() {}

  public ClassBuilder addClass(String name) {
    ClassBuilder builder = new ClassBuilder(name);
    classes.add(builder);
    return builder;
  }

  public ImmutableList<ClassBuilder> getClasses() {
    return ImmutableList.copyOf(classes);
  }

  private static byte[] compile(ClassBuilder builder) throws Exception {
    ClassFile file = new ClassFile();
    file.readJasmin(new StringReader(builder.toString()), builder.name, false);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    file.write(out);
    return out.toByteArray();
  }

  public AndroidApp build() throws Exception {
    AndroidApp.Builder builder = AndroidApp.builder();
    for (ClassBuilder clazz : classes) {
      builder.addClassProgramData(compile(clazz));
    }
    return builder.build();
  }

  public DexApplication read() throws Exception {
    return read(new InternalOptions());
  }

  public DexApplication read(InternalOptions options) throws Exception {
    DexItemFactory factory = new DexItemFactory();
    Timing timing = new Timing("JasminTest");
    return new ApplicationReader(build(), options, timing).read();
  }
}
