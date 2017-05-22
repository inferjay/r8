# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

.class public LTest;

.super Ljava/lang/Object;

.method static addMinusOne(I)I
    .locals 1

    const/4 v0, -1
    add-int v0, p0, v0
    return v0
.end method

.method static addTwoConstants()I
    .locals 2

    const/4 v0, 1
    const/4 v1, 2
    add-int v0, v0, v1
    return v0
.end method

.method static addSameTwoConstants()I
    .locals 2

    const/4 v0, 1
    add-int v0, v0, v0
    return v0
.end method

.method static subMinusOne(I)I
    .locals 1

    const/4 v0, -1
    sub-int v0, p0, v0
    return v0
.end method

.method static subSameTwoConstants()I
    .locals 2

    const/4 v0, 1
    sub-int v0, v0, v0
    return v0
.end method

.method static subtractConstants()I
    .locals 9

    const/4 v0, 0

    const v1, 127  # Max 8-bit signed integer.
    const v2, -128  # Min 8-bit signed integer.
    const v3, 128  # Max 8-bit signed integer plus one.
    const v4, -129  # Min 8-bit signed integer minus one.

    const v5, 32767  # Max 16-bit signed integer.
    const v6, -32768  # Min 16-bit signed integer.
    const v7, 32768  # Max 16-bit signed integer plus one.
    const v8, -32769  # Min 16-bit signed integer minus one.

    sub-int v0, v0, v1
    sub-int v0, v0, v2
    sub-int v0, v0, v3
    sub-int v0, v0, v4
    sub-int v0, v0, v5
    sub-int v0, v0, v6
    sub-int v0, v0, v7
    sub-int v0, v0, v8
    sub-int v0, v1, v0
    sub-int v0, v2, v0
    sub-int v0, v3, v0
    sub-int v0, v4, v0
    sub-int v0, v5, v0
    sub-int v0, v6, v0
    sub-int v0, v7, v0
    sub-int v0, v8, v0

    return v0
.end method

.method static sixteenIntArgMethod(IIIIIIIIIIIIIIII)V
    .locals 0
    return-void
.end method

# Same code as subtractConstants, but try to force the register allocator to allocate registers for
# the arithmetic operations above 15.
.method static subtractConstants8bitRegisters()I
    .locals 32

    const/4 v0, 0

    const v1, 127  # Max 8-bit signed integer.
    const v2, -128  # Min 8-bit signed integer.
    const v3, 128  # Max 8-bit signed integer plus one.
    const v4, -129  # Min 8-bit signed integer minus one.

    const v5, 32767  # Max 16-bit signed integer.
    const v6, -32768  # Min 16-bit signed integer.
    const v7, 32768  # Max 16-bit signed integer plus one.
    const v8, -32769  # Min 16-bit signed integer minus one.

    const v9, 9
    const v10, 10
    const v11, 11
    const v12, 12
    const v13, 13
    const v14, 14
    const v15, 15

    sub-int v16, v0, v1
    sub-int v17, v16, v2
    sub-int v18, v17, v3
    sub-int v19, v18, v4
    sub-int v20, v19, v5
    sub-int v21, v20, v6
    sub-int v22, v21, v7
    sub-int v23, v22, v8
    sub-int v24, v1, v23
    sub-int v25, v2, v24
    sub-int v26, v3, v25
    sub-int v27, v4, v26
    sub-int v28, v5, v27
    sub-int v29, v6, v28
    sub-int v30, v7, v29
    sub-int v31, v8, v30

    invoke-static/range {v16 .. v31}, LTest;->sixteenIntArgMethod(IIIIIIIIIIIIIIII)V

    return v31
.end method

.method static addConstantUsedTwice()I
    .locals 4

    const/4 v0, 0
    const/4 v1, 1
    add-int/2addr v0, v1
    add-int/2addr v0, v1
    return v0
.end method

.method static addTwoLongConstants()J
    .locals 4

    const-wide v0, 1
    const-wide v2, 2
    add-long v0, v0, v2
    return-wide v0
.end method

.method static addTwoDoubleConstants()D
    .locals 4

    const-wide v0, 0x3ff0000000000000L  # 1.0
    const-wide v2, 0x4000000000000000L  # 2.0
    add-double v0, v0, v2
    return-wide v0
.end method

.method static cmpFold()I
    .locals 4

    const-wide v0, 0
    const-wide v2, 0
    cmp-long v0, v0, v2
    return v0
.end method

.method static addFoldLeft(I)I
    .locals 2

    const/4 v0, 1
    const/4 v1, 2
    add-int/2addr v0, v1
    add-int/2addr v0, p0
    return v0
.end method

.method static subFoldLeft(I)I
    .locals 2

    const/4 v0, 1
    const/4 v1, 2
    sub-int/2addr v0, v1
    sub-int/2addr v0, p0
    return v0
.end method

.method public static main([Ljava/lang/String;)V
    .locals 3

    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    # Calculate: 0 + (-1).
    const/4 v1, 0
    invoke-static {v1}, LTest;->addMinusOne(I)I
    move-result v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    invoke-static {}, LTest;->addTwoConstants()I
    move-result v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    invoke-static {}, LTest;->addSameTwoConstants()I
    move-result v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    invoke-static {}, LTest;->addTwoLongConstants()J
    move-result-wide v1
    invoke-virtual {v0, v1, v2}, Ljava/io/PrintStream;->println(J)V

    invoke-static {}, LTest;->addTwoDoubleConstants()D
    move-result-wide v1
    invoke-virtual {v0, v1, v2}, Ljava/io/PrintStream;->println(D)V

    # Calculate: 0 - (-1).
    const/4 v1, 0
    invoke-static {v1}, LTest;->subMinusOne(I)I
    move-result v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    invoke-static {}, LTest;->subSameTwoConstants()I
    move-result v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    invoke-static {}, LTest;->subtractConstants()I
    move-result v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    invoke-static {}, LTest;->subtractConstants8bitRegisters()I
    move-result v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    invoke-static {}, LTest;->addConstantUsedTwice()I
    move-result v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    const/4 v1, 1
    invoke-static {v1}, LTest;->addFoldLeft(I)I
    move-result v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    const/4 v1, 1
    invoke-static {v1}, LTest;->subFoldLeft(I)I
    move-result v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    return-void
.end method
