// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.dex;

import com.android.tools.r8.graph.DexApplication.Builder;
import com.android.tools.r8.graph.DexCallSite;
import com.android.tools.r8.graph.DexDebugEvent;
import com.android.tools.r8.graph.DexDebugInfo;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexMethodHandle;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexProto;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.ObjectToOffsetMapping;
import org.junit.Assert;
import org.junit.Test;

public class DebugByteCodeWriterTest {

  ObjectToOffsetMapping emptyObjectTObjectMapping() {
    return new ObjectToOffsetMapping(
        0,
        new Builder(new DexItemFactory(), null).build(),
        new DexProgramClass[] {},
        new DexProto[] {},
        new DexType[] {},
        new DexMethod[] {},
        new DexField[] {},
        new DexString[] {},
        new DexCallSite[] {},
        new DexMethodHandle[] {});
  }

  @Test
  public void testEmptyDebugInfo() {
    DexDebugInfo debugInfo = new DexDebugInfo(1, new DexString[]{}, new DexDebugEvent[]{});
    DebugBytecodeWriter writer = new DebugBytecodeWriter(debugInfo, emptyObjectTObjectMapping());
    Assert.assertEquals(3, writer.generate().length);
  }
}
