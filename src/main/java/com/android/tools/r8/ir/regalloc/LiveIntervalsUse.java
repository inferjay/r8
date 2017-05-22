// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.regalloc;

public class LiveIntervalsUse implements Comparable<LiveIntervalsUse> {
  private int position;
  private int limit;

  public LiveIntervalsUse(int position, int limit) {
    this.position = position;
    this.limit = limit;
  }

  public int getPosition() {
    return position;
  }

  public int getLimit() {
    return limit;
  }

  @Override
  public int hashCode() {
    return position + limit * 7;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof LiveIntervalsUse)) {
      return false;
    }
    LiveIntervalsUse o = (LiveIntervalsUse) other;
    return o.position == position && o.limit == limit;
  }

  @Override
  public int compareTo(LiveIntervalsUse o) {
    if (o.position != position) {
      return position - o.position;
    }
    return limit - o.limit;
  }
}
