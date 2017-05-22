// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package shaking2;

public class StaticFields {
  public static int used = 42;
  // Unused but initialized by <clinit>.
  public static int unused = -42;
  // Not even used by <clinit>.
  public static int completelyUnused;

  public static int readInt;
  public static int writeInt;
  public static boolean readBoolean;
  public static boolean writeBoolean;
  public static byte readByte;
  public static byte writeByte;
  public static char readChar;
  public static char writeChar;
  public static Object readObject;
  public static Object writeObject;
  public static short readShort;
  public static short writeShort;
  public static double readDouble;
  public static double writeDouble;
}