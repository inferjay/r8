// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.naming;

import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.IndexedDexItem;
import com.android.tools.r8.naming.MemberNaming.FieldSignature;
import com.android.tools.r8.naming.MemberNaming.MethodSignature;
import com.android.tools.r8.naming.MemberNaming.Signature;
import com.android.tools.r8.utils.DescriptorUtils;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Map;
import java.util.function.Consumer;

public class ClassNameMapper {

  private final ImmutableMap<String, ClassNaming> classNameMappings;
  private ImmutableBiMap<String, String> nameMapping;

  private Hashtable<Signature, Signature> signatureMap = new Hashtable<>();

  ClassNameMapper(Map<String, ClassNaming> classNameMappings) {
    this.classNameMappings = ImmutableMap.copyOf(classNameMappings);
  }

  private Signature canonicalizeSignature(Signature signature) {
    Signature result = signatureMap.get(signature);
    if (result != null) {
      return result;
    }
    signatureMap.put(signature, signature);
    return signature;
  }

  public MethodSignature getRenamedMethodSignature(DexMethod method) {
    DexType[] parameters = method.proto.parameters.values;
    String[] parameterTypes = new String[parameters.length];
    for (int i = 0; i < parameters.length; i++) {
      parameterTypes[i] = deobfuscateType(parameters[i].toDescriptorString());
    }
    String returnType = deobfuscateType(method.proto.returnType.toDescriptorString());

    MethodSignature signature = new MethodSignature(method.name.toString(), returnType,
        parameterTypes);
    return (MethodSignature) canonicalizeSignature(signature);
  }

  public Signature getRenamedFieldSignature(DexField field) {
    String type = deobfuscateType(field.type.toDescriptorString());
    return canonicalizeSignature(new FieldSignature(field.name.toString(), type));
  }

  /**
   * Deobfuscate a class name.
   *
   * Returns the deobfuscated name if a mapping was found. Otherwise it returns the passed in name.
   */
  public String deobfuscateClassName(String name) {
    ClassNaming classNaming = classNameMappings.get(name);
    if (classNaming == null) {
      return name;
    }
    return classNaming.originalName;
  }

  private String deobfuscateType(String asString) {
    return DescriptorUtils.descriptorToJavaType(asString, this);
  }

  public ClassNaming getClassNaming(String name) {
    return classNameMappings.get(name);
  }

  public void write(Writer writer, boolean collapseRanges) throws IOException {
    for (ClassNaming naming : classNameMappings.values()) {
      naming.write(writer, collapseRanges);
    }
  }

  public void forAllClassNamings(Consumer<ClassNaming> consumer) {
    classNameMappings.values().forEach(consumer);
  }

  @Override
  public String toString() {
    try {
      StringWriter writer = new StringWriter();
      write(writer, false);
      return writer.toString();
    } catch (IOException e) {
      return e.toString();
    }
  }

  public BiMap<String, String> getObfuscatedToOriginalMapping() {
    if (nameMapping == null) {
      ImmutableBiMap.Builder<String, String> builder = ImmutableBiMap.builder();
      for (String name : classNameMappings.keySet()) {
        builder.put(name, classNameMappings.get(name).originalName);
      }
      nameMapping = builder.build();
    }
    return nameMapping;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof ClassNameMapper
        && classNameMappings.equals(((ClassNameMapper) o).classNameMappings);
  }

  @Override
  public int hashCode() {
    return 31 * classNameMappings.hashCode();
  }

  public String originalNameOf(IndexedDexItem item) {
    if (item instanceof DexField) {
      return lookupName(getRenamedFieldSignature((DexField) item), ((DexField) item).clazz);
    } else if (item instanceof DexMethod) {
      return lookupName(getRenamedMethodSignature((DexMethod) item), ((DexMethod) item).holder);
    } else if (item instanceof DexType) {
      return DescriptorUtils.descriptorToJavaType(((DexType) item).toDescriptorString(), this);
    } else {
      return item.toString();
    }
  }

  private String lookupName(Signature signature, DexType clazz) {
    String decoded = DescriptorUtils.descriptorToJavaType(clazz.descriptor.toString());
    ClassNaming classNaming = getClassNaming(decoded);
    if (classNaming == null) {
      return decoded + " " + signature.toString();
    }
    MemberNaming memberNaming = classNaming.lookup(signature);
    if (memberNaming == null) {
      return classNaming.originalName + " " + signature.toString();
    }
    return classNaming.originalName + " " + memberNaming.signature.toString();
  }

  public Signature originalSignatureOf(DexMethod method) {
    String decoded = DescriptorUtils
        .descriptorToJavaType(method.holder.descriptor.toString());
    MethodSignature memberSignature = getRenamedMethodSignature(method);
    ClassNaming classNaming = getClassNaming(decoded);
    if (classNaming == null) {
      return memberSignature;
    }
    MemberNaming memberNaming = classNaming.lookup(memberSignature);
    if (memberNaming == null) {
      return memberSignature;
    }
    return memberNaming.signature;
  }

  public String originalNameOf(DexType clazz) {
    return deobfuscateType(clazz.descriptor.toString());
  }
}
