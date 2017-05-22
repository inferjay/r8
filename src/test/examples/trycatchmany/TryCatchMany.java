// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

// This code is not run directly. It needs to be compiled to dex code.
// 'trycatchmany.dex' is what is run.

package trycatchmany;

class TryCatchMany {
  public static void main(String[] args) {
    try {
      foo();
      try {
        bar();
      } catch (RuntimeException e) {
        System.out.println("Another error");
      }
      foo();
    } catch (IllegalStateException e) {
      System.out.print("Error again");
    } catch (RuntimeException e) {
      System.out.print("Success");
    } finally {
      System.out.print("!");
    }
  }

  private static void foo() {
    try {
      throw new RuntimeException();
    } catch (IllegalStateException e) {
      System.out.println("Error");
    }
  }

  private static void bar() {
    throw new IllegalStateException();
  }
}
