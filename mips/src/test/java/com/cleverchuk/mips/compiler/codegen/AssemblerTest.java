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
    int expectedEncoding = 0x10000000;
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
    int expectedEncoding = 0x11000000;
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
    int expectedEncoding = 0x15000000;
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
    int expectedEncoding = 0x0411fffd;
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
  public void testAddiu() {
    String[] instructions = {".text", "addiu $t0, $t1, 1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x25280001;
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
    int expectedEncoding = 0xebfffffe;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBc() {
    String[] instructions = {".text", "label: andi $t0, $t1, 5", "bc label"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset() + 4);
    int expectedEncoding = 0xcbfffffe;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBc1eqz() {
    String[] instructions = {
      ".data", "bytes: .byte 1,2", ".text", "bc1eqz $f1, label", "label: andi $t0, $t1, 5"
    };
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x45210000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBc1nez() {
    String[] instructions = {".text", "bc1nez $f1, label", "label: andi $t0, $t1, 5"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x45a10000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBc2eqz() {
    String[] instructions = {
      ".data", "bytes: .byte 1,2", ".text", "bc2eqz $f1, label", "label: andi $t0, $t1, 5"
    };
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x49210000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBc2nez() {
    String[] instructions = {".text", "bc2nez $f1, label", "label: andi $t0, $t1, 5"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x49a10000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBeq() {
    String[] instructions = {".text", "beq $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x11090000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBeqc() {
    String[] instructions = {".text", "beqc $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x21090000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBeqzalc() {
    String[] instructions = {".text", "beqzalc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x20080000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBeqzc() {
    String[] instructions = {".text", "beqzc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0xd9000000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBgec() {
    String[] instructions = {".text", "bgec $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x59090000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBgezalc() {
    String[] instructions = {".text", "bgezalc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x19080000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBgezal() {
    String[] instructions = {".text", "bgezal $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x05110000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBgezc() {
    String[] instructions = {".text", "bgezc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x59080000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBgez() {
    String[] instructions = {".text", "bgez $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x05010000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBgeuc() {
    String[] instructions = {".text", "bgeuc $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x19090000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBgtzalc() {
    String[] instructions = {".text", "bgtzalc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x1c080000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBgtzc() {
    String[] instructions = {".text", "bgtzc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x5c080000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBgtz() {
    String[] instructions = {".text", "bgtz $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x1d000000;
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
    int expectedEncoding = 0x18080000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBlezc() {
    String[] instructions = {".text", "blezc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x58080000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBlez() {
    String[] instructions = {".text", "blez $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x19000000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBltc() {
    String[] instructions = {".text", "bltc $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x5d090000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBltuc() {
    String[] instructions = {".text", "bltuc $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x1d090000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBltzalc() {
    String[] instructions = {".text", "bltzalc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x1d080000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBltzal() {
    String[] instructions = {".text", "bltzal $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x05100000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBltzc() {
    String[] instructions = {".text", "bltzc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x5d080000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBltz() {
    String[] instructions = {".text", "bltz $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x05000000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBne() {
    String[] instructions = {".text", "bne $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x15090000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBnec() {
    String[] instructions = {".text", "bnec $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x61090000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBnezalc() {
    String[] instructions = {".text", "bnezalc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x60080000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBnezc() {
    String[] instructions = {".text", "bnezc $t0, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0xf9000000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBnvc() {
    String[] instructions = {".text", "bnvc $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x61090000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testBovc() {
    String[] instructions = {".text", "bovc $t0, $t1, label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x21090000;
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
    int expectedEncoding = 0x48480000;
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
    int expectedEncoding = 0x48c80000;
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
    String[] instructions = {".text", "div $t2, $t1, $t0"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x0128509a;
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
    int expectedEncoding = 0x0128509b;
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
    int expectedEncoding = 0x08000001;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testJal() {
    String[] instructions = {".text", "jal label", "label: la $t4, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x0c000001;
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
    int expectedEncoding = 0xd8080000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testJialc() {
    String[] instructions = {".text", "jialc $t0, label", "label: la $t1, bytes"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0xf8080000;
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
    int expectedEncoding = 0x48080000;
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
    int expectedEncoding = 0x48680000;
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
    int expectedEncoding = 0x48880000;
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
    int expectedEncoding = 0x48e80000;
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

  @Test
  public void testnal() {
    String[] instructions = {".text", "nal $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x04100000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testnegd() {
    String[] instructions = {".text", "neg.d $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46201047;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testnegs() {
    String[] instructions = {".text", "neg.s $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46001047;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testnor() {
    String[] instructions = {".text", "nor $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x012a4027;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testor() {
    String[] instructions = {".text", "or $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x012a4025;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testori() {
    String[] instructions = {".text", "ori $t0, $t1, 5"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x35280005;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testpause() {
    String[] instructions = {".text", "pause"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x00000140;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testpref() {
    String[] instructions = {".text", "pref 3, 1($t0)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d0300b5;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testprefe() {
    String[] instructions = {".text", "prefe 3, 1($t0)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d0300a3;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testrdhwr() {
    String[] instructions = {".text", "rdhwr $t0, $t1, 1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7c08487b;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testrdpgpr() {
    String[] instructions = {".text", "rdpgpr $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x41494000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testrecipd() {
    String[] instructions = {".text", "recip.d $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46201055;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testrecips() {
    String[] instructions = {".text", "recip.s $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46001055;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testrintd() {
    String[] instructions = {".text", "rint.d $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4620105a;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testrints() {
    String[] instructions = {".text", "rint.s $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4600105a;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testrotr() {
    String[] instructions = {".text", "rotr $t0, $t1, 1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x00294042;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testrotrv() {
    String[] instructions = {".text", "rotrv $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x01494046;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testroundld() {
    String[] instructions = {".text", "round.l.d $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46201048;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testroundls() {
    String[] instructions = {".text", "round.l.s $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46001048;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testroundwd() {
    String[] instructions = {".text", "round.w.d $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4620104c;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testroundws() {
    String[] instructions = {".text", "round.w.s $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4600104c;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testrsqrtd() {
    String[] instructions = {".text", "rsqrt.d $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46201056;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testrsqrts() {
    String[] instructions = {".text", "rsqrt.s $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46001056;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testsb() {
    String[] instructions = {".text", "sb $t0, 3($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0xa1280003;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testsbe() {
    String[] instructions = {".text", "sbe $t0, 3($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d28019c;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testsc() {
    String[] instructions = {".text", "sc $t0, 3($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d2801a6;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testsce() {
    String[] instructions = {".text", "sce $t0, 3($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d28019e;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testscwp() {
    String[] instructions = {".text", "scwp $t0, $t2, ($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d285066;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testscwpe() {
    String[] instructions = {".text", "scwpe $t0, $t2, ($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d28505e;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testsdc1() {
    String[] instructions = {".text", "sdc1 $f1, 3($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0xf5210003;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testsdbbp() {
    String[] instructions = {".text", "sdbbp 3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0xce;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testseb() {
    String[] instructions = {".text", "seb $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7c094420;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testseh() {
    String[] instructions = {".text", "seh $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7c094620;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testseld() {
    String[] instructions = {".text", "sel.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46231050;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testsels() {
    String[] instructions = {".text", "sel.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46031050;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testseleqz() {
    String[] instructions = {".text", "seleqz $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x012a4035;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testselnez() {
    String[] instructions = {".text", "selnez $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x012a4037;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testseleqzd() {
    String[] instructions = {".text", "seleqz.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46231054;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testseleqzs() {
    String[] instructions = {".text", "seleqz.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46031054;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testselnezd() {
    String[] instructions = {".text", "selnez.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46231057;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testselnezs() {
    String[] instructions = {".text", "selnez.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46031057;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testsh() {
    String[] instructions = {".text", "sh $t0, 3($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0xa5280003;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testshe() {
    String[] instructions = {".text", "she $t0, 3($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d28019d;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testsigrie() {
    String[] instructions = {".text", "sigrie 3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x04170003;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testsll() {
    String[] instructions = {".text", "sll $t0, $t1, 3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x000940c0;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testsllv() {
    String[] instructions = {".text", "sllv $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x01494004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testslt() {
    String[] instructions = {".text", "slt $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x012a402a;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testslti() {
    String[] instructions = {".text", "slti $t0, $t1, 3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x29280003;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testsltiu() {
    String[] instructions = {".text", "sltiu $t0, $t1, 3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x2d280003;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testsltu() {
    String[] instructions = {".text", "sltu $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x012a402b;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testsra() {
    String[] instructions = {".text", "sra $t0, $t1, 2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x00094083;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testsrav() {
    String[] instructions = {".text", "srav $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x01494007;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testsrl() {
    String[] instructions = {".text", "srl $t0, $t1, 2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x00094082;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testsrlv() {
    String[] instructions = {".text", "srlv $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x01494006;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testssnop() {
    String[] instructions = {".text", "ssnop"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x00000040;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testsqrtd() {
    String[] instructions = {".text", "sqrt.d $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46201044;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testsqrts() {
    String[] instructions = {".text", "sqrt.s $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46001044;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testsub() {
    String[] instructions = {".text", "sub $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x012a4022;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testsubd() {
    String[] instructions = {".text", "sub.d $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46231041;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testsubs() {
    String[] instructions = {".text", "sub.s $f1, $f2, $f3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46031041;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testsubu() {
    String[] instructions = {".text", "subu $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x012a4023;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testsw() {
    String[] instructions = {".text", "sw $t0, 3($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0xad280003;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testswc1() {
    String[] instructions = {".text", "swc1 $f1, 3($t0)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0xe5010003;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testswc2() {
    String[] instructions = {".text", "swc2 $t0, 3($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x49684803;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testswe() {
    String[] instructions = {".text", "swe $t0, 3($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7d28019f;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testswl() {
    String[] instructions = {".text", "swl $t0, 3($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0xa9280003;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testswr() {
    String[] instructions = {".text", "swr $t0, 3($t1)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0xb9280003;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testsync() {
    String[] instructions = {".text", "sync 1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x0000004f;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testsynci() {
    String[] instructions = {".text", "synci 3($t0)"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x051f0003;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testsyscall() {
    String[] instructions = {".text", "syscall"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0xc;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testteq() {
    String[] instructions = {".text", "teq $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x01090034;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testtge() {
    String[] instructions = {".text", "tge $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x01090030;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testtlbinv() {
    String[] instructions = {".text", "tlbinv"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x42000003;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testtlbinvf() {
    String[] instructions = {".text", "tlbinvf"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x42000004;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testtlbp() {
    String[] instructions = {".text", "tlbp"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x42000008;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testtlbr() {
    String[] instructions = {".text", "tlbr"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x42000001;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testtlbwi() {
    String[] instructions = {".text", "tlbwi"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x42000002;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testtlbwr() {
    String[] instructions = {".text", "tlbwr"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x42000006;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testtlt() {
    String[] instructions = {".text", "tlt $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x01090032;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testtltu() {
    String[] instructions = {".text", "tltu $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x01090033;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testtne() {
    String[] instructions = {".text", "tne $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x01090036;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testtruncld() {
    String[] instructions = {".text", "trunc.l.d $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46201049;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testtruncls() {
    String[] instructions = {".text", "trunc.l.s $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x46001049;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testtruncwd() {
    String[] instructions = {".text", "trunc.w.d $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4620104d;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testtruncws() {
    String[] instructions = {".text", "trunc.w.s $f1, $f2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x4600104d;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testwait() {
    String[] instructions = {".text", "wait"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x42000020;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testwrpgpr() {
    String[] instructions = {".text", "wrpgpr $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x41c94000;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testwsbh() {
    String[] instructions = {".text", "wsbh $t0, $t1"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x7c0940a0;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testxor() {
    String[] instructions = {".text", "xor $t0, $t1, $t2"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x012a4026;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testxori() {
    String[] instructions = {".text", "xori $t0, $t1, 3"};
    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    int actualEncoding = layout.readWord(tested.getTextOffset());
    int expectedEncoding = 0x39280003;
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testLabelDeclaredBeforeUse() {
    String[] instructions = {
      "# This code calculates the arithmetic mean of the array",
      ".data",
      "array: .double 1.0, 2.0, 3.0, 4.0, 5.0, 6.0  # Array of floating-point numbers",
      "len: .word 6                              # Length of the array",
      "",
      ".text",
      "mtc1 $zero, $f12     # Set $f12 low 32-bits to 0.0 (initialize the sum to 0.0)",
      "mthc1 $zero, $f12    # Set $f12 high 32-bits to 0.0 (initialize the sum to 0.0)",
      "",
      "la $t4, array          # Load the address of the array into $t4",
      "li $t2, 48           # Load the size of the array in bytes",
      "",
      "loop:",
      "bnez $t2, 1          # If $t2 is not zero, continue to the next iteration of the loop",
      "",
      "j end                # Jump to the end of the program if $t2 is zero",
      "addiu $t2, $t2, -8   # Decrement $t2 by 8 (move to the previous element)",
      "add $t3, $t4, $t2    # Calculate the address of the current element of the array",
      "",
      "ldc1 $f10, 0($t3)    # Load the value of the current element into $f10",
      "add.d $f12, $f12, $f10  # Add the value of the current element to the sum in $f12",
      "j loop               # Jump back to the beginning of the loop",
      "",
      "end:",
      "la $t4, len          # Load the address of the length variable into $t4",
      "lwc1 $f10, 0($t4)   # Load the value of the length into $f10",
      "",
      "cvt.d.w $f10, $f10   # Convert the integer length to double precision",
      "div.d $f12, $f12, $f10  # Divide the sum by the length to calculate the mean",
      "li $v0, 3            # Set $v0 to 3 (system call code for printing a floating-point number)",
      "syscall",
      "",
      "li $a0, 10           # Set $a0 to 10 (ASCII code for newline character)",
      "li $v0, 11           # Set $v0 to 11 (system call code for printing a character)",
      "syscall",
    };

    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    // data: 6 doubles (48 bytes) + 1 word (4 bytes) => text starts at 52
    assertEquals(0, (int) tested.getSymbolTable().get("array"));
    assertEquals(48, (int) tested.getSymbolTable().get("len"));
    assertEquals(52, tested.getTextOffset());

    // text: mtc1, mthc1, la (2 words), li => loop is the 6th word of text
    assertEquals(72, (int) tested.getSymbolTable().get("loop"));
    // bnez, j, addi, add, ldc1, add.d, j => end is 7 words after loop
    assertEquals(100, (int) tested.getSymbolTable().get("end"));

    // j end => target address / 4
    int actualEncoding = layout.readWord(tested.getTextOffset() + 24);
    int expectedEncoding = 0x08000000 | (100 / 4);
    assertEquals(expectedEncoding, actualEncoding);

    // j loop => target address / 4
    actualEncoding = layout.readWord(tested.getTextOffset() + 44);
    expectedEncoding = 0x08000000 | (72 / 4);
    assertEquals(expectedEncoding, actualEncoding);
  }

  @Test
  public void testGeometricMeanTutorial() {
    String[] instructions = {
      "#### This code computes the geometric mean of the array ####",
      ".data",
      "array: .double 1, 2, 3, 4, 5, 6    # Array of numbers",
      "length: .word 6                  # Length of the array",
      "eps:    .double 0.001",
      "",
      ".text",
      ".globl main",
      "",
      "main:",
      "# Initialize variables",
      "la $t0, array                # Load the address of the array into $t0",
      "la $t1, length               # Load the address of length into $t1",
      "lw $t1, 0($t1)               # Load the value of length into $t1",
      "li $t2, 1                    # Initialize $t2 with 1",
      "",
      "mtc1 $t2, $f1                # Move $t2 to the floating-point register $f1",
      "cvt.d.w $f1, $f1             # Convert the integer value in $f1 to double precision",
      "",
      "loop:",
      "beqz $t1, end_loop           # If length is zero, exit the loop",
      "ldc1 $f2, 0($t0)             # Load the double value at the current index of array into $f2",
      "mul.d $f1, $f1, $f2          # Multiply the product with the current element",
      "addiu $t0, $t0, 8            # Increment the array pointer by 8 bytes (64 bits)",
      "addiu $t1, $t1, -1           # Decrement the length by 1",
      "j loop                       # Jump back to the loop",
      "",
      "end_loop:",
      "addiu $sp, $sp, -16          # Allocate space on the stack",
      "la $t1, length               # Load the address of length into $t1",
      "lw $t1, 0($t1)               # Load the value of length into $t1",
      "",
      "sdc1 $f1, 0($sp)             # Store the product value on the stack",
      "sw $t1, 8($sp)               # Store the length value on the stack",
      "jal binary_search            # Jump and link to the binary_search function",
      "",
      "# Print the result",
      "li $v0, 3                    # System call code for printing float",
      "syscall",
      "",
      "# Print the line",
      "li $v0, 11                   # System call code for printing character",
      "li $a0, 10                   # Load the ASCII code for a new line into $a0",
      "syscall",
      "",
      "# Exit the program",
      "li $v0, 10                   # System call code for exit",
      "syscall",
      "",
      "# Functions",
      "pow_n:",
      "ldc1 $f20, 0($sp)            # Load the number from the stack",
      "ldc1 $f21, 0($sp)            # Load the number from the stack",
      "lw $t0, 8($sp)               # Load n from the stack",
      "",
      "pow_n_loop:",
      "addiu $t0, $t0, -1           # Decrement n by 1",
      "bnez $t0, pow_n_continue     # If n is not zero, continue",
      "mov.d $f12, $f21             # Move the result (ans) to the output register",
      "jr $ra                       # Return",
      "",
      "pow_n_continue:",
      "mul.d $f21, $f21, $f20       # Multiply ans by the number",
      "j pow_n_loop                 # Jump back to pow_n_loop",
      "",
      "binary_search:",
      "ldc1 $f1, 0($sp)             # Load the number from the stack",
      "ldc1 $f10, 0($sp)            # Load the number from the stack",
      "lw $t0, 8($sp)               # Load n from the stack",
      "li $t2, 1                    # Initialize low = 1.0",
      "",
      "mtc1 $t2, $f2                # Move $t2 to the floating-point register $f2",
      "cvt.d.w $f2, $f2             # Convert the integer value in $f2 to double precision",
      "la $t2, eps                  # Load the address of eps into $t2",
      "",
      "ldc1 $f3, 0($t2)             # Load the value of eps into $f3",
      "li $t3, 2                    # Load 2 into $t3",
      "mtc1 $t3, $f8                # Move $t3 to the floating-point register $f8",
      "cvt.d.w $f8, $f8             # Convert the integer value in $f8 to double precision",
      "",
      "bs_loop:",
      "sub.d $f4, $f1, $f2          # Calculate high - low",
      "cmp.le.d $f5, $f4, $f3       # Compare (high - low) with eps",
      "bc1eqz $f5, 1                # If (high - low) <= eps, exit the loop",
      "j bs_exit                    # Jump to bs_exit",
      "",
      "add.d $f6, $f2, $f1          # Calculate low + high",
      "div.d $f7, $f6, $f8          # Calculate (low + high) / 2.0",
      "addiu $sp, $sp, -52          # Allocate space on the stack",
      "",
      "# Preserve registers",
      "sw $ra, 48($sp)",
      "sdc1 $f10, 40($sp)           # Push low onto the stack",
      "sdc1 $f2, 32($sp)            # Push low onto the stack",
      "sdc1 $f1, 24($sp)            # Push high onto the stack",
      "",
      "sdc1 $f8, 16($sp)            # Push 2.0 onto the stack",
      "sw $t0, 8($sp)               # Push n onto the stack",
      "sdc1 $f7, 0($sp)             # Push mid onto the stack",
      "",
      "jal pow_n                    # Call the pow_n function",
      "",
      "# Restore registers",
      "lw $ra, 48($sp)",
      "ldc1 $f10, 40($sp)           # Pop low from the stack",
      "ldc1 $f2, 32($sp)            # Pop low from the stack",
      "ldc1 $f1, 24($sp)            # Pop high from the stack",
      "",
      "ldc1 $f8, 16($sp)            # Pop 2.0 from the stack",
      "lw $t0, 8($sp)               # Pop n from the stack",
      "ldc1 $f7, 0($sp)             # Pop mid from the stack",
      "addiu $sp, $sp, 52           # Deallocate space on the stack",
      "",
      "cmp.lt.d $f9, $f12, $f10     # Compare pow_n(mid, n) with high",
      "bc1eqz $f9, 1                # If pow_n(mid, n) < high, jump to bs_low",
      "j bs_low                     # Jump to bs_low",
      "mov.d $f1, $f7               # Set high = mid",
      "j bs_loop                    # Jump back to bs_loop",
      "",
      "bs_low:",
      "mov.d $f2, $f7               # Set low = mid",
      "j bs_loop                    # Jump back to bs_loop",
      "",
      "bs_exit:",
      "mov.d $f12, $f2              # Set low = mid",
      "jr $ra                       # Return",
    };

    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    // data: 6 doubles (48) + 1 word (4) + 1 double (8) => text starts at 60
    assertEquals(0, (int) tested.getSymbolTable().get("array"));
    assertEquals(48, (int) tested.getSymbolTable().get("length"));
    assertEquals(52, (int) tested.getSymbolTable().get("eps"));
    assertEquals(60, tested.getTextOffset());

    assertEquals(60, (int) tested.getSymbolTable().get("main"));
    // main: la (2), la (2), lw, li, mtc1, cvt.d.w => 8 words
    assertEquals(92, (int) tested.getSymbolTable().get("loop"));
    // loop: beqz, ldc1, mul.d, addiu, addiu, j => 6 words
    assertEquals(116, (int) tested.getSymbolTable().get("end_loop"));
    // end_loop: addiu, la (2), lw, sdc1, sw, jal, li, syscall,
    // li, li, syscall, li, syscall => 14 words
    assertEquals(172, (int) tested.getSymbolTable().get("pow_n"));
    // pow_n: ldc1, ldc1, lw => 3 words
    assertEquals(184, (int) tested.getSymbolTable().get("pow_n_loop"));
    // pow_n_loop: addiu, bnez, mov.d, jr => 4 words
    assertEquals(200, (int) tested.getSymbolTable().get("pow_n_continue"));
    // pow_n_continue: mul.d, j => 2 words
    assertEquals(208, (int) tested.getSymbolTable().get("binary_search"));
    // binary_search: ldc1, ldc1, lw, li, mtc1, cvt.d.w, la (2), ldc1, li, mtc1, cvt.d.w => 12 words
    assertEquals(256, (int) tested.getSymbolTable().get("bs_loop"));
    // bs_loop: 28 single-word instructions
    assertEquals(368, (int) tested.getSymbolTable().get("bs_low"));
    // bs_low: mov.d, j => 2 words
    assertEquals(376, (int) tested.getSymbolTable().get("bs_exit"));

    // j loop is the 6th word after loop (92 + 5 * 4 = 112)
    assertEquals(0x08000000 | (92 / 4), layout.readWord(112));
    // jal binary_search is the 7th word after end_loop (116 + 6 * 4 = 140)
    assertEquals(0x0c000000 | (208 / 4), layout.readWord(140));
    // j pow_n_loop is the 2nd word after pow_n_continue (200 + 4)
    assertEquals(0x08000000 | (184 / 4), layout.readWord(204));
    // j bs_exit is the 4th word after bs_loop (256 + 3 * 4 = 268)
    assertEquals(0x08000000 | (376 / 4), layout.readWord(268));
    // jal pow_n is the 15th word after bs_loop (256 + 14 * 4 = 312)
    assertEquals(0x0c000000 | (172 / 4), layout.readWord(312));
    // beqz $t1, end_loop at loop (92): pc-relative offset (116 - 92 - 4) / 4 = 5
    assertEquals(0x10000000 | 9 << 21 | 5, layout.readWord(92));
  }

  @Test
  public void testHypotenuseTutorial() {
    String[] instructions = {
      "#### This code computes the hypothenus given x and y ####",
      ".text",
      ".globl main",
      "",
      "main:",
      "# Load default values for side1 and side2",
      "li $s0, 3        # Load 3 into $s0",
      "li $s1, 4        # Load 4 into $s1",
      "",
      "# Calculate the squares of side1 and side2",
      "mul $t0, $s0, $s0    # side1 * side1",
      "mul $t1, $s1, $s1    # side2 * side2",
      "",
      "# Calculate the sum of the squares",
      "add $t2, $t0, $t1    # side1^2 + side2^2",
      "",
      "# Calculate the square root of the sum",
      "mtc1 $t2, $f12",
      "cvt.s.w $f12, $f12",
      "sqrt.s $f12, $f12      # Square root of the sum",
      "",
      "# Print the result",
      "li $v0, 2            # System call code for printing integer",
      "syscall",
      "",
      "# Print the line",
      "li $v0, 11          # System call code for printing character",
      "li $a0, 10      # load new line ascii code to $a0",
      "syscall",
      "",
      "# Exit the program",
      "li $v0, 10           # System call code for exit",
      "syscall",
    };

    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    assertEquals(0, tested.getTextOffset());
    assertEquals(0, (int) tested.getSymbolTable().get("main"));

    // li $s0, 3 => ori $s0, $zero, 3
    assertEquals(0x34000000 | 16 << 16 | 3, layout.readWord(0));
    // li $v0, 10 => ori $v0, $zero, 10 is the 15th word
    assertEquals(0x34000000 | 2 << 16 | 10, layout.readWord(52));
    // final syscall is the 16th word
    assertEquals(0x0000000c, layout.readWord(56));
  }

  @Test
  public void testPowerTutorial() {
    String[] instructions = {
      "#### This code calculates x to the power ####",
      ".text",
      ".globl main",
      "main:",
      "# Store x and n in registers",
      "li $s0, 2         # Move x to $s0",
      "li $s1, 2         # Move n to $s1",
      "",
      "# Initialize result to 1",
      "li $t0, 1             # Load 1 into $t0",
      "",
      "loop:",
      "beqz $s1, exit     # Exit loop if n == 0",
      "mul $t0, $t0, $s0  # Multiply result by x",
      "addiu $s1, $s1, -1    # Decrement n by 1",
      "j loop             # Jump to loop label",
      "",
      "exit:",
      "# Print the result",
      "li $v0, 1          # System call code for printing integer",
      "move $a0, $t0      # Move result to $a0",
      "syscall",
      "",
      "# Print the line",
      "li $v0, 11          # System call code for printing character",
      "li $a0, 10      # load new line ascii code to $a0",
      "syscall",
      "",
      "# Exit the program",
      "li $v0, 10         # System call code for exit",
      "syscall",
    };

    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    assertEquals(0, tested.getTextOffset());
    assertEquals(0, (int) tested.getSymbolTable().get("main"));
    // main: li, li, li => 3 words
    assertEquals(12, (int) tested.getSymbolTable().get("loop"));
    // loop: beqz, mul, addiu, j => 4 words
    assertEquals(28, (int) tested.getSymbolTable().get("exit"));

    // beqz $s1, exit at loop (12): pc-relative offset (28 - 12 - 4) / 4 = 3
    assertEquals(0x10000000 | 17 << 21 | 3, layout.readWord(12));
    // j loop is the 4th word after loop (12 + 3 * 4 = 24)
    assertEquals(0x08000000 | (12 / 4), layout.readWord(24));
  }

  @Test
  public void testNewtonApproximationTutorial() {
    String[] instructions = {
      "#### This snippet computes the root of the function y = x^2 - x"
          + " using Newton approximation ####",
      ".data",
      "x0: .float 1.5       # Initial guess for the root",
      "epsilon: .float 0.001 # Error tolerance",
      "",
      ".text",
      ".globl main",
      "",
      "main:",
      "# Load initial guess and error tolerance",
      "la $t0, x0",
      "lwc1 $f0, 0($t0)         # Load x0 into $f0",
      "la $t0, epsilon",
      "lwc1 $f1, 0($t0)   # Load epsilon into $f1",
      "",
      "loop:",
      "# Compute the function value and its derivative at x0",
      "mul.s $f2, $f0, $f0     # Compute x0^2",
      "sub.s $f3, $f2, $f0     # Compute x0^2 - x0 (function value)",
      "addiu $sp, $sp, -4      # Allocate space on the stack",
      "swc1 $f0, 0($sp)       # Store x0 on the stack",
      "addiu $sp, $sp, -4      # Allocate space on the stack",
      "swc1 $f2, 0($sp)       # Store x0^2 on the stack",
      "addiu $sp, $sp, -4      # Allocate space on the stack",
      "swc1 $f3, 0($sp)       # Store function value on the stack",
      "jal calculate_derivative # Jump to calculate_derivative function",
      "addiu $sp, $sp, 12      # Deallocate the stack space",
      "",
      "# Calculate the new approximation using Newton's method",
      "div.s $f6, $f3, $f6   # Divide function value by derivative",
      "sub.s $f0, $f0, $f6   # Subtract the division result from x0",
      "",
      "# Check if the approximation is within the error tolerance",
      "abs.s $f12, $f6        # Take the absolute value of the division result",
      "cmp.lt.s $f12, $f12, $f1   # Compare the absolute value with epsilon",
      "bc1eqz $f12, 1             # Branch if true to the exit label",
      "j exit",
      "",
      "# Repeat the loop",
      "j loop",
      "",
      "calculate_derivative:",
      "lwc1 $f4, 8($sp)       # Load x0 from the stack",
      "lwc1 $f5, 4($sp)       # Load x0^2 from the stack",
      "li $t4, 2",
      "mtc1 $t4, $f12",
      "cvt.s.w $f12, $f12",
      "mul.s $f6, $f4, $f12   # Compute 2 * x0",
      "li $t4, 1",
      "mtc1 $t4, $f12",
      "cvt.s.w $f12, $f12",
      "sub.s $f6, $f6, $f12   # Compute 2 * x0  - 1 (derivative value)",
      "jr $ra",
      "",
      "exit:",
      "# Print the result",
      "li $v0, 2             # System call code for printing float",
      "mov.s $f12, $f0     # Load the result from memory to $f12",
      "syscall",
      "",
      "# Print the line",
      "li $v0, 11          # System call code for printing character",
      "li $a0, 10      # load new line ascii code to $a0",
      "syscall",
      "",
      "# Exit the program",
      "li $v0, 10            # System call code for exit",
      "syscall",
    };

    parser.parse(toLineDelimited(instructions));
    Memory layout = tested.getLayout();

    // data: 2 floats (8 bytes) => text starts at 8
    assertEquals(0, (int) tested.getSymbolTable().get("x0"));
    assertEquals(4, (int) tested.getSymbolTable().get("epsilon"));
    assertEquals(8, tested.getTextOffset());

    assertEquals(8, (int) tested.getSymbolTable().get("main"));
    // main: la (2), lwc1, la (2), lwc1 => 6 words
    assertEquals(32, (int) tested.getSymbolTable().get("loop"));
    // loop: 17 single-word instructions
    assertEquals(100, (int) tested.getSymbolTable().get("calculate_derivative"));
    // calculate_derivative: 11 single-word instructions
    assertEquals(144, (int) tested.getSymbolTable().get("exit"));

    // jal calculate_derivative is the 9th word after loop (32 + 8 * 4 = 64)
    assertEquals(0x0c000000 | (100 / 4), layout.readWord(64));
    // j exit is the 16th word after loop (32 + 15 * 4 = 92)
    assertEquals(0x08000000 | (144 / 4), layout.readWord(92));
    // j loop is the 17th word after loop (32 + 16 * 4 = 96)
    assertEquals(0x08000000 | (32 / 4), layout.readWord(96));
  }

  public String toLineDelimited(String[] array) {
    StringBuilder builder = new StringBuilder();
    for (String ins : array) {
      builder.append(ins).append('\n');
    }

    return builder.toString();
  }
}
