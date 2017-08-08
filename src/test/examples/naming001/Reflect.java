// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package naming001;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class Reflect {
  void keep() throws ClassNotFoundException {
    Class.forName("naming001.Reflect2");
    Class.forName("ClassThatDoesNotExists");
  }

  void keep2() throws NoSuchFieldException, SecurityException {
    Reflect2.class.getField("fieldPublic");
    Reflect2.class.getField("fieldPrivate");
  }

  void keep3() throws NoSuchFieldException, SecurityException {
    Reflect2.class.getDeclaredField("fieldPublic");
    Reflect2.class.getDeclaredField("fieldPrivate");
  }

  void keep4() throws SecurityException, NoSuchMethodException {
    Reflect2.class.getMethod("m", new Class[] {naming001.Reflect2.A.class});
    Reflect2.class.getMethod("m", new Class[] {naming001.Reflect2.B.class});
    Reflect2.class.getMethod("methodThatDoesNotExist",
        new Class[] {naming001.Reflect2.A.class});
  }

  void keep5() throws SecurityException, NoSuchMethodException {
    Reflect2.class.getDeclaredMethod("m", new Class[] {naming001.Reflect2.A.class});
    Reflect2.class.getDeclaredMethod("m", new Class[] {naming001.Reflect2.B.class});
  }

  void keep6() throws SecurityException {
    AtomicIntegerFieldUpdater.newUpdater(Reflect2.class, "fieldPublic");
  }

  void keep7() throws SecurityException {
    AtomicLongFieldUpdater.newUpdater(Reflect2.class, "fieldLong");
    AtomicLongFieldUpdater.newUpdater(Reflect2.class, "fieldLong2");
  }

  void keep8() throws SecurityException {
    AtomicReferenceFieldUpdater.newUpdater(Reflect2.class, Reflect2.A.class, "a");
    AtomicReferenceFieldUpdater.newUpdater(Reflect2.class, Reflect2.A.class, "b");
    AtomicReferenceFieldUpdater.newUpdater(Reflect2.class, Object.class, "c");
  }
}

