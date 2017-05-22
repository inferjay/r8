// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.utils;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.junit.Test;

public class DescriptorUtilsTest {

  @Test
  public void toShorty() throws IOException {
    assertEquals("Z", DescriptorUtils.javaTypeToShorty("boolean"));
    assertEquals("B", DescriptorUtils.javaTypeToShorty("byte"));
    assertEquals("S", DescriptorUtils.javaTypeToShorty("short"));
    assertEquals("C", DescriptorUtils.javaTypeToShorty("char"));
    assertEquals("I", DescriptorUtils.javaTypeToShorty("int"));
    assertEquals("J", DescriptorUtils.javaTypeToShorty("long"));
    assertEquals("F", DescriptorUtils.javaTypeToShorty("float"));
    assertEquals("D", DescriptorUtils.javaTypeToShorty("double"));
    assertEquals("L", DescriptorUtils.javaTypeToShorty("int[]"));
    assertEquals("L", DescriptorUtils.javaTypeToShorty("int[][]"));
    assertEquals("L", DescriptorUtils.javaTypeToShorty("java.lang.Object"));
    assertEquals("L", DescriptorUtils.javaTypeToShorty("a.b.C"));
  }

  @Test
  public void toDescriptor() throws IOException {
    assertEquals("Z", DescriptorUtils.javaTypeToDescriptor("boolean"));
    assertEquals("B", DescriptorUtils.javaTypeToDescriptor("byte"));
    assertEquals("S", DescriptorUtils.javaTypeToDescriptor("short"));
    assertEquals("C", DescriptorUtils.javaTypeToDescriptor("char"));
    assertEquals("I", DescriptorUtils.javaTypeToDescriptor("int"));
    assertEquals("J", DescriptorUtils.javaTypeToDescriptor("long"));
    assertEquals("F", DescriptorUtils.javaTypeToDescriptor("float"));
    assertEquals("D", DescriptorUtils.javaTypeToDescriptor("double"));
    assertEquals("[I", DescriptorUtils.javaTypeToDescriptor("int[]"));
    assertEquals("[[I", DescriptorUtils.javaTypeToDescriptor("int[][]"));
    assertEquals("Ljava/lang/Object;", DescriptorUtils.javaTypeToDescriptor("java.lang.Object"));
    assertEquals("La/b/C;", DescriptorUtils.javaTypeToDescriptor("a.b.C"));
  }

  @Test
  public void toJavaType() throws IOException {
    assertEquals("boolean", DescriptorUtils.descriptorToJavaType("Z"));
    assertEquals("byte", DescriptorUtils.descriptorToJavaType("B"));
    assertEquals("short", DescriptorUtils.descriptorToJavaType("S"));
    assertEquals("char", DescriptorUtils.descriptorToJavaType("C"));
    assertEquals("int", DescriptorUtils.descriptorToJavaType("I"));
    assertEquals("long", DescriptorUtils.descriptorToJavaType("J"));
    assertEquals("float", DescriptorUtils.descriptorToJavaType("F"));
    assertEquals("double", DescriptorUtils.descriptorToJavaType("D"));
    assertEquals("int[]", DescriptorUtils.descriptorToJavaType("[I"));
    assertEquals("int[][]", DescriptorUtils.descriptorToJavaType("[[I"));
    assertEquals("java.lang.Object", DescriptorUtils.descriptorToJavaType("Ljava/lang/Object;"));
    assertEquals("a.b.C", DescriptorUtils.descriptorToJavaType("La/b/C;"));
  }
}
