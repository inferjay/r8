# Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

.class public final LTestObject;
.super Ljava/lang/Object;

.field private c:LTest;

.method public onClick(LTest;)V
    .registers 10
    const/4             v2, 0x00  # 0
    invoke-virtual      { v9 }, LTest;->getId()I
    move-result         v0
    const               v1, 0x7f0f01bf  # 2131689919
    if-ne               v0, v1, :label_247
    invoke-virtual      { v8 }, LTestObject;->getActivity()LTest;
    move-result-object  v0
    invoke-static       { v0, v9 }, LTest;->b(LTest;LTest;)Z
    iget-object         v0, v8, LTestObject;->c:LTest;
    invoke-virtual      { v0 }, LTest;->a()Z
    move-result         v0
    if-eqz              v0, :label_231
    iget-object         v0, v8, LTestObject;->d:Landroid/widget/Button;
    const/4             v1, 0x00  # 0
    invoke-virtual      { v0, v1 }, Landroid/widget/Button;->setEnabled(Z)V
    iget-object         v0, v8, LTestObject;->b:Landroid/widget/LinearLayout;
    if-nez              v0, :label_72
    const-string        v0, "a"
    const/4             v1, 0x05  # 5
    invoke-static       { v0, v1 }, Landroid/util/Log;->isLoggable(Ljava/lang/String;I)Z
    move-result         v0
    if-eqz              v0, :label_51
    const-string        v0, "b"
    const-string        v1, "c"
    invoke-static       { v0, v1 }, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I
  :label_51
    invoke-static       { v2 }, LTest;->c(LTest;)Z
    move-result         v0
    if-eqz              v0, :label_66
    iget-object         v0, v8, LTestObject;->a:LTest;
    sget-object         v1, Ljok;->d:LTest;
    sget-object         v3, Ljol;->c:LTest;
    invoke-virtual      { v0, v1, v3 }, LTest;->a(LTest;LTest;)V
  :label_66
    iget-object         v0, v8, LTestObject;->g:LTest;
    invoke-virtual      { v0, v2 }, LTest;->a(LTest;)V
  :label_71
    return-void
  :label_72
    new-instance        v5, Ljava/util/ArrayList;
    invoke-direct       { v5 }, Ljava/util/ArrayList;-><init>()V
    iget-object         v0, v8, LTestObject;->b:Landroid/widget/LinearLayout;
    invoke-direct       { v8, v0 }, LTestObject;->a(Landroid/widget/LinearLayout;)Ljava/util/List;
    move-result-object  v0
    invoke-interface    { v0 }, Ljava/util/List;->iterator()Ljava/util/Iterator;
    move-result-object  v6
    move-object         v1, v2
    move-object         v4, v2
  :label_89
    invoke-interface    { v6 }, Ljava/util/Iterator;->hasNext()Z
    move-result         v0
    if-eqz              v0, :label_151
    invoke-interface    { v6 }, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object  v0
    check-cast          v0, LTest;
    invoke-virtual      { v0 }, LTest;->c()LTest;
    move-result-object  v3
    if-eqz              v3, :label_148
    instance-of         v7, v0, LTest;
    if-eqz              v7, :label_127
    if-eqz              v1, :label_116
    invoke-virtual      { v5, v1 }, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z
  :label_116
    invoke-virtual      { v5, v3 }, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z
    if-eqz              v4, :label_124
    invoke-virtual      { v5, v4 }, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z
  :label_124
    move-object         v1, v2
    move-object         v4, v2
    goto                :label_89
  :label_127
    instance-of         v7, v0, LTest;
    if-eqz              v7, :label_133
    move-object         v1, v3
    goto                :label_89
  :label_133
    instance-of         v0, v0, LTest;
    if-eqz              v0, :label_139
    move-object         v4, v3
    goto                :label_89
  :label_139
    if-eqz              v4, :label_145
    invoke-virtual      { v5, v4 }, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z
    move-object         v4, v2
  :label_145
    invoke-virtual      { v5, v3 }, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z
  :label_148
    move-object         v3, v4
    move-object         v4, v3
    goto                :label_89
  :label_151
    if-eqz              v4, :label_156
    invoke-virtual      { v5, v4 }, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z
  :label_156
    new-instance        v1, LTest;
    invoke-direct       { v1 }, LTest;-><init>()V
    const-string        v0, "d"
    iput-object         v0, v1, LTest;->b:Ljava/lang/String;
    iget-object         v0, v1, LTest;->c:Ljava/util/Set;
    const/4             v3, 0x06  # 6
    invoke-static       { v3 }, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object  v3
    invoke-interface    { v0, v3 }, Ljava/util/Set;->add(Ljava/lang/Object;)Z
    new-instance        v3, LTest;
    invoke-direct       { v3 }, LTest;-><init>()V
    iput-object         v5, v3, LTest;->a:Ljava/util/List;
    iget-object         v0, v3, LTest;->b:Ljava/util/Set;
    const/4             v4, 0x02  # 2
    invoke-static       { v4 }, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object  v4
    invoke-interface    { v0, v4 }, Ljava/util/Set;->add(Ljava/lang/Object;)Z
    new-instance        v0, LTest;
    iget-object         v4, v3, LTest;->b:Ljava/util/Set;
    iget-object         v3, v3, LTest;->a:Ljava/util/List;
    invoke-direct       { v0, v4, v3 }, LTest;-><init>(Ljava/util/Set;Ljava/util/List;)V
    check-cast          v0, LTest;
    iput-object         v0, v1, LTest;->a:LTest;
    iget-object         v0, v1, LTest;->c:Ljava/util/Set;
    const/4             v3, 0x04  # 4
    invoke-static       { v3 }, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object  v3
    invoke-interface    { v0, v3 }, Ljava/util/Set;->add(Ljava/lang/Object;)Z
    new-instance        v0, LTest;
    iget-object         v3, v1, LTest;->c:Ljava/util/Set;
    iget-object         v4, v1, LTest;->a:LTest;
    iget-object         v1, v1, LTest;->b:Ljava/lang/String;
    invoke-direct       { v0, v3, v2, v4, v1 }, LTest;-><init>(Ljava/util/Set;LTest;LTest;Ljava/lang/String;)V
    check-cast          v0, LTest;
    move-object         v2, v0
    goto/16             :label_51
  :label_231
    iget-object         v0, v8, LTestObject;->c:LTest;
    const/16            v1, 0x0082  # 130
    invoke-virtual      { v0, v1 }, LTest;->pageScroll(I)Z
    iget-object         v0, v8, LTestObject;->a:LTest;
    sget-object         v1, Ljok;->k:LTest;
    invoke-virtual      { v0, v1 }, LTest;->a(LTest;)V
    goto/16             :label_71
  :label_247
    const               v1, 0x7f0f0204  # 2131689988
    if-ne               v0, v1, :label_71
    iget-object         v0, v8, LTestObject;->a:LTest;
    sget-object         v1, Ljok;->q:LTest;
    invoke-virtual      { v0, v1 }, LTest;->a(LTest;)V
    iget-object         v0, v8, LTestObject;->g:LTest;
    invoke-virtual      { v0 }, LTest;->g()V
    goto/16             :label_71
.end method

.method public getActivity()LTest;
  .registers 1
  const/4               v0, 0x00
  return-object         v0
.end method

.method private a(Landroid/widget/LinearLayout;)Ljava/util/List;
  .registers 2
  const/4               v0, 0x00
  return-object         v0
.end method
