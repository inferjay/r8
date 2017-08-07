// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package nestedproto2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import nestedproto2.GeneratedNestedProto.Outer;

public class Nestedproto {

  private static final byte[] NESTED_MESSAGE_WITH_BOTH = new byte[]{25, 8, 42, 18, 12, 8, 1, 18, 8,
      105, 110, 110, 101, 114, 79, 110, 101, 26, 7, 8, 2, 21, 0, 0, -10, 66};

  private static final byte[] NESTED_MESSAGE_WITH_ONE = new byte[]{16, 8, 42, 18, 12, 8, 1, 18, 8,
      105,
      110, 110, 101, 114, 79, 110, 101};

  // Test that all fields remain when roundtripping with removed fields.
  public static void main(String... args) throws IOException {
    testWith(NESTED_MESSAGE_WITH_BOTH);
    testWith(NESTED_MESSAGE_WITH_ONE);
  }

  private static void testWith(byte[] data) throws IOException {
    ByteArrayInputStream input = new ByteArrayInputStream(data);
    Outer.Builder builder = Outer.newBuilder();
    builder.mergeDelimitedFrom(input);
    builder.setId(1982);
    Outer outer = builder.build();
    outer.writeTo(System.out);
  }
}
