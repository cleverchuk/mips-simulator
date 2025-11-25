/*
 * MIT License
 *
 * Copyright (c) 2022 CleverChuk
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.cleverchuk.mips.simulator.binary;

import com.cleverchuk.mips.compiler.codegen.Assembler;
import com.cleverchuk.mips.compiler.lexer.MipsLexer;
import com.cleverchuk.mips.compiler.parser.RecursiveDescentParser;
import com.cleverchuk.mips.compiler.semantic.SemanticAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.FourOpAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.InstructionAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.OneOpAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.ThreeOpAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.TwoOpAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.ZeroOpAnalyzer;
import com.cleverchuk.mips.simulator.mem.Memory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MipsInstructionDecoderTest {

  RecursiveDescentParser parser =
      new RecursiveDescentParser(
          new MipsLexer(),
          new SemanticAnalyzer(
              new InstructionAnalyzer(
                  new ZeroOpAnalyzer(),
                  new OneOpAnalyzer(),
                  new TwoOpAnalyzer(
                      new TwoOpAnalyzer.LoadStoreAnalyzer(),
                      new TwoOpAnalyzer.TwoRegOpcodeAnalyzer(),
                      new TwoOpAnalyzer.BranchOpcodeAnalyzer()),
                  new ThreeOpAnalyzer(
                      new ThreeOpAnalyzer.ShiftRotateAnalyzer(),
                      new ThreeOpAnalyzer.ConditionalTestingAndMoveAnalyzer(),
                      new ThreeOpAnalyzer.ArithmeticAndLogicalOpcodeAnalyzer()),
                  new FourOpAnalyzer())));

  private Assembler assembler;


  @Before
  public void setup() {
    assembler = new Assembler();
    parser.addVisitor(assembler);
  }

  @After
  public void teardown() {
    parser.removeVisitor(assembler);
  }

  @Test
  public void testLi() {
    String[] instructions = {".text", "li $t0, 300"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.ORI, actualOpcode);
  }

  @Test
  public void testSdc2() {
    String[] instructions = {".text", "sdc2 $t0, 3($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SDC2, actualOpcode);
  }

  @Test
  public void testB() {
    String[] instructions = {".text", "b label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BEQ, actualOpcode);
  }

  @Test
  public void testBeqz() {
    String[] instructions = {
        ".data", "bytes: .byte 1,2", ".text", "beqz $t0, label", "label: la $t4, bytes"
    };
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BEQZ, actualOpcode);
  }

  @Test
  public void testLa() {
    String[] instructions = {
        ".data", "floats: .float 1.666,2.333", "bytes: .byte 1,2", ".text", "la $t0, bytes"
    };
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.AUI, actualOpcode);

    actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset() + 4));
    assertEquals(Opcode.ORI, actualOpcode);
  }

  @Test
  public void testMove() {
    String[] instructions = {
        ".data", "floats: .float 1.666,2.333", "bytes: .byte 1,2", ".text", "move $t0, $t1"
    };
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.OR, actualOpcode);
  }

  @Test
  public void testNegu() {
    String[] instructions = {".text", "negu $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SUBU, actualOpcode);
  }

  @Test
  public void testNop() {
    String[] instructions = {".text", "nop"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SLL, actualOpcode);
  }

  @Test
  public void testNot() {
    String[] instructions = {".text", "not $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.NOR, actualOpcode);
  }

  @Test
  public void testBnez() {
    String[] instructions = {
        ".data", "bytes: .byte 1, 2", ".text", "bnez $t0, label", "label: la $t4, bytes"
    };
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BNE, actualOpcode);
  }

  @Test
  public void testBal() {
    String[] instructions = {
        ".data", "bytes: .byte 1, 2", ".text", "label: la $t4, bytes", "bal label"
    };
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset() + 8));
    assertEquals(Opcode.BAL, actualOpcode);
  }

  @Test
  public void testUlw() {
    String[] instructions = {".text", "ulw $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.LWL, actualOpcode);

    actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset() + 4));
    assertEquals(Opcode.LWR, actualOpcode);
  }

  @Test
  public void testUsw() {
    String[] instructions = {".text", "usw $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SWL, actualOpcode);

    actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset() + 4));
    assertEquals(Opcode.SWR, actualOpcode);
  }

  @Test
  public void testEhb() {
    String[] instructions = {".text", "ehb"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SLL, actualOpcode);
  }

  @Test
  public void testAbs_d() {
    String[] instructions = {".text", "abs.d $f8, $f9"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.ABS_D, actualOpcode);
  }

  @Test
  public void testAbs_s() {
    String[] instructions = {".text", "abs.s $f8, $f9"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.ABS_S, actualOpcode);
  }

  @Test
  public void testAdd() {
    String[] instructions = {".text", "add $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.ADD, actualOpcode);
  }

  @Test
  public void testAdd_d() {
    String[] instructions = {".text", "add.d $f8, $f9, $f10"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.ADD_D, actualOpcode);
  }

  @Test
  public void testAdd_s() {
    String[] instructions = {".text", "add.s $f8, $f9, $f10"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.ADD_S, actualOpcode);
  }

  @Test
  public void testAddi() {
    String[] instructions = {".text", "addi $t0, $t1, 1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.ADDI, actualOpcode);
  }

  @Test
  public void testAddiu() {
    String[] instructions = {".text", "addiu $t0, $t1, 1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.ADDIU, actualOpcode);
  }

  @Test
  public void testAddiupc() {
    String[] instructions = {".text", "addiupc $t0, 1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.ADDIUPC, actualOpcode);
  }

  @Test
  public void testAddu() {
    String[] instructions = {".text", "addu $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.ADDU, actualOpcode);
  }

  @Test
  public void testAlign() {
    String[] instructions = {".text", "align $t0, $t1, $t2, 1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.ALIGN, actualOpcode);
  }

  @Test
  public void testAluipc() {
    String[] instructions = {".text", "aluipc $t0, 1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.ALUIPC, actualOpcode);
  }

  @Test
  public void testAnd() {
    String[] instructions = {".text", "and $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.AND, actualOpcode);
  }

  @Test
  public void testAndi() {
    String[] instructions = {".text", "andi $t0, $t1, 5"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.ANDI, actualOpcode);
  }

  @Test
  public void testAui() {
    String[] instructions = {".text", "aui $t0, $t1, 5"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.AUI, actualOpcode);
  }

  @Test
  public void testAuipc() {
    String[] instructions = {".text", "auipc $t0, 5"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.AUIPC, actualOpcode);
  }

  @Test
  public void testBalc() {
    String[] instructions = {".text", "label: andi $t0, $t1, 5", "balc label"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BALC, actualOpcode);
  }

  @Test
  public void testBc() {
    String[] instructions = {".text", "label: andi $t0, $t1, 5", "bc label"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BC, actualOpcode);
  }

  @Test
  public void testBc1eqz() {
    String[] instructions = {".data", "bytes: .byte 1,2", ".text", "bc1eqz $f1, label", "label: andi $t0, $t1, 5"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BC1EQZ, actualOpcode);
  }

  @Test
  public void testBc1nez() {
    String[] instructions = {".text", "bc1nez $f1, label", "label: andi $t0, $t1, 5"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BC1NEZ, actualOpcode);
  }

  @Test
  public void testBc2eqz() {
    String[] instructions = {".data", "bytes: .byte 1,2", ".text", "bc2eqz $f1, label", "label: andi $t0, $t1, 5"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BC2EQZ, actualOpcode);
  }

  @Test
  public void testBc2nez() {
    String[] instructions = {".text", "bc2nez $f1, label", "label: andi $t0, $t1, 5"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BC2EQZ, actualOpcode);
  }

  @Test
  public void testBeq() {
    String[] instructions = {".text", "beq $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BEQ, actualOpcode);
  }

  @Test
  public void testBeqc() {
    String[] instructions = {".text", "beqc $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BEQC, actualOpcode);
  }

  @Test
  public void testBeqzalc() {
    String[] instructions = {".text", "beqzalc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BEQZALC, actualOpcode);
  }

  @Test
  public void testBeqzc() {
    String[] instructions = {".text", "beqzc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BEQZC, actualOpcode);
  }

  @Test
  public void testBgec() {
    String[] instructions = {".text", "bgec $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BGEC, actualOpcode);
  }

  @Test
  public void testBgezalc() {
    String[] instructions = {".text", "bgezalc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BGEZALC, actualOpcode);
  }

  @Test
  public void testBgezal() {
    String[] instructions = {".text", "bgezal $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BGEZAL, actualOpcode);
  }

  @Test
  public void testBgezc() {
    String[] instructions = {".text", "bgezc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BGEZC, actualOpcode);
  }

  @Test
  public void testBgez() {
    String[] instructions = {".text", "bgez $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BGEZ, actualOpcode);
  }

  @Test
  public void testBgeuc() {
    String[] instructions = {".text", "bgeuc $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BGEUC, actualOpcode);
  }

  @Test
  public void testBgtzalc() {
    String[] instructions = {".text", "bgtzalc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BGTZALC, actualOpcode);
  }

  @Test
  public void testBgtzc() {
    String[] instructions = {".text", "bgtzc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BGTZC, actualOpcode);
  }

  @Test
  public void testBgtz() {
    String[] instructions = {".text", "bgtz $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BGTZ, actualOpcode);
  }

  @Test
  public void testBitswap() {
    String[] instructions = {".text", "bitswap $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BITSWAP, actualOpcode);
  }

  @Test
  public void testBlezalc() {
    String[] instructions = {".text", "blezalc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BLEZALC, actualOpcode);
  }

  @Test
  public void testBlezc() {
    String[] instructions = {".text", "blezc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BLEZC, actualOpcode);
  }

  @Test
  public void testBlez() {
    String[] instructions = {".text", "blez $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BLEZ, actualOpcode);
  }

  @Test
  public void testBltc() {
    String[] instructions = {".text", "bltc $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BLTC, actualOpcode);
  }

  @Test
  public void testBltuc() {
    String[] instructions = {".text", "bltuc $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BLTUC, actualOpcode);
  }

  @Test
  public void testBltzalc() {
    String[] instructions = {".text", "bltzalc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BLTZALC, actualOpcode);
  }

  @Test
  public void testBltzal() {
    String[] instructions = {".text", "bltzal $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BLTZAL, actualOpcode);
  }

  @Test
  public void testBltzc() {
    String[] instructions = {".text", "bltzc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BLTZC, actualOpcode);
  }

  @Test
  public void testBltz() {
    String[] instructions = {".text", "bltz $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BLTZ, actualOpcode);
  }

  @Test
  public void testBne() {
    String[] instructions = {".text", "bne $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BNE, actualOpcode);
  }

  @Test
  public void testBnec() {
    String[] instructions = {".text", "bnec $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BNEC, actualOpcode);
  }

  @Test
  public void testBnezalc() {
    String[] instructions = {".text", "bnezalc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BNEZALC, actualOpcode);
  }

  @Test
  public void testBnezc() {
    String[] instructions = {".text", "bnezc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BNEZC, actualOpcode);
  }

  @Test
  public void testBnvc() {
    String[] instructions = {".text", "bnvc $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BNVC, actualOpcode);
  }

  @Test
  public void testBovc() {
    String[] instructions = {".text", "bovc $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BOVC, actualOpcode);
  }

  @Test
  public void testBreak() {
    String[] instructions = {".text", "break"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.BREAK, actualOpcode);
  }

  @Test
  public void testCache() {
    String[] instructions = {".text", "cache 4, 2($t0)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CACHE, actualOpcode);
  }

  @Test
  public void testCachee() {
    String[] instructions = {".text", "cachee 4, 2($t0)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CACHEE, actualOpcode);
  }

  @Test
  public void testCeil_l_d() {
    String[] instructions = {".text", "ceil.l.d $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CEIL_L_D, actualOpcode);
  }

  @Test
  public void testCeil_l_s() {
    String[] instructions = {".text", "ceil.l.s $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CEIL_L_S, actualOpcode);
  }

  @Test
  public void testCeil_w_d() {
    String[] instructions = {".text", "ceil.w.d $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CEIL_W_D, actualOpcode);
  }

  @Test
  public void testCeil_w_s() {
    String[] instructions = {".text", "ceil.w.s $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CEIL_W_S, actualOpcode);
  }

  @Test
  public void testCfc1() {
    String[] instructions = {".text", "cfc1 $t0, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CFC1, actualOpcode);
  }

  @Test
  public void testCfc2() {
    String[] instructions = {".text", "cfc2 $t0, 3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CFC2, actualOpcode);
  }

  @Test
  public void testClass_d() {
    String[] instructions = {".text", "class.d $f1, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CLASS_D, actualOpcode);
  }

  @Test
  public void testClass_s() {
    String[] instructions = {".text", "class.s $f1, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CLASS_S, actualOpcode);
  }

  @Test
  public void testClo() {
    String[] instructions = {".text", "clo $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CLO, actualOpcode);
  }

  @Test
  public void testClz() {
    String[] instructions = {".text", "clz $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CLZ, actualOpcode);
  }

  @Test
  public void testCmp_af_d() {
    String[] instructions = {".text", "cmp.af.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_AF_D, actualOpcode);
  }

  @Test
  public void testCmp_af_s() {
    String[] instructions = {".text", "cmp.af.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_AF_S, actualOpcode);
  }

  @Test
  public void testCmp_at_d() {
    String[] instructions = {".text", "cmp.at.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_AT_D, actualOpcode);
  }

  @Test
  public void testCmp_at_s() {
    String[] instructions = {".text", "cmp.at.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_AT_S, actualOpcode);
  }

  @Test
  public void testCmp_un_d() {
    String[] instructions = {".text", "cmp.un.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_UN_D, actualOpcode);
  }

  @Test
  public void testCmp_un_s() {
    String[] instructions = {".text", "cmp.un.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_UN_S, actualOpcode);
  }

  @Test
  public void testCmp_eq_d() {
    String[] instructions = {".text", "cmp.eq.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_EQ_D, actualOpcode);
  }

  @Test
  public void testCmp_eq_s() {
    String[] instructions = {".text", "cmp.eq.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_EQ_S, actualOpcode);
  }

  @Test
  public void testCmp_ueq_d() {
    String[] instructions = {".text", "cmp.ueq.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_UEQ_D, actualOpcode);
  }

  @Test
  public void testCmp_ueq_s() {
    String[] instructions = {".text", "cmp.ueq.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_UEQ_S, actualOpcode);
  }

  @Test
  public void testCmp_lt_d() {
    String[] instructions = {".text", "cmp.lt.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_LT_D, actualOpcode);
  }

  @Test
  public void testCmp_lt_s() {
    String[] instructions = {".text", "cmp.lt.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_LT_S, actualOpcode);
  }

  @Test
  public void testCmp_ult_d() {
    String[] instructions = {".text", "cmp.ult.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_ULT_D, actualOpcode);
  }

  @Test
  public void testCmp_ult_s() {
    String[] instructions = {".text", "cmp.ult.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_ULT_S, actualOpcode);
  }

  @Test
  public void testCmp_le_d() {
    String[] instructions = {".text", "cmp.le.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_LE_D, actualOpcode);
  }

  @Test
  public void testCmp_le_s() {
    String[] instructions = {".text", "cmp.le.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_LE_S, actualOpcode);
  }

  @Test
  public void testCmp_ule_d() {
    String[] instructions = {".text", "cmp.ule.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_ULE_D, actualOpcode);
  }

  @Test
  public void testCmp_ule_s() {
    String[] instructions = {".text", "cmp.ule.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_ULE_S, actualOpcode);
  }

  @Test
  public void testCmp_saf_d() {
    String[] instructions = {".text", "cmp.saf.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SAF_D, actualOpcode);
  }

  @Test
  public void testCmp_saf_s() {
    String[] instructions = {".text", "cmp.saf.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SAF_S, actualOpcode);
  }

  @Test
  public void testCmp_sun_d() {
    String[] instructions = {".text", "cmp.sun.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SUN_D, actualOpcode);
  }

  @Test
  public void testCmp_sun_s() {
    String[] instructions = {".text", "cmp.sun.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SUN_S, actualOpcode);
  }

  @Test
  public void testCmp_seq_d() {
    String[] instructions = {".text", "cmp.seq.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SEQ_D, actualOpcode);
  }

  @Test
  public void testCmp_seq_s() {
    String[] instructions = {".text", "cmp.seq.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SEQ_S, actualOpcode);
  }

  @Test
  public void testCmp_sueq_d() {
    String[] instructions = {".text", "cmp.sueq.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SUEQ_D, actualOpcode);
  }

  @Test
  public void testCmp_sueq_s() {
    String[] instructions = {".text", "cmp.sueq.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SUEQ_S, actualOpcode);
  }

  @Test
  public void testCmp_slt_d() {
    String[] instructions = {".text", "cmp.slt.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SLT_D, actualOpcode);
  }

  @Test
  public void testCmp_slt_s() {
    String[] instructions = {".text", "cmp.slt.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SLT_S, actualOpcode);
  }

  @Test
  public void testCmp_sult_d() {
    String[] instructions = {".text", "cmp.sult.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SULT_D, actualOpcode);
  }

  @Test
  public void testCmp_sult_s() {
    String[] instructions = {".text", "cmp.sult.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SULT_S, actualOpcode);
  }

  @Test
  public void testCmp_sle_d() {
    String[] instructions = {".text", "cmp.sle.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SLE_D, actualOpcode);
  }

  @Test
  public void testCmp_sle_s() {
    String[] instructions = {".text", "cmp.sle.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SLE_S, actualOpcode);
  }

  @Test
  public void testCmp_sule_d() {
    String[] instructions = {".text", "cmp.sule.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SULE_D, actualOpcode);
  }

  @Test
  public void testCmp_sule_s() {
    String[] instructions = {".text", "cmp.sule.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SULE_S, actualOpcode);
  }

  @Test
  public void testCmp_or_d() {
    String[] instructions = {".text", "cmp.or.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_OR_D, actualOpcode);
  }

  @Test
  public void testCmp_or_s() {
    String[] instructions = {".text", "cmp.or.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_OR_S, actualOpcode);
  }

  @Test
  public void testCmp_une_d() {
    String[] instructions = {".text", "cmp.une.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_UNE_D, actualOpcode);
  }

  @Test
  public void testCmp_une_s() {
    String[] instructions = {".text", "cmp.une.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_UNE_S, actualOpcode);
  }

  @Test
  public void testCmp_ne_d() {
    String[] instructions = {".text", "cmp.ne.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_NE_D, actualOpcode);
  }

  @Test
  public void testCmp_ne_s() {
    String[] instructions = {".text", "cmp.ne.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_NE_S, actualOpcode);
  }

  @Test
  public void testCmp_uge_d() {
    String[] instructions = {".text", "cmp.uge.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_UGE_D, actualOpcode);
  }

  @Test
  public void testCmp_uge_s() {
    String[] instructions = {".text", "cmp.uge.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_UGE_S, actualOpcode);
  }

  @Test
  public void testCmp_oge_d() {
    String[] instructions = {".text", "cmp.oge.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_OGE_D, actualOpcode);
  }

  @Test
  public void testCmp_oge_s() {
    String[] instructions = {".text", "cmp.oge.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_OGE_S, actualOpcode);
  }

  @Test
  public void testCmp_ugt_d() {
    String[] instructions = {".text", "cmp.ugt.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_UGT_D, actualOpcode);
  }

  @Test
  public void testCmp_ugt_s() {
    String[] instructions = {".text", "cmp.ugt.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_UGT_S, actualOpcode);
  }

  @Test
  public void testCmp_ogt_d() {
    String[] instructions = {".text", "cmp.ogt.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_OGT_D, actualOpcode);
  }

  @Test
  public void testCmp_ogt_s() {
    String[] instructions = {".text", "cmp.ogt.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_OGT_S, actualOpcode);
  }

  @Test
  public void testCmp_sat_d() {
    String[] instructions = {".text", "cmp.sat.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SAT_D, actualOpcode);
  }

  @Test
  public void testCmp_sat_s() {
    String[] instructions = {".text", "cmp.sat.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SAT_S, actualOpcode);
  }

  @Test
  public void testCmp_sor_d() {
    String[] instructions = {".text", "cmp.sor.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SOR_D, actualOpcode);
  }

  @Test
  public void testCmp_sor_s() {
    String[] instructions = {".text", "cmp.sor.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SOR_S, actualOpcode);
  }

  @Test
  public void testCmp_sune_d() {
    String[] instructions = {".text", "cmp.sune.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SUNE_D, actualOpcode);
  }

  @Test
  public void testCmp_sune_s() {
    String[] instructions = {".text", "cmp.sune.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SUNE_S, actualOpcode);
  }

  @Test
  public void testCmp_sne_d() {
    String[] instructions = {".text", "cmp.sne.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SNE_D, actualOpcode);
  }

  @Test
  public void testCmp_sne_s() {
    String[] instructions = {".text", "cmp.sne.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SNE_S, actualOpcode);
  }

  @Test
  public void testCmp_suge_d() {
    String[] instructions = {".text", "cmp.suge.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SUGE_D, actualOpcode);
  }

  @Test
  public void testCmp_suge_s() {
    String[] instructions = {".text", "cmp.suge.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SUGE_S, actualOpcode);
  }

  @Test
  public void testCmp_soge_d() {
    String[] instructions = {".text", "cmp.soge.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SOGE_D, actualOpcode);
  }

  @Test
  public void testCmp_soge_s() {
    String[] instructions = {".text", "cmp.soge.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SOGE_S, actualOpcode);
  }

  @Test
  public void testCmp_sugt_d() {
    String[] instructions = {".text", "cmp.sugt.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SUGT_D, actualOpcode);
  }

  @Test
  public void testCmp_sugt_s() {
    String[] instructions = {".text", "cmp.sugt.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SUGT_S, actualOpcode);
  }

  @Test
  public void testCmp_sogt_d() {
    String[] instructions = {".text", "cmp.sogt.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SOGT_D, actualOpcode);
  }

  @Test
  public void testCmp_sogt_s() {
    String[] instructions = {".text", "cmp.sogt.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CMP_SOGT_S, actualOpcode);
  }

  @Test
  public void testCop2() {
    String[] instructions = {".text", "cop2 6"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.COP2, actualOpcode);
  }

  @Test
  public void testCrc32b() {
    String[] instructions = {".text", "crc32b $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CRC32B, actualOpcode);
  }

  @Test
  public void testCrc32h() {
    String[] instructions = {".text", "crc32h $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CRC32H, actualOpcode);
  }

  @Test
  public void testCrc32w() {
    String[] instructions = {".text", "crc32w $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CRC32W, actualOpcode);
  }

  @Test
  public void testCrc32cb() {
    String[] instructions = {".text", "crc32cb $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CRC32CB, actualOpcode);
  }

  @Test
  public void testCrc32ch() {
    String[] instructions = {".text", "crc32ch $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CRC32CH, actualOpcode);
  }

  @Test
  public void testCrc32cw() {
    String[] instructions = {".text", "crc32cw $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CRC32CW, actualOpcode);
  }

  @Test
  public void testCtc1() {
    String[] instructions = {".text", "ctc1 $t0, $f1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CTC1, actualOpcode);
  }

  @Test
  public void testCtc2() {
    String[] instructions = {".text", "ctc2 $t0, 3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CTC2, actualOpcode);
  }

  @Test
  public void testCvt_d_l() {
    String[] instructions = {".text", "cvt.d.l $f1, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CVT_D_L, actualOpcode);
  }

  @Test
  public void testCvt_d_s() {
    String[] instructions = {".text", "cvt.d.s $f1, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CVT_D_S, actualOpcode);
  }

  @Test
  public void testCvt_d_w() {
    String[] instructions = {".text", "cvt.d.w $f1, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CVT_D_W, actualOpcode);
  }

  @Test
  public void testCvt_l_d() {
    String[] instructions = {".text", "cvt.l.d $f1, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CVT_L_D, actualOpcode);
  }

  @Test
  public void testCvt_l_s() {
    String[] instructions = {".text", "cvt.l.s $f1, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CVT_L_S, actualOpcode);
  }

  @Test
  public void testCvt_s_d() {
    String[] instructions = {".text", "cvt.s.d $f1, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CVT_S_D, actualOpcode);
  }

  @Test
  public void testCvt_s_l() {
    String[] instructions = {".text", "cvt.s.l $f1, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CVT_S_L, actualOpcode);
  }

  @Test
  public void testCvt_s_w() {
    String[] instructions = {".text", "cvt.s.w $f1, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CVT_S_W, actualOpcode);
  }

  @Test
  public void testCvt_w_d() {
    String[] instructions = {".text", "cvt.w.d $f1, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CVT_W_D, actualOpcode);
  }

  @Test
  public void testCvt_w_s() {
    String[] instructions = {".text", "cvt.w.s $f1, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.CVT_W_S, actualOpcode);
  }

  @Test
  public void testDeret() {
    String[] instructions = {".text", "deret"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.DERET, actualOpcode);
  }

  @Test
  public void testDi() {
    String[] instructions = {".text", "di $t0"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.DI, actualOpcode);
  }

  @Test
  public void testDiv() {
    String[] instructions = {".text", "div $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.DIV, actualOpcode);
  }

  @Test
  public void testDiv_d() {
    String[] instructions = {".text", "div.d $f3, $f2, $f1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.DIV_D, actualOpcode);
  }

  @Test
  public void testDiv_s() {
    String[] instructions = {".text", "div.s $f3, $f2, $f1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.DIV_S, actualOpcode);
  }

  @Test
  public void testDivu() {
    String[] instructions = {".text", "divu $t2, $t1, $t0"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.DIVU, actualOpcode);
  }

  @Test
  public void testDvp() {
    String[] instructions = {".text", "dvp $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.DVP, actualOpcode);
  }

  @Test
  public void testEi() {
    String[] instructions = {".text", "ei $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.EI, actualOpcode);
  }

  @Test
  public void testEret() {
    String[] instructions = {".text", "eret $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.ERET, actualOpcode);
  }

  @Test
  public void testEretnc() {
    String[] instructions = {".text", "eretnc $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.ERETNC, actualOpcode);
  }

  @Test
  public void testEvp() {
    String[] instructions = {".text", "evp $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.EVP, actualOpcode);
  }

  @Test
  public void testExt() {
    String[] instructions = {".text", "ext $t2, $t1, 4, 8"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.EXT, actualOpcode);
  }

  @Test
  public void testFloor_l_d() {
    String[] instructions = {".text", "floor.l.d $f2, $f1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.FLOOR_L_D, actualOpcode);
  }

  @Test
  public void testFloor_l_s() {
    String[] instructions = {".text", "floor.l.s $f2, $f1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.FLOOR_L_S, actualOpcode);
  }

  @Test
  public void testFloor_w_d() {
    String[] instructions = {".text", "floor.w.d $f2, $f1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.FLOOR_W_D, actualOpcode);
  }

  @Test
  public void testFloor_w_s() {
    String[] instructions = {".text", "floor.w.s $f2, $f1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.FLOOR_W_S, actualOpcode);
  }

  @Test
  public void testGinvi() {
    String[] instructions = {".text", "ginvi $t0"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.GINVI, actualOpcode);
  }

  @Test
  public void testGinvt() {
    String[] instructions = {".text", "ginvt $t0, 3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.GINVT, actualOpcode);
  }

  @Test
  public void testIns() {
    String[] instructions = {".text", "ins $t2, $t1, 4, 8"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.INS, actualOpcode);
  }

  @Test
  public void testJ() {
    String[] instructions = {".text", "j label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.J, actualOpcode);
  }

  @Test
  public void testJal() {
    String[] instructions = {".text", "jal label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.JAL, actualOpcode);
  }

  @Test
  public void testJalr() {
    String[] instructions = {".text", "jalr $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.JALR, actualOpcode);
  }

  @Test
  public void testJalrhb() {
    String[] instructions = {".text", "jalr.hb $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.JALR_HB, actualOpcode);
  }

  @Test
  public void testJic() {
    String[] instructions = {".text", "jic $t0, label", "label: la $t1, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.JIC, actualOpcode);
  }

  @Test
  public void testJialc() {
    String[] instructions = {".text", "jialc $t0, label", "label: la $t1, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.JIALC, actualOpcode);
  }

  @Test
  public void testJr() {
    String[] instructions = {".text", "jr $t0"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.JR, actualOpcode);
  }

  @Test
  public void testJrhb() {
    String[] instructions = {".text", "jr.hb $t0"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.JR_HB, actualOpcode);
  }

  @Test
  public void testlb() {
    String[] instructions = {".text", "lb $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.LB, actualOpcode);
  }

  @Test
  public void testlbe() {
    String[] instructions = {".text", "lbe $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.LBE, actualOpcode);
  }

  @Test
  public void testlbu() {
    String[] instructions = {".text", "lbu $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.LBU, actualOpcode);
  }

  @Test
  public void testlbue() {
    String[] instructions = {".text", "lbue $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.LBUE, actualOpcode);
  }

  @Test
  public void testldc1() {
    String[] instructions = {".text", "ldc1 $f1, 4($t0)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.LDC1, actualOpcode);
  }

  @Test
  public void testldc2() {
    String[] instructions = {".text", "ldc2 $t1, 4($t0)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.LDC2, actualOpcode);
  }

  @Test
  public void testlh() {
    String[] instructions = {".text", "lh $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.LH, actualOpcode);
  }

  @Test
  public void testlhe() {
    String[] instructions = {".text", "lhe $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.LHE, actualOpcode);
  }


  @Test
  public void testlhu() {
    String[] instructions = {".text", "lhu $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.LHU, actualOpcode);
  }

  @Test
  public void testlhue() {
    String[] instructions = {".text", "lhue $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.LHUE, actualOpcode);
  }

  @Test
  public void testll() {
    String[] instructions = {".text", "ll $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.LL, actualOpcode);
  }

  @Test
  public void testlle() {
    String[] instructions = {".text", "lle $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.LLE, actualOpcode);
  }

  @Test
  public void testllwp() {
    String[] instructions = {".text", "llwp $t0, $t2, ($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.LLWP, actualOpcode);
  }

  @Test
  public void testllwpe() {
    String[] instructions = {".text", "llwpe $t0, $t2, ($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.LLWPE, actualOpcode);
  }

  @Test
  public void testlsa() {
    String[] instructions = {".text", "lsa $t0, $t2, $t1, 2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.LSA, actualOpcode);
  }

  @Test
  public void testlui() {
    String[] instructions = {".text", "lui $t0, 4"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.AUI, actualOpcode);
  }

  @Test
  public void testlw() {
    String[] instructions = {".text", "lw $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.LW, actualOpcode);
  }

  @Test
  public void testlwc1() {
    String[] instructions = {".text", "lwc1 $f1, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.LWC1, actualOpcode);
  }

  @Test
  public void testlwc2() {
    String[] instructions = {".text", "lwc2 $t1, 4($t0)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.LWC2, actualOpcode);
  }

  @Test
  public void testlwe() {
    String[] instructions = {".text", "lwe $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.LWE, actualOpcode);
  }

  @Test
  public void testlwl() {
    String[] instructions = {".text", "lwl $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.LWL, actualOpcode);
  }

  @Test
  public void testlwpc() {
    String[] instructions = {".text", "lwpc $t0, 4"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.LWPC, actualOpcode);
  }

  @Test
  public void testlwr() {
    String[] instructions = {".text", "lwr $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.LWR, actualOpcode);
  }

  @Test
  public void testmadd() {
    String[] instructions = {".text", "madd $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MADD, actualOpcode);
  }

  @Test
  public void testmaddf_d() {
    String[] instructions = {".text", "maddf.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MADDF_D, actualOpcode);
  }

  @Test
  public void testmaddf_s() {
    String[] instructions = {".text", "maddf.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MADDF_S, actualOpcode);
  }

  @Test
  public void testmaddu() {
    String[] instructions = {".text", "maddu $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MADDU, actualOpcode);
  }

  @Test
  public void testmax_d() {
    String[] instructions = {".text", "max.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MAX_D, actualOpcode);
  }

  @Test
  public void testmax_s() {
    String[] instructions = {".text", "max.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MAX_S, actualOpcode);
  }

  @Test
  public void testmaxa_d() {
    String[] instructions = {".text", "maxa.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MAXA_D, actualOpcode);
  }

  @Test
  public void testmaxa_s() {
    String[] instructions = {".text", "maxa.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MAXA_S, actualOpcode);
  }

  @Test
  public void testmfc0() {
    String[] instructions = {".text", "mfc0 $t0, $t1, 3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MFC0, actualOpcode);
  }

  @Test
  public void testmfc1() {
    String[] instructions = {".text", "mfc1 $t0, $f1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MFC1, actualOpcode);
  }

  @Test
  public void testmfc2() {
    String[] instructions = {".text", "mfc2 $t0, 1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MFC2, actualOpcode);
  }

  @Test
  public void testmfhc0() {
    String[] instructions = {".text", "mfhc0 $t0, $t1, 3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MFHC0, actualOpcode);
  }

  @Test
  public void testmfhc1() {
    String[] instructions = {".text", "mfhc1 $t0, $f1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MFHC1, actualOpcode);
  }

  @Test
  public void testmfhc2() {
    String[] instructions = {".text", "mfhc2 $t0, 1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MFHC2, actualOpcode);
  }

  @Test
  public void testmfhi() {
    String[] instructions = {".text", "mfhi $t0"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MFHI, actualOpcode);
  }

  @Test
  public void testmflo() {
    String[] instructions = {".text", "mflo $t0"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MFLO, actualOpcode);
  }

  @Test
  public void testmin_d() {
    String[] instructions = {".text", "min.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MIN_D, actualOpcode);
  }

  @Test
  public void testmin_s() {
    String[] instructions = {".text", "min.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MIN_S, actualOpcode);
  }

  @Test
  public void testmina_d() {
    String[] instructions = {".text", "mina.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MINA_D, actualOpcode);
  }

  @Test
  public void testmina_s() {
    String[] instructions = {".text", "mina.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MINA_S, actualOpcode);
  }

  @Test
  public void testmod() {
    String[] instructions = {".text", "mod $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MOD, actualOpcode);
  }

  @Test
  public void testmodu() {
    String[] instructions = {".text", "modu $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MODU, actualOpcode);
  }

  @Test
  public void testmovd() {
    String[] instructions = {".text", "mov.d $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MOV_D, actualOpcode);
  }

  @Test
  public void testmovs() {
    String[] instructions = {".text", "mov.s $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MOV_S, actualOpcode);
  }

  @Test
  public void testmovn() {
    String[] instructions = {".text", "movn $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MOVN, actualOpcode);
  }

  @Test
  public void testmovz() {
    String[] instructions = {".text", "movz $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MOVZ, actualOpcode);
  }

  @Test
  public void testmsubf_d() {
    String[] instructions = {".text", "msubf.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MSUBF_D, actualOpcode);
  }

  @Test
  public void testmsubf_s() {
    String[] instructions = {".text", "msubf.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MSUBF_S, actualOpcode);
  }

  @Test
  public void testmsub() {
    String[] instructions = {".text", "msub $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MSUB, actualOpcode);
  }

  @Test
  public void testmsubu() {
    String[] instructions = {".text", "msubu $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MSUBU, actualOpcode);
  }

  @Test
  public void testmtc0() {
    String[] instructions = {".text", "mtc0 $t0, $t1, 3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MTC0, actualOpcode);
  }

  @Test
  public void testmtc1() {
    String[] instructions = {".text", "mtc1 $t0, $f1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MTC1, actualOpcode);
  }

  @Test
  public void testmtc2() {
    String[] instructions = {".text", "mtc2 $t0, 1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MTC2, actualOpcode);
  }

  @Test
  public void testmthc0() {
    String[] instructions = {".text", "mthc0 $t0, $t1, 3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MTHC0, actualOpcode);
  }

  @Test
  public void testmthc1() {
    String[] instructions = {".text", "mthc1 $t0, $f1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MTHC1, actualOpcode);
  }

  @Test
  public void testmthc2() {
    String[] instructions = {".text", "mthc2 $t0, 1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MTHC2, actualOpcode);
  }

  @Test
  public void testmthi() {
    String[] instructions = {".text", "mthi $t0"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MTHI, actualOpcode);
  }

  @Test
  public void testmtlo() {
    String[] instructions = {".text", "mtlo $t0"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MTLO, actualOpcode);
  }

  @Test
  public void testmuh() {
    String[] instructions = {".text", "muh $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MUH, actualOpcode);
  }

  @Test
  public void testmuhu() {
    String[] instructions = {".text", "muhu $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MUHU, actualOpcode);
  }

  @Test
  public void testmul() {
    String[] instructions = {".text", "mul $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MUL, actualOpcode);
  }

  @Test
  public void testmulu() {
    String[] instructions = {".text", "mulu $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MULU, actualOpcode);
  }

  @Test
  public void testmuld() {
    String[] instructions = {".text", "mul.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MUL_D, actualOpcode);
  }

  @Test
  public void testmuls() {
    String[] instructions = {".text", "mul.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MUL_S, actualOpcode);
  }

  @Test
  public void testmult() {
    String[] instructions = {".text", "mult $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MULT, actualOpcode);
  }

  @Test
  public void testmultu() {
    String[] instructions = {".text", "multu $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.MULTU, actualOpcode);
  }

  @Test
  public void testnal() {
    String[] instructions = {".text", "nal $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.NAL, actualOpcode);
  }

  @Test
  public void testnegd() {
    String[] instructions = {".text", "neg.d $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.NEG_D, actualOpcode);
  }

  @Test
  public void testnegs() {
    String[] instructions = {".text", "neg.s $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.NEG_S, actualOpcode);
  }

  @Test
  public void testnor() {
    String[] instructions = {".text", "nor $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.NOR, actualOpcode);
  }

  @Test
  public void testor() {
    String[] instructions = {".text", "or $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.OR, actualOpcode);
  }

  @Test
  public void testori() {
    String[] instructions = {".text", "ori $t0, $t1, 5"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.ORI, actualOpcode);
  }

  @Test
  public void testpause() {
    String[] instructions = {".text", "pause"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.PAUSE, actualOpcode);
  }

  @Test
  public void testpref() {
    String[] instructions = {".text", "pref 3, 1($t0)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.PREF, actualOpcode);
  }

  @Test
  public void testprefe() {
    String[] instructions = {".text", "prefe 3, 1($t0)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.PREFE, actualOpcode);
  }

  @Test
  public void testrdhwr() {
    String[] instructions = {".text", "rdhwr $t0, $t1, 1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.RDHWR, actualOpcode);
  }

  @Test
  public void testrdpgpr() {
    String[] instructions = {".text", "rdpgpr $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.RDPGPR, actualOpcode);
  }

  @Test
  public void testrecipd() {
    String[] instructions = {".text", "recip.d $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.RECIP_D, actualOpcode);
  }

  @Test
  public void testrecips() {
    String[] instructions = {".text", "recip.s $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.RECIP_S, actualOpcode);
  }

  @Test
  public void testrintd() {
    String[] instructions = {".text", "rint.d $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.RINT_D, actualOpcode);
  }

  @Test
  public void testrints() {
    String[] instructions = {".text", "rint.s $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.RINT_S, actualOpcode);
  }

  @Test
  public void testrotr() {
    String[] instructions = {".text", "rotr $t0, $t1, 1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.ROTR, actualOpcode);
  }

  @Test
  public void testrotrv() {
    String[] instructions = {".text", "rotrv $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.ROTRV, actualOpcode);
  }

  @Test
  public void testroundld() {
    String[] instructions = {".text", "round.l.d $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.ROUND_L_D, actualOpcode);
  }

  @Test
  public void testroundls() {
    String[] instructions = {".text", "round.l.s $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.ROUND_L_S, actualOpcode);
  }

  @Test
  public void testroundwd() {
    String[] instructions = {".text", "round.w.d $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.ROUND_W_D, actualOpcode);
  }

  @Test
  public void testroundws() {
    String[] instructions = {".text", "round.w.s $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.ROUND_W_S, actualOpcode);
  }

  @Test
  public void testrsqrtd() {
    String[] instructions = {".text", "rsqrt.d $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.RSQRT_D, actualOpcode);
  }

  @Test
  public void testrsqrts() {
    String[] instructions = {".text", "rsqrt.s $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.RSQRT_S, actualOpcode);
  }

  @Test
  public void testsb() {
    String[] instructions = {".text", "sb $t0, 3($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SB, actualOpcode);
  }

  @Test
  public void testsbe() {
    String[] instructions = {".text", "sbe $t0, 3($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SBE, actualOpcode);
  }

  @Test
  public void testsc() {
    String[] instructions = {".text", "sc $t0, 3($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SC, actualOpcode);
  }

  @Test
  public void testsce() {
    String[] instructions = {".text", "sce $t0, 3($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SCE, actualOpcode);
  }

  @Test
  public void testscwp() {
    String[] instructions = {".text", "scwp $t0, $t2, ($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SCWP, actualOpcode);
  }

  @Test
  public void testscwpe() {
    String[] instructions = {".text", "scwpe $t0, $t2, ($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SCWPE, actualOpcode);
  }

  @Test
  public void testsdc1() {
    String[] instructions = {".text", "sdc1 $f1, 3($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SDC1, actualOpcode);
  }

  @Test
  public void testsdbbp() {
    String[] instructions = {".text", "sdbbp 3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SDBBP, actualOpcode);
  }

  @Test
  public void testseb() {
    String[] instructions = {".text", "seb $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SEB, actualOpcode);
  }

  @Test
  public void testseh() {
    String[] instructions = {".text", "seh $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SEH, actualOpcode);
  }

  @Test
  public void testseld() {
    String[] instructions = {".text", "sel.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SEL_D, actualOpcode);
  }

  @Test
  public void testsels() {
    String[] instructions = {".text", "sel.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SEL_S, actualOpcode);
  }

  @Test
  public void testseleqz() {
    String[] instructions = {".text", "seleqz $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SELEQZ, actualOpcode);
  }

  @Test
  public void testselnez() {
    String[] instructions = {".text", "selnez $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SELNEZ, actualOpcode);
  }

  @Test
  public void testseleqzd() {
    String[] instructions = {".text", "seleqz.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SELEQZ_D, actualOpcode);
  }

  @Test
  public void testseleqzs() {
    String[] instructions = {".text", "seleqz.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SELEQZ_S, actualOpcode);
  }


  @Test
  public void testselnezd() {
    String[] instructions = {".text", "selnez.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SELNEZ_D, actualOpcode);
  }

  @Test
  public void testselnezs() {
    String[] instructions = {".text", "selnez.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SELNEZ_S, actualOpcode);
  }

  @Test
  public void testsh() {
    String[] instructions = {".text", "sh $t0, 3($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SH, actualOpcode);
  }

  @Test
  public void testshe() {
    String[] instructions = {".text", "she $t0, 3($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SHE, actualOpcode);
  }

  @Test
  public void testsigrie() {
    String[] instructions = {".text", "sigrie 3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SIGRIE, actualOpcode);
  }

  @Test
  public void testsll() {
    String[] instructions = {".text", "sll $t0, $t1, 3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SLL, actualOpcode);
  }

  @Test
  public void testsllv() {
    String[] instructions = {".text", "sllv $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SLLV, actualOpcode);
  }

  @Test
  public void testslt() {
    String[] instructions = {".text", "slt $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SLT, actualOpcode);
  }

  @Test
  public void testslti() {
    String[] instructions = {".text", "slti $t0, $t1, 3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SLTI, actualOpcode);
  }

  @Test
  public void testsltiu() {
    String[] instructions = {".text", "sltiu $t0, $t1, 3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SLTIU, actualOpcode);
  }

  @Test
  public void testsltu() {
    String[] instructions = {".text", "sltu $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SLTU, actualOpcode);
  }

  @Test
  public void testsra() {
    String[] instructions = {".text", "sra $t0, $t1, 2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SRA, actualOpcode);
  }

  @Test
  public void testsrav() {
    String[] instructions = {".text", "srav $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SRAV, actualOpcode);
  }

  @Test
  public void testsrl() {
    String[] instructions = {".text", "srl $t0, $t1, 2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SRL, actualOpcode);
  }

  @Test
  public void testsrlv() {
    String[] instructions = {".text", "srlv $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SRLV, actualOpcode);
  }

  @Test
  public void testssnop() {
    String[] instructions = {".text", "ssnop"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SSNOP, actualOpcode);
  }

  @Test
  public void testsqrtd() {
    String[] instructions = {".text", "sqrt.d $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SQRT_D, actualOpcode);
  }

  @Test
  public void testsqrts() {
    String[] instructions = {".text", "sqrt.s $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SQRT_S, actualOpcode);
  }

  @Test
  public void testsub() {
    String[] instructions = {".text", "sub $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SUB, actualOpcode);
  }

  @Test
  public void testsubd() {
    String[] instructions = {".text", "sub.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SUB_D, actualOpcode);
  }

  @Test
  public void testsubs() {
    String[] instructions = {".text", "sub.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SUB_S, actualOpcode);
  }

  @Test
  public void testsubu() {
    String[] instructions = {".text", "subu $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SUBU, actualOpcode);
  }

  @Test
  public void testsw() {
    String[] instructions = {".text", "sw $t0, 3($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SW, actualOpcode);
  }

  @Test
  public void testswc1() {
    String[] instructions = {".text", "swc1 $f1, 3($t0)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SWC1, actualOpcode);
  }

  @Test
  public void testswc2() {
    String[] instructions = {".text", "swc2 $t0, 3($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SWC2, actualOpcode);
  }

  @Test
  public void testswe() {
    String[] instructions = {".text", "swe $t0, 3($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SWE, actualOpcode);
  }

  @Test
  public void testswl() {
    String[] instructions = {".text", "swl $t0, 3($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SWL, actualOpcode);
  }

  @Test
  public void testswr() {
    String[] instructions = {".text", "swr $t0, 3($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SWR, actualOpcode);
  }

  @Test
  public void testsync() {
    String[] instructions = {".text", "sync 1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SYNC, actualOpcode);
  }

  @Test
  public void testsynci() {
    String[] instructions = {".text", "synci 3($t0)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SYNCI, actualOpcode);
  }

  @Test
  public void testsyscall() {
    String[] instructions = {".text", "syscall"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.SYSCALL, actualOpcode);
  }

  @Test
  public void testteq() {
    String[] instructions = {".text", "teq $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.TEQ, actualOpcode);
  }

  @Test
  public void testtge() {
    String[] instructions = {".text", "tge $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.TGE, actualOpcode);
  }

  @Test
  public void testtlbinv() {
    String[] instructions = {".text", "tlbinv"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.TLBINV, actualOpcode);
  }

  @Test
  public void testtlbinvf() {
    String[] instructions = {".text", "tlbinvf"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.TLBINVF, actualOpcode);
  }

  @Test
  public void testtlbp() {
    String[] instructions = {".text", "tlbp"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.TLBP, actualOpcode);
  }

  @Test
  public void testtlbr() {
    String[] instructions = {".text", "tlbr"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.TLBR, actualOpcode);
  }

  @Test
  public void testtlbwi() {
    String[] instructions = {".text", "tlbwi"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.TLBWI, actualOpcode);
  }

  @Test
  public void testtlbwr() {
    String[] instructions = {".text", "tlbwr"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.TLBWR, actualOpcode);
  }

  @Test
  public void testtlt() {
    String[] instructions = {".text", "tlt $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.TLT, actualOpcode);
  }

  @Test
  public void testtltu() {
    String[] instructions = {".text", "tltu $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.TLTU, actualOpcode);
  }

  @Test
  public void testtne() {
    String[] instructions = {".text", "tne $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.TNE, actualOpcode);
  }

  @Test
  public void testtruncld() {
    String[] instructions = {".text", "trunc.l.d $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.TRUNC_L_D, actualOpcode);
  }

  @Test
  public void testtruncls() {
    String[] instructions = {".text", "trunc.l.s $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.TRUNC_L_S, actualOpcode);
  }

  @Test
  public void testtruncwd() {
    String[] instructions = {".text", "trunc.w.d $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.TRUNC_W_D, actualOpcode);
  }

  @Test
  public void testtruncws() {
    String[] instructions = {".text", "trunc.w.s $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.TRUNC_W_S, actualOpcode);
  }

  @Test
  public void testwait() {
    String[] instructions = {".text", "wait"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.WAIT, actualOpcode);
  }

  @Test
  public void testwrpgpr() {
    String[] instructions = {".text", "wrpgpr $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.WRPGPR, actualOpcode);
  }

  @Test
  public void testwsbh() {
    String[] instructions = {".text", "wsbh $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.WSBH, actualOpcode);
  }

  @Test
  public void testxor() {
    String[] instructions = {".text", "xor $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.XOR, actualOpcode);
  }

  @Test
  public void testxori() {
    String[] instructions = {".text", "xori $t0, $t1, 3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = assembler.getLayout();

    Opcode actualOpcode = MipsInstructionDecoder.decode(layout.readWord(assembler.getTextOffset()));
    assertEquals(Opcode.XORI, actualOpcode);
  }

  public String toLineDelimited(String[] array) {
    StringBuilder builder = new StringBuilder();
    for (String ins : array) {
      builder.append(ins).append('\n');
    }

    return builder.toString();
  }

}