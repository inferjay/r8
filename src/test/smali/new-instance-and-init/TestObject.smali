# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

.class public final LTestObject;
.super Ljava/lang/Object;

.method public static allocate(I)V
    .registers 4
    new-instance v2, LTest;
    if-nez v3, :cond_9
    const/4 v1, 0x0
    invoke-direct {v2, v1}, LTest;-><init>(I)V
    :goto_8
    return-void
    :cond_9
    const/16 v0, 0xa
    if-ge v3, v0, :cond_14
    const/4 v1, 0x0
    invoke-direct {v2, v1}, LTest;-><init>(I)V
    goto :goto_8
    :cond_14
    const/4 v1, 0x0
    invoke-direct {v2, v1}, LTest;-><init>(I)V
    goto :goto_8
.end method