// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package nestedtrycatches;

public class NestedTryCatches {
  private static void throwException() {
    throw new RuntimeException("IGNORED");
  }

  private static void test() throws Throwable {
    RuntimeException _primaryExc = null;
    try {
      throw new RuntimeException("PRIMARY");
    } catch (RuntimeException _t) {
      _primaryExc = _t;
      throw _t;
    } finally {
      // Keep the two calls to throwException() the same line
      if(_primaryExc!=null) {
        try {
          throwException();
        } catch(Throwable _suppressed) {
        }
      } else {
        throwException();
      }
    }
  }

  public static void main(String[] args) throws Exception {
    try {
      test();
    } catch (Throwable e) {
      System.out.println("EXCEPTION: " + e.getMessage());
    }
  }
}
