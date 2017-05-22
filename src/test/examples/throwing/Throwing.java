// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

// This code is not run directly. It needs to be compiled to dex code.
// 'throwing.dex' is what is run.

package throwing;

import java.util.Collections;
import java.util.List;

class Throwing {

  static int[] used = new int[10];

  public static void main(String[] args) {
    try {
      used[0] = throwAtFistLine(42);
    } catch (Exception e) {
      printFrameHead(e);
    }
    try {
      used[1] = throwInMiddle(42);
    } catch (Exception e) {
      printFrameHead(e);
    }
    try {
      used[2] = throwAfterMultiInline(42);
    } catch (Exception e) {
      printFrameHead(e);
    }
    try {
      int value = magicNumber(42);
      // This throws after an inline, on the top-level.
      used[6] = value * 10;
      used[7] = anotherInlinedFunction(value);
      //
      // Some space to increase line numbers...
      //
      used[8] = value / (value & 0x0);
    } catch (Exception e) {
      printFrameHead(e);
    }

    Nested nested = new Nested();

    try {
      used[3] = nested.justThrow(42);
    } catch (Exception e) {
      printFrameHead(e);
    }

    nested.doSomethingUseless();

    used[0] += Nested.callAMethod(nested, 11);
    used[0] += Nested.callAMethod(nested, 42);

    RenamedClass aInstance = RenamedClass.create();
    aInstance.takeThingsForASpin(42);

    System.out.print(used[0]);

    try {
      throwInAFunctionThatIsNotInlinedAndCalledTwice();
    } catch (Exception e) {
      printFrameHead(e);
    }

    try {
      throwInAFunctionThatIsNotInlinedAndCalledTwice();
    } catch (Exception e) {
      printFrameHead(e);
    }

    try {
      aFunctionThatCallsAnInlinedMethodThatThrows(Collections.emptyList());
    } catch (Exception e) {
      printFrameHead(e);
    }

    try {
      anotherFunctionThatCallsAnInlinedMethodThatThrows("string");
    } catch (Exception e) {
      printFrameHead(e);
    }

    try {
      aFunctionsThatThrowsBeforeAnInlinedMethod(magicNumber(42));
    } catch (Exception e) {
      printFrameHead(e);
    }
  }

  public static int magicNumber(int value) {
    if (value < 0) {
      return magicNumber(value++);
    }
    return value;
  }

  public static void printFrameHead(Exception e) {
    for (StackTraceElement element : e.getStackTrace()) {
      System.out.println("FRAME: " + element);
    }
  }

  // This throws in the first line of the method.
  public static int throwAtFistLine(int value) {
    int aValue = value * 2 / (value & 0x0);
    return aValue;
  }

  // This throws a little further down.
  public static int throwInMiddle(int value) {
    used[2] = value * 10;
    used[3] = value >> 3;
    used[4] = value / (value & 0x0);
    used[5] = value * 20;
    return value >> 5;
  }

  // This throws after another inlined function.
  public static int throwAfterMultiInline(int value) {
    used[6] = value * 10;
    used[7] = anotherInlinedFunction(value);
    //
    // Some space to increase line numbers...
    //
    used[8] = value / (value & 0x0);
    return value >> 5;
  }

  public static int throwInAFunctionThatIsNotInlinedAndCalledTwice() {
    for (int i = 0; i < 10; i++) {
      used[9] += i;
      System.out.println("Increment by one!");
    }
    System.out.println("Incremented by 10.");
    used[9] = used[9] / (used[9] & 0x0);
    return used[9];
  }

  // Small method that throws and can be inlined.
  private static int anotherThrowingMethodToInline(int value) {
    used[4] = value / (value & 0x0);
    return value >> 5;
  }

  // It is important that this function uses an argument type that is otherwise unused, so it gets
  // the same minified name.
  public static int aFunctionThatCallsAnInlinedMethodThatThrows(List aList) {
    used[9] = aList.size();
    for (int i = 0; i < 10; i++) {
      used[9] += i;
      System.out.println("Increment by one!");
    }
    System.out.println("Incremented by 10.");
    used[9] = anotherThrowingMethodToInline(used[9]);
    return used[9];
  }

  // Small method that throws and can be inlined.
  private static int yetAnotherThrowingMethodToInline(int value) {
    used[5] = value / (value & 0x0);
    return value >> 5;
  }

  // It is important that this function uses an argument type that is otherwise unused, so it gets
  // the same minified name.
  public static int anotherFunctionThatCallsAnInlinedMethodThatThrows(String aString) {
    used[0] = aString.length();
    for (int i = 0; i < 10; i++) {
      used[8] += i;
      System.out.println("Increment by one!");
    }
    System.out.println("Incremented by 10.");
    used[8] = yetAnotherThrowingMethodToInline(used[8]);
    return used[8];
  }

  public static int aFunctionsThatThrowsBeforeAnInlinedMethod(int value) {
    used[1] = value / (value & 0x0);
    anotherInlinedFunction(used[1]);
    return used[1];
  }

  // This will be inlined above but does not throw
  public static int anotherInlinedFunction(int value) {
    return value / (value & 0xff);
  }

  /**
   * A nested class with different kind of methods to have inlining from a nested class and also
   * renamings of a nested class in the mapping file.
   *
   * <p>Some methods are recursive to avoid inlining.
   */
  static class Nested {

    int justThrow(int value) {
      return used[8] = value / (value & 0x0);
    }

    // This will also be inlined. Not used in test but for generating interesting mapping files.
    void doSomethingUseless() {
      Throwing.used[9] = 11;
    }

    static int callAMethod(Nested on, int value) {
      if (value > 20) {
        return callAMethod(on, value - 1);
      } else {
        return on.aMethod(value);
      }
    }

    int aMethod(int value) {
      if (value > 10) {
        return aMethod(value - 1);
      } else {
        return value;
      }
    }
  }

}
