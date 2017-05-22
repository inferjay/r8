# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

# Before Lollipop (Art 5.1.1) Art/Dalvik failed on verification if an empty sparse switch payload
# was the last instruction in a method. This was originally reported as 19827056, and fixed in
# https://android.googlesource.com/platform/art/+/9ccd151d0d27a729f88af9d00285afe4d147981a

# This test is copied from
# https://android.googlesource.com/platform/art/+/9ccd151d0d27a729f88af9d00285afe4d147981a
.class public LTest;
.super Ljava/lang/Object;

.method public static run()V
    .registers 2

    :start
    const v0, 0

    sparse-switch v0, :SparseSwitch

    return-void

    :SparseSwitch
    .sparse-switch
    .end sparse-switch

.end method

.method public static main([Ljava/lang/String;)V
    .locals 0

    invoke-static {}, LTest;->run()V

    return-void
.end method
