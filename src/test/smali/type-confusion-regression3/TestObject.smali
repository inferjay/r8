# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

.class public final LTestObject;
.super Ljava/lang/Object;

.field private i:I
.field private o:LTest;

.method public final a(LTest;LTest;)I
    .registers 28
    .prologue
    :cond_0
    :goto_0
    move-object/from16 v0, p0
    iget v4, v0, LTestObject;->i:I
    packed-switch v4, :pswitch_data_60a
    move-object/from16 v0, p0
    iget v4, v0, LTestObject;->i:I
    const/4 v5, 0x3
    if-ne v4, v5, :cond_4ac
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->o:LTest;
    if-nez v4, :cond_431
    move-object/from16 v0, p0
    iget-object v11, v0, LTestObject;->o:LTest;
    const/4 v5, 0x0
    const-wide v8, 0x7fffffffffffffffL
    invoke-virtual {v11}, LTest;->size()I
    move-result v12
    const/4 v4, 0x0
    move v10, v4
    :goto_24
    if-ge v10, v12, :cond_3e8
    invoke-virtual {v11, v10}, LTest;->valueAt(I)Ljava/lang/Object;
    move-result-object v4
    check-cast v4, LTest;
    iget v6, v4, LTest;->e:I
    iget-object v7, v4, LTest;->a:LTest;
    iget v7, v7, LTest;->d:I
    if-eq v6, v7, :cond_5fe
    iget-object v6, v4, LTest;->a:LTest;
    iget-wide v6, v6, LTest;->b:J
    cmp-long v13, v6, v8
    if-gez v13, :cond_5fe
    move-wide/from16 v23, v6
    move-object v6, v4
    move-wide/from16 v4, v23
    :goto_41
    add-int/lit8 v7, v10, 0x1
    move v10, v7
    move-wide v8, v4
    move-object v5, v6
    goto :goto_24
    :pswitch_47
    move-object/from16 v0, p0
    iget v4, v0, LTestObject;->i:I
    if-nez v4, :cond_8a
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->o:LTest;
    iget-object v4, v4, LTest;->a:[B
    const/4 v5, 0x0
    const/16 v6, 0x8
    const/4 v7, 0x1
    move-object/from16 v0, p1
    invoke-virtual {v0, v4, v5, v6, v7}, LTest;->a([BIIZ)Z
    move-result v4
    if-nez v4, :cond_64
    const/4 v4, 0x0
    :goto_60
    if-nez v4, :cond_0
    const/4 v4, -0x1
    :goto_63
    return v4
    :cond_64
    const/16 v4, 0x8
    move-object/from16 v0, p0
    iput v4, v0, LTestObject;->i:I
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->o:LTest;
    const/4 v5, 0x0
    invoke-virtual {v4, v5}, LTest;->c(I)V
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->o:LTest;
    invoke-virtual {v4}, LTest;->h()J
    move-result-wide v4
    move-object/from16 v0, p0
    iput-wide v4, v0, LTestObject;->n:J
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->o:LTest;
    invoke-virtual {v4}, LTest;->j()I
    move-result v4
    move-object/from16 v0, p0
    iput v4, v0, LTestObject;->i:I
    :cond_8a
    move-object/from16 v0, p0
    iget-wide v4, v0, LTestObject;->n:J
    const-wide/16 v6, 0x1
    cmp-long v4, v4, v6
    if-nez v4, :cond_b9
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->o:LTest;
    iget-object v4, v4, LTest;->a:[B
    const/16 v5, 0x8
    const/16 v6, 0x8
    move-object/from16 v0, p1
    invoke-virtual {v0, v4, v5, v6}, LTest;->b([BII)V
    move-object/from16 v0, p0
    iget v4, v0, LTestObject;->i:I
    add-int/lit8 v4, v4, 0x8
    move-object/from16 v0, p0
    iput v4, v0, LTestObject;->i:I
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->o:LTest;
    invoke-virtual {v4}, LTest;->p()J
    move-result-wide v4
    move-object/from16 v0, p0
    iput-wide v4, v0, LTestObject;->n:J
    :cond_b9
    invoke-virtual/range {p1 .. p1}, LTest;->c()J
    move-result-wide v4
    move-object/from16 v0, p0
    iget v6, v0, LTestObject;->i:I
    int-to-long v6, v6
    sub-long v6, v4, v6
    move-object/from16 v0, p0
    iget v4, v0, LTestObject;->i:I
    sget v5, LTest;->J:I
    if-ne v4, v5, :cond_ec
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->o:LTest;
    invoke-virtual {v4}, LTest;->size()I
    move-result v8
    const/4 v4, 0x0
    move v5, v4
    :goto_d6
    if-ge v5, v8, :cond_ec
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->o:LTest;
    invoke-virtual {v4, v5}, LTest;->valueAt(I)Ljava/lang/Object;
    move-result-object v4
    check-cast v4, LTest;
    iget-object v4, v4, LTest;->a:LTest;
    iput-wide v6, v4, LTest;->c:J
    iput-wide v6, v4, LTest;->b:J
    add-int/lit8 v4, v5, 0x1
    move v5, v4
    goto :goto_d6
    :cond_ec
    move-object/from16 v0, p0
    iget v4, v0, LTestObject;->i:I
    sget v5, LTest;->h:I
    if-ne v4, v5, :cond_11e
    const/4 v4, 0x0
    move-object/from16 v0, p0
    iput-object v4, v0, LTestObject;->o:LTest;
    move-object/from16 v0, p0
    iget-wide v4, v0, LTestObject;->n:J
    add-long/2addr v4, v6
    move-object/from16 v0, p0
    iput-wide v4, v0, LTestObject;->q:J
    move-object/from16 v0, p0
    iget-boolean v4, v0, LTestObject;->w:Z
    if-nez v4, :cond_116
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->o:LTest;
    sget-object v5, LTest;->f:LTest;
    invoke-virtual {v4, v5}, LTest;->a(LTest;)V
    const/4 v4, 0x1
    move-object/from16 v0, p0
    iput-boolean v4, v0, LTestObject;->w:Z
    :cond_116
    const/4 v4, 0x2
    move-object/from16 v0, p0
    iput v4, v0, LTestObject;->i:I
    :goto_11b
    const/4 v4, 0x1
    goto/16 :goto_60
    :cond_11e
    move-object/from16 v0, p0
    iget v4, v0, LTestObject;->i:I
    sget v5, LTest;->A:I
    if-eq v4, v5, :cond_146
    sget v5, LTest;->C:I
    if-eq v4, v5, :cond_146
    sget v5, LTest;->D:I
    if-eq v4, v5, :cond_146
    sget v5, LTest;->E:I
    if-eq v4, v5, :cond_146
    sget v5, LTest;->F:I
    if-eq v4, v5, :cond_146
    sget v5, LTest;->J:I
    if-eq v4, v5, :cond_146
    sget v5, LTest;->K:I
    if-eq v4, v5, :cond_146
    sget v5, LTest;->L:I
    if-eq v4, v5, :cond_146
    sget v5, LTest;->O:I
    if-ne v4, v5, :cond_178
    :cond_146
    const/4 v4, 0x1
    :goto_147
    if-eqz v4, :cond_17e
    invoke-virtual/range {p1 .. p1}, LTest;->c()J
    move-result-wide v4
    move-object/from16 v0, p0
    iget-wide v6, v0, LTestObject;->n:J
    add-long/2addr v4, v6
    const-wide/16 v6, 0x8
    sub-long/2addr v4, v6
    move-object/from16 v0, p0
    iget-object v6, v0, LTestObject;->k:Ljava/util/Stack;
    new-instance v7, LTest;
    move-object/from16 v0, p0
    iget v8, v0, LTestObject;->i:I
    invoke-direct {v7, v8, v4, v5}, LTest;-><init>(IJ)V
    invoke-virtual {v6, v7}, Ljava/util/Stack;->add(Ljava/lang/Object;)Z
    move-object/from16 v0, p0
    iget-wide v6, v0, LTestObject;->n:J
    move-object/from16 v0, p0
    iget v8, v0, LTestObject;->i:I
    int-to-long v8, v8
    cmp-long v6, v6, v8
    if-nez v6, :cond_17a
    move-object/from16 v0, p0
    invoke-direct {v0, v4, v5}, LTestObject;->a(J)V
    goto :goto_11b
    :cond_178
    const/4 v4, 0x0
    goto :goto_147
    :cond_17a
    invoke-direct/range {p0 .. p0}, LTestObject;->a()V
    goto :goto_11b
    :cond_17e
    move-object/from16 v0, p0
    iget v4, v0, LTestObject;->i:I
    sget v5, LTest;->R:I
    if-eq v4, v5, :cond_1d2
    sget v5, LTest;->Q:I
    if-eq v4, v5, :cond_1d2
    sget v5, LTest;->B:I
    if-eq v4, v5, :cond_1d2
    sget v5, LTest;->z:I
    if-eq v4, v5, :cond_1d2
    sget v5, LTest;->S:I
    if-eq v4, v5, :cond_1d2
    sget v5, LTest;->v:I
    if-eq v4, v5, :cond_1d2
    sget v5, LTest;->w:I
    if-eq v4, v5, :cond_1d2
    sget v5, LTest;->N:I
    if-eq v4, v5, :cond_1d2
    sget v5, LTest;->x:I
    if-eq v4, v5, :cond_1d2
    sget v5, LTest;->y:I
    if-eq v4, v5, :cond_1d2
    sget v5, LTest;->T:I
    if-eq v4, v5, :cond_1d2
    sget v5, LTest;->ab:I
    if-eq v4, v5, :cond_1d2
    sget v5, LTest;->ac:I
    if-eq v4, v5, :cond_1d2
    sget v5, LTest;->ag:I
    if-eq v4, v5, :cond_1d2
    sget v5, LTest;->ad:I
    if-eq v4, v5, :cond_1d2
    sget v5, LTest;->ae:I
    if-eq v4, v5, :cond_1d2
    sget v5, LTest;->af:I
    if-eq v4, v5, :cond_1d2
    sget v5, LTest;->P:I
    if-eq v4, v5, :cond_1d2
    sget v5, LTest;->M:I
    if-eq v4, v5, :cond_1d2
    sget v5, LTest;->aD:I
    if-ne v4, v5, :cond_1e5
    :cond_1d2
    const/4 v4, 0x1
    :goto_1d3
    if-eqz v4, :cond_222
    move-object/from16 v0, p0
    iget v4, v0, LTestObject;->i:I
    const/16 v5, 0x8
    if-eq v4, v5, :cond_1e7
    new-instance v4, LTest;
    const-string v5, "a"
    invoke-direct {v4, v5}, LTest;-><init>(Ljava/lang/String;)V
    throw v4
    :cond_1e5
    const/4 v4, 0x0
    goto :goto_1d3
    :cond_1e7
    move-object/from16 v0, p0
    iget-wide v4, v0, LTestObject;->n:J
    const-wide/32 v6, 0x7fffffff
    cmp-long v4, v4, v6
    if-lez v4, :cond_1fa
    new-instance v4, LTest;
    const-string v5, "a"
    invoke-direct {v4, v5}, LTest;-><init>(Ljava/lang/String;)V
    throw v4
    :cond_1fa
    new-instance v4, LTest;
    move-object/from16 v0, p0
    iget-wide v6, v0, LTestObject;->n:J
    long-to-int v5, v6
    invoke-direct {v4, v5}, LTest;-><init>(I)V
    move-object/from16 v0, p0
    iput-object v4, v0, LTestObject;->o:LTest;
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->o:LTest;
    iget-object v4, v4, LTest;->a:[B
    const/4 v5, 0x0
    move-object/from16 v0, p0
    iget-object v6, v0, LTestObject;->o:LTest;
    iget-object v6, v6, LTest;->a:[B
    const/4 v7, 0x0
    const/16 v8, 0x8
    invoke-static {v4, v5, v6, v7, v8}, Ljava/lang/System;->arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V
    const/4 v4, 0x1
    move-object/from16 v0, p0
    iput v4, v0, LTestObject;->i:I
    goto/16 :goto_11b
    :cond_222
    move-object/from16 v0, p0
    iget-wide v4, v0, LTestObject;->n:J
    const-wide/32 v6, 0x7fffffff
    cmp-long v4, v4, v6
    if-lez v4, :cond_235
    new-instance v4, LTest;
    const-string v5, "a"
    invoke-direct {v4, v5}, LTest;-><init>(Ljava/lang/String;)V
    throw v4
    :cond_235
    const/4 v4, 0x0
    move-object/from16 v0, p0
    iput-object v4, v0, LTestObject;->o:LTest;
    const/4 v4, 0x1
    move-object/from16 v0, p0
    iput v4, v0, LTestObject;->i:I
    goto/16 :goto_11b
    :pswitch_241
    move-object/from16 v0, p0
    iget-wide v4, v0, LTestObject;->n:J
    long-to-int v4, v4
    move-object/from16 v0, p0
    iget v5, v0, LTestObject;->i:I
    sub-int/2addr v4, v5
    move-object/from16 v0, p0
    iget-object v5, v0, LTestObject;->o:LTest;
    if-eqz v5, :cond_368
    move-object/from16 v0, p0
    iget-object v5, v0, LTestObject;->o:LTest;
    iget-object v5, v5, LTest;->a:[B
    const/16 v6, 0x8
    move-object/from16 v0, p1
    invoke-virtual {v0, v5, v6, v4}, LTest;->b([BII)V
    new-instance v5, LTest;
    move-object/from16 v0, p0
    iget v4, v0, LTestObject;->i:I
    move-object/from16 v0, p0
    iget-object v6, v0, LTestObject;->o:LTest;
    invoke-direct {v5, v4, v6}, LTest;-><init>(ILTest;)V
    invoke-virtual/range {p1 .. p1}, LTest;->c()J
    move-result-wide v10
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->k:Ljava/util/Stack;
    invoke-virtual {v4}, Ljava/util/Stack;->isEmpty()Z
    move-result v4
    if-nez v4, :cond_291
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->k:Ljava/util/Stack;
    invoke-virtual {v4}, Ljava/util/Stack;->peek()Ljava/lang/Object;
    move-result-object v4
    check-cast v4, LTest;
    invoke-virtual {v4, v5}, LTest;->a(LTest;)V
    :cond_286
    :goto_286
    invoke-virtual/range {p1 .. p1}, LTest;->c()J
    move-result-wide v4
    move-object/from16 v0, p0
    invoke-direct {v0, v4, v5}, LTestObject;->a(J)V
    goto/16 :goto_0
    :cond_291
    iget v4, v5, LTest;->aL:I
    sget v6, LTest;->z:I
    if-ne v4, v6, :cond_359
    iget-object v0, v5, LTest;->aM:LTest;
    move-object/from16 v16, v0
    const/16 v4, 0x8
    move-object/from16 v0, v16
    invoke-virtual {v0, v4}, LTest;->c(I)V
    invoke-virtual/range {v16 .. v16}, LTest;->j()I
    move-result v4
    invoke-static {v4}, LTest;->a(I)I
    move-result v4
    const/4 v5, 0x4
    move-object/from16 v0, v16
    invoke-virtual {v0, v5}, LTest;->d(I)V
    invoke-virtual/range {v16 .. v16}, LTest;->h()J
    move-result-wide v8
    if-nez v4, :cond_304
    invoke-virtual/range {v16 .. v16}, LTest;->h()J
    move-result-wide v6
    invoke-virtual/range {v16 .. v16}, LTest;->h()J
    move-result-wide v4
    add-long/2addr v4, v10
    move-wide v10, v4
    move-wide v4, v6
    :goto_2c1
    const/4 v6, 0x2
    move-object/from16 v0, v16
    invoke-virtual {v0, v6}, LTest;->d(I)V
    invoke-virtual/range {v16 .. v16}, LTest;->e()I
    move-result v17
    move/from16 v0, v17
    new-array v0, v0, [I
    move-object/from16 v18, v0
    move/from16 v0, v17
    new-array v0, v0, [J
    move-object/from16 v19, v0
    move/from16 v0, v17
    new-array v0, v0, [J
    move-object/from16 v20, v0
    move/from16 v0, v17
    new-array v0, v0, [J
    move-object/from16 v21, v0
    const-wide/32 v6, 0xf4240
    invoke-static/range {v4 .. v9}, LTest;->a(JJJ)J
    move-result-wide v12
    const/4 v6, 0x0
    move-wide v14, v10
    move v10, v6
    move-wide v6, v4
    move-wide v4, v12
    :goto_2ef
    move/from16 v0, v17
    if-ge v10, v0, :cond_33e
    invoke-virtual/range {v16 .. v16}, LTest;->j()I
    move-result v11
    const/high16 v12, -0x80000000
    and-int/2addr v12, v11
    if-eqz v12, :cond_310
    new-instance v4, LTest;
    const-string v5, "a"
    invoke-direct {v4, v5}, LTest;-><init>(Ljava/lang/String;)V
    throw v4
    :cond_304
    invoke-virtual/range {v16 .. v16}, LTest;->p()J
    move-result-wide v6
    invoke-virtual/range {v16 .. v16}, LTest;->p()J
    move-result-wide v4
    add-long/2addr v4, v10
    move-wide v10, v4
    move-wide v4, v6
    goto :goto_2c1
    :cond_310
    invoke-virtual/range {v16 .. v16}, LTest;->h()J
    move-result-wide v12
    const v22, 0x7fffffff
    and-int v11, v11, v22
    aput v11, v18, v10
    aput-wide v14, v19, v10
    aput-wide v4, v21, v10
    add-long v4, v6, v12
    const-wide/32 v6, 0xf4240
    invoke-static/range {v4 .. v9}, LTest;->a(JJJ)J
    move-result-wide v12
    aget-wide v6, v21, v10
    sub-long v6, v12, v6
    aput-wide v6, v20, v10
    const/4 v6, 0x4
    move-object/from16 v0, v16
    invoke-virtual {v0, v6}, LTest;->d(I)V
    aget v6, v18, v10
    int-to-long v6, v6
    add-long/2addr v14, v6
    add-int/lit8 v6, v10, 0x1
    move v10, v6
    move-wide v6, v4
    move-wide v4, v12
    goto :goto_2ef
    :cond_33e
    new-instance v4, LTest;
    move-object/from16 v0, v18
    move-object/from16 v1, v19
    move-object/from16 v2, v20
    move-object/from16 v3, v21
    invoke-direct {v4, v0, v1, v2, v3}, LTest;-><init>([I[J[J[J)V
    move-object/from16 v0, p0
    iget-object v5, v0, LTestObject;->o:LTest;
    invoke-virtual {v5, v4}, LTest;->a(LTest;)V
    const/4 v4, 0x1
    move-object/from16 v0, p0
    iput-boolean v4, v0, LTestObject;->w:Z
    goto/16 :goto_286
    :cond_359
    iget v4, v5, LTest;->aL:I
    sget v6, LTest;->aD:I
    if-ne v4, v6, :cond_286
    iget-object v4, v5, LTest;->aM:LTest;
    move-object/from16 v0, p0
    invoke-virtual {v0, v4}, LTestObject;->a(LTest;)V
    goto/16 :goto_286
    :cond_368
    move-object/from16 v0, p1
    invoke-virtual {v0, v4}, LTest;->b(I)V
    goto/16 :goto_286
    :pswitch_36f
    const/4 v5, 0x0
    const-wide v6, 0x7fffffffffffffffL
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->o:LTest;
    invoke-virtual {v4}, LTest;->size()I
    move-result v9
    const/4 v4, 0x0
    move v8, v4
    :goto_37f
    if-ge v8, v9, :cond_3b1
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->o:LTest;
    invoke-virtual {v4, v8}, LTest;->valueAt(I)Ljava/lang/Object;
    move-result-object v4
    check-cast v4, LTest;
    iget-object v4, v4, LTest;->a:LTest;
    iget-boolean v10, v4, LTest;->m:Z
    if-eqz v10, :cond_602
    iget-wide v10, v4, LTest;->c:J
    cmp-long v10, v10, v6
    if-gez v10, :cond_602
    iget-wide v6, v4, LTest;->c:J
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->o:LTest;
    invoke-virtual {v4, v8}, LTest;->valueAt(I)Ljava/lang/Object;
    move-result-object v4
    check-cast v4, LTest;
    move-wide/from16 v23, v6
    move-object v6, v4
    move-wide/from16 v4, v23
    :goto_3a8
    add-int/lit8 v7, v8, 0x1
    move v8, v7
    move-wide/from16 v23, v4
    move-object v5, v6
    move-wide/from16 v6, v23
    goto :goto_37f
    :cond_3b1
    if-nez v5, :cond_3ba
    const/4 v4, 0x3
    move-object/from16 v0, p0
    iput v4, v0, LTestObject;->i:I
    goto/16 :goto_0
    :cond_3ba
    invoke-virtual/range {p1 .. p1}, LTest;->c()J
    move-result-wide v8
    sub-long/2addr v6, v8
    long-to-int v4, v6
    if-gez v4, :cond_3ca
    new-instance v4, LTest;
    const-string v5, "a"
    invoke-direct {v4, v5}, LTest;-><init>(Ljava/lang/String;)V
    throw v4
    :cond_3ca
    move-object/from16 v0, p1
    invoke-virtual {v0, v4}, LTest;->b(I)V
    iget-object v4, v5, LTest;->a:LTest;
    iget-object v5, v4, LTest;->l:LTest;
    iget-object v5, v5, LTest;->a:[B
    const/4 v6, 0x0
    iget v7, v4, LTest;->k:I
    move-object/from16 v0, p1
    invoke-virtual {v0, v5, v6, v7}, LTest;->b([BII)V
    iget-object v5, v4, LTest;->l:LTest;
    const/4 v6, 0x0
    invoke-virtual {v5, v6}, LTest;->c(I)V
    const/4 v5, 0x0
    iput-boolean v5, v4, LTest;->m:Z
    goto/16 :goto_0
    :cond_3e8
    move-object/from16 v0, p0
    iput-object v5, v0, LTestObject;->o:LTest;
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->o:LTest;
    if-nez v4, :cond_414
    move-object/from16 v0, p0
    iget-wide v4, v0, LTestObject;->q:J
    invoke-virtual/range {p1 .. p1}, LTest;->c()J
    move-result-wide v6
    sub-long/2addr v4, v6
    long-to-int v4, v4
    if-gez v4, :cond_406
    new-instance v4, LTest;
    const-string v5, ""
    invoke-direct {v4, v5}, LTest;-><init>(Ljava/lang/String;)V
    throw v4
    :cond_406
    move-object/from16 v0, p1
    invoke-virtual {v0, v4}, LTest;->b(I)V
    invoke-direct/range {p0 .. p0}, LTestObject;->a()V
    const/4 v4, 0x0
    :goto_40f
    if-eqz v4, :cond_0
    const/4 v4, 0x0
    goto/16 :goto_63
    :cond_414
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->o:LTest;
    iget-object v4, v4, LTest;->a:LTest;
    iget-wide v4, v4, LTest;->b:J
    invoke-virtual/range {p1 .. p1}, LTest;->c()J
    move-result-wide v6
    sub-long/2addr v4, v6
    long-to-int v4, v4
    if-gez v4, :cond_42c
    new-instance v4, LTest;
    const-string v5, ""
    invoke-direct {v4, v5}, LTest;-><init>(Ljava/lang/String;)V
    throw v4
    :cond_42c
    move-object/from16 v0, p1
    invoke-virtual {v0, v4}, LTest;->b(I)V
    :cond_431
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->o:LTest;
    iget-object v4, v4, LTest;->a:LTest;
    iget-object v4, v4, LTest;->e:[I
    move-object/from16 v0, p0
    iget-object v5, v0, LTestObject;->o:LTest;
    iget v5, v5, LTest;->e:I
    aget v4, v4, v5
    move-object/from16 v0, p0
    iput v4, v0, LTestObject;->i:I
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->o:LTest;
    iget-object v4, v4, LTest;->a:LTest;
    iget-boolean v4, v4, LTest;->i:Z
    if-eqz v4, :cond_553
    move-object/from16 v0, p0
    iget-object v5, v0, LTestObject;->o:LTest;
    iget-object v6, v5, LTest;->a:LTest;
    iget-object v7, v6, LTest;->l:LTest;
    iget-object v4, v6, LTest;->a:LTest;
    iget v4, v4, LTest;->a:I
    iget-object v8, v6, LTest;->n:LTest;
    if-eqz v8, :cond_534
    iget-object v4, v6, LTest;->n:LTest;
    :goto_461
    iget v8, v4, LTest;->a:I
    iget-object v4, v6, LTest;->j:[Z
    iget v6, v5, LTest;->e:I
    aget-boolean v6, v4, v6
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->o:LTest;
    iget-object v9, v4, LTest;->a:[B
    const/4 v10, 0x0
    if-eqz v6, :cond_53c
    const/16 v4, 0x80
    :goto_474
    or-int/2addr v4, v8
    int-to-byte v4, v4
    aput-byte v4, v9, v10
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->o:LTest;
    const/4 v9, 0x0
    invoke-virtual {v4, v9}, LTest;->c(I)V
    iget-object v4, v5, LTest;->b:LTest;
    move-object/from16 v0, p0
    iget-object v5, v0, LTestObject;->o:LTest;
    const/4 v9, 0x1
    invoke-virtual {v4, v5, v9}, LTest;->a(LTest;I)V
    invoke-virtual {v4, v7, v8}, LTest;->a(LTest;I)V
    if-nez v6, :cond_53f
    add-int/lit8 v4, v8, 0x1
    :goto_491
    move-object/from16 v0, p0
    iput v4, v0, LTestObject;->i:I
    move-object/from16 v0, p0
    iget v4, v0, LTestObject;->i:I
    move-object/from16 v0, p0
    iget v5, v0, LTestObject;->i:I
    add-int/2addr v4, v5
    move-object/from16 v0, p0
    iput v4, v0, LTestObject;->i:I
    :goto_4a2
    const/4 v4, 0x4
    move-object/from16 v0, p0
    iput v4, v0, LTestObject;->i:I
    const/4 v4, 0x0
    move-object/from16 v0, p0
    iput v4, v0, LTestObject;->i:I
    :cond_4ac
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->o:LTest;
    iget-object v12, v4, LTest;->a:LTest;
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->o:LTest;
    iget-object v9, v4, LTest;->c:LTest;
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->o:LTest;
    iget-object v5, v4, LTest;->b:LTest;
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->o:LTest;
    iget v8, v4, LTest;->e:I
    iget v4, v9, LTest;->n:I
    const/4 v6, -0x1
    if-eq v4, v6, :cond_57a
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->o:LTest;
    iget-object v4, v4, LTest;->a:[B
    const/4 v6, 0x0
    const/4 v7, 0x0
    aput-byte v7, v4, v6
    const/4 v6, 0x1
    const/4 v7, 0x0
    aput-byte v7, v4, v6
    const/4 v6, 0x2
    const/4 v7, 0x0
    aput-byte v7, v4, v6
    iget v4, v9, LTest;->n:I
    iget v6, v9, LTest;->n:I
    rsub-int/lit8 v6, v6, 0x4
    :goto_4e1
    move-object/from16 v0, p0
    iget v7, v0, LTestObject;->i:I
    move-object/from16 v0, p0
    iget v10, v0, LTestObject;->i:I
    if-ge v7, v10, :cond_59e
    move-object/from16 v0, p0
    iget v7, v0, LTestObject;->i:I
    if-nez v7, :cond_55a
    move-object/from16 v0, p0
    iget-object v7, v0, LTestObject;->o:LTest;
    iget-object v7, v7, LTest;->a:[B
    move-object/from16 v0, p1
    invoke-virtual {v0, v7, v6, v4}, LTest;->b([BII)V
    move-object/from16 v0, p0
    iget-object v7, v0, LTestObject;->o:LTest;
    const/4 v10, 0x0
    invoke-virtual {v7, v10}, LTest;->c(I)V
    move-object/from16 v0, p0
    iget-object v7, v0, LTestObject;->o:LTest;
    invoke-virtual {v7}, LTest;->n()I
    move-result v7
    move-object/from16 v0, p0
    iput v7, v0, LTestObject;->i:I
    move-object/from16 v0, p0
    iget-object v7, v0, LTestObject;->o:LTest;
    const/4 v10, 0x0
    invoke-virtual {v7, v10}, LTest;->c(I)V
    move-object/from16 v0, p0
    iget-object v7, v0, LTestObject;->o:LTest;
    const/4 v10, 0x4
    invoke-virtual {v5, v7, v10}, LTest;->a(LTest;I)V
    move-object/from16 v0, p0
    iget v7, v0, LTestObject;->i:I
    add-int/lit8 v7, v7, 0x4
    move-object/from16 v0, p0
    iput v7, v0, LTestObject;->i:I
    move-object/from16 v0, p0
    iget v7, v0, LTestObject;->i:I
    add-int/2addr v7, v6
    move-object/from16 v0, p0
    iput v7, v0, LTestObject;->i:I
    goto :goto_4e1
    :cond_534
    iget-object v8, v5, LTest;->c:LTest;
    iget-object v8, v8, LTest;->k:[LTest;
    aget-object v4, v8, v4
    goto/16 :goto_461
    :cond_53c
    const/4 v4, 0x0
    goto/16 :goto_474
    :cond_53f
    invoke-virtual {v7}, LTest;->e()I
    move-result v5
    const/4 v6, -0x2
    invoke-virtual {v7, v6}, LTest;->d(I)V
    mul-int/lit8 v5, v5, 0x6
    add-int/lit8 v5, v5, 0x2
    invoke-virtual {v4, v7, v5}, LTest;->a(LTest;I)V
    add-int/lit8 v4, v8, 0x1
    add-int/2addr v4, v5
    goto/16 :goto_491
    :cond_553
    const/4 v4, 0x0
    move-object/from16 v0, p0
    iput v4, v0, LTestObject;->i:I
    goto/16 :goto_4a2
    :cond_55a
    move-object/from16 v0, p0
    iget v7, v0, LTestObject;->i:I
    const/4 v10, 0x0
    move-object/from16 v0, p1
    invoke-virtual {v5, v0, v7, v10}, LTest;->a(LTest;IZ)I
    move-result v7
    move-object/from16 v0, p0
    iget v10, v0, LTestObject;->i:I
    add-int/2addr v10, v7
    move-object/from16 v0, p0
    iput v10, v0, LTestObject;->i:I
    move-object/from16 v0, p0
    iget v10, v0, LTestObject;->i:I
    sub-int v7, v10, v7
    move-object/from16 v0, p0
    iput v7, v0, LTestObject;->i:I
    goto/16 :goto_4e1
    :cond_57a
    :goto_57a
    move-object/from16 v0, p0
    iget v4, v0, LTestObject;->i:I
    move-object/from16 v0, p0
    iget v6, v0, LTestObject;->i:I
    if-ge v4, v6, :cond_59e
    move-object/from16 v0, p0
    iget v4, v0, LTestObject;->i:I
    move-object/from16 v0, p0
    iget v6, v0, LTestObject;->i:I
    sub-int/2addr v4, v6
    const/4 v6, 0x0
    move-object/from16 v0, p1
    invoke-virtual {v5, v0, v4, v6}, LTest;->a(LTest;IZ)I
    move-result v4
    move-object/from16 v0, p0
    iget v6, v0, LTestObject;->i:I
    add-int/2addr v4, v6
    move-object/from16 v0, p0
    iput v4, v0, LTestObject;->i:I
    goto :goto_57a
    :cond_59e
    iget-object v4, v12, LTest;->g:[J
    aget-wide v6, v4, v8
    iget-object v4, v12, LTest;->f:[I
    aget v4, v4, v8
    int-to-long v10, v4
    add-long/2addr v6, v10
    const-wide/16 v10, 0x3e8
    mul-long/2addr v6, v10
    iget-boolean v4, v12, LTest;->i:Z
    if-eqz v4, :cond_5f3
    const/4 v4, 0x2
    :goto_5b0
    iget-object v10, v12, LTest;->h:[Z
    aget-boolean v8, v10, v8
    if-eqz v8, :cond_5f5
    const/4 v8, 0x1
    :goto_5b7
    or-int/2addr v8, v4
    iget-object v4, v12, LTest;->a:LTest;
    iget v4, v4, LTest;->a:I
    const/4 v11, 0x0
    iget-boolean v10, v12, LTest;->i:Z
    if-eqz v10, :cond_5ca
    iget-object v10, v12, LTest;->n:LTest;
    if-eqz v10, :cond_5f7
    iget-object v4, v12, LTest;->n:LTest;
    iget-object v4, v4, LTest;->b:[B
    :goto_5c9
    move-object v11, v4
    :cond_5ca
    move-object/from16 v0, p0
    iget v9, v0, LTestObject;->i:I
    const/4 v10, 0x0
    invoke-virtual/range {v5 .. v11}, LTest;->a(JIII[B)V
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->o:LTest;
    iget v5, v4, LTest;->e:I
    add-int/lit8 v5, v5, 0x1
    iput v5, v4, LTest;->e:I
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->o:LTest;
    iget v4, v4, LTest;->e:I
    iget v5, v12, LTest;->d:I
    if-ne v4, v5, :cond_5eb
    const/4 v4, 0x0
    move-object/from16 v0, p0
    iput-object v4, v0, LTestObject;->o:LTest;
    :cond_5eb
    const/4 v4, 0x3
    move-object/from16 v0, p0
    iput v4, v0, LTestObject;->i:I
    const/4 v4, 0x1
    goto/16 :goto_40f
    :cond_5f3
    const/4 v4, 0x0
    goto :goto_5b0
    :cond_5f5
    const/4 v8, 0x0
    goto :goto_5b7
    :cond_5f7
    iget-object v9, v9, LTest;->k:[LTest;
    aget-object v4, v9, v4
    iget-object v4, v4, LTest;->b:[B
    goto :goto_5c9
    :cond_5fe
    move-object v6, v5
    move-wide v4, v8
    goto/16 :goto_41
    :cond_602
    move-wide/from16 v23, v6
    move-object v6, v5
    move-wide/from16 v4, v23
    goto/16 :goto_3a8
    nop
    :pswitch_data_60a
    .packed-switch 0x0
        :pswitch_47
        :pswitch_241
        :pswitch_36f
    .end packed-switch
.end method

.method private final a(J)V
    .registers 3
    .prologue
    return-void
.end method

