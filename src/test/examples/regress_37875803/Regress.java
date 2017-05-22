// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package regress_37875803;

public class Regress {

  private static int digitSub(int codePoint) {
    int result = -888;
    if ('a' <= codePoint && codePoint <= 'z') {
      result = 10 + (codePoint - 'a');
    } else if ('A' <= codePoint && codePoint <= 'Z') {
      result = 10 + (codePoint - 'A');
    }
    if (result < 0) {
      throw new RuntimeException("codePoint = " + codePoint + " result = " + result);
    }
    return result;
  }

  private static int digitSubLeft(int codePoint) {
    int result = -888;
    if ('a' <= codePoint && codePoint <= 'z') {
      result = 10 + ('a' - codePoint);
    } else if ('A' <= codePoint && codePoint <= 'Z') {
      result = 10 + ('A' - codePoint);
    }
    if (result < 0) {
      throw new RuntimeException("codePoint = " + codePoint + " result = " + result);
    }
    return result;
  }

  private static int digitAdd(int codePoint) {
    int result = -888;
    if ('a' <= codePoint && codePoint <= 'z') {
      result = 10 + (codePoint + 'a');
    } else if ('A' <= codePoint && codePoint <= 'Z') {
      result = 10 + (codePoint + 'A');
    }
    if (result < 0) {
      throw new RuntimeException("codePoint = " + codePoint + " result = " + result);
    }
    return result;
  }

  private static int digitOr(int codePoint) {
    int result = -888;
    if ('a' <= codePoint && codePoint <= 'z') {
      result = 10 + (codePoint | 'a');
    } else if ('A' <= codePoint && codePoint <= 'Z') {
      result = 10 + (codePoint | 'A');
    }
    if (result < 0) {
      throw new RuntimeException("codePoint = " + codePoint + " result = " + result);
    }
    return result;
  }

  public static void main(String[] args) {
    System.out.println(digitSub((int) 'a'));
    System.out.println(digitSub((int) 'A'));
    System.out.println(digitSubLeft((int) 'a'));
    System.out.println(digitSubLeft((int) 'A'));
    System.out.println(digitAdd((int) 'a'));
    System.out.println(digitAdd((int) 'A'));
    System.out.println(digitOr((int) 'a'));
    System.out.println(digitOr((int) 'A'));
  }
}
