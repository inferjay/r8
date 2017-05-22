// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package interfaceinlining;

// This test ensures a check cast instruction is inserted IFF
// the expression "((DataI) other).field()" is inlined.
// Failing to do so will result in an ART verification error.
public class Main {
  public interface DataI {
    int field();
  }
  public static class Data implements DataI {
    final int a;
    public boolean equals(Object other) {
      if (other instanceof DataI) {
        return a == ((DataI) other).field();
      }
      return false;
    }
    Data(int a) {
      this.a = a;
    }
    public int field() {
      return a;
    }
  }
  public static void main (String[] args) {
    System.out.print(new Data(1).equals(new Data(1)));
  }
}
