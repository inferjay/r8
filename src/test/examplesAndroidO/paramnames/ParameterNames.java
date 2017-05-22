// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package paramnames;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class ParameterNames {

  private static final int MODIFIER_NONE = 0;
  private static final int MODIFIER_FINAL = 0X10;

  public ParameterNames(int a, final int b) {
  }

  public static void check(String expected, String checked) {
    if (!expected.equals(checked)) {
      throw new RuntimeException("Found '" + checked + "' while expecting '" + expected + "'");
    }
  }

  public static void check(int expected, int checked) {
    if (expected != checked) {
      throw new RuntimeException("Found '" + checked + "' while expecting '" + expected + "'");
    }
  }

  public static void myMethod(int a, final int b) throws NoSuchMethodException {
    Class<ParameterNames> clazz = ParameterNames.class;
    Method myMethod = clazz.getDeclaredMethod("myMethod", int.class, int.class);
    Parameter[] parameters = myMethod.getParameters();
    check(2, parameters.length);
    check("a", parameters[0].getName());
    check("b", parameters[1].getName());
    check(MODIFIER_NONE, parameters[0].getModifiers());
    check(MODIFIER_FINAL, parameters[1].getModifiers());
  }

  public static void myConstructor() throws NoSuchMethodException {
    Class<ParameterNames> clazz = ParameterNames.class;
    Constructor<?> myConstructor = clazz.getDeclaredConstructor(int.class, int.class);
    Parameter[] parameters = myConstructor.getParameters();
    check(2, parameters.length);
    check("a", parameters[0].getName());
    check("b", parameters[1].getName());
    check(MODIFIER_NONE, parameters[0].getModifiers());
    check(MODIFIER_FINAL, parameters[1].getModifiers());
  }

  public static void main(String[] args) throws NoSuchMethodException {
    myMethod(0, 1);
    myConstructor();
  }
}
