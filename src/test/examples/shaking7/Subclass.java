// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package shaking7;

public class Subclass extends Superclass {

  public int theIntField = 4;

  public double theDoubleField = 2.0;

  public String toString() {
    return "Subclass";
  }
}
