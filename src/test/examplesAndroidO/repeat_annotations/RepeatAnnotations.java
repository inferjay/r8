// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package repeat_annotations;

// Simple test of Java 8 repeated annotations.
public class RepeatAnnotations {

  @NumberAnnotation(number = 1)
  @NumberAnnotation(number = 2)
  @NumberAnnotation(number = 3)
  class Inner {
  }

  public static void main(String[] args) {
    NumberAnnotations annotations = Inner.class.getAnnotation(NumberAnnotations.class);
    System.out.println(annotations.value().length);
    for (NumberAnnotation annotation : annotations.value()) {
      System.out.println("Number annotation value: " + annotation.number());
    }
  }
}
