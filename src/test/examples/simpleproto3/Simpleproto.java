// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package simpleproto3;

import com.google.protobuf.UninitializedMessageException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import simpleproto3.GeneratedSimpleProto.Simple;

public class Simpleproto {

  private static final byte[] WITH_REQUIRED_FIELDS = new byte[]{7, 8, 42, 21, 0, 0, -10, 66};
  private static final byte[] WITH_MISSING_FIELD = new byte[]{2, 8, 42};


  public static void main(String... args) throws IOException {
    readProtoWithAllReqFields();
    partialBuildFails();
    partialReadFails();
  }

  private static void partialBuildFails() {
    Simple.Builder builder = Simple.newBuilder();
    builder.setId(32);
    try {
      builder.build();
    } catch (UninitializedMessageException e) {
      System.out.println("got exception");
    }
  }

  private static void partialReadFails() throws IOException {
    ByteArrayInputStream input = new ByteArrayInputStream(WITH_MISSING_FIELD);
    Simple.Builder builder = Simple.newBuilder();
    builder.mergeDelimitedFrom(input);
    try {
      builder.build();
    } catch (UninitializedMessageException e) {
      System.out.println("got exception");
    }
  }

  private static void readProtoWithAllReqFields() throws IOException {
    ByteArrayInputStream input = new ByteArrayInputStream(WITH_REQUIRED_FIELDS);
    Simple.Builder builder = Simple.newBuilder();
    builder.mergeDelimitedFrom(input);
    Simple simple = builder.build();
    ByteArrayOutputStream output = new ByteArrayOutputStream(WITH_REQUIRED_FIELDS.length);
    simple.writeDelimitedTo(output);
    System.out.println(isContained(WITH_REQUIRED_FIELDS, output.toByteArray()));
  }

  // After shaking, the serialized proto will no longer contain fields that are not referenced.
  private static boolean isContained(byte[] fullBytes, byte[] reducedBytes) {
    int j = 1;
    for (int i = 1; i < fullBytes.length && j < reducedBytes.length; i++) {
      if (fullBytes[i] == reducedBytes[j]) {
        j++;
      }
    }
    return j == reducedBytes.length;
  }
}
