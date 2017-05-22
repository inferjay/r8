# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

.class public LTest;

.super Ljava/lang/Object;

# Simple fill array data
.method public static test1()[I
  .registers 2

  const/4 v1, 3
  new-array v0, v1, [I
  fill-array-data v0, :array_data
  return-object v0

  :array_data
  .array-data 4
    1 2 3
  .end array-data
.end method

# Fill array data after data
.method public static test2()[I
  .registers 2

  goto :start

  :array_data
  .array-data 4
    4 5 6
  .end array-data

  :start
  const/4 v1, 3
  new-array v0, v1, [I
  fill-array-data v0, :array_data
  return-object v0

.end method

.method public static main([Ljava/lang/String;)V
    .locals 2

    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    invoke-static {}, LTest;->test1()[I
    move-result-object v1
    invoke-static {v1}, Ljava/util/Arrays;->toString([I)Ljava/lang/String;
    move-result-object v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->print(Ljava/lang/Object;)V

    invoke-static {}, LTest;->test2()[I
    move-result-object v1
    invoke-static {v1}, Ljava/util/Arrays;->toString([I)Ljava/lang/String;
    move-result-object v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->print(Ljava/lang/Object;)V

    return-void
.end method
