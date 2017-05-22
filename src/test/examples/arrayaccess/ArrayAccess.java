// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package arrayaccess;

public class ArrayAccess {

  public static int loadStoreBoolean(int i, boolean b) {
    boolean[] array = new boolean[i + 2];
    array[i] = b;
    array[i + 1] = !array[i];
    return (array[i] ? 1 : 0) + (array[i + 1] ? 1 : 0);
  }

  public static int loadStoreByte(int i) {
    byte[] array = new byte[i + 2];
    array[i] = 1;
    array[i + 1] = (byte) (array[i] + (byte) 1);
    return array[i] + array[i + 1];
  }

  public static int loadStoreChar(int i) {
    char[] array = new char[i + 2];
    array[i] = 1;
    array[i + 1] = (char) (array[i] + (char) 1);
    return array[i] + array[i + 1];
  }

  public static int loadStoreShort(int i) {
    short[] array = new short[i + 2];
    array[i] = 1;
    array[i + 1] = (short) (array[i] + (short) 1);
    return array[i] + array[i + 1];
  }

  public static float loadStoreFloat(int i) {
    float[] array = new float[i + 2];
    array[i] = 1.0f;
    array[i + 1] = array[i] + 1.0f;
    return array[i] + array[i + 1];
  }

  public static double loadStoreDouble(int i) {
    double[] array = new double[i + 2];
    array[i] = 1.0;
    array[i + 1] = array[i] + 1.0;
    return array[i] + array[i + 1];
  }

  public static int loadStoreObject(int i, Object o) {
    Object[] array = new Object[i + 2];
    array[i] = o;
    array[i + 1] = o;
    return 1 + (array[i].hashCode() - array[i + 1].hashCode());
  }

  public static int loadStoreArray(int i, Object[] os) {
    Object[][] array = new Object[i + 2][];
    array[i] = os;
    array[i + 1] = os;
    return array[i].length + array[i + 1].length;
  }

  public static void main(String[] args) {
    int i = 0;
    i += loadStoreBoolean(1, true);
    i += loadStoreByte(0);
    i += loadStoreChar(1);
    i += loadStoreShort(2);
    i += loadStoreFloat(3);
    i += loadStoreDouble(4);
    i += loadStoreObject(1, "foo");
    i += loadStoreArray(1, new Object[10]);
    System.out.println("37=" + i);
  }
}