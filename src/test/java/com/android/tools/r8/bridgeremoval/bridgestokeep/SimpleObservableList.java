// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.bridgeremoval.bridgestokeep;

import com.android.tools.r8.bridgeremoval.bridgestokeep.ObservableList.Observer;

public class SimpleObservableList<O extends Observer>
    implements ObservableList<O> {

  @Override
  public void registerObserver(O observer) {
  }
}