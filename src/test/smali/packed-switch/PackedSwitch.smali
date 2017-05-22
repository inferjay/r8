# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

.class public LTest;

.super Ljava/lang/Object;

# Empty packed switch
.method public static test1(I)I
  .registers 1
  packed-switch v0, :packed_switch_data
  const/4 v0, 0x1
  return v0

  :packed_switch_data
  .packed-switch 0x0
  .end packed-switch
.end method

# Empty packed switch after data
.method public static test2(I)I
  .registers 1

  goto :packed_switch

  :packed_switch_data
  .packed-switch 0x0
  .end packed-switch

  :packed_switch
  packed-switch v0, :packed_switch_data
  const/4 v0, 0x2
  return v0
.end method

# Packed switch after data
.method public static test3(I)I
  .registers 1

  goto :packed_switch

  :case_0
  const/4 v0, 0x3
  goto :return

  :packed_switch_data
  .packed-switch 0x0
    :case_0
    :case_1
  .end packed-switch

  :packed_switch
  packed-switch v0, :packed_switch_data
  const/4 v0, 0x5
  goto :return

  :case_1
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

    const/4 v1, 0x0
    invoke-static {v1}, LTest;->test3(I)I
    move-result v1
    invoke-static {v1}, Ljava/lang/Integer;->toString(I)Ljava/lang/String;
    move-result-object v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->print(Ljava/lang/Object;)V

    const/4 v1, 0x1
    invoke-static {v1}, LTest;->test3(I)I
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

    return-void
.end method
