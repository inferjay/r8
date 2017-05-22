# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

.class public LTest;

.super Ljava/lang/Object;

.field static private result:Ljava/lang/String;

.method public static test1(Z)Ljava/lang/String;
    .locals 1
    .param p0

    if-eqz p0, :false_branch

    const-string v0, "T"
    return-object v0

    :false_branch
    const-string v0, "F"
    return-object v0
.end method

.method public static test2(Z)V
    .locals 1
    .param p0

    if-eqz p0, :false_branch

    const-string v0, "t"
    sput-object v0, LTest;->result:Ljava/lang/String;
    return-void

    :false_branch
    const-string v0, "f"
    sput-object v0, LTest;->result:Ljava/lang/String;
    return-void
.end method

.method public static test3(Z)J
    .locals 2
    .param p0

    if-eqz p0, :false_branch

    const-wide v0, 0x1
    return-wide v0

    :false_branch
    const-wide/high16 v0, 0x4000000000000000L
    return-wide v0
.end method

.method public static test4(Z)Z
    .locals 0
    .param p0

    if-eqz p0, :false_branch

    return p0

    :false_branch
    return p0
.end method

.method public static main([Ljava/lang/String;)V
    .locals 3
    .param p0

    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    const/4 v1, 0x1
    invoke-static {v1}, LTest;->test1(Z)Ljava/lang/String;
    move-result-object v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->print(Ljava/lang/Object;)V

    const/4 v1, 0x0
    invoke-static {v1}, LTest;->test1(Z)Ljava/lang/String;
    move-result-object v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->print(Ljava/lang/Object;)V

    const/4 v1, 0x1
    invoke-static {v1}, LTest;->test2(Z)V
    sget-object v1, LTest;->result:Ljava/lang/String;
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->print(Ljava/lang/Object;)V

    const/4 v1, 0x0
    invoke-static {v1}, LTest;->test2(Z)V
    sget-object v1, LTest;->result:Ljava/lang/String;
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->print(Ljava/lang/Object;)V

    invoke-virtual {v0}, Ljava/io/PrintStream;->println()V

    const/4 v1, 0x1
    invoke-static {v1}, LTest;->test3(Z)J
    move-result-wide v1
    invoke-static {v1, v2}, Ljava/lang/Long;->toString(J)Ljava/lang/String;
    move-result-object v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(Ljava/lang/Object;)V

    const/4 v1, 0x0
    invoke-static {v1}, LTest;->test3(Z)J
    move-result-wide v1
    invoke-static {v1, v2}, Ljava/lang/Long;->toString(J)Ljava/lang/String;
    move-result-object v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(Ljava/lang/Object;)V

    const/4 v1, 0x1
    invoke-static {v1}, LTest;->test4(Z)Z
    move-result v1
    invoke-static {v1}, Ljava/lang/Boolean;->toString(Z)Ljava/lang/String;
    move-result-object v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(Ljava/lang/Object;)V

    const/4 v1, 0x0
    invoke-static {v1}, LTest;->test4(Z)Z
    move-result v1
    invoke-static {v1}, Ljava/lang/Boolean;->toString(Z)Ljava/lang/String;
    move-result-object v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(Ljava/lang/Object;)V

    return-void
.end method


