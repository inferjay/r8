// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

// This code is not run directly. It needs to be compiled to dex code.
// 'throwing.dex' is what is run.

package throwing;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This class' logic is completely bogus. The only purpose is to be recursive to avoid inlining
 * and terminate.
 */
public class RenamedClass {
  public List list = new ArrayList();

  public List getList() {
    if (list == null) { // always false
      setList(getList());
    }
    return list;
  }

  public void setList(List list) {
    if (list == null) {
      setList(new LinkedList());
    } else {
      this.list = list;
    }
  }

  // Another method with the same signature as getList
  public void swap(List list) {
    List before = getList();
    setList(list);
    if (before == null) { // always false
      swap(list);
    }
  }

  static RenamedClass create() {
    RenamedClass theClass = new RenamedClass();
    theClass.setList(new LinkedList());
    return theClass;
  }

  void takeThingsForASpin(int value) {
    if (value == 42) {
      swap(new LinkedList<>());
      setList(getList());
    } else {
      takeThingsForASpin(42);
    }
  }
}
