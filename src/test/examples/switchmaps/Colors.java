// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package switchmaps;

public enum Colors {
  RED("rar"), BLUE("blew"), GREEN("soylent"), GRAY("fifty");

  private String aField;

  Colors(String string) {
    aField = string;
  }

  @Override
  public String toString() {
    return aField;
  }
}
