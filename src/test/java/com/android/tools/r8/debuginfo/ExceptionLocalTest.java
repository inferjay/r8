// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.debuginfo;

public class ExceptionLocalTest {

  public void foo(int x) {
    Integer obj = new Integer(x + x);
    long l = obj.longValue();
    try {
      l = obj.longValue();
      x = (int) l / x;
      invokerange(l, l, l, l, l, l);
      sout(x);
    } catch (ArithmeticException e) {
      sout(l);
    } catch (RuntimeException e) {
      sout(l); // We should not attempt to read the previous definition of 'e' here or below.
    } catch (Throwable e) {
      sout(l);
    }
  }

  private void sout(long l) {
    System.out.print(l);
  }

  private void invokerange(long a, long b, long c, long d, long e, long f) {
    if (a != d) {
      throw new RuntimeException("unexpected");
    }
  }

  public static void main(String[] args) {
    new ExceptionLocalTest().foo(21);
  }
}
