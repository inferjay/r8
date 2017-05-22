# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

.class public LTest;

.super Ljava/lang/Object;

# Empty sparse switch
.method public static test1(I)I
  .registers 1
  sparse-switch v0, :sparse_switch_data
  const/4 v0, 0x1
  return v0

  :sparse_switch_data
  .sparse-switch
  .end sparse-switch
.end method

# Empty sparse switch after data
.method public static test2(I)I
  .registers 1

  goto :sparse_switch

  :sparse_switch_data
  .sparse-switch
  .end sparse-switch

  :sparse_switch
  sparse-switch v0, :sparse_switch_data
  const/4 v0, 0x2
  return v0
.end method

# Sparse switch after data
.method public static test3(I)I
  .registers 1

  goto :sparse_switch

  :case_2
  const/4 v0, 0x3
  goto :return

  :sparse_switch_data
  .sparse-switch
    0x2 -> :case_2
    0x4 -> :case_4
  .end sparse-switch

  :sparse_switch
  sparse-switch v0, :sparse_switch_data
  const/4 v0, 0x5
  goto :return

  :case_4
  const/4 v0, 0x4

  :return
  return v0
.end method

.method public static main([Ljava/lang/String;)V
    .locals 2

    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    const/4 v1, 0x0
    invoke-static {v1}, LTest;->test1(I)I
    move-result v1
    invoke-static {v1}, Ljava/lang/Integer;->toString(I)Ljava/lang/String;
    move-result-object v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->print(Ljava/lang/Object;)V

    const/4 v1, 0x0
    invoke-static {v1}, LTest;->test2(I)I
    move-result v1
    invoke-static {v1}, Ljava/lang/Integer;->toString(I)Ljava/lang/String;
    move-result-object v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->print(Ljava/lang/Object;)V

    const/4 v1, 0x2
    invoke-static {v1}, LTest;->test3(I)I
    move-result v1
    invoke-static {v1}, Ljava/lang/Integer;->toString(I)Ljava/lang/String;
    move-result-object v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->print(Ljava/lang/Object;)V

    const/4 v1, 0x4
    invoke-static {v1}, LTest;->test3(I)I
    move-result v1
    invoke-static {v1}, Ljava/lang/Integer;->toString(I)Ljava/lang/String;
    move-result-object v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->print(Ljava/lang/Object;)V

    const/4 v1, 0x6
    invoke-static {v1}, LTest;->test3(I)I
    move-result v1
    invoke-static {v1}, Ljava/lang/Integer;->toString(I)Ljava/lang/String;
    move-result-object v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->print(Ljava/lang/Object;)V

    return-void
.end method
