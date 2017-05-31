// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package multidex001;

/**
 * Class directly referenced from Activity, will be kept in main dex. The class is not referenced
 * by <clinit> or <init>, its direct references are not kept in main dex.
 */
public class ClassForMainDex {

    public static int getVersion() {
        return Version.getVersion();
    }

}
