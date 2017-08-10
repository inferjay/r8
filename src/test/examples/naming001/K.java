// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package naming001;

public class K {
  private int i;
  private int h;
  private final int i2 = 7;
  private static int j;
  private final static int i3 = 7;

  private static final Object o = "TAG";
  private static final String TAG = "TAG";
  private final String TAG2 = "TAG";

  static {
    j = 6;
  }

  {
    i = 6;
  }

  void keep() {
    h = 7;
  }
}

