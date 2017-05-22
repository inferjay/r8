// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

// This code is not run directly. It needs to be compiled to dex code.
// 'stringbuilding.dex' is what is run.

package stringbuilding;

class StringBuilding {

  static class X {

    public String toString() {
      return "an X";
    }
  }

  public static void main(String[] args) {
    StringBuilder builder = new StringBuilder();
    buildWithStatements(builder);
    buildWithExpressions(builder);
    System.out.print(builder);
    System.out.println(buildWithConcat());
    System.out.print(buildWithAllAppendSignatures());
  }

  private static void buildWithStatements(StringBuilder builder) {
    builder.append("a");
    builder.append(2);
    builder.append("c");
    builder.append("-");
  }

  private static void buildWithExpressions(StringBuilder builder) {
    builder.append("x").append('y').append("z").append("-");
  }

  private static String buildWithConcat() {
    return "a" + "b" + "c" + someValue() + "x" + "y" + "z";
  }

  private static String buildWithAllAppendSignatures() {
    CharSequence seq = "1234";
    StringBuilder builder = new StringBuilder();
    builder
        .append(true)
        .append('A')
        .append(new char[]{'B', 'C'})
        .append(new char[]{'C', 'D', 'E', 'F'}, 1, 2)
        .append(seq)
        .append(seq, 1, 3)
        .append(2.2)
        .append(1.1f)
        .append(0)
        .append(1L)
        .append(new X())
        .append("string")
        .append(new StringBuilder("builder"));
    return builder.toString();
  }

  private static int someValue() {
    return 7;
  }
}