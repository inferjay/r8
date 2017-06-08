// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.bridgeremoval.bridgestokeep;

// Reduced test case from code where removal of bridge methods caused failure.
public class Main {

  public static void registerObserver(DataAdapter dataAdapter) {
    dataAdapter.registerObserver(null);
  }

  public static void main(String[] args) {
    registerObserver(new SimpleDataAdapter());
  }
}
