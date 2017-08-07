// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package repeatedproto;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import repeatedproto.GeneratedRepeatedProto.Repeated;

public class Repeatedproto {

  private static final byte[] WITH_ALL_FIELDS = new byte[]{29, 8, 123, 18, 3, 111, 110, 101, 18, 3,
      116, 119, 111, 18, 5, 116, 104, 114, 101, 101, 24, 1, 34, 2, 8, 42, 34, 2, 8, 42};

  public static void main(String... args) throws IOException {
    ByteArrayInputStream input = new ByteArrayInputStream(WITH_ALL_FIELDS);
    Repeated.Builder builder = Repeated.newBuilder();
    builder.mergeDelimitedFrom(input);
    Repeated repeated = builder.build();
    System.out.println(repeated.getRepeatedList());
  }
}
