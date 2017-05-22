# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

.class LTest;
.super Ljava/lang/Object;

# Fall through to catch block.
.method public test1()V
    .registers 1
    :try_start
    monitor-enter p0
    monitor-exit  p0
    :try_end
    return-void
    .catchall {:try_start .. :try_end} :try_end
.end method

.method public static test2()I
    .locals 1
    const v0, 0
    :try_start
    const v0, 1
    goto :return
    :try_end
    .catch Ljava/lang/Exception; {:try_start .. :try_end} :return
    .catch Ljava/lang/Throwable; {:try_start .. :try_end} :error
    :error
    move-exception v0
    const v0, 2
    :return
    return v0
.end method

# Dead catch block.
.method public test3()I
    .locals 1
    const v0, 0
    return v0
    :start
    nop
    :end
    .catchall {:start .. :end} :catch
    nop
    :catch
    nop
.end method

.method public static main([Ljava/lang/String;)V
    .locals 0
    return-void
.end method

.method public test4(I)V
    .locals 1
    const/4 v0, 0
    if-nez p0, :not_zero
    const/4 v0, 1
    goto :try_end
    :not_zero
    const/4 v0, 2
    :try_start
    invoke-static {}, Ltest/X;->f()V
    const/4 v0, 3
    :try_end
    return-void
    .catchall {:try_start .. :try_end} :try_end
.end method

.method public f()V
    .locals 0
    return-void
.end method
