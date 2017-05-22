// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package lambdadesugaring.other;

public class OtherRefs {
  public static class PublicInit {
    public PublicInit() {
    }

    @Override
    public String toString() {
      return "OtherRefs::PublicInit::init()";
    }
  }

  protected String fooOtherProtected() {
    return "OtherRefs::fooOtherProtected()";
  }

  public String fooOtherPublic() {
    return "OtherRefs::fooOtherPublic()";
  }

  protected static String staticOtherProtected() {
    return "OtherRefs::staticOtherProtected()";
  }

  public static String staticOtherPublic() {
    return "OtherRefs::staticOtherPublic()";
  }
}
