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

  public String toLineDelimited(String[] array) {
    StringBuilder builder = new StringBuilder();
    for (String ins : array) {
      builder.append(ins).append('\n');
    }

    return builder.toString();
  }
}
