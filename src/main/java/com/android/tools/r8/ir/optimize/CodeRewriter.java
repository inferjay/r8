// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.optimize;

import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.graph.AppInfo;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.ArrayGet;
import com.android.tools.r8.ir.code.ArrayPut;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.Binop;
import com.android.tools.r8.ir.code.CatchHandlers;
import com.android.tools.r8.ir.code.Cmp;
import com.android.tools.r8.ir.code.Cmp.Bias;
import com.android.tools.r8.ir.code.ConstNumber;
import com.android.tools.r8.ir.code.ConstType;
import com.android.tools.r8.ir.code.DominatorTree;
import com.android.tools.r8.ir.code.Goto;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.If;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionIterator;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.Invoke;
import com.android.tools.r8.ir.code.InvokeDirect;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.InvokeVirtual;
import com.android.tools.r8.ir.code.JumpInstruction;
import com.android.tools.r8.ir.code.MoveType;
import com.android.tools.r8.ir.code.NewArrayEmpty;
import com.android.tools.r8.ir.code.NewArrayFilledData;
import com.android.tools.r8.ir.code.NumericType;
import com.android.tools.r8.ir.code.Phi;
import com.android.tools.r8.ir.code.Return;
import com.android.tools.r8.ir.code.StaticGet;
import com.android.tools.r8.ir.code.StaticPut;
import com.android.tools.r8.ir.code.Switch;
import com.android.tools.r8.ir.code.Switch.Type;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.LongInterval;
import com.google.common.base.Equivalence;
import com.google.common.base.Equivalence.Wrapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class CodeRewriter {

  private static final int UNKNOWN_CAN_THROW = 0;
  private static final int CAN_THROW = 1;
  private static final int CANNOT_THROW = 2;
  private static final int MAX_FILL_ARRAY_SIZE = 4 * Constants.KILOBYTE;

  private final AppInfo appInfo;
  private final DexItemFactory dexItemFactory;

  public CodeRewriter(AppInfo appInfo) {
    this.appInfo = appInfo;
    this.dexItemFactory = appInfo.dexItemFactory;
  }

  /**
   * Removes all debug positions that are not needed to maintain proper stack trace information.
   * If a debug position is followed by another debug position and no instructions between the two
   * can throw then it is unneeded (in a release build).
   * If a block with a position has (normal) outgoing edges, this property depends on the
   * possibility of the successors throwing before the next debug position is hit.
   */
  public static boolean removedUnneededDebugPositions(IRCode code) {
    computeThrowsColorForAllBlocks(code);
    for (BasicBlock block : code.blocks) {
      InstructionListIterator iterator = block.listIterator();
      while (iterator.hasNext()) {
        Instruction instruction = iterator.next();
        if (instruction.isDebugPosition()
            && getThrowsColorForBlock(block, iterator.nextIndex()) == CANNOT_THROW) {
          iterator.remove();
        }
      }
    }
    return true;
  }

  private static void computeThrowsColorForAllBlocks(IRCode code) {
    // First pass colors blocks in reverse topological order, based on the instructions.
    code.clearMarks();
    List<BasicBlock> blocks = code.blocks;
    ArrayList<BasicBlock> worklist = new ArrayList<>();
    for (int i = blocks.size() - 1; i >= 0; i--) {
      BasicBlock block = blocks.get(i);
      // Mark the block as not-throwing if no successor implies otherwise.
      // This ensures that a loop back to this block will be seen as non-throwing.
      block.setColor(CANNOT_THROW);
      int color = getThrowsColorForBlock(block, 0);
      block.setColor(color);
      if (color == UNKNOWN_CAN_THROW) {
        worklist.add(block);
      }
    }
    // A fixed point then ensures that we propagate the color backwards over normal edges.
    ArrayList<BasicBlock> remaining = new ArrayList<>(worklist.size());
    while (!worklist.isEmpty()) {
      ImmutableList<BasicBlock> work = new ImmutableList.Builder<BasicBlock>()
          .addAll(worklist)
          .addAll(remaining)
          .build();
      worklist.clear();
      remaining.clear();
      for (BasicBlock block : work) {
        if (!block.hasColor(UNKNOWN_CAN_THROW)) {
          continue;
        }
        block.setColor(CANNOT_THROW);
        int color = getThrowsColorForSuccessors(block);
        block.setColor(color);
        if (color == UNKNOWN_CAN_THROW) {
          remaining.add(block);
        } else {
          for (BasicBlock predecessor : block.getNormalPredecessors()) {
            if (predecessor.hasColor(UNKNOWN_CAN_THROW)) {
              worklist.add(predecessor);
            }
          }
        }
      }
    }
    // Any remaining set of blocks represents a cycle of blocks containing no throwing instructions.
    for (BasicBlock block : remaining) {
      assert !block.canThrow();
      block.setColor(CANNOT_THROW);
    }
  }

  private static int getThrowsColorForBlock(BasicBlock block, int index) {
    InstructionListIterator iterator = block.listIterator(index);
    while (iterator.hasNext()) {
      Instruction instruction = iterator.next();
      if (instruction.isDebugPosition()) {
        return CANNOT_THROW;
      }
      if (instruction.instructionTypeCanThrow()) {
        return CAN_THROW;
      }
    }
    return getThrowsColorForSuccessors(block);
  }

  private static int getThrowsColorForSuccessors(BasicBlock block) {
    int color = CANNOT_THROW;
    for (BasicBlock successor : block.getNormalSucessors()) {
      if (successor.hasColor(CAN_THROW)) {
        return CAN_THROW;
      }
      if (successor.hasColor(UNKNOWN_CAN_THROW)) {
        color = UNKNOWN_CAN_THROW;
      }
    }
    return color;
  }

  private static boolean removedTrivialGotos(IRCode code) {
    ListIterator<BasicBlock> iterator = code.listIterator();
    assert iterator.hasNext();
    BasicBlock block = iterator.next();
    BasicBlock nextBlock;
    do {
      nextBlock = iterator.hasNext() ? iterator.next() : null;
      // Trivial goto block are only kept if they are self-targeting or are targeted by
      // fallthroughs.
      BasicBlock blk = block;  // Additional local for lambda below.
      assert !block.isTrivialGoto()
          || block.exit().asGoto().getTarget() == block
          || block.getPredecessors().stream().anyMatch((b) -> b.exit().fallthroughBlock() == blk);
      // Trivial goto blocks never target the next block (in that case there should just be a
      // fallthrough).
      assert !block.isTrivialGoto() || block.exit().asGoto().getTarget() != nextBlock;
      block = nextBlock;
    } while (block != null);
    return true;
  }

  private static BasicBlock endOfGotoChain(BasicBlock block) {
    block.mark();
    BasicBlock target = block;
    while (target.isTrivialGoto()) {
      BasicBlock nextTarget = target.exit().asGoto().getTarget();
      if (nextTarget.isMarked()) {
        clearTrivialGotoMarks(block);
        return nextTarget;
      }
      nextTarget.mark();
      target = nextTarget;
    }
    clearTrivialGotoMarks(block);
    return target;
  }

  private static void clearTrivialGotoMarks(BasicBlock block) {
    while (block.isMarked()) {
      block.clearMark();
      if (block.isTrivialGoto()) {
        block = block.exit().asGoto().getTarget();
      }
    }
  }

  private static void collapsTrivialGoto(
      BasicBlock block, BasicBlock nextBlock, List<BasicBlock> blocksToRemove) {

    // This is the base case for GOTO loops.
    if (block.exit().asGoto().getTarget() == block) {
      return;
    }

    BasicBlock target = endOfGotoChain(block);

    boolean needed = false;
    if (target != nextBlock) {
      for (BasicBlock pred : block.getPredecessors()) {
        if (pred.exit().fallthroughBlock() == block) {
          needed = true;
          break;
        }
      }
    }

    // This implies we are in a loop of GOTOs. In that case, we will iteratively remove each trival
    // GOTO one-by-one until the above base case (one block targeting itself) is left.
    if (target == block) {
      target = block.exit().asGoto().getTarget();
    }

    if (!needed) {
      blocksToRemove.add(block);
      for (BasicBlock pred : block.getPredecessors()) {
        pred.replaceSuccessor(block, target);
      }
      for (BasicBlock succ : block.getSuccessors()) {
        succ.getPredecessors().remove(block);
      }
      for (BasicBlock pred : block.getPredecessors()) {
        if (!target.getPredecessors().contains(pred)) {
          target.getPredecessors().add(pred);
        }
      }
    }
  }

  private static void collapsIfTrueTarget(BasicBlock block) {
    If insn = block.exit().asIf();
    BasicBlock target = insn.getTrueTarget();
    BasicBlock newTarget = endOfGotoChain(target);
    BasicBlock fallthrough = insn.fallthroughBlock();
    BasicBlock newFallthrough = endOfGotoChain(fallthrough);
    if (target != newTarget) {
      insn.getBlock().replaceSuccessor(target, newTarget);
      target.getPredecessors().remove(block);
      if (!newTarget.getPredecessors().contains(block)) {
        newTarget.getPredecessors().add(block);
      }
    }
    if (block.exit().isIf()) {
      insn = block.exit().asIf();
      if (insn.getTrueTarget() == newFallthrough) {
        // Replace if with the same true and fallthrough target with a goto to the fallthrough.
        block.replaceSuccessor(insn.getTrueTarget(), fallthrough);
        assert block.exit().isGoto();
        assert block.exit().asGoto().getTarget() == fallthrough;
      }
    }
  }

  private static void collapsNonFallthroughSwitchTargets(BasicBlock block) {
    Switch insn = block.exit().asSwitch();
    BasicBlock fallthroughBlock = insn.fallthroughBlock();
    Set<BasicBlock> replacedBlocks = new HashSet<>();
    for (int j = 0; j < insn.targetBlockIndices().length; j++) {
      BasicBlock target = insn.targetBlock(j);
      if (target != fallthroughBlock) {
        BasicBlock newTarget = endOfGotoChain(target);
        if (target != newTarget && !replacedBlocks.contains(target)) {
          insn.getBlock().replaceSuccessor(target, newTarget);
          target.getPredecessors().remove(block);
          if (!newTarget.getPredecessors().contains(block)) {
            newTarget.getPredecessors().add(block);
          }
          replacedBlocks.add(target);
        }
      }
    }
  }

  /**
   * Inline the indirection of switch maps into the switch statement.
   * <p>
   * To ensure binary compatibility, javac generated code does not use ordinal values of enums
   * directly in switch statements but instead generates a companion class that computes a mapping
   * from switch branches to ordinals at runtime. As we have whole-program knowledge, we can
   * analyze these maps and inline the indirection into the switch map again.
   * <p>
   * In particular, we look for code of the form
   *
   * <blockquote><pre>
   * switch(CompanionClass.$switchmap$field[enumValue.ordinal()]) {
   *   ...
   * }
   * </pre></blockquote>
   * See {@link #extractIndexMapFrom} and {@link #extractOrdinalsMapFor} for
   * details of the companion class and ordinals computation.
   */
  public void removeSwitchMaps(IRCode code) {
    for (BasicBlock block : code.blocks) {
      InstructionListIterator it = block.listIterator();
      while (it.hasNext()) {
        Instruction insn = it.next();
        // Pattern match a switch on a switch map as input.
        if (insn.isSwitch()) {
          Switch switchInsn = insn.asSwitch();
          Instruction input = switchInsn.inValues().get(0).definition;
          if (input == null || !input.isArrayGet()) {
            continue;
          }
          ArrayGet arrayGet = input.asArrayGet();
          Instruction index = arrayGet.index().definition;
          if (index == null || !index.isInvokeVirtual()) {
            continue;
          }
          InvokeVirtual ordinalInvoke = index.asInvokeVirtual();
          DexMethod ordinalMethod = ordinalInvoke.getInvokedMethod();
          DexClass enumClass = appInfo.definitionFor(ordinalMethod.holder);
          if (enumClass == null
              || (!enumClass.accessFlags.isEnum() && enumClass.type != dexItemFactory.enumType)
              || ordinalMethod.name != dexItemFactory.ordinalMethodName
              || ordinalMethod.proto.returnType != dexItemFactory.intType
              || !ordinalMethod.proto.parameters.isEmpty()) {
            continue;
          }
          Instruction array = arrayGet.array().definition;
          if (array == null || !array.isStaticGet()) {
            continue;
          }
          StaticGet staticGet = array.asStaticGet();
          if (staticGet.getField().name.toSourceString().startsWith("$SwitchMap$")) {
            Int2ReferenceMap<DexField> indexMap = extractIndexMapFrom(staticGet.getField());
            if (indexMap == null || indexMap.isEmpty()) {
              continue;
            }
            // Due to member rebinding, only the fields are certain to provide the actual enums
            // class.
            DexType switchMapHolder = indexMap.values().iterator().next().getHolder();
            Reference2IntMap ordinalsMap = extractOrdinalsMapFor(switchMapHolder);
            if (ordinalsMap != null) {
              Int2IntMap targetMap = new Int2IntArrayMap();
              int keys[] = new int[switchInsn.numberOfKeys()];
              for (int i = 0; i < keys.length; i++) {
                keys[i] = ordinalsMap.getInt(indexMap.get(switchInsn.getKey(i)));
                targetMap.put(keys[i], switchInsn.targetBlockIndices()[i]);
              }
              Arrays.sort(keys);
              int[] targets = new int[keys.length];
              for (int i = 0; i < keys.length; i++) {
                targets[i] = targetMap.get(keys[i]);
              }

              Switch newSwitch = new Switch(Type.SPARSE, ordinalInvoke.outValue(), keys,
                  targets, switchInsn.getFallthroughBlockIndex());
              // Replace the switch itself.
              it.replaceCurrentInstruction(newSwitch);
              // If the original input to the switch is now unused, remove it too. It is not dead
              // as it might have side-effects but we ignore these here.
              if (arrayGet.outValue().numberOfUsers() == 0) {
                arrayGet.inValues().forEach(v -> v.removeUser(arrayGet));
                arrayGet.getBlock().removeInstruction(arrayGet);
              }
              if (staticGet.outValue().numberOfUsers() == 0) {
                assert staticGet.inValues().isEmpty();
                staticGet.getBlock().removeInstruction(staticGet);
              }
            }
          }
        }
      }
    }
  }

  /**
   * Extracts the mapping from ordinal values to switch case constants.
   * <p>
   * This is done by pattern-matching on the class initializer of the synthetic switch map class.
   * For a switch
   *
   * <blockquote><pre>
   * switch (day) {
   *   case WEDNESDAY:
   *   case FRIDAY:
   *     System.out.println("3 or 5");
   *     break;
   *   case SUNDAY:
   *     System.out.println("7");
   *     break;
   *   default:
   *     System.out.println("other");
   * }
   * </pre></blockquote>
   *
   * the generated companing class initializer will have the form
   *
   * <blockquote><pre>
   * class Switches$1 {
   *   static {
   *   $SwitchMap$switchmaps$Days[Days.WEDNESDAY.ordinal()] = 1;
   *   $SwitchMap$switchmaps$Days[Days.FRIDAY.ordinal()] = 2;
   *   $SwitchMap$switchmaps$Days[Days.SUNDAY.ordinal()] = 3;
   * }
   * </pre></blockquote>
   *
   * Note that one map per class is generated, so the map might contain additional entries as used
   * by other switches in the class.
   */
  private Int2ReferenceMap<DexField> extractIndexMapFrom(DexField field) {
    DexClass clazz = appInfo.definitionFor(field.getHolder());
    if (!clazz.accessFlags.isSynthetic()) {
      return null;
    }
    DexEncodedMethod initializer = clazz.getClassInitializer();
    if (initializer == null || initializer.getCode() == null) {
      return null;
    }
    IRCode code = initializer.getCode().buildIR(initializer, new InternalOptions());
    Int2ReferenceMap<DexField> switchMap = new Int2ReferenceArrayMap<>();
    for (BasicBlock block : code.blocks) {
      InstructionListIterator it = block.listIterator();
      Instruction insn = it.nextUntil(i -> i.isStaticGet() && i.asStaticGet().getField() == field);
      if (insn == null) {
        continue;
      }
      for (Instruction use : insn.outValue().uniqueUsers()) {
        if (use.isArrayPut()) {
          Instruction index = use.asArrayPut().source().definition;
          if (index == null || !index.isConstNumber()) {
            return null;
          }
          int integerIndex = index.asConstNumber().getIntValue();
          Instruction value = use.asArrayPut().index().definition;
          if (value == null || !value.isInvokeVirtual()) {
            return null;
          }
          InvokeVirtual invoke = value.asInvokeVirtual();
          DexClass holder = appInfo.definitionFor(invoke.getInvokedMethod().holder);
          if (holder == null ||
              (!holder.accessFlags.isEnum() && holder.type != dexItemFactory.enumType)) {
            return null;
          }
          Instruction enumGet = invoke.arguments().get(0).definition;
          if (enumGet == null || !enumGet.isStaticGet()) {
            return null;
          }
          DexField enumField = enumGet.asStaticGet().getField();
          if (!appInfo.definitionFor(enumField.getHolder()).accessFlags.isEnum()) {
            return null;
          }
          if (switchMap.put(integerIndex, enumField) != null) {
            return null;
          }
        } else {
          return null;
        }
      }
    }
    return switchMap;
  }

  /**
   * Extracts the ordinal values for an Enum class from the classes static initializer.
   * <p>
   * An Enum class has a field for each value. In the class initializer, each field is initialized
   * to a singleton object that represents the value. This code matches on the corresponding call
   * to the constructor (instance initializer) and extracts the value of the second argument, which
   * is the ordinal.
   */
  private Reference2IntMap<DexField> extractOrdinalsMapFor(DexType enumClass) {
    DexClass clazz = appInfo.definitionFor(enumClass);
    if (clazz == null || clazz.isLibraryClass()) {
      // We have to keep binary compatibility in tact for libraries.
      return null;
    }
    DexEncodedMethod initializer = clazz.getClassInitializer();
    if (!clazz.accessFlags.isEnum() || initializer == null || initializer.getCode() == null) {
      return null;
    }
    IRCode code = initializer.getCode().buildIR(initializer, new InternalOptions());
    Reference2IntMap<DexField> ordinalsMap = new Reference2IntArrayMap<>();
    ordinalsMap.defaultReturnValue(-1);
    InstructionIterator it = code.instructionIterator();
    while (it.hasNext()) {
      Instruction insn = it.next();
      if (!insn.isStaticPut()) {
        continue;
      }
      StaticPut staticPut = insn.asStaticPut();
      if (staticPut.getField().type != enumClass) {
        continue;
      }
      Instruction newInstance = staticPut.inValue().definition;
      if (newInstance == null || !newInstance.isNewInstance()) {
        continue;
      }
      Instruction ordinal = null;
      for (Instruction ctorCall : newInstance.outValue().uniqueUsers()) {
        if (!ctorCall.isInvokeDirect()) {
          continue;
        }
        InvokeDirect invoke = ctorCall.asInvokeDirect();
        if (!dexItemFactory.isConstructor(invoke.getInvokedMethod())
            || invoke.arguments().size() < 3) {
          continue;
        }
        ordinal = invoke.arguments().get(2).definition;
        break;
      }
      if (ordinal == null || !ordinal.isConstNumber()) {
        return null;
      }
      if (ordinalsMap.put(staticPut.getField(), ordinal.asConstNumber().getIntValue()) != -1) {
        return null;
      }
    }
    return ordinalsMap;
  }

  /**
   * Rewrite all branch targets to the destination of trivial goto chains when possible.
   * Does not rewrite fallthrough targets as that would require block reordering and the
   * transformation only makes sense after SSA destruction where there are no phis.
   */
  public static void collapsTrivialGotos(DexEncodedMethod method, IRCode code) {
    assert code.isConsistentGraph();
    List<BasicBlock> blocksToRemove = new ArrayList<>();
    // Rewrite all non-fallthrough targets to the end of trivial goto chains and remove
    // first round of trivial goto blocks.
    ListIterator<BasicBlock> iterator = code.listIterator();
    assert iterator.hasNext();
    BasicBlock block = iterator.next();
    BasicBlock nextBlock;

    // The marks will be used for cycle detection.
    code.clearMarks();
    do {
      nextBlock = iterator.hasNext() ? iterator.next() : null;
      if (block.isTrivialGoto()) {
        collapsTrivialGoto(block, nextBlock, blocksToRemove);
      }
      if (block.exit().isIf()) {
        collapsIfTrueTarget(block);
      }
      if (block.exit().isSwitch()) {
        collapsNonFallthroughSwitchTargets(block);
      }
      block = nextBlock;
    } while (nextBlock != null);
    code.removeBlocks(blocksToRemove);
    // Get rid of gotos to the next block.
    while (!blocksToRemove.isEmpty()) {
      blocksToRemove = new ArrayList<>();
      iterator = code.listIterator();
      block = iterator.next();
      do {
        nextBlock = iterator.hasNext() ? iterator.next() : null;
        if (block.isTrivialGoto()) {
          collapsTrivialGoto(block, nextBlock, blocksToRemove);
        }
        block = nextBlock;
      } while (block != null);
      code.removeBlocks(blocksToRemove);
    }
    assert removedTrivialGotos(code);
    assert code.isConsistentGraph();
  }

  public void identifyReturnsArgument(
      DexEncodedMethod method, IRCode code) {
    if (code.getNormalExitBlock() != null) {
      Return ret = code.getNormalExitBlock().exit().asReturn();
      if (!ret.isReturnVoid()) {
        Value returnValue = ret.returnValue();
        if (returnValue.isArgument()) {
          // Find the argument number.
          int index = code.collectArguments().indexOf(returnValue);
          assert index != -1;
          method.markReturnsArgument(index);
        }
        if (returnValue.isConstant() && returnValue.definition.isConstNumber()) {
          long value = returnValue.definition.asConstNumber().getRawValue();
          method.markReturnsConstant(value);
        }
        if (returnValue.isNeverNull()) {
          method.markNeverReturnsNull();
        }
      }
    }
  }

  private boolean checkArgumentType(InvokeMethod invoke, DexMethod target, int argumentIndex) {
    DexType returnType = invoke.getInvokedMethod().proto.returnType;
    // TODO(sgjesse): Insert cast if required.
    if (invoke.isInvokeStatic()) {
      return invoke.getInvokedMethod().proto.parameters.values[argumentIndex] == returnType;
    } else {
      if (argumentIndex == 0) {
        return invoke.getInvokedMethod().getHolder() == returnType;
      } else {
        return invoke.getInvokedMethod().proto.parameters.values[argumentIndex - 1] == returnType;
      }
    }
  }

  // Replace result uses for methods where something is known about what is returned.
  public void rewriteMoveResult(IRCode code) {
    if (!appInfo.hasSubtyping()) {
      return;
    }
    InstructionIterator iterator = code.instructionIterator();
    while (iterator.hasNext()) {
      Instruction current = iterator.next();
      if (current.isInvokeMethod()) {
        InvokeMethod invoke = current.asInvokeMethod();
        if (invoke.outValue() != null) {
          DexEncodedMethod target = invoke.computeSingleTarget(appInfo.withSubtyping());
          if (target != null) {
            DexMethod invokedMethod = target.method;
            // Check if the invoked method is known to return one of its arguments.
            DexEncodedMethod definition = appInfo.definitionFor(invokedMethod);
            if (definition != null && definition.getOptimizationInfo().returnsArgument()) {
              int argumentIndex = definition.getOptimizationInfo().getReturnedArgument();
              // Replace the out value of the invoke with the argument and ignore the out value.
              if (argumentIndex != -1 && checkArgumentType(invoke, target.method, argumentIndex)) {
                Value argument = invoke.arguments().get(argumentIndex);
                assert (invoke.outType() == argument.outType()) ||
                    (invoke.outType() == MoveType.OBJECT
                        && argument.outType() == MoveType.SINGLE
                        && argument.getConstInstruction().asConstNumber().isZero());
                invoke.outValue().replaceUsers(argument);
                invoke.setOutValue(null);
              }
            }
          }
        }
      }
    }
    assert code.isConsistentGraph();
  }

  private boolean canBeFolded(Instruction instruction) {
    return (instruction.isBinop() && instruction.asBinop().canBeFolded()) ||
        (instruction.isUnop() && instruction.asUnop().canBeFolded());
  }

  public void foldConstants(IRCode code) {
    Queue<BasicBlock> worklist = new LinkedList<>();
    worklist.addAll(code.blocks);
    for (BasicBlock block = worklist.poll(); block != null; block = worklist.poll()) {
      InstructionIterator iterator = block.iterator();
      while (iterator.hasNext()) {
        Instruction current = iterator.next();
        Instruction folded;
        if (canBeFolded(current)) {
          folded = current.fold(code);
          iterator.replaceCurrentInstruction(folded);
          folded.outValue().uniqueUsers()
              .forEach(instruction -> worklist.add(instruction.getBlock()));
        }
      }
    }
    assert code.isConsistentSSA();
  }

  // Constants are canonicalized in the entry block. We split some of them when it is likely
  // that having them canonicalized in the entry block will lead to poor code quality.
  public void splitConstants(IRCode code) {
    for (BasicBlock block : code.blocks) {
      // Split constants that flow into phis. It is likely that these constants will have moves
      // generated for them anyway and we might as well insert a const instruction in the right
      // predecessor block.
      splitPhiConstants(code, block);
      // Split constants that flow into ranged invokes. This gives the register allocator more
      // freedom in assigning register to ranged invokes which can greatly reduce the number
      // of register needed (and thereby code size as well).
      splitRangedInvokeConstants(code, block);
    }
  }

  private void splitRangedInvokeConstants(IRCode code, BasicBlock block) {
    InstructionListIterator it = block.listIterator();
    while (it.hasNext()) {
      Instruction current = it.next();
      if (current.isInvoke() && current.asInvoke().requiredArgumentRegisters() > 5) {
        Invoke invoke = current.asInvoke();
        it.previous();
        Map<ConstNumber, ConstNumber> oldToNew = new HashMap<>();
        for (int i = 0; i < invoke.inValues().size(); i++) {
          Value value = invoke.inValues().get(i);
          if (value.isConstant() && value.numberOfUsers() > 1) {
            ConstNumber definition = value.getConstInstruction().asConstNumber();
            Value originalValue = definition.outValue();
            ConstNumber newNumber = oldToNew.get(definition);
            if (newNumber == null) {
              newNumber = ConstNumber.copyOf(code, definition);
              it.add(newNumber);
              oldToNew.put(definition, newNumber);
            }
            invoke.inValues().set(i, newNumber.outValue());
            originalValue.removeUser(invoke);
            newNumber.outValue().addUser(invoke);
          }
        }
        it.next();
      }
    }
  }

  private void splitPhiConstants(IRCode code, BasicBlock block) {
    for (int i = 0; i < block.getPredecessors().size(); i++) {
      Map<ConstNumber, ConstNumber> oldToNew = new HashMap<>();
      BasicBlock predecessor = block.getPredecessors().get(i);
      for (Phi phi : block.getPhis()) {
        Value operand = phi.getOperand(i);
        if (!operand.isPhi() && operand.isConstant()) {
          ConstNumber definition = operand.getConstInstruction().asConstNumber();
          ConstNumber newNumber = oldToNew.get(definition);
          Value originalValue = definition.outValue();
          if (newNumber == null) {
            newNumber = ConstNumber.copyOf(code, definition);
            oldToNew.put(definition, newNumber);
            insertConstantInBlock(newNumber, predecessor);
          }
          phi.getOperands().set(i, newNumber.outValue());
          originalValue.removePhiUser(phi);
          newNumber.outValue().addPhiUser(phi);
        }
      }
    }
  }

  public void shortenLiveRanges(IRCode code) {
    // Currently, we are only shortening the live range of constants in the entry block.
    // TODO(ager): Generalize this to shorten live ranges for more instructions? Currently
    // doing so seems to make things worse.
    DominatorTree dominatorTree = new DominatorTree(code);
    BasicBlock block = code.blocks.get(0);
    InstructionListIterator it = block.listIterator();
    List<Instruction> toInsertInThisBlock = new ArrayList<>();
    while (it.hasNext()) {
      Instruction instruction = it.next();
      if (instruction.isConstNumber()) {
        // Collect the blocks for all users of the constant.
        List<BasicBlock> userBlocks = new LinkedList<>();
        for (Instruction user : instruction.outValue().uniqueUsers()) {
          userBlocks.add(user.getBlock());
        }
        for (Phi phi : instruction.outValue().uniquePhiUsers()) {
          userBlocks.add(phi.getBlock());
        }
        // Locate the closest dominator block for all user blocks.
        BasicBlock dominator = dominatorTree.closestDominator(userBlocks);
        // If the closest dominator block is a block that uses the constant for a phi the constant
        // needs to go in the immediate dominator block so that it is available for phi moves.
        for (Phi phi : instruction.outValue().uniquePhiUsers()) {
          if (phi.getBlock() == dominator) {
            dominator = dominatorTree.immediateDominator(dominator);
            break;
          }
        }
        // Move the const instruction as close to its uses as possible.
        it.detach();
        if (dominator != block) {
          insertConstantInBlock(instruction, dominator);
        } else {
          toInsertInThisBlock.add(instruction);
        }
      }
    }
    for (Instruction toInsert : toInsertInThisBlock) {
      insertConstantInBlock(toInsert, block);
    }
  }

  private void insertConstantInBlock(Instruction instruction, BasicBlock block) {
    boolean hasCatchHandlers = block.hasCatchHandlers();
    InstructionListIterator insertAt = block.listIterator();
    // Place the instruction as late in the block as we can. It needs to go before users
    // and if we have catch handlers it needs to be placed before the throwing instruction.
    insertAt.nextUntil(i -> {
      return i.inValues().contains(instruction.outValue())
          || i.isJumpInstruction()
          || (hasCatchHandlers && i.instructionInstanceCanThrow());
    });
    insertAt.previous();
    insertAt.add(instruction);
  }

  private short[] computeArrayFilledData(
      NewArrayEmpty newArray, int size, BasicBlock block, int elementSize) {
    ConstNumber[] values = computeConstantArrayValues(newArray, block, size);
    if (values == null) {
      return null;
    }
    if (elementSize == 1) {
      short[] result = new short[(size + 1) / 2];
      for (int i = 0; i < size; i += 2) {
        assert values[i].getIntValue() <= Constants.S8BIT_MAX
            && values[i].getIntValue() >= Constants.S8BIT_MIN;
        short value = (short) (values[i].getIntValue() & 0xFF);
        if (i + 1 < size) {
          value |= (short) ((values[i + 1].getIntValue() & 0xFF) << 8);
        }
        result[i / 2] = value;
      }
      return result;
    }
    assert elementSize == 2 || elementSize == 4 || elementSize == 8;
    int shortsPerConstant = elementSize / 2;
    short[] result = new short[size * shortsPerConstant];
    for (int i = 0; i < size; i++) {
      long value = values[i].getRawValue();
      for (int part = 0; part < shortsPerConstant; part++) {
        result[i * shortsPerConstant + part] = (short) ((value >> (16 * part)) & 0xFFFFL);
      }
    }
    return result;
  }

  private ConstNumber[] computeConstantArrayValues(
      NewArrayEmpty newArray, BasicBlock block, int size) {
    if (size > MAX_FILL_ARRAY_SIZE) {
      return null;
    }
    ConstNumber[] values = new ConstNumber[size];
    int remaining = size;
    Set<Instruction> users = newArray.outValue().uniqueUsers();
    // We allow the array instantiations to cross block boundaries as long as it hasn't encountered
    // an instruction instance that can throw an exception.
    InstructionListIterator it = block.listIterator();
    it.nextUntil(i -> i == newArray);
    do {
      while (it.hasNext()) {
        Instruction instruction = it.next();
        // If we encounter an instruction that can throw an exception we need to bail out of the
        // optimization so that we do not transform half-initialized arrays into fully initialized
        // arrays on exceptional edges.
        if (instruction.instructionInstanceCanThrow()) {
          return null;
        }
        if (!users.contains(instruction)) {
          continue;
        }
        // If the initialization sequence is broken by another use we cannot use a
        // fill-array-data instruction.
        if (!instruction.isArrayPut()) {
          return null;
        }
        ArrayPut arrayPut = instruction.asArrayPut();
        if (!arrayPut.source().isConstant()) {
          return null;
        }
        assert arrayPut.index().isConstant();
        int index = arrayPut.index().getConstInstruction().asConstNumber().getIntValue();
        assert index >= 0 && index < values.length;
        if (values[index] != null) {
          return null;
        }
        ConstNumber value = arrayPut.source().getConstInstruction().asConstNumber();
        values[index] = value;
        --remaining;
        if (remaining == 0) {
          return values;
        }
      }
      block = block.exit().isGoto() ? block.exit().asGoto().getTarget() : null;
      it = block != null ? block.listIterator() : null;
    } while (it != null);
    return null;
  }

  private boolean isPrimitiveNewArrayWithConstantPositiveSize(Instruction instruction) {
    if (!(instruction instanceof NewArrayEmpty)) {
      return false;
    }
    NewArrayEmpty newArray = instruction.asNewArrayEmpty();
    if (!newArray.size().isConstant()) {
      return false;
    }
    int size = newArray.size().getConstInstruction().asConstNumber().getIntValue();
    if (size < 1) {
      return false;
    }
    if (!newArray.type.isPrimitiveArrayType()) {
      return false;
    }
    return true;
  }

  /**
   * Replace NewArrayEmpty followed by stores of constants to all entries with NewArrayEmpty
   * and FillArrayData.
   */
  public void simplifyArrayConstruction(IRCode code) {
    for (BasicBlock block : code.blocks) {
      // Map from the array value to the number of array put instruction to remove for that value.
      Map<Value, Integer> storesToRemoveForArray = new HashMap<>();
      // First pass: identify candidates and insert fill array data instruction.
      InstructionListIterator it = block.listIterator();
      while (it.hasNext()) {
        Instruction instruction = it.next();
        if (!isPrimitiveNewArrayWithConstantPositiveSize(instruction)) {
          continue;
        }
        NewArrayEmpty newArray = instruction.asNewArrayEmpty();
        int size = newArray.size().getConstInstruction().asConstNumber().getIntValue();
        // If there is only one element it is typically smaller to generate the array put
        // instruction instead of fill array data.
        if (size == 1) {
          continue;
        }
        int elementSize = newArray.type.elementSizeForPrimitiveArrayType();
        short[] contents = computeArrayFilledData(newArray, size, block, elementSize);
        if (contents == null) {
          continue;
        }
        storesToRemoveForArray.put(newArray.outValue(), size);
        int arraySize = newArray.size().getConstInstruction().asConstNumber().getIntValue();
        NewArrayFilledData fillArray = new NewArrayFilledData(
            newArray.outValue(), elementSize, arraySize, contents);
        it.add(fillArray);
      }
      // Second pass: remove all the array put instructions for the array for which we have
      // inserted a fill array data instruction instead.
      if (!storesToRemoveForArray.isEmpty()) {
        do {
          it = block.listIterator();
          while (it.hasNext()) {
            Instruction instruction = it.next();
            if (instruction.isArrayPut()) {
              Value array = instruction.asArrayPut().array();
              Integer toRemoveCount = storesToRemoveForArray.get(array);
              if (toRemoveCount != null && toRemoveCount > 0) {
                storesToRemoveForArray.put(array, toRemoveCount - 1);
                it.remove();
              }
            }
          }
          block = block.exit().isGoto() ? block.exit().asGoto().getTarget() : null;
        } while (block != null);
      }
    }
  }

  private class ExpressionEquivalence extends Equivalence<Instruction> {

    @Override
    protected boolean doEquivalent(Instruction a, Instruction b) {
      if (a.getClass() != b.getClass() || !a.identicalNonValueParts(b)) {
        return false;
      }
      // For commutative binary operations any order of in-values are equal.
      if (a.isBinop() && a.asBinop().isCommutative()) {
        Value a0 = a.inValues().get(0);
        Value a1 = a.inValues().get(1);
        Value b0 = b.inValues().get(0);
        Value b1 = b.inValues().get(1);
        return (a0.equals(b0) && a1.equals(b1)) || (a0.equals(b1) && a1.equals(b0));
      } else {
        // Compare all in-values.
        assert a.inValues().size() == b.inValues().size();
        for (int i = 0; i < a.inValues().size(); i++) {
          if (!a.inValues().get(i).equals(b.inValues().get(i))) {
            return false;
          }
        }
        return true;
      }
    }

    @Override
    protected int doHash(Instruction instruction) {
      final int prime = 29;
      int hash = instruction.getClass().hashCode();
      if (instruction.isBinop()) {
        Binop binop = instruction.asBinop();
        Value in0 = instruction.inValues().get(0);
        Value in1 = instruction.inValues().get(1);
        if (binop.isCommutative()) {
          hash += hash * prime + in0.hashCode() * in1.hashCode();
        } else {
          hash += hash * prime + in0.hashCode();
          hash += hash * prime + in1.hashCode();
        }
        return hash;
      } else {
        for (Value value : instruction.inValues()) {
          hash += hash * prime + value.hashCode();
        }
      }
      return hash;
    }
  }

  private boolean shareCatchHandlers(Instruction i0, Instruction i1) {
    if (!i0.instructionTypeCanThrow()) {
      assert !i1.instructionTypeCanThrow();
      return true;
    }
    assert i1.instructionTypeCanThrow();
    // TODO(sgjesse): This could be even better by checking for the exceptions thrown, e.g. div
    // and rem only ever throw ArithmeticException.
    CatchHandlers<BasicBlock> ch0 = i0.getBlock().getCatchHandlers();
    CatchHandlers<BasicBlock> ch1 = i1.getBlock().getCatchHandlers();
    return ch0.equals(ch1);
  }

  public void commonSubexpressionElimination(IRCode code) {
    final ListMultimap<Wrapper<Instruction>, Value> instructionToValue = ArrayListMultimap.create();
    final DominatorTree dominatorTree = new DominatorTree(code);
    final ExpressionEquivalence equivalence = new ExpressionEquivalence();

    for (int i = 0; i < dominatorTree.getSortedBlocks().length; i++) {
      BasicBlock block = dominatorTree.getSortedBlocks()[i];
      Iterator<Instruction> iterator = block.iterator();
      while (iterator.hasNext()) {
        Instruction instruction = iterator.next();
        if (instruction.isBinop()
            || instruction.isUnop()
            || instruction.isInstanceOf()
            || instruction.isCheckCast()) {
          List<Value> candidates = instructionToValue.get(equivalence.wrap(instruction));
          boolean eliminated = false;
          if (candidates.size() > 0) {
            for (Value candidate : candidates) {
              if (dominatorTree.dominatedBy(block, candidate.definition.getBlock()) &&
                  shareCatchHandlers(instruction, candidate.definition)) {
                instruction.outValue().replaceUsers(candidate);
                eliminated = true;
                iterator.remove();
                break;  // Don't try any more candidates.
              }
            }
          }
          if (!eliminated) {
            instructionToValue.put(equivalence.wrap(instruction), instruction.outValue());
          }
        }
      }
    }
    assert code.isConsistentSSA();
  }

  public void simplifyIf(IRCode code) {
    DominatorTree dominator = new DominatorTree(code);
    code.clearMarks();
    for (BasicBlock block : code.blocks) {
      if (block.isMarked()) {
        continue;
      }
      JumpInstruction exit = block.exit();
      if (exit.isIf()) {
        If theIf = exit.asIf();
        List<Value> inValues = theIf.inValues();
        int cond;
        if (inValues.get(0).isConstant()
            && (theIf.isZeroTest() || inValues.get(1).isConstant())) {
          // Zero test with a constant of comparison between between two constants.
          if (theIf.isZeroTest()) {
            cond = inValues.get(0).getConstInstruction().asConstNumber().getIntValue();
          } else {
            int left = inValues.get(0).getConstInstruction().asConstNumber().getIntValue();
            int right = inValues.get(1).getConstInstruction().asConstNumber().getIntValue();
            cond = left - right;
          }
        } else if (inValues.get(0).hasValueRange()
            && (theIf.isZeroTest() || inValues.get(1).hasValueRange())) {
          // Zero test with a value range, or comparison between between two values,
          // each with a value ranges.
          if (theIf.isZeroTest()) {
            if (inValues.get(0).isValueInRange(0)) {
              // Zero in in the range - can't determine the comparison.
              continue;
            }
            cond = Long.signum(inValues.get(0).getValueRange().getMin());
          } else {
            LongInterval leftRange = inValues.get(0).getValueRange();
            LongInterval rightRange = inValues.get(1).getValueRange();
            if (leftRange.overlapsWith(rightRange)) {
              // Ranges overlap - can't determine the comparison.
              continue;
            }
            // There is no overlap.
            cond = Long.signum(leftRange.getMin() - rightRange.getMin());
          }
        } else {
          continue;
        }
        BasicBlock target = theIf.targetFromCondition(cond);
        BasicBlock deadTarget =
            target == theIf.getTrueTarget() ? theIf.fallthroughBlock() : theIf.getTrueTarget();
        List<BasicBlock> removedBlocks = block.unlink(deadTarget, dominator);
        for (BasicBlock removedBlock : removedBlocks) {
          if (!removedBlock.isMarked()) {
            removedBlock.mark();
          }
        }
        assert theIf == block.exit();
        InstructionListIterator iterator = block.listIterator(block.getInstructions().size());
        iterator.previous();
        iterator.replaceCurrentInstruction(new Goto());
        assert block.exit().isGoto();
        assert block.exit().asGoto().getTarget() == target;
      }
    }
    code.removeMarkedBlocks();
    assert code.isConsistentSSA();
  }

  public void removeUnneededCatchHandlers(IRCode code) {
    DominatorTree dominator = new DominatorTree(code);
    code.clearMarks();
    for (BasicBlock block : code.blocks) {
      if (block.hasCatchHandlers() && !block.canThrow()) {
        CatchHandlers<BasicBlock> handlers = block.getCatchHandlers();
        for (BasicBlock target : handlers.getUniqueTargets()) {
          for (BasicBlock unlinked : block.unlink(target, dominator)) {
            if (!unlinked.isMarked()) {
              unlinked.mark();
            }
          }
        }
      }
    }
    code.removeMarkedBlocks();
    assert code.isConsistentSSA();
  }

  public void rewriteLongCompareAndRequireNonNull(IRCode code, boolean canUseObjectsNonNull) {
    InstructionIterator iterator = code.instructionIterator();

    while (iterator.hasNext()) {
      Instruction current = iterator.next();
      if (current.isInvokeMethod()) {
        DexMethod invokedMethod = current.asInvokeMethod().getInvokedMethod();
        if (invokedMethod == dexItemFactory.longMethods.compare) {
          List<Value> inValues = current.inValues();
          assert inValues.size() == 2;
          iterator.replaceCurrentInstruction(
              new Cmp(NumericType.LONG, Bias.NONE, current.outValue(), inValues.get(0),
                  inValues.get(1)));
        } else if (!canUseObjectsNonNull
            && invokedMethod == dexItemFactory.objectsMethods.requireNonNull) {
          // Rewrite calls to Objects.requireNonNull(Object) because Javac 9 start to use it for
          // synthesized null checks.
          InvokeVirtual callToGetClass = new InvokeVirtual(dexItemFactory.objectMethods.getClass,
              null, current.inValues());
          if (current.outValue() != null) {
            current.outValue().replaceUsers(current.inValues().get(0));
            current.setOutValue(null);
          }
          iterator.replaceCurrentInstruction(callToGetClass);
        }
      }
    }
    assert code.isConsistentSSA();
  }

  // Removes calls to Throwable.addSuppressed(Throwable) and rewrites
  // Throwable.getSuppressed() into new Throwable[0].
  //
  // Note that addSuppressed() and getSuppressed() methods are final in
  // Throwable, so these changes don't have to worry about overrides.
  public void rewriteThrowableAddAndGetSuppressed(IRCode code) {
    boolean removeUnneededCatchHandlers = false;
    DexItemFactory.ThrowableMethods throwableMethods = dexItemFactory.throwableMethods;

    for (BasicBlock block : code.blocks) {
      InstructionListIterator iterator = block.listIterator();
      while (iterator.hasNext()) {
        Instruction current = iterator.next();
        if (current.isInvokeMethod()) {
          DexMethod invokedMethod = current.asInvokeMethod().getInvokedMethod();

          if (matchesMethodOfThrowable(invokedMethod, throwableMethods.addSuppressed)) {
            // Remove Throwable::addSuppressed(Throwable) call.
            iterator.remove();
            removeUnneededCatchHandlers = true;

          } else if (matchesMethodOfThrowable(invokedMethod, throwableMethods.getSuppressed)) {
            Value destValue = current.outValue();
            if (destValue == null) {
              // If the result of the call was not used we don't create
              // an empty array and just remove the call.
              iterator.remove();
              removeUnneededCatchHandlers = true;
              continue;
            }

            // Replace call to Throwable::getSuppressed() with new Throwable[0].

            // First insert the constant value *before* the current instruction.
            Value zero = code.createValue(MoveType.SINGLE);
            assert iterator.hasPrevious();
            iterator.previous();
            iterator.add(new ConstNumber(ConstType.INT, zero, 0));

            // Then replace the invoke instruction with NewArrayEmpty instruction.
            Instruction next = iterator.next();
            assert current == next;
            NewArrayEmpty newArray = new NewArrayEmpty(destValue, zero,
                dexItemFactory.createType(dexItemFactory.throwableArrayDescriptor));
            iterator.replaceCurrentInstruction(newArray);

            // NOTE: nothing needs to be changed in catch handlers since we replace
            //       one throwable instruction with another.
          }
        }
      }
    }

    // If at least one addSuppressed(...) call was removed, or we were able
    // to remove getSuppressed() call without replacing it with a new empty array,
    // we need to deal with possible unreachable catch handlers.
    if (removeUnneededCatchHandlers) {
      removeUnneededCatchHandlers(code);
    }

    assert code.isConsistentSSA();
  }

  private boolean matchesMethodOfThrowable(DexMethod invoked, DexMethod expected) {
    return invoked.name == expected.name
        && invoked.proto == expected.proto
        && isSubtypeOfThrowable(invoked.holder);
  }

  private boolean isSubtypeOfThrowable(DexType type) {
    while (type != null && type != dexItemFactory.objectType) {
      if (type == dexItemFactory.throwableType) {
        return true;
      }
      DexClass dexClass = appInfo.definitionFor(type);
      if (dexClass == null) {
        throw new CompilationError("Class or interface " + type.toSourceString() +
            " required for desugaring of try-with-resources is not found.");
      }
      type = dexClass.superType;
    }
    return false;
  }
}
