// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.graph.DexDebugEvent.StartLocal;
import com.android.tools.r8.ir.code.DebugPosition;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Builder for constructing a list of debug events suitable for DexDebugInfo.
 *
 * This builder is intended to be very pedantic and ensure a well-formed structure of the resulting
 * event stream.
 */
public class DexDebugEventBuilder {

  private static final int NO_PC_INFO = -1;
  private static final int NO_LINE_INFO = -1;

  private static class PositionState {
    int pc = NO_PC_INFO;
    int line = NO_LINE_INFO;
    DexString file = null;
    ImmutableMap<Integer, DebugLocalInfo> locals = null;
  }

  private final DexMethod method;

  private final DexItemFactory dexItemFactory;

  // Previous and current position info to delay emitting position changes.
  private final PositionState previous;
  private final PositionState current;

  // In order list of non-this argument locals.
  private int lastArgumentRegister = -1;
  private final List<DebugLocalInfo> arguments;

  // Mapping from register to local for currently open/visible locals.
  private final Map<Integer, DebugLocalInfo> openLocals = new HashMap<>();

  // Mapping from register to the last known local in that register (See DBG_RESTART_LOCAL).
  private final Map<Integer, DebugLocalInfo> lastKnownLocals = new HashMap<>();

  // Flushed events.
  private final List<DexDebugEvent> events = new ArrayList<>();

  private int startLine = NO_LINE_INFO;

  public DexDebugEventBuilder(DexMethod method, DexItemFactory dexItemFactory) {
    this.method = method;
    this.dexItemFactory = dexItemFactory;
    arguments = new ArrayList<>(method.proto.parameters.values.length);
    current = new PositionState();
    previous = new PositionState();
  }

  public void startArgument(int register, DebugLocalInfo local, boolean isThis) {
    // Verify that arguments are started in order.
    assert register > lastArgumentRegister;
    lastArgumentRegister = register;
    // If this is an actual argument record it for header information.
    if (!isThis) {
      arguments.add(local);
    }
    // If the argument does not have a parametrized type, implicitly open it.
    if (local != null && local.signature == null) {
      openLocals.put(register, local);
      lastKnownLocals.put(register, local);
    }
  }

  /** Emits a positions entry if the position has changed and associates any local changes. */
  public void setPosition(int pc, DebugPosition position) {
    setPosition(pc, position.line, position.file, position.getLocals());
  }

  public void setPosition(
      int pc, int line, DexString file, ImmutableMap<Integer, DebugLocalInfo> locals) {
    // If we have a pending position and the next differs from it flush the pending one.
    if (previous.pc != current.pc && positionChanged(current, pc, line, file)) {
      flushCurrentPosition();
    }
    current.pc = pc;
    current.line = line;
    current.file = file;
    current.locals = locals;
  }

  private void flushCurrentPosition() {
    // If this is the first emitted possition, initialize previous state: start-line is forced to be
    // the first actual line, in-effect, causing the first position to be a zero-delta line change.
    if (startLine == NO_LINE_INFO) {
      assert events.isEmpty();
      assert previous.pc == NO_PC_INFO;
      assert previous.line == NO_LINE_INFO;
      startLine = current.line;
      previous.line = current.line;
      previous.pc = 0;
    }
    // Emit position change (which might result in additional advancement events).
    emitAdvancementEvents();
    // Emit local changes for new current position (they relate to the already emitted position).
    // Locals are either defined on all positions or on none.
    assert current.locals != null || previous.locals == null;
    if (current.locals != null) {
      emitLocalChanges();
    }
  }

  /** Build the resulting DexDebugInfo object. */
  public DexDebugInfo build() {
    if (previous.pc != current.pc) {
      flushCurrentPosition();
    }
    if (startLine == NO_LINE_INFO) {
      return null;
    }
    DexString[] params = new DexString[method.proto.parameters.values.length];
    assert arguments.isEmpty() || params.length == arguments.size();
    for (int i = 0; i < arguments.size(); i++) {
      DebugLocalInfo local = arguments.get(i);
      params[i] = (local == null || local.signature != null) ? null : local.name;
    }
    return new DexDebugInfo(startLine, params, events.toArray(new DexDebugEvent[events.size()]));
  }

  private static boolean positionChanged(
      PositionState current, int nextPc, int nextLine, DexString nextFile) {
    return nextPc != current.pc && (nextLine != current.line || nextFile != current.file);
  }

  private void emitAdvancementEvents() {
    int pcDelta = current.pc - previous.pc;
    int lineDelta = current.line - previous.line;
    assert pcDelta >= 0;
    if (current.file != previous.file) {
      assert current.file == null || !current.file.equals(previous.file);
      events.add(dexItemFactory.createSetFile(current.file));
    }
    if (lineDelta < Constants.DBG_LINE_BASE
        || lineDelta - Constants.DBG_LINE_BASE >= Constants.DBG_LINE_RANGE) {
      events.add(dexItemFactory.createAdvanceLine(lineDelta));
      // TODO(herhut): To be super clever, encode only the part that is above limit.
      lineDelta = 0;
    }
    if (pcDelta >= Constants.DBG_ADDRESS_RANGE) {
      events.add(dexItemFactory.createAdvancePC(pcDelta));
      pcDelta = 0;
    }
    // TODO(herhut): Maybe only write this one if needed (would differ from DEX).
    int specialOpcode =
        0x0a + (lineDelta - Constants.DBG_LINE_BASE) + Constants.DBG_LINE_RANGE * pcDelta;
    assert specialOpcode >= 0x0a;
    assert specialOpcode <= 0xff;
    events.add(dexItemFactory.createDefault(specialOpcode));
    previous.pc = current.pc;
    previous.line = current.line;
    previous.file = current.file;
  }

  private void emitLocalChanges() {
    if (previous.locals == current.locals) {
      return;
    }
    SortedSet<Integer> currentRegisters = new TreeSet<>(openLocals.keySet());
    SortedSet<Integer> positionRegisters = new TreeSet<>(current.locals.keySet());
    for (Integer register : currentRegisters) {
      if (!positionRegisters.contains(register)) {
        events.add(dexItemFactory.createEndLocal(register));
        openLocals.remove(register);
      }
    }
    for (Integer register : positionRegisters) {
      DebugLocalInfo positionLocal = current.locals.get(register);
      DebugLocalInfo currentLocal = openLocals.get(register);
      if (currentLocal != positionLocal) {
        openLocals.put(register, positionLocal);
        if (currentLocal == null && lastKnownLocals.get(register) == positionLocal) {
          events.add(dexItemFactory.createRestartLocal(register));
        } else {
          events.add(new StartLocal(register, positionLocal));
          lastKnownLocals.put(register, positionLocal);
        }
      }
    }
    previous.locals = current.locals;
  }
}
