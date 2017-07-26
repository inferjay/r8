// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package minifygeneric;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class Minifygeneric {

  public static void main(String[] args) throws NoSuchMethodException, SecurityException, NoSuchFieldException {
    for (TypeVariable<Class<Generic>> var : Generic.class.getTypeParameters()) {
      System.out.println(var.getName());
      Type bound = var.getBounds()[0];
      System.out.println(((Class<?>) bound).getName().equals(AA.class.getName()));
    }

    Field f = Generic.class.getField("f");
    ParameterizedType fieldType = (java.lang.reflect.ParameterizedType)f.getGenericType();
    checkOneParameterType(fieldType, Generic.class, AA.class);

    ParameterizedType methodReturnType =
        (ParameterizedType) Generic.class.getMethod("get").getGenericReturnType();
    checkOneParameterType(methodReturnType, Generic.class, AA.class);
  }

  private static void checkOneParameterType(ParameterizedType toCheck, Class<?> rawType,
      Class<?>... bounds) {
    System.out.println(((Class<?>) toCheck.getRawType()).getName()
        .equals(rawType.getName()));
    Type[] parameters = toCheck.getActualTypeArguments();
    System.out.println(parameters.length);
    TypeVariable<?> parameter = (TypeVariable<?>) parameters[0];
    System.out.println(parameter.getName());
    Type[] actualBounds = parameter.getBounds();
    for (int i = 0; i < bounds.length; i++) {
      System.out.println(((Class<?>) actualBounds[i]).getName().equals(bounds[i].getName()));
    }
  }
}
