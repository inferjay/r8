# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

.class public LTest;

.super Ljava/lang/Object;

.method static fibonacci(I)I
    .locals 2

    if-eqz p0, :return
    const/4 v0, 0x1
    if-ne p0, v0, :calc

    :return
    return p0

    :calc
    add-int/lit8 v0, p0, -0x1
    invoke-static {v0}, LTest;->fibonacci(I)I
    move-result v0
    add-int/lit8 v1, p0, -0x2
    invoke-static {v1}, LTest;->fibonacci(I)I
    move-result v1
    add-int p0, v0, v1
    goto :return
.end method

.method static fibonacciLong(J)J
    .locals 4

    const-wide/16 v2, 0x1
    const-wide/16 v0, 0x0
    cmp-long v0, p0, v0
    if-eqz v0, :return
    cmp-long v0, p0, v2
    if-nez v0, :calc

    :return
    return-wide p0

    :calc
    sub-long v0, p0, v2
    invoke-static {v0, v1}, LTest;->fibonacciLong(J)J
    move-result-wide v0
    const-wide/16 v2, 0x2
    sub-long v2, p0, v2
    invoke-static {v2, v3}, LTest;->fibonacciLong(J)J
    move-result-wide v2
    add-long p0, v0, v2
    goto :return
.end method

.method static fibonacciJack(I)I
    .locals 2

    if-eqz p0, :return
    const/4 v0, 0x1
    if-ne p0, v0, :calc

    :return
    return p0

    :calc
    add-int/lit8 v0, p0, -0x1
    invoke-static {v0}, LTest;->fibonacciJack(I)I
    move-result v0
    add-int/lit8 v1, p0, -0x2
    invoke-static {v1}, LTest;->fibonacciJack(I)I
    move-result v1
    add-int/2addr v0, v1
    return v0
.end method

.method static fibonacciLongJack(J)J
    .locals 4

    const-wide/16 v2, 0x1
    const-wide/16 v0, 0x0
    cmp-long v0, p0, v0
    if-eqz v0, :return
    cmp-long v0, p0, v2
    if-nez v0, :calc

    :return
    return-wide p0

    :calc
    sub-long v0, p0, v2
    invoke-static {v0, v1}, LTest;->fibonacciLongJack(J)J
    move-result-wide v0
    const-wide/16 v2, 0x2
    sub-long v2, p0, v2
    invoke-static {v2, v3}, LTest;->fibonacciLongJack(J)J
    move-result-wide v2
    add-long/2addr v0, v2
    return-wide v0
.end method

.method public static main([Ljava/lang/String;)V
    .registers 5

    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    const/16 v1, 0xa
    invoke-static {v1}, LTest;->fibonacci(I)I
    move-result v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;
    const-wide/16 v2, 0xa
    invoke-static {v2, v3}, LTest;->fibonacciLong(J)J
    move-result-wide v2
    invoke-virtual {v0, v2, v3}, Ljava/io/PrintStream;->println(J)V

    const/16 v1, 0xa
    invoke-static {v1}, LTest;->fibonacciJack(I)I
    move-result v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;
    const-wide/16 v2, 0xa
    invoke-static {v2, v3}, LTest;->fibonacciLongJack(J)J
    move-result-wide v2
    invoke-virtual {v0, v2, v3}, Ljava/io/PrintStream;->println(J)V

    return-void
.end method
