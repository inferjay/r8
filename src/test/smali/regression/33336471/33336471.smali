# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

.class public LTest;

.super Ljava/lang/Object;

# This method is not called by the test, and is only for reference of dex code failing on
# art from Android 5.1.1 with mixed int/float constants.
# When run dex2oat prints the warning:
#
# dex2oat W  7568  7571 art/compiler/dex/vreg_analysis.cc:367]
# void Test.intAndFloatZeroConstantsNotWorking() op at block 6 has both fp and core/ref uses for
# same def.
.method static intAndFloatZeroConstantsNotWorking()V
    .locals 8

    const-string        v6, "START"
    sget-object         v7, Ljava/lang/System;->out:Ljava/io/PrintStream;
    invoke-virtual      {v7, v6}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    const/high16        v0, 0x3f800000  # 1.0
    const/4             v1, 0x00  # 0 / 0.0
    const/4             v3, 2
    move                v4, v1

  :label_a
    invoke-virtual      {v7, v4}, Ljava/io/PrintStream;->println(I)V
    invoke-virtual      {v7, v3}, Ljava/io/PrintStream;->println(I)V
    if-ge               v4, v3, :label_b
    const-string        v6, "LOOP"
    invoke-virtual      {v7, v6}, Ljava/io/PrintStream;->println(Ljava/lang/Object;)V
    add-int/lit8        v4, v4, 0x01  # 1
    goto                :label_a

  :label_b
    const/4             v5, 0x01
    new-array           v5, v5, [F
    const/4             v3, 0x00
    aget                v4, v5, v3
    cmpl-float          v1, v4, v1
    if-nez              v1, :label_c
    cmpl-float          v0, v4, v0
    if-eqz              v0, :label_c
    const-string        v0, "DONE"
    invoke-virtual      {v7, v0}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V
    goto                :label_d

  :label_c
    const-string        v0, "FLOAT COMPARISON FAILED"
    invoke-virtual      {v7, v0}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

  :label_d
    return-void
.end method

.method static intAndFloatZeroConstants()V
    .locals 8

    const-string        v6, "START"
    sget-object         v7, Ljava/lang/System;->out:Ljava/io/PrintStream;
    invoke-virtual      {v7, v6}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    const/high16        v0, 0x3f800000  # 1.0
    const/4             v1, 0x00  # 0
    const/4             v2, 0x00  # 0.0
    const/4             v3, 2
    move                v4, v1

  :label_a
    invoke-virtual      {v7, v4}, Ljava/io/PrintStream;->println(I)V
    invoke-virtual      {v7, v3}, Ljava/io/PrintStream;->println(I)V
    if-ge               v4, v3, :label_b
    const-string        v6, "LOOP"
    invoke-virtual      {v7, v6}, Ljava/io/PrintStream;->println(Ljava/lang/Object;)V
    add-int/lit8        v4, v4, 0x01  # 1
    goto                :label_a

  :label_b
    const/4             v5, 0x01
    new-array           v5, v5, [F
    const/4             v3, 0x00
    aget                v4, v5, v3
    cmpl-float          v1, v4, v2
    if-nez              v1, :label_c
    cmpl-float          v0, v4, v0
    if-eqz              v0, :label_c
    const-string        v0, "DONE"
    invoke-virtual      {v7, v0}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V
    goto                :label_d

  :label_c
    const-string        v0, "FLOAT COMPARISON FAILED"
    invoke-virtual      {v7, v0}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

  :label_d
    return-void
.end method

# This method is not called by the test, and is only for reference of dex code failing on
# art from Android 5.1.1 with mixed long/double constants.
# This code does actually work, but dex2oat still prints the warning:
#
# dex2oat W  7568  7571 art/compiler/dex/vreg_analysis.cc:367]
# void Test.longAndDoubleZeroConstantsNotWorking() op at block 6 has both fp and core/ref uses
#for same def.
.method static longAndDoubleZeroConstantsNotWorking()V
    .locals 14

    const-string        v12, "START"
    sget-object         v13, Ljava/lang/System;->out:Ljava/io/PrintStream;
    invoke-virtual      {v13, v12}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    const-wide/high16   v0, 0x3f80000000000000L  # 1.0 0->0 1->2 2->4 3->6 4->8 5->10+11 6->12 7->13
    const-wide/16       v2, 0x00L  # 0 / 0.0
    const-wide/16       v6, 2
    move-wide           v8, v2

  :label_a
    invoke-virtual      {v13, v8, v9}, Ljava/io/PrintStream;->println(J)V
    invoke-virtual      {v13, v6, v7}, Ljava/io/PrintStream;->println(J)V
    cmp-long            v12, v8, v6
    if-gez              v12, :label_b
    const-string        v12, "LOOP"
    invoke-virtual      {v13, v12}, Ljava/io/PrintStream;->println(Ljava/lang/Object;)V
    const-wide/16       v10, 0x01
    add-long            v8, v8, v10
    goto                :label_a

  :label_b
    const/4             v10, 0x01
    new-array           v10, v10, [D
    const/4             v6, 0x00
    aget-wide           v8, v10, v6
    cmpl-double         v2, v8, v2
    if-nez              v2, :label_c
    cmpl-double         v0, v8, v0
    if-eqz              v0, :label_c
    const-string        v0, "DONE"
    invoke-virtual      {v13, v0}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V
    goto                :label_d

  :label_c
    const-string        v0, "FLOAT COMPARISON FAILED"
    invoke-virtual      {v13, v0}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

  :label_d
    return-void
.end method

.method static longAndDoubleZeroConstants()V
    .locals 14

    const-string        v12, "START"
    sget-object         v13, Ljava/lang/System;->out:Ljava/io/PrintStream;
    invoke-virtual      {v13, v12}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    const-wide/high16   v0, 0x3f80000000000000L
    const-wide/16       v2, 0x00L  # 0
    const-wide/16       v4, 0x00L  # 0.0
    const-wide/16       v6, 2
    move-wide           v8, v2

  :label_a
    invoke-virtual      {v13, v8, v9}, Ljava/io/PrintStream;->println(J)V
    invoke-virtual      {v13, v6, v7}, Ljava/io/PrintStream;->println(J)V
    cmp-long            v12, v8, v6
    if-gez              v12, :label_b
    const-string        v12, "LOOP"
    invoke-virtual      {v13, v12}, Ljava/io/PrintStream;->println(Ljava/lang/Object;)V
    const-wide/16       v10, 0x01
    add-long            v8, v8, v10
    goto                :label_a

  :label_b
    const/4             v10, 0x01
    new-array           v10, v10, [D
    const/4             v6, 0x00
    aget-wide           v8, v10, v6
    cmpl-double         v2, v8, v4
    if-nez              v2, :label_c
    cmpl-double         v0, v8, v0
    if-eqz              v0, :label_c
    const-string        v0, "DONE"
    invoke-virtual      {v13, v0}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V
    goto                :label_d

  :label_c
    const-string        v0, "DOUBLE COMPARISON FAILED"
    invoke-virtual      {v13, v0}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

  :label_d
    return-void
.end method

.method public static main([Ljava/lang/String;)V
    .locals 0

    invoke-static {}, LTest;->intAndFloatZeroConstants()V
    invoke-static {}, LTest;->longAndDoubleZeroConstants()V

    return-void
.end method
