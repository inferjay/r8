// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package multidex003;


public class TestAnnotationResolution {

    public void testAnnotation() throws Exception {
        Class<?> clazz = Class.forName("com.google.annotationresolution.test." + "Annotated");
        clazz.getAnnotations();
        clazz = Class.forName("com.google.annotationresolution.test." + "Annotated2");
        clazz.getAnnotations();
        clazz = Class.forName("com.google.annotationresolution.test." + "Annotated3");
        clazz.getAnnotations();
   }

}
