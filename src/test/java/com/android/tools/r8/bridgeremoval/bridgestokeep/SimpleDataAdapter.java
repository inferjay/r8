// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.bridgeremoval.bridgestokeep;

public class SimpleDataAdapter
    extends SimpleObservableList<DataAdapter.Observer>
    implements DataAdapter {

  public SimpleDataAdapter() {
  }

  // This class has no implementation of method registerObserver(DataAdapter.Observer observer)
  // from interface DataAdapter. There is one in SimpleObservableList<DataAdapter.Observer> so
  // javac inserts a bridge with signature registerObserver(DataAdapter.Observer observer).
}