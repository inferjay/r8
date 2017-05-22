# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

.class public LTest;

.super Ljava/lang/Object;

# Filled new array from arguments.
.method public static test1(III)[I
  .locals 1

  filled-new-array {p0, p1, p2}, [I
  move-result-object v0
  return-object v0
.end method

# Filled new array from constants.
.method public static test2()[I
  .locals 3

  const/4 v0, 4
  const/4 v1, 5
  const/4 v2, 6
  filled-new-array {v0, v1, v2}, [I
  move-result-object v0
  return-object v0
.end method

# Filled new array range from arguments.
.method public static test3(IIIIII)[I
  .locals 1

  filled-new-array/range {p0 .. p5}, [I
  move-result-object v0
  return-object v0
.end method

# Filled new array range from constants.
.method public static test4()[I
  .locals 6

  const/4 v0, 6
  const/4 v1, 5
  const/4 v2, 4
  const/4 v3, 3
  const/4 v4, 2
  const/4 v5, 1
  filled-new-array/range {v0  .. v5}, [I
  move-result-object v0
  return-object v0
.end method

.method public static main([Ljava/lang/String;)V
    .locals 7

    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    const/4 v1, 1
    const/4 v2, 2
    const/4 v3, 3
    invoke-static {v1, v2, v3}, LTest;->test1(III)[I
    move-result-object v1
    invoke-static {v1}, Ljava/util/Arrays;->toString([I)Ljava/lang/String;
    move-result-object v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->print(Ljava/lang/Object;)V

    invoke-static {}, LTest;->test2()[I
    move-result-object v1
    invoke-static {v1}, Ljava/util/Arrays;->toString([I)Ljava/lang/String;
    move-result-object v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->print(Ljava/lang/Object;)V

    const/4 v1, 1
    const/4 v2, 2
    const/4 v3, 3
    const/4 v4, 4
    const/4 v5, 5
    const/4 v6, 6
    invoke-static/range {v1 .. v6}, LTest;->test3(IIIIII)[I
    move-result-object v1
    invoke-static {v1}, Ljava/util/Arrays;->toString([I)Ljava/lang/String;
    move-result-object v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->print(Ljava/lang/Object;)V

    invoke-static {}, LTest;->test4()[I
    move-result-object v1
    invoke-static {v1}, Ljava/util/Arrays;->toString([I)Ljava/lang/String;
    move-result-object v1
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->print(Ljava/lang/Object;)V

    return-void
.end method
