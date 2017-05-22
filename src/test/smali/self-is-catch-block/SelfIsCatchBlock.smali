# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

.class public LTest;

.super Ljava/lang/Object;

.method private static throwOnPositive(I)V
    .registers 2
    if-lez v1, :cond_nothrow
    new-instance v0, Ljava/lang/RuntimeException;
    invoke-direct {v0}, Ljava/lang/RuntimeException;-><init>()V
    throw v0
    :cond_nothrow
    return-void
.end method

# Tests the flow of values in the pathological case that the block is its own catch handler.
# This tests that the register allocator does not insert moves at the end of the throwing
# block since in the case of a throw the block does not actually complete.
.method static loopWhileThrow(I)I
    .registers 4
    :catchall_0
    move v0, p0
    add-int/lit8 p0, p0, -0x1
    :try_start_6
    invoke-static {v0}, LTest;->throwOnPositive(I)V
    :try_end_9
    .catchall {:try_start_6 .. :try_end_9} :catchall_0
    return p0
.end method

.method public static main([Ljava/lang/String;)V
    .registers 2
    const v0, 0x64
    sget-object v1, Ljava/lang/System;->out:Ljava/io/PrintStream;
    invoke-virtual {v1, v0}, Ljava/io/PrintStream;->println(I)V
    invoke-static {v0}, LTest;->loopWhileThrow(I)I
    move-result v0
    invoke-virtual {v1, v0}, Ljava/io/PrintStream;->println(I)V
    return-void
.end method
