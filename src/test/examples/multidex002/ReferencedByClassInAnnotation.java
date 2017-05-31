// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package multidex002;

public enum ReferencedByClassInAnnotation {

    A {
        private ReferencedByEnum a = new ReferencedByEnum();
        @Override
        public int get() {
            return a.hashCode();
        }
    },
    B {
        private ReferencedByEnum b = new ReferencedByEnum();
        @Override
        public int get() {
            return b.hashCode();
        }
    };


    public abstract int get();
}
