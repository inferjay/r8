# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

.class public LTest;

.super Ljava/lang/Object;

.method static constantEqTrue()I
    .locals 1

    const v0, 0
    if-eqz v0, :equals
    const v0, 1
    return v0
    :equals
    const v0, 2
    return v0
.end method

.method static constantEqFalse()I
    .locals 1

    const v0, 1
    if-eqz v0, :equals
    const v0, 1
    return v0
    :equals
    const v0, 2
    return v0
.end method

.method static constantNeTrue()I
    .locals 1

    const v0, 1
    if-nez v0, :equals
    const v0, 1
    return v0
    :equals
    const v0, 2
    return v0
.end method

.method static constantNeFalse()I
    .locals 1

    const v0, 0
    if-nez v0, :equals
    const v0, 1
    return v0
    :equals
    const v0, 2
    return v0
.end method

.method static constantLtzTrue()I
    .locals 1

    const v0, -1
    if-ltz v0, :equals
    const v0, 1
    return v0
    :equals
    const v0, 2
    return v0
.end method

.method static constantLtzFalse()I
    .locals 1

    const v0, 0
    if-ltz v0, :equals
    const v0, 1
    return v0
    :equals
    const v0, 2
    return v0
.end method

.method static constantLezTrue()I
    .locals 1

    const v0, 0
    if-lez v0, :equals
    const v0, 1
    return v0
    :equals
    const v0, 2
    return v0
.end method

.method static constantLezFalse()I
    .locals 1

    const v0, 1
    if-lez v0, :equals
    const v0, 1
    return v0
    :equals
    const v0, 2
    return v0
.end method

.method static constantGtzTrue()I
    .locals 1

    const v0, 1
    if-gtz v0, :equals
    const v0, 1
    return v0
    :equals
    const v0, 2
    return v0
.end method

.method static constantGtzFalse()I
    .locals 1

    const v0, 0
    if-gtz v0, :equals
    const v0, 1
    return v0
    :equals
    const v0, 2
    return v0
.end method

.method static constantGezTrue()I
    .locals 1

    const v0, 0
    if-gez v0, :equals
    const v0, 1
    return v0
    :equals
    const v0, 2
    return v0
.end method

.method static constantGezFalse()I
    .locals 1

    const v0, -1
    if-gez v0, :equals
    const v0, 1
    return v0
    :equals
    const v0, 2
    return v0
.end method

.method static cmpConstantLong()I
    .locals 4

    const-wide v0, 0
    const-wide v2, 0
    cmp-long v0, v0, v2
    if-eqz v0, :equals
    const v0, 1
    return v0
    :equals
    const v0, 2
    return v0
.end method

.method public static main([Ljava/lang/String;)V
    .locals 3

    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    invoke-static {}, LTest;->constantEqTrue()I
    move-result v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    invoke-static {}, LTest;->constantEqFalse()I
    move-result v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    invoke-static {}, LTest;->constantNeTrue()I
    move-result v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    invoke-static {}, LTest;->constantNeFalse()I
    move-result v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    invoke-static {}, LTest;->constantLtzTrue()I
    move-result v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    invoke-static {}, LTest;->constantLtzFalse()I
    move-result v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    invoke-static {}, LTest;->constantLezTrue()I
    move-result v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    invoke-static {}, LTest;->constantLezFalse()I
    move-result v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    invoke-static {}, LTest;->constantGtzTrue()I
    move-result v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    invoke-static {}, LTest;->constantGtzFalse()I
    move-result v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    invoke-static {}, LTest;->constantGezTrue()I
    move-result v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    invoke-static {}, LTest;->constantGezFalse()I
    move-result v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    invoke-static {}, LTest;->cmpConstantLong()I
    move-result v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    return-void
.end method
