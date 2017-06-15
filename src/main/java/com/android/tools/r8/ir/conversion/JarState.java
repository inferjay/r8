// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.conversion;

import static com.android.tools.r8.ir.conversion.JarSourceCode.getArrayElementType;

import com.android.tools.r8.graph.DebugLocalInfo;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;

/**
 * Abstraction of the java bytecode state at a given control-flow point.
 *
 * The abstract state is defined by the abstract contents of locals and contents of the stack.
 */
public class JarState {

  // Type representatives of "any object/array" using invalid names (only valid for asserting).
  public static final Type REFERENCE_TYPE = Type.getObjectType("<any reference>");
  public static final Type OBJECT_TYPE = Type.getObjectType("<any object>");
  public static final Type ARRAY_TYPE = Type.getObjectType("[<any array>");

  // Type representative for the null value (non-existent but works for tracking the types here).
  public static final Type NULL_TYPE = Type.getObjectType("<null>");

  // Type representative for an address type (used by JSR/RET).
  public static final Type ADDR_TYPE = Type.getObjectType("<address>");

  // Typed mapping from a local slot or stack slot to a virtual register.
  public static class Slot {
    public final int register;
    public final Type type;

    @Override
    public String toString() {
      return "r" + register + ":" + type;
    }

    public Slot(int register, Type type) {
      assert type != REFERENCE_TYPE;
      assert type != OBJECT_TYPE;
      assert type != ARRAY_TYPE;
      this.register = register;
      this.type = type;
    }

    public boolean isCompatibleWith(Type other) {
      return isCompatible(type, other);
    }

    public boolean isCategory1() {
      return isCategory1(type);
    }

    public static boolean isCategory1(Type type) {
      return type != Type.LONG_TYPE && type != Type.DOUBLE_TYPE;
    }

    public static boolean isCompatible(Type type, Type other) {
      assert type != REFERENCE_TYPE;
      assert type != OBJECT_TYPE;
      assert type != ARRAY_TYPE;
      int sort = type.getSort();
      int otherSort = other.getSort();
      if (isReferenceCompatible(type, other)) {
        return true;
      }
      // Integers are assumed compatible with any other 32-bit integral.
      if (isIntCompatible(sort)) {
        return isIntCompatible(otherSort);
      }
      if (isIntCompatible(otherSort)) {
        return isIntCompatible(sort);
      }
      // In all other cases we require the two types to represent the same concrete type.
      return type.equals(other);
    }

    private static boolean isIntCompatible(int sort) {
      return Type.BOOLEAN <= sort && sort <= Type.INT;
    }

    private static boolean isReferenceCompatible(Type type, Type other) {
      int sort = type.getSort();
      int otherSort = other.getSort();

      // Catch all matching.
      if (other == REFERENCE_TYPE) {
        return sort == Type.OBJECT || sort == Type.ARRAY;
      }
      if (other == OBJECT_TYPE) {
        return sort == Type.OBJECT;
      }
      if (other == ARRAY_TYPE) {
        return type == NULL_TYPE || sort == Type.ARRAY;
      }

      return (sort == Type.OBJECT && otherSort == Type.ARRAY)
          || (sort == Type.ARRAY && otherSort == Type.OBJECT)
          || (sort == Type.OBJECT && otherSort == Type.OBJECT)
          || (sort == Type.ARRAY && otherSort == Type.ARRAY
              && isReferenceCompatible(getArrayElementType(type), getArrayElementType(other)));
    }
  }

  public static class Local {
    final Slot slot;
    final DebugLocalInfo info;

    public Local(Slot slot, DebugLocalInfo info) {
      this.slot = slot;
      this.info = info;
    }
  }

  // Immutable recording of the state (locals and stack should not be mutated).
  private static class Snapshot {
    public final Local[] locals;
    public final ImmutableList<Slot> stack;

    public Snapshot(Local[] locals, ImmutableList<Slot> stack) {
      this.locals = locals;
      this.stack = stack;
    }

    @Override
    public String toString() {
      return "locals: " + localsToString(Arrays.asList(locals))
          + ", stack: " + stackToString(stack);
    }
  }

  private final int startOfStack;
  private int topOfStack;

  // Locals are split into three parts based on types:
  //  1) reference-type locals have registers in range: [0; localsSize[
  //  2) single-width locals have registers in range: [localsSize; 2*localsSize[
  //  3) wide-width locals have registers in range: [2*localsSize; 3*localsSize[
  // This ensures that we can insert debugging-ranges into the SSA graph (via DebugLocal{Start,End})
  // without conflating locals that are shared among different types. This issue arises because a
  // debugging range can be larger than the definite-assignment scope of a local (eg, a local
  // introduced in an unscoped switch case). To ensure that the SSA graph is valid we must introduce
  // the local before inserting any DebugLocalReads (we do so in the method prelude, but that can
  // potentially lead to phi functions merging locals of different move-types. Thus we allocate
  // registers from the three distinct spaces.
  private final int localsSize;
  private final Local[] locals;

  // Mapping from local-variable nodes to their canonical local info.
  private final Map<LocalVariableNode, DebugLocalInfo> localVariables;

  // Scope-points of all local variables for inserting debug scoping instructions.
  private final Multimap<LabelNode, LocalVariableNode> localVariableStartPoints;
  private final Multimap<LabelNode, LocalVariableNode> localVariableEndPoints;


  private final Deque<Slot> stack = new ArrayDeque<>();

  private final Map<Integer, Snapshot> targetStates = new HashMap<>();


  public JarState(int maxLocals, Map<LocalVariableNode, DebugLocalInfo> localVariables) {
    int localsRegistersSize = maxLocals * 3;
    localsSize = maxLocals;
    locals = new Local[localsRegistersSize];
    startOfStack = localsRegistersSize;
    topOfStack = startOfStack;
    this.localVariables = localVariables;
    localVariableStartPoints = HashMultimap.create();
    localVariableEndPoints = HashMultimap.create();
    populateLocalTables();
  }

  private void populateLocalTables() {
    for (LocalVariableNode node : localVariables.keySet()) {
      if (node.start != node.end) {
        localVariableStartPoints.put(node.start, node);
        localVariableEndPoints.put(node.end, node);
      }
    }
  }

  // Local variable procedures.

  public List<Local> openLocals(LabelNode label) {
    Collection<LocalVariableNode> nodes = localVariableStartPoints.get(label);
    ArrayList<Local> locals = new ArrayList<>(nodes.size());
    for (LocalVariableNode node : nodes) {
      locals.add(setLocal(node.index, Type.getType(node.desc), localVariables.get(node)));
    }
    // Sort to ensure deterministic instruction ordering (correctness is unaffected).
    locals.sort(Comparator.comparingInt(local -> local.slot.register));
    return locals;
  }

  public List<Local> getLocalsToClose(LabelNode label) {
    Collection<LocalVariableNode> nodes = localVariableEndPoints.get(label);
    ArrayList<Local> locals = new ArrayList<>(nodes.size());
    for (LocalVariableNode node : nodes) {
      Type type = Type.getType(node.desc);
      int register = getLocalRegister(node.index, type);
      Local local = getLocalForRegister(register);
      assert local != null;
      locals.add(local);
    }
    // Sort to ensure deterministic instruction ordering (correctness is unaffected).
    locals.sort(Comparator.comparingInt(local -> local.slot.register));
    return locals;
  }

  public void closeLocals(List<Local> localsToClose) {
    for (Local local : localsToClose) {
      assert local != null;
      assert local == getLocalForRegister(local.slot.register);
      setLocalForRegister(local.slot.register, local.slot.type, null);
    }
  }

  public ImmutableList<Local> getLocals() {
    ImmutableList.Builder<Local> nonNullLocals = ImmutableList.builder();
    for (Local local : locals) {
      if (local != null) {
        nonNullLocals.add(local);
      }
    }
    return nonNullLocals.build();
  }

  int getLocalRegister(int index, Type type) {
    assert index < localsSize;
    if (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY) {
      return index;
    }
    return Slot.isCategory1(type) ? index + localsSize : index + 2 * localsSize;
  }

  public DebugLocalInfo getLocalInfoForRegister(int register) {
    if (register >= locals.length) {
      return null;
    }
    Local local = getLocalForRegister(register);
    return local == null ? null : local.info;
  }

  private Local getLocalForRegister(int register) {
    return locals[register];
  }

  private Local getLocal(int index, Type type) {
    return getLocalForRegister(getLocalRegister(index, type));
  }

  private Local setLocal(int index, Type type, DebugLocalInfo info) {
    return setLocalForRegister(getLocalRegister(index, type), type, info);
  }

  private Local setLocalForRegister(int register, Type type, DebugLocalInfo info) {
    Slot slot = new Slot(register, type);
    Local local = new Local(slot, info);
    locals[register] = local;
    return local;
  }

  public int writeLocal(int index, Type type) {
    assert type != null;
    Local local = getLocal(index, type);
    assert local == null || local.info == null || local.slot.isCompatibleWith(type);
    // We cannot assume consistency for writes because we do not have complete information about the
    // scopes of locals. We assume the program to be verified and overwrite if the types mismatch.
    if (local == null || (local.info == null && !local.slot.type.equals(type))) {
      local = setLocal(index, type, null);
    }
    return local.slot.register;
  }

  public Slot readLocal(int index, Type type) {
    Local local = getLocal(index, type);
    assert local != null;
    assert local.slot.isCompatibleWith(type);
    return local.slot;
  }

  // Stack procedures.

  public int push(Type type) {
    assert type != null;
    int top = topOfStack;
    // For simplicity, every stack slot (and local variable) is wide (uses two registers).
    topOfStack += 2;
    Slot slot = new Slot(top, type);
    stack.push(slot);
    return top;
  }

  public Slot peek() {
    return stack.peek();
  }

  public Slot peek(Type type) {
    Slot slot = stack.peek();
    assert slot.isCompatibleWith(type);
    return slot;
  }

  public Slot pop() {
    assert topOfStack > startOfStack;
    // For simplicity, every stack slot (and local variable) is wide (uses two registers).
    topOfStack -= 2;
    Slot slot = stack.pop();
    assert slot.type != null;
    assert slot.register == topOfStack;
    return slot;
  }

  public Slot pop(Type type) {
    Slot slot = pop();
    assert slot.isCompatibleWith(type);
    return slot;
  }

  public Slot[] popReverse(int count) {
    Slot[] slots = new Slot[count];
    for (int i = count - 1; i >= 0; i--) {
      slots[i] = pop();
    }
    return slots;
  }

  public Slot[] popReverse(int count, Type type) {
    Slot[] slots = popReverse(count);
    assert verifySlots(slots, type);
    return slots;
  }

  // State procedures.

  public boolean hasState(int offset) {
    return targetStates.get(offset) != null;
  }

  public void restoreState(int offset) {
    Snapshot snapshot = targetStates.get(offset);
    assert snapshot != null;
    assert locals.length == snapshot.locals.length;
    for (int i = 0; i < locals.length; i++) {
      locals[i] = snapshot.locals[i];
    }
    stack.clear();
    stack.addAll(snapshot.stack);
    topOfStack = startOfStack + 2 * stack.size();
  }

  public boolean recordStateForTarget(int target, JarSourceCode source) {
    return recordStateForTarget(target, locals.clone(), ImmutableList.copyOf(stack), source);
  }

  public boolean recordStateForExceptionalTarget(int target, JarSourceCode source) {
    return recordStateForTarget(target, locals.clone(), ImmutableList.of(), source);
  }

  private boolean recordStateForTarget(int target, Local[] locals, ImmutableList<Slot> stack,
      JarSourceCode source) {
    if (!localVariables.isEmpty()) {
      for (int i = 0; i < locals.length; i++) {
        if (locals[i] != null) {
          locals[i] = new Local(locals[i].slot, null);
        }
      }
      // TODO(zerny): Precompute and sort the local ranges.
      for (LocalVariableNode node : localVariables.keySet()) {
        int startOffset = source.getOffset(node.start);
        int endOffset = source.getOffset(node.end);
        if (startOffset <= target && target < endOffset) {
          int register = getLocalRegister(node.index, Type.getType(node.desc));
          Local local = locals[register];
          locals[register] = new Local(local.slot, localVariables.get(node));
        }
      }
    }
    Snapshot snapshot = targetStates.get(target);
    if (snapshot != null) {
      Local[] newLocals = mergeLocals(snapshot.locals, locals);
      ImmutableList<Slot> newStack = mergeStacks(snapshot.stack, stack);
      if (newLocals != snapshot.locals || newStack != snapshot.stack) {
        targetStates.put(target, new Snapshot(newLocals, newStack));
        return true;
      }
      // The snapshot is up to date - no new type information recoded.
      return false;
    }
    targetStates.put(target, new Snapshot(locals, stack));
    return true;
  }

  private ImmutableList<Slot> mergeStacks(
      ImmutableList<Slot> currentStack, ImmutableList<Slot> newStack) {
    assert currentStack.size() == newStack.size();
    List<Slot> mergedStack = null;
    for (int i = 0; i < currentStack.size(); i++) {
      if (currentStack.get(i).type == JarState.NULL_TYPE &&
          newStack.get(i).type != JarState.NULL_TYPE) {
        if (mergedStack == null) {
          mergedStack = new ArrayList<>();
          mergedStack.addAll(currentStack.subList(0, i));
        }
        mergedStack.add(newStack.get(i));
      } else if (mergedStack != null) {
        assert currentStack.get(i).isCompatibleWith(newStack.get(i).type);
        mergedStack.add(currentStack.get(i));
      }
    }
    return mergedStack != null ? ImmutableList.copyOf(mergedStack) : currentStack;
  }

  private Local[] mergeLocals(Local[] currentLocals, Local[] newLocals) {
    assert currentLocals.length == newLocals.length;
    Local[] mergedLocals = null;
    for (int i = 0; i < currentLocals.length; i++) {
      Local currentLocal = currentLocals[i];
      Local newLocal = newLocals[i];
      if (currentLocal == null || newLocal == null) {
        continue;
      }
      // If this assert triggers we can get different debug information for the same local
      // on different control-flow paths and we will have to merge them.
      assert currentLocal.info == newLocal.info;
      if (currentLocal.slot.type == JarState.NULL_TYPE &&
          newLocal.slot.type != JarState.NULL_TYPE) {
        if (mergedLocals == null) {
          mergedLocals = new Local[currentLocals.length];
          System.arraycopy(currentLocals, 0, mergedLocals, 0, i);
        }
        Slot newSlot = new Slot(newLocal.slot.register, newLocal.slot.type);
        mergedLocals[i] = new Local(newSlot, newLocal.info);
      } else if (mergedLocals != null) {
        mergedLocals[i] = currentLocals[i];
      }
    }
    return mergedLocals != null ? mergedLocals : currentLocals;
  }

  private static boolean verifyStack(List<Slot> stack, List<Slot> other) {
    assert stack.size() == other.size();
    int i = 0;
    for (Slot slot : stack) {
      assert slot.isCompatibleWith(other.get(i++).type);
    }
    return true;
  }

  // Other helpers.

  private static boolean verifySlots(Slot[] slots, Type type) {
    for (Slot slot : slots) {
      assert slot.isCompatibleWith(type);
    }
    return true;
  }

  // Printing helpers.

  public String toString() {
    return "locals: " + localsToString(Arrays.asList(locals)) + ", stack: " + stackToString(stack);
  }

  public static String stackToString(Collection<Slot> stack) {
    List<String> strings = new ArrayList<>(stack.size());
    for (Slot slot : stack) {
      strings.add(slot.type.toString());
    }
    StringBuilder builder = new StringBuilder("{ ");
    for (int i = strings.size() - 1; i >= 0; i--) {
      builder.append(strings.get(i));
      if (i > 0) {
        builder.append(", ");
      }
    }
    builder.append(" }");
    return builder.toString();
  }

  public static String localsToString(Collection<Local> locals) {
    StringBuilder builder = new StringBuilder("{ ");
    boolean first = true;
    for (Local local : locals) {
      if (!first) {
        builder.append(", ");
      } else {
        first = false;
      }
      if (local == null) {
        builder.append("_");
      } else if (local.info != null) {
        builder.append(local.info);
      } else {
        builder.append(local.slot.type.toString());
      }
    }
    builder.append(" }");
    return builder.toString();
  }
}
