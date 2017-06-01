// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package switchmaps;

public class Switches {

  public static void main(String... args) {
    for (Days value : Days.values()) {
      switchWithDefault(value);
      switchFull(value);
    }
    for (Colors color : Colors.values()) {
      switchOnColors(color);
    }
  }

  private static void switchOnColors(Colors color) {
    System.out.println(color.toString());
    switch (color) {
      case GRAY:
        System.out.println("not really");
        break;
      case GREEN:
        System.out.println("sooo green");
        break;
      default:
        System.out.println("colorful");
    }
  }

  private static void switchWithDefault(Days day) {
    switch (day) {
      case WEDNESDAY:
      case FRIDAY:
        System.out.println("3 or 5");
        break;
      case SUNDAY:
        System.out.println("7");
        break;
      default:
        System.out.println("other");
    }
  }

  private static void switchFull(Days day) {
    switch (day) {
      case MONDAY:
      case WEDNESDAY:
      case THURSDAY:
        System.out.println("1, 3 or 4");
      case TUESDAY:
      case FRIDAY:
        System.out.println("2 or 5");
        break;
      case SUNDAY:
        System.out.println("7");
        break;
      case SATURDAY:
        System.out.println("6");
        break;
      default:
        System.out.println("other");
    }
  }
}
