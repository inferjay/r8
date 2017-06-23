// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.ir.code.MoveType;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder to construct a "per position" representation of the debug information.
 *
 * This builder is relatively relaxed about the stream of build operations and should accept
 * any stream from any input file we expect to process correctly.
 */
public class DexDebugEntryBuilder {

  private static class LocalEntry {
    DebugLocalInfo current;
    DebugLocalInfo last;

    void set(DebugLocalInfo value) {
      current = value;
      last = value;
    }

    void unset() {
      current = null;
    }

    void reset() {
      current = last;
    }
  }

  // The variables of the state machine.
  private int currentPc = 0;
  private int currentLine;
  private DexString currentFile = null;
  private boolean prologueEnd = false;
  private boolean epilogueBegin = false;
  private final Map<Integer, LocalEntry> locals = new HashMap<>();

  // Delayed construction of an entry. Is finalized once locals information has been collected.
  private DexDebugEntry pending = null;

  // Canonicalization of locals (the IR/Dex builders assume identity of locals).
  private final Map<DebugLocalInfo, DebugLocalInfo> canonicalizedLocals = new HashMap<>();

  // Resulting debug entries.
  private List<DexDebugEntry> entries = new ArrayList<>();

  public DexDebugEntryBuilder(int startLine) {
    currentLine = startLine;
  }

  public DexDebugEntryBuilder(DexEncodedMethod method, DexItemFactory factory) {
    DexCode code = method.getCode().asDexCode();
    DexDebugInfo info = code.getDebugInfo();
    int argumentRegister = code.registerSize - code.incomingRegisterSize;
    if (!method.accessFlags.isStatic()) {
      DexString name = factory.thisName;
      DexType type = method.method.getHolder();
      startLocal(argumentRegister, name, type, null);
      argumentRegister += MoveType.fromDexType(type).requiredRegisters();
    }
    DexType[] types = method.method.proto.parameters.values;
    DexString[] names = info.parameters;
    for (int i = 0; i < types.length; i++) {
      // If null, the parameter has a parameterized type and the local is introduced in the stream.
      if (names[i] != null) {
        startLocal(argumentRegister, names[i], types[i], null);
        argumentRegister += MoveType.fromDexType(types[i]).requiredRegisters();
      }
    }
    currentLine = info.startLine;
    for (DexDebugEvent event : info.events) {
      event.addToBuilder(this);
    }
  }

  public void setFile(DexString file) {
    currentFile = file;
  }

  public void advancePC(int pcDelta) {
    assert pcDelta >= 0;
    currentPc += pcDelta;
  }

  public void advanceLine(int line) {
    currentLine += line;
  }

  public void endPrologue() {
    prologueEnd = true;
  }

  public void beginEpilogue() {
    epilogueBegin = true;
  }

  public void startLocal(int register, DexString name, DexType type, DexString signature) {
    getEntry(register).set(canonicalize(name, type, signature));
  }

  public void endLocal(int register) {
    getEntry(register).unset();
  }

  public void restartLocal(int register) {
    getEntry(register).reset();
  }

  public void setPosition(int pcDelta, int lineDelta) {
    assert pcDelta >= 0;
    if (pending != null) {
      // Local changes contribute to the pending position entry.
      entries.add(new DexDebugEntry(
          pending.address, pending.line, pending.sourceFile,
          pending.prologueEnd, pending.epilogueBegin,
          getLocals()));
    }
    currentPc += pcDelta;
    currentLine += lineDelta;
    pending = new DexDebugEntry(
        currentPc, currentLine, currentFile, prologueEnd, epilogueBegin, null);
    prologueEnd = false;
    epilogueBegin = false;
  }

  public List<DexDebugEntry> build() {
    // Flush any pending entry.
    if (pending != null) {
      setPosition(0, 0);
      pending = null;
    }
    List<DexDebugEntry> result = entries;
    entries = null;
    return result;
  }

  private DebugLocalInfo canonicalize(DexString name, DexType type, DexString signature) {
    DebugLocalInfo local = new DebugLocalInfo(name, type, signature);
    DebugLocalInfo canonical = canonicalizedLocals.putIfAbsent(local, local);
    return canonical != null ? canonical : local;
  }

  private LocalEntry getEntry(int register) {
    LocalEntry entry = locals.get(register);
    if (entry == null) {
      entry = new LocalEntry();
      locals.put(register, entry);
    }
    return entry;
  }

  private ImmutableMap<Integer, DebugLocalInfo> getLocals() {
    ImmutableMap.Builder<Integer, DebugLocalInfo> builder = ImmutableMap.builder();
    for (Integer register : locals.keySet()) {
      LocalEntry entry = locals.get(register);
      if (entry.current != null) {
        builder.put(register, entry.current);
      }
    }
    return builder.build();
  }
}
