# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

.class public final LTestObject;
.super Ljava/lang/Object;

.method public static a(LTest;LTest;LTest;LTest;)V
    .registers 40
    .prologue
    invoke-virtual/range {p2 .. p2}, LTest;->e()LTest;
    move-result-object v2
    iget-wide v0, v2, LTest;->l:J
    move-wide/from16 v22, v0
    invoke-virtual/range {p2 .. p2}, LTest;->e()LTest;
    move-result-object v2
    iget-wide v2, v2, LTest;->l:J
    const-wide/16 v4, 0x3e8
    mul-long/2addr v2, v4
    const-wide/16 v4, 0x3e8
    mul-long/2addr v2, v4
    div-long v2, v2, v22
    move-object/from16 v0, p0
    iput-wide v2, v0, LTest;->l:J
    invoke-virtual/range {p1 .. p1}, LTest;->e()LTest;
    move-result-object v2
    iget-wide v4, v2, LTest;->d:D
    double-to-int v3, v4
    move-object/from16 v0, p0
    iput v3, v0, LTest;->i:I
    iget-wide v4, v2, LTest;->d:D
    double-to-int v3, v4
    move-object/from16 v0, p0
    iput v3, v0, LTest;->i:I
    iget-object v2, v2, LTest;->h:LTest;
    invoke-static {v2}, LTest;->a(LTest;)I
    move-result v2
    move-object/from16 v0, p0
    iput v2, v0, LTest;->i:I
    const/4 v3, 0x0
    invoke-virtual/range {p3 .. p3}, LTest;->e()LTest;
    move-result-object v2
    if-eqz v2, :cond_59
    invoke-virtual/range {p3 .. p3}, LTest;->e()LTest;
    move-result-object v2
    iget-object v2, v2, LTest;->a:Ljava/util/List;
    invoke-interface {v2}, Ljava/util/List;->iterator()Ljava/util/Iterator;
    move-result-object v4
    :cond_47
    invoke-interface {v4}, Ljava/util/Iterator;->hasNext()Z
    move-result v2
    if-eqz v2, :cond_59
    invoke-interface {v4}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v2
    check-cast v2, LTest;
    iget v2, v2, LTest;->i:I
    if-eqz v2, :cond_47
    const/4 v2, 0x1
    move v3, v2
    :cond_59
    move-object/from16 v0, p0
    iput-boolean v3, v0, LTest;->b:Z
    invoke-virtual/range {p3 .. p3}, LTest;->e()LTest;
    move-result-object v2
    iget-object v0, v2, LTest;->a:Ljava/util/List;
    move-object/from16 v17, v0
    const/4 v2, 0x0
    invoke-interface/range {v17 .. v17}, Ljava/util/List;->iterator()Ljava/util/Iterator;
    move-result-object v5
    move v4, v2
    :goto_6b
    invoke-interface {v5}, Ljava/util/Iterator;->hasNext()Z
    move-result v2
    if-eqz v2, :cond_8c
    invoke-interface {v5}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v2
    check-cast v2, LTest;
    iget-wide v6, v2, LTest;->l:J
    const-wide/16 v8, 0x0
    cmp-long v2, v6, v8
    if-gez v2, :cond_87
    new-instance v2, LTest;
    const-string v3, "string"
    invoke-direct {v2, v3}, LTest;-><init>(Ljava/lang/String;)V
    throw v2
    :cond_87
    int-to-long v8, v4
    add-long/2addr v6, v8
    long-to-int v2, v6
    move v4, v2
    goto :goto_6b
    :cond_8c
    if-gtz v4, :cond_96
    new-instance v2, LTest;
    const-string v3, "string"
    invoke-direct {v2, v3}, LTest;-><init>(Ljava/lang/String;)V
    throw v2
    :cond_96
    invoke-virtual/range {p3 .. p3}, LTest;->e()LTest;
    move-result-object v5
    const/4 v2, 0x0
    if-eqz v5, :cond_260
    iget-object v2, v5, LTest;->al:[J
    if-eqz v2, :cond_a4
    array-length v5, v2
    if-nez v5, :cond_ac
    :cond_a4
    new-instance v2, LTest;
    const-string v3, "string"
    invoke-direct {v2, v3}, LTest;-><init>(Ljava/lang/String;)V
    throw v2
    :cond_ac
    array-length v5, v2
    add-int/lit8 v5, v5, -0x1
    aget-wide v6, v2, v5
    int-to-long v8, v4
    cmp-long v5, v6, v8
    if-lez v5, :cond_be
    new-instance v2, LTest;
    const-string v3, "string"
    invoke-direct {v2, v3}, LTest;-><init>(Ljava/lang/String;)V
    throw v2
    :cond_be
    move-object v5, v2
    :goto_bf
    const/4 v2, 0x0
    const/4 v6, 0x0
    invoke-virtual/range {p3 .. p3}, LTest;->e()LTest;
    move-result-object v7
    if-eqz v7, :cond_f2
    iget-object v7, v7, LTest;->a:Ljava/util/List;
    invoke-interface {v7}, Ljava/util/List;->iterator()Ljava/util/Iterator;
    move-result-object v8
    move v6, v2
    :goto_ce
    invoke-interface {v8}, Ljava/util/Iterator;->hasNext()Z
    move-result v2
    if-eqz v2, :cond_f0
    invoke-interface {v8}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v2
    check-cast v2, LTest;
    iget v2, v2, LTest;->i:I
    int-to-long v10, v2
    const-wide/16 v12, 0x0
    cmp-long v2, v10, v12
    if-gez v2, :cond_eb
    new-instance v2, LTest;
    const-string v3, "string"
    invoke-direct {v2, v3}, LTest;-><init>(Ljava/lang/String;)V
    throw v2
    :cond_eb
    int-to-long v12, v6
    add-long/2addr v10, v12
    long-to-int v2, v10
    move v6, v2
    goto :goto_ce
    :cond_f0
    move v2, v6
    move-object v6, v7
    :cond_f2
    if-eqz v2, :cond_fe
    if-eq v2, v4, :cond_fe
    new-instance v2, LTest;
    const-string v3, "string"
    invoke-direct {v2, v3}, LTest;-><init>(Ljava/lang/String;)V
    throw v2
    :cond_fe
    if-eqz v5, :cond_149
    new-instance v2, LTest;
    array-length v7, v5
    invoke-direct {v2, v4, v7}, LTest;-><init>(II)V
    move-object v4, v2
    :goto_107
    iget-object v0, v4, LTest;->al:[J
    move-object/from16 v24, v0
    iget-object v0, v4, LTest;->ai:[I
    move-object/from16 v25, v0
    const/16 v16, 0x0
    const/4 v7, -0x1
    const-wide/16 v14, 0x0
    if-eqz v6, :cond_150
    invoke-interface {v6}, Ljava/util/List;->size()I
    move-result v2
    if-lez v2, :cond_150
    invoke-interface {v6}, Ljava/util/List;->iterator()Ljava/util/Iterator;
    move-result-object v2
    move-object v6, v2
    :goto_121
    const-wide/16 v12, 0x0
    const-wide/16 v10, 0x0
    const-wide/16 v8, 0x0
    invoke-interface/range {v17 .. v17}, Ljava/util/List;->iterator()Ljava/util/Iterator;
    move-result-object v26
    :cond_12b
    invoke-interface/range {v26 .. v26}, Ljava/util/Iterator;->hasNext()Z
    move-result v2
    if-eqz v2, :cond_22d
    invoke-interface/range {v26 .. v26}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v2
    check-cast v2, LTest;
    iget-wide v0, v2, LTest;->l:J
    move-wide/from16 v28, v0
    const-wide/16 v18, 0x0
    cmp-long v17, v28, v18
    if-gez v17, :cond_153
    new-instance v2, LTest;
    const-string v3, "string"
    invoke-direct {v2, v3}, LTest;-><init>(Ljava/lang/String;)V
    throw v2
    :cond_149
    new-instance v2, LTest;
    invoke-direct {v2, v4}, LTest;-><init>(I)V
    move-object v4, v2
    goto :goto_107
    :cond_150
    const/4 v2, 0x0
    move-object v6, v2
    goto :goto_121
    :cond_153
    iget-wide v0, v2, LTest;->l:J
    move-wide/from16 v18, v0
    move-wide/from16 v20, v18
    :goto_159
    const-wide/16 v18, 0x0
    cmp-long v2, v20, v18
    if-lez v2, :cond_12b
    if-eqz v6, :cond_192
    move-wide/from16 v18, v12
    :goto_163
    const-wide/16 v12, 0x0
    cmp-long v2, v18, v12
    if-gtz v2, :cond_178
    invoke-interface {v6}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v2
    check-cast v2, LTest;
    iget v10, v2, LTest;->i:I
    int-to-long v0, v10
    move-wide/from16 v18, v0
    iget v2, v2, LTest;->i:I
    int-to-long v10, v2
    goto :goto_163
    :cond_178
    if-nez v16, :cond_17b
    move-wide v8, v10
    :cond_17b
    add-long v12, v14, v10
    sub-long/2addr v12, v8
    move-wide/from16 v34, v12
    move-wide v12, v10
    move-wide v10, v8
    move-wide/from16 v8, v34
    :goto_184
    const-wide/16 v30, 0x0
    cmp-long v2, v8, v30
    if-gez v2, :cond_198
    new-instance v2, LTest;
    const-string v3, "string"
    invoke-direct {v2, v3}, LTest;-><init>(Ljava/lang/String;)V
    throw v2
    :cond_192
    move-wide/from16 v18, v12
    move-wide v12, v10
    move-wide v10, v8
    move-wide v8, v14
    goto :goto_184
    :cond_198
    const-wide/16 v30, 0x3e8
    mul-long v8, v8, v30
    const-wide/16 v30, 0x3e8
    mul-long v8, v8, v30
    div-long v8, v8, v22
    move/from16 v2, v16
    :goto_1a4
    if-lez v2, :cond_1cb
    add-int/lit8 v17, v2, -0x1
    aget-wide v30, v24, v17
    cmp-long v17, v30, v8
    if-lez v17, :cond_1cb
    add-int/lit8 v17, v2, -0x1
    aget-wide v30, v24, v17
    aput-wide v30, v24, v2
    if-eqz v25, :cond_1c8
    if-ltz v7, :cond_1c8
    add-int/lit8 v17, v2, -0x1
    aget v27, v25, v7
    move/from16 v0, v17
    move/from16 v1, v27
    if-ne v0, v1, :cond_1c8
    aget v17, v25, v7
    add-int/lit8 v17, v17, 0x1
    aput v17, v25, v7
    :cond_1c8
    add-int/lit8 v2, v2, -0x1
    goto :goto_1a4
    :cond_1cb
    aput-wide v8, v24, v2
    if-lez v2, :cond_1ea
    add-int/lit8 v17, v2, -0x1
    aget-wide v30, v24, v17
    cmp-long v8, v30, v8
    if-nez v8, :cond_1ea
    const/4 v3, 0x1
    if-ne v2, v3, :cond_1e2
    new-instance v2, LTest;
    const-string v3, "string"
    invoke-direct {v2, v3}, LTest;-><init>(Ljava/lang/String;)V
    throw v2
    :cond_1e2
    new-instance v2, LTest;
    const-string v3, "string"
    invoke-direct {v2, v3}, LTest;-><init>(Ljava/lang/String;)V
    throw v2
    :cond_1ea
    if-eqz v5, :cond_216
    add-int/lit8 v8, v7, 0x1
    array-length v9, v5
    if-ge v8, v9, :cond_216
    move/from16 v0, v16
    int-to-long v8, v0
    add-int/lit8 v17, v7, 0x1
    aget-wide v30, v5, v17
    const-wide/16 v32, 0x1
    sub-long v30, v30, v32
    cmp-long v8, v8, v30
    if-nez v8, :cond_216
    add-int/lit8 v7, v7, 0x1
    aput v2, v25, v7
    if-lez v7, :cond_216
    add-int/lit8 v2, v7, -0x1
    aget v2, v25, v2
    aget v8, v25, v7
    if-lt v2, v8, :cond_216
    new-instance v2, LTest;
    const-string v3, "string"
    invoke-direct {v2, v3}, LTest;-><init>(Ljava/lang/String;)V
    throw v2
    :cond_216
    add-int/lit8 v2, v16, 0x1
    add-long v16, v14, v28
    const-wide/16 v8, 0x1
    sub-long v14, v18, v8
    const-wide/16 v8, 0x1
    sub-long v8, v20, v8
    move-wide/from16 v20, v8
    move-wide v8, v10
    move-wide v10, v12
    move-wide v12, v14
    move-wide/from16 v14, v16
    move/from16 v16, v2
    goto/16 :goto_159
    :cond_22d
    iget-object v2, v4, LTest;->al:[J
    move-object/from16 v0, p0
    iput-object v2, v0, LTest;->al:[J
    iget-object v2, v4, LTest;->ai:[I
    if-eqz v3, :cond_241
    if-nez v2, :cond_241
    new-instance v2, LTest;
    const-string v3, "string"
    invoke-direct {v2, v3}, LTest;-><init>(Ljava/lang/String;)V
    throw v2
    :cond_241
    if-eqz v2, :cond_25b
    array-length v3, v2
    if-gtz v3, :cond_24e
    new-instance v2, LTest;
    const-string v3, "string"
    invoke-direct {v2, v3}, LTest;-><init>(Ljava/lang/String;)V
    throw v2
    :cond_24e
    const/4 v3, 0x0
    aget v3, v2, v3
    if-eqz v3, :cond_25b
    new-instance v2, LTest;
    const-string v3, "string"
    invoke-direct {v2, v3}, LTest;-><init>(Ljava/lang/String;)V
    throw v2
    :cond_25b
    move-object/from16 v0, p0
    iput-object v2, v0, LTest;->ai:[I
    return-void
    :cond_260
    move-object v5, v2
    goto/16 :goto_bf
.end method