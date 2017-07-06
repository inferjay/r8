// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.conversion;

import com.android.tools.r8.code.FillArrayData;
import com.android.tools.r8.code.FillArrayDataPayload;
import com.android.tools.r8.code.Format31t;
import com.android.tools.r8.code.Goto;
import com.android.tools.r8.code.Goto16;
import com.android.tools.r8.code.Goto32;
import com.android.tools.r8.code.IfEq;
import com.android.tools.r8.code.IfEqz;
import com.android.tools.r8.code.IfGe;
import com.android.tools.r8.code.IfGez;
import com.android.tools.r8.code.IfGt;
import com.android.tools.r8.code.IfGtz;
import com.android.tools.r8.code.IfLe;
import com.android.tools.r8.code.IfLez;
import com.android.tools.r8.code.IfLt;
import com.android.tools.r8.code.IfLtz;
import com.android.tools.r8.code.IfNe;
import com.android.tools.r8.code.IfNez;
import com.android.tools.r8.code.Instruction;
import com.android.tools.r8.code.Move16;
import com.android.tools.r8.code.MoveFrom16;
import com.android.tools.r8.code.MoveObject;
import com.android.tools.r8.code.MoveObject16;
import com.android.tools.r8.code.MoveObjectFrom16;
import com.android.tools.r8.code.MoveWide;
import com.android.tools.r8.code.MoveWide16;
import com.android.tools.r8.code.MoveWideFrom16;
import com.android.tools.r8.code.Nop;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.DexCode;
import com.android.tools.r8.graph.DexCode.Try;
import com.android.tools.r8.graph.DexCode.TryHandler;
import com.android.tools.r8.graph.DexCode.TryHandler.TypeAddrPair;
import com.android.tools.r8.graph.DexDebugEventBuilder;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.Argument;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.CatchHandlers;
import com.android.tools.r8.ir.code.DebugPosition;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.If;
import com.android.tools.r8.ir.code.InstructionIterator;
import com.android.tools.r8.ir.code.Move;
import com.android.tools.r8.ir.code.NewArrayFilledData;
import com.android.tools.r8.ir.code.Switch;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.ir.regalloc.LinearScanRegisterAllocator;
import com.android.tools.r8.ir.regalloc.RegisterAllocator;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * Builder object for constructing dex bytecode from the high-level IR.
 */
public class DexBuilder {

  // The IR representation of the code to build.
  private final IRCode ir;

  // The register allocator providing register assignments for the code to build.
  private final RegisterAllocator registerAllocator;

  private final DexItemFactory dexItemFactory;

  // List of information about switch payloads that have to be created at the end of the
  // dex code.
  private final List<SwitchPayloadInfo> switchPayloadInfos = new ArrayList<>();

  // List of generated FillArrayData dex instructions.
  private final List<FillArrayDataInfo> fillArrayDataInfos = new ArrayList<>();

  // First jumbo string if known.
  private final DexString firstJumboString;

  // Set of if instructions that have offsets that are so large that they cannot be encoded in
  // the if instruction format.
  private Set<BasicBlock> ifsNeedingRewrite = Sets.newIdentityHashSet();

  // Running bounds on offsets.
  private int maxOffset = 0;
  private int minOffset = 0;

  // Mapping from IR instructions to info for computing the dex translation. Use the
  // getInfo/setInfo methods to access the mapping.
  private Info[] instructionToInfo;

  // The number of ingoing and outgoing argument registers for the code.
  private int inRegisterCount = 0;
  private int outRegisterCount = 0;

  // The string reference in the code with the highest index.
  private DexString highestSortingReferencedString = null;

  BasicBlock nextBlock;

  public DexBuilder(IRCode ir, RegisterAllocator registerAllocator, DexItemFactory dexItemFactory) {
    assert ir != null;
    assert registerAllocator != null;
    assert dexItemFactory != null;
    this.ir = ir;
    this.registerAllocator = registerAllocator;
    this.dexItemFactory = dexItemFactory;
    this.firstJumboString = null;
  }

  public DexBuilder(IRCode ir, RegisterAllocator registerAllocator,
      DexItemFactory dexItemFactory, DexString firstJumboString) {
    assert ir != null;
    assert registerAllocator != null;
    assert dexItemFactory != null;
    this.ir = ir;
    this.registerAllocator = registerAllocator;
    this.dexItemFactory = dexItemFactory;
    this.firstJumboString = firstJumboString;
  }

  private void reset() {
    switchPayloadInfos.clear();
    fillArrayDataInfos.clear();
    ifsNeedingRewrite.clear();
    maxOffset = 0;
    minOffset = 0;
    instructionToInfo = new Info[instructionNumberToIndex(ir.numberRemainingInstructions())];
    inRegisterCount = 0;
    outRegisterCount = 0;
    highestSortingReferencedString = null;
    nextBlock = null;
  }

  public boolean isJumboString(DexString string) {
    if (firstJumboString == null) {
      return false;
    }
    // We have to use compareTo here, as slowCompareTo will return the wrong order when minification
    // is used.
    return firstJumboString.compareTo(string) <= 0;
  }

  /**
   * Build the dex instructions added to this builder.
   *
   * This is a two pass construction that will first compute concrete offsets and then construct
   * the concrete instructions.
   */
  public DexCode build(int numberOfArguments) {
    int numberOfInstructions;
    int offset;

    do {
      // Rewrite ifs that are know from the previous iteration to have offsets that are too
      // large for the if encoding.
      rewriteIfs();

      // Reset the state of the builder to start from scratch.
      reset();

      // Populate the builder info objects.
      numberOfInstructions = 0;
      ListIterator<BasicBlock> iterator = ir.listIterator();
      assert iterator.hasNext();
      BasicBlock block = iterator.next();
      do {
        nextBlock = iterator.hasNext() ? iterator.next() : null;
        block.buildDex(this);
        block = nextBlock;
      } while (block != null);

      // Compute offsets.
      offset = 0;
      InstructionIterator it = ir.instructionIterator();
      while (it.hasNext()) {
        Info info = getInfo(it.next());
        info.setOffset(offset);
        offset += info.computeSize(this);
        ++numberOfInstructions;
      }
    } while (!ifsNeedingRewrite.isEmpty());

    // Build instructions.
    DexDebugEventBuilder debugEventBuilder = new DexDebugEventBuilder(ir.method, dexItemFactory);
    List<Instruction> dexInstructions = new ArrayList<>(numberOfInstructions);
    int instructionOffset = 0;
    InstructionIterator instructionIterator = ir.instructionIterator();
    while (instructionIterator.hasNext()) {
      com.android.tools.r8.ir.code.Instruction ir = instructionIterator.next();
      Info info = getInfo(ir);
      int previousInstructionCount = dexInstructions.size();
      info.addInstructions(this, dexInstructions);
      debugEventBuilder.add(instructionOffset, ir);
      if (previousInstructionCount < dexInstructions.size()) {
        while (previousInstructionCount < dexInstructions.size()) {
          Instruction instruction = dexInstructions.get(previousInstructionCount++);
          instruction.setOffset(instructionOffset);
          instructionOffset += instruction.getSize();
        }
      }
    }

    // Compute switch payloads.
    for (SwitchPayloadInfo switchPayloadInfo : switchPayloadInfos) {
      // Align payloads at even addresses.
      if (offset % 2 != 0) {
        Nop nop = new Nop();
        nop.setOffset(offset++);
        dexInstructions.add(nop);
      }
      // Create payload and add it to the instruction stream.
      Nop payload = createSwitchPayload(switchPayloadInfo, offset);
      payload.setOffset(offset);
      offset += payload.getSize();
      dexInstructions.add(payload);
    }

    // Compute fill array data payloads.
    for (FillArrayDataInfo info : fillArrayDataInfos) {
      // Align payloads at even addresses.
      if (offset % 2 != 0) {
        Nop nop = new Nop();
        nop.setOffset(offset++);
        dexInstructions.add(nop);
      }
      // Create payload and add it to the instruction stream.
      FillArrayDataPayload payload = info.ir.createPayload();
      payload.setOffset(offset);
      info.dex.setPayloadOffset(offset - info.dex.getOffset());
      offset += payload.getSize();
      dexInstructions.add(payload);
    }

    // Construct try-catch info.
    TryInfo tryInfo = computeTryInfo();

    // Return the dex code.
    DexCode code = new DexCode(
        registerAllocator.registersUsed(),
        inRegisterCount,
        outRegisterCount,
        dexInstructions.toArray(new Instruction[dexInstructions.size()]), tryInfo.tries,
        tryInfo.handlers,
        debugEventBuilder.build(),
        highestSortingReferencedString);

    return code;
  }

  // Rewrite ifs with offsets that are too large for the if encoding. The rewriting transforms:
  //
  //
  // BB0: if condition goto BB_FAR_AWAY
  // BB1: ...
  //
  // to:
  //
  // BB0: if !condition goto BB1
  // BB2: goto BB_FAR_AWAY
  // BB1: ...
  private void rewriteIfs() {
    if (ifsNeedingRewrite.isEmpty()) {
      return;
    }
    ListIterator<BasicBlock> it = ir.blocks.listIterator();
    while (it.hasNext()) {
      BasicBlock block = it.next();
      if (ifsNeedingRewrite.contains(block)) {
        If theIf = block.exit().asIf();
        BasicBlock trueTarget = theIf.getTrueTarget();
        BasicBlock newBlock = BasicBlock.createGotoBlock(trueTarget, ir.blocks.size());
        theIf.setTrueTarget(newBlock);
        theIf.invert();
        it.add(newBlock);
      }
    }
  }

  private void needsIfRewriting(BasicBlock block) {
    ifsNeedingRewrite.add(block);
  }

  public void registerStringReference(DexString string) {
    if (highestSortingReferencedString == null
        || string.slowCompareTo(highestSortingReferencedString) > 0) {
      highestSortingReferencedString = string;
    }
  }

  public void requestOutgoingRegisters(int requiredRegisterCount) {
    if (requiredRegisterCount > outRegisterCount) {
      outRegisterCount = requiredRegisterCount;
    }
  }

  public int allocatedRegister(Value value, int instructionNumber) {
    return registerAllocator.getRegisterForValue(value, instructionNumber);
  }

  // Get the argument register for a value if it is an argument, otherwise returns the
  // allocated register at the instruction number.
  public int argumentOrAllocateRegister(Value value, int instructionNumber) {
    return registerAllocator.getArgumentOrAllocateRegisterForValue(value, instructionNumber);
  }

  public boolean argumentValueUsesHighRegister(Value value, int instructionNumber) {
    return registerAllocator.argumentValueUsesHighRegister(value, instructionNumber);
  }

  public void addGoto(com.android.tools.r8.ir.code.Goto jump) {
    if (jump.getTarget() != nextBlock) {
      add(jump, new GotoInfo(jump));
    } else {
      addNop(jump);
    }
  }

  public void addIf(If branch) {
    assert nextBlock == branch.fallthroughBlock();
    add(branch, new IfInfo(branch));
  }

  public void addMove(Move move) {
    add(move, new MoveInfo(move));
  }

  public void addNop(com.android.tools.r8.ir.code.Instruction instruction) {
    add(instruction, new FallThroughInfo(instruction));
  }

  private boolean isNopInstruction(com.android.tools.r8.ir.code.Instruction instruction) {
    return instruction.isDebugLocalsChange()
        || (instruction.isConstNumber() && !instruction.outValue().needsRegister());
  }

  public void addDebugPosition(DebugPosition position) {
    BasicBlock block = position.getBlock();
    int blockIndex = ir.blocks.indexOf(block);
    Iterator<com.android.tools.r8.ir.code.Instruction> iterator = block.listIterator(position);

    com.android.tools.r8.ir.code.Instruction next = null;
    while (next == null) {
      next = iterator.next();
      while (isNopInstruction(next)) {
        next = iterator.next();
      }
      if (next.isGoto()) {
        ++blockIndex;
        BasicBlock nextBlock = blockIndex < ir.blocks.size() ? ir.blocks.get(blockIndex) : null;
        if (next.asGoto().getTarget() == nextBlock) {
          iterator = nextBlock.iterator();
          next = null;
        }
      }
    }
    if (next.isDebugPosition()) {
      add(position, new FixedSizeInfo(position, new Nop()));
    } else {
      addNop(position);
    }
  }

  public void add(com.android.tools.r8.ir.code.Instruction ir, Instruction dex) {
    assert !ir.isGoto();
    add(ir, new FixedSizeInfo(ir, dex));
  }

  public void add(com.android.tools.r8.ir.code.Instruction ir, Instruction[] dex) {
    assert !ir.isGoto();
    add(ir, new MultiFixedSizeInfo(ir, dex));
  }

  public void addSwitch(Switch s, Format31t dex) {
    assert nextBlock == s.fallthroughBlock();
    switchPayloadInfos.add(new SwitchPayloadInfo(s, dex));
    add(s, dex);
  }

  public void addFillArrayData(NewArrayFilledData nafd, FillArrayData dex) {
    fillArrayDataInfos.add(new FillArrayDataInfo(nafd, dex));
    add(nafd, dex);
  }

  public void addArgument(Argument argument) {
    inRegisterCount += argument.outValue().requiredRegisters();
    add(argument, new FallThroughInfo(argument));
  }

  private void add(com.android.tools.r8.ir.code.Instruction ir, Info info) {
    assert ir != null;
    assert info != null;
    assert getInfo(ir) == null;
    info.setMinOffset(minOffset);
    info.setMaxOffset(maxOffset);
    minOffset += info.minSize();
    maxOffset += info.maxSize();
    setInfo(ir, info);
  }

  private static int instructionNumberToIndex(int instructionNumber) {
    return instructionNumber / LinearScanRegisterAllocator.INSTRUCTION_NUMBER_DELTA;
  }

  // Helper used by the info objects.
  private Info getInfo(com.android.tools.r8.ir.code.Instruction instruction) {
    return instructionToInfo[instructionNumberToIndex(instruction.getNumber())];
  }

  private void setInfo(com.android.tools.r8.ir.code.Instruction instruction, Info info) {
    instructionToInfo[instructionNumberToIndex(instruction.getNumber())] = info;
  }

  private Info getTargetInfo(BasicBlock block) {
    InstructionIterator iterator = block.iterator();
    com.android.tools.r8.ir.code.Instruction instruction = null;
    while (iterator.hasNext()) {
      instruction = iterator.next();
      Info info = getInfo(instruction);
      if (!(info instanceof FallThroughInfo)) {
        return info;
      }
    }
    assert instruction != null && instruction.isGoto();
    return getTargetInfo(instruction.asGoto().getTarget());
  }

  // Helper for computing switch payloads.
  private Nop createSwitchPayload(SwitchPayloadInfo info, int offset) {
    Switch ir = info.ir;
    // Patch the payload offset in the generated switch instruction now
    // that the location is known.
    info.dex.setPayloadOffset(offset - getInfo(ir).getOffset());
    // Compute target offset for each of the keys based on the offset of the
    // first instruction in the block that the switch goes to for that key.
    int[] targetBlockIndices = ir.targetBlockIndices();
    int[] targets = new int[targetBlockIndices.length];
    for (int i = 0; i < targetBlockIndices.length; i++) {
      BasicBlock targetBlock = ir.targetBlock(i);
      com.android.tools.r8.ir.code.Instruction targetInstruction = targetBlock.entry();
      targets[i] = getInfo(targetInstruction).getOffset() - getInfo(ir).getOffset();
    }
    BasicBlock fallthroughBlock = ir.fallthroughBlock();
    com.android.tools.r8.ir.code.Instruction fallthroughTargetInstruction =
        fallthroughBlock.entry();
    int fallthroughTarget =
        getInfo(fallthroughTargetInstruction).getOffset() - getInfo(ir).getOffset();

    return ir.buildPayload(targets, fallthroughTarget);
  }

  // Helpers for computing the try items and handlers.

  private TryInfo computeTryInfo() {
    // Canonical map of handlers.
    BiMap<CatchHandlers<BasicBlock>, Integer> canonicalHandlers = HashBiMap.create();
    // Compute the list of try items and their handlers.
    List<TryItem> tryItems = computeTryItems(canonicalHandlers);
    // Compute handler sets before dex items which depend on the handler index.
    Try[] tries = getDexTryItems(tryItems, canonicalHandlers);
    TryHandler[] handlers = getDexTryHandlers(canonicalHandlers.inverse());
    return new TryInfo(tries, handlers);
  }

  private List<TryItem> computeTryItems(
      BiMap<CatchHandlers<BasicBlock>, Integer> handlerToIndex) {
    BiMap<Integer, CatchHandlers<BasicBlock>> indexToHandler = handlerToIndex.inverse();
    List<TryItem> tryItems = new ArrayList<>();
    List<BasicBlock> blocksWithHandlers = new ArrayList<>();
    TryItem currentTryItem = null;
    // Create try items with maximal ranges to get as much coalescing as possible. After coalescing
    // the try ranges are trimmed.
    for (BasicBlock block : ir.blocks) {
      CatchHandlers<BasicBlock> handlers = block.getCatchHandlers();
      // If this assert is hit, then the block contains no instruction that can throw. This is most
      // likely due to dead-code elimination or other optimizations that might now work on a refined
      // notion of what can throw. If so, the trivial blocks should either be removed or their catch
      // handlers deleted to reflect the simpler graph prior to building the dex code.
      assert handlers.isEmpty() || block.canThrow();
      if (!handlers.isEmpty()) {
        if (handlerToIndex.containsKey(handlers)) {
          handlers = indexToHandler.get(handlerToIndex.get(handlers));
        } else {
          handlerToIndex.put(handlers, handlerToIndex.size());
        }
        Info startInfo = getInfo(block.entry());
        Info endInfo = getInfo(block.exit());
        int start = startInfo.getOffset();
        int end = endInfo.getOffset() + endInfo.getSize();
        currentTryItem = new TryItem(handlers, start, end);
        tryItems.add(currentTryItem);
        blocksWithHandlers.add(block);
      } else if (currentTryItem != null && !block.canThrow()) {
        Info endInfo = getInfo(block.exit());
        // If the block only contains a goto there might not be an info for the exit instruction.
        if (endInfo != null) {
          currentTryItem.end = endInfo.getOffset() + endInfo.getSize();
        }
      } else {
        currentTryItem = null;
      }
    }

    // If there are no try items it is trivially coalesced.
    if (tryItems.isEmpty()) {
      return tryItems;
    }

    // Coalesce try blocks.
    tryItems.sort(TryItem::compareTo);
    List<TryItem> coalescedTryItems = new ArrayList<>(tryItems.size());
    for (int i = 0; i < tryItems.size(); ) {
      TryItem item = tryItems.get(i);
      coalescedTryItems.add(item);
      // Trim the range start for non-throwing instructions when starting a new range.
      List<com.android.tools.r8.ir.code.Instruction> instructions = blocksWithHandlers.get(i).getInstructions();
      for (com.android.tools.r8.ir.code.Instruction insn : instructions) {
        if (insn.instructionTypeCanThrow()) {
          item.start = getInfo(insn).getOffset();
          break;
        }
      }
      // Append all consecutive ranges that define the same handlers.
      ++i;
      while (i < tryItems.size()) {
        TryItem next = tryItems.get(i);
        if (item.end != next.start || !item.handlers.equals(next.handlers)) {
          item.end = trimEnd(blocksWithHandlers.get(i - 1));
          break;
        }
        item.end = next.end;
        ++i;
      }
    }
    // Trim the last try range.
    int lastIndex = tryItems.size() - 1;
    TryItem lastItem = tryItems.get(lastIndex);
    lastItem.end = trimEnd(blocksWithHandlers.get(lastIndex));
    return coalescedTryItems;
  }

  private int trimEnd(BasicBlock block) {
    // Trim the range end for non-throwing instructions when end has been computed.
    List<com.android.tools.r8.ir.code.Instruction> instructions = block.getInstructions();
    for (com.android.tools.r8.ir.code.Instruction insn : Lists.reverse(instructions)) {
      if (insn.instructionTypeCanThrow()) {
        Info info = getInfo(insn);
        return info.getOffset() + info.getSize();
      }
    }
    throw new Unreachable("Expected to find a possibly throwing instruction");
  }

  private static Try[] getDexTryItems(List<TryItem> tryItems,
      Map<CatchHandlers<BasicBlock>, Integer> catchHandlers) {
    Try[] tries = new Try[tryItems.size()];
    for (int i = 0; i < tries.length; ++i) {
      TryItem item = tryItems.get(i);
      Try dexTry = new Try(item.start, item.end - item.start, -1);
      dexTry.handlerIndex = catchHandlers.get(item.handlers);
      tries[i] = dexTry;
    }
    return tries;
  }

  private TryHandler[] getDexTryHandlers(Map<Integer, CatchHandlers<BasicBlock>> catchHandlers) {
    TryHandler[] handlers = new TryHandler[catchHandlers.size()];
    for (int j = 0; j < catchHandlers.size(); j++) {
      CatchHandlers<BasicBlock> handlerGroup = catchHandlers.get(j);
      int catchAllOffset = TryHandler.NO_HANDLER;
      List<TypeAddrPair> pairs = new ArrayList<>();
      for (int i = 0; i < handlerGroup.getGuards().size(); i++) {
        DexType type = handlerGroup.getGuards().get(i);
        BasicBlock target = handlerGroup.getAllTargets().get(i);
        int targetOffset = getInfo(target.entry()).getOffset();
        if (type == DexItemFactory.catchAllType) {
          assert i == handlerGroup.getGuards().size() - 1;
          catchAllOffset = targetOffset;
        } else {
          pairs.add(new TypeAddrPair(type, targetOffset, -1));
        }
      }
      TypeAddrPair[] pairsArray = pairs.toArray(new TypeAddrPair[pairs.size()]);
      handlers[j] = new TryHandler(pairsArray, catchAllOffset);
    }
    return handlers;
  }

  // Dex instruction wrapper with information to compute instruction sizes and offsets for jumps.
  private static abstract class Info {

    private final com.android.tools.r8.ir.code.Instruction ir;
    // Concrete final offset of the instruction.
    private int offset = -1;
    // Lower and upper bound of the final offset.
    private int minOffset = -1;
    private int maxOffset = -1;

    public Info(com.android.tools.r8.ir.code.Instruction ir) {
      assert ir != null;
      this.ir = ir;
    }

    // Computes the final size of the instruction.
    // All instruction offsets up-to and including this instruction will be defined at this point.
    public abstract int computeSize(DexBuilder builder);

    // Materialize the actual construction.
    // All instruction offsets are known at this point.
    public abstract void addInstructions(DexBuilder builder, List<Instruction> instructions);

    // Lower bound on the size of the instruction.
    public abstract int minSize();

    // Upper bound on the size of the instruction.
    public abstract int maxSize();

    public abstract int getSize();

    public int getOffset() {
      assert offset >= 0 : this;
      return offset;
    }

    public void setOffset(int offset) {
      assert offset >= 0;
      this.offset = offset;
    }

    public int getMinOffset() {
      assert minOffset >= 0;
      return minOffset;
    }

    public void setMinOffset(int minOffset) {
      assert minOffset >= 0;
      this.minOffset = minOffset;
    }

    public int getMaxOffset() {
      assert maxOffset >= 0;
      return maxOffset;
    }

    public void setMaxOffset(int maxOffset) {
      assert maxOffset >= 0;
      this.maxOffset = maxOffset;
    }

    public com.android.tools.r8.ir.code.Instruction getIR() {
      return ir;
    }
  }

  private static class FixedSizeInfo extends Info {

    private Instruction instruction;

    public FixedSizeInfo(com.android.tools.r8.ir.code.Instruction ir, Instruction instruction) {
      super(ir);
      this.instruction = instruction;
    }

    @Override
    public int getSize() {
      return instruction.getSize();
    }

    @Override
    public int minSize() {
      return instruction.getSize();
    }

    @Override
    public int maxSize() {
      return instruction.getSize();
    }

    @Override
    public int computeSize(DexBuilder builder) {
      instruction.setOffset(getOffset()); // for better printing of the dex code.
      return instruction.getSize();
    }

    @Override
    public void addInstructions(DexBuilder builder, List<Instruction> instructions) {
      instructions.add(instruction);
    }
  }

  private static class MultiFixedSizeInfo extends Info {

    private Instruction[] instructions;
    private final int size;

    public MultiFixedSizeInfo(com.android.tools.r8.ir.code.Instruction ir, Instruction[] instructions) {
      super(ir);
      this.instructions = instructions;
      int size = 0;
      for (Instruction instruction : instructions) {
        size += instruction.getSize();
      }
      this.size = size;
    }

    @Override
    public int computeSize(DexBuilder builder) {
      return size;
    }

    @Override
    public void addInstructions(DexBuilder builder, List<Instruction> instructions) {
      int offset = getOffset();
      for (Instruction instruction : this.instructions) {
        instructions.add(instruction);
        instruction.setOffset(offset);
        offset += instruction.getSize();
      }
    }

    @Override
    public int minSize() {
      return size;
    }

    @Override
    public int maxSize() {
      return size;
    }

    @Override
    public int getSize() {
      return size;
    }
  }

  private static class FallThroughInfo extends Info {

    public FallThroughInfo(com.android.tools.r8.ir.code.Instruction ir) {
      super(ir);
    }

    @Override
    public int getSize() {
      return 0;
    }

    @Override
    public int computeSize(DexBuilder builder) {
      return 0;
    }

    @Override
    public void addInstructions(DexBuilder builder, List<Instruction> instructions) {
    }

    @Override
    public int minSize() {
      return 0;
    }

    @Override
    public int maxSize() {
      return 0;
    }
  }

  private static class GotoInfo extends Info {

    private int size = -1;

    public GotoInfo(com.android.tools.r8.ir.code.Goto jump) {
      super(jump);
    }

    private com.android.tools.r8.ir.code.Goto getJump() {
      return (com.android.tools.r8.ir.code.Goto) getIR();
    }

    @Override
    public int getSize() {
      assert size > 0;
      return size;
    }

    @Override
    public int minSize() {
      assert new Goto(42).getSize() == 1;
      return 1;
    }

    @Override
    public int maxSize() {
      assert new Goto32(0).getSize() == 3;
      return 3;
    }

    @Override
    public int computeSize(DexBuilder builder) {
      assert size < 0;
      com.android.tools.r8.ir.code.Goto jump = getJump();
      Info targetInfo = builder.getTargetInfo(jump.getTarget());
      // Trivial loop will be emitted as: nop & goto -1
      if (jump == targetInfo.getIR()) {
        size = 2;
        return size;
      }
      int maxOffset = getMaxOffset();
      int maxTargetOffset = targetInfo.getMaxOffset();
      int delta;
      if (maxTargetOffset < maxOffset) {
        // Backward branch: compute exact size (the target offset is set).
        delta = getOffset() - targetInfo.getOffset();
      } else {
        // Forward branch: over estimate the distance, but take into account the sizes
        // of instructions generated so far. That way the over estimation is only for the
        // instructions between this one and the target.
        int maxOverEstimation = maxOffset - getOffset();
        delta = (maxTargetOffset - maxOverEstimation) - getOffset();
      }
      if (delta <= Byte.MAX_VALUE) {
        size = 1;
      } else if (delta <= Short.MAX_VALUE) {
        size = 2;
      } else {
        size = 3;
      }
      if (targetInfo.getIR().isReturn()) {
        // Set the size to the min of the size of the return and the size of the goto. When
        // adding instructions, we use the return if the computed size matches the size of the
        // return.
        size = Math.min(targetInfo.getSize(), size);
      }
      return size;
    }

    @Override
    public void addInstructions(DexBuilder builder, List<Instruction> instructions) {
      com.android.tools.r8.ir.code.Goto jump = getJump();
      int source = builder.getInfo(jump).getOffset();
      Info targetInfo = builder.getTargetInfo(jump.getTarget());
      int relativeOffset = targetInfo.getOffset() - source;
      // We should never generate a goto to the following instruction or two consecutive returns.
      // TODO(b/34726595): We might have a goto to the following instruction if we fail to DCE.
      // assert relativeOffset != size;
      Instruction dex;
      // Emit a return if the target is a return and the size of the return is the computed
      // size of this instruction.
      if (targetInfo.getIR().isReturn() && size == targetInfo.getSize()) {
        dex = targetInfo.getIR().asReturn().createDexInstruction(builder);
      } else {
        switch (size) {
          case 1:
            assert relativeOffset != 0;
            dex = new Goto(relativeOffset);
            break;
          case 2:
            if (relativeOffset == 0) {
              Nop nop = new Nop();
              instructions.add(nop);
              dex = new Goto(-nop.getSize());
            } else {
              dex = new Goto16(relativeOffset);
            }
            break;
          case 3:
            dex = new Goto32(relativeOffset);
            break;
          default:
            throw new Unreachable("Unexpected size for goto instruction: " + size);
        }
      }
      dex.setOffset(getOffset()); // for better printing of the dex code.
      instructions.add(dex);
    }
  }

  public static class IfInfo extends Info {

    private int size = -1;

    public IfInfo(If branch) {
      super(branch);
    }

    private If getBranch() {
      return (If) getIR();
    }

    private boolean branchesToSelf(DexBuilder builder) {
      If branch = getBranch();
      Info trueTargetInfo = builder.getTargetInfo(branch.getTrueTarget());
      return branch == trueTargetInfo.getIR();
    }

    private boolean offsetOutOfRange(DexBuilder builder) {
      Info targetInfo = builder.getTargetInfo(getBranch().getTrueTarget());
      int maxOffset = getMaxOffset();
      int maxTargetOffset = targetInfo.getMaxOffset();
      if (maxTargetOffset < maxOffset) {
        return getOffset() - targetInfo.getOffset() < Short.MIN_VALUE;
      }
      // Forward branch: over estimate the distance, but take into account the sizes
      // of instructions generated so far. That way the over estimation is only for the
      // instructions between this one and the target.
      int maxOverEstimation = maxOffset - getOffset();
      return (maxTargetOffset - maxOverEstimation) - getOffset() > Short.MAX_VALUE;
    }

    @Override
    public void addInstructions(DexBuilder builder, List<Instruction> instructions) {
      If branch = getBranch();
      int source = builder.getInfo(branch).getOffset();
      int target = builder.getInfo(branch.getTrueTarget().entry()).getOffset();
      int relativeOffset = target - source;
      int register1 = builder.allocatedRegister(branch.inValues().get(0), branch.getNumber());
      if (size == 3) {
        assert branchesToSelf(builder);
        Nop nop = new Nop();
        relativeOffset -= nop.getSize();
        instructions.add(nop);
      }
      assert relativeOffset != 0;
      Instruction instruction = null;
      if (branch.isZeroTest()) {
        switch (getBranch().getType()) {
          case EQ:
            instruction = new IfEqz(register1, relativeOffset);
            break;
          case GE:
            instruction = new IfGez(register1, relativeOffset);
            break;
          case GT:
            instruction = new IfGtz(register1, relativeOffset);
            break;
          case LE:
            instruction = new IfLez(register1, relativeOffset);
            break;
          case LT:
            instruction = new IfLtz(register1, relativeOffset);
            break;
          case NE:
            instruction = new IfNez(register1, relativeOffset);
            break;
        }
      } else {
        int register2 = builder.allocatedRegister(branch.inValues().get(1), branch.getNumber());
        switch (getBranch().getType()) {
          case EQ:
            instruction = new IfEq(register1, register2, relativeOffset);
            break;
          case GE:
            instruction = new IfGe(register1, register2, relativeOffset);
            break;
          case GT:
            instruction = new IfGt(register1, register2, relativeOffset);
            break;
          case LE:
            instruction = new IfLe(register1, register2, relativeOffset);
            break;
          case LT:
            instruction = new IfLt(register1, register2, relativeOffset);
            break;
          case NE:
            instruction = new IfNe(register1, register2, relativeOffset);
            break;
        }
      }
      instruction.setOffset(getOffset());
      instructions.add(instruction);
    }

    @Override
    public int computeSize(DexBuilder builder) {
      if (offsetOutOfRange(builder)) {
        builder.needsIfRewriting(getBranch().getBlock());
      }
      size = branchesToSelf(builder) ? 3 : 2;
      return size;
    }

    @Override
    public int minSize() {
      return 2;
    }

    @Override
    public int maxSize() {
      return 3;
    }

    @Override
    public int getSize() {
      return size;
    }
  }

  public static class MoveInfo extends Info {

    private int size = -1;

    public MoveInfo(Move move) {
      super(move);
    }

    private Move getMove() {
      return (Move) getIR();
    }

    @Override
    public int computeSize(DexBuilder builder) {
      Move move = getMove();
      int srcRegister = builder.allocatedRegister(move.src(), move.getNumber());
      int destRegister = builder.allocatedRegister(move.dest(), move.getNumber());
      if (srcRegister == destRegister) {
        size = 1;
      } else if (srcRegister <= Constants.U4BIT_MAX && destRegister <= Constants.U4BIT_MAX) {
        size = 1;
      } else if (destRegister <= Constants.U8BIT_MAX) {
        size = 2;
      } else {
        size = 3;
      }
      return size;
    }

    @Override
    public void addInstructions(DexBuilder builder, List<Instruction> instructions) {
      Move move = getMove();
      int dest = builder.allocatedRegister(move.dest(), move.getNumber());
      int src = builder.allocatedRegister(move.src(), move.getNumber());
      Instruction instruction = null;
      switch (size) {
        case 1:
          if (src == dest) {
            instruction = new Nop();
            break;
          }
          switch (move.outType()) {
            case SINGLE:
              instruction = new com.android.tools.r8.code.Move(dest, src);
              break;
            case WIDE:
              instruction = new MoveWide(dest, src);
              break;
            case OBJECT:
              instruction = new MoveObject(dest, src);
              break;
            default:
              throw new Unreachable("Unexpected type: " + move.outType());
          }
          break;
        case 2:
          switch (move.outType()) {
            case SINGLE:
              instruction = new MoveFrom16(dest, src);
              break;
            case WIDE:
              instruction = new MoveWideFrom16(dest, src);
              break;
            case OBJECT:
              instruction = new MoveObjectFrom16(dest, src);
              break;
            default:
              throw new Unreachable("Unexpected type: " + move.outType());
          }
          break;
        case 3:
          switch (move.outType()) {
            case SINGLE:
              instruction = new Move16(dest, src);
              break;
            case WIDE:
              instruction = new MoveWide16(dest, src);
              break;
            case OBJECT:
              instruction = new MoveObject16(dest, src);
              break;
            default:
              throw new Unreachable("Unexpected type: " + move.outType());
          }
          break;
      }
      instruction.setOffset(getOffset());
      instructions.add(instruction);
    }

    @Override
    public int minSize() {
      assert new Nop().getSize() == 1 && new com.android.tools.r8.code.Move(0, 0).getSize() == 1;
      return 1;
    }

    @Override
    public int maxSize() {
      assert new Move16(0, 0).getSize() == 3;
      return 3;
    }

    @Override
    public int getSize() {
      assert size > 0;
      return size;
    }
  }

  // Return-type wrapper for try-related data.
  private static class TryInfo {

    public final Try[] tries;
    public final TryHandler[] handlers;

    public TryInfo(Try[] tries, TryHandler[] handlers) {
      this.tries = tries;
      this.handlers = handlers;
    }
  }

  // Helper class for coalescing ranges for try blocks.
  private static class TryItem implements Comparable<TryItem> {

    public final CatchHandlers<BasicBlock> handlers;
    public int start;
    public int end;

    public TryItem(CatchHandlers<BasicBlock> handlers, int start, int end) {
      this.handlers = handlers;
      this.start = start;
      this.end = end;
    }

    @Override
    public int compareTo(TryItem other) {
      return Integer.compare(start, other.start);
    }
  }

  private static class SwitchPayloadInfo {

    public final Switch ir;
    public final Format31t dex;

    public SwitchPayloadInfo(Switch ir, Format31t dex) {
      this.ir = ir;
      this.dex = dex;
    }
  }

  private static class FillArrayDataInfo {

    public final NewArrayFilledData ir;
    public final FillArrayData dex;

    public FillArrayDataInfo(NewArrayFilledData ir, FillArrayData dex) {
      this.ir = ir;
      this.dex = dex;
    }
  }
}
