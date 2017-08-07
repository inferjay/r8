// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package enumproto;


import enumproto.GeneratedEnumProto.Enum;
import enumproto.three.GeneratedEnumProto.EnumThree;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Enumproto {

  private static final byte[] WITH_ALL_FIELDS = new byte[]{6, 8, 42, 16, 2, 24, 3};
  private static final byte[] WITH_DEFAULT_FOR_ENUM = new byte[]{2, 8, 42};


  public static void main(String... args) throws IOException {
    readProtoAndPrintDaEnum(WITH_ALL_FIELDS);
    readProtoAndPrintDaEnum(WITH_DEFAULT_FOR_ENUM);
    readProtoThreeAndPrintDaEnum(WITH_ALL_FIELDS);
    readProtoThreeAndPrintDaEnum(WITH_DEFAULT_FOR_ENUM);
    roundTrip(WITH_ALL_FIELDS);
    roundTrip(WITH_DEFAULT_FOR_ENUM);
    roundTripThree(WITH_ALL_FIELDS);
    roundTripThree(WITH_DEFAULT_FOR_ENUM);
  }

  private static void readProtoAndPrintDaEnum(byte[] bytes) throws IOException {
    ByteArrayInputStream input = new ByteArrayInputStream(bytes);
    Enum.Builder builder = Enum.newBuilder();
    builder.mergeDelimitedFrom(input);
    Enum buffer = builder.build();
    System.out.println(buffer.getEnum());
  }

  private static void readProtoThreeAndPrintDaEnum(byte[] bytes) throws IOException {
    ByteArrayInputStream input = new ByteArrayInputStream(bytes);
    EnumThree.Builder builder = EnumThree.newBuilder();
    builder.mergeDelimitedFrom(input);
    EnumThree buffer = builder.build();
    System.out.println(buffer.getEnum());
  }

  private static void roundTrip(byte[] bytes) throws IOException {
    ByteArrayInputStream input = new ByteArrayInputStream(bytes);
    Enum.Builder builder = Enum.newBuilder();
    builder.mergeDelimitedFrom(input);
    Enum buffer = builder.build();
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    buffer.writeDelimitedTo(output);
    readProtoAndPrintDaEnum(output.toByteArray());
  }

  private static void roundTripThree(byte[] bytes) throws IOException {
    ByteArrayInputStream input = new ByteArrayInputStream(bytes);
    EnumThree.Builder builder = EnumThree.newBuilder();
    builder.mergeDelimitedFrom(input);
    EnumThree buffer = builder.build();
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    buffer.writeDelimitedTo(output);
    readProtoThreeAndPrintDaEnum(output.toByteArray());
  }

}
