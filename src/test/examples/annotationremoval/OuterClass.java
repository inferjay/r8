// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package annotationremoval;

public class OuterClass {
  public class InnerClass {
    private int value;

    public InnerClass(int x) {
      this.value = x;
    }

    int computeAResult(int y) {
      int result = 1;
      for (int i = value; i < y; i++) {
        result++;
        if (result == 1) {
          return result;
        }
      }
      return value * y;
    }
  }

  public abstract class MagicClass {
    public abstract int returnAnInt();
  }

  public int getValueFromInner(int x) {
    class LocalMagic extends MagicClass {

      @Override
      public int returnAnInt() {
        return 123;
      }
    }

    InnerClass inner = new InnerClass(x);
    MagicClass magic = new MagicClass() {

      @Override
      public int returnAnInt() {
        return 124;
      }
    };
    MagicClass localMagic = new LocalMagic();
    return inner.computeAResult(42) + magic.returnAnInt() + localMagic.returnAnInt();
  }
}
