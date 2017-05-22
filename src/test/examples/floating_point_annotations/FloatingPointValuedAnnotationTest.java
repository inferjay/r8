// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package floating_point_annotations;

public class FloatingPointValuedAnnotationTest {

  @FloatingPointValuedAnnotation(doubleValue = -0d)
  class A {
  }

  @FloatingPointValuedAnnotation(doubleValue = 0d)
  class B {
  }

  @FloatingPointValuedAnnotation(floatValue = -0f)
  class C {
  }

  @FloatingPointValuedAnnotation(floatValue = 0f)
  class D {
  }

  public static void main(String[] args) {
    System.out.println(A.class.getAnnotation(FloatingPointValuedAnnotation.class)
        .equals(B.class.getAnnotation(FloatingPointValuedAnnotation.class)));

    System.out.println(C.class.getAnnotation(FloatingPointValuedAnnotation.class)
        .equals(D.class.getAnnotation(FloatingPointValuedAnnotation.class)));
  }
}
