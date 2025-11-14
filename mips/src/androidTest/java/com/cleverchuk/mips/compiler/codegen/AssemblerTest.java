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

package com.cleverchuk.mips.compiler.codegen;

import static org.junit.Assert.assertEquals;

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
public class AssemblerTest {
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

  private Assembler tested;

  @Before
  public void setup() {
    tested = new Assembler();
    parser.addVisitor(tested);
  }

  @After
  public void teardown() {
    parser.removeVisitor(tested);
  }

  @Test
  public void testLi() {
    String[] instructions = {".text", "li $t0, 300"};

    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x3408012c;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testSdc2() {
    String[] instructions = {".text", "sdc2 $t0, 3($t1)"};

    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x49e84803;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testB() {
    String[] instructions = {".text", "b label", "label: la $t4, bytes"};

    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x10000004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBeqz() {
    String[] instructions = {
      ".data", "bytes: .byte 1,2", ".text", "beqz $t0, label", "label: la $t4, bytes"
    };

    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x11000004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testLa() {
    String[] instructions = {
      ".data", "floats: .float 1.666,2.333", "bytes: .byte 1,2", ".text", "la $t0, bytes"
    };

    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x3c080000;
    assertEquals(expectedEncoding, actualEncoding);

    actualEncoding = layout.readWord(tested.getTextOffset() + 4);
    expectedEncoding = 0x35080008;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testMove() {
    String[] instructions = {
      ".data", "floats: .float 1.666,2.333", "bytes: .byte 1,2", ".text", "move $t0, $t1"
    };

    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x01204025;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testNegu() {
    String[] instructions = {".text", "negu $t0, $t1"};

    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x00094023;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testNop() {
    String[] instructions = {".text", "nop"};

    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x0;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testNot() {
    String[] instructions = {".text", "not $t0, $t1"};

    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x01204027;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBnez() {
    String[] instructions = {
      ".data", "bytes: .byte 1, 2", ".text", "bnez $t0, label", "label: la $t4, bytes"
    };

    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x15000004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBal() {
    String[] instructions = {
      ".data", "bytes: .byte 1, 2", ".text", "label: la $t4, bytes", "bal label"
    };

    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset() + 8);
    int expectedEncoding = 0x0411fffc;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testUlw() {
    String[] instructions = {".text", "ulw $t0, 4($t1)"};

    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x89280004;
    assertEquals(expectedEncoding, actualEncoding);

    actualEncoding = layout.readWord(tested.getTextOffset() + 4);
    expectedEncoding = 0x99280007;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testUsw() {
    String[] instructions = {".text", "usw $t0, 4($t1)"};

    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0xa9280004;
    assertEquals(expectedEncoding, actualEncoding);

    actualEncoding = layout.readWord(tested.getTextOffset() + 4);
    expectedEncoding = 0xb9280007;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testEhb() {
    String[] instructions = {".text", "ehb"};

    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x000000c0;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testAbs_d() {
    String[] instructions = {".text", "abs.d $f8, $f9"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46204a05;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testAbs_s() {
    String[] instructions = {".text", "abs.s $f8, $f9"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46004a05;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testAdd() {
    String[] instructions = {".text", "add $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x012a4020;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testAdd_d() {
    String[] instructions = {".text", "add.d $f8, $f9, $f10"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x462a4a00;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testAdd_s() {
    String[] instructions = {".text", "add.s $f8, $f9, $f10"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x460a4a00;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testAddi() {
    String[] instructions = {".text", "addi $t0, $t1, 1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x21090001;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testAddiu() {
    String[] instructions = {".text", "addiu $t0, $t1, 1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x25090001;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testAddiupc() {
    String[] instructions = {".text", "addiupc $t0, 1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0xed000001;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testAddu() {
    String[] instructions = {".text", "addu $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x012a4021;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testAlign() {
    String[] instructions = {".text", "align $t0, $t1, $t2, 1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d2a4260;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testAluipc() {
    String[] instructions = {".text", "aluipc $t0, 1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0xed1f0001;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testAnd() {
    String[] instructions = {".text", "and $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x012a4024;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testAndi() {
    String[] instructions = {".text", "andi $t0, $t1, 5"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x31280005;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testAui() {
    String[] instructions = {".text", "aui $t0, $t1, 5"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x3d280005;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testAuipc() {
    String[] instructions = {".text", "auipc $t0, 5"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0xed1e0005;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBalc() {
    String[] instructions = {".text", "label: andi $t0, $t1, 5", "balc label"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset() + 4);
    int expectedEncoding = 0xe8000000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBc() {
    String[] instructions = {".text", "label: andi $t0, $t1, 5", "bc label"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset() + 4);
    int expectedEncoding = 0xc8000000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBc1eqz() {
    String[] instructions = {".data", "bytes: .byte 1,2", ".text", "bc1eqz $f1, label", "label: andi $t0, $t1, 5"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x45210004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBc1nez() {
    String[] instructions = {".text", "bc1nez $f1, label", "label: andi $t0, $t1, 5"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x45a10004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBc2eqz() {
    String[] instructions = {".data", "bytes: .byte 1,2", ".text", "bc2eqz $f1, label", "label: andi $t0, $t1, 5"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x49210004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBc2nez() {
    String[] instructions = {".text", "bc2nez $f1, label", "label: andi $t0, $t1, 5"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x49a10004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBeq() {
    String[] instructions = {".text", "beq $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x11090004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBeqc() {
    String[] instructions = {".text", "beqc $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x21090004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBeqzalc() {
    String[] instructions = {".text", "beqzalc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x20080004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBeqzc() {
    String[] instructions = {".text", "beqzc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0xd9000004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBgec() {
    String[] instructions = {".text", "bgec $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x59090004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBgezalc() {
    String[] instructions = {".text", "bgezalc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x19080004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBgezal() {
    String[] instructions = {".text", "bgezal $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x05110004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBgezc() {
    String[] instructions = {".text", "bgezc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x59080004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBgez() {
    String[] instructions = {".text", "bgez $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x05010004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBgeuc() {
    String[] instructions = {".text", "bgeuc $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x19090004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBgtzalc() {
    String[] instructions = {".text", "bgtzalc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x1c080004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBgtzc() {
    String[] instructions = {".text", "bgtzc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x5c080004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBgtz() {
    String[] instructions = {".text", "bgtz $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x1d000004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBitswap() {
    String[] instructions = {".text", "bitswap $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7c094020;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBlezalc() {
    String[] instructions = {".text", "blezalc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x18080004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBlezc() {
    String[] instructions = {".text", "blezc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x58080004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBlez() {
    String[] instructions = {".text", "blez $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x19000004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBltc() {
    String[] instructions = {".text", "bltc $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x5d090004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBltuc() {
    String[] instructions = {".text", "bltuc $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x1d090004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBltzalc() {
    String[] instructions = {".text", "bltzalc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x1d080004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBltzal() {
    String[] instructions = {".text", "bltzal $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x05100004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBltzc() {
    String[] instructions = {".text", "bltzc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x5d080004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBltz() {
    String[] instructions = {".text", "bltz $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x05000004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBne() {
    String[] instructions = {".text", "bne $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x15090004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBnec() {
    String[] instructions = {".text", "bnec $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x61090004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBnezalc() {
    String[] instructions = {".text", "bnezalc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x60080004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBnezc() {
    String[] instructions = {".text", "bnezc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0xf9000004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBnvc() {
    String[] instructions = {".text", "bnvc $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x61090004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBovc() {
    String[] instructions = {".text", "bovc $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x21090004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBreak() {
    String[] instructions = {".text", "break"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x0000000d;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCache() {
    String[] instructions = {".text", "cache 4, 2($t0)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d040125;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCachee() {
    String[] instructions = {".text", "cachee 4, 2($t0)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d04011b;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCeil_l_d() {
    String[] instructions = {".text", "ceil.l.d $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4620104a;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCeil_l_s() {
    String[] instructions = {".text", "ceil.l.s $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4600104a;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCeil_w_d() {
    String[] instructions = {".text", "ceil.w.d $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4620104e;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCeil_w_s() {
    String[] instructions = {".text", "ceil.w.s $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4600104e;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCfc1() {
    String[] instructions = {".text", "cfc1 $t0, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x44481000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCfc2() {
    String[] instructions = {".text", "cfc2 $t0, 3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x48480003;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testClass_d() {
    String[] instructions = {".text", "class.d $f1, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4620185b;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testClass_s() {
    String[] instructions = {".text", "class.s $f1, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4600185b;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testClo() {
    String[] instructions = {".text", "clo $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x01204051;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testClz() {
    String[] instructions = {".text", "clz $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x01204050;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_af_d() {
    String[] instructions = {".text", "cmp.af.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a31040;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_af_s() {
    String[] instructions = {".text", "cmp.af.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46831040;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_at_d() {
    String[] instructions = {".text", "cmp.at.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a31050;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_at_s() {
    String[] instructions = {".text", "cmp.at.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46831050;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_un_d() {
    String[] instructions = {".text", "cmp.un.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a31041;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_un_s() {
    String[] instructions = {".text", "cmp.un.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46831041;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_eq_d() {
    String[] instructions = {".text", "cmp.eq.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a31042;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_eq_s() {
    String[] instructions = {".text", "cmp.eq.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46831042;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_ueq_d() {
    String[] instructions = {".text", "cmp.ueq.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a31043;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_ueq_s() {
    String[] instructions = {".text", "cmp.ueq.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46831043;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_lt_d() {
    String[] instructions = {".text", "cmp.lt.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a31044;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_lt_s() {
    String[] instructions = {".text", "cmp.lt.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46831044;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_ult_d() {
    String[] instructions = {".text", "cmp.ult.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a31045;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_ult_s() {
    String[] instructions = {".text", "cmp.ult.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46831045;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_le_d() {
    String[] instructions = {".text", "cmp.le.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a31046;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_le_s() {
    String[] instructions = {".text", "cmp.le.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46831046;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_ule_d() {
    String[] instructions = {".text", "cmp.ule.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a31047;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_ule_s() {
    String[] instructions = {".text", "cmp.ule.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46831047;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_saf_d() {
    String[] instructions = {".text", "cmp.saf.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a31048;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_saf_s() {
    String[] instructions = {".text", "cmp.saf.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46831048;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_sun_d() {
    String[] instructions = {".text", "cmp.sun.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a31049;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_sun_s() {
    String[] instructions = {".text", "cmp.sun.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46831049;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_seq_d() {
    String[] instructions = {".text", "cmp.seq.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a3104a;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_seq_s() {
    String[] instructions = {".text", "cmp.seq.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4683104a;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_sueq_d() {
    String[] instructions = {".text", "cmp.sueq.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a3104b;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_sueq_s() {
    String[] instructions = {".text", "cmp.sueq.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4683104b;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_slt_d() {
    String[] instructions = {".text", "cmp.slt.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a3104c;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_slt_s() {
    String[] instructions = {".text", "cmp.slt.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4683104c;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_sult_d() {
    String[] instructions = {".text", "cmp.sult.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a3104d;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_sult_s() {
    String[] instructions = {".text", "cmp.sult.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4683104d;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_sle_d() {
    String[] instructions = {".text", "cmp.sle.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a3104e;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_sle_s() {
    String[] instructions = {".text", "cmp.sle.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4683104e;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_sule_d() {
    String[] instructions = {".text", "cmp.sule.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a3104f;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_sule_s() {
    String[] instructions = {".text", "cmp.sule.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4683104f;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_or_d() {
    String[] instructions = {".text", "cmp.or.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a31051;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_or_s() {
    String[] instructions = {".text", "cmp.or.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46831051;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_une_d() {
    String[] instructions = {".text", "cmp.une.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a31052;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_une_s() {
    String[] instructions = {".text", "cmp.une.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46831052;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_ne_d() {
    String[] instructions = {".text", "cmp.ne.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a31053;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_ne_s() {
    String[] instructions = {".text", "cmp.ne.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46831053;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_uge_d() {
    String[] instructions = {".text", "cmp.uge.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a31054;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_uge_s() {
    String[] instructions = {".text", "cmp.uge.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46831054;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_oge_d() {
    String[] instructions = {".text", "cmp.oge.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a31055;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_oge_s() {
    String[] instructions = {".text", "cmp.oge.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46831055;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_ugt_d() {
    String[] instructions = {".text", "cmp.ugt.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a31056;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_ugt_s() {
    String[] instructions = {".text", "cmp.ugt.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46831056;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_ogt_d() {
    String[] instructions = {".text", "cmp.ogt.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a31057;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_ogt_s() {
    String[] instructions = {".text", "cmp.ogt.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46831057;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_sat_d() {
    String[] instructions = {".text", "cmp.sat.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a31058;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_sat_s() {
    String[] instructions = {".text", "cmp.sat.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46831058;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_sor_d() {
    String[] instructions = {".text", "cmp.sor.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a31059;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_sor_s() {
    String[] instructions = {".text", "cmp.sor.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46831059;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_sune_d() {
    String[] instructions = {".text", "cmp.sune.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a3105a;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_sune_s() {
    String[] instructions = {".text", "cmp.sune.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4683105a;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_sne_d() {
    String[] instructions = {".text", "cmp.sne.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a3105b;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_sne_s() {
    String[] instructions = {".text", "cmp.sne.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4683105b;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_suge_d() {
    String[] instructions = {".text", "cmp.suge.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a3105c;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_suge_s() {
    String[] instructions = {".text", "cmp.suge.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4683105c;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_soge_d() {
    String[] instructions = {".text", "cmp.soge.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a3105d;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_soge_s() {
    String[] instructions = {".text", "cmp.soge.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4683105d;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_sugt_d() {
    String[] instructions = {".text", "cmp.sugt.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a3105e;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_sugt_s() {
    String[] instructions = {".text", "cmp.sugt.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4683105e;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_sogt_d() {
    String[] instructions = {".text", "cmp.sogt.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a3105f;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCmp_sogt_s() {
    String[] instructions = {".text", "cmp.sogt.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4683105f;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCop2() {
    String[] instructions = {".text", "cop2 6"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4a000006;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCrc32b() {
    String[] instructions = {".text", "crc32b $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d28000f;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCrc32h() {
    String[] instructions = {".text", "crc32h $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d28004f;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCrc32w() {
    String[] instructions = {".text", "crc32w $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d28008f;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCrc32cb() {
    String[] instructions = {".text", "crc32cb $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d28010f;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCrc32ch() {
    String[] instructions = {".text", "crc32ch $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d28014f;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCrc32cw() {
    String[] instructions = {".text", "crc32cw $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d28018f;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCtc1() {
    String[] instructions = {".text", "ctc1 $t0, $f1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x44c80800;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCtc2() {
    String[] instructions = {".text", "ctc2 $t0, 3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x48c80003;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCvt_d_l() {
    String[] instructions = {".text", "cvt.d.l $f1, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a01861;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCvt_d_s() {
    String[] instructions = {".text", "cvt.d.s $f1, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46001861;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCvt_d_w() {
    String[] instructions = {".text", "cvt.d.w $f1, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46801861;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCvt_l_d() {
    String[] instructions = {".text", "cvt.l.d $f1, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46201865;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCvt_l_s() {
    String[] instructions = {".text", "cvt.l.s $f1, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46001865;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCvt_s_d() {
    String[] instructions = {".text", "cvt.s.d $f1, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46201860;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCvt_s_l() {
    String[] instructions = {".text", "cvt.s.l $f1, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46a01860;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCvt_s_w() {
    String[] instructions = {".text", "cvt.s.w $f1, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46801860;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCvt_w_d() {
    String[] instructions = {".text", "cvt.w.d $f1, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46201864;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testCvt_w_s() {
    String[] instructions = {".text", "cvt.w.s $f1, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46001864;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testDeret() {
    String[] instructions = {".text", "deret"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4200001f;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testDi() {
    String[] instructions = {".text", "di $t0"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x41686000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testDiv() {
    String[] instructions = {".text", "div $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x0109001a;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testDiv_d() {
    String[] instructions = {".text", "div.d $f3, $f2, $f1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x462110c3;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testDiv_s() {
    String[] instructions = {".text", "div.s $f3, $f2, $f1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x460110c3;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testDivu() {
    String[] instructions = {".text", "divu $t2, $t1, $t0"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x0128509a;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testDvp() {
    String[] instructions = {".text", "dvp $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x416a0024;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testEi() {
    String[] instructions = {".text", "ei $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x416a6020;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testEret() {
    String[] instructions = {".text", "eret $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x42000018;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testEretnc() {
    String[] instructions = {".text", "eretnc $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x42000058;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testEvp() {
    String[] instructions = {".text", "evp $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x416a0004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testExt() {
    String[] instructions = {".text", "ext $t2, $t1, 4, 8"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d2a4100;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testFloor_l_d() {
    String[] instructions = {".text", "floor.l.d $f2, $f1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4620088b;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testFloor_l_s() {
    String[] instructions = {".text", "floor.l.s $f2, $f1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4600088b;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testFloor_w_d() {
    String[] instructions = {".text", "floor.w.d $f2, $f1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4620088f;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testFloor_w_s() {
    String[] instructions = {".text", "floor.w.s $f2, $f1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4600088f;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testGinvi() {
    String[] instructions = {".text", "ginvi $t0"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d00003d;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testGinvt() {
    String[] instructions = {".text", "ginvt $t0, 3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d0003bd;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testIns() {
    String[] instructions = {".text", "ins $t2, $t1, 4, 8"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d2a4104;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testJ() {
    String[] instructions = {".text", "j label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x08000004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testJal() {
    String[] instructions = {".text", "jal label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x0c000004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testJalr() {
    String[] instructions = {".text", "jalr $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x01204009;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testJalrhb() {
    String[] instructions = {".text", "jalr.hb $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x01204409;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testJic() {
    String[] instructions = {".text", "jic $t0, label", "label: la $t1, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0xd8080004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testJialc() {
    String[] instructions = {".text", "jialc $t0, label", "label: la $t1, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0xf8080004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testJr() {
    String[] instructions = {".text", "jr $t0"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x01000009;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testJrhb() {
    String[] instructions = {".text", "jr.hb $t0"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x01000409;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testlb() {
    String[] instructions = {".text", "lb $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x81280004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testlbe() {
    String[] instructions = {".text", "lbe $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d28022c;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testlbu() {
    String[] instructions = {".text", "lbu $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x91280004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testlbue() {
    String[] instructions = {".text", "lbue $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d280228;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testldc1() {
    String[] instructions = {".text", "ldc1 $f1, 4($t0)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0xd5010004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testldc2() {
    String[] instructions = {".text", "ldc2 $t1, 4($t0)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x49c94004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testlh() {
    String[] instructions = {".text", "lh $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x85280004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testlhe() {
    String[] instructions = {".text", "lhe $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d28022d;
    assertEquals(expectedEncoding, actualEncoding);
  }


  @Test
  public void testlhu() {
    String[] instructions = {".text", "lhu $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x95280004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testlhue() {
    String[] instructions = {".text", "lhue $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d280229;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testll() {
    String[] instructions = {".text", "ll $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d280236;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testlle() {
    String[] instructions = {".text", "lle $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d28022e;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testllwp() {
    String[] instructions = {".text", "llwp $t0, $t2, ($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d285076;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testllwpe() {
    String[] instructions = {".text", "llwpe $t0, $t2, ($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d28506e;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testlsa() {
    String[] instructions = {".text", "lsa $t0, $t2, $t1, 2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x01494085;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testlui() {
    String[] instructions = {".text", "lui $t0, 4"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x3c080004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testlw() {
    String[] instructions = {".text", "lw $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x8d280004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testlwc1() {
    String[] instructions = {".text", "lwc1 $f1, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0xc5210004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testlwc2() {
    String[] instructions = {".text", "lwc2 $t1, 4($t0)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x49494004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testlwe() {
    String[] instructions = {".text", "lwe $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d28022f;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testlwl() {
    String[] instructions = {".text", "lwl $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x89280004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testlwpc() {
    String[] instructions = {".text", "lwpc $t0, 4"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0xed080004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testlwr() {
    String[] instructions = {".text", "lwr $t0, 4($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x99280004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmadd() {
    String[] instructions = {".text", "madd $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x71090000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmaddf_d() {
    String[] instructions = {".text", "maddf.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46231058;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmaddf_s() {
    String[] instructions = {".text", "maddf.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46031058;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmaddu() {
    String[] instructions = {".text", "maddu $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x71090001;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmax_d() {
    String[] instructions = {".text", "max.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4623105e;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmax_s() {
    String[] instructions = {".text", "max.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4603105e;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmaxa_d() {
    String[] instructions = {".text", "maxa.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4623105f;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmaxa_s() {
    String[] instructions = {".text", "maxa.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4603105f;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmfc0() {
    String[] instructions = {".text", "mfc0 $t0, $t1, 3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x40084803;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmfc1() {
    String[] instructions = {".text", "mfc1 $t0, $f1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x44080800;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmfc2() {
    String[] instructions = {".text", "mfc2 $t0, 1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x48080001;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmfhc0() {
    String[] instructions = {".text", "mfhc0 $t0, $t1, 3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x40484803;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmfhc1() {
    String[] instructions = {".text", "mfhc1 $t0, $f1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x44680800;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmfhc2() {
    String[] instructions = {".text", "mfhc2 $t0, 1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x48680001;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmfhi() {
    String[] instructions = {".text", "mfhi $t0"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x00004010;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmflo() {
    String[] instructions = {".text", "mflo $t0"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x00004012;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmin_d() {
    String[] instructions = {".text", "min.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4623105c;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmin_s() {
    String[] instructions = {".text", "min.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4603105c;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmina_d() {
    String[] instructions = {".text", "mina.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4623105d;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmina_s() {
    String[] instructions = {".text", "mina.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4603105d;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmod() {
    String[] instructions = {".text", "mod $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x012a40da;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmodu() {
    String[] instructions = {".text", "modu $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x012a40db;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmovd() {
    String[] instructions = {".text", "mov.d $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46201046;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmovs() {
    String[] instructions = {".text", "mov.s $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46001046;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmovn() {
    String[] instructions = {".text", "movn $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x012a400b;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmovz() {
    String[] instructions = {".text", "movz $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x012a400a;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmsubf_d() {
    String[] instructions = {".text", "msubf.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46231059;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmsubf_s() {
    String[] instructions = {".text", "msubf.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46031059;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmsub() {
    String[] instructions = {".text", "msub $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x71090004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmsubu() {
    String[] instructions = {".text", "msubu $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x71090005;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmtc0() {
    String[] instructions = {".text", "mtc0 $t0, $t1, 3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x40884803;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmtc1() {
    String[] instructions = {".text", "mtc1 $t0, $f1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x44880800;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmtc2() {
    String[] instructions = {".text", "mtc2 $t0, 1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x48880001;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmthc0() {
    String[] instructions = {".text", "mthc0 $t0, $t1, 3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x40c84803;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmthc1() {
    String[] instructions = {".text", "mthc1 $t0, $f1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x44e80800;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmthc2() {
    String[] instructions = {".text", "mthc2 $t0, 1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x48e80001;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmthi() {
    String[] instructions = {".text", "mthi $t0"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x01000011;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmtlo() {
    String[] instructions = {".text", "mtlo $t0"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x01000013;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmuh() {
    String[] instructions = {".text", "muh $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x012a40d8;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmuhu() {
    String[] instructions = {".text", "muhu $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x012a40d9;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmul() {
    String[] instructions = {".text", "mul $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x012a4098;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmulu() {
    String[] instructions = {".text", "mulu $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x012a4099;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmuld() {
    String[] instructions = {".text", "mul.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46231042;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmuls() {
    String[] instructions = {".text", "mul.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46031042;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmult() {
    String[] instructions = {".text", "mult $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x01090018;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testmultu() {
    String[] instructions = {".text", "multu $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x01090019;
    assertEquals(expectedEncoding, actualEncoding);
  }

  public String toLineDelimited(String[] array) {
    StringBuilder builder = new StringBuilder();
    for (String ins : array) {
      builder.append(ins).append('\n');
    }

    return builder.toString();
  }
}
