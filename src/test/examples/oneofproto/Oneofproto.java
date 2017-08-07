// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package oneofproto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import oneofproto.GeneratedOneOfProto.Oneof;

public class Oneofproto {

  private static final byte[] WITH_BOOL_FIELD = new byte[]{4, 8, 42, 24, 1};
  private static final byte[] WITH_FLOAT_FIELD = new byte[]{7, 8, 42, 21, 0, 0, -10, 66};
  private static final byte[] WITH_STRING_FIELD = new byte[]{9, 8, 42, 34, 5, 104, 101, 108, 108,
      111};
  private static final byte[] WITH_NO_FIELD = new byte[]{2, 8, 42};


  public static void main(String... args) throws IOException {
    roundTrip(WITH_BOOL_FIELD);
    roundTrip(WITH_FLOAT_FIELD);
    roundTrip(WITH_STRING_FIELD);
    roundTrip(WITH_NO_FIELD);
  }

  private static void roundTrip(byte[] data) throws IOException {
    ByteArrayInputStream input = new ByteArrayInputStream(data);
    Oneof.Builder builder = Oneof.newBuilder();
    builder.mergeDelimitedFrom(input);
    Oneof oneof = builder.build();
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    oneof.writeDelimitedTo(output);
    System.out.println(Arrays.toString(output.toByteArray()));
  }

}
