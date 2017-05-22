// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package barray;

public class BArray {

  public static void main(String[] args) {
    boolean[] boolArray = null;
    byte[] byteArray = null;
    boolean bool;
    byte bits;
    try {
      bool = boolArray[0] || boolArray[1];
    } catch (Throwable e) {
      bool = true;
    }
    try {
      bits = byteArray[0];
    } catch (Throwable e) {
      bits = 42;
    }
    System.out.println("bits " + bits + " and bool " + bool);
  }
}
