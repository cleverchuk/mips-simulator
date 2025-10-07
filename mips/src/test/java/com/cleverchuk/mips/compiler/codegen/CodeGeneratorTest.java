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

import static org.junit.Assert.assertNotNull;

import com.cleverchuk.mips.compiler.lexer.MipsLexer;
import com.cleverchuk.mips.compiler.parser.Node;
import com.cleverchuk.mips.compiler.parser.RecursiveDescentParser;
import com.cleverchuk.mips.compiler.semantic.SemanticAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.FourOpAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.InstructionAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.OneOpAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.ThreeOpAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.TwoOpAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.ZeroOpAnalyzer;
import com.cleverchuk.mips.simulator.mem.BigEndianMainMemory;
import org.junit.Test;

public class CodeGeneratorTest {
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

  @Test
  public void codeGen() {
    String source = ".text\n" + "ins $t0, $t0, 0, 2\n" + "lw $t0, 02*2-2*4($t1)\n";

    Node program = parser.parse(source);
    CodeGenerator codeGenerator = new CodeGenerator(new BigEndianMainMemory(1024));

    assertNotNull(program);
    codeGenerator.generate(program);
  }

  @Test
  public void codeGenExt() {
    String source = ".text\n" + "li $t0, -15\n" + "ext $t0, $t0, 0, 5";

    Node program = parser.parse(source);
    CodeGenerator codeGenerator = new CodeGenerator(new BigEndianMainMemory(1024));

    assertNotNull(program);
    codeGenerator.generate(program);
  }

  @Test
  public void codeGen0() {
    String source =
        ".text\n"
            + "add $t0, $t1, $t2 # comment\n"
            + "# hello no op\n"
            + "addi $t0, $t1, 400\n"
            + "beq $t0, $t1, label\n"
            + "lw $t0, 2($t1   )\n"
            + "sw $t0, 67 (   $sp )\n"
            + "li $t0, 300\n"
            + "la $t0, label # comment\n"
            + "jal label\n"
            + "return:jr $ra\n"
            + "addi $t0, $zero, 300\n"
            + "add $t0, $t1,             $zero\n"
            + "li $v0,                       1\n"
            + "syscall\n"
            + ".data\n"
            + "dummy0: .space 5\n"
            + "dummy1: .byte 10\n"
            + "dummy2: .half 10\n";

    Node program = parser.parse(source);
    CodeGenerator codeGenerator = new CodeGenerator(new BigEndianMainMemory(1024));

    assertNotNull(program);
    codeGenerator.generate(program);
  }

  @Test
  public void codeGenData() {
    String source =
        ".data\n"
            + "dummy0: .word 5, 6, 7, 8\n"
            + "dummy1: .half 5, 6, 7, 8\n"
            + "dummy2: .asciiz \"hello world\"\n";
    Node program = parser.parse(source);
    CodeGenerator codeGenerator = new CodeGenerator(new BigEndianMainMemory(1024));

    assertNotNull(program);
    codeGenerator.generate(program);
  }
}
