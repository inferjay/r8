// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.regalloc;

import static org.junit.Assert.assertEquals;

import com.android.tools.r8.errors.Unimplemented;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.Move;
import com.android.tools.r8.ir.code.MoveType;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.junit.Test;

public class RegisterMoveSchedulerTest {

  private static class CollectMovesIterator implements InstructionListIterator {

    private LinkedList<Instruction> list = new LinkedList<>();
    private ListIterator<Instruction> it = list.listIterator();

    public Move get(int i) {
      return list.get(i).asMove();
    }

    public int size() {
      return list.size();
    }

    @Override
    public void replaceCurrentInstruction(Instruction newInstruction) {
      throw new Unimplemented();
    }

    @Override
    public boolean hasNext() {
      return it.hasNext();
    }

    @Override
    public Instruction next() {
      return it.next();
    }

    @Override
    public boolean hasPrevious() {
      return it.hasPrevious();
    }

    @Override
    public Instruction previous() {
      return it.previous();
    }

    @Override
    public int nextIndex() {
      return it.nextIndex();
    }

    @Override
    public int previousIndex() {
      return it.previousIndex();
    }

    @Override
    public void remove() {
      it.remove();
    }

    @Override
    public void detach() {
      remove();
    }

    @Override
    public void set(Instruction instruction) {
      it.set(instruction);
    }

    @Override
    public void add(Instruction instruction) {
      it.add(instruction);
    }

    @Override
    public BasicBlock split(IRCode code, ListIterator<BasicBlock> blockIterator) {
      throw new Unimplemented();
    }

    @Override
    public BasicBlock split(int instructions, IRCode code, ListIterator<BasicBlock> blockIterator) {
      throw new Unimplemented();
    }

    @Override
    public BasicBlock inlineInvoke(
        IRCode code, IRCode inlinee, ListIterator<BasicBlock> blockIterator,
        List<BasicBlock> blocksToRemove, DexType downcast) {
      throw new Unimplemented();
    }
  }

  @Test
  public void testSingleParallelMove() {
    CollectMovesIterator moves = new CollectMovesIterator();
    int temp = 42;
    RegisterMoveScheduler scheduler = new RegisterMoveScheduler(moves, temp);
    scheduler.addMove(new RegisterMove(0, 1, MoveType.SINGLE));
    scheduler.addMove(new RegisterMove(1, 0, MoveType.SINGLE));
    scheduler.schedule();
    assertEquals(3, moves.size());
    Move tempMove = moves.get(0);
    Move firstMove = moves.get(1);
    Move secondMove = moves.get(2);
    assertEquals(MoveType.SINGLE, tempMove.outType());
    assertEquals(MoveType.SINGLE, firstMove.outType());
    assertEquals(MoveType.SINGLE, secondMove.outType());
    assertEquals(temp, tempMove.dest().asFixedRegisterValue().getRegister());
    assertEquals(
        tempMove.src().asFixedRegisterValue().getRegister(),
        firstMove.dest().asFixedRegisterValue().getRegister());
    assertEquals(temp, secondMove.src().asFixedRegisterValue().getRegister());
    assertEquals(
        firstMove.src().asFixedRegisterValue().getRegister(),
        secondMove.dest().asFixedRegisterValue().getRegister());
  }

  @Test
  public void testWideParallelMove() {
    CollectMovesIterator moves = new CollectMovesIterator();
    int temp = 42;
    RegisterMoveScheduler scheduler = new RegisterMoveScheduler(moves, temp);
    scheduler.addMove(new RegisterMove(0, 2, MoveType.WIDE));
    scheduler.addMove(new RegisterMove(2, 0, MoveType.WIDE));
    scheduler.schedule();
    assertEquals(3, moves.size());
    Move tempMove = moves.get(0);
    Move firstMove = moves.get(1);
    Move secondMove = moves.get(2);
    assertEquals(MoveType.WIDE, tempMove.outType());
    assertEquals(MoveType.WIDE, firstMove.outType());
    assertEquals(MoveType.WIDE, secondMove.outType());
    assertEquals(temp, tempMove.dest().asFixedRegisterValue().getRegister());
    assertEquals(
        tempMove.src().asFixedRegisterValue().getRegister(),
        firstMove.dest().asFixedRegisterValue().getRegister());
    assertEquals(temp, secondMove.src().asFixedRegisterValue().getRegister());
    assertEquals(
        firstMove.src().asFixedRegisterValue().getRegister(),
        secondMove.dest().asFixedRegisterValue().getRegister());
  }

  @Test
  public void testMixedParralelMove() {
    CollectMovesIterator moves = new CollectMovesIterator();
    int temp = 42;
    RegisterMoveScheduler scheduler = new RegisterMoveScheduler(moves, temp);
    scheduler.addMove(new RegisterMove(1, 0, MoveType.WIDE));
    scheduler.addMove(new RegisterMove(0, 1, MoveType.SINGLE));
    scheduler.schedule();
    assertEquals(3, moves.size());
    Move tempMove = moves.get(0).asMove();
    Move firstMove = moves.get(1).asMove();
    Move secondMove = moves.get(2).asMove();
    assertEquals(MoveType.WIDE, tempMove.outType());
    assertEquals(MoveType.SINGLE, firstMove.outType());
    assertEquals(MoveType.WIDE, secondMove.outType());
    assertEquals(temp, tempMove.dest().asFixedRegisterValue().getRegister());
    assertEquals(
        tempMove.src().asFixedRegisterValue().getRegister(),
        firstMove.dest().asFixedRegisterValue().getRegister());
    assertEquals(temp, secondMove.src().asFixedRegisterValue().getRegister());
    assertEquals(
        firstMove.src().asFixedRegisterValue().getRegister(),
        secondMove.dest().asFixedRegisterValue().getRegister());
  }

  @Test
  public void testMixedParralelMove2() {
    CollectMovesIterator moves = new CollectMovesIterator();
    int temp = 42;
    RegisterMoveScheduler scheduler = new RegisterMoveScheduler(moves, temp);
    scheduler.addMove(new RegisterMove(0, 1, MoveType.SINGLE));
    scheduler.addMove(new RegisterMove(1, 0, MoveType.WIDE));
    scheduler.schedule();
    assertEquals(3, moves.size());
    Move tempMove = moves.get(0).asMove();
    Move firstMove = moves.get(1).asMove();
    Move secondMove = moves.get(2).asMove();
    assertEquals(MoveType.WIDE, tempMove.outType());
    assertEquals(MoveType.SINGLE, firstMove.outType());
    assertEquals(MoveType.WIDE, secondMove.outType());
    assertEquals(temp, tempMove.dest().asFixedRegisterValue().getRegister());
    assertEquals(
        tempMove.src().asFixedRegisterValue().getRegister(),
        firstMove.dest().asFixedRegisterValue().getRegister());
    assertEquals(temp, secondMove.src().asFixedRegisterValue().getRegister());
    assertEquals(
        firstMove.src().asFixedRegisterValue().getRegister(),
        secondMove.dest().asFixedRegisterValue().getRegister());
  }

  @Test
  public void testSlideWideMoves() {
    CollectMovesIterator moves = new CollectMovesIterator();
    int temp = 42;
    RegisterMoveScheduler scheduler = new RegisterMoveScheduler(moves, temp);
    scheduler.addMove(new RegisterMove(0, 1, MoveType.WIDE));
    scheduler.addMove(new RegisterMove(2, 3, MoveType.WIDE));
    scheduler.schedule();
    assertEquals(2, moves.size());
    Move firstMove = moves.get(0).asMove();
    Move secondMove = moves.get(1).asMove();
    assertEquals(MoveType.WIDE, firstMove.outType());
    assertEquals(MoveType.WIDE, secondMove.outType());
    assertEquals(0, firstMove.dest().asFixedRegisterValue().getRegister());
    assertEquals(1, firstMove.src().asFixedRegisterValue().getRegister());
    assertEquals(2, secondMove.dest().asFixedRegisterValue().getRegister());
    assertEquals(3, secondMove.src().asFixedRegisterValue().getRegister());
  }

  @Test
  public void testSlideWideMoves2() {
    CollectMovesIterator moves = new CollectMovesIterator();
    int temp = 42;
    RegisterMoveScheduler scheduler = new RegisterMoveScheduler(moves, temp);
    scheduler.addMove(new RegisterMove(2, 1, MoveType.WIDE));
    scheduler.addMove(new RegisterMove(0, 3, MoveType.WIDE));
    scheduler.schedule();
    assertEquals(3, moves.size());
    Move firstMove = moves.get(0).asMove();
    Move secondMove = moves.get(1).asMove();
    Move thirdMove = moves.get(2).asMove();
    assertEquals(MoveType.WIDE, firstMove.outType());
    assertEquals(MoveType.WIDE, secondMove.outType());
    assertEquals(MoveType.WIDE, thirdMove.outType());
    assertEquals(42, firstMove.dest().asFixedRegisterValue().getRegister());
    assertEquals(1, firstMove.src().asFixedRegisterValue().getRegister());
    assertEquals(0, secondMove.dest().asFixedRegisterValue().getRegister());
    assertEquals(3, secondMove.src().asFixedRegisterValue().getRegister());
    assertEquals(2, thirdMove.dest().asFixedRegisterValue().getRegister());
    assertEquals(42, thirdMove.src().asFixedRegisterValue().getRegister());
  }

  @Test
  public void testWideBlockedByTwoSingle() {
    CollectMovesIterator moves = new CollectMovesIterator();
    int temp = 42;
    RegisterMoveScheduler scheduler = new RegisterMoveScheduler(moves, temp);
    scheduler.addMove(new RegisterMove(2, 0, MoveType.WIDE));
    scheduler.addMove(new RegisterMove(0, 2, MoveType.SINGLE));
    scheduler.addMove(new RegisterMove(1, 3, MoveType.SINGLE));
    scheduler.schedule();
    assertEquals(4, moves.size());
    Move firstMove = moves.get(0).asMove();
    Move secondMove = moves.get(1).asMove();
    Move thirdMove = moves.get(2).asMove();
    Move fourthMove = moves.get(3).asMove();
    assertEquals(MoveType.WIDE, firstMove.outType());
    assertEquals(MoveType.SINGLE, secondMove.outType());
    assertEquals(MoveType.SINGLE, thirdMove.outType());
    assertEquals(MoveType.WIDE, fourthMove.outType());
    assertEquals(temp, firstMove.dest().asFixedRegisterValue().getRegister());
    assertEquals(0, secondMove.dest().asFixedRegisterValue().getRegister());
    assertEquals(1, thirdMove.dest().asFixedRegisterValue().getRegister());
    assertEquals(2, fourthMove.dest().asFixedRegisterValue().getRegister());
  }

  @Test
  public void testSingleBlockedBySecondPartOfWide() {
    CollectMovesIterator moves = new CollectMovesIterator();
    int temp = 42;
    RegisterMoveScheduler scheduler = new RegisterMoveScheduler(moves, temp);
    scheduler.addMove(new RegisterMove(0, 2, MoveType.WIDE));
    scheduler.addMove(new RegisterMove(3, 0, MoveType.SINGLE));
    scheduler.schedule();
    assertEquals(3, moves.size());
    Move firstMove = moves.get(0).asMove();
    Move secondMove = moves.get(1).asMove();
    Move thirdMove = moves.get(2).asMove();
    assertEquals(MoveType.WIDE, firstMove.outType());
    assertEquals(MoveType.SINGLE, secondMove.outType());
    assertEquals(MoveType.WIDE, thirdMove.outType());
    assertEquals(temp, firstMove.dest().asFixedRegisterValue().getRegister());
    assertEquals(2, firstMove.src().asFixedRegisterValue().getRegister());
    assertEquals(3, secondMove.dest().asFixedRegisterValue().getRegister());
    assertEquals(0, secondMove.src().asFixedRegisterValue().getRegister());
    assertEquals(0, thirdMove.dest().asFixedRegisterValue().getRegister());
    assertEquals(temp, thirdMove.src().asFixedRegisterValue().getRegister());
  }

  @Test
  public void multipleWideMoves() {
    CollectMovesIterator moves = new CollectMovesIterator();
    int temp = 42;
    RegisterMoveScheduler scheduler = new RegisterMoveScheduler(moves, temp);
    scheduler.addMove(new RegisterMove(14, 11, MoveType.WIDE));
    scheduler.addMove(new RegisterMove(16, 13, MoveType.WIDE));
    scheduler.addMove(new RegisterMove(10, 17, MoveType.WIDE));
    scheduler.addMove(new RegisterMove(12, 19, MoveType.WIDE));
    scheduler.schedule();
    // In order to resolve these moves, we need to use two temporary register pairs.
    assertEquals(6, moves.size());
    assertEquals(42, moves.get(0).asMove().dest().asFixedRegisterValue().getRegister());
    assertEquals(11, moves.get(0).asMove().src().asFixedRegisterValue().getRegister());
    assertEquals(44, moves.get(1).asMove().dest().asFixedRegisterValue().getRegister());
    assertEquals(13, moves.get(1).asMove().src().asFixedRegisterValue().getRegister());
    assertEquals(12, moves.get(2).asMove().dest().asFixedRegisterValue().getRegister());
  }

  @Test
  public void multipleLiveTempRegisters() {
    CollectMovesIterator moves = new CollectMovesIterator();
    int temp = 42;
    RegisterMoveScheduler scheduler = new RegisterMoveScheduler(moves, temp);
    scheduler.addMove(new RegisterMove(26, 22, MoveType.SINGLE));
    scheduler.addMove(new RegisterMove(29, 24, MoveType.WIDE));
    scheduler.addMove(new RegisterMove(28, 26, MoveType.OBJECT));
    scheduler.addMove(new RegisterMove(23, 28, MoveType.WIDE));
    scheduler.schedule();
    // For this example we need recursive unblocking.
    assertEquals(6, moves.size());
    assertEquals(42, moves.get(0).asMove().dest().asFixedRegisterValue().getRegister());
    assertEquals(26, moves.get(0).asMove().src().asFixedRegisterValue().getRegister());
    assertEquals(26, moves.get(1).asMove().dest().asFixedRegisterValue().getRegister());
    assertEquals(22, moves.get(1).asMove().src().asFixedRegisterValue().getRegister());
    assertEquals(43, moves.get(2).asMove().dest().asFixedRegisterValue().getRegister());
    assertEquals(28, moves.get(2).asMove().src().asFixedRegisterValue().getRegister());
    assertEquals(28, moves.get(3).asMove().dest().asFixedRegisterValue().getRegister());
    assertEquals(42, moves.get(3).asMove().src().asFixedRegisterValue().getRegister());
    assertEquals(29, moves.get(4).asMove().dest().asFixedRegisterValue().getRegister());
    assertEquals(24, moves.get(4).asMove().src().asFixedRegisterValue().getRegister());
    assertEquals(23, moves.get(5).asMove().dest().asFixedRegisterValue().getRegister());
    assertEquals(43, moves.get(5).asMove().src().asFixedRegisterValue().getRegister());
  }

  // Debugging aid.
  private void printMoves(List<Instruction> moves) {
    System.out.println("Generated moves:");
    System.out.println("----------------");
    for (Instruction move : moves) {
      System.out.println(move.asMove().dest().asFixedRegisterValue().getRegister() + " <- " +
          move.asMove().src().asFixedRegisterValue().getRegister() + " (" + move.outType() + ")");
    }
    System.out.println("----------------");
  }
}
