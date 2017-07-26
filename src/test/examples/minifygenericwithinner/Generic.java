// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package minifygenericwithinner;

import java.util.List;
import java.util.Map;

public class Generic<T extends AA> {

  public <U extends List> T m (Object o, T[] t, Map<T,U> m) {
    return null;
  }

  public <U extends Map> T m2 (Object o, T[] t, Map<T,U> m) {
    return null;
  }

  public <U extends List> T m3 (Object o, T t, Map<T,U> m) {
    return null;
  }

  public <U extends List> T m4 (Object o, T[] t, List<U> m) {
    return null;
  }

  public <V extends BB> Inner<V> getInner(V obj) {
    return new Inner<>();
  }

  public class Inner<V extends BB> {

    public Generic<T>.Inner<V> f;
    public <U extends List> T m5 (V o, T[] t, Map<T,U> m) {
      return m(o, t, m);
    }

   public Generic<T>.Inner<V> get() {
      return this;
    }
  }
}
