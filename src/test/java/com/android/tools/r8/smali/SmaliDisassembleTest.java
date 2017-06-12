// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.smali;

import static org.junit.Assert.assertEquals;

import com.android.tools.r8.dex.ApplicationReader;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.utils.AndroidApp;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.Smali;
import com.android.tools.r8.utils.Timing;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

public class SmaliDisassembleTest extends SmaliTestBase {

  // Run the provided smali through R8 smali disassembler and expect the exact same output.
  void roundTripRawSmali(String smali) {
    try {
      DexApplication application =
          new ApplicationReader(
                  AndroidApp.fromDexProgramData(Smali.compile(smali)),
                  new InternalOptions(),
                  new Timing("SmaliTest"))
              .read();
      assertEquals(smali, application.smali(new InternalOptions()));
    } catch (IOException | RecognitionException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void simpleSmokeTest() {
    DexApplication application = singleMethodApplication(
        "int", Collections.singletonList("int"),
        4,
        "    const/4 v0, 1           ",
        "    const/16 v1, 2          ",
        "    const v2, 3             ",
        "    const-wide v3, -1       ",
        "    add-int/2addr v1, v0    ",
        "    mul-int/2addr v2, v1    ",
        "    div-int/2addr v3, v2    ",
        "    return v3\n             "
    );

    String expected =
        ".class public LTest;\n" +
            "\n" +
            ".super Ljava/lang/Object;\n" +
            "\n" +
            ".method public static method(I)I\n" +
            "    .registers 5\n" +
            "\n" +
            "    const/4             v0, 0x01  # 1\n" +
            "    const/16            v1, 0x0002  # 2\n" +
            "    const               v2, 0x00000003  # 3\n" +
            "    const-wide          v3, 0xffffffffffffffffL  # -1\n" +
            "    add-int/2addr       v1, v0\n" +
            "    mul-int/2addr       v2, v1\n" +
            "    div-int/2addr       v3, v2\n" +
            "    return              v3\n" +
            ".end method\n" +
            "\n" +
            "# End of class LTest;\n";

    assertEquals(expected, application.smali(new InternalOptions()));

    roundTripRawSmali(expected);
  }

  @Test
  public void sparseSwitchTest() {
    DexApplication application = singleMethodApplication(
        "int", Collections.singletonList("int"),
        0,
        "    sparse-switch v0, :sparse_switch_data",
        "    const/4 v0, 0x0",
        "    goto :return",
        "    :case_1",
        "    const/4 v0, 0x1",
        "    goto :return",
        "    :case_2",
        "    const/4 v0, 0x2",
        "    :return",
        "    return v0",
        "    :sparse_switch_data     ",
        "      .sparse-switch         ",
        "      0x1 -> :case_1         ",
        "      0x2 -> :case_2         ",
        "    .end sparse-switch     "

    );

    String expected =
        ".class public LTest;\n" +
            "\n" +
            ".super Ljava/lang/Object;\n" +
            "\n" +
            ".method public static method(I)I\n" +
            "    .registers 1\n" +
            "\n" +
            "    sparse-switch       v0, :label_10\n" +
            "    const/4             v0, 0x00  # 0\n" +
            "    goto                :label_8\n" +
            "  :label_5\n" +
            "    const/4             v0, 0x01  # 1\n" +
            "    goto                :label_8\n" +
            "  :label_7\n" +
            "    const/4             v0, 0x02  # 2\n" +
            "  :label_8\n" +
            "    return              v0\n" +
            "    nop\n" +
            "  :label_10\n" +
            "    .sparse-switch\n" +
            "      0x00000001 -> :label_5  # 1\n" +
            "      0x00000002 -> :label_7  # 2\n" +
            "    .end sparse-switch\n" +
            ".end method\n" +
            "\n" +
            "# End of class LTest;\n";

    assertEquals(expected, application.smali(new InternalOptions()));

    roundTripRawSmali(expected);
  }

  @Test
  public void packedSwitchTest() {
    DexApplication application = singleMethodApplication(
        "int", Collections.singletonList("int"),
        0,
        "    packed-switch v0, :packed_switch_data",
        "    const/4 v0, 0x0         ",
        "    goto :return            ",
        "    :case_1                 ",
        "    const/4 v0, 0x1         ",
        "    goto :return            ",
        "    :case_2                 ",
        "    const/4 v0, 0x2         ",
        "    :return                 ",
        "    return v0               ",
        "    :packed_switch_data     ",
        "      .packed-switch 0x1    ",
        "        :case_1             ",
        "        :case_2             ",
        "    .end packed-switch      "

    );

    String expected =
        ".class public LTest;\n" +
            "\n" +
            ".super Ljava/lang/Object;\n" +
            "\n" +
            ".method public static method(I)I\n" +
            "    .registers 1\n" +
            "\n" +
            "    packed-switch       v0, :label_10\n" +
            "    const/4             v0, 0x00  # 0\n" +
            "    goto                :label_8\n" +
            "  :label_5\n" +
            "    const/4             v0, 0x01  # 1\n" +
            "    goto                :label_8\n" +
            "  :label_7\n" +
            "    const/4             v0, 0x02  # 2\n" +
            "  :label_8\n" +
            "    return              v0\n" +
            "    nop\n" +
            "  :label_10\n" +
            "    .packed-switch 0x00000001  # 1\n" +
            "      :label_5\n" +
            "      :label_7\n" +
            "    .end packed-switch\n" +
            ".end method\n" +
            "\n" +
            "# End of class LTest;\n";

    assertEquals(expected, application.smali(new InternalOptions()));

    roundTripRawSmali(expected);
  }

  @Test
  public void fillArrayDataTest8Bit() {
    DexApplication application = singleMethodApplication(
        "int[]", ImmutableList.of(),
        2,
        "    const/4 v1, 3",
        "    new-array v0, v1, [I",
        "    fill-array-data v0, :array_data",
        "    return-object v0",
        "    :array_data",
        "    .array-data 1",
        "      1 2 255",
        "    .end array-data"
    );

    String expected =
        ".class public LTest;\n" +
            "\n" +
            ".super Ljava/lang/Object;\n" +
            "\n" +
            ".method public static method()[I\n" +
            "    .registers 2\n" +
            "\n" +
            "    const/4             v1, 0x03  # 3\n" +
            "    new-array           v0, v1, [I\n" +
            "    fill-array-data     v0, :label_8\n" +
            "    return-object       v0\n" +
            "    nop\n" +
            "  :label_8\n" +
            "    .array-data 0x1  # 1\n" +
            "      0x01  # 1\n" +
            "      0x02  # 2\n" +
            "      0xff  # 255\n" +
            "    .end array-data\n" +
            ".end method\n" +
            "\n" +
            "# End of class LTest;\n";

    assertEquals(expected, application.smali(new InternalOptions()));

    roundTripRawSmali(expected);
  }

  @Test
  public void fillArrayDataTest16Bit() {
    DexApplication application = singleMethodApplication(
        "int[]", ImmutableList.of(),
        2,
        "    const/4 v1, 3",
        "    new-array v0, v1, [I",
        "    fill-array-data v0, :array_data",
        "    return-object v0",
        "    :array_data",
        "    .array-data 2",
        "      1 2 65535",
        "    .end array-data"
    );

    String expected =
        ".class public LTest;\n" +
            "\n" +
            ".super Ljava/lang/Object;\n" +
            "\n" +
            ".method public static method()[I\n" +
            "    .registers 2\n" +
            "\n" +
            "    const/4             v1, 0x03  # 3\n" +
            "    new-array           v0, v1, [I\n" +
            "    fill-array-data     v0, :label_8\n" +
            "    return-object       v0\n" +
            "    nop\n" +
            "  :label_8\n" +
            "    .array-data 0x2  # 2\n" +
            "      0x0001  # 1\n" +
            "      0x0002  # 2\n" +
            "      0xffff  # 65535\n" +
            "    .end array-data\n" +
            ".end method\n" +
            "\n" +
            "# End of class LTest;\n";

    assertEquals(expected, application.smali(new InternalOptions()));

    roundTripRawSmali(expected);
  }

  @Test
  public void fillArrayDataTest32Bit() {
    DexApplication application = singleMethodApplication(
        "int[]", ImmutableList.of(),
        2,
        "    const/4 v1, 3",
        "    new-array v0, v1, [I",
        "    fill-array-data v0, :array_data",
        "    return-object v0",
        "    :array_data",
        "    .array-data 4",
        "      1 2 4294967295",
        "    .end array-data"
    );

    String expected =
        ".class public LTest;\n" +
            "\n" +
            ".super Ljava/lang/Object;\n" +
            "\n" +
            ".method public static method()[I\n" +
            "    .registers 2\n" +
            "\n" +
            "    const/4             v1, 0x03  # 3\n" +
            "    new-array           v0, v1, [I\n" +
            "    fill-array-data     v0, :label_8\n" +
            "    return-object       v0\n" +
            "    nop\n" +
            "  :label_8\n" +
            "    .array-data 0x4  # 4\n" +
            "      0x00000001  # 1\n" +
            "      0x00000002  # 2\n" +
            "      0xffffffff  # 4294967295\n" +
            "    .end array-data\n" +
            ".end method\n" +
            "\n" +
            "# End of class LTest;\n";

    assertEquals(expected, application.smali(new InternalOptions()));

    roundTripRawSmali(expected);
  }

  @Test
  public void fillArrayDataTest64Bit() {
    DexApplication application = singleMethodApplication(
        "int[]", ImmutableList.of(),
        2,
        "    const/4 v1, 3",
        "    new-array v0, v1, [I",
        "    fill-array-data v0, :array_data",
        "    return-object v0",
        "    :array_data",
        "    .array-data 8",
        "      1 2 -1",
        "    .end array-data"
    );

    String expected =
        ".class public LTest;\n" +
            "\n" +
            ".super Ljava/lang/Object;\n" +
            "\n" +
            ".method public static method()[I\n" +
            "    .registers 2\n" +
            "\n" +
            "    const/4             v1, 0x03  # 3\n" +
            "    new-array           v0, v1, [I\n" +
            "    fill-array-data     v0, :label_8\n" +
            "    return-object       v0\n" +
            "    nop\n" +
            "  :label_8\n" +
            "    .array-data 0x8  # 8\n" +
            "      0x0000000000000001  # 1\n" +
            "      0x0000000000000002  # 2\n" +
            "      0xffffffffffffffff  # -1\n" +
            "    .end array-data\n" +
            ".end method\n" +
            "\n" +
            "# End of class LTest;\n";

    assertEquals(expected, application.smali(new InternalOptions()));

    roundTripRawSmali(expected);
  }

  @Test
  public void interfaceClass() {
    SmaliBuilder builder = new SmaliBuilder();
    builder.addInterface("Test");
    builder.addAbstractMethod("int", "test", ImmutableList.of());
    DexApplication application = buildApplication(builder);
    assertEquals(1, Iterables.size(application.classes()));

    String expected =
        ".class public interface abstract LTest;\n" +
            "\n" +
            ".super Ljava/lang/Object;\n" +
            "\n" +
            ".method public abstract test()I\n" +
            ".end method\n" +
            "\n" +
            "# End of class LTest;\n";

    assertEquals(expected, application.smali(new InternalOptions()));

    roundTripRawSmali(expected);
  }

  @Test
  public void implementsInterface() {
    SmaliBuilder builder = new SmaliBuilder();
    builder.addClass("Test", "java.lang.Object", ImmutableList.of("java.util.List"));
    builder.addAbstractMethod("int", "test", ImmutableList.of());
    DexApplication application = buildApplication(builder);
    assertEquals(1, Iterables.size(application.classes()));

    String expected =
        ".class public LTest;\n" +
            "\n" +
            ".super Ljava/lang/Object;\n" +
            ".implements Ljava/util/List;\n" +
            "\n" +
            ".method public abstract test()I\n" +
            ".end method\n" +
            "\n" +
            "# End of class LTest;\n";

    assertEquals(expected, application.smali(new InternalOptions()));

    roundTripRawSmali(expected);
  }
}
