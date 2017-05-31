// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package multidex002;

import java.lang.annotation.Annotation;
import multidex002.fakelibrary.MultiDexApplication;

@AnnotationWithEnum(ReferencedByAnnotation.B)
public class TestApplication extends MultiDexApplication {

    public static Annotation annotation = getAnnotationWithEnum();
    public static Annotation annotation2 = getSoleAnnotation(Annotated.class);
    public static Annotation annotation3 = getSoleAnnotation(Annotated2.class);
    public static Class<?> interfaceClass = InterfaceWithEnum.class;

    public static Annotation getAnnotationWithEnum() {
        return getSoleAnnotation(TestApplication.class);
    }

    public static Annotation getSoleAnnotation(Class<?> annotated) {
        Annotation[] annot = annotated.getAnnotations();
        if (annot.length == 1) {
            return annot[0];
        }

        throw new AssertionError();
    }

}
