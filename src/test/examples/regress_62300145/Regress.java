// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package regress_62300145;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;

public class Regress {
  @Retention(RetentionPolicy.RUNTIME)
  public @interface A {
  }

  public class InnerClass {
    public InnerClass(@A String p) {}
  }

  public static void main(String[] args) throws NoSuchMethodException {
    Constructor<InnerClass> constructor =
        InnerClass.class.getDeclaredConstructor(Regress.class, String.class);
    int i = 0;
    for (Annotation[] annotations : constructor.getParameterAnnotations()) {
      System.out.print(i++ + ": ");
      for (Annotation annotation : annotations) {
        System.out.println(annotation);
      }
    }
  }
}
