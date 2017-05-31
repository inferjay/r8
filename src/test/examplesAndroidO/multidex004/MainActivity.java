// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package multidex004;

import multidex004.fakeframeworks.Activity;

public class MainActivity extends Activity {

    protected void onCreate() {
    }

    public int getVersion(VersionInterface version) {
        return version.getVersion();
    }

    public int getVersion() {
      return getVersion(() -> VersionStatic.getVersion());
    }

}
