// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import static org.junit.Assert.assertEquals;

import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.ToolHelper.ArtCommandBuilder;
import com.android.tools.r8.ToolHelper.DexVm;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class ArtCommandBuilderTest {

  @Before
  public void setUp() {
    Assume.assumeTrue(ToolHelper.artSupported());
  }

  @Test
  public void noArguments() {
    ArtCommandBuilder builder = new ArtCommandBuilder();
    Assert.assertEquals("/bin/bash " + ToolHelper.getArtBinary(), builder.build());
  }

  @Test
  public void simple() {
    ToolHelper.ArtCommandBuilder builder = new ToolHelper.ArtCommandBuilder();
    builder.appendClasspath("xxx.dex").setMainClass("Test");
    assertEquals("/bin/bash " + ToolHelper.getArtBinary() + " -cp xxx.dex Test", builder.build());
  }

  @Test
  public void classpath() {
    ToolHelper.ArtCommandBuilder builder = new ToolHelper.ArtCommandBuilder();
    builder.appendClasspath("xxx.dex").appendClasspath("yyy.jar");
    assertEquals("/bin/bash " + ToolHelper.getArtBinary() + " -cp xxx.dex:yyy.jar",
                 builder.build());
  }

  @Test
  public void artOptions() {
    ToolHelper.ArtCommandBuilder builder = new ToolHelper.ArtCommandBuilder();
    builder.appendArtOption("-d").appendArtOption("--test");
    assertEquals("/bin/bash " + ToolHelper.getArtBinary() + " -d --test", builder.build());
  }

  @Test
  public void artSystemProperties() {
    ToolHelper.ArtCommandBuilder builder = new ToolHelper.ArtCommandBuilder();
    builder.appendArtSystemProperty("a.b.c", "1").appendArtSystemProperty("x.y.z", "2");
    assertEquals("/bin/bash " + ToolHelper.getArtBinary() + " -Da.b.c=1 -Dx.y.z=2",
                 builder.build());
  }

  @Test
  public void programOptions() {
    ToolHelper.ArtCommandBuilder builder = new ToolHelper.ArtCommandBuilder();
    builder.setMainClass("Test").appendProgramArgument("hello").appendProgramArgument("world");
    assertEquals("/bin/bash " + ToolHelper.getArtBinary() + " Test hello world", builder.build());
  }

  @Test
  public void allOfTheAbove() {
    ToolHelper.ArtCommandBuilder builder = new ToolHelper.ArtCommandBuilder();
    builder
        .appendArtOption("-d")
        .appendArtOption("--test")
        .appendArtSystemProperty("a.b.c", "1")
        .appendArtSystemProperty("x.y.z", "2")
        .appendClasspath("xxx.dex")
        .appendClasspath("yyy.jar")
        .setMainClass("Test")
        .appendProgramArgument("hello")
        .appendProgramArgument("world");
    assertEquals(
        "/bin/bash " + ToolHelper.getArtBinary()
            + " -d --test -Da.b.c=1 -Dx.y.z=2 -cp xxx.dex:yyy.jar Test hello world",
        builder.build());
  }

  @Test
  public void allOfTheAboveWithClasspathAndSystemPropertiesBeforeOptions() {
    ToolHelper.ArtCommandBuilder builder = new ToolHelper.ArtCommandBuilder();
    builder
        .appendClasspath("xxx.dex")
        .appendClasspath("yyy.jar")
        .appendArtSystemProperty("a.b.c", "1")
        .appendArtSystemProperty("x.y.z", "2")
        .appendArtOption("-d")
        .appendArtOption("--test")
        .setMainClass("Test")
        .appendProgramArgument("hello")
        .appendProgramArgument("world");
    assertEquals(
        "/bin/bash " + ToolHelper.getArtBinary()
            + " -d --test -Da.b.c=1 -Dx.y.z=2 -cp xxx.dex:yyy.jar Test hello world",
        builder.build());
  }

  @Test
  public void testVersion() {
    for (DexVm version : ToolHelper.getArtVersions()) {
      ToolHelper.ArtCommandBuilder builder = new ToolHelper.ArtCommandBuilder(version);
      builder.setMainClass("Test").appendProgramArgument("hello").appendProgramArgument("world");
      assertEquals("/bin/bash " + ToolHelper.getArtBinary(version)
          + " Test hello world", builder.build());
    }
  }
}
