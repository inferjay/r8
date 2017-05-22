// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

// This code is not run directly. It needs to be compiled to dex code.
// 'switches.dex' is what is run.

package switches;

class Switches {

  public static void packedSwitch(int value) {
    System.out.print("packedSwitch cases: ");
    switch (value) {
      case 0:
        System.out.print("0 ");
      case 1:
      case 2:
        System.out.print("1 2 ");
        break;
      case 3:
        System.out.print("3 ");
        break;
    }
    System.out.println("after switch " + value);
  }

  public static void sparseSwitch(int value) {
    switch (value) {
      case 0:
        System.out.println("0 ");
      case 100:
        System.out.println("100 ");
        break;
      case 200:
        System.out.println("200 ");
        break;
    }
    System.out.println("after switch " + value);
  }

  public static void switchWithLocals(int value) {
    switch (value) {
      case 0: {
        int i = 42;
        System.out.println(" " + i + value);
        break;
      }
      case 2: {
        double d = 1.0;
        System.out.println(" " + d + value);
        break;
      }
    }
  }

  public static void maybePackedSwitch(int value) {
    switch (value) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
      case 8:
      case 10:
      case 11:
      case 12:
      case 13:
      case 14:
      case 15:
      case 16:
      case 17:
      case 18:
      case 19:
      case 20:
      case 21:
        System.out.print("0-21 ");
        break;
      case 60:
        System.out.print("60 ");
        break;
    }
    System.out.println("after switch " + value);
  }

  public static void main(String[] args) {
    packedSwitch(0);
    packedSwitch(1);
    packedSwitch(2);
    packedSwitch(-1);  // No such case, use fallthrough.
    sparseSwitch(0);
    sparseSwitch(100);
    sparseSwitch(200);
    sparseSwitch(-1);  // No such case, use fallthrough.
    switchWithLocals(0);
    switchWithLocals(2);
    maybePackedSwitch(1);
    maybePackedSwitch(10);
    maybePackedSwitch(40);  // Fallthrough.
    maybePackedSwitch(60);
  }
}
