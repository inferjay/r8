// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.optimize;

import com.android.tools.r8.graph.DebugLocalInfo;
import com.android.tools.r8.graph.DexCode;
import com.android.tools.r8.graph.DexDebugEntry;
import com.android.tools.r8.graph.DexDebugEventBuilder;
import com.android.tools.r8.graph.DexDebugInfo;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.naming.ClassNameMapper;
import com.android.tools.r8.naming.ClassNaming;
import com.android.tools.r8.naming.MemberNaming;
import com.android.tools.r8.naming.MemberNaming.Range;
import com.android.tools.r8.naming.MemberNaming.Signature;
import com.android.tools.r8.utils.HashMapInt;
import com.android.tools.r8.utils.InternalOptions;
import com.google.common.collect.ImmutableMap;
import java.util.List;

public class DebugStripper {

  private static final int USED_MORE_THAN_ONCE = 0;
  private static final int USED_ONCE = -1;

  private final ClassNameMapper classNameMapper;
  private final InternalOptions options;

  public DebugStripper(ClassNameMapper classNameMapper, InternalOptions options) {
    this.classNameMapper = classNameMapper;
    this.options = options;
  }

  private String descriptorToName(String descriptor) {
    // The format is L<name>; and '/' is used as package separator.
    return descriptor.substring(1, descriptor.length() - 1).replace('/', '.');
  }

  private Range findRange(int value, List<Range> ranges, Range defaultRange) {
    for (Range range : ranges) {
      if (range.contains(value)) {
        return range;
      }
    }
    return defaultRange;
  }

  private static class NumberedDebugInfo {
    final int numberOfEntries;
    final DexDebugInfo info;

    public NumberedDebugInfo(int numberOfEntries, DexDebugInfo info) {
      this.numberOfEntries = numberOfEntries;
      this.info = info;
    }
  }

  private NumberedDebugInfo processDebugInfo(DexMethod method, DexDebugInfo info,
      MemberNaming naming, int startLine) {
    if (info == null || naming == null) {
      return new NumberedDebugInfo(0, null);
    }
    List<Range> ranges = naming.getInlineRanges();
    // Maintain line and address but only when entering or leaving a range of line numbers
    // that pertains to a different method body.
    Range currentRange = naming.topLevelRange;
    DexDebugEventBuilder builder = new DexDebugEventBuilder(method);
    // Always start with a no-op bytecode to make sure that the start-line is manifested by
    // the Dalvik VM and the event based processing in R8. This also avoids empty bytecode
    // sequences.
    int entryCount = 1;
    DexString file = null;
    ImmutableMap<Integer, DebugLocalInfo> locals = null;
    builder.setPosition(0, startLine, file, locals);
    for (DexDebugEntry entry : info.computeEntries()) {
      boolean addEntry = false;
      // We are in a range, check whether we have left it.
      if (currentRange != null && !currentRange.contains(entry.line)) {
        currentRange = null;
        addEntry = true;
      }
      // We have no range (because we left the old one or never were in a range).
      if (currentRange == null) {
        currentRange = findRange(entry.line, ranges, naming.topLevelRange);
        // We entered a new Range, emit this entry.
        if (currentRange != null) {
          addEntry = true;
        }
      }
      if (addEntry) {
        int line = options.skipDebugLineNumberOpt
            ? entry.line
            : startLine + ranges.indexOf(currentRange) + 1;
        builder.setPosition(entry.address, line, file, locals);
        ++entryCount;
      }
    }
    return new NumberedDebugInfo(entryCount, builder.build());
  }

  private void processCode(DexEncodedMethod encodedMethod, MemberNaming naming,
      HashMapInt<DexString> nameCounts) {
    if (encodedMethod.getCode() == null) {
      return;
    }
    DexCode code = encodedMethod.getCode().asDexCode();
    DexString name = encodedMethod.method.name;
    DexDebugInfo originalInfo = code.getDebugInfo();
    if (originalInfo == null) {
      return;
    }
    int startLine;
    boolean isUsedOnce = false;
    if (options.skipDebugLineNumberOpt) {
      startLine = originalInfo.startLine;
    } else {
      int nameCount = nameCounts.get(name);
      if (nameCount == USED_ONCE) {
        isUsedOnce = true;
        startLine = 0;
      } else {
        startLine = nameCount;
      }
    }

    NumberedDebugInfo numberedInfo = processDebugInfo(
        encodedMethod.method, originalInfo, naming, startLine);
    DexDebugInfo newInfo = numberedInfo.info;
    if (!options.skipDebugLineNumberOpt) {
      // Fix up the line information.
      int previousCount = nameCounts.get(name);
      nameCounts.put(name, previousCount + numberedInfo.numberOfEntries);
      // If we don't actually need line information and there are no debug entries, throw it away.
      if (newInfo != null && isUsedOnce && newInfo.events.length == 0) {
        newInfo = null;
      } else if (naming != null && newInfo != null) {
        naming.setCollapsedStartLineNumber(startLine);
        // Preserve the line number information we had.
        naming.setOriginalStartLineNumber(originalInfo.startLine);
      }
    }
    code.setDebugInfo(newInfo);
  }

  private void processMethod(DexEncodedMethod method, ClassNaming classNaming,
      HashMapInt<DexString> nameCounts) {
    MemberNaming naming = null;
    if (classNaming != null) {
      Signature renamedSignature = classNameMapper.getRenamedMethodSignature(method.method);
      naming = classNaming.lookup(renamedSignature);
    }
    processCode(method, naming, nameCounts);
  }

  private void processMethods(DexEncodedMethod[] methods, ClassNaming naming,
      HashMapInt<DexString> nameCounts) {
    if (methods == null) {
      return;
    }
    for (DexEncodedMethod method : methods) {
      processMethod(method, naming, nameCounts);
    }
  }

  public void processClass(DexProgramClass clazz) {
    if (!clazz.hasMethodsOrFields()) {
      return;
    }
    String name = descriptorToName(clazz.type.toDescriptorString());
    ClassNaming naming = classNameMapper == null ? null : classNameMapper.getClassNaming(name);
    HashMapInt<DexString> nameCounts = new HashMapInt<>();
    setIntialNameCounts(nameCounts, clazz.directMethods());
    setIntialNameCounts(nameCounts, clazz.virtualMethods());
    processMethods(clazz.directMethods(), naming, nameCounts);
    processMethods(clazz.virtualMethods(), naming, nameCounts);
  }

  private void setIntialNameCounts(HashMapInt<DexString> nameCounts,
      DexEncodedMethod[] methods) {
    for (DexEncodedMethod method : methods) {
      if (nameCounts.containsKey(method.method.name)) {
        nameCounts.put(method.method.name, USED_MORE_THAN_ONCE);
      } else {
        nameCounts.put(method.method.name, USED_ONCE);
      }
    }
  }
}
