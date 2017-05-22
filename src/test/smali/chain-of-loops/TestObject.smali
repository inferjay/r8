# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

.class public final LTestObject;
.super Ljava/lang/Object;

.field public i:I
.field public e:Ljava/lang/String;
.field public z:Ljava/lang/String;
.field public f:LTest;
.field public a:[LTest;

.method public final method()I
    .registers 7
    .prologue
    const/4 v5, 0x1
    const/4 v1, 0x0
    invoke-virtual {p0}, LTestObject;->method2()I
    move-result v0
    iget-object v2, p0, LTestObject;->e:Ljava/lang/String;
    if-eqz v2, :cond_1b
    iget-object v2, p0, LTestObject;->e:Ljava/lang/String;
    const-string v3, ""
    invoke-virtual {v2, v3}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v2
    if-nez v2, :cond_1b
    iget-object v2, p0, LTestObject;->e:Ljava/lang/String;
    invoke-static {v5, v2}, LTest;->b(ILjava/lang/String;)I
    move-result v2
    add-int/2addr v0, v2
    :cond_1b
    iget-object v2, p0, LTestObject;->f:LTest;
    if-eqz v2, :cond_27
    const/4 v2, 0x2
    iget-object v3, p0, LTestObject;->f:LTest;
    invoke-static {v2, v3}, LTest;->d(ILTest;)I
    move-result v2
    add-int/2addr v0, v2
    :cond_27
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_47
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_47
    move v2, v0
    move v0, v1
    :goto_32
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_46
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_43
    const/4 v4, 0x3
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_43
    add-int/lit8 v0, v0, 0x1
    goto :goto_32
    :cond_46
    move v0, v2
    :cond_47
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_67
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_67
    move v2, v0
    move v0, v1
    :goto_52
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_66
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_63
    const/4 v4, 0x4
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_63
    add-int/lit8 v0, v0, 0x1
    goto :goto_52
    :cond_66
    move v0, v2
    :cond_67
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_87
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_87
    move v2, v0
    move v0, v1
    :goto_72
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_86
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_83
    const/4 v4, 0x5
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_83
    add-int/lit8 v0, v0, 0x1
    goto :goto_72
    :cond_86
    move v0, v2
    :cond_87
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_a7
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_a7
    move v2, v0
    move v0, v1
    :goto_92
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_a6
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_a3
    const/4 v4, 0x6
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_a3
    add-int/lit8 v0, v0, 0x1
    goto :goto_92
    :cond_a6
    move v0, v2
    :cond_a7
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_c7
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_c7
    move v2, v0
    move v0, v1
    :goto_b2
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_c6
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_c3
    const/4 v4, 0x7
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_c3
    add-int/lit8 v0, v0, 0x1
    goto :goto_b2
    :cond_c6
    move v0, v2
    :cond_c7
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_e8
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_e8
    move v2, v0
    move v0, v1
    :goto_d2
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_e7
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_e4
    const/16 v4, 0x8
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_e4
    add-int/lit8 v0, v0, 0x1
    goto :goto_d2
    :cond_e7
    move v0, v2
    :cond_e8
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_109
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_109
    move v2, v0
    move v0, v1
    :goto_f3
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_108
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_105
    const/16 v4, 0x9
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_105
    add-int/lit8 v0, v0, 0x1
    goto :goto_f3
    :cond_108
    move v0, v2
    :cond_109
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_12a
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_12a
    move v2, v0
    move v0, v1
    :goto_114
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_129
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_126
    const/16 v4, 0xa
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_126
    add-int/lit8 v0, v0, 0x1
    goto :goto_114
    :cond_129
    move v0, v2
    :cond_12a
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_14b
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_14b
    move v2, v0
    move v0, v1
    :goto_135
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_14a
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_147
    const/16 v4, 0xb
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_147
    add-int/lit8 v0, v0, 0x1
    goto :goto_135
    :cond_14a
    move v0, v2
    :cond_14b
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_16c
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_16c
    move v2, v0
    move v0, v1
    :goto_156
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_16b
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_168
    const/16 v4, 0xc
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_168
    add-int/lit8 v0, v0, 0x1
    goto :goto_156
    :cond_16b
    move v0, v2
    :cond_16c
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_18d
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_18d
    move v2, v0
    move v0, v1
    :goto_177
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_18c
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_189
    const/16 v4, 0xd
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_189
    add-int/lit8 v0, v0, 0x1
    goto :goto_177
    :cond_18c
    move v0, v2
    :cond_18d
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_1ae
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_1ae
    move v2, v0
    move v0, v1
    :goto_198
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_1ad
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_1aa
    const/16 v4, 0xe
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_1aa
    add-int/lit8 v0, v0, 0x1
    goto :goto_198
    :cond_1ad
    move v0, v2
    :cond_1ae
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_1cf
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_1cf
    move v2, v0
    move v0, v1
    :goto_1b9
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_1ce
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_1cb
    const/16 v4, 0xf
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_1cb
    add-int/lit8 v0, v0, 0x1
    goto :goto_1b9
    :cond_1ce
    move v0, v2
    :cond_1cf
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_1f0
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_1f0
    move v2, v0
    move v0, v1
    :goto_1da
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_1ef
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_1ec
    const/16 v4, 0x10
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_1ec
    add-int/lit8 v0, v0, 0x1
    goto :goto_1da
    :cond_1ef
    move v0, v2
    :cond_1f0
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_211
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_211
    move v2, v0
    move v0, v1
    :goto_1fb
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_210
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_20d
    const/16 v4, 0x11
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_20d
    add-int/lit8 v0, v0, 0x1
    goto :goto_1fb
    :cond_210
    move v0, v2
    :cond_211
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_232
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_232
    move v2, v0
    move v0, v1
    :goto_21c
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_231
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_22e
    const/16 v4, 0x12
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_22e
    add-int/lit8 v0, v0, 0x1
    goto :goto_21c
    :cond_231
    move v0, v2
    :cond_232
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_253
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_253
    move v2, v0
    move v0, v1
    :goto_23d
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_252
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_24f
    const/16 v4, 0x13
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_24f
    add-int/lit8 v0, v0, 0x1
    goto :goto_23d
    :cond_252
    move v0, v2
    :cond_253
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_274
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_274
    move v2, v0
    move v0, v1
    :goto_25e
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_273
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_270
    const/16 v4, 0x14
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_270
    add-int/lit8 v0, v0, 0x1
    goto :goto_25e
    :cond_273
    move v0, v2
    :cond_274
    iget-object v2, p0, LTestObject;->z:Ljava/lang/String;
    if-eqz v2, :cond_28b
    iget-object v2, p0, LTestObject;->z:Ljava/lang/String;
    const-string v3, ""
    invoke-virtual {v2, v3}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v2
    if-nez v2, :cond_28b
    const/16 v2, 0x15
    iget-object v3, p0, LTestObject;->z:Ljava/lang/String;
    invoke-static {v2, v3}, LTest;->b(ILjava/lang/String;)I
    move-result v2
    add-int/2addr v0, v2
    :cond_28b
    iget v2, p0, LTestObject;->i:I
    if-eq v2, v5, :cond_298
    const/16 v2, 0x16
    iget v3, p0, LTestObject;->i:I
    invoke-static {v2, v3}, LTest;->f(II)I
    move-result v2
    add-int/2addr v0, v2
    :cond_298
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_2b9
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_2b9
    move v2, v0
    move v0, v1
    :goto_2a3
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_2b8
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_2b5
    const/16 v4, 0x17
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_2b5
    add-int/lit8 v0, v0, 0x1
    goto :goto_2a3
    :cond_2b8
    move v0, v2
    :cond_2b9
    iget-object v2, p0, LTestObject;->z:Ljava/lang/String;
    if-eqz v2, :cond_2d0
    iget-object v2, p0, LTestObject;->z:Ljava/lang/String;
    const-string v3, ""
    invoke-virtual {v2, v3}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v2
    if-nez v2, :cond_2d0
    const/16 v2, 0x18
    iget-object v3, p0, LTestObject;->z:Ljava/lang/String;
    invoke-static {v2, v3}, LTest;->b(ILjava/lang/String;)I
    move-result v2
    add-int/2addr v0, v2
    :cond_2d0
    iget-object v2, p0, LTest;->M:LTest;
    if-eqz v2, :cond_2dd
    const/16 v2, 0x19
    iget-object v3, p0, LTest;->M:LTest;
    invoke-static {v2, v3}, LTest;->d(ILTest;)I
    move-result v2
    add-int/2addr v0, v2
    :cond_2dd
    iget-object v2, p0, LTestObject;->z:Ljava/lang/String;
    if-eqz v2, :cond_2f4
    iget-object v2, p0, LTestObject;->z:Ljava/lang/String;
    const-string v3, ""
    invoke-virtual {v2, v3}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v2
    if-nez v2, :cond_2f4
    const/16 v2, 0x1a
    iget-object v3, p0, LTestObject;->z:Ljava/lang/String;
    invoke-static {v2, v3}, LTest;->b(ILjava/lang/String;)I
    move-result v2
    add-int/2addr v0, v2
    :cond_2f4
    iget-object v2, p0, LTest;->o:[LTest;
    if-eqz v2, :cond_315
    iget-object v2, p0, LTest;->o:[LTest;
    array-length v2, v2
    if-lez v2, :cond_315
    move v2, v0
    move v0, v1
    :goto_2ff
    iget-object v3, p0, LTest;->o:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_314
    iget-object v3, p0, LTest;->o:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_311
    const/16 v4, 0x1b
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_311
    add-int/lit8 v0, v0, 0x1
    goto :goto_2ff
    :cond_314
    move v0, v2
    :cond_315
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_336
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_336
    move v2, v0
    move v0, v1
    :goto_320
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_335
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_332
    const/16 v4, 0x1c
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_332
    add-int/lit8 v0, v0, 0x1
    goto :goto_320
    :cond_335
    move v0, v2
    :cond_336
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_357
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_357
    move v2, v0
    move v0, v1
    :goto_341
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_356
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_353
    const/16 v4, 0x1d
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_353
    add-int/lit8 v0, v0, 0x1
    goto :goto_341
    :cond_356
    move v0, v2
    :cond_357
    iget-object v2, p0, LTest;->t:[LTest;
    if-eqz v2, :cond_378
    iget-object v2, p0, LTest;->t:[LTest;
    array-length v2, v2
    if-lez v2, :cond_378
    move v2, v0
    move v0, v1
    :goto_362
    iget-object v3, p0, LTest;->t:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_377
    iget-object v3, p0, LTest;->t:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_374
    const/16 v4, 0x1e
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_374
    add-int/lit8 v0, v0, 0x1
    goto :goto_362
    :cond_377
    move v0, v2
    :cond_378
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_399
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_399
    move v2, v0
    move v0, v1
    :goto_383
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_398
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_395
    const/16 v4, 0x1f
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_395
    add-int/lit8 v0, v0, 0x1
    goto :goto_383
    :cond_398
    move v0, v2
    :cond_399
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_3ba
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_3ba
    move v2, v0
    move v0, v1
    :goto_3a4
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_3b9
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_3b6
    const/16 v4, 0x20
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_3b6
    add-int/lit8 v0, v0, 0x1
    goto :goto_3a4
    :cond_3b9
    move v0, v2
    :cond_3ba
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_3db
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_3db
    move v2, v0
    move v0, v1
    :goto_3c5
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_3da
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_3d7
    const/16 v4, 0x21
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_3d7
    add-int/lit8 v0, v0, 0x1
    goto :goto_3c5
    :cond_3da
    move v0, v2
    :cond_3db
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_3fc
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_3fc
    move v2, v0
    move v0, v1
    :goto_3e6
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_3fb
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_3f8
    const/16 v4, 0x22
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_3f8
    add-int/lit8 v0, v0, 0x1
    goto :goto_3e6
    :cond_3fb
    move v0, v2
    :cond_3fc
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_41d
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_41d
    move v2, v0
    move v0, v1
    :goto_407
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_41c
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_419
    const/16 v4, 0x23
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_419
    add-int/lit8 v0, v0, 0x1
    goto :goto_407
    :cond_41c
    move v0, v2
    :cond_41d
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_43e
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_43e
    move v2, v0
    move v0, v1
    :goto_428
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_43d
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_43a
    const/16 v4, 0x24
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_43a
    add-int/lit8 v0, v0, 0x1
    goto :goto_428
    :cond_43d
    move v0, v2
    :cond_43e
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_45f
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_45f
    move v2, v0
    move v0, v1
    :goto_449
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_45e
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_45b
    const/16 v4, 0x25
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_45b
    add-int/lit8 v0, v0, 0x1
    goto :goto_449
    :cond_45e
    move v0, v2
    :cond_45f
    iget-object v2, p0, LTest;->T:LTest;
    if-eqz v2, :cond_46c
    const/16 v2, 0x26
    iget-object v3, p0, LTest;->T:LTest;
    invoke-static {v2, v3}, LTest;->d(ILTest;)I
    move-result v2
    add-int/2addr v0, v2
    :cond_46c
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_48d
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_48d
    move v2, v0
    move v0, v1
    :goto_477
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_48c
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_489
    const/16 v4, 0x27
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_489
    add-int/lit8 v0, v0, 0x1
    goto :goto_477
    :cond_48c
    move v0, v2
    :cond_48d
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_4ae
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_4ae
    move v2, v0
    move v0, v1
    :goto_498
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_4ad
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_4aa
    const/16 v4, 0x28
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_4aa
    add-int/lit8 v0, v0, 0x1
    goto :goto_498
    :cond_4ad
    move v0, v2
    :cond_4ae
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_4cf
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_4cf
    move v2, v0
    move v0, v1
    :goto_4b9
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_4ce
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_4cb
    const/16 v4, 0x29
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_4cb
    add-int/lit8 v0, v0, 0x1
    goto :goto_4b9
    :cond_4ce
    move v0, v2
    :cond_4cf
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_4f0
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_4f0
    move v2, v0
    move v0, v1
    :goto_4da
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_4ef
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_4ec
    const/16 v4, 0x2a
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_4ec
    add-int/lit8 v0, v0, 0x1
    goto :goto_4da
    :cond_4ef
    move v0, v2
    :cond_4f0
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_511
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_511
    move v2, v0
    move v0, v1
    :goto_4fb
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_510
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_50d
    const/16 v4, 0x2b
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_50d
    add-int/lit8 v0, v0, 0x1
    goto :goto_4fb
    :cond_510
    move v0, v2
    :cond_511
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_532
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_532
    move v2, v0
    move v0, v1
    :goto_51c
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_531
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_52e
    const/16 v4, 0x2c
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_52e
    add-int/lit8 v0, v0, 0x1
    goto :goto_51c
    :cond_531
    move v0, v2
    :cond_532
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_553
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_553
    move v2, v0
    move v0, v1
    :goto_53d
    iget-object v3, p0, LTestObject;->a:[LTest;
    array-length v3, v3
    if-ge v0, v3, :cond_552
    iget-object v3, p0, LTestObject;->a:[LTest;
    aget-object v3, v3, v0
    if-eqz v3, :cond_54f
    const/16 v4, 0x2d
    invoke-static {v4, v3}, LTest;->d(ILTest;)I
    move-result v3
    add-int/2addr v2, v3
    :cond_54f
    add-int/lit8 v0, v0, 0x1
    goto :goto_53d
    :cond_552
    move v0, v2
    :cond_553
    iget-object v2, p0, LTestObject;->a:[LTest;
    if-eqz v2, :cond_571
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-lez v2, :cond_571
    :goto_55c
    iget-object v2, p0, LTestObject;->a:[LTest;
    array-length v2, v2
    if-ge v1, v2, :cond_571
    iget-object v2, p0, LTestObject;->a:[LTest;
    aget-object v2, v2, v1
    if-eqz v2, :cond_56e
    const/16 v3, 0x2e
    invoke-static {v3, v2}, LTest;->d(ILTest;)I
    move-result v2
    add-int/2addr v0, v2
    :cond_56e
    add-int/lit8 v1, v1, 0x1
    goto :goto_55c
    :cond_571
    iget-object v1, p0, LTest;->V:LTest;
    if-eqz v1, :cond_57e
    const/16 v1, 0x64
    iget-object v2, p0, LTest;->V:LTest;
    invoke-static {v1, v2}, LTest;->d(ILTest;)I
    move-result v1
    add-int/2addr v0, v1
    :cond_57e
    return v0
.end method

.method public final method2()I
    .registers 1
    .prologue
    const/4 v0, 0x1
    return v0
.end method
