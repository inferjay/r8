// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

public class DebugLambda {

  interface I {
    int getInt();
  }

  private static void printInt(I i) {
    System.out.println(i.getInt());
  }

  public static void testLambda(int i, int j) {
    printInt(() -> i + j);
  }

  private static void printInt2(I i) {
    System.out.println(i.getInt());
  }

  public static void testLambdaWithMethodReference() {
    printInt2(DebugLambda::returnOne);
  }

  private static int returnOne() {
    return 1;
  }

  private static void printInt3(BinaryOpInterface i, int a, int b) {
    System.out.println(i.binaryOp(a, b));
  }

  public static void testLambdaWithArguments(int i, int j) {
    printInt3((a, b) -> {
      return a + b;
    }, i, j);
  }

  interface ObjectProvider {
    Object foo(String a, String b, String c);
  }

  private static void testLambdaWithMethodReferenceAndConversion(ObjectProvider objectProvider) {
    System.out.println(objectProvider.foo("A", "B", "C"));
  }

  public static void main(String[] args) {
    DebugLambda.testLambda(5, 10);
    DebugLambda.testLambdaWithArguments(5, 10);
    DebugLambda.testLambdaWithMethodReference();
    DebugLambda.testLambdaWithMethodReferenceAndConversion(DebugLambda::concatObjects);
  }

  private static Object concatObjects(Object... objects) {
    StringBuilder sb = new StringBuilder();
    for (Object o : objects) {
      sb.append(o.toString());
    }
    return sb.toString();
  }

  interface BinaryOpInterface {
    int binaryOp(int a, int b);
  }
}
