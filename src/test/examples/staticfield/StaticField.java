// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package staticfield;

public class StaticField {

  // Final static initialized fields, out of order, in dex these must be sorted by field idx.
  public static final String fieldB = "B";
  public static final String fieldC = "C";
  public static final String fieldA = "A";

  public static StaticField field = null;

  private int x;

  public StaticField(int x) {
    this.x = x;
  }

  @Override
  public String toString() {
    return "" + x;
  }

  public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
    StaticField value = new StaticField(101010);
    StaticField.field = value;
    System.out.println(StaticField.field);
    System.out.println(value.field);

    System.out.print(StaticField.fieldA);
    System.out.print(StaticField.fieldB);
    System.out.println(StaticField.fieldC);

    // Check that we can access the same static final value via the class object.
    System.out.print(StaticField.class.getField("fieldA").get(value));
    System.out.print(StaticField.class.getField("fieldB").get(value));
    System.out.println(StaticField.class.getField("fieldC").get(value));
  }
}
