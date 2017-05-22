# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

.class public final LTestObject;
.super Ljava/lang/Object;

.method public final a(LTest;LTest;)LTest;
    .registers 7
    .prologue
    const/4 v0, 0x0
    if-eqz p1, :cond_23
    iget-boolean v1, p1, LTest;->b:Z
    if-eqz v1, :cond_35
    iget-object v1, p0, LTest;->f:LTest;
    iget-object v1, v1, LTest;->b:LTest;
    invoke-virtual {v1}, LTest;->hasPrevious()Z
    move-result v1
    if-eqz v1, :cond_35
    new-instance p2, LTest;
    const/4 v1, 0x1
    iget-object v2, p0, LTest;->f:LTest;
    iget-object v3, v2, LTest;->b:LTest;
    invoke-virtual {v3}, LTest;->hasPrevious()Z
    move-result v3
    if-nez v3, :cond_24
    :goto_1e
    iget-object v0, v0, LTest;->a:LTest;
    invoke-direct {p2, v1, v0}, LTest;-><init>(ILTest;)V
    :cond_23
    :goto_23
    return-object p2
    :cond_24
    iget-object v0, v2, LTest;->b:LTest;
    iget-object v0, v0, LTest;->a:Ljava/util/ArrayList;
    iget-object v2, v2, LTest;->b:LTest;
    invoke-virtual {v2}, LTest;->previousIndex()I
    move-result v2
    invoke-interface {v0, v2}, Ljava/util/List;->get(I)Ljava/lang/Object;
    move-result-object v0
    check-cast v0, LTest;
    goto :goto_1e
    :cond_35
    iget-boolean v1, p1, LTest;->b:Z
    if-eqz v1, :cond_67
    iget-object v1, p0, LTest;->f:LTest;
    iget-object v1, v1, LTest;->b:LTest;
    invoke-virtual {v1}, LTest;->hasNext()Z
    move-result v1
    if-eqz v1, :cond_67
    new-instance p2, LTest;
    const/4 v1, 0x2
    iget-object v2, p0, LTest;->f:LTest;
    iget-object v3, v2, LTest;->b:LTest;
    invoke-virtual {v3}, LTest;->hasNext()Z
    move-result v3
    if-nez v3, :cond_56
    :goto_50
    iget-object v0, v0, LTest;->a:LTest;
    invoke-direct {p2, v1, v0}, LTest;-><init>(ILTest;)V
    goto :goto_23
    :cond_56
    iget-object v0, v2, LTest;->b:LTest;
    iget-object v0, v0, LTest;->a:Ljava/util/ArrayList;
    iget-object v2, v2, LTest;->b:LTest;
    invoke-virtual {v2}, LTest;->nextIndex()I
    move-result v2
    invoke-interface {v0, v2}, Ljava/util/List;->get(I)Ljava/lang/Object;
    move-result-object v0
    check-cast v0, LTest;
    goto :goto_50
    :cond_67
    iget-object v0, p1, LTest;->a:LTest;
    if-eqz v0, :cond_23
    new-instance p2, LTest;
    const/4 v0, 0x0
    new-instance v1, LTest;
    iget-object v2, p1, LTest;->a:LTest;
    invoke-direct {v1, v2}, LTest;-><init>(LTest;)V
    invoke-direct {p2, v0, v1}, LTest;-><init>(ILTest;)V
    goto :goto_23
.end method