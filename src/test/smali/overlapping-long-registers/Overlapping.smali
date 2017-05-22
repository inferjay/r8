# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

# Some versions of dalvik had a bug where you cannot use the second half of an input long
# as the first part of an output long. This smali code explicitly has that issue so the
# generated dex file can be used to test art/dalvik versions.
#
# The issue was that if you have
#
#   add-long v3, v0, v2
#
# dalvik would add v0 and v2 and store the result in v3 before adding v1 and v3 (now clobbered).

.class public LTest;

.super Ljava/lang/Object;

.method static add(JJ)J
    .locals 5
    move-wide v0, p0
    move-wide v2, p2
    add-long v3, v0, v2
    return-wide v3
.end method

.method public static main([Ljava/lang/String;)V
    .locals 10

    sget-object v5, Ljava/lang/System;->out:Ljava/io/PrintStream;

    const-wide/high16 v0, 0x4000000000000000L
    const-wide/high16 v2, 0x4100000000000000L
    invoke-static {v0, v1, v2, v3}, LTest;->add(JJ)J
    move-result-wide v6
    invoke-virtual {v5, v6, v7}, Ljava/io/PrintStream;->println(J)V

    # Adding loop in an attempt to get the jit to process the add method.
    const v8, 1000000
    const v9, 1
    :loop
    if-eqz v8, :exit
    const-wide v0, 0x4000000000040000L
    const-wide v2, 0x4100000000041000L
    invoke-static {v0, v1, v2, v3}, LTest;->add(JJ)J
    move-result-wide v6
    sub-int v8, v8, v9
    goto :loop

    :exit
    invoke-virtual {v5, v6, v7}, Ljava/io/PrintStream;->println(J)V
    return-void
.end method
