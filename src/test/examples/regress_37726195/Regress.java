// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package regress_37726195;

public class Regress {

  // Regression test for issue where aput instructions for different primitive array types
  // were joined. The art verifier doesn't allow that.
  public static void set(Object array, int index, byte value) {
    if (array instanceof float[]) {
      float[] floats = (float[]) array;
      floats[index] = value;
    } else if (array instanceof int[]) {
      int[] ints = (int[]) array;
      ints[index] = value;
    }
  }

  // Regression test for issue where aget instructions for different primitive array types
  // were joined. The art verifier doesn't allow that.
  public static void get(Object array, int index) {
    if (array instanceof float[]) {
      float[] floats = (float[]) array;
      float f = floats[index];
    } else if (array instanceof int[]) {
      int[] ints = (int[]) array;
      int i = ints[index];
    }
  }

  public static void main(String[] args) {
    int[] ints = { 0 };
    float[] floats = { 0.0f };
    set(ints, 0, (byte) 4);
    System.out.println(ints[0]);
    set(floats, 0, (byte) 4);
    System.out.println(floats[0]);
    get(ints, 0);
    get(floats, 0);
  }
}
