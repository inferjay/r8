# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

.class public LTest;

.super Ljava/lang/Object;

.method public static test1()I
  .registers 1
  goto :return

  :dummy
  const/4 v0, 0x1

  :return
  const/4 v0, 0x7
  return v0

.end method

.method public static test2()I
  .registers 1
  goto :return

  :dummy1
  const/4 v0, 0x1

  :dummy2
  const/4 v0, 0x2

  :return
  const/4 v0, 0x7
  return v0

.end method

.method public static test3()I
  .registers 1
  goto :return

  :dummy1
  const/4 v0, 0x1
  goto :dummy3

  :dummy2
  const/4 v0, 0x2
  goto :return

  :dummy3
  const/4 v0, 0x3
  goto :return

  :dummy4
  const/4 v0, 0x4

  :return
  const/4 v0, 0x7
  return v0

.end method

.method public static main([Ljava/lang/String;)V
    .locals 2

    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    invoke-static {}, LTest;->test1()I
    move-result v1
    invoke-static {v1}, Ljava/lang/Integer;->toString(I)Ljava/lang/String;
    move-result-object v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->print(Ljava/lang/Object;)V

    invoke-static {}, LTest;->test2()I
    move-result v1
    invoke-static {v1}, Ljava/lang/Integer;->toString(I)Ljava/lang/String;
    move-result-object v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->print(Ljava/lang/Object;)V

    invoke-static {}, LTest;->test3()I
    move-result v1
    invoke-static {v1}, Ljava/lang/Integer;->toString(I)Ljava/lang/String;
    move-result-object v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->print(Ljava/lang/Object;)V

    return-void
.end method
