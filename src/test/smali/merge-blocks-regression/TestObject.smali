# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

.class public final LTestObject;
.super Ljava/lang/Object;

.field public c:LTest;
.field public b:LTest;
.field public d:LTest;
.field public e:LTest;
.field public h:LTest;
.field public i:LTest;
.field public j:LTest;

.method public final f()V
    .registers 15
    const/16 v11, 0xd
    const/4 v2, 0x0
    const/4 v1, 0x1
    iget-object v3, p0, LTestObject;->c:LTest;
    iget-object v0, p0, LTestObject;->b:LTest;
    invoke-virtual {v0}, LTest;->bW_()LTest;
    move-result-object v0
    iget-object v4, p0, LTestObject;->b:LTest;
    invoke-virtual {v4}, LTest;->e()LTest;
    move-result-object v4
    new-instance v5, Ljava/io/File;
    invoke-virtual {v0}, LTest;->b()Ljava/io/File;
    move-result-object v0
    const-string v6, "nlp_state"
    invoke-direct {v5, v0, v6}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V
    invoke-virtual {v4}, LTest;->d()J
    move-result-wide v6
    invoke-virtual {v4}, LTest;->c()J
    move-result-wide v8
    :try_start_25
    new-instance v0, Ljava/io/FileInputStream;
    invoke-direct {v0, v5}, Ljava/io/FileInputStream;-><init>(Ljava/io/File;)V
    new-instance v4, Ljava/io/BufferedInputStream;
    invoke-direct {v4, v0}, Ljava/io/BufferedInputStream;-><init>(Ljava/io/InputStream;)V
    iget-object v0, v3, LTest;->g:LTest;
    invoke-virtual {v0}, LTest;->cB()[B
    :try_end_34
    .catch Ljava/io/FileNotFoundException; {:try_start_25 .. :try_end_34} :catch_bf
    .catch Ljava/lang/SecurityException; {:try_start_25 .. :try_end_34} :catch_c8
    .catch Ljava/io/IOException; {:try_start_25 .. :try_end_34} :catch_d8
    move-result-object v0
    :try_start_35
    new-instance v5, Ljava/io/DataInputStream;
    invoke-direct {v5, v4}, Ljava/io/DataInputStream;-><init>(Ljava/io/InputStream;)V
    invoke-virtual {v5}, Ljava/io/DataInputStream;->readUnsignedShort()I
    move-result v10
    if-ne v10, v11, :cond_c4
    if-ne v10, v11, :cond_b2
    iget-object v10, v3, LTest;->f:LTest;
    if-nez v10, :cond_4d
    const/4 v10, 0x0
    invoke-static {v0, v10}, LTest;->a([BLTest;)LTest;
    move-result-object v0
    iput-object v0, v3, LTest;->f:LTest;
    :cond_4d
    iget-object v0, v3, LTest;->f:LTest;
    invoke-virtual {v0, v5}, LTest;->a(Ljava/io/DataInputStream;)LTest;
    move-result-object v0
    iget-object v0, v0, LTest;->b:Ljava/lang/Object;
    check-cast v0, [B
    invoke-static {v0}, Ljava/nio/ByteBuffer;->wrap([B)Ljava/nio/ByteBuffer;
    :try_end_5a
    .catch Ljava/io/IOException; {:try_start_35 .. :try_end_5a} :catch_ba
    .catch Ljava/io/FileNotFoundException; {:try_start_35 .. :try_end_5a} :catch_bf
    .catch Ljava/lang/SecurityException; {:try_start_35 .. :try_end_5a} :catch_c8
    move-result-object v5
    :try_start_5b
    invoke-virtual {v5}, Ljava/nio/ByteBuffer;->getLong()J
    move-result-wide v10
    invoke-virtual {v5}, Ljava/nio/ByteBuffer;->getLong()J
    move-result-wide v12
    add-long/2addr v10, v12
    sub-long v6, v10, v6
    invoke-static {v8, v9, v6, v7}, Ljava/lang/Math;->min(JJ)J
    move-result-wide v6
    invoke-virtual {v5}, Ljava/nio/ByteBuffer;->get()B
    move-result v0
    if-ne v0, v1, :cond_cd
    move v0, v1
    :goto_71
    invoke-virtual {v3, v6, v7, v0}, LTest;->a(JZ)V
    :try_end_74
    .catch Ljava/lang/IllegalArgumentException; {:try_start_5b .. :try_end_74} :catch_cf
    .catch Ljava/nio/BufferUnderflowException; {:try_start_5b .. :try_end_74} :catch_dd
    .catch Ljava/io/IOException; {:try_start_5b .. :try_end_74} :catch_ba
    .catch Ljava/io/FileNotFoundException; {:try_start_5b .. :try_end_74} :catch_bf
    .catch Ljava/lang/SecurityException; {:try_start_5b .. :try_end_74} :catch_c8
    :try_start_74
    iget-object v0, v3, LTest;->d:LTest;
    invoke-virtual {v0, v5}, LTest;->c(Ljava/nio/ByteBuffer;)V
    iget-object v0, v3, LTest;->e:LTest;
    invoke-virtual {v0, v5}, LTest;->c(Ljava/nio/ByteBuffer;)V
    :try_end_7e
    .catch Ljava/io/IOException; {:try_start_74 .. :try_end_7e} :catch_ba
    .catch Ljava/io/FileNotFoundException; {:try_start_74 .. :try_end_7e} :catch_bf
    .catch Ljava/lang/SecurityException; {:try_start_74 .. :try_end_7e} :catch_c8
    :goto_7e
    :try_start_7e
    invoke-virtual {v4}, Ljava/io/BufferedInputStream;->close()V
    :try_end_81
    .catch Ljava/io/FileNotFoundException; {:try_start_7e .. :try_end_81} :catch_bf
    .catch Ljava/lang/SecurityException; {:try_start_7e .. :try_end_81} :catch_c8
    .catch Ljava/io/IOException; {:try_start_7e .. :try_end_81} :catch_d8
    :goto_81
    iget-object v0, p0, LTestObject;->i:LTest;
    invoke-virtual {v0, v1}, LTest;->c(Z)V
    iget-object v0, p0, LTestObject;->i:LTest;
    invoke-virtual {v0, v2}, LTest;->d(Z)V
    iget-object v0, p0, LTestObject;->d:LTest;
    if-eqz v0, :cond_94
    iget-object v0, p0, LTestObject;->d:LTest;
    invoke-virtual {v0}, LTest;->a()V
    :cond_94
    iget-object v0, p0, LTestObject;->e:LTest;
    if-eqz v0, :cond_9d
    iget-object v0, p0, LTestObject;->e:LTest;
    invoke-virtual {v0}, LTest;->eV()V
    :cond_9d
    iget-object v0, p0, LTestObject;->h:LTest;
    iget-object v1, p0, LTestObject;->b:LTest;
    invoke-virtual {v1}, LTest;->e()LTest;
    move-result-object v1
    invoke-virtual {v1}, LTest;->c()J
    move-result-wide v2
    invoke-virtual {v0, v2, v3}, LTest;->a(J)V
    iget-object v0, p0, LTestObject;->j:LTest;
    invoke-virtual {v0}, LTest;->a()V
    return-void
    :cond_b2
    :try_start_b2
    new-instance v0, Ljava/io/IOException;
    const-string v4, "Incompatible version."
    invoke-direct {v0, v4}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V
    throw v0
    :try_end_ba
    .catch Ljava/io/IOException; {:try_start_b2 .. :try_end_ba} :catch_ba
    .catch Ljava/io/FileNotFoundException; {:try_start_b2 .. :try_end_ba} :catch_bf
    .catch Ljava/lang/SecurityException; {:try_start_b2 .. :try_end_ba} :catch_c8
    :catch_ba
    move-exception v0
    :try_start_bb
    invoke-virtual {v3, v8, v9}, LTest;->a(J)V
    throw v0
    :try_end_bf
    .catch Ljava/io/FileNotFoundException; {:try_start_bb .. :try_end_bf} :catch_bf
    .catch Ljava/lang/SecurityException; {:try_start_bb .. :try_end_bf} :catch_c8
    .catch Ljava/io/IOException; {:try_start_bb .. :try_end_bf} :catch_d8
    :catch_bf
    move-exception v0
    invoke-virtual {v3, v8, v9}, LTest;->a(J)V
    goto :goto_81
    :cond_c4
    :try_start_c4
    invoke-virtual {v3, v8, v9}, LTest;->a(J)V
    :try_end_c7
    .catch Ljava/io/IOException; {:try_start_c4 .. :try_end_c7} :catch_ba
    .catch Ljava/io/FileNotFoundException; {:try_start_c4 .. :try_end_c7} :catch_bf
    .catch Ljava/lang/SecurityException; {:try_start_c4 .. :try_end_c7} :catch_c8
    goto :goto_7e
    :catch_c8
    move-exception v0
    invoke-virtual {v3, v8, v9}, LTest;->a(J)V
    goto :goto_81
    :cond_cd
    move v0, v2
    goto :goto_71
    :catch_cf
    move-exception v0
    :goto_d0
    :try_start_d0
    new-instance v4, Ljava/io/IOException;
    const-string v5, "Byte buffer read failed."
    invoke-direct {v4, v5, v0}, Ljava/io/IOException;-><init>(Ljava/lang/String;Ljava/lang/Throwable;)V
    throw v4
    :try_end_d8
    .catch Ljava/io/IOException; {:try_start_d0 .. :try_end_d8} :catch_ba
    .catch Ljava/io/FileNotFoundException; {:try_start_d0 .. :try_end_d8} :catch_bf
    .catch Ljava/lang/SecurityException; {:try_start_d0 .. :try_end_d8} :catch_c8
    :catch_d8
    move-exception v0
    invoke-virtual {v3, v8, v9}, LTest;->a(J)V
    goto :goto_81
    :catch_dd
    move-exception v0
    goto :goto_d0
.end method
