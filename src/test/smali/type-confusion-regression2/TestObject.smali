# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

.class public final LTestObject;
.super Ljava/lang/Object;

.field private static final l:[I

.field public a:I
.field public b:I
.field public c:[I
.field public d:[D
.field public e:I
.field public f:I
.field public g:[D
.field public h:[D
.field public i:[D
.field public j:I
.field private k:[D

.method public final b()V
    .registers 29
    move-object/from16 v0, p0
    iget v2, v0, LTestObject;->a:I
    const/4 v3, 0x1
    if-ne v2, v3, :cond_8
    :cond_7
    return-void
    :cond_8
    move-object/from16 v0, p0
    iget v2, v0, LTestObject;->a:I
    mul-int/lit8 v12, v2, 0x2
    const/4 v5, 0x0
    move-object/from16 v0, p0
    iget v2, v0, LTestObject;->a:I
    const/4 v3, 0x0
    const/4 v4, 0x0
    :goto_15
    add-int/lit8 v6, v4, 0x1
    const/4 v4, 0x4
    if-gt v6, v4, :cond_55
    sget-object v4, LTestObject;->l:[I
    add-int/lit8 v5, v6, -0x1
    aget v4, v4, v5
    move v5, v4
    move v4, v3
    :goto_22
    div-int v3, v2, v5
    mul-int v7, v5, v3
    sub-int v7, v2, v7
    if-nez v7, :cond_f9
    add-int/lit8 v2, v4, 0x1
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->g:[D
    add-int/lit8 v7, v2, 0x1
    add-int/2addr v7, v12
    int-to-double v8, v5
    aput-wide v8, v4, v7
    const/4 v4, 0x2
    if-ne v5, v4, :cond_64
    const/4 v4, 0x1
    if-eq v2, v4, :cond_64
    const/4 v4, 0x2
    :goto_3d
    if-gt v4, v2, :cond_5a
    sub-int v7, v2, v4
    add-int/lit8 v7, v7, 0x2
    add-int/2addr v7, v12
    move-object/from16 v0, p0
    iget-object v8, v0, LTestObject;->g:[D
    add-int/lit8 v9, v7, 0x1
    move-object/from16 v0, p0
    iget-object v10, v0, LTestObject;->g:[D
    aget-wide v10, v10, v7
    aput-wide v10, v8, v9
    add-int/lit8 v4, v4, 0x1
    goto :goto_3d
    :cond_55
    add-int/lit8 v4, v5, 0x2
    move v5, v4
    move v4, v3
    goto :goto_22
    :cond_5a
    move-object/from16 v0, p0
    iget-object v4, v0, LTestObject;->g:[D
    add-int/lit8 v7, v12, 0x2
    const-wide/high16 v8, 0x4000000000000000L
    aput-wide v8, v4, v7
    :cond_64
    const/4 v4, 0x1
    if-ne v3, v4, :cond_f5
    move-object/from16 v0, p0
    iget-object v3, v0, LTestObject;->g:[D
    move-object/from16 v0, p0
    iget v4, v0, LTestObject;->a:I
    int-to-double v4, v4
    aput-wide v4, v3, v12
    move-object/from16 v0, p0
    iget-object v3, v0, LTestObject;->g:[D
    add-int/lit8 v4, v12, 0x1
    int-to-double v6, v2
    aput-wide v6, v3, v4
    const-wide v4, 0x401921fb54442d18L
    move-object/from16 v0, p0
    iget v3, v0, LTestObject;->a:I
    int-to-double v6, v3
    div-double v14, v4, v6
    const/4 v6, 0x0
    add-int/lit8 v13, v2, -0x1
    const/4 v2, 0x1
    if-eqz v13, :cond_7
    const/4 v3, 0x1
    move v9, v2
    move v11, v3
    :goto_90
    if-gt v11, v13, :cond_7
    move-object/from16 v0, p0
    iget-object v2, v0, LTestObject;->g:[D
    add-int/lit8 v3, v11, 0x1
    add-int/2addr v3, v12
    aget-wide v2, v2, v3
    double-to-int v3, v2
    const/4 v2, 0x0
    mul-int v10, v9, v3
    move-object/from16 v0, p0
    iget v4, v0, LTestObject;->a:I
    div-int v16, v4, v10
    add-int/lit8 v17, v3, -0x1
    const/4 v3, 0x1
    move v8, v3
    :goto_a9
    move/from16 v0, v17
    if-gt v8, v0, :cond_f0
    add-int v7, v2, v9
    int-to-double v2, v7
    mul-double v18, v2, v14
    const-wide/16 v4, 0x0
    const/4 v2, 0x3
    move v3, v6
    :goto_b6
    move/from16 v0, v16
    if-gt v2, v0, :cond_e9
    add-int/lit8 v3, v3, 0x2
    const-wide/high16 v20, 0x3ff0000000000000L
    add-double v4, v4, v20
    mul-double v20, v4, v18
    move-object/from16 v0, p0
    iget v0, v0, LTestObject;->a:I
    move/from16 v22, v0
    add-int v22, v22, v3
    move-object/from16 v0, p0
    iget-object v0, v0, LTestObject;->g:[D
    move-object/from16 v23, v0
    add-int/lit8 v24, v22, -0x2
    invoke-static/range {v20 .. v21}, Ljava/lang/Math;->cos(D)D
    move-result-wide v26
    aput-wide v26, v23, v24
    move-object/from16 v0, p0
    iget-object v0, v0, LTestObject;->g:[D
    move-object/from16 v23, v0
    add-int/lit8 v22, v22, -0x1
    invoke-static/range {v20 .. v21}, Ljava/lang/Math;->sin(D)D
    move-result-wide v20
    aput-wide v20, v23, v22
    add-int/lit8 v2, v2, 0x2
    goto :goto_b6
    :cond_e9
    add-int v6, v6, v16
    add-int/lit8 v2, v8, 0x1
    move v8, v2
    move v2, v7
    goto :goto_a9
    :cond_f0
    add-int/lit8 v2, v11, 0x1
    move v9, v10
    move v11, v2
    goto :goto_90
    :cond_f5
    move v4, v2
    move v2, v3
    goto/16 :goto_22
    :cond_f9
    move v3, v4
    move v4, v6
    goto/16 :goto_15
.end method