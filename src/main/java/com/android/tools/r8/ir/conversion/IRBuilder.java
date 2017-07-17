// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.conversion;

import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.errors.InternalCompilerError;
import com.android.tools.r8.graph.DebugLocalInfo;
import com.android.tools.r8.graph.DexCallSite;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItem;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexMethodHandle;
import com.android.tools.r8.graph.DexProto;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.Add;
import com.android.tools.r8.ir.code.And;
import com.android.tools.r8.ir.code.Argument;
import com.android.tools.r8.ir.code.ArrayGet;
import com.android.tools.r8.ir.code.ArrayLength;
import com.android.tools.r8.ir.code.ArrayPut;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.BasicBlock.EdgeType;
import com.android.tools.r8.ir.code.BasicBlock.ThrowingInfo;
import com.android.tools.r8.ir.code.CatchHandlers;
import com.android.tools.r8.ir.code.CheckCast;
import com.android.tools.r8.ir.code.Cmp;
import com.android.tools.r8.ir.code.Cmp.Bias;
import com.android.tools.r8.ir.code.ConstClass;
import com.android.tools.r8.ir.code.ConstNumber;
import com.android.tools.r8.ir.code.ConstString;
import com.android.tools.r8.ir.code.ConstType;
import com.android.tools.r8.ir.code.DebugLocalUninitialized;
import com.android.tools.r8.ir.code.DebugLocalWrite;
import com.android.tools.r8.ir.code.DebugPosition;
import com.android.tools.r8.ir.code.Div;
import com.android.tools.r8.ir.code.Goto;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.If;
import com.android.tools.r8.ir.code.InstanceGet;
import com.android.tools.r8.ir.code.InstanceOf;
import com.android.tools.r8.ir.code.InstancePut;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.Invoke;
import com.android.tools.r8.ir.code.Invoke.Type;
import com.android.tools.r8.ir.code.InvokeCustom;
import com.android.tools.r8.ir.code.MemberType;
import com.android.tools.r8.ir.code.Monitor;
import com.android.tools.r8.ir.code.MoveException;
import com.android.tools.r8.ir.code.MoveType;
import com.android.tools.r8.ir.code.Mul;
import com.android.tools.r8.ir.code.Neg;
import com.android.tools.r8.ir.code.NewArrayEmpty;
import com.android.tools.r8.ir.code.NewArrayFilledData;
import com.android.tools.r8.ir.code.NewInstance;
import com.android.tools.r8.ir.code.Not;
import com.android.tools.r8.ir.code.NumberConversion;
import com.android.tools.r8.ir.code.NumericType;
import com.android.tools.r8.ir.code.Or;
import com.android.tools.r8.ir.code.Phi;
import com.android.tools.r8.ir.code.Rem;
import com.android.tools.r8.ir.code.Return;
import com.android.tools.r8.ir.code.Shl;
import com.android.tools.r8.ir.code.Shr;
import com.android.tools.r8.ir.code.StaticGet;
import com.android.tools.r8.ir.code.StaticPut;
import com.android.tools.r8.ir.code.Sub;
import com.android.tools.r8.ir.code.Switch;
import com.android.tools.r8.ir.code.Throw;
import com.android.tools.r8.ir.code.Ushr;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.code.Value.DebugInfo;
import com.android.tools.r8.ir.code.ValueNumberGenerator;
import com.android.tools.r8.ir.code.Xor;
import com.android.tools.r8.utils.InternalOptions;
import it.unimi.dsi.fastutil.ints.Int2ReferenceAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceSortedMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Builder object for constructing high-level IR from dex bytecode.
 *
 * <p>The generated IR is in SSA form. The SSA construction is based on the paper
 * "Simple and Efficient Construction of Static Single Assignment Form" available at
 * http://compilers.cs.uni-saarland.de/papers/bbhlmz13cc.pdf
 */
public class IRBuilder {

  public static final int INITIAL_BLOCK_OFFSET = -1;

  // SSA construction uses a worklist of basic blocks reachable from the entry and their
  // instruction offsets.
  private static class WorklistItem {

    private final BasicBlock block;
    private final int firstInstructionIndex;

    private WorklistItem(BasicBlock block, int firstInstructionIndex) {
      assert block != null;
      this.block = block;
      this.firstInstructionIndex = firstInstructionIndex;
    }
  }

  /**
   * Representation of lists of values that can be used as keys in maps. A list of
   * values is equal to another list of values if it contains exactly the same values
   * in the same order.
   */
  private static class ValueList {

    private List<Value> values = new ArrayList<>();

    /**
     * Creates a ValueList of all the operands at the given index in the list of phis.
     */
    public static ValueList fromPhis(List<Phi> phis, int index) {
      ValueList result = new ValueList();
      for (Phi phi : phis) {
        result.values.add(phi.getOperand(index));
      }
      return result;
    }

    @Override
    public int hashCode() {
      return values.hashCode();
    }

    @Override
    public boolean equals(Object other) {
      if (!(other instanceof ValueList)) {
        return false;
      }
      ValueList o = (ValueList) other;
      if (o.values.size() != values.size()) {
        return false;
      }
      for (int i = 0; i < values.size(); i++) {
        if (values.get(i) != o.values.get(i)) {
          return false;
        }
      }
      return true;
    }
  }

  public static class BlockInfo {
    BasicBlock block = new BasicBlock();
    IntSet normalPredecessors = new IntArraySet();
    IntSet normalSuccessors = new IntArraySet();
    IntSet exceptionalPredecessors = new IntArraySet();
    IntSet exceptionalSuccessors = new IntArraySet();

    void addNormalPredecessor(int offset) {
      normalPredecessors.add(offset);
    }

    void addNormalSuccessor(int offset) {
      normalSuccessors.add(offset);
    }

    void replaceNormalPredecessor(int existing, int replacement) {
      normalPredecessors.remove(existing);
      normalPredecessors.add(replacement);
    }

    void addExceptionalPredecessor(int offset) {
      exceptionalPredecessors.add(offset);
    }

    void addExceptionalSuccessor(int offset) {
      exceptionalSuccessors.add(offset);
    }

    int predecessorCount() {
      return normalPredecessors.size() + exceptionalPredecessors.size();
    }

    BlockInfo split(
        int blockStartOffset, int fallthroughOffset, Int2ReferenceMap<BlockInfo> targets) {
      BlockInfo fallthroughInfo = new BlockInfo();
      fallthroughInfo.normalPredecessors = new IntArraySet(Collections.singleton(blockStartOffset));
      fallthroughInfo.block.incrementUnfilledPredecessorCount();
      // Move all normal successors to the fallthrough block.
      IntIterator normalSuccessorIterator = normalSuccessors.iterator();
      while (normalSuccessorIterator.hasNext()) {
        BlockInfo normalSuccessor = targets.get(normalSuccessorIterator.nextInt());
        normalSuccessor.replaceNormalPredecessor(blockStartOffset, fallthroughOffset);
      }
      fallthroughInfo.normalSuccessors = normalSuccessors;
      normalSuccessors = new IntArraySet(Collections.singleton(fallthroughOffset));
      // Copy all exceptional successors to the fallthrough block.
      IntIterator exceptionalSuccessorIterator = fallthroughInfo.exceptionalSuccessors.iterator();
      while (exceptionalSuccessorIterator.hasNext()) {
        BlockInfo exceptionalSuccessor = targets.get(exceptionalSuccessorIterator.nextInt());
        exceptionalSuccessor.addExceptionalPredecessor(fallthroughOffset);
      }
      fallthroughInfo.exceptionalSuccessors = new IntArraySet(this.exceptionalSuccessors);
      return fallthroughInfo;
    }
  }

  // Mapping from instruction offsets to basic-block targets.
  private final Int2ReferenceSortedMap<BlockInfo> targets = new Int2ReferenceAVLTreeMap<>();

  // Worklist of reachable blocks.
  private final Queue<Integer> traceBlocksWorklist = new LinkedList<>();

  // Bitmap to ensure we don't process an instruction more than once.
  private boolean[] processedInstructions = null;

  // Bitmap of processed subroutine instructions. Lazily allocated off the fast-path.
  private Set<Integer> processedSubroutineInstructions = null;

  // Worklist for SSA construction.
  private final Queue<WorklistItem> ssaWorklist = new LinkedList<>();

  // Basic blocks. Added after processing from the worklist.
  private LinkedList<BasicBlock> blocks = new LinkedList<>();

  private BasicBlock currentBlock = null;

  // Mappings for canonicalizing constants of a given type at IR construction time.
  private Map<Long, ConstNumber> intConstants = new HashMap<>();
  private Map<Long, ConstNumber> longConstants = new HashMap<>();
  private Map<Long, ConstNumber> floatConstants = new HashMap<>();
  private Map<Long, ConstNumber> doubleConstants = new HashMap<>();
  private Map<Long, ConstNumber> nullConstants = new HashMap<>();

  private List<BasicBlock> exitBlocks = new ArrayList<>();
  private BasicBlock normalExitBlock;

  private List<BasicBlock.Pair> needGotoToCatchBlocks = new ArrayList<>();

  final private ValueNumberGenerator valueNumberGenerator;

  private DebugPosition currentDebugPosition = null;

  private DexEncodedMethod method;

  // Source code to build IR from. Null if already built.
  private SourceCode source;

  boolean throwingInstructionInCurrentBlock = false;

  private final InternalOptions options;

  // Pending local changes.
  private List<Value> debugLocalStarts = new ArrayList<>();
  private List<Value> debugLocalReads = new ArrayList<>();
  private List<Value> debugLocalEnds = new ArrayList<>();

  private int nextBlockNumber = 0;

  public IRBuilder(DexEncodedMethod method, SourceCode source, InternalOptions options) {
    this(method, source, new ValueNumberGenerator(), options);
  }

  public IRBuilder(
      DexEncodedMethod method,
      SourceCode source,
      ValueNumberGenerator valueNumberGenerator,
      InternalOptions options) {
    assert source != null;
    this.method = method;
    this.source = source;
    this.valueNumberGenerator = valueNumberGenerator;
    this.options = options;
  }

  public Int2ReferenceSortedMap<BlockInfo> getCFG() {
    return targets;
  }

  private void addToWorklist(BasicBlock block, int firstInstructionIndex) {
    // TODO(ager): Filter out the ones that are already in the worklist, mark bit in block?
    if (!block.isFilled()) {
      ssaWorklist.add(new WorklistItem(block, firstInstructionIndex));
    }
  }

  private void setCurrentBlock(BasicBlock block) {
    currentBlock = block;
  }

  /**
   * Build the high-level IR in SSA form.
   *
   * @return The list of basic blocks. First block is the main entry.
   */
  public IRCode build() {
    assert source != null;
    source.setUp();

    // Create entry block (at a non-targetable address).
    targets.put(INITIAL_BLOCK_OFFSET, new BlockInfo());

    // Process reachable code paths starting from instruction 0.
    processedInstructions = new boolean[source.instructionCount()];
    traceBlocksWorklist.add(0);
    while (!traceBlocksWorklist.isEmpty()) {
      int startOfBlockOffset = traceBlocksWorklist.remove();
      int startOfBlockIndex = source.instructionIndex(startOfBlockOffset);
      // Check that the block has not been processed after being added.
      if (isIndexProcessed(startOfBlockIndex)) {
        continue;
      }
      // Process each instruction until the block is closed.
      for (int index = startOfBlockIndex; index < source.instructionCount(); ++index) {
        markIndexProcessed(index);
        int closedAt = source.traceInstruction(index, this);
        if (closedAt != -1) {
          if (closedAt + 1 < source.instructionCount()) {
            ensureBlockWithoutEnqueuing(source.instructionOffset(closedAt + 1));
          }
          break;
        }
        // If the next instruction starts a block, fall through to it.
        if (index + 1 < source.instructionCount()) {
          int nextOffset = source.instructionOffset(index + 1);
          if (targets.get(nextOffset) != null) {
            ensureNormalSuccessorBlock(startOfBlockOffset, nextOffset);
            break;
          }
        }
      }
    }
    processedInstructions = null;

    setCurrentBlock(targets.get(INITIAL_BLOCK_OFFSET).block);
    source.buildPrelude(this);

    // Process normal blocks reachable from the entry block using a worklist of reachable
    // blocks.
    addToWorklist(currentBlock, 0);
    processWorklist();

    // Check that the last block is closed and does not fall off the end.
    assert currentBlock == null;

    // Handle where a catch handler hits the same block as the fallthrough.
    handleFallthroughToCatchBlock();

    // Verify that we have properly filled all blocks
    // Must be after handle-catch (which has delayed edges),
    // but before handle-exit (which does not maintain predecessor counts).
    assert verifyFilledPredecessors();

    // If there are multiple returns create an exit block.
    handleExitBlock();

    // Clear all reaching definitions to free up memory (and avoid invalid use).
    for (BasicBlock block : blocks) {
      block.clearCurrentDefinitions();
    }

    // Join predecessors for which all phis have the same inputs. This avoids generating the
    // same phi moves in multiple blocks.
    joinPredecessorsWithIdenticalPhis();

    // Split critical edges to make sure that we have a place to insert phi moves if
    // necessary.
    splitCriticalEdges();

    // Package up the IR code.
    IRCode ir = new IRCode(method, blocks, normalExitBlock, valueNumberGenerator);

    // Create block order and make sure that all blocks are immediately followed by their
    // fallthrough block if any.
    traceBlocks(ir);

    // Clear the code so we don't build multiple times.
    source.clear();
    clearCanonicalizationMaps();
    source = null;

    assert ir.isConsistentSSA();
    return ir;
  }

  private void clearCanonicalizationMaps() {
    intConstants = null;
    longConstants = null;
    floatConstants = null;
    doubleConstants = null;
    nullConstants = null;
  }

  private boolean verifyFilledPredecessors() {
    for (BasicBlock block : blocks) {
      assert verifyFilledPredecessors(block);
    }
    return true;
  }

  private boolean verifyFilledPredecessors(BasicBlock block) {
    assert block.verifyFilledPredecessors();
    // TODO(zerny): Consider moving the validation of the initial control-flow graph to after its
    // construction and prior to building the IR.
    for (BlockInfo info : targets.values()) {
      if (info != null && info.block == block) {
        assert info.predecessorCount() == block.getPredecessors().size();
        assert info.normalSuccessors.size() == block.getNormalSucessors().size();
        if (block.hasCatchHandlers()) {
          assert info.exceptionalSuccessors.size()
              == block.getCatchHandlers().getUniqueTargets().size();
        } else {
          assert !block.canThrow()
              || info.exceptionalSuccessors.isEmpty()
              || (info.exceptionalSuccessors.size() == 1
                  && info.exceptionalSuccessors.iterator().nextInt() < 0);
        }
        return true;
      }
    }
    // There are places where we add in new blocks that we do not represent in the initial CFG.
    // TODO(zerny): Should we maintain the initial CFG after instruction building?
    return true;
  }

  private void processWorklist() {
    for (WorklistItem item = ssaWorklist.poll(); item != null; item = ssaWorklist.poll()) {
      if (item.block.isFilled()) {
        continue;
      }
      setCurrentBlock(item.block);
      blocks.add(currentBlock);
      currentBlock.setNumber(nextBlockNumber++);
      // Build IR for each dex instruction in the block.
      for (int i = item.firstInstructionIndex; i < source.instructionCount(); ++i) {
        if (currentBlock == null) {
          source.closedCurrentBlock();
          break;
        }
        BlockInfo info = targets.get(source.instructionOffset(i));
        if (info != null && info.block != currentBlock) {
          closeCurrentBlockWithFallThrough(info.block);
          source.closedCurrentBlockWithFallthrough(i);
          addToWorklist(info.block, i);
          break;
        }
        source.buildInstruction(this, i);
      }
    }
  }

  // Helper to resolve switch payloads and build switch instructions (dex code only).
  public void resolveAndBuildSwitch(int value, int fallthroughOffset, int payloadOffset) {
    source.resolveAndBuildSwitch(value, fallthroughOffset, payloadOffset, this);
  }

  // Helper to resolve fill-array data and build new-array instructions (dex code only).
  public void resolveAndBuildNewArrayFilledData(int arrayRef, int payloadOffset) {
    source.resolveAndBuildNewArrayFilledData(arrayRef, payloadOffset, this);
  }

  /**
   * Add an (non-jump) instruction to the builder.
   *
   * @param ir IR instruction to add as the next instruction.
   */
  public void add(Instruction ir) {
    assert !ir.isJumpInstruction();
    addInstruction(ir);
  }

  public void addThisArgument(int register) {
    DebugLocalInfo local = getCurrentLocal(register);
    DebugInfo info = local == null ? null : new DebugInfo(local, null);
    Value value = writeRegister(register, MoveType.OBJECT, ThrowingInfo.NO_THROW, info);
    addInstruction(new Argument(value));
    value.markAsThis();
  }

  public void addNonThisArgument(int register, MoveType moveType) {
    DebugLocalInfo local = getCurrentLocal(register);
    DebugInfo info = local == null ? null : new DebugInfo(local, null);
    Value value = writeRegister(register, moveType, ThrowingInfo.NO_THROW, info);
    addInstruction(new Argument(value));
  }

  public void addDebugUninitialized(int register, ConstType type) {
    if (!options.debug) {
      return;
    }
    Value value = writeRegister(register, MoveType.fromConstType(type), ThrowingInfo.NO_THROW,
        null);
    assert value.getLocalInfo() == null;
    addInstruction(new DebugLocalUninitialized(type, value));
  }

  public void addDebugLocalStart(int register, DebugLocalInfo local) {
    if (!options.debug) {
      return;
    }
    assert local != null;
    assert local == getCurrentLocal(register);
    MoveType moveType = MoveType.fromDexType(local.type);
    Value in = readRegisterIgnoreLocal(register, moveType);
    if (in.isPhi() || in.getLocalInfo() != local) {
      // We cannot shortcut if the local is defined by a phi as it could end up being trivial.
      addDebugLocalWrite(moveType, register, in);
    } else {
      Value value = getLocalValue(register, local);
      if (value != null) {
        debugLocalStarts.add(value);
      }
    }
  }

  private void addDebugLocalWrite(MoveType type, int dest, Value in) {
    Value out = writeRegister(dest, type, ThrowingInfo.NO_THROW);
    DebugLocalWrite write = new DebugLocalWrite(out, in);
    assert !write.instructionTypeCanThrow();
    addInstruction(write);
  }

  private Value getLocalValue(int register, DebugLocalInfo local) {
    assert local != null;
    assert local == getCurrentLocal(register);
    MoveType moveType = MoveType.fromDexType(local.type);
    // Invalid debug-info may cause attempt to read a local that is not actually alive.
    // See b/37722432 and regression test {@code jasmin.InvalidDebugInfoTests::testInvalidInfoThrow}
    Value in = readRegisterIgnoreLocal(register, moveType);
    if (in.isUninitializedLocal() || in.getLocalInfo() != local) {
      return null;
    }
    assert in.getLocalInfo() == local;
    return in;
  }

  public void addDebugLocalRead(int register, DebugLocalInfo local) {
    if (!options.debug) {
      return;
    }
    Value value = getLocalValue(register, local);
    if (value != null) {
      debugLocalReads.add(value);
    }
  }

  public void addDebugLocalEnd(int register, DebugLocalInfo local) {
    if (!options.debug) {
      return;
    }
    Value value = getLocalValue(register, local);
    if (value != null) {
      debugLocalEnds.add(value);
    }
  }

  public void addAdd(NumericType type, int dest, int left, int right) {
    Value in1 = readNumericRegister(left, type);
    Value in2 = readNumericRegister(right, type);
    Value out = writeNumericRegister(dest, type, ThrowingInfo.NO_THROW);
    Add instruction = new Add(type, out, in1, in2);
    assert !instruction.instructionTypeCanThrow();
    addInstruction(instruction);
  }

  public void addAddLiteral(NumericType type, int dest, int value, int constant) {
    assert isNonLongIntegerType(type);
    Value in1 = readNumericRegister(value, type);
    Value in2 = readLiteral(type, constant);
    Value out = writeNumericRegister(dest, type, ThrowingInfo.NO_THROW);
    Add instruction = new Add(type, out, in1, in2);
    assert !instruction.instructionTypeCanThrow();
    addInstruction(instruction);
  }

  public void addAnd(NumericType type, int dest, int left, int right) {
    assert isIntegerType(type);
    Value in1 = readNumericRegister(left, type);
    Value in2 = readNumericRegister(right, type);
    Value out = writeNumericRegister(dest, type, ThrowingInfo.NO_THROW);
    And instruction = new And(type, out, in1, in2);
    assert !instruction.instructionTypeCanThrow();
    addInstruction(instruction);
  }

  public void addAndLiteral(NumericType type, int dest, int value, int constant) {
    assert isNonLongIntegerType(type);
    Value in1 = readNumericRegister(value, type);
    Value in2 = readLiteral(type, constant);
    Value out = writeNumericRegister(dest, type, ThrowingInfo.NO_THROW);
    And instruction = new And(type, out, in1, in2);
    assert !instruction.instructionTypeCanThrow();
    addInstruction(instruction);
  }

  public void addArrayGet(MemberType type, int dest, int array, int index) {
    Value in1 = readRegister(array, MoveType.OBJECT);
    Value in2 = readRegister(index, MoveType.SINGLE);
    Value out = writeRegister(dest, MoveType.fromMemberType(type), ThrowingInfo.CAN_THROW);
    ArrayGet instruction = new ArrayGet(type, out, in1, in2);
    assert instruction.instructionTypeCanThrow();
    add(instruction);
  }

  public void addArrayLength(int dest, int array) {
    Value in = readRegister(array, MoveType.OBJECT);
    Value out = writeRegister(dest, MoveType.SINGLE, ThrowingInfo.CAN_THROW);
    ArrayLength instruction = new ArrayLength(out, in);
    assert instruction.instructionTypeCanThrow();
    add(instruction);
  }

  public void addArrayPut(MemberType type, int value, int array, int index) {
    List<Value> ins = new ArrayList<>(3);
    ins.add(readRegister(value, MoveType.fromMemberType(type)));
    ins.add(readRegister(array, MoveType.OBJECT));
    ins.add(readRegister(index, MoveType.SINGLE));
    ArrayPut instruction = new ArrayPut(type, ins);
    add(instruction);
  }

  public void addCheckCast(int value, DexType type) {
    Value in = readRegister(value, MoveType.OBJECT);
    Value out = writeRegister(value, MoveType.OBJECT, ThrowingInfo.CAN_THROW);
    CheckCast instruction = new CheckCast(out, in, type);
    assert instruction.instructionTypeCanThrow();
    add(instruction);
  }

  public void addCmp(NumericType type, Bias bias, int dest, int left, int right) {
    Value in1 = readNumericRegister(left, type);
    Value in2 = readNumericRegister(right, type);
    Value out = writeRegister(dest, MoveType.SINGLE, ThrowingInfo.NO_THROW);
    Cmp instruction = new Cmp(type, bias, out, in1, in2);
    assert !instruction.instructionTypeCanThrow();
    add(instruction);
  }

  public void addConst(MoveType type, int dest, long value) {
    ConstNumber instruction;
    if (type == MoveType.SINGLE) {
      Value out = writeRegister(dest, type, ThrowingInfo.NO_THROW);
      instruction = new ConstNumber(ConstType.INT_OR_FLOAT, out, value);
    } else {
      assert type == MoveType.WIDE;
      Value out = writeRegister(dest, type, ThrowingInfo.NO_THROW);
      instruction = new ConstNumber(ConstType.LONG_OR_DOUBLE, out, value);
    }
    assert !instruction.instructionTypeCanThrow();
    add(instruction);
  }

  // TODO(ager): Does art support changing the value of locals  during debugging? If so, we need
  // to disable constant canonicalization in debug builds to make sure we have separate values
  // for separate locals.
  private void canonicalizeAndAddConst(
      ConstType type, int dest, long value, Map<Long, ConstNumber> table) {
    ConstNumber existing = table.get(value);
    if (existing != null) {
      currentBlock.writeCurrentDefinition(dest, existing.outValue(), ThrowingInfo.NO_THROW);
    } else {
      Value out = writeRegister(dest, MoveType.fromConstType(type), ThrowingInfo.NO_THROW);
      ConstNumber instruction = new ConstNumber(type, out, value);
      BasicBlock entryBlock = blocks.get(0);
      if (currentBlock != entryBlock) {
        // Insert the constant instruction at the start of the block right after the argument
        // instructions. It is important that the const instruction is put before any instruction
        // that can throw exceptions (since the value could be used on the exceptional edge).
        InstructionListIterator it = entryBlock.listIterator();
        while (it.hasNext()) {
          if (!it.next().isArgument()) {
            it.previous();
            break;
          }
        }
        it.add(instruction);
      } else {
        add(instruction);
      }
      table.put(value, instruction);
    }
  }

  public void addLongConst(int dest, long value) {
    canonicalizeAndAddConst(ConstType.LONG, dest, value, longConstants);
  }

  public void addDoubleConst(int dest, long value) {
    canonicalizeAndAddConst(ConstType.DOUBLE, dest, value, doubleConstants);
  }

  public void addIntConst(int dest, long value) {
    canonicalizeAndAddConst(ConstType.INT, dest, value, intConstants);
  }

  public void addFloatConst(int dest, long value) {
    canonicalizeAndAddConst(ConstType.FLOAT, dest, value, floatConstants);
  }

  public void addNullConst(int dest, long value) {
    canonicalizeAndAddConst(ConstType.INT, dest, value, nullConstants);
  }

  public void addConstClass(int dest, DexType type) {
    Value out = writeRegister(dest, MoveType.OBJECT, ThrowingInfo.CAN_THROW);
    ConstClass instruction = new ConstClass(out, type);
    assert instruction.instructionTypeCanThrow();
    add(instruction);
  }

  public void addConstString(int dest, DexString string) {
    Value out = writeRegister(dest, MoveType.OBJECT, ThrowingInfo.CAN_THROW);
    ConstString instruction = new ConstString(out, string);
    add(instruction);
  }

  public void addDiv(NumericType type, int dest, int left, int right) {
    boolean canThrow = type != NumericType.DOUBLE && type != NumericType.FLOAT;
    Value in1 = readNumericRegister(left, type);
    Value in2 = readNumericRegister(right, type);
    Value out = writeNumericRegister(dest, type,
        canThrow ? ThrowingInfo.CAN_THROW : ThrowingInfo.NO_THROW);
    Div instruction = new Div(type, out, in1, in2);
    assert instruction.instructionTypeCanThrow() == canThrow;
    add(instruction);
  }

  public void addDivLiteral(NumericType type, int dest, int value, int constant) {
    assert isNonLongIntegerType(type);
    boolean canThrow = type != NumericType.DOUBLE && type != NumericType.FLOAT;
    Value in1 = readNumericRegister(value, type);
    Value in2 = readLiteral(type, constant);
    Value out = writeNumericRegister(dest, type,
        canThrow ? ThrowingInfo.CAN_THROW : ThrowingInfo.NO_THROW);
    Div instruction = new Div(type, out, in1, in2);
    assert instruction.instructionTypeCanThrow() == canThrow;
    add(instruction);
  }

  public Monitor addMonitor(Monitor.Type type, int monitor) {
    Value in = readRegister(monitor, MoveType.OBJECT);
    Monitor monitorEnter = new Monitor(type, in);
    add(monitorEnter);
    return monitorEnter;
  }

  public void addMove(MoveType type, int dest, int src) {
    Value in = readRegister(src, type);
    if (options.debug) {
      // If the move is writing to a different local we must construct a new value.
      DebugLocalInfo destLocal = getCurrentLocal(dest);
      if (destLocal != null && destLocal != in.getLocalInfo()) {
        addDebugLocalWrite(type, dest, in);
        return;
      }
    }
    currentBlock.writeCurrentDefinition(dest, in, ThrowingInfo.NO_THROW);
  }

  public void addMul(NumericType type, int dest, int left, int right) {
    Value in1 = readNumericRegister(left, type);
    Value in2 = readNumericRegister(right, type);
    Value out = writeNumericRegister(dest, type, ThrowingInfo.NO_THROW);
    Mul instruction = new Mul(type, out, in1, in2);
    assert !instruction.instructionTypeCanThrow();
    addInstruction(instruction);
  }

  public void addMulLiteral(NumericType type, int dest, int value, int constant) {
    assert isNonLongIntegerType(type);
    Value in1 = readNumericRegister(value, type);
    Value in2 = readLiteral(type, constant);
    Value out = writeNumericRegister(dest, type, ThrowingInfo.NO_THROW);
    Mul instruction = new Mul(type, out, in1, in2);
    assert !instruction.instructionTypeCanThrow();
    addInstruction(instruction);
  }

  public void addRem(NumericType type, int dest, int left, int right) {
    boolean canThrow = type != NumericType.DOUBLE && type != NumericType.FLOAT;
    Value in1 = readNumericRegister(left, type);
    Value in2 = readNumericRegister(right, type);
    Value out = writeNumericRegister(dest, type,
        canThrow ? ThrowingInfo.CAN_THROW : ThrowingInfo.NO_THROW);
    Rem instruction = new Rem(type, out, in1, in2);
    assert instruction.instructionTypeCanThrow() == canThrow;
    addInstruction(instruction);
  }

  public void addRemLiteral(NumericType type, int dest, int value, int constant) {
    assert isNonLongIntegerType(type);
    boolean canThrow = type != NumericType.DOUBLE && type != NumericType.FLOAT;
    Value in1 = readNumericRegister(value, type);
    Value in2 = readLiteral(type, constant);
    Value out = writeNumericRegister(dest, type,
        canThrow ? ThrowingInfo.CAN_THROW : ThrowingInfo.NO_THROW);
    Rem instruction = new Rem(type, out, in1, in2);
    assert instruction.instructionTypeCanThrow() == canThrow;
    addInstruction(instruction);
  }

  public void addGoto(int targetOffset) {
    addInstruction(new Goto());
    BasicBlock targetBlock = getTarget(targetOffset);
    if (currentBlock.hasCatchSuccessor(targetBlock)) {
      needGotoToCatchBlocks.add(new BasicBlock.Pair(currentBlock, targetBlock));
    } else {
      currentBlock.link(targetBlock);
    }
    addToWorklist(targetBlock, source.instructionIndex(targetOffset));
    closeCurrentBlock();
  }

  private void addTrivialIf(int trueTargetOffset, int falseTargetOffset) {
    assert trueTargetOffset == falseTargetOffset;
    // Conditional instructions with the same true and false targets are noops. They will
    // always go to the next instruction. We end this basic block with a goto instead of
    // a conditional.
    BasicBlock target = getTarget(trueTargetOffset);
    // We expected an if here and therefore we incremented the expected predecessor count
    // twice for the following block.
    target.decrementUnfilledPredecessorCount();
    addInstruction(new Goto());
    currentBlock.link(target);
    addToWorklist(target, source.instructionIndex(trueTargetOffset));
    closeCurrentBlock();
  }

  private void addNonTrivialIf(If instruction, int trueTargetOffset, int falseTargetOffset) {
    addInstruction(instruction);
    BasicBlock trueTarget = getTarget(trueTargetOffset);
    BasicBlock falseTarget = getTarget(falseTargetOffset);
    currentBlock.link(trueTarget);
    currentBlock.link(falseTarget);
    // Generate fall-through before the block that is branched to.
    addToWorklist(falseTarget, source.instructionIndex(falseTargetOffset));
    addToWorklist(trueTarget, source.instructionIndex(trueTargetOffset));
    closeCurrentBlock();
  }

  public void addIf(If.Type type, int value1, int value2,
      int trueTargetOffset, int falseTargetOffset) {
    if (trueTargetOffset == falseTargetOffset) {
      addTrivialIf(trueTargetOffset, falseTargetOffset);
    } else {
      List<Value> values = new ArrayList<>(2);
      values.add(readRegister(value1, MoveType.SINGLE));
      values.add(readRegister(value2, MoveType.SINGLE));
      If instruction = new If(type, values);
      addNonTrivialIf(instruction, trueTargetOffset, falseTargetOffset);
    }
  }

  public void addIfZero(If.Type type, int value, int trueTargetOffset, int falseTargetOffset) {
    if (trueTargetOffset == falseTargetOffset) {
      addTrivialIf(trueTargetOffset, falseTargetOffset);
    } else {
      If instruction = new If(type, readRegister(value, MoveType.SINGLE));
      addNonTrivialIf(instruction, trueTargetOffset, falseTargetOffset);
    }
  }

  public void addInstanceGet(
      MemberType type,
      int dest,
      int object,
      DexField field) {
    Value in = readRegister(object, MoveType.OBJECT);
    Value out = writeRegister(dest, MoveType.fromMemberType(type), ThrowingInfo.CAN_THROW);
    InstanceGet instruction = new InstanceGet(type, out, in, field);
    assert instruction.instructionTypeCanThrow();
    addInstruction(instruction);
  }

  public void addInstanceOf(int dest, int value, DexType type) {
    Value in = readRegister(value, MoveType.OBJECT);
    Value out = writeRegister(dest, MoveType.SINGLE, ThrowingInfo.CAN_THROW);
    InstanceOf instruction = new InstanceOf(out, in, type);
    assert instruction.instructionTypeCanThrow();
    addInstruction(instruction);
  }

  public void addInstancePut(
      MemberType type,
      int value,
      int object,
      DexField field) {
    List<Value> values = new ArrayList<>(2);
    values.add(readRegister(value, MoveType.fromMemberType(type)));
    values.add(readRegister(object, MoveType.OBJECT));
    InstancePut instruction = new InstancePut(type, values, field);
    add(instruction);
  }

  public void addInvoke(
      Type type, DexItem item, DexProto callSiteProto, List<Value> arguments) {
    if (type == Invoke.Type.POLYMORPHIC && !options.canUseInvokePolymorphic()) {
      throw new CompilationError(
          "MethodHandle.invoke and MethodHandle.invokeExact is unsupported before "
              + "Android O (--min-api " + Constants.ANDROID_O_API + ")");
    }
    add(Invoke.create(type, item, callSiteProto, null, arguments));
  }

  public void addInvoke(
      Invoke.Type type,
      DexItem item,
      DexProto callSiteProto,
      List<MoveType> types,
      List<Integer> registers) {
    assert types.size() == registers.size();
    List<Value> arguments = new ArrayList<>(types.size());
    for (int i = 0; i < types.size(); i++) {
      arguments.add(readRegister(registers.get(i), types.get(i)));
    }
    addInvoke(type, item, callSiteProto, arguments);
  }

  public void addInvokeCustomRegisters(
      DexCallSite callSite, int argumentRegisterCount, int[] argumentRegisters) {
    int registerIndex = 0;
    DexMethodHandle bootstrapMethod = callSite.bootstrapMethod;
    List<Value> arguments = new ArrayList<>(argumentRegisterCount);

    if (!bootstrapMethod.isStaticHandle()) {
      arguments.add(readRegister(argumentRegisters[registerIndex], MoveType.OBJECT));
      registerIndex += MoveType.OBJECT.requiredRegisters();
    }

    String shorty = callSite.methodProto.shorty.toString();

    for (int i = 1; i < shorty.length(); i++) {
      MoveType moveType = MoveType.fromTypeDescriptorChar(shorty.charAt(i));
      arguments.add(readRegister(argumentRegisters[registerIndex], moveType));
      registerIndex += moveType.requiredRegisters();
    }

    add(new InvokeCustom(callSite, null, arguments));
  }

  public void addInvokeCustomRange(
      DexCallSite callSite, int argumentCount, int firstArgumentRegister) {
    DexMethodHandle bootstrapMethod = callSite.bootstrapMethod;
    List<Value> arguments = new ArrayList<>(argumentCount);

    int register = firstArgumentRegister;
    if (!bootstrapMethod.isStaticHandle()) {
      arguments.add(readRegister(register, MoveType.OBJECT));
      register += MoveType.OBJECT.requiredRegisters();
    }

    String shorty = callSite.methodProto.shorty.toString();

    for (int i = 1; i < shorty.length(); i++) {
      MoveType moveType = MoveType.fromTypeDescriptorChar(shorty.charAt(i));
      arguments.add(readRegister(register, moveType));
      register += moveType.requiredRegisters();
    }
    checkInvokeArgumentRegisters(register, firstArgumentRegister + argumentCount);
    add(new InvokeCustom(callSite, null, arguments));
  }

  public void addInvokeCustom(
      DexCallSite callSite, List<MoveType> types, List<Integer> registers) {
    assert types.size() == registers.size();
    List<Value> arguments = new ArrayList<>(types.size());
    for (int i = 0; i < types.size(); i++) {
      arguments.add(readRegister(registers.get(i), types.get(i)));
    }
    add(new InvokeCustom(callSite, null, arguments));
  }

  public void addInvokeRegisters(
      Invoke.Type type,
      DexMethod method,
      DexProto callSiteProto,
      int argumentRegisterCount,
      int[] argumentRegisters) {
    // The value of argumentRegisterCount is the number of registers - not the number of values,
    // but it is an upper bound on the number of arguments.
    List<Value> arguments = new ArrayList<>(argumentRegisterCount);
    int registerIndex = 0;
    if (type != Invoke.Type.STATIC) {
      arguments.add(readRegister(argumentRegisters[registerIndex], MoveType.OBJECT));
      registerIndex += MoveType.OBJECT.requiredRegisters();
    }
    DexString methodShorty;
    if (type == Invoke.Type.POLYMORPHIC) {
      // The call site signature for invoke polymorphic must be take from call site and not from
      // the called method.
      methodShorty = callSiteProto.shorty;
    } else {
      methodShorty = method.proto.shorty;
    }
    String shorty = methodShorty.toString();
    for (int i = 1; i < methodShorty.size; i++) {
      MoveType moveType = MoveType.fromTypeDescriptorChar(shorty.charAt(i));
      arguments.add(readRegister(argumentRegisters[registerIndex], moveType));
      registerIndex += moveType.requiredRegisters();
    }
    checkInvokeArgumentRegisters(registerIndex, argumentRegisterCount);
    addInvoke(type, method, callSiteProto, arguments);
  }

  public void addInvokeNewArray(DexType type, int argumentCount, int[] argumentRegisters) {
    String descriptor = type.descriptor.toString();
    assert descriptor.charAt(0) == '[';
    assert descriptor.length() >= 2;
    MoveType moveType = MoveType.fromTypeDescriptorChar(descriptor.charAt(1));
    List<Value> arguments = new ArrayList<>(argumentCount / moveType.requiredRegisters());
    int registerIndex = 0;
    while (registerIndex < argumentCount) {
      arguments.add(readRegister(argumentRegisters[registerIndex], moveType));
      if (moveType == MoveType.WIDE) {
        assert registerIndex < argumentCount - 1;
        assert argumentRegisters[registerIndex] == argumentRegisters[registerIndex + 1] + 1;
      }
      registerIndex += moveType.requiredRegisters();
    }
    checkInvokeArgumentRegisters(registerIndex, argumentCount);
    addInvoke(Invoke.Type.NEW_ARRAY, type, null, arguments);
  }

  public void addInvokeRange(
      Invoke.Type type,
      DexMethod method,
      DexProto callSiteProto,
      int argumentCount,
      int firstArgumentRegister) {
    // The value of argumentCount is the number of registers - not the number of values, but it
    // is an upper bound on the number of arguments.
    List<Value> arguments = new ArrayList<>(argumentCount);
    int register = firstArgumentRegister;
    if (type != Invoke.Type.STATIC) {
      arguments.add(readRegister(register, MoveType.OBJECT));
      register += MoveType.OBJECT.requiredRegisters();
    }
    DexString methodShorty;
    if (type == Invoke.Type.POLYMORPHIC) {
      // The call site signature for invoke polymorphic must be take from call site and not from
      // the called method.
      methodShorty = callSiteProto.shorty;
    } else {
      methodShorty = method.proto.shorty;
    }
    String shorty = methodShorty.toString();
    for (int i = 1; i < methodShorty.size; i++) {
      MoveType moveType = MoveType.fromTypeDescriptorChar(shorty.charAt(i));
      arguments.add(readRegister(register, moveType));
      register += moveType.requiredRegisters();
    }
    checkInvokeArgumentRegisters(register, firstArgumentRegister + argumentCount);
    addInvoke(type, method, callSiteProto, arguments);
  }

  public void addInvokeRangeNewArray(DexType type, int argumentCount, int firstArgumentRegister) {
    String descriptor = type.descriptor.toString();
    assert descriptor.charAt(0) == '[';
    assert descriptor.length() >= 2;
    MoveType moveType = MoveType.fromTypeDescriptorChar(descriptor.charAt(1));
    List<Value> arguments = new ArrayList<>(argumentCount / moveType.requiredRegisters());
    int register = firstArgumentRegister;
    while (register < firstArgumentRegister + argumentCount) {
      arguments.add(readRegister(register, moveType));
      register += moveType.requiredRegisters();
    }
    checkInvokeArgumentRegisters(register, firstArgumentRegister + argumentCount);
    addInvoke(Invoke.Type.NEW_ARRAY, type, null, arguments);
  }

  private void checkInvokeArgumentRegisters(int expected, int actual) {
    if (expected != actual) {
      throw new CompilationError("Invalid invoke instruction. "
          + "Expected use of " + expected + " argument registers, "
          + "found actual use of " + actual);
    }
  }

  public void addMoveException(int dest) {
    Value out = writeRegister(dest, MoveType.OBJECT, ThrowingInfo.NO_THROW);
    assert out.getDebugInfo() == null;
    MoveException instruction = new MoveException(out);
    assert !instruction.instructionTypeCanThrow();
    if (!currentBlock.getInstructions().isEmpty()) {
      throw new CompilationError("Invalid MoveException instruction encountered. "
          + "The MoveException instruction is not the first instruction in the block in "
          + method.qualifiedName()
          + ".");
    }
    addInstruction(instruction);
  }

  public void addMoveResult(MoveType type, int dest) {
    List<Instruction> instructions = currentBlock.getInstructions();
    Invoke invoke = instructions.get(instructions.size() - 1).asInvoke();
    assert invoke.outValue() == null;
    assert invoke.instructionTypeCanThrow();
    invoke.setOutValue(writeRegister(dest, type, ThrowingInfo.CAN_THROW));
  }

  public void addNeg(NumericType type, int dest, int value) {
    Value in = readNumericRegister(value, type);
    Value out = writeNumericRegister(dest, type, ThrowingInfo.NO_THROW);
    Neg instruction = new Neg(type, out, in);
    assert !instruction.instructionTypeCanThrow();
    addInstruction(instruction);
  }

  public void addNot(NumericType type, int dest, int value) {
    Value in = readNumericRegister(value, type);
    Value out = writeNumericRegister(dest, type, ThrowingInfo.NO_THROW);
    Not instruction = new Not(type, out, in);
    assert !instruction.instructionTypeCanThrow();
    addInstruction(instruction);
  }

  public void addNewArrayEmpty(int dest, int size, DexType type) {
    assert type.isArrayType();
    Value in = readRegister(size, MoveType.SINGLE);
    Value out = writeRegister(dest, MoveType.OBJECT, ThrowingInfo.CAN_THROW);
    NewArrayEmpty instruction = new NewArrayEmpty(out, in, type);
    assert instruction.instructionTypeCanThrow();
    addInstruction(instruction);
  }

  public void addNewArrayFilledData(int arrayRef, int elementWidth, long size, short[] data) {
    add(new NewArrayFilledData(readRegister(arrayRef, MoveType.OBJECT), elementWidth, size, data));
  }

  public void addNewInstance(int dest, DexType type) {
    Value out = writeRegister(dest, MoveType.OBJECT, ThrowingInfo.CAN_THROW);
    NewInstance instruction = new NewInstance(type, out);
    assert instruction.instructionTypeCanThrow();
    addInstruction(instruction);
  }

  public void addReturn(MoveType type, int value) {
    Value in = readRegister(value, type);
    addInstruction(new Return(in, type));
    exitBlocks.add(currentBlock);
    closeCurrentBlock();
  }

  public void addReturn() {
    addInstruction(new Return());
    exitBlocks.add(currentBlock);
    closeCurrentBlock();
  }

  public void addStaticGet(MemberType type, int dest, DexField field) {
    Value out = writeRegister(dest, MoveType.fromMemberType(type), ThrowingInfo.CAN_THROW);
    StaticGet instruction = new StaticGet(type, out, field);
    assert instruction.instructionTypeCanThrow();
    addInstruction(instruction);
  }

  public void addStaticPut(MemberType type, int value, DexField field) {
    Value in = readRegister(value, MoveType.fromMemberType(type));
    add(new StaticPut(type, in, field));
  }

  public void addSub(NumericType type, int dest, int left, int right) {
    Value in1 = readNumericRegister(left, type);
    Value in2 = readNumericRegister(right, type);
    Value out = writeNumericRegister(dest, type, ThrowingInfo.NO_THROW);
    Sub instruction = new Sub(type, out, in1, in2);
    assert !instruction.instructionTypeCanThrow();
    addInstruction(instruction);
  }

  public void addRsubLiteral(NumericType type, int dest, int value, int constant) {
    assert type != NumericType.DOUBLE;
    Value in1 = readNumericRegister(value, type);
    Value in2 = readLiteral(type, constant);
    Value out = writeNumericRegister(dest, type, ThrowingInfo.NO_THROW);
    // Add this as a sub instruction - sub instructions with literals need to have the constant
    // on the left side (rsub).
    Sub instruction = new Sub(type, out, in2, in1);
    assert !instruction.instructionTypeCanThrow();
    addInstruction(instruction);
  }

  private void addSwitchIf(int key, int value, int caseOffset, int fallthroughOffset) {
    if (key == 0) {
      addIfZero(If.Type.EQ, value, caseOffset, fallthroughOffset);
    } else {
      if (caseOffset == fallthroughOffset) {
        addTrivialIf(caseOffset, fallthroughOffset);
      } else {
        List<Value> values = new ArrayList<>(2);
        values.add(readRegister(value, MoveType.SINGLE));
        values.add(readLiteral(NumericType.INT, key));
        If instruction = new If(If.Type.EQ, values);
        addNonTrivialIf(instruction, caseOffset, fallthroughOffset);
      }
    }
  }

  public void addSwitch(int value, int[] keys, int fallthroughOffset, int[] labelOffsets) {
    int numberOfTargets = labelOffsets.length;
    assert (keys.length == 1) || (keys.length == numberOfTargets);

    // If the switch has no targets simply add a goto to the fallthrough.
    if (numberOfTargets == 0) {
      addGoto(fallthroughOffset);
      return;
    }

    Value switchValue = readRegister(value, MoveType.SINGLE);

    // Find the keys not targeting the fallthrough.
    IntList nonFallthroughKeys = new IntArrayList(numberOfTargets);
    IntList nonFallthroughOffsets = new IntArrayList(numberOfTargets);
    int numberOfFallthroughs = 0;
    if (keys.length == 1) {
      int key = keys[0];
      for (int i = 0; i < numberOfTargets; i++) {
        if (labelOffsets[i] != fallthroughOffset) {
          nonFallthroughKeys.add(key);
          nonFallthroughOffsets.add(labelOffsets[i]);
        } else {
          numberOfFallthroughs++;
        }
        key++;
      }
    } else {
      assert keys.length == numberOfTargets;
      for (int i = 0; i < numberOfTargets; i++) {
        if (labelOffsets[i] != fallthroughOffset) {
          nonFallthroughKeys.add(keys[i]);
          nonFallthroughOffsets.add(labelOffsets[i]);
        } else {
          numberOfFallthroughs++;
        }
      }
    }
    targets.get(fallthroughOffset).block.decrementUnfilledPredecessorCount(numberOfFallthroughs);

    // If this was switch with only fallthrough cases we can make it a goto.
    // Oddly, this does happen.
    if (numberOfFallthroughs == numberOfTargets) {
      assert nonFallthroughKeys.size() == 0;
      addGoto(fallthroughOffset);
      return;
    }

    // Create a switch with only the non-fallthrough targets.
    keys = nonFallthroughKeys.toIntArray();
    labelOffsets = nonFallthroughOffsets.toIntArray();
    addInstruction(createSwitch(switchValue, keys, fallthroughOffset, labelOffsets));
    closeCurrentBlock();
  }

  private Switch createSwitch(Value value, int[] keys, int fallthroughOffset, int[] targetOffsets) {
    assert keys.length == targetOffsets.length;
    // Compute target blocks for all keys. Only add a successor block once even
    // if it is hit by more of the keys.
    int[] targetBlockIndices = new int[targetOffsets.length];
    Map<Integer, Integer> offsetToBlockIndex = new HashMap<>();
    // Start with fall-through block.
    BasicBlock fallthroughBlock = getTarget(fallthroughOffset);
    currentBlock.link(fallthroughBlock);
    addToWorklist(fallthroughBlock, source.instructionIndex(fallthroughOffset));
    int fallthroughBlockIndex = currentBlock.getSuccessors().size() - 1;
    offsetToBlockIndex.put(fallthroughOffset, fallthroughBlockIndex);
    // Then all the switch target blocks.
    for (int i = 0; i < targetOffsets.length; i++) {
      int targetOffset = targetOffsets[i];
      BasicBlock targetBlock = getTarget(targetOffset);
      Integer targetBlockIndex = offsetToBlockIndex.get(targetOffset);
      if (targetBlockIndex == null) {
        // Target block not added as successor. Add it now.
        currentBlock.link(targetBlock);
        addToWorklist(targetBlock, source.instructionIndex(targetOffset));
        int successorIndex = currentBlock.getSuccessors().size() - 1;
        offsetToBlockIndex.put(targetOffset, successorIndex);
        targetBlockIndices[i] = successorIndex;
      } else {
        // Target block already added as successor. The target block therefore
        // has one less predecessor than precomputed.
        targetBlock.decrementUnfilledPredecessorCount();
        targetBlockIndices[i] = targetBlockIndex;
      }
    }
    return new Switch(value, keys, targetBlockIndices, fallthroughBlockIndex);
  }

  public void addThrow(int value) {
    Value in = readRegister(value, MoveType.OBJECT);
    addInstruction(new Throw(in));
    closeCurrentBlock();
  }

  public void addOr(NumericType type, int dest, int left, int right) {
    assert isIntegerType(type);
    Value in1 = readNumericRegister(left, type);
    Value in2 = readNumericRegister(right, type);
    Value out = writeNumericRegister(dest, type, ThrowingInfo.NO_THROW);
    Or instruction = new Or(type, out, in1, in2);
    assert !instruction.instructionTypeCanThrow();
    addInstruction(instruction);
  }

  public void addOrLiteral(NumericType type, int dest, int value, int constant) {
    assert isNonLongIntegerType(type);
    Value in1 = readNumericRegister(value, type);
    Value in2 = readLiteral(type, constant);
    Value out = writeNumericRegister(dest, type, ThrowingInfo.NO_THROW);
    Or instruction = new Or(type, out, in1, in2);
    assert !instruction.instructionTypeCanThrow();
    addInstruction(instruction);
  }

  public void addShl(NumericType type, int dest, int left, int right) {
    assert isIntegerType(type);
    Value in1 = readNumericRegister(left, type);
    Value in2 = readRegister(right, MoveType.SINGLE);
    Value out = writeNumericRegister(dest, type, ThrowingInfo.NO_THROW);
    Shl instruction = new Shl(type, out, in1, in2);
    assert !instruction.instructionTypeCanThrow();
    addInstruction(instruction);
  }

  public void addShlLiteral(NumericType type, int dest, int value, int constant) {
    assert isNonLongIntegerType(type);
    Value in1 = readNumericRegister(value, type);
    Value in2 = readLiteral(type, constant);
    Value out = writeNumericRegister(dest, type, ThrowingInfo.NO_THROW);
    Shl instruction = new Shl(type, out, in1, in2);
    assert !instruction.instructionTypeCanThrow();
    addInstruction(instruction);
  }

  public void addShr(NumericType type, int dest, int left, int right) {
    assert isIntegerType(type);
    Value in1 = readNumericRegister(left, type);
    Value in2 = readRegister(right, MoveType.SINGLE);
    Value out = writeNumericRegister(dest, type, ThrowingInfo.NO_THROW);
    Shr instruction = new Shr(type, out, in1, in2);
    assert !instruction.instructionTypeCanThrow();
    addInstruction(instruction);
  }

  public void addShrLiteral(NumericType type, int dest, int value, int constant) {
    assert isNonLongIntegerType(type);
    Value in1 = readNumericRegister(value, type);
    Value in2 = readLiteral(type, constant);
    Value out = writeNumericRegister(dest, type, ThrowingInfo.NO_THROW);
    Shr instruction = new Shr(type, out, in1, in2);
    assert !instruction.instructionTypeCanThrow();
    addInstruction(instruction);
  }

  public void addUshr(NumericType type, int dest, int left, int right) {
    assert isIntegerType(type);
    Value in1 = readNumericRegister(left, type);
    Value in2 = readRegister(right, MoveType.SINGLE);
    Value out = writeNumericRegister(dest, type, ThrowingInfo.NO_THROW);
    Ushr instruction = new Ushr(type, out, in1, in2);
    assert !instruction.instructionTypeCanThrow();
    addInstruction(instruction);
  }

  public void addUshrLiteral(NumericType type, int dest, int value, int constant) {
    assert isNonLongIntegerType(type);
    Value in1 = readNumericRegister(value, type);
    Value in2 = readLiteral(type, constant);
    Value out = writeNumericRegister(dest, type, ThrowingInfo.NO_THROW);
    Ushr instruction = new Ushr(type, out, in1, in2);
    assert !instruction.instructionTypeCanThrow();
    addInstruction(instruction);
  }

  public void addXor(NumericType type, int dest, int left, int right) {
    assert isIntegerType(type);
    Value in1 = readNumericRegister(left, type);
    Value in2 = readNumericRegister(right, type);
    Value out = writeNumericRegister(dest, type, ThrowingInfo.NO_THROW);
    Instruction instruction;
    if (in2.isConstant() && in2.getConstInstruction().asConstNumber().isIntegerNegativeOne(type)) {
      instruction = new Not(type, out, in1);
    } else {
      instruction = new Xor(type, out, in1, in2);
    }
    assert !instruction.instructionTypeCanThrow();
    addInstruction(instruction);
  }

  public void addXorLiteral(NumericType type, int dest, int value, int constant) {
    assert isNonLongIntegerType(type);
    Value in1 = readNumericRegister(value, type);
    Instruction instruction;
    if (constant == -1) {
      Value out = writeNumericRegister(dest, type, ThrowingInfo.NO_THROW);
      instruction = new Not(type, out, in1);
    } else {
      Value in2 = readLiteral(type, constant);
      Value out = writeNumericRegister(dest, type, ThrowingInfo.NO_THROW);
      instruction = new Xor(type, out, in1, in2);
    }
    assert !instruction.instructionTypeCanThrow();
    addInstruction(instruction);
  }

  public void addConversion(NumericType to, NumericType from, int dest, int source) {
    Value in = readNumericRegister(source, from);
    Value out = writeNumericRegister(dest, to, ThrowingInfo.NO_THROW);
    NumberConversion instruction = new NumberConversion(from, to, out, in);
    assert !instruction.instructionTypeCanThrow();
    addInstruction(instruction);
  }

  // Value abstraction methods.

  public Value readRegister(int register, MoveType type) {
    DebugLocalInfo local = getCurrentLocal(register);
    Value value = readRegister(register, currentBlock, EdgeType.NON_EDGE, type, local);
    // Check that any information about a current-local is consistent with the read.
    assert local == null || value.getLocalInfo() == local || value.isUninitializedLocal();
    // Check that any local information on the value is actually visible.
    // If this assert triggers, the probable cause is that we end up reading an SSA value
    // after it should have been ended on a fallthrough from a conditional jump or a trivial-phi
    // removal resurrected the local.
    assert value.getLocalInfo() == null
        || value.getDebugLocalEnds() != null
        || source.verifyLocalInScope(value.getLocalInfo());
    return value;
  }

  public Value readRegisterIgnoreLocal(int register, MoveType type) {
    DebugLocalInfo local = getCurrentLocal(register);
    return readRegister(register, currentBlock, EdgeType.NON_EDGE, type, local);
  }

  public Value readRegister(int register, BasicBlock block, EdgeType readingEdge, MoveType type,
      DebugLocalInfo local) {
    checkRegister(register);
    Value value = block.readCurrentDefinition(register, readingEdge);
    return value != null ? value : readRegisterRecursive(register, block, readingEdge, type, local);
  }

  private Value readRegisterRecursive(
      int register, BasicBlock block, EdgeType readingEdge, MoveType type, DebugLocalInfo local) {
    Value value;
    if (!block.isSealed()) {
      assert !blocks.isEmpty() : "No write to " + register;
      Phi phi = new Phi(valueNumberGenerator.next(), block, type, local);
      block.addIncompletePhi(register, phi, readingEdge);
      value = phi;
    } else if (block.getPredecessors().size() == 1) {
      assert block.verifyFilledPredecessors();
      BasicBlock pred = block.getPredecessors().get(0);
      EdgeType edgeType = pred.getEdgeType(block);
      value = readRegister(register, pred, edgeType, type, local);
    } else {
      Phi phi = new Phi(valueNumberGenerator.next(), block, type, local);
      // We need to write the phi before adding operands to break cycles. If the phi is trivial
      // and is removed by addOperands, the definition is overwritten and looked up again below.
      block.updateCurrentDefinition(register, phi, readingEdge);
      phi.addOperands(this, register);
      // Lookup the value for the register again at this point. Recursive trivial
      // phi removal could have simplified what we wanted to return here.
      value = block.readCurrentDefinition(register, readingEdge);
    }
    block.updateCurrentDefinition(register, value, readingEdge);
    return value;
  }

  public Value readNumericRegister(int register, NumericType type) {
    return readRegister(register, type.moveTypeFor());
  }

  public Value readLiteral(NumericType type, long constant) {
    Value value = new Value(valueNumberGenerator.next(), MoveType.fromNumericType(type), null);
    add(new ConstNumber(ConstType.fromNumericType(type), value, constant));
    return value;
  }

  // This special write register is needed when changing the scoping of a local variable.
  // See addDebugLocalStart and addDebugLocalEnd.
  private Value writeRegister(int register, MoveType type, ThrowingInfo throwing, DebugInfo info) {
    checkRegister(register);
    Value value = new Value(valueNumberGenerator.next(), type, info);
    currentBlock.writeCurrentDefinition(register, value, throwing);
    return value;
  }

  public Value writeRegister(int register, MoveType type, ThrowingInfo throwing) {
    DebugLocalInfo local = getCurrentLocal(register);
    DebugInfo info = null;
    if (local != null) {
      Value previousLocal = readRegisterIgnoreLocal(register, type);
      info = new DebugInfo(local, previousLocal.getLocalInfo() != local ? null : previousLocal);
    }
    return writeRegister(register, type, throwing, info);
  }

  public Value writeNumericRegister(int register, NumericType type, ThrowingInfo throwing) {
    return writeRegister(register, type.moveTypeFor(), throwing);
  }

  private DebugLocalInfo getCurrentLocal(int register) {
    return options.debug ? source.getCurrentLocal(register) : null;
  }

  private void checkRegister(int register) {
    if (register < 0) {
      throw new InternalCompilerError("Invalid register");
    }
    if (!source.verifyRegister(register)) {
      throw new CompilationError("Invalid use of register " + register);
    }
  }

  /**
   * Ensure that the current block can hold a throwing instruction. This will create a new current
   * block if the current block has handlers and already has one throwing instruction.
   */
  void ensureBlockForThrowingInstruction() {
    if (!throwingInstructionInCurrentBlock) {
      return;
    }
    BasicBlock block = new BasicBlock();
    block.setNumber(nextBlockNumber++);
    blocks.add(block);
    block.incrementUnfilledPredecessorCount();
    int freshOffset = INITIAL_BLOCK_OFFSET - 1;
    while (targets.containsKey(freshOffset)) {
      freshOffset--;
    }
    targets.put(freshOffset, null);
    for (int offset : source.getCurrentCatchHandlers().getUniqueTargets()) {
      BlockInfo target = targets.get(offset);
      assert !target.block.isSealed();
      target.block.incrementUnfilledPredecessorCount();
      target.addExceptionalPredecessor(freshOffset);
    }
    addInstruction(new Goto());
    currentBlock.link(block);
    closeCurrentBlock();
    setCurrentBlock(block);
  }

  // Private instruction helpers.
  private void addInstruction(Instruction ir) {
    attachLocalChanges(ir);
    if (currentDebugPosition != null && !ir.isMoveException()) {
      flushCurrentDebugPosition();
    }
    currentBlock.add(ir);
    if (ir.instructionTypeCanThrow()) {
      assert source.verifyCurrentInstructionCanThrow();
      CatchHandlers<Integer> catchHandlers = source.getCurrentCatchHandlers();
      if (catchHandlers != null) {
        assert !throwingInstructionInCurrentBlock;
        throwingInstructionInCurrentBlock = true;
        List<BasicBlock> targets = new ArrayList<>(catchHandlers.getAllTargets().size());
        for (int targetOffset : catchHandlers.getAllTargets()) {
          BasicBlock target = getTarget(targetOffset);
          addToWorklist(target, source.instructionIndex(targetOffset));
          targets.add(target);
        }
        currentBlock.linkCatchSuccessors(catchHandlers.getGuards(), targets);
      }
    }
    if (currentDebugPosition != null) {
      assert ir.isMoveException();
      flushCurrentDebugPosition();
    }
  }

  private void attachLocalChanges(Instruction ir) {
    if (!options.debug) {
      return;
    }
    if (debugLocalStarts.isEmpty() && debugLocalReads.isEmpty() && debugLocalEnds.isEmpty()) {
      return;
    }
    for (Value debugLocalStart : debugLocalStarts) {
      ir.addDebugValue(debugLocalStart);
      debugLocalStart.addDebugLocalStart(ir);
    }
    for (Value debugLocalRead : debugLocalReads) {
      ir.addDebugValue(debugLocalRead);
    }
    for (Value debugLocalEnd : debugLocalEnds) {
      ir.addDebugValue(debugLocalEnd);
      debugLocalEnd.addDebugLocalEnd(ir);
    }
    debugLocalStarts.clear();
    debugLocalReads.clear();
    debugLocalEnds.clear();
  }

  // Package (ie, SourceCode accessed) helpers.

  // Ensure there is a block starting at offset.
  BlockInfo ensureBlockWithoutEnqueuing(int offset) {
    assert offset != INITIAL_BLOCK_OFFSET;
    BlockInfo info = targets.get(offset);
    if (info == null) {
      // If this is a processed instruction, the block split and it has a fall-through predecessor.
      if (offset >= 0 && isOffsetProcessed(offset)) {
        int blockStartOffset = getBlockStartOffset(offset);
        BlockInfo existing = targets.get(blockStartOffset);
        info = existing.split(blockStartOffset, offset, targets);
      } else {
        info = new BlockInfo();
      }
      targets.put(offset, info);
    }
    return info;
  }

  private int getBlockStartOffset(int offset) {
    if (targets.containsKey(offset)) {
      return offset;
    }
    return targets.headMap(offset).lastIntKey();
  }

  // Ensure there is a block starting at offset and add it to the work-list if it needs processing.
  private BlockInfo ensureBlock(int offset) {
    // We don't enqueue negative targets (these are special blocks, eg, an argument prelude).
    if (offset >= 0 && !isOffsetProcessed(offset)) {
      traceBlocksWorklist.add(offset);
    }
    return ensureBlockWithoutEnqueuing(offset);
  }

  private boolean isOffsetProcessed(int offset) {
    return isIndexProcessed(source.instructionIndex(offset));
  }

  private boolean isIndexProcessed(int index) {
    if (index < processedInstructions.length) {
      return processedInstructions[index];
    }
    ensureSubroutineProcessedInstructions();
    return processedSubroutineInstructions.contains(index);
  }

  private void markIndexProcessed(int index) {
    assert !isIndexProcessed(index);
    if (index < processedInstructions.length) {
      processedInstructions[index] = true;
      return;
    }
    ensureSubroutineProcessedInstructions();
    processedSubroutineInstructions.add(index);
  }

  private void ensureSubroutineProcessedInstructions() {
    if (processedSubroutineInstructions == null) {
      processedSubroutineInstructions = new HashSet<>();
    }
  }

  // Ensure there is a block at offset and add a predecessor to it.
  private void ensureSuccessorBlock(int sourceOffset, int targetOffset, boolean normal) {
    BlockInfo targetInfo = ensureBlock(targetOffset);
    int sourceStartOffset = getBlockStartOffset(sourceOffset);
    BlockInfo sourceInfo = targets.get(sourceStartOffset);
    if (normal) {
      sourceInfo.addNormalSuccessor(targetOffset);
      targetInfo.addNormalPredecessor(sourceStartOffset);
    } else {
      sourceInfo.addExceptionalSuccessor(targetOffset);
      targetInfo.addExceptionalPredecessor(sourceStartOffset);
    }
    targetInfo.block.incrementUnfilledPredecessorCount();
  }

  void ensureNormalSuccessorBlock(int sourceOffset, int targetOffset) {
    ensureSuccessorBlock(sourceOffset, targetOffset, true);
  }

  void ensureExceptionalSuccessorBlock(int sourceOffset, int targetOffset) {
    ensureSuccessorBlock(sourceOffset, targetOffset, false);
  }

  // Private block helpers.

  private BasicBlock getTarget(int offset) {
    return targets.get(offset).block;
  }

  private void closeCurrentBlock() {
    // TODO(zerny): To ensure liveness of locals throughout the entire block, we might want to
    // insert reads before closing the block. It is unclear if we can rely on a local-end to ensure
    // liveness in all blocks where the local should be live.
    assert currentBlock != null;
    assert currentDebugPosition == null;
    currentBlock.close(this);
    setCurrentBlock(null);
    throwingInstructionInCurrentBlock = false;
  }

  private void closeCurrentBlockWithFallThrough(BasicBlock nextBlock) {
    assert currentBlock != null;
    addInstruction(new Goto());
    if (currentBlock.hasCatchSuccessor(nextBlock)) {
      needGotoToCatchBlocks.add(new BasicBlock.Pair(currentBlock, nextBlock));
    } else {
      currentBlock.link(nextBlock);
    }
    closeCurrentBlock();
  }

  void handleExitBlock() {
    if (exitBlocks.size() > 0) {
      // Create and populate the exit block if needed (eg, synchronized support for jar).
      setCurrentBlock(new BasicBlock());
      source.buildPostlude(this);
      // If the new exit block is empty and we only have one exit, abort building a new exit block.
      if (currentBlock.getInstructions().isEmpty() && exitBlocks.size() == 1) {
        normalExitBlock = exitBlocks.get(0);
        setCurrentBlock(null);
        return;
      }
      // Commit to creating the new exit block.
      normalExitBlock = currentBlock;
      normalExitBlock.setNumber(nextBlockNumber++);
      blocks.add(normalExitBlock);
      // Add the return instruction possibly creating a phi of return values.
      Return origReturn = exitBlocks.get(0).exit().asReturn();
      Phi phi = null;
      if (origReturn.isReturnVoid()) {
        normalExitBlock.add(new Return());
      } else {
        Value returnValue = origReturn.returnValue();
        MoveType returnType = origReturn.getReturnType();
        assert origReturn.getLocalInfo() == null;
        phi = new Phi(
            valueNumberGenerator.next(), normalExitBlock, returnValue.outType(), null);
        normalExitBlock.add(new Return(phi, returnType));
        assert returnType == MoveType.fromDexType(method.method.proto.returnType);
      }
      closeCurrentBlock();
      // Replace each return instruction with a goto to the new exit block.
      List<Value> operands = new ArrayList<>();
      for (BasicBlock block : exitBlocks) {
        List<Instruction> instructions = block.getInstructions();
        Return ret = block.exit().asReturn();
        if (!ret.isReturnVoid()) {
          operands.add(ret.returnValue());
          ret.returnValue().removeUser(ret);
        }
        Goto gotoExit = new Goto();
        gotoExit.setBlock(block);
        if (options.debug) {
          for (Value value : ret.getDebugValues()) {
            gotoExit.addDebugValue(value);
            value.removeDebugUser(ret);
          }
        }
        instructions.set(instructions.size() - 1, gotoExit);
        block.link(normalExitBlock);
        gotoExit.setTarget(normalExitBlock);
      }
      if (phi != null) {
        phi.addOperands(operands);
      }
    }
  }

  private void handleFallthroughToCatchBlock() {
    // When a catch handler for a block goes to the same block as the fallthrough for that
    // block the graph only has one edge there. In these cases we add an additional block so the
    // catch edge goes through that and then make the fallthrough go through a new direct edge.
    for (BasicBlock.Pair pair : needGotoToCatchBlocks) {
      BasicBlock source = pair.first;
      BasicBlock target = pair.second;

      // New block with one unfilled predecessor.
      BasicBlock newBlock = BasicBlock.createGotoBlock(target, nextBlockNumber++);
      blocks.add(newBlock);
      newBlock.incrementUnfilledPredecessorCount();

      // Link blocks.
      source.replaceSuccessor(target, newBlock);
      newBlock.getPredecessors().add(source);
      source.getSuccessors().add(target);
      target.getPredecessors().add(newBlock);

      // Check that the successor indexes are correct.
      assert source.hasCatchSuccessor(newBlock);
      assert !source.hasCatchSuccessor(target);

      // Mark the filled predecessors to the blocks.
      if (source.isFilled()) {
        newBlock.filledPredecessor(this);
      }
      target.filledPredecessor(this);
    }
  }

  /**
   * Change to control-flow graph to avoid repeated phi operands when all the same values
   * flow in from multiple predecessors.
   *
   * <p> As an example:
   *
   * <pre>
   *
   *              b1          b2         b3
   *              |                       |
   *              ----------\ | /----------
   *
   *                         b4
   *                  v3 = phi(v1, v1, v2)
   * </pre>
   *
   * <p> Is rewritten to:
   *
   * <pre>
   *              b1          b2         b3
   *                  \    /             /
   *                    b5        -------
   *                        \    /
   *                          b4
   *                  v3 = phi(v1, v2)
   *
   * </pre>
   */
  public void joinPredecessorsWithIdenticalPhis() {
    List<BasicBlock> blocksToAdd = new ArrayList<>();
    for (BasicBlock block : blocks) {
      // Consistency check. At this point there should be no incomplete phis.
      // If there are, the input is typically dex code that uses a register
      // that is not defined on all control-flow paths.
      if (block.hasIncompletePhis()) {
        throw new CompilationError(
            "Undefined value encountered during compilation. "
                + "This is typically caused by invalid dex input that uses a register "
                + "that is not define on all control-flow paths leading to the use.");
      }
      if (block.entry() instanceof MoveException) {
        // TODO: Should we support joining in the presence of move-exception instructions?
        continue;
      }
      List<Integer> operandsToRemove = new ArrayList<>();
      Map<ValueList, Integer> values = new HashMap<>();
      Map<Integer, BasicBlock> joinBlocks = new HashMap<>();
      if (block.getPhis().size() > 0) {
        Phi phi = block.getPhis().get(0);
        for (int operandIndex = 0; operandIndex < phi.getOperands().size(); operandIndex++) {
          ValueList v = ValueList.fromPhis(block.getPhis(), operandIndex);
          BasicBlock predecessor = block.getPredecessors().get(operandIndex);
          if (values.containsKey(v)) {
            // Seen before, create a join block (or reuse an existing join block) to join through.
            int otherPredecessorIndex = values.get(v);
            BasicBlock joinBlock = joinBlocks.get(otherPredecessorIndex);
            if (joinBlock == null) {
              joinBlock = BasicBlock.createGotoBlock(block, blocks.size() + blocksToAdd.size());
              joinBlocks.put(otherPredecessorIndex, joinBlock);
              blocksToAdd.add(joinBlock);
              BasicBlock otherPredecessor = block.getPredecessors().get(otherPredecessorIndex);
              joinBlock.getPredecessors().add(otherPredecessor);
              otherPredecessor.replaceSuccessor(block, joinBlock);
              block.getPredecessors().set(otherPredecessorIndex, joinBlock);
            }
            joinBlock.getPredecessors().add(predecessor);
            predecessor.replaceSuccessor(block, joinBlock);
            operandsToRemove.add(operandIndex);
          } else {
            // Record the value and its predecessor index.
            values.put(v, operandIndex);
          }
        }
      }
      block.removePredecessorsByIndex(operandsToRemove);
      block.removePhisByIndex(operandsToRemove);
    }
    blocks.addAll(blocksToAdd);
  }

  private void splitCriticalEdges() {
    List<BasicBlock> newBlocks = new ArrayList<>();
    for (BasicBlock block : blocks) {
      // We are using a spilling register allocator that might need to insert moves at
      // all critical edges, so we always split them all.
      List<BasicBlock> predecessors = block.getPredecessors();
      if (predecessors.size() <= 1) {
        continue;
      }
      // If any of the edges to the block are critical, we need to insert new blocks on each
      // containing the move-exception instruction which must remain the first instruction.
      if (block.entry() instanceof MoveException) {
        block.splitCriticalExceptionEdges(valueNumberGenerator,
            newBlock -> {
              newBlock.setNumber(blocks.size() + newBlocks.size());
              newBlocks.add(newBlock);
            });
        continue;
      }
      for (int predIndex = 0; predIndex < predecessors.size(); predIndex++) {
        BasicBlock pred = predecessors.get(predIndex);
        if (!pred.hasOneNormalExit()) {
          // Critical edge: split it and inject a new block into which the
          // phi moves can be inserted. The new block is created with the
          // correct predecessor and successor structure. It is inserted
          // at the end of the list of blocks disregarding branching
          // structure.
          int blockNumber = blocks.size() + newBlocks.size();
          BasicBlock newBlock = BasicBlock.createGotoBlock(block, blockNumber);
          newBlocks.add(newBlock);
          pred.replaceSuccessor(block, newBlock);
          newBlock.getPredecessors().add(pred);
          predecessors.set(predIndex, newBlock);
        }
      }
    }
    blocks.addAll(newBlocks);
  }

  /**
   * Trace blocks and attempt to put fallthrough blocks immediately after the block that
   * falls through. When we fail to do that we create a new fallthrough block with an explicit
   * goto to the actual fallthrough block.
   */
  private void traceBlocks(IRCode code) {
    BasicBlock[] sorted = code.topologicallySortedBlocks();
    code.clearMarks();
    int nextBlockNumber = blocks.size();
    LinkedList<BasicBlock> tracedBlocks = new LinkedList<>();
    for (BasicBlock block : sorted) {
      if (!block.isMarked()) {
        block.mark();
        tracedBlocks.add(block);
        BasicBlock current = block;
        BasicBlock fallthrough = block.exit().fallthroughBlock();
        while (fallthrough != null && !fallthrough.isMarked()) {
          fallthrough.mark();
          tracedBlocks.add(fallthrough);
          current = fallthrough;
          fallthrough = fallthrough.exit().fallthroughBlock();
        }
        if (fallthrough != null) {
          BasicBlock newFallthrough = BasicBlock.createGotoBlock(fallthrough, nextBlockNumber++);
          current.exit().setFallthroughBlock(newFallthrough);
          newFallthrough.getPredecessors().add(current);
          fallthrough.replacePredecessor(current, newFallthrough);
          newFallthrough.mark();
          tracedBlocks.add(newFallthrough);
        }
      }
    }
    code.blocks = tracedBlocks;
  }

  // Debug info helpers.

  public void updateCurrentDebugPosition(int line, DexString file) {
    // Stack-trace support requires position information in both debug and release mode.
    flushCurrentDebugPosition();
    currentDebugPosition = new DebugPosition(line, file);
    attachLocalChanges(currentDebugPosition);
  }

  private void flushCurrentDebugPosition() {
    if (currentDebugPosition != null) {
      DebugPosition position = currentDebugPosition;
      currentDebugPosition = null;
      addInstruction(position);
    }
  }

  // Other stuff.

  boolean isIntegerType(NumericType type) {
    return type != NumericType.FLOAT && type != NumericType.DOUBLE;
  }

  boolean isNonLongIntegerType(NumericType type) {
    return type != NumericType.FLOAT && type != NumericType.DOUBLE && type != NumericType.LONG;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(("blocks:\n"));
    for (BasicBlock block : blocks) {
      builder.append(block.toDetailedString());
      builder.append("\n");
    }
    return builder.toString();
  }
}
