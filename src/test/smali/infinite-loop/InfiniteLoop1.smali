# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

.class public LTest;

.super Ljava/lang/Object;

.method public static loop1()V
  .locals 0
  goto :a

  :a
  goto :b

  :b
  goto :c

  :c
  goto :d

  :d
  goto :b

  return-void
.end method

.method public static loop2()V
  .locals 2
  sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;
  const/4 v1, 0x1
  invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V
  goto :a

  :a
  goto :b

  :b
  goto :c

  :c
  goto :d

  :d
  goto :b

  return-void
.end method

.method public static main([Ljava/lang/String;)V
    .locals 0
    return-void
.end method
