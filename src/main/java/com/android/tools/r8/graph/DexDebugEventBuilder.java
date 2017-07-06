// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.graph.DexDebugEvent.StartLocal;
import com.android.tools.r8.ir.code.Argument;
import com.android.tools.r8.ir.code.DebugLocalsChange;
import com.android.tools.r8.ir.code.DebugPosition;
import com.android.tools.r8.ir.code.Instruction;
import it.unimi.dsi.fastutil.ints.Int2ReferenceAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMaps;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceSortedMap;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder for constructing a list of debug events suitable for DexDebugInfo.
 *
 * This builder is intended to be very pedantic and ensure a well-formed structure of the resulting
 * event stream.
 */
public class DexDebugEventBuilder {

  private static final int NO_PC_INFO = -1;
  private static final int NO_LINE_INFO = -1;

  private final DexEncodedMethod method;
  private final DexItemFactory factory;

  // In order list of non-this argument locals.
  private ArrayList<DebugLocalInfo> arguments;

  // Mapping from register to the last known local in that register (See DBG_RESTART_LOCAL).
  private Int2ReferenceMap<DebugLocalInfo> lastKnownLocals;

  // Mapping from register to local for currently open/visible locals.
  private Int2ReferenceMap<DebugLocalInfo> pendingLocals = null;

  // Conservative pending-state of locals to avoid some equality checks on locals.
  // pendingLocalChanges == true ==> localsEqual(emittedLocals, pendingLocals).
  private boolean pendingLocalChanges = false;

  // State of pc, line, file and locals in the emitted event stream.
  private int emittedPc = NO_PC_INFO;
  private int emittedLine = NO_LINE_INFO;
  private DexString emittedFile = null;
  private Int2ReferenceMap<DebugLocalInfo> emittedLocals;

  // If lastMoveInstructionPc != NO_PC_INFO, then the last pc-advancing instruction was a
  // move-exception at lastMoveInstructionPc. This is needed to maintain the art/dx specific
  // behaviour that the move-exception pc is associated with the catch-declaration line.
  // See debug.ExceptionTest.testStepOnCatch().
  private int lastMoveInstructionPc = NO_PC_INFO;

  // Emitted events.
  private final List<DexDebugEvent> events = new ArrayList<>();

  // Initial known line for the method.
  private int startLine = NO_LINE_INFO;

  public DexDebugEventBuilder(DexEncodedMethod method, DexItemFactory factory) {
    this.method = method;
    this.factory = factory;
  }

  // Public method for the DebugStripper.
  public void setPosition(int pc, int line) {
    emitDebugPosition(pc, line, null);
  }

  /** Add events at pc for instruction. */
  public void add(int pc, Instruction instruction) {
    // Initialize locals state on block entry.
    if (instruction.getBlock().entry() == instruction) {
      updateBlockEntry(instruction);
    }
    assert pendingLocals != null;

    // If this is a position emit and exit as it always emits events.
    if (instruction.isDebugPosition()) {
      emitDebugPosition(pc, instruction.asDebugPosition());
      return;
    }

    if (instruction.isArgument()) {
      startArgument(instruction.asArgument());
    } else if (instruction.isDebugLocalsChange()) {
      updateLocals(instruction.asDebugLocalsChange());
    } else if (instruction.getBlock().exit() == instruction) {
      // If this is the end of the block clear out the pending state and exit.
      pendingLocals = null;
      pendingLocalChanges = false;
      return;
    } else if (instruction.isMoveException()) {
      lastMoveInstructionPc = pc;
    } else {
      // For non-exit / pc-advancing instructions emit any pending changes.
      emitLocalChanges(pc);
    }
  }

  /** Build the resulting DexDebugInfo object. */
  public DexDebugInfo build() {
    assert pendingLocals == null;
    assert !pendingLocalChanges;
    if (startLine == NO_LINE_INFO) {
      return null;
    }
    DexString[] params = new DexString[method.method.proto.parameters.values.length];
    if (arguments != null) {
      assert params.length == arguments.size();
      for (int i = 0; i < arguments.size(); i++) {
        DebugLocalInfo local = arguments.get(i);
        params[i] = (local == null || local.signature != null) ? null : local.name;
      }
    }
    return new DexDebugInfo(startLine, params, events.toArray(new DexDebugEvent[events.size()]));
  }

  private void updateBlockEntry(Instruction instruction) {
    assert pendingLocals == null;
    assert !pendingLocalChanges;
    Int2ReferenceMap<DebugLocalInfo> locals = instruction.getBlock().getLocalsAtEntry();
    if (locals == null) {
      pendingLocals = Int2ReferenceMaps.emptyMap();
    } else {
      pendingLocals = new Int2ReferenceOpenHashMap<>(locals);
      pendingLocalChanges = true;
    }
    if (emittedLocals == null) {
      initialize(locals);
    }
  }

  private void initialize(Int2ReferenceMap<DebugLocalInfo> locals) {
    assert arguments == null;
    assert emittedLocals == null;
    assert lastKnownLocals == null;
    assert startLine == NO_LINE_INFO;
    if (locals == null) {
      emittedLocals = Int2ReferenceMaps.emptyMap();
      lastKnownLocals = Int2ReferenceMaps.emptyMap();
      return;
    }
    // Implicitly open all unparameterized arguments.
    emittedLocals = new Int2ReferenceOpenHashMap<>();
    for (Entry<DebugLocalInfo> entry : locals.int2ReferenceEntrySet()) {
      if (entry.getValue().signature == null) {
        emittedLocals.put(entry.getIntKey(), entry.getValue());
      }
    }
    lastKnownLocals = new Int2ReferenceOpenHashMap<>(emittedLocals);
  }

  private void startArgument(Argument argument) {
    if (arguments == null) {
      arguments = new ArrayList<>(method.method.proto.parameters.values.length);
    }
    if (!argument.outValue().isThis()) {
      arguments.add(argument.getLocalInfo());
    }
  }

  private void updateLocals(DebugLocalsChange change) {
    pendingLocalChanges = true;
    for (Entry<DebugLocalInfo> end : change.getEnding().int2ReferenceEntrySet()) {
      assert pendingLocals.get(end.getIntKey()) == end.getValue();
      pendingLocals.remove(end.getIntKey());
    }
    for (Entry<DebugLocalInfo> start : change.getStarting().int2ReferenceEntrySet()) {
      assert !pendingLocals.containsKey(start.getIntKey());
      pendingLocals.put(start.getIntKey(), start.getValue());
    }
  }

  private boolean localsChanged() {
    if (!pendingLocalChanges) {
      return false;
    }
    pendingLocalChanges = !localsEqual(emittedLocals, pendingLocals);
    return pendingLocalChanges;
  }

  private void emitDebugPosition(int pc, DebugPosition position) {
    emitDebugPosition(pc, position.line, position.file);
  }

  private void emitDebugPosition(int pc, int line, DexString file) {
    int emitPc = lastMoveInstructionPc != NO_PC_INFO ? lastMoveInstructionPc : pc;
    lastMoveInstructionPc = NO_PC_INFO;
    // The position requires a pc change event and possible events for line, file and local changes.
    // Verify that we do not ever produce two subsequent positions at the same pc.
    assert emittedPc != emitPc;
    if (startLine == NO_LINE_INFO) {
      assert emittedLine == NO_LINE_INFO;
      startLine = line;
      emittedLine = line;
    }
    emitAdvancementEvents(emittedPc, emittedLine, emittedFile, emitPc, line, file, events, factory);
    emittedPc = emitPc;
    emittedLine = line;
    emittedFile = file;
    if (localsChanged()) {
      emitLocalChangeEvents(emittedLocals, pendingLocals, lastKnownLocals, events, factory);
      assert localsEqual(emittedLocals, pendingLocals);
    }
    pendingLocalChanges = false;
  }

  private void emitLocalChanges(int pc) {
    // If pc advanced since the locals changed and locals indeed have changed, emit the changes.
    if (localsChanged()) {
      int emitPc = lastMoveInstructionPc != NO_PC_INFO ? lastMoveInstructionPc : pc;
      lastMoveInstructionPc = NO_PC_INFO;
      emitAdvancementEvents(
          emittedPc, emittedLine, emittedFile, emitPc, emittedLine, emittedFile, events, factory);
      emittedPc = emitPc;
      emitLocalChangeEvents(emittedLocals, pendingLocals, lastKnownLocals, events, factory);
      pendingLocalChanges = false;
      assert localsEqual(emittedLocals, pendingLocals);
    }
  }

  private static void emitAdvancementEvents(
      int previousPc,
      int previousLine,
      DexString previousFile,
      int nextPc,
      int nextLine,
      DexString nextFile,
      List<DexDebugEvent> events,
      DexItemFactory factory) {
    int pcDelta = previousPc == NO_PC_INFO ? nextPc : nextPc - previousPc;
    int lineDelta = nextLine == NO_LINE_INFO ? 0 : nextLine - previousLine;
    assert pcDelta >= 0;
    if (nextFile != previousFile) {
      events.add(factory.createSetFile(nextFile));
    }
    if (lineDelta < Constants.DBG_LINE_BASE
        || lineDelta - Constants.DBG_LINE_BASE >= Constants.DBG_LINE_RANGE) {
      events.add(factory.createAdvanceLine(lineDelta));
      // TODO(herhut): To be super clever, encode only the part that is above limit.
      lineDelta = 0;
    }
    if (pcDelta >= Constants.DBG_ADDRESS_RANGE) {
      events.add(factory.createAdvancePC(pcDelta));
      pcDelta = 0;
    }
    // TODO(herhut): Maybe only write this one if needed (would differ from DEX).
    int specialOpcode =
        0x0a + (lineDelta - Constants.DBG_LINE_BASE) + Constants.DBG_LINE_RANGE * pcDelta;
    assert specialOpcode >= 0x0a;
    assert specialOpcode <= 0xff;
    events.add(factory.createDefault(specialOpcode));
  }

  public static void emitLocalChangeEvents(
      Int2ReferenceMap<DebugLocalInfo> previousLocals,
      Int2ReferenceMap<DebugLocalInfo> nextLocals,
      Int2ReferenceMap<DebugLocalInfo> lastKnownLocals,
      List<DexDebugEvent> events,
      DexItemFactory factory) {
    Int2ReferenceSortedMap<DebugLocalInfo> ending = new Int2ReferenceAVLTreeMap<>();
    Int2ReferenceSortedMap<DebugLocalInfo> starting = new Int2ReferenceAVLTreeMap<>();
    for (Entry<DebugLocalInfo> entry : previousLocals.int2ReferenceEntrySet()) {
      int register = entry.getIntKey();
      DebugLocalInfo local = entry.getValue();
      if (nextLocals.get(register) != local) {
        ending.put(register, local);
      }
    }
    for (Entry<DebugLocalInfo> entry : nextLocals.int2ReferenceEntrySet()) {
      int register = entry.getIntKey();
      DebugLocalInfo local = entry.getValue();
      if (previousLocals.get(register) != local) {
        starting.put(register, local);
      }
    }
    assert !ending.isEmpty() || !starting.isEmpty();
    for (Entry<DebugLocalInfo> end : ending.int2ReferenceEntrySet()) {
      int register = end.getIntKey();
      if (!starting.containsKey(register)) {
        previousLocals.remove(register);
        events.add(factory.createEndLocal(register));
      }
    }
    for (Entry<DebugLocalInfo> start : starting.int2ReferenceEntrySet()) {
      int register = start.getIntKey();
      DebugLocalInfo local = start.getValue();
      previousLocals.put(register, local);
      if (lastKnownLocals.get(register) == local) {
        events.add(factory.createRestartLocal(register));
      } else {
        events.add(new StartLocal(register, local));
        lastKnownLocals.put(register, local);
      }
    }
  }

  private static boolean localsEqual(
      Int2ReferenceMap<DebugLocalInfo> locals1, Int2ReferenceMap<DebugLocalInfo> locals2) {
    if (locals1 == locals2) {
      return true;
    }
    if (locals1.size() != locals2.size()) {
      return false;
    }
    for (Int2ReferenceMap.Entry<DebugLocalInfo> entry : locals1.int2ReferenceEntrySet()) {
      if (locals2.get(entry.getIntKey()) != entry.getValue()) {
        return false;
      }
    }
    return true;
  }
}
