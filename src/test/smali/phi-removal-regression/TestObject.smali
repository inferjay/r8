# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

.class public final LTestObject;
.super Ljava/lang/Object;

.field final list:Ljava/util/ArrayList;
.field private final s:Z

.method public final a(LTest;I)Z
    .registers 9
    .prologue
    const/4 v2, 0x1
    const/4 v1, 0x0
    invoke-virtual {p1}, LTest;->returnBoolean()Z
    move-result v0
    if-eqz v0, :cond_a
    move v0, v1
    :goto_9
    return v0
    :cond_a
    and-int/lit8 v0, p2, 0x2
    if-nez v0, :cond_14
    iget-boolean v0, p0, LTestObject;->b:Z
    if-eqz v0, :cond_14
    move v0, v2
    goto :goto_9
    :cond_14
    iget-object v0, p0, LTestObject;->list:Ljava/util/ArrayList;
    invoke-virtual {v0}, Ljava/util/ArrayList;->size()I
    move-result v4
    move v3, v1
    :goto_1b
    if-ge v3, v4, :cond_3b
    iget-object v0, p0, LTestObject;->list:Ljava/util/ArrayList;
    invoke-virtual {v0, v3}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;
    move-result-object v0
    check-cast v0, LTest;
    and-int/lit8 v5, p2, 0x1
    if-eqz v5, :cond_2f
    invoke-virtual {v0}, LTest;->returnBoolean()Z
    move-result v5
    if-nez v5, :cond_37
    :cond_2f
    invoke-virtual {v0, p1}, LTest;->returnTheOtherBoolean(LTest;)Z
    move-result v0
    if-eqz v0, :cond_37
    move v0, v2
    goto :goto_9
    :cond_37
    add-int/lit8 v0, v3, 0x1
    move v3, v0
    goto :goto_1b
    :cond_3b
    move v0, v1
    goto :goto_9
.end method
