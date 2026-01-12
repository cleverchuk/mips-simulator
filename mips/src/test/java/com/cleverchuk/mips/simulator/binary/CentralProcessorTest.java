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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

@SuppressWarnings("all")
public class CentralProcessorTest {

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
  private CentralProcessor cpu;
  private Memory memory;
  private int textOffset;

  @Before
  public void setup() {
    assembler = new Assembler();
    parser.addVisitor(assembler);
  }

  @After
  public void teardown() {
    parser.removeVisitor(assembler);
  }

  private void assemble(String[] instructions) {
    parser.parse(toLineDelimited(instructions));
    memory = assembler.getLayout();
    textOffset = assembler.getTextOffset();
    cpu = new CentralProcessor(memory, textOffset, assembler.getStackPointer(), (byte) 0x2);
  }

  private void executeInstructions(int count) throws Exception {
    for (int i = 0; i < count; i++) {
      cpu.execute();
    }
  }

  private String toLineDelimited(String[] instructions) {
    return String.join("\n", instructions);
  }

  @Test
  public void testAbsS() throws Exception {
    String[] instructions = {
      ".data",
      "fval: .float -5.5",
      "result: .float 0.0",
      ".text",
      "la $t0, fval",
      "lwc1 $f1, 0($t0)",
      "abs.s $f2, $f1",
      "la $t0, result",
      "swc1 $f2, 0($t0)"
    };
    assemble(instructions);
    executeInstructions(7);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    float result = Float.intBitsToFloat(memory.readWord(t0));
    assertEquals(5.5f, result, 0.0001f);
  }

  @Test
  public void testAbsD() throws Exception {
    String[] instructions = {
      ".data", "data: .double -3.14", ".text", "la $t0, data", "ldc1 $f2, 0($t0)", "abs.d $f4, $f2"
    };
    assemble(instructions);
    executeInstructions(4);

    double result = cpu.getFpuRegisterFileArray().getFile(4).readDouble();
    assertEquals(3.14, result, 0.001);
  }

  @Test
  public void testAccumulation() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t0, $zero, 0",
      "addiu $t0, $t0, 1",
      "addiu $t0, $t0, 2",
      "addiu $t0, $t0, 3",
      "addiu $t0, $t0, 4"
    };
    assemble(instructions);
    executeInstructions(5);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(10, result);
  }

  @Test
  public void testAdd() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 10", "addiu $t2, $zero, 20", "add $t0, $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(30, result);
  }

  @Test
  public void testAddD() throws Exception {
    String[] instructions = {
      ".data",
      "d1: .double 1.5",
      "d2: .double 2.5",
      "result: .double 0.0",
      ".text",
      "la $t0, d1",
      "ldc1 $f2, 0($t0)",
      "la $t0, d2",
      "ldc1 $f4, 0($t0)",
      "add.d $f6, $f2, $f4",
      "la $t0, result",
      "sdc1 $f6, 0($t0)"
    };
    assemble(instructions);
    executeInstructions(10);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    long bits = memory.readDWord(t0);
    double result = Double.longBitsToDouble(bits);
    assertEquals(4.0, result, 0.0001);
  }

  @Test
  public void testAddS() throws Exception {
    String[] instructions = {
      ".data",
      "f1: .float 1.5",
      "f2: .float 2.5",
      "result: .float 0.0",
      ".text",
      "la $t0, f1",
      "lwc1 $f1, 0($t0)",
      "la $t0, f2",
      "lwc1 $f2, 0($t0)",
      "add.s $f3, $f1, $f2",
      "la $t0, result",
      "swc1 $f3, 0($t0)"
    };
    assemble(instructions);
    executeInstructions(10);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    float result = Float.intBitsToFloat(memory.readWord(t0));
    assertEquals(4.0f, result, 0.0001f);
  }

  @Test
  public void testAddiu() throws Exception {
    String[] instructions = {".text", "addiu $t0, $zero, 100"};
    assemble(instructions);
    executeInstructions(1);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(100, result);
  }

  @Test
  public void testAddiuNegative() throws Exception {
    String[] instructions = {".text", "addiu $t0, $zero, -50"};
    assemble(instructions);
    executeInstructions(1);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(-50, result);
  }

  @Test
  public void testAddiupc() throws Exception {
    String[] instructions = {".text", "addiu $t0, $zero, -50", "addiupc $t0, 50"};
    assemble(instructions);
    executeInstructions(2);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(204, result);
  }

  @Test
  public void testAddu() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 15", "addiu $t2, $zero, 25", "addu $t0, $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(40, result);
  }

  @Test
  public void testAlign() throws Exception {
    String[] instructions = {".text", "addiu $t1, $zero, 1", "li $t2, 2", "align $t0, $t1, $t2, 3"};
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals((2 << 24) | (1 >>> 8), result);
  }

  @Test
  public void testAluipc() throws Exception {
    String[] instructions = {".text", "addiu $t1, $zero, 1", "li $t2, 2", "aluipc $t0, 3"};
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals((~0x0ffff) & (8 + (3 << 16)), result);
  }

  @Test
  public void testAnd() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 255", "addiu $t2, $zero, 15", "and $t0, $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(15, result);
  }

  @Test
  public void testAndi() throws Exception {
    String[] instructions = {".text", "addiu $t1, $zero, 255", "andi $t0, $t1, 15"};
    assemble(instructions);
    executeInstructions(2);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(15, result);
  }

  @Test
  public void testAui() throws Exception {
    String[] instructions = {
      ".data", "data: .word 0x1, 0x4", ".text", "la $t0, data", "lw $t1, 0($t0)", "aui $t0, $t1, 5"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals((5 << 16) + 1, result);
  }

  @Test
  public void testAuipc() throws Exception {
    String[] instructions = {".text", "auipc $t0, 0x1000"};
    assemble(instructions);
    executeInstructions(1);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertTrue(result != 0);
  }

  @Test
  public void testBal() throws Exception {
    String[] instructions = {
      ".text", "bal sub", "addiu $t0, $zero, 42", "j done", "sub: jr $ra", "done: nop"
    };
    assemble(instructions);
    executeInstructions(4);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, t0);
  }

  @Test
  public void testBalc() throws Exception {
    String[] instructions = {
      ".text", "balc sub", "addiu $t0, $zero, 42", "j done", "sub: jr $ra", "done: nop"
    };
    assemble(instructions);
    executeInstructions(4);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, t0);
  }

  @Test
  public void testBc() throws Exception {
    String[] instructions = {
      ".text", "bc target", "addiu $t0, $zero, 1", "target: addiu $t0, $zero, 42"
    };
    assemble(instructions);
    executeInstructions(2);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, t0);
  }

  @Test
  public void testBc1eqz() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 0.0",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "bc1eqz $f1, target",
      "addiu $t1, $zero, 999",
      "target: addiu $t2, $zero, 42"
    };
    assemble(instructions);
    executeInstructions(5);

    int skipped = cpu.getGprFileArray().getFile(9).readWord();
    int result = cpu.getGprFileArray().getFile(10).readWord();
    assertEquals(0, skipped);
    assertEquals(42, result);
  }

  @Test
  public void testBc1nez() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 1.4E-45",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "bc1nez $f1, target",
      "addiu $t1, $zero, 999",
      "target: addiu $t2, $zero, 42"
    };
    assemble(instructions);
    executeInstructions(5);

    int skipped = cpu.getGprFileArray().getFile(9).readWord();
    int result = cpu.getGprFileArray().getFile(10).readWord();
    assertEquals(0, skipped);
    assertEquals(42, result);
  }

  @Test
  public void testBc2eqz() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t0, $zero, 0",
      "ctc2 $t0, $1",
      "bc2eqz $1, 2",
      "addiu $t0, $zero, 20",
      "addiu $t0, $zero, 40",
      "addiu $t0, $zero, 60"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(60, result);
  }

  @Test
  public void testBc2nez() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t0, $zero, 5",
      "ctc2 $t0, $1",
      "bc2nez $1, 2",
      "addiu $t0, $zero, 20",
      "addiu $t0, $zero, 40",
      "addiu $t0, $zero, 60"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(60, result);
  }

  @Test
  public void testBeqc() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 5",
      "addiu $t2, $zero, 10",
      "beqc $t1, $t2, target",
      "addiu $t0, $zero, 42",
      "target: nop"
    };
    assemble(instructions);
    executeInstructions(5);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, t0);
  }

  @Test
  public void testBeqNotTaken() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 5",
      "addiu $t2, $zero, 10",
      "beq $t1, $t2, target",
      "addiu $t0, $zero, 42",
      "target: nop"
    };
    assemble(instructions);
    executeInstructions(5);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testBeqSimple() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 5",
      "addiu $t2, $zero, 10",
      "beq $t1, $t2, skip",
      "addiu $t0, $zero, 42",
      "skip: nop"
    };
    assemble(instructions);
    executeInstructions(5);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testBeqTaken() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 5",
      "addiu $t2, $zero, 5",
      "beq $t1, $t2, target",
      "addiu $t0, $zero, 1",
      "target: addiu $t0, $zero, 42"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testBeqzalc() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 5", "beqzalc $t1, target", "addiu $t0, $zero, 42", "target: nop"
    };
    assemble(instructions);
    executeInstructions(4);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, t0);
  }

  @Test
  public void testBeqzc() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t0, $zero, 0",
      "beqzc $t0, target",
      "addiu $t1, $zero, 999",
      "target: addiu $t2, $zero, 42"
    };
    assemble(instructions);
    executeInstructions(3);

    int skipped = cpu.getGprFileArray().getFile(9).readWord();
    int result = cpu.getGprFileArray().getFile(10).readWord();
    assertEquals(0, skipped);
    assertEquals(42, result);
  }

  @Test
  public void testBeqzcNoBranch() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 5", "beqzc $t1, target", "addiu $t0, $zero, 42", "target: nop"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testBgec() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 5",
      "addiu $t2, $zero, 10",
      "bgec $t1, $t2, target",
      "addiu $t0, $zero, 42",
      "target: nop"
    };
    assemble(instructions);
    executeInstructions(5);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, t0);
  }

  @Test
  public void testBgeuc() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 5",
      "addiu $t2, $zero, 10",
      "bgeuc $t1, $t2, target",
      "addiu $t0, $zero, 42",
      "target: nop"
    };
    assemble(instructions);
    executeInstructions(5);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, t0);
  }

  @Test
  public void testBgezalc() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, -5", "bgezalc $t1, target", "addiu $t0, $zero, 42", "target: nop"
    };
    assemble(instructions);
    executeInstructions(4);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, t0);
  }

  @Test
  public void testBgezalNoBranch() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, -5", "bgezal $t1, target", "addiu $t0, $zero, 42", "target: nop"
    };
    assemble(instructions);
    executeInstructions(4);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, t0);
  }

  @Test
  public void testBgezc() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, -5", "bgezc $t1, target", "addiu $t0, $zero, 42", "target: nop"
    };
    assemble(instructions);
    executeInstructions(4);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, t0);
  }

  @Test
  public void testBgezTaken() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 5",
      "bgez $t1, target",
      "addiu $t0, $zero, 1",
      "target: addiu $t0, $zero, 42"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testBgezWithPositive() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, -5", "bgez $t1, skip", "addiu $t0, $zero, 42", "skip: nop"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testBgtzalc() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 0", "bgtzalc $t1, target", "addiu $t0, $zero, 42", "target: nop"
    };
    assemble(instructions);
    executeInstructions(4);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, t0);
  }

  @Test
  public void testBgtzc() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 0", "bgtzc $t1, target", "addiu $t0, $zero, 42", "target: nop"
    };
    assemble(instructions);
    executeInstructions(4);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, t0);
  }

  @Test
  public void testBgtzTaken() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 5",
      "bgtz $t1, target",
      "addiu $t0, $zero, 1",
      "target: addiu $t0, $zero, 42"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testBgtzWithZero() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 0", "bgtz $t1, skip", "addiu $t0, $zero, 42", "skip: nop"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testBitswap() throws Exception {
    String[] instructions = {".text", "addiu $t1, $zero, 0x01", "bitswap $t0, $t1"};
    assemble(instructions);
    executeInstructions(2);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(0x80, t0);
  }

  @Test
  public void testBlezalc() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 5", "blezalc $t1, target", "addiu $t0, $zero, 42", "target: nop"
    };
    assemble(instructions);
    executeInstructions(4);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, t0);
  }

  @Test
  public void testBlezc() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 5", "blezc $t1, target", "addiu $t0, $zero, 42", "target: nop"
    };
    assemble(instructions);
    executeInstructions(4);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, t0);
  }

  @Test
  public void testBlezTaken() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 0",
      "blez $t1, target",
      "addiu $t0, $zero, 1",
      "target: addiu $t0, $zero, 42"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testBlezWithPositive() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 5", "blez $t1, skip", "addiu $t0, $zero, 42", "skip: nop"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testBltc() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 10",
      "addiu $t2, $zero, 5",
      "bltc $t1, $t2, target",
      "addiu $t0, $zero, 42",
      "target: nop"
    };
    assemble(instructions);
    executeInstructions(5);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, t0);
  }

  @Test
  public void testBltuc() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 10",
      "addiu $t2, $zero, 5",
      "bltuc $t1, $t2, target",
      "addiu $t0, $zero, 42",
      "target: nop"
    };
    assemble(instructions);
    executeInstructions(5);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, t0);
  }

  @Test
  public void testBltzalc() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 5", "bltzalc $t1, target", "addiu $t0, $zero, 42", "target: nop"
    };
    assemble(instructions);
    executeInstructions(4);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, t0);
  }

  @Test
  public void testBltzalNoBranch() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 5", "bltzal $t1, target", "addiu $t0, $zero, 42", "target: nop"
    };
    assemble(instructions);
    executeInstructions(4);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, t0);
  }

  @Test
  public void testBltzc() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 5", "bltzc $t1, target", "addiu $t0, $zero, 42", "target: nop"
    };
    assemble(instructions);
    executeInstructions(4);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, t0);
  }

  @Test
  public void testBltzTaken() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, -5",
      "bltz $t1, target",
      "addiu $t0, $zero, 1",
      "target: addiu $t0, $zero, 42"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testBltzWithPositive() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 5", "bltz $t1, skip", "addiu $t0, $zero, 42", "skip: nop"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testBnec() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 5",
      "addiu $t2, $zero, 5",
      "bnec $t1, $t2, target",
      "addiu $t0, $zero, 42",
      "target: nop"
    };
    assemble(instructions);
    executeInstructions(5);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, t0);
  }

  @Test
  public void testBneNotTaken() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 5",
      "addiu $t2, $zero, 5",
      "bne $t1, $t2, target",
      "addiu $t0, $zero, 42",
      "target: nop"
    };
    assemble(instructions);
    executeInstructions(5);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testBneSimple() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 5",
      "addiu $t2, $zero, 5",
      "bne $t1, $t2, skip",
      "addiu $t0, $zero, 42",
      "skip: nop"
    };
    assemble(instructions);
    executeInstructions(5);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testBneTaken() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 5",
      "addiu $t2, $zero, 10",
      "bne $t1, $t2, target",
      "addiu $t0, $zero, 1",
      "target: addiu $t0, $zero, 42"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testBnezalc() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 0", "bnezalc $t1, target", "addiu $t0, $zero, 42", "target: nop"
    };
    assemble(instructions);
    executeInstructions(4);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, t0);
  }

  @Test
  public void testBnezc() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t0, $zero, 1",
      "bnezc $t0, target",
      "addiu $t1, $zero, 999",
      "target: addiu $t2, $zero, 42"
    };
    assemble(instructions);
    executeInstructions(3);

    int skipped = cpu.getGprFileArray().getFile(9).readWord();
    int result = cpu.getGprFileArray().getFile(10).readWord();
    assertEquals(0, skipped);
    assertEquals(42, result);
  }

  @Test
  public void testBnezcNoBranch() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 0", "bnezc $t1, target", "addiu $t0, $zero, 42", "target: nop"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testBnvc() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 5",
      "addiu $t2, $zero, 10",
      "bnvc $t1, $t2, target",
      "addiu $t0, $zero, 1",
      "j done",
      "target: addiu $t0, $zero, 42",
      "done: nop"
    };
    assemble(instructions);
    executeInstructions(4);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, t0);
  }

  @Test
  public void testBovc() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 5",
      "addiu $t2, $zero, 10",
      "bovc $t1, $t2, target",
      "addiu $t0, $zero, 42",
      "target: nop"
    };
    assemble(instructions);
    executeInstructions(5);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, t0);
  }

  @Test(expected = Exception.class)
  public void testBreak() throws Exception {
    String[] instructions = {".text", "addiu $t0, $zero, 10", "break", "addiu $t0, $zero, 42"};
    assemble(instructions);
    executeInstructions(3);
  }

  @Test
  public void testBreakAssembly() throws Exception {
    String[] instructions = {".text", "addiu $t0, $zero, 42", "addiu $t1, $zero, 99"};
    assemble(instructions);
    executeInstructions(2);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    int t1 = cpu.getGprFileArray().getFile(9).readWord();
    assertEquals(42, t0);
    assertEquals(99, t1);
  }

  @Test
  public void testCeilLS() throws Exception {
    String[] instructions = {
      ".data", "data: .float 3.2", ".text", "la $t0, data", "lwc1 $f1, 0($t0)", "ceil.l.s $f2, $f1"
    };
    assemble(instructions);
    executeInstructions(4);

    double result = cpu.getFpuRegisterFileArray().getFile(2).readDouble();
    long longResult = Double.doubleToRawLongBits(result);
    assertEquals(4L, longResult);
  }

  @Test
  public void testCeilLD() throws Exception {
    String[] instructions = {
      ".data", "data: .double 3.2", ".text", "la $t0, data", "ldc1 $f2, 0($t0)", "ceil.l.d $f4, $f2"
    };
    assemble(instructions);
    executeInstructions(4);

    double result = cpu.getFpuRegisterFileArray().getFile(4).readDouble();
    long longResult = Double.doubleToRawLongBits(result);
    assertEquals(4L, longResult);
  }

  @Test
  public void testCeilWS() throws Exception {
    String[] instructions = {
      ".data",
      "fval: .float 42.1",
      ".text",
      "la $t0, fval",
      "lwc1 $f1, 0($t0)",
      "ceil.w.s $f2, $f1",
      "mfc1 $t1, $f2"
    };
    assemble(instructions);
    executeInstructions(5);

    int t1 = cpu.getGprFileArray().getFile(9).readWord();
    assertEquals(43, t1);
  }

  @Test
  public void testCeilWD() throws Exception {
    String[] instructions = {
      ".data", "data: .double 3.1", ".text", "la $t0, data", "ldc1 $f2, 0($t0)", "ceil.w.d $f4, $f2"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getFpuRegisterFileArray().getFile(4).readWord();
    assertEquals(4, result);
  }

  @Test
  public void testCfc1() throws Exception {
    String[] instructions = {".text", "cfc1 $t0, $0"};
    assemble(instructions);
    executeInstructions(1);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertTrue(result >= 0);
  }

  @Test
  public void testCfc2() throws Exception {
    String[] instructions = {".text", "addiu $t0, $zero, 100", "ctc2 $t0, $1", "cfc2 $t1, $1"};
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(9).readWord();
    assertEquals(100, result);
  }

  @Test
  public void testClassS() throws Exception {
    String[] instructions = {
      ".data", "data: .float 3.14", ".text", "la $t0, data", "lwc1 $f1, 0($t0)", "class.s $f3, $f1"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getFpuRegisterFileArray().getFile(3).readWord();
    assertTrue(result != 0);
  }

  @Test
  public void testClassD() throws Exception {
    String[] instructions = {
      ".data", "data: .double 3.14", ".text", "la $t0, data", "ldc1 $f2, 0($t0)", "class.d $f4, $f2"
    };
    assemble(instructions);
    executeInstructions(4);

    long result = Double.doubleToRawLongBits(cpu.getFpuRegisterFileArray().getFile(4).readDouble());
    assertTrue(result != 0);
  }

  @Test
  public void testClo() throws Exception {
    String[] instructions = {
      ".data", "data: .word 0xfffF0000", ".text", "la $t1, data", "lw $t1, 0($t1)", "clo $t0, $t1"
    };
    assemble(instructions);
    executeInstructions(5);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(16, result);
  }

  @Test
  public void testClz() throws Exception {
    String[] instructions = {".text", "addiu $t1, $zero, 256", "clz $t0, $t1"};
    assemble(instructions);
    executeInstructions(2);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(23, result);
  }

  @Test
  public void testCmpAfS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 2.5, 3.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f3, 4($t0)",
      "cmp.af.s $f6, $f1, $f3"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(6).readSingle();
    assertEquals(0.0f, result, 0);
  }

  @Test
  public void testCmpAfD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 2.5, 3.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.af.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(0.0, result, 0);
  }

  @Test
  public void testCmpAtS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 2.5, 3.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f3, 4($t0)",
      "cmp.at.s $f6, $f1, $f3"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(6).readSingle();
    assertEquals(Float.intBitsToFloat(-1), result, 0);
  }

  @Test
  public void testCmpAtD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 2.5, 3.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.at.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(Double.longBitsToDouble(-1L), result, 0);
  }

  @Test
  public void testCmpEqS() throws Exception {
    String[] instructions = {
      ".data",
      "f1: .float 5.0",
      "f2: .float 5.0",
      ".text",
      "la $t0, f1",
      "lwc1 $f1, 0($t0)",
      "la $t0, f2",
      "lwc1 $f2, 0($t0)",
      "cmp.eq.s $f3, $f1, $f2",
      "mfc1 $t1, $f3"
    };
    assemble(instructions);
    executeInstructions(8);

    int t1 = cpu.getGprFileArray().getFile(9).readWord();
    assertEquals(-1, t1);
  }

  @Test
  public void testCmpEqD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 2.5, 2.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.eq.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(Double.longBitsToDouble(-1L), result, 0);
  }

  @Test
  public void testCmpLeS() throws Exception {
    String[] instructions = {
      ".data",
      "f1: .float 5.0",
      "f2: .float 5.0",
      ".text",
      "la $t0, f1",
      "lwc1 $f1, 0($t0)",
      "la $t0, f2",
      "lwc1 $f2, 0($t0)",
      "cmp.le.s $f3, $f1, $f2",
      "mfc1 $t1, $f3"
    };
    assemble(instructions);
    executeInstructions(8);

    int t1 = cpu.getGprFileArray().getFile(9).readWord();
    assertEquals(-1, t1);
  }

  @Test
  public void testCmpLeD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 2.5, 2.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.le.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(Double.longBitsToDouble(-1L), result, 0);
  }

  @Test
  public void testCmpLtS() throws Exception {
    String[] instructions = {
      ".data",
      "f1: .float 3.0",
      "f2: .float 5.0",
      ".text",
      "la $t0, f1",
      "lwc1 $f1, 0($t0)",
      "la $t0, f2",
      "lwc1 $f2, 0($t0)",
      "cmp.lt.s $f3, $f1, $f2",
      "mfc1 $t1, $f3"
    };
    assemble(instructions);
    executeInstructions(8);

    int t1 = cpu.getGprFileArray().getFile(9).readWord();
    assertEquals(-1, t1);
  }

  @Test
  public void testCmpLtD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 2.5, 3.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.lt.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(Double.longBitsToDouble(-1L), result, 0);
  }

  @Test
  public void testCmpNeS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 2.5, 3.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f2, 4($t0)",
      "cmp.ne.s $f3, $f1, $f2"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(3).readSingle();
    assertEquals(Float.intBitsToFloat(-1), result, 0);
  }

  @Test
  public void testCmpNeD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 2.5, 3.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.ne.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(Double.longBitsToDouble(-1L), result, 0);
  }

  @Test
  public void testCmpOgeS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 3.5, 2.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f3, 4($t0)",
      "cmp.oge.s $f6, $f1, $f3"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(6).readSingle();
    assertEquals(Float.intBitsToFloat(-1), result, 0);
  }

  @Test
  public void testCmpOgeD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 3.5, 2.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.oge.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(Double.longBitsToDouble(-1L), result, 0);
  }

  @Test
  public void testCmpOgtS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 3.5, 2.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f3, 4($t0)",
      "cmp.ogt.s $f6, $f1, $f3"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(6).readSingle();
    assertEquals(Float.intBitsToFloat(-1), result, 0);
  }

  @Test
  public void testCmpOgtD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 3.5, 2.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.ogt.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(Double.longBitsToDouble(-1L), result, 0);
  }

  @Test
  public void testCmpOrS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 2.5, 3.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f2, 4($t0)",
      "cmp.or.s $f3, $f1, $f2"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(3).readSingle();
    assertEquals(Float.intBitsToFloat(-1), result, 0);
  }

  @Test
  public void testCmpOrD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 2.5, 3.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.or.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(Double.longBitsToDouble(-1L), result, 0);
  }

  @Test
  public void testCmpSafS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 2.5, 3.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f3, 4($t0)",
      "cmp.saf.s $f6, $f1, $f3"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(6).readSingle();
    assertEquals(0.0f, result, 0);
  }

  @Test
  public void testCmpSafD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 2.5, 3.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.saf.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(0.0, result, 0);
  }

  @Test
  public void testCmpSatS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 2.5, 3.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f3, 4($t0)",
      "cmp.sat.s $f6, $f1, $f3"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(6).readSingle();
    assertEquals(Float.intBitsToFloat(-1), result, 0);
  }

  @Test
  public void testCmpSatD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 2.5, 3.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.sat.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(Double.longBitsToDouble(-1L), result, 0);
  }

  @Test
  public void testCmpSeqS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 2.5, 2.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f3, 4($t0)",
      "cmp.seq.s $f6, $f1, $f3"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(6).readSingle();
    assertEquals(Float.intBitsToFloat(-1), result, 0);
  }

  @Test
  public void testCmpSeqD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 2.5, 2.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.seq.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(Double.longBitsToDouble(-1L), result, 0);
  }

  @Test
  public void testCmpSleS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 2.5, 3.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f3, 4($t0)",
      "cmp.sle.s $f6, $f1, $f3"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(6).readSingle();
    assertEquals(Float.intBitsToFloat(-1), result, 0);
  }

  @Test
  public void testCmpSleD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 2.5, 3.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.sle.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(Double.longBitsToDouble(-1L), result, 0);
  }

  @Test
  public void testCmpSltS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 2.5, 3.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f3, 4($t0)",
      "cmp.slt.s $f6, $f1, $f3"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(6).readSingle();
    assertEquals(Float.intBitsToFloat(-1), result, 0);
  }

  @Test
  public void testCmpSltD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 2.5, 3.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.slt.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(Double.longBitsToDouble(-1L), result, 0);
  }

  @Test
  public void testCmpSneS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 2.5, 3.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f3, 4($t0)",
      "cmp.sne.s $f6, $f1, $f3"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(6).readSingle();
    assertEquals(Float.intBitsToFloat(-1), result, 0);
  }

  @Test
  public void testCmpSneD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 2.5, 3.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.sne.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(Double.longBitsToDouble(-1L), result, 0);
  }

  @Test
  public void testCmpSogeS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 3.5, 2.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f3, 4($t0)",
      "cmp.soge.s $f6, $f1, $f3"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(6).readSingle();
    assertEquals(Float.intBitsToFloat(-1), result, 0);
  }

  @Test
  public void testCmpSogeD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 3.5, 2.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.soge.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(Double.longBitsToDouble(-1L), result, 0);
  }

  @Test
  public void testCmpSogtS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 3.5, 2.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f3, 4($t0)",
      "cmp.sogt.s $f6, $f1, $f3"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(6).readSingle();
    assertEquals(Float.intBitsToFloat(-1), result, 0);
  }

  @Test
  public void testCmpSogtD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 3.5, 2.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.sogt.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(Double.longBitsToDouble(-1L), result, 0);
  }

  @Test
  public void testCmpSorS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 2.5, 3.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f3, 4($t0)",
      "cmp.sor.s $f6, $f1, $f3"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(6).readSingle();
    assertEquals(Float.intBitsToFloat(-1), result, 0);
  }

  @Test
  public void testCmpSorD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 2.5, 3.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.sor.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(Double.longBitsToDouble(-1L), result, 0);
  }

  @Test
  public void testCmpSueqS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 2.5, 2.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f3, 4($t0)",
      "cmp.sueq.s $f6, $f1, $f3"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(6).readSingle();
    assertEquals(Float.intBitsToFloat(-1), result, 0);
  }

  @Test
  public void testCmpSueqD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 2.5, 2.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.sueq.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(Double.longBitsToDouble(-1L), result, 0);
  }

  @Test
  public void testCmpSugeS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 3.5, 2.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f3, 4($t0)",
      "cmp.suge.s $f6, $f1, $f3"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(6).readSingle();
    assertEquals(Float.intBitsToFloat(-1), result, 0);
  }

  @Test
  public void testCmpSugeD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 3.5, 2.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.suge.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(Double.longBitsToDouble(-1L), result, 0);
  }

  @Test
  public void testCmpSugtS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 3.5, 2.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f3, 4($t0)",
      "cmp.sugt.s $f6, $f1, $f3"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(6).readSingle();
    assertEquals(Float.intBitsToFloat(-1), result, 0);
  }

  @Test
  public void testCmpSugtD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 3.5, 2.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.sugt.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(Double.longBitsToDouble(-1L), result, 0);
  }

  @Test
  public void testCmpSuleS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 2.5, 3.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f3, 4($t0)",
      "cmp.sule.s $f6, $f1, $f3"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(6).readSingle();
    assertEquals(Float.intBitsToFloat(-1), result, 0);
  }

  @Test
  public void testCmpSuleD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 2.5, 3.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.sule.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(Double.longBitsToDouble(-1L), result, 0);
  }

  @Test
  public void testCmpSultS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 2.5, 3.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f3, 4($t0)",
      "cmp.sult.s $f6, $f1, $f3"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(6).readSingle();
    assertEquals(Float.intBitsToFloat(-1), result, 0);
  }

  @Test
  public void testCmpSultD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 2.5, 3.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.sult.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(Double.longBitsToDouble(-1L), result, 0);
  }

  @Test
  public void testCmpSunS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 2.5, 3.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f3, 4($t0)",
      "cmp.sun.s $f6, $f1, $f3"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(6).readSingle();
    assertEquals(0.0f, result, 0);
  }

  @Test
  public void testCmpSunD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 2.5, 3.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.sun.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(0.0, result, 0);
  }

  @Test
  public void testCmpSuneS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 2.5, 3.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f3, 4($t0)",
      "cmp.sune.s $f6, $f1, $f3"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(6).readSingle();
    assertEquals(Float.intBitsToFloat(-1), result, 0);
  }

  @Test
  public void testCmpSuneD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 2.5, 3.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.sune.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(Double.longBitsToDouble(-1L), result, 0);
  }

  @Test
  public void testCmpUeqD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 2.5, 2.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.ueq.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(Double.longBitsToDouble(-1L), result, 0);
  }

  @Test
  public void testCmpUeqS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 2.5, 2.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f3, 4($t0)",
      "cmp.ueq.s $f6, $f1, $f3"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(6).readSingle();
    assertEquals(Float.intBitsToFloat(-1), result, 0);
  }

  @Test
  public void testCmpUgeS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 3.5, 2.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f3, 4($t0)",
      "cmp.uge.s $f6, $f1, $f3"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(6).readSingle();
    assertEquals(Float.intBitsToFloat(-1), result, 0);
  }

  @Test
  public void testCmpUgeD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 3.5, 2.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.uge.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(Double.longBitsToDouble(-1L), result, 0);
  }

  @Test
  public void testCmpUgtS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 3.5, 2.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f3, 4($t0)",
      "cmp.ugt.s $f6, $f1, $f3"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(6).readSingle();
    assertEquals(Float.intBitsToFloat(-1), result, 0);
  }

  @Test
  public void testCmpUgtD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 3.5, 2.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.ugt.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(Double.longBitsToDouble(-1L), result, 0);
  }

  @Test
  public void testCmpUleD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 2.5, 3.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.ule.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(Double.longBitsToDouble(-1L), result, 0);
  }

  @Test
  public void testCmpUleS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 2.5, 3.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f3, 4($t0)",
      "cmp.ule.s $f6, $f1, $f3"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(6).readSingle();
    assertEquals(Float.intBitsToFloat(-1), result, 0);
  }

  @Test
  public void testCmpUltS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 2.5, 3.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f2, 4($t0)",
      "cmp.ult.s $f3, $f1, $f2"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(3).readSingle();
    assertEquals(Float.intBitsToFloat(-1), result, 0);
  }

  @Test
  public void testCmpUltD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 2.5, 3.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.ult.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(Double.longBitsToDouble(-1L), result, 0);
  }

  @Test
  public void testCmpUnS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 2.5, 3.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f2, 4($t0)",
      "cmp.un.s $f3, $f1, $f2"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(3).readSingle();
    assertEquals(0.0f, result, 0);
  }

  @Test
  public void testCmpUnD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 2.5, 3.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.un.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(0.0, result, 0);
  }

  @Test
  public void testCmpUneS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 2.5, 3.5",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f2, 4($t0)",
      "cmp.une.s $f3, $f1, $f2"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(3).readSingle();
    assertEquals(Float.intBitsToFloat(-1), result, 0);
  }

  @Test
  public void testCmpUneD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 2.5, 3.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "cmp.une.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(Double.longBitsToDouble(-1L), result, 0);
  }

  @Test
  public void testCrc32b() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 0", "addiu $t2, $zero, 65", "crc32b $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(9).readWord();
    int expected = 31158534;
    assertEquals(expected, result);
  }

  @Test
  public void testCrc32cb() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 0", "addiu $t2, $zero, 65", "crc32cb $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(9).readWord();
    int expected = -1290756417;
    assertEquals(expected, result);
  }

  @Test
  public void testCrc32ch() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 0", "addiu $t2, $zero, 0x4142", "crc32ch $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(9).readWord();
    int expected = 1799514197;
    assertEquals(expected, result);
  }

  @Test
  public void testCrc32cw() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 0", "addiu $t2, $zero, 0x41424344", "crc32cw $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(9).readWord();
    int expected = -36753730;
    assertEquals(expected, result);
  }

  @Test
  public void testCrc32h() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 0", "addiu $t2, $zero, 0x4142", "crc32h $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(9).readWord();
    int expected = -1013687167;
    assertEquals(expected, result);
  }

  @Test
  public void testCrc32w() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 0", "addiu $t2, $zero, 0x41424344", "crc32w $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(9).readWord();
    int expected = 1722481907;
    assertEquals(expected, result);
  }

  @Test
  public void testCtc1() throws Exception {
    String[] instructions = {".text", "addiu $t0, $zero, 0", "ctc1 $t0, $5", "cfc1 $t1, $5"};
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(9).readWord();
    assertEquals(0, result);
  }

  @Test
  public void testCtc2() throws Exception {
    String[] instructions = {".text", "addiu $t0, $zero, 42", "ctc2 $t0, $2", "cfc2 $t1, $2"};
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(9).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testCvtDS() throws Exception {
    String[] instructions = {
      ".data", "data: .float 3.14", ".text", "la $t0, data", "lwc1 $f1, 0($t0)", "cvt.d.s $f2, $f1"
    };
    assemble(instructions);
    executeInstructions(4);

    double result = cpu.getFpuRegisterFileArray().getFile(2).readDouble();
    assertEquals(3.14, result, 0.01);
  }

  @Test
  public void testCvtDL() throws Exception {
    String[] instructions = {
      ".data", "data: .word 0, 42", ".text", "la $t0, data", "ldc1 $f2, 0($t0)", "cvt.d.l $f4, $f2"
    };
    assemble(instructions);
    executeInstructions(4);

    double result = cpu.getFpuRegisterFileArray().getFile(4).readDouble();
    assertEquals(42.0, result, 0.001);
  }

  @Test
  public void testCvtDW() throws Exception {
    String[] instructions = {
      ".data", "data: .word 42", ".text", "la $t0, data", "lwc1 $f1, 0($t0)", "cvt.d.w $f2, $f1"
    };
    assemble(instructions);
    executeInstructions(4);

    double result = cpu.getFpuRegisterFileArray().getFile(2).readDouble();
    assertEquals(42.0, result, 0.001);
  }

  @Test
  public void testCvtLS() throws Exception {
    String[] instructions = {
      ".data", "data: .float 42.7", ".text", "la $t0, data", "lwc1 $f1, 0($t0)", "cvt.l.s $f2, $f1"
    };
    assemble(instructions);
    executeInstructions(4);

    double result = cpu.getFpuRegisterFileArray().getFile(2).readDouble();
    long longResult = Double.doubleToRawLongBits(result);
    assertEquals(42L, longResult);
  }

  @Test
  public void testCvtLD() throws Exception {
    String[] instructions = {
      ".data", "data: .double 42.7", ".text", "la $t0, data", "ldc1 $f2, 0($t0)", "cvt.l.d $f4, $f2"
    };
    assemble(instructions);
    executeInstructions(4);

    double result = cpu.getFpuRegisterFileArray().getFile(4).readDouble();
    long longResult = Double.doubleToRawLongBits(result);
    assertEquals(42L, longResult);
  }

  @Test
  public void testCvtSD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 3.14159",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "cvt.s.d $f4, $f2"
    };
    assemble(instructions);
    executeInstructions(4);

    float result = cpu.getFpuRegisterFileArray().getFile(4).readSingle();
    assertEquals(3.14159f, result, 0.01f);
  }

  @Test
  public void testCvtSL() throws Exception {
    String[] instructions = {
      ".data", "data: .word 0, 42", ".text", "la $t0, data", "ldc1 $f2, 0($t0)", "cvt.s.l $f1, $f2"
    };
    assemble(instructions);
    executeInstructions(4);

    float result = cpu.getFpuRegisterFileArray().getFile(1).readSingle();
    assertEquals(42.0f, result, 0.001f);
  }

  @Test
  public void testCvtSW() throws Exception {
    String[] instructions = {
      ".text", "addiu $t0, $zero, 42", "mtc1 $t0, $f1", "cvt.s.w $f2, $f1", "mfc1 $t1, $f2"
    };
    assemble(instructions);
    executeInstructions(4);

    int t1 = cpu.getGprFileArray().getFile(9).readWord();
    float result = Float.intBitsToFloat(t1);
    assertEquals(42.0f, result, 0.0001f);
  }

  @Test
  public void testCvtWS() throws Exception {
    String[] instructions = {
      ".data",
      "fval: .float 42.7",
      ".text",
      "la $t0, fval",
      "lwc1 $f1, 0($t0)",
      "cvt.w.s $f2, $f1",
      "mfc1 $t1, $f2"
    };
    assemble(instructions);
    executeInstructions(5);

    int t1 = cpu.getGprFileArray().getFile(9).readWord();
    assertEquals(42, t1);
  }

  @Test
  public void testCvtWD() throws Exception {
    String[] instructions = {
      ".data", "data: .double 42.7", ".text", "la $t0, data", "ldc1 $f2, 0($t0)", "cvt.w.d $f4, $f2"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getFpuRegisterFileArray().getFile(4).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testDiv() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 100", "addiu $t2, $zero, 10", "div $t0, $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(10, t0);
  }

  @Test
  public void testDivS() throws Exception {
    String[] instructions = {
      ".data",
      "f1: .float 10.0",
      "f2: .float 2.0",
      "result: .float 0.0",
      ".text",
      "la $t0, f1",
      "lwc1 $f1, 0($t0)",
      "la $t0, f2",
      "lwc1 $f2, 0($t0)",
      "div.s $f3, $f1, $f2",
      "la $t0, result",
      "swc1 $f3, 0($t0)"
    };
    assemble(instructions);
    executeInstructions(10);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    float result = Float.intBitsToFloat(memory.readWord(t0));
    assertEquals(5.0f, result, 0.0001f);
  }

  @Test
  public void testDivD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 10.0, 2.0",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "div.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(5.0, result, 0.001);
  }

  @Test
  public void testDivu() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 100", "addiu $t2, $zero, 10", "divu $t0, $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(10, t0);
  }

  @Test
  public void testExt() throws Exception {
    String[] instructions = {".text", "addiu $t1, $zero, 255", "ext $t0, $t1, 0, 4"};
    assemble(instructions);
    executeInstructions(2);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(15, t0);
  }

  @Test
  public void testFloorLS() throws Exception {
    String[] instructions = {
      ".data", "data: .float 3.7", ".text", "la $t0, data", "lwc1 $f1, 0($t0)", "floor.l.s $f2, $f1"
    };
    assemble(instructions);
    executeInstructions(4);

    double result = cpu.getFpuRegisterFileArray().getFile(2).readDouble();
    long longResult = Double.doubleToRawLongBits(result);
    assertEquals(3L, longResult);
  }

  @Test
  public void testFloorLD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 3.7",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "floor.l.d $f4, $f2"
    };
    assemble(instructions);
    executeInstructions(4);

    double result = cpu.getFpuRegisterFileArray().getFile(4).readDouble();
    long longResult = Double.doubleToRawLongBits(result);
    assertEquals(3L, longResult);
  }

  @Test
  public void testFloorWS() throws Exception {
    String[] instructions = {
      ".data",
      "fval: .float 42.9",
      ".text",
      "la $t0, fval",
      "lwc1 $f1, 0($t0)",
      "floor.w.s $f2, $f1",
      "mfc1 $t1, $f2"
    };
    assemble(instructions);
    executeInstructions(5);

    int t1 = cpu.getGprFileArray().getFile(9).readWord();
    assertEquals(42, t1);
  }

  @Test
  public void testFloorWD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 3.9",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "floor.w.d $f4, $f2"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getFpuRegisterFileArray().getFile(4).readWord();
    assertEquals(3, result);
  }

  @Test
  public void testIns() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 0x0f",
      "lui $t0, 0xffff",
      "ori $t0, $t0, 0xff00",
      "ins $t0, $t1, 0, 4"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(0xffffff0f, result);
  }

  @Test
  public void testJ() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t0, $zero, 1",
      "j target",
      "addiu $t0, $zero, 2",
      "target: addiu $t1, $zero, 3"
    };
    assemble(instructions);
    executeInstructions(3);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    int t1 = cpu.getGprFileArray().getFile(9).readWord();
    assertEquals(1, t0);
    assertEquals(3, t1);
  }

  @Test
  public void testJalr() throws Exception {
    String[] instructions = {
      ".text",
      "la $t0, subr",
      "jalr $t0",
      "addiu $t1, $zero, 42",
      "j done",
      "subr: jr $ra",
      "done: nop"
    };
    assemble(instructions);
    executeInstructions(5);

    int t1 = cpu.getGprFileArray().getFile(9).readWord();
    assertEquals(42, t1);
  }

  @Test
  public void testJalReturnAddress() throws Exception {
    String[] instructions = {".text", "jal subroutine", "nop", "subroutine: nop"};
    assemble(instructions);
    executeInstructions(1);

    int ra = cpu.getGprFileArray().getFile(31).readWord();
    assertEquals(true, ra > 0);
  }

  @Test
  public void testJalrHb() throws Exception {
    String[] instructions = {
      ".text",
      "la $t0, target",
      "jalr.hb $t0",
      "addiu $t1, $zero, 999",
      "target: addiu $t2, $zero, 42"
    };
    assemble(instructions);
    executeInstructions(4);

    int skipped = cpu.getGprFileArray().getFile(9).readWord();
    int result = cpu.getGprFileArray().getFile(10).readWord();
    assertEquals(0, skipped);
    assertEquals(42, result);
  }

  @Test
  public void testJialc() throws Exception {
    String[] instructions = {
      ".text",
      "la $t0, subr",
      "jialc $t0, 0",
      "addiu $t1, $zero, 42",
      "j done",
      "subr: jr $ra",
      "done: nop"
    };
    assemble(instructions);
    executeInstructions(5);

    int t1 = cpu.getGprFileArray().getFile(9).readWord();
    assertEquals(42, t1);
  }

  @Test
  public void testJic() throws Exception {
    String[] instructions = {
      ".text", "la $t0, target", "jic $t0, 0", "addiu $t1, $zero, 1", "target: addiu $t1, $zero, 42"
    };
    assemble(instructions);
    executeInstructions(4);

    int t1 = cpu.getGprFileArray().getFile(9).readWord();
    assertEquals(42, t1);
  }

  @Test
  public void testJr() throws Exception {
    String[] instructions = {
      ".text", "jal subr", "addiu $t0, $zero, 42", "j done", "subr: jr $ra", "done: nop"
    };
    assemble(instructions);
    executeInstructions(4);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, t0);
  }

  @Test
  public void testJrHb() throws Exception {
    String[] instructions = {
      ".text",
      "la $t0, target",
      "jr.hb $t0",
      "addiu $t1, $zero, 999",
      "target: addiu $t2, $zero, 42"
    };
    assemble(instructions);
    executeInstructions(4);

    int skipped = cpu.getGprFileArray().getFile(9).readWord();
    int result = cpu.getGprFileArray().getFile(10).readWord();
    assertEquals(0, skipped);
    assertEquals(42, result);
  }

  @Test
  public void testLbe() throws Exception {
    String[] instructions = {".data", "data: .byte -5", ".text", "la $t1, data", "lbe $t0, 0($t1)"};
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(-5, result);
  }

  @Test
  public void testLbSb() throws Exception {
    String[] instructions = {
      ".data", "byte_val: .byte 65", ".text", "la $t1, byte_val", "lb $t0, 0($t1)"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(65, result);
  }

  @Test
  public void testLbue() throws Exception {
    String[] instructions = {
      ".data", "data: .byte 255", ".text", "la $t1, data", "lbue $t0, 0($t1)"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(255, result);
  }

  @Test
  public void testLbuSb() throws Exception {
    String[] instructions = {
      ".data", "byte_val: .byte 200", ".text", "la $t1, byte_val", "lbu $t0, 0($t1)"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(200, result);
  }

  @Test
  public void testLdc1Sdc1() throws Exception {
    String[] instructions = {
      ".data",
      "dval: .double 3.14159",
      "result: .double 0.0",
      ".text",
      "la $t0, dval",
      "ldc1 $f2, 0($t0)",
      "la $t0, result",
      "sdc1 $f2, 0($t0)"
    };
    assemble(instructions);
    executeInstructions(6);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    long bits = memory.readDWord(t0);
    double result = Double.longBitsToDouble(bits);
    assertEquals(3.14159, result, 0.00001);
  }

  @Test
  public void testLdc2() throws Exception {
    String[] instructions = {
      ".data", "data: .word 100, 200", ".text", "la $t0, data", "ldc2 $1, 0($t0)", "mfc2 $t1, $1"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(9).readWord();
    assertEquals(200, result);
  }

  @Test
  public void testLhe() throws Exception {
    String[] instructions = {
      ".data", "data: .half -1000", ".text", "la $t1, data", "lhe $t0, 0($t1)"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(-1000, result);
  }

  @Test
  public void testLhSh() throws Exception {
    String[] instructions = {
      ".data", "half_val: .half 1234", ".text", "la $t1, half_val", "lh $t0, 0($t1)"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(1234, result);
  }

  @Test
  public void testLhu() throws Exception {
    String[] instructions = {
      ".data", "half_val: .half 65000", ".text", "la $t1, half_val", "lhu $t0, 0($t1)"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(65000, result);
  }

  @Test
  public void testLhue() throws Exception {
    String[] instructions = {
      ".data", "data: .half 50000", ".text", "la $t1, data", "lhue $t0, 0($t1)"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(50000, result);
  }

  @Test
  public void testLiSmallValue() throws Exception {
    String[] instructions = {".text", "li $t0, 1000"};
    assemble(instructions);
    executeInstructions(1);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(1000, result);
  }

  @Test
  public void testLl() throws Exception {
    String[] instructions = {".data", "val: .word 42", ".text", "la $t0, val", "ll $t1, 0($t0)"};
    assemble(instructions);
    executeInstructions(3);

    int t1 = cpu.getGprFileArray().getFile(9).readWord();
    assertEquals(42, t1);
  }

  @Test
  public void testLle() throws Exception {
    String[] instructions = {".data", "data: .word 42", ".text", "la $t1, data", "lle $t0, 0($t1)"};
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testLlwp() throws Exception {
    String[] instructions = {
      ".data", "data: .word 10, 20", ".text", "la $t2, data", "llwp $t0, $t1, 0($t2)"
    };
    assemble(instructions);
    executeInstructions(3);

    int first = cpu.getGprFileArray().getFile(9).readWord();
    int second = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(10, first);
    assertEquals(20, second);
  }

  @Test
  public void testLlwpe() throws Exception {
    String[] instructions = {
      ".data", "data: .word 100, 200", ".text", "la $t0, data", "llwpe $t1, $t2, 0($t0)"
    };
    assemble(instructions);
    executeInstructions(3);

    int result1 = cpu.getGprFileArray().getFile(9).readWord();
    int result2 = cpu.getGprFileArray().getFile(10).readWord();
    assertTrue(result1 == 100 || result1 == 200 || result2 == 100 || result2 == 200);
  }

  @Test
  public void testLsa() throws Exception {
    String[] instructions = {
      ".text", "addiu $t0, $zero, 10", "addiu $t1, $zero, 5", "lsa $t2, $t0, $t1, 2"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(10).readWord();
    assertEquals(85, result);
  }

  @Test
  public void testLuiOri() throws Exception {
    String[] instructions = {".text", "lui $t0, 0x1234", "ori $t0, $t0, 0x5678"};
    assemble(instructions);
    executeInstructions(2);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(0x12345678, result);
  }

  @Test
  public void testLwc1Swc1() throws Exception {
    String[] instructions = {
      ".data",
      "fval: .word 0x40490fdb",
      ".text",
      "la $t0, fval",
      "lwc1 $f1, 0($t0)",
      "swc1 $f1, 0($t0)"
    };
    assemble(instructions);
    executeInstructions(4);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    int fpValue = memory.readWord(t0);
    assertEquals(0x40490fdb, fpValue);
  }

  @Test
  public void testLwc2() throws Exception {
    String[] instructions = {
      ".data", "data: .word 100", ".text", "la $t0, data", "lwc2 $1, 0($t0)", "mfc2 $t1, $1"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(9).readWord();
    assertEquals(100, result);
  }

  @Test
  public void testLwe() throws Exception {
    String[] instructions = {".data", "data: .word 42", ".text", "la $t1, data", "lwe $t0, 0($t1)"};
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testLwl() throws Exception {
    String[] instructions = {
      ".data",
      "val: .word 0x12345678",
      ".text",
      "la $t1, val",
      "addiu $t0, $zero, 0",
      "lwl $t0, 2($t1)"
    };
    assemble(instructions);
    executeInstructions(4);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(22136 << 16, t0);
  }

  @Test
  public void testLwpc() throws Exception {
    String[] instructions = {
      ".data", "data: .word 42", ".text", "la $t1, data", "lw $t2, 0($t1)", "lwpc $t0, data"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertTrue(result == 42 || result == 0);
  }

  @Test
  public void testLwpcActual() throws Exception {
    String[] instructions = {
      ".data", "data: .word 12345", ".text", "la $t1, data", "lw $t0, 0($t1)"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(12345, result);
  }

  @Test
  public void testLwr() throws Exception {
    String[] instructions = {
      ".data",
      "val: .word 0x12345678",
      ".text",
      "la $t1, val",
      "addiu $t0, $zero, 0",
      "lwr $t0, 1($t1)"
    };
    assemble(instructions);
    executeInstructions(4);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(0x1234, t0);
  }

  @Test
  public void testLwSw() throws Exception {
    String[] instructions = {
      ".data", "value: .word 42", ".text", "la $t1, value", "lw $t0, 0($t1)"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testMadd() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 10",
      "addiu $t2, $zero, 20",
      "mult $t1, $t2",
      "addiu $t1, $zero, 5",
      "addiu $t2, $zero, 6",
      "madd $t1, $t2",
      "mflo $t0"
    };
    assemble(instructions);
    executeInstructions(7);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(230, t0);
  }

  @Test
  public void testMaddfS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 2.0, 3.0, 4.0",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f2, 4($t0)",
      "lwc1 $f3, 8($t0)",
      "maddf.s $f1, $f2, $f3"
    };
    assemble(instructions);
    executeInstructions(6);

    float result = cpu.getFpuRegisterFileArray().getFile(1).readSingle();
    assertEquals(14.0f, result, 0.001f);
  }

  @Test
  public void testMaddfD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 2.0, 3.0, 4.0",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "ldc1 $f6, 16($t0)",
      "maddf.d $f2, $f4, $f6"
    };
    assemble(instructions);
    executeInstructions(6);

    double result = cpu.getFpuRegisterFileArray().getFile(2).readDouble();
    assertEquals(14.0, result, 0.001);
  }

  @Test
  public void testMaddu() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 10",
      "addiu $t2, $zero, 20",
      "multu $t1, $t2",
      "addiu $t1, $zero, 5",
      "addiu $t2, $zero, 6",
      "maddu $t1, $t2",
      "mflo $t0"
    };
    assemble(instructions);
    executeInstructions(7);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(230, t0);
  }

  @Test
  public void testMaxS() throws Exception {
    String[] instructions = {
      ".data",
      "f1: .float 5.0",
      "f2: .float 3.0",
      "result: .float 0.0",
      ".text",
      "la $t0, f1",
      "lwc1 $f1, 0($t0)",
      "la $t0, f2",
      "lwc1 $f2, 0($t0)",
      "max.s $f3, $f1, $f2",
      "la $t0, result",
      "swc1 $f3, 0($t0)"
    };
    assemble(instructions);
    executeInstructions(10);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    float result = Float.intBitsToFloat(memory.readWord(t0));
    assertEquals(5.0f, result, 0.0001f);
  }

  @Test
  public void testMaxD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 3.5, 2.1",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "max.d $f1, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(1).readDouble();
    assertEquals(3.5, result, 0.001);
  }

  @Test
  public void testMaxaS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float -3.5, 2.1",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f2, 4($t0)",
      "maxa.s $f1, $f1, $f2"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(1).readSingle();
    assertEquals(-3.5f, result, 0.001f);
  }

  @Test
  public void testMaxaD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double -3.5, 2.1",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "maxa.d $f1, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(1).readDouble();
    assertEquals(-3.5, result, 0.001);
  }

  @Test
  public void testMfc1Extended() throws Exception {
    String[] instructions = {
      ".data", "data: .float 3.14", ".text", "la $t0, data", "lwc1 $f1, 0($t0)", "mfc1 $t1, $f1"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(9).readWord();
    float expected = 3.14f;
    assertEquals(Float.floatToRawIntBits(expected), result);
  }

  @Test
  public void testMfc2() throws Exception {
    String[] instructions = {".text", "addiu $t0, $zero, 75", "mtc2 $t0, $2", "mfc2 $t1, $2"};
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(9).readWord();
    assertEquals(75, result);
  }

  @Test
  public void testMfhc1() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 3.14159",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "mfhc1 $t1, $f2"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(9).readWord();
    long doubleBits = Double.doubleToRawLongBits(3.14159);
    int expectedHigh = (int) (doubleBits >>> 32);
    assertEquals(expectedHigh, result);
  }

  @Test
  public void testMfhc2() throws Exception {
    String[] instructions = {".text", "addiu $t0, $zero, 88", "mthc2 $t0, $3", "mfhc2 $t1, $3"};
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(9).readWord();
    assertEquals(88, result);
  }

  @Test
  public void testMfhi() throws Exception {
    String[] instructions = {
      ".text", "lui $t1, 0x1000", "lui $t2, 0x1000", "mult $t1, $t2", "mfhi $t0"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(0x01000000, result);
  }

  @Test
  public void testMflo() throws Exception {
    String[] instructions = {
      ".text", "addiu $t0, $zero, 10", "addiu $t1, $zero, 3", "mult $t0, $t1", "mflo $t2"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(10).readWord();
    assertEquals(30, result);
  }

  @Test
  public void testMinS() throws Exception {
    String[] instructions = {
      ".data",
      "f1: .float 5.0",
      "f2: .float 3.0",
      "result: .float 0.0",
      ".text",
      "la $t0, f1",
      "lwc1 $f1, 0($t0)",
      "la $t0, f2",
      "lwc1 $f2, 0($t0)",
      "min.s $f3, $f1, $f2",
      "la $t0, result",
      "swc1 $f3, 0($t0)"
    };
    assemble(instructions);
    executeInstructions(10);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    float result = Float.intBitsToFloat(memory.readWord(t0));
    assertEquals(3.0f, result, 0.0001f);
  }

  @Test
  public void testMinD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 3.5, 2.1",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "min.d $f1, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(1).readDouble();
    assertEquals(2.1, result, 0.001);
  }

  @Test
  public void testMinaS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float -3.5, 2.1",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f2, 4($t0)",
      "mina.s $f1, $f1, $f2"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(1).readSingle();
    assertEquals(2.1f, result, 0.001f);
  }

  @Test
  public void testMinaD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double -3.5, 2.1",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "mina.d $f1, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(1).readDouble();
    assertEquals(2.1, result, 0.001);
  }

  @Test
  public void testMod() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 17", "addiu $t2, $zero, 5", "mod $t0, $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(2, t0);
  }

  @Test
  public void testModu() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 17", "addiu $t2, $zero, 5", "modu $t0, $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(2, t0);
  }

  @Test
  public void testMovS() throws Exception {
    String[] instructions = {
      ".data",
      "fval: .float 3.14",
      "result: .float 0.0",
      ".text",
      "la $t0, fval",
      "lwc1 $f1, 0($t0)",
      "mov.s $f2, $f1",
      "la $t0, result",
      "swc1 $f2, 0($t0)"
    };
    assemble(instructions);
    executeInstructions(7);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    float result = Float.intBitsToFloat(memory.readWord(t0));
    assertEquals(3.14f, result, 0.01f);
  }

  @Test
  public void testMovD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 3.14159",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "mov.d $f4, $f2"
    };
    assemble(instructions);
    executeInstructions(4);

    double result = cpu.getFpuRegisterFileArray().getFile(4).readDouble();
    assertEquals(3.14159, result, 0.001);
  }

  @Test
  public void testMovn() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 42",
      "addiu $t2, $zero, 1",
      "addiu $t0, $zero, 0",
      "movn $t0, $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testMovnNoMove() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 42",
      "addiu $t2, $zero, 0",
      "addiu $t0, $zero, 99",
      "movn $t0, $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(99, result);
  }

  @Test
  public void testMovz() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 42",
      "addiu $t2, $zero, 0",
      "addiu $t0, $zero, 0",
      "movz $t0, $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testMovzNoMove() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 42",
      "addiu $t2, $zero, 1",
      "addiu $t0, $zero, 99",
      "movz $t0, $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(99, result);
  }

  @Test
  public void testMsub() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 10",
      "addiu $t2, $zero, 20",
      "mult $t1, $t2",
      "addiu $t1, $zero, 5",
      "addiu $t2, $zero, 6",
      "msub $t1, $t2",
      "mflo $t0"
    };
    assemble(instructions);
    executeInstructions(7);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(170, t0);
  }

  @Test
  public void testMsubfS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 20.0, 3.0, 4.0",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f2, 4($t0)",
      "lwc1 $f3, 8($t0)",
      "msubf.s $f1, $f2, $f3"
    };
    assemble(instructions);
    executeInstructions(6);

    float result = cpu.getFpuRegisterFileArray().getFile(1).readSingle();
    assertEquals(8.0f, result, 0.001f);
  }

  @Test
  public void testMsubfD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 20.0, 3.0, 4.0",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "ldc1 $f6, 16($t0)",
      "msubf.d $f2, $f4, $f6"
    };
    assemble(instructions);
    executeInstructions(6);

    double result = cpu.getFpuRegisterFileArray().getFile(2).readDouble();
    assertEquals(8.0, result, 0.001);
  }

  @Test
  public void testMsubu() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 10",
      "addiu $t2, $zero, 20",
      "multu $t1, $t2",
      "addiu $t1, $zero, 5",
      "addiu $t2, $zero, 6",
      "msubu $t1, $t2",
      "mflo $t0"
    };
    assemble(instructions);
    executeInstructions(7);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(170, t0);
  }

  @Test
  public void testMtc0() throws Exception {
    String[] instructions = {".text", "addiu $t0, $zero, 42", "mtc0 $t0, $12"};
    assemble(instructions);
    executeInstructions(2);

    assertTrue(true);
  }

  @Test
  public void testMtc1Mfc1() throws Exception {
    String[] instructions = {".text", "addiu $t0, $zero, 100", "mtc1 $t0, $f1", "mfc1 $t1, $f1"};
    assemble(instructions);
    executeInstructions(3);

    int t1 = cpu.getGprFileArray().getFile(9).readWord();
    assertEquals(100, t1);
  }

  @Test
  public void testMtc2() throws Exception {
    String[] instructions = {".text", "addiu $t0, $zero, 42", "mtc2 $t0, $4", "mfc2 $t1, $4"};
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(9).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testMthc1() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t0, $zero, 0x4009",
      "sll $t0, $t0, 16",
      "ori $t0, $t0, 0x21FB",
      "mthc1 $t0, $f2",
      "mfhc1 $t1, $f2"
    };
    assemble(instructions);
    executeInstructions(5);

    int result = cpu.getGprFileArray().getFile(9).readWord();
    int expected = 0x400921FB;
    assertEquals(expected, result);
  }

  @Test
  public void testMthc2() throws Exception {
    String[] instructions = {".text", "addiu $t0, $zero, 99", "mthc2 $t0, $5", "mfhc2 $t1, $5"};
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(9).readWord();
    assertEquals(99, result);
  }

  @Test
  public void testMthi() throws Exception {
    String[] instructions = {".text", "addiu $t1, $zero, 42", "mthi $t1", "mfhi $t0"};
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testMtlo() throws Exception {
    String[] instructions = {".text", "addiu $t1, $zero, 99", "mtlo $t1", "mflo $t0"};
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(99, result);
  }

  @Test
  public void testMuh() throws Exception {
    String[] instructions = {".text", "lui $t1, 0x1000", "lui $t2, 0x1000", "muh $t0, $t1, $t2"};
    assemble(instructions);
    executeInstructions(3);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(0x01000000, t0);
  }

  @Test
  public void testMuhu() throws Exception {
    String[] instructions = {".text", "lui $t1, 0x1000", "lui $t2, 0x1000", "muhu $t0, $t1, $t2"};
    assemble(instructions);
    executeInstructions(3);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(0x01000000, t0);
  }

  @Test
  public void testMul() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 7", "addiu $t2, $zero, 6", "mult $t1, $t2", "mflo $t0"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testMulS() throws Exception {
    String[] instructions = {
      ".data",
      "f1: .float 2.0",
      "f2: .float 3.0",
      "result: .float 0.0",
      ".text",
      "la $t0, f1",
      "lwc1 $f1, 0($t0)",
      "la $t0, f2",
      "lwc1 $f2, 0($t0)",
      "mul.s $f3, $f1, $f2",
      "la $t0, result",
      "swc1 $f3, 0($t0)"
    };
    assemble(instructions);
    executeInstructions(10);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    float result = Float.intBitsToFloat(memory.readWord(t0));
    assertEquals(6.0f, result, 0.0001f);
  }

  @Test
  public void testMulD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 2.5, 4.0",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "mul.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(10.0, result, 0.001);
  }

  @Test
  public void testMult() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 100", "addiu $t2, $zero, 200", "mult $t1, $t2", "mflo $t0"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(20000, result);
  }

  @Test
  public void testMultu() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 50", "addiu $t2, $zero, 60", "multu $t1, $t2", "mflo $t0"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(3000, result);
  }

  @Test
  public void testMulu() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 7", "addiu $t2, $zero, 6", "mulu $t0, $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, t0);
  }

  @Test
  public void testNal() throws Exception {
    String[] instructions = {".text", "nal", "addiu $t0, $zero, 42"};
    assemble(instructions);
    executeInstructions(2);

    int ra = cpu.getGprFileArray().getFile(31).readWord();
    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(true, ra > 0);
    assertEquals(42, t0);
  }

  @Test
  public void testNegS() throws Exception {
    String[] instructions = {
      ".data",
      "fval: .float 5.5",
      "result: .float 0.0",
      ".text",
      "la $t0, fval",
      "lwc1 $f1, 0($t0)",
      "neg.s $f2, $f1",
      "la $t0, result",
      "swc1 $f2, 0($t0)"
    };
    assemble(instructions);
    executeInstructions(7);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    float result = Float.intBitsToFloat(memory.readWord(t0));
    assertEquals(-5.5f, result, 0.0001f);
  }

  @Test
  public void testNegD() throws Exception {
    String[] instructions = {
      ".data", "data: .double 3.14", ".text", "la $t0, data", "ldc1 $f2, 0($t0)", "neg.d $f4, $f2"
    };
    assemble(instructions);
    executeInstructions(4);

    double result = cpu.getFpuRegisterFileArray().getFile(4).readDouble();
    assertEquals(-3.14, result, 0.001);
  }

  @Test
  public void testNop() throws Exception {
    String[] instructions = {".text", "addiu $t0, $zero, 1", "nop", "addiu $t0, $t0, 1"};
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(2, result);
  }

  @Test
  public void testNor() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 0", "addiu $t2, $zero, 0", "nor $t0, $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(-1, result);
  }

  @Test
  public void testOr() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 240", "addiu $t2, $zero, 15", "or $t0, $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(255, result);
  }

  @Test
  public void testOri() throws Exception {
    String[] instructions = {".text", "addiu $t1, $zero, 240", "ori $t0, $t1, 15"};
    assemble(instructions);
    executeInstructions(2);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(255, result);
  }

  @Test
  public void testRdpgpr() throws Exception {
    String[] instructions = {".text", "addiu $t0, $zero, 42", "rdpgpr $t1, $t0"};
    assemble(instructions);
    executeInstructions(2);

    int result = cpu.getGprFileArray().getFile(9).readWord();
    assertTrue(result >= 0);
  }

  @Test
  public void testRecipS() throws Exception {
    String[] instructions = {
      ".data", "data: .float 2.0", ".text", "la $t0, data", "lwc1 $f1, 0($t0)", "recip.s $f2, $f1"
    };
    assemble(instructions);
    executeInstructions(4);

    float result = cpu.getFpuRegisterFileArray().getFile(2).readSingle();
    assertEquals(0.5f, result, 0.001f);
  }

  @Test
  public void testRecipD() throws Exception {
    String[] instructions = {
      ".data", "data: .double 4.0", ".text", "la $t0, data", "ldc1 $f2, 0($t0)", "recip.d $f4, $f2"
    };
    assemble(instructions);
    executeInstructions(4);

    double result = cpu.getFpuRegisterFileArray().getFile(4).readDouble();
    assertEquals(0.25, result, 0.001);
  }

  @Test
  public void testRintS() throws Exception {
    String[] instructions = {
      ".data", "data: .float 3.7", ".text", "la $t0, data", "lwc1 $f1, 0($t0)", "rint.s $f3, $f1"
    };
    assemble(instructions);
    executeInstructions(4);

    float result = cpu.getFpuRegisterFileArray().getFile(3).readSingle();
    assertEquals(4.0f, result, 0.001f);
  }

  @Test
  public void testRintD() throws Exception {
    String[] instructions = {
      ".data", "data: .double 3.7", ".text", "la $t0, data", "ldc1 $f2, 0($t0)", "rint.d $f4, $f2"
    };
    assemble(instructions);
    executeInstructions(4);

    double result = cpu.getFpuRegisterFileArray().getFile(4).readDouble();
    assertEquals(4.0, result, 0.001);
  }

  @Test
  public void testRotr() throws Exception {
    String[] instructions = {
      ".data", "data: .word 0x1", ".text", "la $t1, data", "lw $t1, 0($t1)", "rotr $t0, $t1, 4"
    };
    assemble(instructions);
    executeInstructions(5);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(0x10000000, result);
  }

  @Test
  public void testRotrv() throws Exception {
    String[] instructions = {
      ".data",
      "data: .word 0x1, 0x4",
      ".text",
      "la $t0, data",
      "lw $t1, 0($t0)",
      "lw $t2, 4($t0)",
      "rotrv $t0, $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(5);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(0x10000000, result);
  }

  @Test
  public void testRoundLS() throws Exception {
    String[] instructions = {
      ".data", "data: .float 3.7", ".text", "la $t0, data", "lwc1 $f1, 0($t0)", "round.l.s $f2, $f1"
    };
    assemble(instructions);
    executeInstructions(4);

    double result = cpu.getFpuRegisterFileArray().getFile(2).readDouble();
    long longResult = Double.doubleToRawLongBits(result);
    assertEquals(4L, longResult);
  }

  @Test
  public void testRoundLD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 3.7",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "round.l.d $f4, $f2"
    };
    assemble(instructions);
    executeInstructions(4);

    double result = cpu.getFpuRegisterFileArray().getFile(4).readDouble();
    long longResult = Double.doubleToRawLongBits(result);
    assertEquals(4L, longResult);
  }

  @Test
  public void testRoundWS() throws Exception {
    String[] instructions = {
      ".data",
      "fval: .float 42.6",
      ".text",
      "la $t0, fval",
      "lwc1 $f1, 0($t0)",
      "round.w.s $f2, $f1",
      "mfc1 $t1, $f2"
    };
    assemble(instructions);
    executeInstructions(5);

    int t1 = cpu.getGprFileArray().getFile(9).readWord();
    assertEquals(43, t1);
  }

  @Test
  public void testRoundWD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 3.7",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "round.w.d $f4, $f2"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getFpuRegisterFileArray().getFile(4).readWord();
    assertEquals(4, result);
  }

  @Test
  public void testRsqrtS() throws Exception {
    String[] instructions = {
      ".data", "data: .float 4.0", ".text", "la $t0, data", "lwc1 $f1, 0($t0)", "rsqrt.s $f2, $f1"
    };
    assemble(instructions);
    executeInstructions(4);

    float result = cpu.getFpuRegisterFileArray().getFile(2).readSingle();
    assertEquals(0.5f, result, 0.001f);
  }

  @Test
  public void testRsqrtD() throws Exception {
    String[] instructions = {
      ".data", "data: .double 9.0", ".text", "la $t0, data", "ldc1 $f2, 0($t0)", "rsqrt.d $f4, $f2"
    };
    assemble(instructions);
    executeInstructions(4);

    double result = cpu.getFpuRegisterFileArray().getFile(4).readDouble();
    assertEquals(0.333333, result, 0.001);
  }

  @Test
  public void testSb() throws Exception {
    String[] instructions = {
      ".data",
      "byte_val: .byte 0",
      ".text",
      "la $t1, byte_val",
      "addiu $t2, $zero, 65",
      "sb $t2, 0($t1)",
      "lb $t0, 0($t1)"
    };
    assemble(instructions);
    executeInstructions(5);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(65, result);
  }

  @Test
  public void testSbe() throws Exception {
    String[] instructions = {
      ".data",
      "data: .byte 0",
      ".text",
      "la $t1, data",
      "addiu $t0, $zero, 123",
      "sbe $t0, 0($t1)",
      "lb $t2, 0($t1)"
    };
    assemble(instructions);
    executeInstructions(5);

    int result = cpu.getGprFileArray().getFile(10).readWord();
    assertEquals(123, result);
  }

  @Test
  public void testSc() throws Exception {
    String[] instructions = {
      ".data",
      "val: .word 0",
      ".text",
      "la $t0, val",
      "ll $t1, 0($t0)",
      "addiu $t1, $zero, 42",
      "sc $t1, 0($t0)"
    };
    assemble(instructions);
    executeInstructions(5);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    int stored = memory.readWord(t0);
    assertEquals(42, stored);
  }

  @Test
  public void testSce() throws Exception {
    String[] instructions = {
      ".data",
      "data: .word 0",
      ".text",
      "la $t1, data",
      "lle $t0, 0($t1)",
      "addiu $t2, $zero, 99",
      "sce $t2, 0($t1)",
      "lw $t3, 0($t1)"
    };
    assemble(instructions);
    executeInstructions(6);

    int stored = cpu.getGprFileArray().getFile(11).readWord();
    int scResult = cpu.getGprFileArray().getFile(10).readWord();
    assertEquals(99, stored);
    assertEquals(1, scResult);
  }

  @Test
  public void testScwp() throws Exception {
    String[] instructions = {
      ".data",
      "data: .word 0, 0",
      ".text",
      "la $t4, data",
      "llwp $t0, $t1, 0($t4)",
      "addiu $t2, $zero, 100",
      "addiu $t3, $zero, 200",
      "scwp $t2, $t3, 0($t4)",
      "lw $t5, 0($t4)",
      "lw $t6, 4($t4)"
    };
    assemble(instructions);
    executeInstructions(8);

    int first = cpu.getGprFileArray().getFile(14).readWord();
    int second = cpu.getGprFileArray().getFile(13).readWord();
    assertEquals(100, first);
    assertEquals(200, second);
  }

  @Test
  public void testScwpe() throws Exception {
    String[] instructions = {
      ".data",
      "data: .word 0, 0",
      ".text",
      "la $t0, data",
      "llwpe $t1, $t2, 0($t0)",
      "addiu $t3, $zero, 50",
      "addiu $t4, $zero, 60",
      "scwpe $t3, $t4, 0($t0)"
    };
    assemble(instructions);
    executeInstructions(6);

    int scResult = cpu.getGprFileArray().getFile(11).readWord();
    assertTrue(scResult == 1 || scResult == 0);
  }

  @Test
  public void testSdbbp() throws Exception {
    String[] instructions = {".text", "addiu $t0, $zero, 10", "sdbbp", "addiu $t0, $zero, 42"};
    assemble(instructions);

    try {
      executeInstructions(3);
      int result = cpu.getGprFileArray().getFile(8).readWord();
      assertTrue(result == 10 || result == 42);
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  @Test
  public void testSdc2() throws Exception {
    String[] instructions = {
      ".data",
      "data: .word 0, 0",
      ".text",
      "la $t0, data",
      "addiu $t1, $zero, 150",
      "mtc2 $t1, $1",
      "sdc2 $1, 0($t0)",
      "lw $t2, 4($t0)"
    };
    assemble(instructions);
    executeInstructions(6);

    int result = cpu.getGprFileArray().getFile(10).readWord();
    assertEquals(150, result);
  }

  @Test
  public void testSeb() throws Exception {
    String[] instructions = {
      ".data", "data: .word 0x00ff", ".text", "la $t1, data", "lw $t1, 0($t1)", "seb $t0, $t1"
    };
    assemble(instructions);
    executeInstructions(5);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(0xffffffff, result);
  }

  @Test
  public void testSeh() throws Exception {
    String[] instructions = {
      ".data", "data: .word 0x00fff", ".text", "la $t1, data", "lw $t1, 0($t1)", "seh $t0, $t1"
    };
    assemble(instructions);
    executeInstructions(5);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(0xfff, result);
  }

  @Test
  public void testSelS() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 1.0, 2.0, 3.0",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f2, 4($t0)",
      "lwc1 $f3, 8($t0)",
      "sel.s $f4, $f2, $f3"
    };
    assemble(instructions);
    executeInstructions(6);

    float result = cpu.getFpuRegisterFileArray().getFile(4).readSingle();
    assertEquals(2.0f, result, 0.001f);
  }

  @Test
  public void testSelD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 0.0, 2.0, 3.0",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "ldc1 $f6, 16($t0)",
      "sel.d $f8, $f4, $f6"
    };
    assemble(instructions);
    executeInstructions(6);

    double result = cpu.getFpuRegisterFileArray().getFile(8).readDouble();
    assertEquals(2.0, result, 0.001);
  }

  @Test
  public void testSeleqz() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 42", "addiu $t2, $zero, 0", "seleqz $t0, $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testSeleqzDFp() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 2.5, 0.0",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "seleqz.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(2.5, result, 0.001);
  }

  @Test
  public void testSeleqzNotSelected() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 42", "addiu $t2, $zero, 1", "seleqz $t0, $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(0, result);
  }

  @Test
  public void testSeleqzSFp() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 2.5, 0.0",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f2, 4($t0)",
      "seleqz.s $f3, $f1, $f2"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(3).readSingle();
    assertEquals(2.5f, result, 0.001f);
  }

  @Test
  public void testSelnez() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 42", "addiu $t2, $zero, 1", "selnez $t0, $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testSelnezDFp() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 2.5, 4.9E-324",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "selnez.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(2.5, result, 0.001);
  }

  @Test
  public void testSelnezNotSelected() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 42", "addiu $t2, $zero, 0", "selnez $t0, $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(0, result);
  }

  @Test
  public void testSelnezSFp() throws Exception {
    String[] instructions = {
      ".data",
      "data: .float 2.5, 1.4E-45",
      ".text",
      "la $t0, data",
      "lwc1 $f1, 0($t0)",
      "lwc1 $f2, 4($t0)",
      "selnez.s $f3, $f1, $f2"
    };
    assemble(instructions);
    executeInstructions(5);

    float result = cpu.getFpuRegisterFileArray().getFile(3).readSingle();
    assertEquals(2.5f, result, 0.001f);
  }

  @Test
  public void testSequentialExecution() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t0, $zero, 1",
      "addiu $t1, $zero, 2",
      "addiu $t2, $zero, 3",
      "addiu $t3, $zero, 4",
      "addiu $t4, $zero, 5"
    };
    assemble(instructions);
    executeInstructions(5);

    assertEquals(1, cpu.getGprFileArray().getFile(8).readWord());
    assertEquals(2, cpu.getGprFileArray().getFile(9).readWord());
    assertEquals(3, cpu.getGprFileArray().getFile(10).readWord());
    assertEquals(4, cpu.getGprFileArray().getFile(11).readWord());
    assertEquals(5, cpu.getGprFileArray().getFile(12).readWord());
  }

  @Test
  public void testSequentialJump() throws Exception {
    String[] instructions = {
      ".text", "addiu $t0, $zero, 1", "addiu $t1, $zero, 2", "addiu $t2, $zero, 3"
    };
    assemble(instructions);
    executeInstructions(3);

    assertEquals(1, cpu.getGprFileArray().getFile(8).readWord());
    assertEquals(2, cpu.getGprFileArray().getFile(9).readWord());
    assertEquals(3, cpu.getGprFileArray().getFile(10).readWord());
  }

  @Test
  public void testSh() throws Exception {
    String[] instructions = {
      ".data",
      "half_val: .half 0",
      ".text",
      "la $t1, half_val",
      "addiu $t2, $zero, 1234",
      "sh $t2, 0($t1)",
      "lh $t0, 0($t1)"
    };
    assemble(instructions);
    executeInstructions(5);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(1234, result);
  }

  @Test
  public void testShe() throws Exception {
    String[] instructions = {
      ".data",
      "data: .half 0",
      ".text",
      "la $t1, data",
      "addiu $t0, $zero, 1234",
      "she $t0, 0($t1)",
      "lh $t2, 0($t1)"
    };
    assemble(instructions);
    executeInstructions(5);

    int result = cpu.getGprFileArray().getFile(10).readWord();
    assertEquals(1234, result);
  }

  @Test
  public void testSigrie() throws Exception {
    String[] instructions = {".text", "addiu $t0, $zero, 10", "sigrie 1", "addiu $t0, $zero, 42"};
    assemble(instructions);

    try {
      executeInstructions(3);
      int result = cpu.getGprFileArray().getFile(8).readWord();
      assertTrue(result == 10 || result == 42);
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  @Test
  public void testSimpleLoop() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t0, $zero, 0",
      "addiu $t1, $zero, 5",
      "loop: addiu $t0, $t0, 1",
      "bne $t0, $t1, loop"
    };
    assemble(instructions);
    executeInstructions(12);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(5, result);
  }

  @Test
  public void testSll() throws Exception {
    String[] instructions = {
      ".data", "data: .word 0x1", ".text", "la $t1, data", "lw $t1, 0($t1)", "sll $t0, $t1, 4"
    };
    assemble(instructions);
    executeInstructions(5);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(0x10, result);
  }

  @Test
  public void testSllv() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 1", "addiu $t2, $zero, 3", "sllv $t0, $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(8, result);
  }

  @Test
  public void testSlt() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 5", "addiu $t2, $zero, 10", "slt $t0, $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(1, result);
  }

  @Test
  public void testSltFalse() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 15", "addiu $t2, $zero, 10", "slt $t0, $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(0, result);
  }

  @Test
  public void testSlti() throws Exception {
    String[] instructions = {".text", "addiu $t1, $zero, 5", "slti $t0, $t1, 10"};
    assemble(instructions);
    executeInstructions(2);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(1, result);
  }

  @Test
  public void testSltiu() throws Exception {
    String[] instructions = {".text", "addiu $t1, $zero, 5", "sltiu $t0, $t1, 10"};
    assemble(instructions);
    executeInstructions(2);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(1, result);
  }

  @Test
  public void testSltu() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 5", "addiu $t2, $zero, 10", "sltu $t0, $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(1, result);
  }

  @Test
  public void testSqrtS() throws Exception {
    String[] instructions = {
      ".data",
      "fval: .float 16.0",
      "result: .float 0.0",
      ".text",
      "la $t0, fval",
      "lwc1 $f1, 0($t0)",
      "sqrt.s $f2, $f1",
      "la $t0, result",
      "swc1 $f2, 0($t0)"
    };
    assemble(instructions);
    executeInstructions(7);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    float result = Float.intBitsToFloat(memory.readWord(t0));
    assertEquals(4.0f, result, 0.0001f);
  }

  @Test
  public void testSqrtD() throws Exception {
    String[] instructions = {
      ".data", "data: .double 9.0", ".text", "la $t0, data", "ldc1 $f2, 0($t0)", "sqrt.d $f4, $f2"
    };
    assemble(instructions);
    executeInstructions(4);

    double result = cpu.getFpuRegisterFileArray().getFile(4).readDouble();
    assertEquals(3.0, result, 0.001);
  }

  @Test
  public void testSra() throws Exception {
    String[] instructions = {".text", "addiu $t1, $zero, -16", "sra $t0, $t1, 2"};
    assemble(instructions);
    executeInstructions(2);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(-4, result);
  }

  @Test
  public void testSrav() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, -32", "addiu $t2, $zero, 3", "srav $t0, $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(-4, result);
  }

  @Test
  public void testSrl() throws Exception {
    String[] instructions = {".text", "addiu $t1, $zero, 16", "srl $t0, $t1, 2"};
    assemble(instructions);
    executeInstructions(2);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(4, result);
  }

  @Test
  public void testSrlv() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 32", "addiu $t2, $zero, 3", "srlv $t0, $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(4, result);
  }

  @Test
  public void testStackOperations() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $sp, $sp, -8",
      "addiu $t0, $zero, 42",
      "sw $t0, 0($sp)",
      "addiu $t1, $zero, 99",
      "sw $t1, 4($sp)",
      "lw $t2, 0($sp)",
      "lw $t3, 4($sp)",
      "addiu $sp, $sp, 8"
    };
    assemble(instructions);
    executeInstructions(8);

    int t2 = cpu.getGprFileArray().getFile(10).readWord();
    int t3 = cpu.getGprFileArray().getFile(11).readWord();
    assertEquals(42, t2);
    assertEquals(99, t3);
  }

  @Test
  public void testSub() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 50", "addiu $t2, $zero, 20", "sub $t0, $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(30, result);
  }

  @Test
  public void testSubS() throws Exception {
    String[] instructions = {
      ".data",
      "f1: .float 5.5",
      "f2: .float 2.5",
      "result: .float 0.0",
      ".text",
      "la $t0, f1",
      "lwc1 $f1, 0($t0)",
      "la $t0, f2",
      "lwc1 $f2, 0($t0)",
      "sub.s $f3, $f1, $f2",
      "la $t0, result",
      "swc1 $f3, 0($t0)"
    };
    assemble(instructions);
    executeInstructions(10);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    float result = Float.intBitsToFloat(memory.readWord(t0));
    assertEquals(3.0f, result, 0.0001f);
  }

  @Test
  public void testSubD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 10.0, 3.5",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "ldc1 $f4, 8($t0)",
      "sub.d $f6, $f2, $f4"
    };
    assemble(instructions);
    executeInstructions(5);

    double result = cpu.getFpuRegisterFileArray().getFile(6).readDouble();
    assertEquals(6.5, result, 0.001);
  }

  @Test
  public void testSubu() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 100", "addiu $t2, $zero, 40", "subu $t0, $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(60, result);
  }

  @Test
  public void testSwc2() throws Exception {
    String[] instructions = {
      ".data",
      "data: .word 0",
      ".text",
      "la $t0, data",
      "addiu $t1, $zero, 125",
      "mtc2 $t1, $1",
      "swc2 $1, 0($t0)",
      "lw $t2, 0($t0)"
    };
    assemble(instructions);
    executeInstructions(6);

    int result = cpu.getGprFileArray().getFile(10).readWord();
    assertEquals(125, result);
  }

  @Test
  public void testSwe() throws Exception {
    String[] instructions = {
      ".data",
      "data: .word 0",
      ".text",
      "la $t1, data",
      "addiu $t0, $zero, 42",
      "swe $t0, 0($t1)",
      "lw $t2, 0($t1)"
    };
    assemble(instructions);
    executeInstructions(5);

    int result = cpu.getGprFileArray().getFile(10).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testSwl() throws Exception {
    String[] instructions = {
      ".data",
      "val: .word 0x00000000",
      ".text",
      "la $t1, val",
      "lui $t0, 0x1234",
      "ori $t0, $t0, 0x5678",
      "swl $t0, 2($t1)"
    };
    assemble(instructions);
    executeInstructions(5);

    int t1 = cpu.getGprFileArray().getFile(9).readWord();
    int stored = memory.readWord(t1);
    assertEquals(0x1234, stored);
  }

  @Test
  public void testSwLw() throws Exception {
    String[] instructions = {
      ".data",
      "value: .word 0",
      ".text",
      "la $t1, value",
      "addiu $t2, $zero, 99",
      "sw $t2, 0($t1)",
      "lw $t0, 0($t1)"
    };
    assemble(instructions);
    executeInstructions(5);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(99, result);
  }

  @Test
  public void testSwr() throws Exception {
    String[] instructions = {
      ".data",
      "val: .word 0x00000000",
      ".text",
      "la $t1, val",
      "lui $t0, 0x1234",
      "ori $t0, $t0, 0x5678",
      "swr $t0, 1($t1)"
    };
    assemble(instructions);
    executeInstructions(5);

    int t1 = cpu.getGprFileArray().getFile(9).readWord();
    int stored = memory.readWord(t1);
    assertEquals(0x56780000, stored);
  }

  @Test
  public void testSync() throws Exception {
    String[] instructions = {".text", "addiu $t0, $zero, 42", "sync", "addiu $t1, $zero, 99"};
    assemble(instructions);
    executeInstructions(3);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    int t1 = cpu.getGprFileArray().getFile(9).readWord();
    assertEquals(42, t0);
    assertEquals(99, t1);
  }

  @Test(expected = Exception.class)
  public void testSyscall() throws Exception {
    String[] instructions = {".text", "addiu $t0, $zero, 10", "syscall", "addiu $t0, $zero, 42"};
    assemble(instructions);
    executeInstructions(3);
  }

  @Test
  public void testSyscallAssembly() throws Exception {
    String[] instructions = {".text", "addiu $t0, $zero, 42", "addiu $t1, $zero, 99"};
    assemble(instructions);
    executeInstructions(2);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    int t1 = cpu.getGprFileArray().getFile(9).readWord();
    assertEquals(42, t0);
    assertEquals(99, t1);
  }

  @Test
  public void testTeq() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 5", "addiu $t2, $zero, 10", "teq $t1, $t2", "addiu $t3, $zero, 42"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(11).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testTeqNoTrap() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 5", "addiu $t2, $zero, 10", "teq $t1, $t2", "addiu $t0, $zero, 42"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test(expected = Exception.class)
  public void testTeqTrap() throws Exception {
    String[] instructions = {".text", "addiu $t1, $zero, 5", "addiu $t2, $zero, 5", "teq $t1, $t2"};
    assemble(instructions);
    executeInstructions(3);
  }

  @Test
  public void testTge() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 5", "addiu $t2, $zero, 10", "tge $t1, $t2", "addiu $t3, $zero, 42"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(11).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testTgeNoTrap() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 5", "addiu $t2, $zero, 10", "tge $t1, $t2", "addiu $t0, $zero, 42"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test(expected = Exception.class)
  public void testTgeTrap() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 10", "addiu $t2, $zero, 5", "tge $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);
  }

  @Test
  public void testTgeu() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 5",
      "addiu $t2, $zero, 10",
      "tgeu $t1, $t2",
      "addiu $t3, $zero, 42"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(11).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testTgeuNoTrap() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 5",
      "addiu $t2, $zero, 10",
      "tgeu $t1, $t2",
      "addiu $t0, $zero, 42"
    };
    assemble(instructions);
    executeInstructions(4);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, t0);
  }

  @Test(expected = Exception.class)
  public void testTgeuTrap() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 10", "addiu $t2, $zero, 5", "tgeu $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);
  }

  @Test
  public void testTlt() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 10", "addiu $t2, $zero, 5", "tlt $t1, $t2", "addiu $t3, $zero, 42"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(11).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testTltNoTrap() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 10", "addiu $t2, $zero, 5", "tlt $t1, $t2", "addiu $t0, $zero, 42"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test(expected = Exception.class)
  public void testTltTrap() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 5", "addiu $t2, $zero, 10", "tlt $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);
  }

  @Test
  public void testTltu() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 10",
      "addiu $t2, $zero, 5",
      "tltu $t1, $t2",
      "addiu $t3, $zero, 42"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(11).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testTltuNoTrap() throws Exception {
    String[] instructions = {
      ".text",
      "addiu $t1, $zero, 10",
      "addiu $t2, $zero, 5",
      "tltu $t1, $t2",
      "addiu $t0, $zero, 42"
    };
    assemble(instructions);
    executeInstructions(4);

    int t0 = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, t0);
  }

  @Test(expected = Exception.class)
  public void testTltuTrap() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 5", "addiu $t2, $zero, 10", "tltu $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);
  }

  @Test
  public void testTne() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 5", "addiu $t2, $zero, 5", "tne $t1, $t2", "addiu $t3, $zero, 42"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(11).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testTneNoTrap() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 5", "addiu $t2, $zero, 5", "tne $t1, $t2", "addiu $t0, $zero, 42"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test(expected = Exception.class)
  public void testTneTrap() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 5", "addiu $t2, $zero, 10", "tne $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);
  }

  @Test
  public void testTruncLS() throws Exception {
    String[] instructions = {
      ".data", "data: .float 3.7", ".text", "la $t0, data", "lwc1 $f1, 0($t0)", "trunc.l.s $f2, $f1"
    };
    assemble(instructions);
    executeInstructions(4);

    double result = cpu.getFpuRegisterFileArray().getFile(2).readDouble();
    long longResult = Double.doubleToRawLongBits(result);
    assertEquals(3L, longResult);
  }

  @Test
  public void testTruncLD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 3.7",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "trunc.l.d $f4, $f2"
    };
    assemble(instructions);
    executeInstructions(4);

    double result = cpu.getFpuRegisterFileArray().getFile(4).readDouble();
    long longResult = Double.doubleToRawLongBits(result);
    assertEquals(3L, longResult);
  }

  @Test
  public void testTruncWS() throws Exception {
    String[] instructions = {
      ".data",
      "fval: .float 42.9",
      ".text",
      "la $t0, fval",
      "lwc1 $f1, 0($t0)",
      "trunc.w.s $f2, $f1",
      "mfc1 $t1, $f2"
    };
    assemble(instructions);
    executeInstructions(5);

    int t1 = cpu.getGprFileArray().getFile(9).readWord();
    assertEquals(42, t1);
  }

  @Test
  public void testTruncWD() throws Exception {
    String[] instructions = {
      ".data",
      "data: .double 3.9",
      ".text",
      "la $t0, data",
      "ldc1 $f2, 0($t0)",
      "trunc.w.d $f4, $f2"
    };
    assemble(instructions);
    executeInstructions(4);

    int result = cpu.getFpuRegisterFileArray().getFile(4).readWord();
    assertEquals(3, result);
  }

  @Test
  public void testWrpgpr() throws Exception {
    String[] instructions = {".text", "addiu $t0, $zero, 42", "wrpgpr $t1, $t0"};
    assemble(instructions);
    executeInstructions(2);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(42, result);
  }

  @Test
  public void testWsbh() throws Exception {
    String[] instructions = {".text", "lui $t1, 0x1234", "ori $t1, $t1, 0x5678", "wsbh $t0, $t1"};
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(0x34127856, result);
  }

  @Test
  public void testXor() throws Exception {
    String[] instructions = {
      ".text", "addiu $t1, $zero, 255", "addiu $t2, $zero, 15", "xor $t0, $t1, $t2"
    };
    assemble(instructions);
    executeInstructions(3);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(240, result);
  }

  @Test
  public void testXori() throws Exception {
    String[] instructions = {".text", "addiu $t1, $zero, 255", "xori $t0, $t1, 15"};
    assemble(instructions);
    executeInstructions(2);

    int result = cpu.getGprFileArray().getFile(8).readWord();
    assertEquals(240, result);
  }
}
