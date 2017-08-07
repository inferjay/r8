// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package simpleproto2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import simpleproto2.GeneratedSimpleProto.Simple;

/**
 * A class that only uses a has method but otherwise ignores the value of a field.
 */
public class Simpleproto {

  private static final byte[] WITHOUT_HASME_FIELD = new byte[]{2, 8, 42};
  private static final byte[] WITH_HASME_FIELD = new byte[]{7, 8, 42, 21, 0, 0, -10, 66};

  public static void main(String... args) throws IOException {
    testHasWorks(WITHOUT_HASME_FIELD, false);
    testHasWorks(WITH_HASME_FIELD, true);
  }

  private static void testHasWorks(byte[] msg, boolean expected) throws IOException {
    ByteArrayInputStream input = new ByteArrayInputStream(msg);
    Simple.Builder builder = Simple.newBuilder();
    builder.mergeDelimitedFrom(input);
    Simple simple = builder.build();
    System.out.println("Expected " + expected + " and got " + simple.hasHasMe());
  }
}
