# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

.class public final LTestObject;
.super Ljava/lang/Object;

.field public a:LTest;
.field public b:Ljava/util/List;

.method public final a(LTest;LTest;LTest;LTest;Z)LTest;
    .registers 34
    move-object/from16 v0, p0
    iget-object v12, v0, LTestObject;->a:LTest;
    iget-object v4, v12, LTest;->a:LTest;
    invoke-virtual {v4}, LTest;->b()J
    move-result-wide v14
    const/4 v10, 0x0
    const/4 v4, 0x0
    if-eqz p2, :cond_9cf
    move-object/from16 v0, p2
    iget-object v10, v0, LTest;->a:LTest;
    move-object/from16 v0, p2
    iget-object v4, v0, LTest;->b:Ljava/util/List;
    move-object v13, v4
    :goto_17
    if-eqz v10, :cond_1f
    invoke-virtual {v10}, LTest;->d()Z
    move-result v4
    if-nez v4, :cond_21d
    :cond_1f
    new-instance v4, LTest;
    const/4 v5, 0x0
    const/4 v6, 0x0
    sget-object v7, LTest;->P:LTest;
    const-wide/16 v8, 0x0
    const/4 v11, 0x0
    invoke-direct/range {v4 .. v11}, LTest;-><init>(ILTest;LTest;JLTest;Ljava/util/Map;)V
    move-object v14, v4
    :goto_2c
    if-nez p4, :cond_44
    iget-object v4, v14, LTest;->a:LTest;
    sget-object v5, LTest;->P:LTest;
    if-ne v4, v5, :cond_44
    iget-object v4, v14, LTest;->a:LTest;
    new-instance p4, LTest;
    iget v5, v4, LTest;->c:I
    iget v4, v4, LTest;->d:I
    const v6, 0x2faf080
    move-object/from16 v0, p4
    invoke-direct {v0, v5, v4, v6}, LTest;-><init>(III)V
    :cond_44
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->a:LTest;
    invoke-virtual {v4}, LTest;->a()Z
    move-result v5
    if-eqz v5, :cond_6c5
    iget-object v8, v4, LTest;->a:LTest;
    if-nez p1, :cond_61e
    const/4 v4, 0x0
    :cond_53
    :goto_53
    if-eqz v4, :cond_6c8
    move-object v12, v4
    :goto_56
    iget-object v4, v12, LTest;->a:LTest;
    sget-object v5, LTest;->P:LTest;
    if-ne v4, v5, :cond_931
    const/4 v4, 0x1
    :goto_5d
    iget-object v5, v14, LTest;->a:LTest;
    sget-object v6, LTest;->P:LTest;
    if-ne v5, v6, :cond_934
    const/4 v5, 0x1
    :goto_64
    if-nez v4, :cond_937
    if-nez v5, :cond_937
    const/4 v4, 0x0
    move-object v13, v4
    :goto_6a
    sget-object v4, LTest;->P:LTest;
    invoke-static {v4}, LTest;->a(LTest;)Z
    move-result v4
    if-eqz v4, :cond_212
    const/4 v5, 0x0
    const/4 v4, 0x0
    if-eqz v13, :cond_9c8
    if-ne v13, v14, :cond_976
    const/4 v5, 0x1
    move v11, v4
    move v15, v5
    :goto_7b
    if-eqz v12, :cond_97b
    iget-object v4, v12, LTest;->a:LTest;
    sget-object v5, LTest;->P:LTest;
    if-ne v4, v5, :cond_97b
    const/4 v4, 0x1
    :goto_84
    if-eqz v4, :cond_9c
    const/4 v7, 0x0
    iget v4, v12, LTest;->a:I
    packed-switch v4, :pswitch_data_9d2
    :goto_8c
    :pswitch_8c
    if-eqz v7, :cond_9c
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->a:LTest;
    const-string v5, ""
    const-string v6, ""
    const-wide/16 v8, 0x1
    const/4 v10, 0x1
    invoke-interface/range {v4 .. v10}, LTest;->a(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JZ)V
    :cond_9c
    iget-object v4, v14, LTest;->a:LTest;
    sget-object v5, LTest;->P:LTest;
    if-ne v4, v5, :cond_98e
    const/4 v4, 0x1
    :goto_a3
    if-eqz v4, :cond_bb
    const/4 v7, 0x0
    iget v4, v14, LTest;->a:I
    packed-switch v4, :pswitch_data_9e0
    :goto_ab
    if-eqz v7, :cond_bb
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->a:LTest;
    const-string v5, ""
    const-string v6, ""
    const-wide/16 v8, 0x1
    const/4 v10, 0x1
    invoke-interface/range {v4 .. v10}, LTest;->a(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JZ)V
    :cond_bb
    if-eqz v15, :cond_999
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->a:LTest;
    const-string v5, ""
    const-string v6, ""
    const-string v7, ""
    const-wide/16 v8, 0x1
    const/4 v10, 0x1
    invoke-interface/range {v4 .. v10}, LTest;->a(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JZ)V
    :goto_cd
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->a:LTest;
    if-eqz v4, :cond_119
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->a:LTest;
    move-object/from16 v0, p0
    iget-object v5, v0, LTestObject;->a:LTest;
    const-string v6, ""
    invoke-virtual {v4}, LTest;->a()Z
    move-result v7
    if-eqz v7, :cond_f6
    iget-object v4, v4, LTest;->a:LTest;
    iget-object v7, v4, LTest;->a:LTest;
    if-eqz v7, :cond_f6
    iget-object v7, v4, LTest;->a:LTest;
    iget-object v7, v7, LTest;->a:LTest;
    if-eqz v7, :cond_f6
    iget-object v4, v4, LTest;->a:LTest;
    iget-object v4, v4, LTest;->a:LTest;
    invoke-virtual {v4, v5, v6}, LTest;->a(LTest;Ljava/lang/String;)V
    :cond_f6
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->a:LTest;
    move-object/from16 v0, p0
    iget-object v5, v0, LTestObject;->a:LTest;
    const-string v6, ""
    invoke-virtual {v4}, LTest;->b()Z
    move-result v7
    if-eqz v7, :cond_119
    iget-object v4, v4, LTest;->a:LTest;
    iget-object v7, v4, LTest;->a:LTest;
    if-eqz v7, :cond_119
    iget-object v7, v4, LTest;->a:LTest;
    iget-object v7, v7, LTest;->a:LTest;
    if-eqz v7, :cond_119
    iget-object v4, v4, LTest;->a:LTest;
    iget-object v4, v4, LTest;->a:LTest;
    invoke-virtual {v4, v5, v6}, LTest;->a(LTest;Ljava/lang/String;)V
    :cond_119
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->a:LTest;
    if-eqz v4, :cond_212
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->a:LTest;
    move-object/from16 v0, p0
    iget-object v5, v0, LTestObject;->a:LTest;
    const-string v6, ""
    invoke-virtual {v4}, LTest;->a()Z
    move-result v7
    if-eqz v7, :cond_212
    iget-object v4, v4, LTest;->a:LTest;
    invoke-static {v5}, LTest;->a(Ljava/lang/Object;)Ljava/lang/Object;
    invoke-static {v6}, LTest;->a(Ljava/lang/Object;)Ljava/lang/Object;
    iget-object v7, v4, LTest;->a:LTest;
    iget-object v15, v7, LTest;->a:LTest;
    iget-object v4, v4, LTest;->a:LTest;
    iget-object v7, v4, LTest;->a:LTest;
    if-eqz v7, :cond_9bf
    iget-object v4, v4, LTest;->a:LTest;
    invoke-virtual {v4}, LTest;->b()J
    move-result-wide v8
    move-wide/from16 v16, v8
    :goto_149
    iget-wide v8, v15, LTest;->l:J
    const-wide/16 v10, -0x1
    cmp-long v4, v8, v10
    if-eqz v4, :cond_191
    iget-wide v8, v15, LTest;->l:J
    cmp-long v4, v16, v8
    if-lez v4, :cond_191
    iget-wide v8, v15, LTest;->l:J
    sub-long v8, v16, v8
    long-to-float v4, v8
    const v7, 0x4ca4cb80    # 8.64E7f
    div-float v10, v4, v7
    const-string v7, ""
    iget v4, v15, LTest;->g:I
    int-to-long v8, v4
    invoke-static/range {v5 .. v10}, LTest;->a(LTest;Ljava/lang/String;Ljava/lang/String;JF)V
    const-string v7, ""
    iget v4, v15, LTest;->c:I
    int-to-long v8, v4
    invoke-static/range {v5 .. v10}, LTest;->a(LTest;Ljava/lang/String;Ljava/lang/String;JF)V
    const-string v7, ""
    iget v4, v15, LTest;->b:I
    int-to-long v8, v4
    invoke-static/range {v5 .. v10}, LTest;->a(LTest;Ljava/lang/String;Ljava/lang/String;JF)V
    const-string v7, ""
    iget v4, v15, LTest;->a:I
    int-to-long v8, v4
    invoke-static/range {v5 .. v10}, LTest;->a(LTest;Ljava/lang/String;Ljava/lang/String;JF)V
    const-string v7, ""
    iget v4, v15, LTest;->d:I
    int-to-long v8, v4
    invoke-static/range {v5 .. v10}, LTest;->a(LTest;Ljava/lang/String;Ljava/lang/String;JF)V
    const-string v7, ""
    iget v4, v15, LTest;->h:I
    int-to-long v8, v4
    invoke-static/range {v5 .. v10}, LTest;->a(LTest;Ljava/lang/String;Ljava/lang/String;JF)V
    :cond_191
    const-string v7, ""
    iget v4, v15, LTest;->h:I
    int-to-long v8, v4
    iget v10, v15, LTest;->g:I
    const/4 v11, 0x0
    invoke-static/range {v5 .. v11}, LTest;->a(LTest;Ljava/lang/String;Ljava/lang/String;JII)I
    move-result v4
    iput v4, v15, LTest;->h:I
    const-string v7, ""
    iget-wide v8, v15, LTest;->k:J
    iget v10, v15, LTest;->g:I
    const/4 v11, 0x0
    invoke-static/range {v5 .. v11}, LTest;->a(LTest;Ljava/lang/String;Ljava/lang/String;JII)I
    move-result v4
    int-to-long v8, v4
    iput-wide v8, v15, LTest;->k:J
    const-string v7, ""
    iget v4, v15, LTest;->i:I
    int-to-long v8, v4
    iget v10, v15, LTest;->j:I
    const/4 v11, 0x0
    invoke-static/range {v5 .. v11}, LTest;->a(LTest;Ljava/lang/String;Ljava/lang/String;JII)I
    move-result v4
    iput v4, v15, LTest;->i:I
    const/4 v4, 0x0
    iput v4, v15, LTest;->j:I
    const-string v7, ""
    iget v4, v15, LTest;->e:I
    int-to-long v8, v4
    iget v10, v15, LTest;->a:I
    const/4 v11, 0x0
    invoke-static/range {v5 .. v11}, LTest;->a(LTest;Ljava/lang/String;Ljava/lang/String;JII)I
    move-result v4
    iput v4, v15, LTest;->e:I
    const-string v4, ""
    iget v7, v15, LTest;->a:I
    const/4 v8, 0x0
    invoke-static {v5, v6, v4, v7, v8}, LTest;->a(LTest;Ljava/lang/String;Ljava/lang/String;II)I
    move-result v4
    iput v4, v15, LTest;->a:I
    const-string v4, ""
    iget v7, v15, LTest;->b:I
    const/4 v8, 0x0
    invoke-static {v5, v6, v4, v7, v8}, LTest;->a(LTest;Ljava/lang/String;Ljava/lang/String;II)I
    move-result v4
    iput v4, v15, LTest;->b:I
    const-string v4, ""
    iget v7, v15, LTest;->c:I
    const/4 v8, 0x0
    invoke-static {v5, v6, v4, v7, v8}, LTest;->a(LTest;Ljava/lang/String;Ljava/lang/String;II)I
    move-result v4
    iput v4, v15, LTest;->c:I
    const-string v4, ""
    iget v7, v15, LTest;->d:I
    const/4 v8, 0x0
    invoke-static {v5, v6, v4, v7, v8}, LTest;->a(LTest;Ljava/lang/String;Ljava/lang/String;II)I
    move-result v4
    iput v4, v15, LTest;->d:I
    const-string v4, ""
    iget v7, v15, LTest;->f:I
    const/4 v8, 0x0
    invoke-static {v5, v6, v4, v7, v8}, LTest;->a(LTest;Ljava/lang/String;Ljava/lang/String;II)I
    move-result v4
    iput v4, v15, LTest;->f:I
    const-string v4, ""
    iget v7, v15, LTest;->g:I
    const/4 v8, 0x0
    invoke-static {v5, v6, v4, v7, v8}, LTest;->a(LTest;Ljava/lang/String;Ljava/lang/String;II)I
    move-result v4
    iput v4, v15, LTest;->g:I
    move-wide/from16 v0, v16
    iput-wide v0, v15, LTest;->l:J
    :cond_212
    if-eqz p5, :cond_9c5
    if-ne v13, v14, :cond_9c5
    const/4 v4, 0x1
    :goto_217
    new-instance v5, LTest;
    invoke-direct {v5, v13, v12, v14, v4}, LTest;-><init>(LTest;LTest;LTest;Z)V
    return-object v5
    :cond_21d
    invoke-virtual {v12}, LTest;->a()Z
    move-result v4
    if-eqz v4, :cond_57c
    iget-object v8, v12, LTest;->a:LTest;
    iget-wide v4, v8, LTest;->b:J
    const-wide/16 v6, 0x1
    add-long/2addr v4, v6
    iput-wide v4, v8, LTest;->b:J
    if-eqz v10, :cond_578
    iget v4, v10, LTest;->k:I
    const/4 v5, 0x3
    if-ne v4, v5, :cond_29b
    const/4 v4, 0x1
    :goto_234
    if-eqz v4, :cond_578
    iget-object v9, v8, LTest;->a:LTest;
    invoke-static {v10}, LTest;->a(LTest;)J
    move-result-wide v16
    const-wide/16 v4, 0x0
    cmp-long v4, v16, v4
    if-gez v4, :cond_2ca
    const/4 v4, 0x0
    :cond_243
    :goto_243
    if-nez v4, :cond_543
    iget-object v4, v8, LTest;->a:LTest;
    iget-object v4, v4, LTest;->a:LTest;
    iget v5, v4, LTest;->d:I
    add-int/lit8 v5, v5, 0x1
    iput v5, v4, LTest;->d:I
    iget-object v4, v8, LTest;->a:LTest;
    iget-object v5, v4, LTest;->a:LTest;
    iget-boolean v4, v5, LTest;->b:Z
    if-nez v4, :cond_53c
    iget-object v4, v5, LTest;->a:LTest;
    iget-object v4, v4, LTest;->a:LTest;
    iget-object v4, v4, LTest;->f:Ljava/lang/String;
    if-eqz v4, :cond_539
    const/4 v4, 0x1
    :goto_260
    if-eqz v4, :cond_53c
    iget-object v4, v5, LTest;->a:LTest;
    invoke-virtual {v4}, LTest;->b()J
    move-result-wide v6
    iget-wide v8, v5, LTest;->c:J
    sub-long/2addr v6, v8
    iget v4, v5, LTest;->j:I
    int-to-long v4, v4
    cmp-long v4, v6, v4
    if-lez v4, :cond_53c
    const/4 v4, 0x1
    :goto_273
    if-eqz v4, :cond_53f
    new-instance v4, LTest;
    const/4 v5, 0x0
    const/4 v6, 0x0
    sget-object v7, LTest;->P:LTest;
    const-wide/16 v8, 0x0
    const/4 v11, 0x0
    invoke-direct/range {v4 .. v11}, LTest;-><init>(ILTest;LTest;JLTest;Ljava/util/Map;)V
    move-object v6, v4
    :goto_282
    if-eqz v6, :cond_57c
    iget-object v4, v6, LTest;->a:LTest;
    sget-object v5, LTest;->P:LTest;
    if-ne v4, v5, :cond_57c
    new-instance v4, LTest;
    iget v5, v6, LTest;->a:I
    iget-object v6, v6, LTest;->a:LTest;
    sget-object v7, LTest;->P:LTest;
    iget-wide v8, v10, LTest;->i:J
    const/4 v11, 0x0
    invoke-direct/range {v4 .. v11}, LTest;-><init>(ILTest;LTest;JLTest;Ljava/util/Map;)V
    move-object v14, v4
    goto/16 :goto_2c
    :cond_29b
    const/4 v5, 0x4
    if-ne v4, v5, :cond_2c7
    sget-object v4, LTest;->P:LTest;
    invoke-virtual {v4}, LTest;->b()Ljava/lang/Object;
    move-result-object v4
    check-cast v4, Ljava/lang/Integer;
    invoke-virtual {v4}, Ljava/lang/Integer;->intValue()I
    move-result v4
    and-int/lit8 v4, v4, 0x1
    if-nez v4, :cond_2c4
    sget-object v4, LTest;->P:LTest;
    invoke-virtual {v4}, LTest;->a()Ljava/lang/Object;
    move-result-object v4
    check-cast v4, Ljava/lang/Long;
    invoke-virtual {v4}, Ljava/lang/Long;->intValue()I
    move-result v4
    and-int/lit8 v4, v4, 0x1
    if-nez v4, :cond_2c1
    const/4 v4, 0x1
    goto/16 :goto_234
    :cond_2c1
    const/4 v4, 0x0
    goto/16 :goto_234
    :cond_2c4
    const/4 v4, 0x1
    goto/16 :goto_234
    :cond_2c7
    const/4 v4, 0x0
    goto/16 :goto_234
    :cond_2ca
    iget-object v11, v9, LTest;->a:LTest;
    iget-object v4, v11, LTest;->a:LTest;
    if-eqz v4, :cond_325
    iget-wide v4, v11, LTest;->d:J
    cmp-long v4, v16, v4
    if-nez v4, :cond_325
    iget-object v4, v11, LTest;->a:LTest;
    :goto_2d8
    if-nez v4, :cond_243
    invoke-virtual {v10}, LTest;->a()Ljava/lang/String;
    move-result-object v5
    iget v6, v10, LTest;->k:I
    const/4 v7, 0x3
    if-ne v6, v7, :cond_4e1
    const-string v6, ""
    invoke-virtual {v5, v6}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;
    move-result-object v5
    array-length v6, v5
    const/4 v7, 0x5
    if-ne v6, v7, :cond_2f7
    const/4 v6, 0x0
    aget-object v6, v5, v6
    invoke-static {v6}, LTest;->a(Ljava/lang/String;)I
    move-result v6
    const/4 v7, 0x3
    if-eq v6, v7, :cond_489
    :cond_2f7
    const/4 v5, 0x0
    :goto_2f8
    if-eqz v5, :cond_2fe
    iget-object v6, v9, LTest;->a:LTest;
    iput-object v5, v6, LTest;->f:Ljava/lang/String;
    :cond_2fe
    iget-object v5, v9, LTest;->a:LTest;
    invoke-virtual {v5}, LTest;->b()J
    move-result-wide v6
    iget-wide v0, v9, LTest;->g:J
    move-wide/from16 v16, v0
    sub-long v16, v6, v16
    iget-wide v0, v9, LTest;->e:J
    move-wide/from16 v18, v0
    cmp-long v5, v16, v18
    if-lez v5, :cond_243
    iget-wide v0, v9, LTest;->f:J
    move-wide/from16 v16, v0
    sub-long v16, v6, v16
    iget-object v5, v9, LTest;->a:LTest;
    iget-object v5, v5, LTest;->a:LTest;
    move-wide/from16 v0, v16
    invoke-virtual {v5, v0, v1}, LTest;->a(J)V
    iput-wide v6, v9, LTest;->g:J
    goto/16 :goto_243
    :cond_325
    iget-object v4, v11, LTest;->a:LTest;
    const/4 v5, 0x1
    move-wide/from16 v0, v16
    invoke-virtual {v4, v0, v1, v5}, LTest;->a(JZ)[B
    move-result-object v5
    if-nez v5, :cond_332
    const/4 v4, 0x0
    goto :goto_2d8
    :cond_332
    iget-object v0, v11, LTest;->a:LTest;
    move-object/from16 v18, v0
    new-instance v4, LTest;
    sget-object v6, LTest;->P:LTest;
    invoke-direct {v4, v6}, LTest;-><init>(LTest;)V
    array-length v6, v5
    const/4 v7, 0x6
    if-ge v6, v7, :cond_346
    const/4 v4, 0x0
    :cond_342
    :goto_342
    if-nez v4, :cond_481
    const/4 v4, 0x0
    goto :goto_2d8
    :cond_346
    const/4 v6, 0x0
    invoke-static {v5, v6}, LTest;->b([BI)I
    move-result v19
    const/4 v6, 0x3
    move-object/from16 v0, v18
    move/from16 v1, v19
    invoke-virtual {v0, v1, v5, v6}, LTest;->a(I[BI)[D
    move-result-object v6
    if-eqz v6, :cond_35d
    array-length v7, v6
    const/16 v20, 0x3
    move/from16 v0, v20
    if-eq v7, v0, :cond_35f
    :cond_35d
    const/4 v4, 0x0
    goto :goto_342
    :cond_35f
    const/4 v7, 0x2
    const/16 v20, 0x0
    aget-wide v20, v6, v20
    const-wide v22, 0x416312d000000000L    # 1.0E7
    mul-double v20, v20, v22
    move-wide/from16 v0, v20
    double-to-int v0, v0
    move/from16 v20, v0
    move/from16 v0, v20
    int-to-long v0, v0
    move-wide/from16 v20, v0
    move-wide/from16 v0, v20
    invoke-virtual {v4, v7, v0, v1}, LTest;->b(IJ)LTest;
    const/4 v7, 0x3
    const/16 v20, 0x1
    aget-wide v20, v6, v20
    const-wide v22, 0x416312d000000000L    # 1.0E7
    mul-double v20, v20, v22
    move-wide/from16 v0, v20
    double-to-int v0, v0
    move/from16 v20, v0
    move/from16 v0, v20
    int-to-long v0, v0
    move-wide/from16 v20, v0
    move-wide/from16 v0, v20
    invoke-virtual {v4, v7, v0, v1}, LTest;->b(IJ)LTest;
    const/4 v7, 0x4
    const/16 v20, 0x2
    aget-wide v20, v6, v20
    move-wide/from16 v0, v20
    double-to-float v6, v0
    invoke-virtual {v4, v7, v6}, LTest;->b(IF)LTest;
    array-length v6, v5
    const/4 v7, 0x6
    if-eq v6, v7, :cond_47e
    array-length v6, v5
    add-int/lit8 v6, v6, -0x6
    new-array v0, v6, [B
    move-object/from16 v20, v0
    const/4 v7, 0x6
    const/16 v21, 0x0
    move-object/from16 v0, v20
    move/from16 v1, v21
    invoke-static {v5, v7, v0, v1, v6}, Ljava/lang/System;->arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V
    new-instance v21, LTest;
    move-object/from16 v0, v21
    move-object/from16 v1, v20
    invoke-direct {v0, v1}, LTest;-><init>([B)V
    const/4 v5, 0x0
    move-object/from16 v0, v21
    iput v5, v0, LTest;->a:I
    :goto_3c3
    move-object/from16 v0, v21
    iget v5, v0, LTest;->a:I
    move-object/from16 v0, v21
    iget-object v6, v0, LTest;->b:[B
    array-length v6, v6
    add-int/lit8 v6, v6, -0x1
    if-ge v5, v6, :cond_47e
    move-object/from16 v0, v21
    iget v6, v0, LTest;->a:I
    invoke-virtual/range {v21 .. v21}, LTest;->a()I
    move-result v5
    move-object/from16 v0, v20
    array-length v7, v0
    add-int/2addr v5, v6
    if-ge v7, v5, :cond_3e7
    const/4 v5, 0x0
    :goto_3df
    if-nez v5, :cond_46b
    const/4 v5, 0x0
    :goto_3e2
    if-nez v5, :cond_342
    const/4 v4, 0x0
    goto/16 :goto_342
    :cond_3e7
    new-instance v5, LTest;
    sget-object v7, LTest;->P:LTest;
    invoke-direct {v5, v7}, LTest;-><init>(LTest;)V
    move-object/from16 v0, v20
    invoke-static {v0, v6}, LTest;->a([BI)I
    move-result v22
    add-int/lit8 v6, v6, 0x1
    aget-byte v23, v20, v6
    add-int/lit8 v7, v6, 0x1
    const/4 v6, 0x0
    :goto_3fb
    move/from16 v0, v22
    if-ge v6, v0, :cond_41a
    move-object/from16 v0, v20
    move/from16 v1, v23
    invoke-static {v0, v7, v1, v6}, LTest;->a([BIBI)I
    move-result v24
    const/16 v25, 0x2
    move/from16 v0, v24
    int-to-long v0, v0
    move-wide/from16 v26, v0
    move/from16 v0, v25
    move-wide/from16 v1, v26
    invoke-virtual {v5, v0, v1, v2}, LTest;->a(IJ)V
    add-int/lit8 v7, v7, 0x1
    add-int/lit8 v6, v6, 0x1
    goto :goto_3fb
    :cond_41a
    move-object/from16 v0, v18
    move/from16 v1, v19
    move-object/from16 v2, v20
    invoke-virtual {v0, v1, v2, v7}, LTest;->a(I[BI)[D
    move-result-object v6
    if-nez v6, :cond_428
    const/4 v5, 0x0
    goto :goto_3df
    :cond_428
    const/4 v7, 0x3
    const/16 v22, 0x0
    aget-wide v22, v6, v22
    const-wide v24, 0x416312d000000000L    # 1.0E7
    mul-double v22, v22, v24
    move-wide/from16 v0, v22
    double-to-int v0, v0
    move/from16 v22, v0
    move/from16 v0, v22
    int-to-long v0, v0
    move-wide/from16 v22, v0
    move-wide/from16 v0, v22
    invoke-virtual {v5, v7, v0, v1}, LTest;->a(IJ)V
    const/4 v7, 0x4
    const/16 v22, 0x1
    aget-wide v22, v6, v22
    const-wide v24, 0x416312d000000000L    # 1.0E7
    mul-double v22, v22, v24
    move-wide/from16 v0, v22
    double-to-int v0, v0
    move/from16 v22, v0
    move/from16 v0, v22
    int-to-long v0, v0
    move-wide/from16 v22, v0
    move-wide/from16 v0, v22
    invoke-virtual {v5, v7, v0, v1}, LTest;->a(IJ)V
    const/4 v7, 0x5
    const/16 v22, 0x2
    aget-wide v22, v6, v22
    move-wide/from16 v0, v22
    double-to-float v6, v0
    invoke-virtual {v5, v7, v6}, LTest;->a(IF)V
    goto/16 :goto_3df
    :cond_46b
    const/4 v6, 0x5
    invoke-virtual {v4, v6, v5}, LTest;->a(ILjava/lang/Object;)V
    move-object/from16 v0, v21
    iget v5, v0, LTest;->a:I
    invoke-virtual/range {v21 .. v21}, LTest;->a()I
    move-result v6
    add-int/2addr v5, v6
    move-object/from16 v0, v21
    iput v5, v0, LTest;->a:I
    goto/16 :goto_3c3
    :cond_47e
    const/4 v5, 0x1
    goto/16 :goto_3e2
    :cond_481
    iput-object v4, v11, LTest;->a:LTest;
    move-wide/from16 v0, v16
    iput-wide v0, v11, LTest;->d:J
    goto/16 :goto_2d8
    :cond_489
    new-instance v6, Ljava/lang/StringBuilder;
    invoke-direct {v6}, Ljava/lang/StringBuilder;-><init>()V
    const/4 v7, 0x0
    aget-object v7, v5, v7
    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    move-result-object v6
    const-string v7, ""
    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    move-result-object v6
    const/4 v7, 0x1
    aget-object v7, v5, v7
    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    move-result-object v6
    const-string v7, ""
    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    move-result-object v6
    const/4 v7, 0x2
    aget-object v7, v5, v7
    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    move-result-object v6
    const-string v7, ""
    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    move-result-object v6
    const/4 v7, 0x4
    aget-object v7, v5, v7
    invoke-static {v7}, Ljava/lang/Integer;->parseInt(Ljava/lang/String;)I
    move-result v7
    shr-int/lit8 v7, v7, 0x10
    const v11, 0xffff
    and-int/2addr v7, v11
    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;
    move-result-object v6
    const-string v7, ""
    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    move-result-object v6
    const/4 v7, 0x4
    aget-object v5, v5, v7
    invoke-static {v5}, Ljava/lang/Integer;->parseInt(Ljava/lang/String;)I
    move-result v5
    const v7, 0xffff
    and-int/2addr v5, v7
    invoke-virtual {v6, v5}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;
    move-result-object v5
    invoke-virtual {v5}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object v5
    goto/16 :goto_2f8
    :cond_4e1
    iget v6, v10, LTest;->k:I
    const/4 v7, 0x4
    if-ne v6, v7, :cond_536
    const-string v6, ""
    invoke-virtual {v5, v6}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;
    move-result-object v5
    array-length v6, v5
    const/4 v7, 0x4
    if-ne v6, v7, :cond_4fa
    const/4 v6, 0x0
    aget-object v6, v5, v6
    invoke-static {v6}, LTest;->a(Ljava/lang/String;)I
    move-result v6
    const/4 v7, 0x4
    if-eq v6, v7, :cond_4fd
    :cond_4fa
    const/4 v5, 0x0
    goto/16 :goto_2f8
    :cond_4fd
    new-instance v6, Ljava/lang/StringBuilder;
    invoke-direct {v6}, Ljava/lang/StringBuilder;-><init>()V
    const/4 v7, 0x0
    aget-object v7, v5, v7
    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    move-result-object v6
    const-string v7, ""
    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    move-result-object v6
    const/4 v7, 0x1
    aget-object v7, v5, v7
    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    move-result-object v6
    const-string v7, ""
    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    move-result-object v6
    const/4 v7, 0x2
    aget-object v7, v5, v7
    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    move-result-object v6
    const-string v7, ""
    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    move-result-object v6
    const/4 v7, 0x3
    aget-object v5, v5, v7
    invoke-virtual {v6, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    move-result-object v5
    invoke-virtual {v5}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object v5
    goto/16 :goto_2f8
    :cond_536
    const/4 v5, 0x0
    goto/16 :goto_2f8
    :cond_539
    const/4 v4, 0x0
    goto/16 :goto_260
    :cond_53c
    const/4 v4, 0x0
    goto/16 :goto_273
    :cond_53f
    const/4 v4, 0x0
    move-object v6, v4
    goto/16 :goto_282
    :cond_543
    iget-object v5, v8, LTest;->a:LTest;
    iget-object v5, v5, LTest;->a:LTest;
    iget-object v6, v10, LTest;->j:Ljava/util/Collection;
    invoke-interface {v6}, Ljava/util/Collection;->size()I
    move-result v6
    iget v7, v5, LTest;->e:I
    add-int/2addr v6, v7
    iput v6, v5, LTest;->e:I
    invoke-virtual {v8, v10, v4}, LTest;->a(LTest;LTest;)LTest;
    move-result-object v6
    if-eqz v6, :cond_578
    iget-object v4, v8, LTest;->a:LTest;
    iget-object v4, v4, LTest;->a:LTest;
    iget v5, v4, LTest;->c:I
    add-int/lit8 v5, v5, 0x1
    iput v5, v4, LTest;->c:I
    iget-wide v4, v8, LTest;->c:J
    const-wide/16 v16, 0x1
    add-long v4, v4, v16
    iput-wide v4, v8, LTest;->c:J
    new-instance v4, LTest;
    const/4 v5, 0x2
    sget-object v7, LTest;->P:LTest;
    iget-wide v8, v10, LTest;->i:J
    const/4 v11, 0x0
    invoke-direct/range {v4 .. v11}, LTest;-><init>(ILTest;LTest;JLTest;Ljava/util/Map;)V
    move-object v6, v4
    goto/16 :goto_282
    :cond_578
    const/4 v4, 0x0
    move-object v6, v4
    goto/16 :goto_282
    :cond_57c
    const/4 v4, 0x0
    invoke-virtual {v12, v10, v4, v14, v15}, LTest;->a(LTest;Ljava/util/Map;J)LTest;
    move-result-object v4
    if-nez v4, :cond_5a1
    iget-object v4, v12, LTest;->a:LTest;
    invoke-interface {v4}, LTest;->c()J
    move-result-wide v4
    invoke-virtual {v10, v4, v5}, LTest;->a(J)LTest;
    move-result-object v4
    move-object/from16 v0, p3
    invoke-virtual {v0, v4}, LTest;->a(LTest;)V
    new-instance v4, LTest;
    const/4 v5, 0x1
    const/4 v6, 0x0
    sget-object v7, LTest;->P:LTest;
    const-wide/16 v8, 0x0
    const/4 v11, 0x0
    invoke-direct/range {v4 .. v11}, LTest;-><init>(ILTest;LTest;JLTest;Ljava/util/Map;)V
    move-object v14, v4
    goto/16 :goto_2c
    :cond_5a1
    invoke-virtual {v4}, LTest;->b()Z
    move-result v4
    if-nez v4, :cond_5b6
    new-instance v4, LTest;
    const/4 v5, 0x0
    const/4 v6, 0x0
    sget-object v7, LTest;->P:LTest;
    const-wide/16 v8, 0x0
    const/4 v11, 0x0
    invoke-direct/range {v4 .. v11}, LTest;-><init>(ILTest;LTest;JLTest;Ljava/util/Map;)V
    move-object v14, v4
    goto/16 :goto_2c
    :cond_5b6
    if-nez v13, :cond_60a
    const/4 v4, 0x0
    :goto_5b9
    new-instance v16, LTest;
    add-int/lit8 v4, v4, 0x1
    move-object/from16 v0, v16
    invoke-direct {v0, v4}, LTest;-><init>(I)V
    new-instance v11, LTest;
    const/4 v13, 0x0
    invoke-direct/range {v11 .. v16}, LTest;-><init>(LTest;Ljava/util/Map;JLTest;)V
    iget-wide v0, v10, LTest;->i:J
    move-wide/from16 v18, v0
    const-wide/16 v20, 0x7530
    move-object/from16 v17, p2
    move-object/from16 v22, v11
    invoke-virtual/range {v17 .. v22}, LTest;->a(JJLTest;)V
    new-instance v6, LTest;
    invoke-virtual/range {v16 .. v16}, LTest;->a()D
    move-result-wide v4
    invoke-static {v4, v5}, LTest;->a(D)I
    move-result v4
    invoke-virtual/range {v16 .. v16}, LTest;->b()D
    move-result-wide v8
    invoke-static {v8, v9}, LTest;->a(D)I
    move-result v5
    invoke-virtual/range {v16 .. v16}, LTest;->c()I
    move-result v7
    invoke-static {v7}, LTest;->b(I)I
    move-result v7
    move-object/from16 v0, v16
    iget v8, v0, LTest;->d:I
    invoke-direct {v6, v4, v5, v7, v8}, LTest;-><init>(IIII)V
    invoke-static {v6}, LTest;->c(LTest;)Z
    move-result v4
    if-eqz v4, :cond_60f
    new-instance v4, LTest;
    const/4 v5, 0x1
    sget-object v7, LTest;->P:LTest;
    iget-wide v8, v10, LTest;->i:J
    const/4 v11, 0x0
    invoke-direct/range {v4 .. v11}, LTest;-><init>(ILTest;LTest;JLTest;Ljava/util/Map;)V
    move-object v14, v4
    goto/16 :goto_2c
    :cond_60a
    invoke-interface {v13}, Ljava/util/LTest;->size()I
    move-result v4
    goto :goto_5b9
    :cond_60f
    new-instance v4, LTest;
    const/4 v5, 0x0
    const/4 v6, 0x0
    sget-object v7, LTest;->P:LTest;
    const-wide/16 v8, 0x0
    const/4 v11, 0x0
    invoke-direct/range {v4 .. v11}, LTest;-><init>(ILTest;LTest;JLTest;Ljava/util/Map;)V
    move-object v14, v4
    goto/16 :goto_2c
    :cond_61e
    iget-object v4, v8, LTest;->a:LTest;
    iget-object v5, v4, LTest;->a:LTest;
    iget-object v4, v4, LTest;->a:LTest;
    iget v6, v5, LTest;->d:I
    add-int/lit8 v6, v6, 0x1
    iput v6, v5, LTest;->d:I
    invoke-virtual {v4}, LTest;->c()J
    move-result-wide v6
    iput-wide v6, v5, LTest;->f:J
    move-object/from16 v0, p1
    invoke-virtual {v8, v0}, LTest;->a(LTest;)LTest;
    move-result-object v6
    iget-object v4, v8, LTest;->a:LTest;
    invoke-virtual {v4}, LTest;->a()V
    iget-object v4, v8, LTest;->a:LTest;
    iget-object v5, v4, LTest;->a:LTest;
    invoke-virtual {v5}, LTest;->b()J
    move-result-wide v10
    iget-wide v12, v4, LTest;->m:J
    sub-long v12, v10, v12
    iget-wide v0, v4, LTest;->k:J
    move-wide/from16 v16, v0
    cmp-long v5, v12, v16
    if-lez v5, :cond_65f
    iget-wide v12, v4, LTest;->l:J
    sub-long v12, v10, v12
    iget-object v5, v4, LTest;->a:LTest;
    invoke-virtual {v5, v12, v13}, LTest;->a(J)V
    iget-object v5, v4, LTest;->a:LTest;
    invoke-virtual {v5, v12, v13}, LTest;->a(J)V
    iput-wide v10, v4, LTest;->m:J
    :cond_65f
    iget-object v5, v4, LTest;->a:LTest;
    invoke-virtual {v5}, LTest;->a()V
    iget-object v5, v4, LTest;->a:LTest;
    invoke-virtual {v5}, LTest;->a()V
    iget-object v5, v4, LTest;->a:LTest;
    invoke-virtual {v5}, LTest;->a()V
    iget-object v4, v4, LTest;->a:LTest;
    invoke-virtual {v4}, LTest;->a()V
    iget-object v4, v8, LTest;->a:LTest;
    iget-object v5, v4, LTest;->a:LTest;
    iget-object v4, v4, LTest;->a:LTest;
    iget-wide v10, v5, LTest;->f:J
    const-wide/16 v12, -0x1
    cmp-long v7, v10, v12
    if-eqz v7, :cond_698
    invoke-virtual {v4}, LTest;->c()J
    move-result-wide v10
    iget-wide v12, v5, LTest;->f:J
    sub-long/2addr v10, v12
    const-wide/16 v12, -0x1
    iput-wide v12, v5, LTest;->f:J
    const-wide/16 v12, 0x0
    cmp-long v4, v10, v12
    if-ltz v4, :cond_698
    const-wide/16 v12, 0x7530
    cmp-long v4, v10, v12
    if-lez v4, :cond_6b0
    :cond_698
    :goto_698
    const/4 v4, 0x0
    if-eqz v6, :cond_53
    new-instance v4, LTest;
    const/4 v5, 0x4
    sget-object v7, LTest;->P:LTest;
    iget-object v8, v8, LTest;->a:LTest;
    iget-object v8, v8, LTest;->a:LTest;
    invoke-virtual {v8}, LTest;->c()J
    move-result-wide v8
    const/4 v11, 0x0
    move-object/from16 v10, p1
    invoke-direct/range {v4 .. v11}, LTest;-><init>(ILTest;LTest;JLTest;Ljava/util/Map;)V
    goto/16 :goto_53
    :cond_6b0
    long-to-int v4, v10
    iput v4, v5, LTest;->a:I
    if-eqz v6, :cond_6c0
    iget v4, v5, LTest;->e:I
    add-int/lit8 v4, v4, 0x1
    iput v4, v5, LTest;->e:I
    iget v4, v5, LTest;->a:I
    iput v4, v5, LTest;->b:I
    goto :goto_698
    :cond_6c0
    iget v4, v5, LTest;->a:I
    iput v4, v5, LTest;->c:I
    goto :goto_698
    :cond_6c5
    const/4 v4, 0x0
    goto/16 :goto_53
    :cond_6c8
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->a:LTest;
    invoke-virtual {v4}, LTest;->b()Z
    move-result v5
    if-eqz v5, :cond_91d
    iget-object v8, v4, LTest;->a:LTest;
    iget-object v4, v8, LTest;->a:LTest;
    iget-object v5, v4, LTest;->a:LTest;
    iget-boolean v4, v5, LTest;->d:Z
    if-nez v4, :cond_716
    sget-object v4, LTest;->P:LTest;
    :goto_6de
    iput-object v4, v8, LTest;->a:LTest;
    iget-object v4, v8, LTest;->a:LTest;
    iget-object v5, v8, LTest;->a:LTest;
    iget-object v5, v5, LTest;->a:LTest;
    invoke-virtual {v5}, LTest;->b()J
    move-result-wide v6
    invoke-virtual {v4, v6, v7}, LTest;->a(J)V
    if-nez p1, :cond_721
    iget-object v4, v8, LTest;->a:LTest;
    const/4 v5, 0x3
    iget-object v6, v8, LTest;->a:LTest;
    iget-object v6, v6, LTest;->a:LTest;
    invoke-virtual {v6}, LTest;->b()J
    move-result-wide v6
    invoke-virtual {v4, v5, v6, v7}, LTest;->a(IJ)V
    const/4 v4, 0x0
    :goto_6fe
    if-eqz v4, :cond_920
    iget-object v5, v4, LTest;->a:LTest;
    sget-object v6, LTest;->P:LTest;
    if-ne v5, v6, :cond_9cc
    move-object/from16 v0, p0
    iget-object v5, v0, LTestObject;->a:LTest;
    move-object/from16 v0, p1
    move-object/from16 v1, p4
    move-object/from16 v2, p3
    invoke-virtual {v5, v0, v1, v2}, LTest;->a(LTest;LTest;LTest;)LTest;
    move-object v12, v4
    goto/16 :goto_56
    :cond_716
    new-instance v4, LTest;
    invoke-direct {v4}, LTest;-><init>()V
    iget-object v5, v5, LTest;->a:LTest;
    invoke-virtual {v5, v4}, LTest;->a(Ljava/lang/Object;)Ljava/lang/Object;
    goto :goto_6de
    :cond_721
    iget-object v4, v8, LTest;->a:LTest;
    move-object/from16 v0, p1
    invoke-virtual {v4, v0}, LTest;->a(LTest;)V
    iget-object v4, v8, LTest;->a:LTest;
    iget-object v5, v4, LTest;->a:LTest;
    iget-object v4, v4, LTest;->a:LTest;
    const-string v6, ""
    invoke-virtual {v5, v6}, LTest;->a(Ljava/lang/String;)LTest;
    move-result-object v5
    iget v6, v5, LTest;->c:I
    add-int/lit8 v6, v6, 0x1
    iput v6, v5, LTest;->c:I
    invoke-virtual {v4}, LTest;->c()J
    move-result-wide v6
    iput-wide v6, v5, LTest;->b:J
    iget-object v4, v8, LTest;->a:LTest;
    iget-object v5, v4, LTest;->a:LTest;
    iget-object v4, v4, LTest;->a:LTest;
    const-string v6, ""
    invoke-virtual {v5, v6}, LTest;->a(Ljava/lang/String;)LTest;
    move-result-object v5
    iget v6, v5, LTest;->c:I
    add-int/lit8 v6, v6, 0x1
    iput v6, v5, LTest;->c:I
    invoke-virtual {v4}, LTest;->c()J
    move-result-wide v6
    iput-wide v6, v5, LTest;->b:J
    move-object/from16 v0, p1
    invoke-virtual {v8, v0}, LTest;->a(LTest;)Z
    move-result v4
    iget-object v5, v8, LTest;->a:LTest;
    iget-object v6, v5, LTest;->a:LTest;
    iget-object v5, v5, LTest;->a:LTest;
    const-string v7, ""
    invoke-virtual {v6, v7}, LTest;->a(Ljava/lang/String;)LTest;
    move-result-object v6
    iget-wide v10, v6, LTest;->b:J
    const-wide/16 v12, -0x1
    cmp-long v7, v10, v12
    if-eqz v7, :cond_789
    invoke-virtual {v5}, LTest;->c()J
    move-result-wide v10
    iget-wide v12, v6, LTest;->b:J
    sub-long/2addr v10, v12
    const-wide/16 v12, -0x1
    iput-wide v12, v6, LTest;->b:J
    const-wide/16 v12, 0x0
    cmp-long v5, v10, v12
    if-ltz v5, :cond_789
    const-wide/16 v12, 0x7530
    cmp-long v5, v10, v12
    if-lez v5, :cond_83e
    :cond_789
    :goto_789
    if-eqz v4, :cond_793
    iget-object v4, v8, LTest;->a:LTest;
    invoke-virtual {v4}, LTest;->c()Z
    move-result v4
    if-eqz v4, :cond_843
    :cond_793
    iget-object v4, v8, LTest;->a:LTest;
    invoke-virtual {v4}, LTest;->a()V
    const/4 v6, 0x0
    :goto_799
    iget-object v4, v8, LTest;->a:LTest;
    const/4 v5, 0x0
    invoke-virtual {v4, v5}, LTest;->a(Ljava/util/LTest;)V
    iget-object v4, v8, LTest;->a:LTest;
    invoke-virtual {v4}, LTest;->a()V
    iget-object v4, v8, LTest;->a:LTest;
    iget-object v5, v4, LTest;->a:LTest;
    invoke-virtual {v5}, LTest;->b()J
    move-result-wide v10
    iget-wide v12, v4, LTest;->q:J
    sub-long v12, v10, v12
    iget-wide v0, v4, LTest;->o:J
    move-wide/from16 v16, v0
    cmp-long v5, v12, v16
    if-lez v5, :cond_7c3
    iget-wide v12, v4, LTest;->p:J
    sub-long v12, v10, v12
    iget-object v5, v4, LTest;->a:LTest;
    invoke-virtual {v5, v12, v13}, LTest;->a(J)V
    iput-wide v10, v4, LTest;->q:J
    :cond_7c3
    iget-wide v12, v4, LTest;->s:J
    sub-long v12, v10, v12
    iget-wide v0, v4, LTest;->r:J
    move-wide/from16 v16, v0
    cmp-long v5, v12, v16
    if-lez v5, :cond_7d6
    iget-object v5, v4, LTest;->a:LTest;
    invoke-virtual {v5}, LTest;->a()V
    iput-wide v10, v4, LTest;->s:J
    :cond_7d6
    iget-object v4, v4, LTest;->a:LTest;
    invoke-virtual {v4}, LTest;->a()V
    iget-object v4, v8, LTest;->a:LTest;
    iget-object v5, v4, LTest;->a:LTest;
    iget-object v4, v4, LTest;->a:LTest;
    const-string v7, ""
    invoke-virtual {v5, v7}, LTest;->a(Ljava/lang/String;)LTest;
    move-result-object v7
    iget-wide v10, v7, LTest;->b:J
    const-wide/16 v12, -0x1
    cmp-long v9, v10, v12
    if-eqz v9, :cond_806
    invoke-virtual {v4}, LTest;->c()J
    move-result-wide v10
    iget-wide v12, v7, LTest;->b:J
    sub-long/2addr v10, v12
    const-wide/16 v12, -0x1
    iput-wide v12, v7, LTest;->b:J
    const-wide/16 v12, 0x0
    cmp-long v4, v10, v12
    if-ltz v4, :cond_806
    const-wide/16 v12, 0x7530
    cmp-long v4, v10, v12
    if-lez v4, :cond_8d7
    :cond_806
    :goto_806
    if-eqz v6, :cond_8dc
    iget v4, v5, LTest;->f:I
    add-int/lit8 v4, v4, 0x1
    iput v4, v5, LTest;->f:I
    iget v4, v7, LTest;->a:I
    iput v4, v5, LTest;->g:I
    :goto_812
    iget-object v4, v8, LTest;->a:LTest;
    invoke-virtual {v4}, LTest;->c()Z
    move-result v4
    if-eqz v4, :cond_8e2
    iget-object v4, v8, LTest;->a:LTest;
    const/4 v5, 0x7
    iget-object v6, v8, LTest;->a:LTest;
    iget-object v6, v6, LTest;->a:LTest;
    invoke-virtual {v6}, LTest;->b()J
    move-result-wide v6
    invoke-virtual {v4, v5, v6, v7}, LTest;->a(IJ)V
    new-instance v4, LTest;
    const/4 v5, 0x0
    const/4 v6, 0x0
    sget-object v7, LTest;->P:LTest;
    iget-object v8, v8, LTest;->a:LTest;
    iget-object v8, v8, LTest;->a:LTest;
    invoke-virtual {v8}, LTest;->c()J
    move-result-wide v8
    const/4 v11, 0x0
    move-object/from16 v10, p1
    invoke-direct/range {v4 .. v11}, LTest;-><init>(ILTest;LTest;JLTest;Ljava/util/Map;)V
    goto/16 :goto_6fe
    :cond_83e
    long-to-int v5, v10
    iput v5, v6, LTest;->a:I
    goto/16 :goto_789
    :cond_843
    iget-object v4, v8, LTest;->a:LTest;
    iget-object v5, v4, LTest;->a:LTest;
    iget-object v4, v4, LTest;->a:LTest;
    const-string v6, ""
    invoke-virtual {v5, v6}, LTest;->a(Ljava/lang/String;)LTest;
    move-result-object v5
    iget v6, v5, LTest;->c:I
    add-int/lit8 v6, v6, 0x1
    iput v6, v5, LTest;->c:I
    invoke-virtual {v4}, LTest;->c()J
    move-result-wide v6
    iput-wide v6, v5, LTest;->b:J
    iget-object v4, v8, LTest;->a:LTest;
    invoke-virtual {v4}, LTest;->b()LTest;
    move-result-object v4
    iput-object v4, v8, LTest;->a:LTest;
    iget-object v4, v8, LTest;->a:LTest;
    iget-object v5, v4, LTest;->a:LTest;
    iget-object v4, v4, LTest;->a:LTest;
    const-string v6, ""
    invoke-virtual {v5, v6}, LTest;->a(Ljava/lang/String;)LTest;
    move-result-object v5
    iget-wide v6, v5, LTest;->b:J
    const-wide/16 v10, -0x1
    cmp-long v6, v6, v10
    if-eqz v6, :cond_88e
    invoke-virtual {v4}, LTest;->c()J
    move-result-wide v6
    iget-wide v10, v5, LTest;->b:J
    sub-long/2addr v6, v10
    const-wide/16 v10, -0x1
    iput-wide v10, v5, LTest;->b:J
    const-wide/16 v10, 0x0
    cmp-long v4, v6, v10
    if-ltz v4, :cond_88e
    const-wide/16 v10, 0x7530
    cmp-long v4, v6, v10
    if-lez v4, :cond_8a7
    :cond_88e
    :goto_88e
    iget-object v4, v8, LTest;->a:LTest;
    invoke-virtual {v4}, LTest;->b()Z
    move-result v4
    if-nez v4, :cond_8ab
    iget-object v4, v8, LTest;->a:LTest;
    const/4 v5, 0x4
    iget-object v6, v8, LTest;->a:LTest;
    iget-object v6, v6, LTest;->a:LTest;
    invoke-virtual {v6}, LTest;->b()J
    move-result-wide v6
    invoke-virtual {v4, v5, v6, v7}, LTest;->a(IJ)V
    const/4 v6, 0x0
    goto/16 :goto_799
    :cond_8a7
    long-to-int v4, v6
    iput v4, v5, LTest;->a:I
    goto :goto_88e
    :cond_8ab
    iget-object v4, v8, LTest;->a:LTest;
    iget-wide v4, v4, LTest;->a:J
    const/4 v6, 0x2
    new-array v6, v6, [I
    invoke-static {v4, v5, v6}, LTest;->a(J[I)[I
    move-result-object v4
    iget-object v5, v8, LTest;->a:LTest;
    iget-object v6, v8, LTest;->a:LTest;
    iget-object v6, v6, LTest;->a:LTest;
    iget v6, v6, LTest;->b:I
    iget-object v7, v8, LTest;->a:LTest;
    iget-object v7, v7, LTest;->a:LTest;
    iget v7, v7, LTest;->b:I
    invoke-virtual {v5, v6, v7, v4}, LTest;->a(II[I)I
    move-result v5
    new-instance v6, LTest;
    const/4 v7, 0x0
    aget v7, v4, v7
    const/4 v9, 0x1
    aget v4, v4, v9
    const/16 v9, 0x6d
    invoke-direct {v6, v7, v4, v5, v9}, LTest;-><init>(IIII)V
    goto/16 :goto_799
    :cond_8d7
    long-to-int v4, v10
    iput v4, v7, LTest;->a:I
    goto/16 :goto_806
    :cond_8dc
    iget v4, v7, LTest;->a:I
    iput v4, v5, LTest;->h:I
    goto/16 :goto_812
    :cond_8e2
    if-nez v6, :cond_8f5
    iget-object v4, v8, LTest;->a:LTest;
    const/4 v5, 0x5
    iget-object v6, v8, LTest;->a:LTest;
    iget-object v6, v6, LTest;->a:LTest;
    invoke-virtual {v6}, LTest;->b()J
    move-result-wide v6
    invoke-virtual {v4, v5, v6, v7}, LTest;->a(IJ)V
    const/4 v4, 0x0
    goto/16 :goto_6fe
    :cond_8f5
    iget-object v4, v8, LTest;->a:LTest;
    invoke-virtual {v4, v6}, LTest;->a(LTest;)V
    iget-object v4, v8, LTest;->a:LTest;
    const/4 v5, 0x1
    iget-object v7, v8, LTest;->a:LTest;
    iget-object v7, v7, LTest;->a:LTest;
    invoke-virtual {v7}, LTest;->b()J
    move-result-wide v10
    invoke-virtual {v4, v5, v10, v11}, LTest;->a(IJ)V
    new-instance v4, LTest;
    const/4 v5, 0x5
    sget-object v7, LTest;->P:LTest;
    iget-object v8, v8, LTest;->a:LTest;
    iget-object v8, v8, LTest;->a:LTest;
    invoke-virtual {v8}, LTest;->c()J
    move-result-wide v8
    const/4 v11, 0x0
    move-object/from16 v10, p1
    invoke-direct/range {v4 .. v11}, LTest;-><init>(ILTest;LTest;JLTest;Ljava/util/Map;)V
    goto/16 :goto_6fe
    :cond_91d
    const/4 v4, 0x0
    goto/16 :goto_6fe
    :cond_920
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->a:LTest;
    move-object/from16 v0, p1
    move-object/from16 v1, p4
    move-object/from16 v2, p3
    invoke-virtual {v4, v0, v1, v2}, LTest;->a(LTest;LTest;LTest;)LTest;
    move-result-object v4
    move-object v12, v4
    goto/16 :goto_56
    :cond_931
    const/4 v4, 0x0
    goto/16 :goto_5d
    :cond_934
    const/4 v5, 0x0
    goto/16 :goto_64
    :cond_937
    if-nez v4, :cond_93c
    move-object v13, v14
    goto/16 :goto_6a
    :cond_93c
    if-eqz v5, :cond_973
    iget-object v5, v12, LTest;->a:LTest;
    iget-object v6, v14, LTest;->a:LTest;
    invoke-static {v5, v6}, LTest;->a(LTest;LTest;)I
    move-result v4
    iget v7, v5, LTest;->e:I
    iget v8, v6, LTest;->e:I
    add-int/2addr v7, v8
    const v8, 0x3567e0
    invoke-static {v7, v8}, Ljava/lang/Math;->max(II)I
    move-result v7
    div-int/lit16 v7, v7, 0x3e8
    if-gt v4, v7, :cond_965
    const/4 v4, 0x1
    :goto_957
    if-eqz v4, :cond_969
    iget v4, v5, LTest;->e:I
    iget v5, v6, LTest;->e:I
    if-le v4, v5, :cond_967
    const/4 v4, 0x1
    :goto_960
    if-eqz v4, :cond_973
    move-object v13, v14
    goto/16 :goto_6a
    :cond_965
    const/4 v4, 0x0
    goto :goto_957
    :cond_967
    const/4 v4, 0x0
    goto :goto_960
    :cond_969
    iget v4, v5, LTest;->f:I
    iget v5, v6, LTest;->f:I
    if-ge v4, v5, :cond_971
    const/4 v4, 0x1
    goto :goto_960
    :cond_971
    const/4 v4, 0x0
    goto :goto_960
    :cond_973
    move-object v13, v12
    goto/16 :goto_6a
    :cond_976
    const/4 v4, 0x1
    move v11, v4
    move v15, v5
    goto/16 :goto_7b
    :cond_97b
    const/4 v4, 0x0
    goto/16 :goto_84
    :pswitch_97e
    const-string v7, ""
    goto/16 :goto_8c
    :pswitch_982
    const-string v7, ""
    goto/16 :goto_8c
    :pswitch_986
    const-string v7, ""
    goto/16 :goto_8c
    :pswitch_98a
    const-string v7, ""
    goto/16 :goto_8c
    :cond_98e
    const/4 v4, 0x0
    goto/16 :goto_a3
    :pswitch_991
    const-string v7, ""
    goto/16 :goto_ab
    :pswitch_995
    const-string v7, ""
    goto/16 :goto_ab
    :cond_999
    if-eqz v11, :cond_9ad
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->a:LTest;
    const-string v5, ""
    const-string v6, ""
    const-string v7, ""
    const-wide/16 v8, 0x1
    const/4 v10, 0x1
    invoke-virtual/range {v4 .. v10}, LTest;->a(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JZ)V
    goto/16 :goto_cd
    :cond_9ad
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->a:LTest;
    const-string v5, ""
    const-string v6, ""
    const-string v7, ""
    const-wide/16 v8, 0x1
    const/4 v10, 0x1
    invoke-virtual/range {v4 .. v10}, LTest;->a(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JZ)V
    goto/16 :goto_cd
    :cond_9bf
    const-wide/16 v8, 0x0
    move-wide/from16 v16, v8
    goto/16 :goto_149
    :cond_9c5
    const/4 v4, 0x0
    goto/16 :goto_217
    :cond_9c8
    move v11, v4
    move v15, v5
    goto/16 :goto_7b
    :cond_9cc
    move-object v12, v4
    goto/16 :goto_56
    :cond_9cf
    move-object v13, v4
    goto/16 :goto_17
    :pswitch_data_9d2
    .packed-switch 0x1
        :pswitch_97e
        :pswitch_982
        :pswitch_8c
        :pswitch_986
        :pswitch_98a
    .end packed-switch
    :pswitch_data_9e0
    .packed-switch 0x1
        :pswitch_991
        :pswitch_995
    .end packed-switch
.end method