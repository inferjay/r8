// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package shaking12;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Shaking {

  static private Named createInstance(Class<? extends Named> aClass) {
    try {
      return aClass.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      e.printStackTrace();
    }
    return new Named() {
      @Override
      public String getName() {
        return "Unknown";
      }
    };
  }

  public static void main(String[] args) {
    List<Class<? extends Named>> classes = new ArrayList<>(3);
    classes.add(MetaphorClass.class);
    classes.add(PeopleClass.class);
    classes.add(ThingClass.class);
    Iterator<Class<? extends Named>> iterator = classes.iterator();
    iterator.next();
    while (iterator.hasNext()) {
      Named item = createInstance(iterator.next());
      if (item instanceof AnimalClass) {
        System.out.println("An animal!");
      }
      System.out.println(createInstance(iterator.next()).getName());
    }
  }
}
