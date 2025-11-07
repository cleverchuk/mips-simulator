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

package com.cleverchuk.mips;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.util.SparseIntArray;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.cleverchuk.mips.compiler.MipsCompiler;
import com.cleverchuk.mips.compiler.codegen.Assembler;
import com.cleverchuk.mips.compiler.codegen.CodeGenerator;
import com.cleverchuk.mips.compiler.lexer.MipsLexer;
import com.cleverchuk.mips.compiler.parser.ErrorRecorder;
import com.cleverchuk.mips.compiler.parser.RecursiveDescentParser;
import com.cleverchuk.mips.compiler.semantic.SemanticAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.FourOpAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.InstructionAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.OneOpAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.ThreeOpAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.TwoOpAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.ZeroOpAnalyzer;
import com.cleverchuk.mips.simulator.MipsSimulator;
import com.cleverchuk.mips.simulator.cpu.CpuRegisterFile;
import com.cleverchuk.mips.simulator.fpu.FpuRegisterFileArray;
import com.cleverchuk.mips.simulator.mem.BigEndianMainMemory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@SuppressWarnings("All")
@RunWith(AndroidJUnit4.class)
public class MipsSimulatorTest {

  private MipsSimulator mipsSimulator;

  private Context context;

  private CpuRegisterFile registerFile;

  private FpuRegisterFileArray fpuRegisterFileArray;

  private final String[] instructions = {
    ".text",
    "add $t0, $t1, $t2 # comment",
    "addi $t0, $t1, 400",
    "beq $t0, $t1, label",
    "lw $t0, 2($t1   )",
    "sw $t0, 67 (   $sp )",
    "li $t0, 300",
    "la $t0, label # comment",
    "jal label",
    "return:jr $ra",
    "addi $t0, $zero, 300",
    "add $t0, $t1,             $zero",
    "li $v0,                       1",
    "syscall",
    "             "
  };

  @Before
  public void setup() throws Exception {
    context = InstrumentationRegistry.getInstrumentation().getTargetContext();
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

    parser.addVisitor(new Assembler());
    BigEndianMainMemory bigEndianMainMemory = new BigEndianMainMemory(1024);
    mipsSimulator =
        new MipsSimulator(
            new Handler(context.getMainLooper()),
            new MipsCompiler(parser, new CodeGenerator(bigEndianMainMemory)),
            bigEndianMainMemory,
            (byte) 0x2);

    registerFile = mipsSimulator.getCpu().getRegisterFile();
    fpuRegisterFileArray = (mipsSimulator.getCop()).registerFiles();
    mipsSimulator.start();
  }

  @After
  public void tearDown() {
    if (mipsSimulator != null) {
      mipsSimulator.shutDown();
    }
  }

  public String toLineDelimited(String[] array) {

    StringBuilder builder = new StringBuilder();
    for (String ins : array) {
      builder.append(ins).append('\n');
    }

    return builder.toString();
  }

  @Test
  public void testLw() {
    String[] instructions = {
      ".data", "label: .word 5,6,7", ".text", "li $t0, 300", "la $s1, label", "lw $s1, 0($s1)"
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;

    long val = registerFile.read("$t0");
    assertEquals(300, val);
    val = registerFile.read("$s0");

    assertEquals(0, val);
    val = registerFile.read("$s1");
    assertEquals(5, val);
  }

  @Test
  public void testLwAsm() {
    String[] instructions = {".data", "label: .word 5,6,7", ".text", "lw $s1, label"};

    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;

    int val = registerFile.read("$s1");
    assertEquals(5, val);
  }

  @Test
  public void testCodeWithGlobl() {
    String[] instructions = {
      ".data",
      "label: .word 5,6,7",
      ".text",
      ".globl main",
      "main: ",
      "li $t0, 300",
      "la $s1, label",
      "lw $s1, 0($s1)"
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    long val = registerFile.read("$t0");
    assertEquals(300, val);
    val = registerFile.read("$s0");
    assertEquals(0, val);
    val = registerFile.read("$s1");
    assertEquals(5, val);
  }

  @Test
  public void testSw() {
    String[] instructions = {
      ".data",
      "label: .word 30, 50,10",
      ".text",
      "li $t0, 300",
      "la $t1, label",
      "lw $t2, 2+2*4-6($t1)",
      "add $t0, $t0, $t2",
      "sw $t0, 2*4+2-6($t1)"
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    long val = registerFile.read("$t0");
    assertEquals(350, val);
    val = registerFile.read("$t2");
    assertEquals(50, val);
  }

  @Test
  public void testMul() {
    String[] instructions = {".text", "li $t0, 3", "li $t1, 4", "mul $s0, $t0, $t1"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(12, registerFile.read("$s0"));
  }

  @Test
  public void testDiv() {
    String[] instructions = {".text", "li $t0, 12", "li $t1, 4", "div $t0, $t1"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(3, registerFile.accLO());
  }

  @Test
  public void testAdd() {
    String[] instructions = {".text", "li $t1, 500", "li $t0, 300", "add $s1, $t0, $t1"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(800, registerFile.read("$s1"));
  }

  @Test
  public void testAddi() {
    String[] instructions = {".text", "addi $t0, $t0, 300", "addi $s1, $t0, 54"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(300, registerFile.read("$t0"));
    assertEquals(354, registerFile.read("$s1"));
  }

  @Test
  public void testSub() {
    String[] instructions = {".text", "li $t0, 3", "li $t1, 4", "sub $s0, $t0, $t1"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(-1, registerFile.read("$s0"));
  }

  @Test
  public void testSubu() {
    String[] instructions = {".text", "li $t0, 3", "li $t1, 4", "subu $s0, $t0, $t1"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(-1, registerFile.read("$s0"));
  }

  @Test
  public void testLi() {
    String[] instructions = {".text", "li $t1, 500", "li $t0, 300", "add $s1, $t0, $t1"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(800, registerFile.read("$s1"));
  }

  @Test
  public void testLa() {
    String[] instructions = {
      ".text", "label: li $t1, 500", "la $t0, label",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(0, registerFile.read("$t0"));
  }

  @Test
  public void testAddiu() {
    String[] instructions = {".text", "addiu $t0, $t0, 300", "addiu $s1, $t0, 54"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(300, registerFile.read("$t0"));
    assertEquals(354, registerFile.read("$s1"));
  }

  @Test
  public void testAddiuWithNegNumbers() {
    String[] instructions = {".text", "addiu $t0, $t0, -5", "addiu $s1, $t0, 54"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(-5, registerFile.read("$t0"));
    assertEquals(49, registerFile.read("$s1"));
  }

  @Test
  public void testAddu() {
    String[] instructions = {".text", "li $t1, 500", "li $t0, 300", "addu $s1, $t0, $t1"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(800, registerFile.read("$s1"));
  }

  @Test
  public void testAdduWithOverflow() {
    String[] instructions = {
      ".data", "word: .word 2147483647", ".text", "lw $t1, word", "li $t0, 1", "addu $s1, $t0, $t1"
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(Integer.MIN_VALUE, registerFile.read("$s1"));
  }

  @Test
  public void testClo() {
    String[] instructions = {".text", "addiu $t0, $t0, 10", "clo $s1, $t0"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(1, registerFile.read("$s1"));
  }

  @Test
  public void testClz() {
    String[] instructions = {".text", "addiu $t0, $t0, 1", "clz $s1, $t0"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(31, registerFile.read("$s1"));
  }

  @Test
  public void testLui() {
    String[] instructions = {".text", "lui $s1, 300"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(300 << 16, registerFile.read("$s1"));
  }

  @Test
  public void testLuiWithInvalidNumber() {
    String[] instructions = {".text", "lui $s1, 66000"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    assertEquals(0, registerFile.read("$s1"));
    assertTrue(ErrorRecorder.hasErrors());
  }

  @Test
  public void testMove() {
    String[] instructions = {".text", "li $s1, 300", "move $t0, $s1"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(300, registerFile.read("$t0"));
  }

  @Test
  public void testNegu() {
    String[] instructions = {".text", "li $s1, 300", "negu $t0, $s1"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(-300, registerFile.read("$t0"));
  }

  @Test
  public void testSll() {
    String[] instructions = {".text", "li $t0, 300", "sll $t0, $t0, 3"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(300 << 3, registerFile.read("$t0"));
  }

  @Test
  public void testSllInvalidNumber() {
    String[] instructions = {".text", "li $t0, 32", "sll $t0, $t0, 32"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(0, registerFile.read("$t0"));
    assertTrue(ErrorRecorder.hasErrors());
  }

  @Test
  public void testSrl() {
    String[] instructions = {".text", "li $t0, -300", "srl $t0, $t0, 3"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertTrue(registerFile.read("$t0") > 0);
  }

  @Test
  public void testBne() {
    String[] instructions = {
      ".text", "li $t1, 1", "bne $t1, $t0, 1", "li $t0, 300", "addi $s1, $t0, 54"
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(54, registerFile.read("$s1"));
  }

  @Test
  public void testBneWithInvalidNumber() {
    String[] instructions = {
      ".text", "li $t1, 1", "bne $t1, $t0, 131073", "li $t0, 300", "addi $s1, $t0, 54"
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    assertEquals(0, registerFile.read("$s1"));
    assertTrue(ErrorRecorder.hasErrors());
  }

  @Test
  public void testBeq() {
    String[] instructions = {".text", "beq $t1, $t0, 1", "li $t0, 300", "addi $s1, $t0, 54"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(54, registerFile.read("$s1"));
  }

  @Test
  public void testJ() {
    String[] instructions = {".text", "j label", "li $t0, 300", "label: addi $s1, $t0, 54"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(54, registerFile.read("$s1"));
  }

  @Test
  public void testJr() {
    String[] instructions = {
      ".text", "jal label", "li $v0, 10", "syscall", "label: addi $s1, $t0, 54", "jr $ra"
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(54, registerFile.read("$s1"));
  }

  @Test
  public void testJal() {
    String[] instructions = {
      ".text", "jal label", "li $v0, 10", "syscall", "label: addi $s1, $t0, 54", "jr $ra"
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(54, registerFile.read("$s1"));
  }

  @Test
  public void testSllv() {
    String[] instructions = {".text", "li $t0, 2", "li $t1, 5", "sllv $t0, $t1, $t0"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(20, registerFile.read("$t0"));
  }

  @Test
  public void testMovn() {
    String[] instructions = {".text", "li $t0, 2", "li $t1, 5", "movn $t0, $t1, $t0"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(5, registerFile.read("$t0"));
  }

  @Test
  public void testMovz() {
    String[] instructions = {".text", "li $t0, 0", "li $t1, 5", "movz $t0, $t1, $t0"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(5, registerFile.read("$t0"));
  }

  @Test
  public void testSlt() {
    String[] instructions = {".text", "li $t0, 2", "li $t1, 5", "slt $t0, $t0, $t1"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(1, registerFile.read("$t0"));
  }

  @Test
  public void testSltu() {
    String[] instructions = {".text", "li $t0, 2", "li $t1, -5", "sltu $t0, $t0, $t1"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(1, registerFile.read("$t0"));
  }

  @Test
  public void testSlti() {
    String[] instructions = {".text", "li $t0, 2", "li $t1, 5", "slti $t0, $t1, 17"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(1, registerFile.read("$t0"));
  }

  @Test
  public void testSltiu() {
    String[] instructions = {".text", "li $t0, 2", "li $t1, -15", "sltiu $t0, $t1, 10"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(0, registerFile.read("$t0"));
  }

  @Test
  public void testAnd() {
    String[] instructions = {".text", "li $t0, 2", "li $t1, 0", "and $t0, $t1, $t0"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(0, registerFile.read("$t0"));
  }

  @Test
  public void testAndi() {
    String[] instructions = {".text", "li $t0, 2", "andi $t0, $t0, 3"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(2, registerFile.read("$t0"));
  }

  @Test
  public void testNor() {
    String[] instructions = {".text", "li $t0, 2", "li $t1, 3", "nor $t0, $t1, $t0"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(-4, registerFile.read("$t0"));
  }

  @Test
  public void testNot() {
    String[] instructions = {".text", "li $t0, 2", "not $t0, $t0"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(-3, registerFile.read("$t0"));
  }

  @Test
  public void testOr() {
    String[] instructions = {".text", "li $t0, 2", "li $t1, 3", "or $t0, $t1, $t0"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(3, registerFile.read("$t0"));
  }

  @Test
  public void testOri() {
    String[] instructions = {".text", "li $t0, 2", "ori $t0, $t0, 10"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(10, registerFile.read("$t0"));
  }

  @Test
  public void testXor() {
    String[] instructions = {".text", "li $t0, 2", "li $t1, 3", "xor $t0, $t1, $t0"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(1, registerFile.read("$t0"));
  }

  @Test
  public void testXori() {
    String[] instructions = {".text", "li $t0, 2", "xori $t0, $t0, 0"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(2, registerFile.read("$t0"));
  }

  @Test
  public void testExt() {
    Log.i("-15 bit string", Integer.toBinaryString(-15));
    String[] instructions = {".text", "li $t0, -15", "ext $t0, $t0, 0, 5"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(17, registerFile.read("$t0"));
  }

  @Test
  public void testIns() {
    String[] instructions = {".text", "li $t0, -15", "ins $t0, $t0, 0, 2"};
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(-15, registerFile.read("$t0"));

    instructions = new String[] {".text", "li $t0, 15", "li $t1, 10", "ins $t0, $t1, 1, 3"};

    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(11, registerFile.read("$t0"));
  }

  @Test
  public void testSpaceStorage() {
    String[] instructions = {
      ".data",
      "bytes: .space 12",
      ".text",
      "la $t4, bytes",
      "li $t3, 20",
      "sw $t3, 0($t4)",
      "li $t3, 21",
      "sw $t3, 4($t4)",
      "li $t3, 22",
      "sw $t3, 8($t4)",
      "lw $t0, 0($t4)",
      "lw $t1, 4($t4)",
      "lw $t2, 8($t4)",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;

    long val = registerFile.read("$t0");
    assertEquals(20, val);
    val = registerFile.read("$t1");

    assertEquals(21, val);
    val = registerFile.read("$t2");
    assertEquals(22, val);
  }

  @Test
  public void testlb() {
    String[] instructions = {
      ".data",
      "bytes: .byte 1, 2, 3, 4, 5",
      "string: .ascii \"hello word\"",
      "space: .space 10",
      ".text",
      "la $t4, bytes",
      "lb $t0, 0($t4)",
      "lb $t1, 4($t4)"
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;

    long val = registerFile.read("$t0");
    assertEquals(1, val);
    val = registerFile.read("$t1");
    assertEquals(5, val);
  }

  @Test
  public void testlbu() {
    String[] instructions = {
      ".data",
      "bytes: .byte -1, 2, 3, 4, 5",
      ".text",
      "la $t4, bytes",
      "lbu $t0, 0($t4)",
      "lbu $t1, 4($t4)"
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    int val = registerFile.read("$t0");

    assertTrue(val > 0);
    val = registerFile.read("$t1");
    assertEquals(5, val);
  }

  @Test
  public void testlh() {
    String[] instructions = {
      ".data", "bytes: .byte 1, 2, 3, 4, 5", ".text", "la $t4, bytes", "lh $t0, 0($t4)",
    };

    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;

    long val = registerFile.read("$t0");
    assertEquals(258, val);
  }

  @Test
  public void testlhu() {
    String[] instructions = {
      ".data", "bytes: .byte -1, 2, 3, 4, 5", ".text", "la $t4, bytes", "lhu $t0, 0($t4)"
    };

    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;

    long val = registerFile.read("$t0");
    assertEquals(65282, val);
  }

  @Test
  public void testlwl() {
    String[] instructions = {
      ".data", "bytes: .byte 0, 0, 0, 4, 5", ".text", "la $t4, bytes", "lwl $t0, 0($t4)",
    };

    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;

    long val = registerFile.read("$t0");
    assertEquals(4, val);
  }

  @Test
  public void testlwr() {
    String[] instructions = {
      ".data", "bytes: .byte 0, 0, 0, 4, 5", ".text", "la $t4, bytes", "lwr $t0, 0($t4)",
    };

    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;

    long val = registerFile.read("$t0");
    assertEquals(4, val);
  }

  @Test
  public void testsb() {
    String[] instructions = {
      ".data",
      "bytes: .byte 1, 2, 3, 4, 5",
      ".text",
      "la $t4, bytes",
      "li $t0, 10",
      "sb $t0, 0($t4)",
      "lb $t1, 0($t4)"
    };

    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;

    long val = registerFile.read("$t1");
    assertEquals(10, val);
  }

  @Test
  public void testsh() {
    String[] instructions = {
      ".data",
      "bytes: .byte 1, 2, 3, 4, 5",
      ".text",
      "la $t4, bytes",
      "li $t0, 10",
      "sh $t0, 0($t4)",
      "lh $t1, 0($t4)"
    };

    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;

    long val = registerFile.read("$t1");
    assertEquals(10, val);
  }

  @Test
  public void testswl() {
    String[] instructions = {
      ".data",
      "bytes: .byte 1, 2, 3, 4, 5",
      ".text",
      "la $t4, bytes",
      "li $t0, 10",
      "swl $t0, 0($t4)",
      "lwl $t1, 0($t4)"
    };

    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;

    long val = registerFile.read("$t1");
    assertEquals(10, val);
  }

  @Test
  public void testswr() {
    String[] instructions = {
      ".data",
      "bytes: .byte 1, 2, 3, 4, 5",
      ".text",
      "la $t4, bytes",
      "li $t0, 10",
      "swr $t0, 0($t4)",
      "lwr $t1, 0($t4)"
    };

    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;

    long val = registerFile.read("$t1");
    assertEquals(10, val);
  }

  @Test
  public void testulw() {
    String[] instructions = {
      ".data", "bytes: .byte 1, 0, 0, 0, 5", ".text", "la $t4, bytes", "ulw $t0, 1($t4)",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    long val = registerFile.read("$t0");
    assertEquals(5, val);
  }

  @Test
  public void testusw() {
    String[] instructions = {
      ".data",
      "bytes: .byte 1, 2, 3, 4, 5",
      ".text",
      "la $t4, bytes",
      "li $t0, 10",
      "usw $t0, 0($t4)",
      "ulw $t1, 0($t4)"
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    long val = registerFile.read("$t1");
    assertEquals(10, val);
  }

  @Test
  public void testb() {
    String[] instructions = {
      ".data", "bytes: .byte 1, 2, 3, 4, 5", ".text", "b 1", "la $t4, bytes", "li $t0, 10"
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(10, registerFile.read("$t0"));
    assertEquals(0, registerFile.read("$t4"));
  }

  @Test
  public void testbal() {
    String[] instructions = {".text", "bal 2"};

    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(3, mipsSimulator.getPC());
    long val = registerFile.read("$ra");
    assertEquals(2, val);
  }

  @Test
  public void testbeqz() {
    String[] instructions = {".text", "li $t0, 0", "beqz $t0, 4"};

    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(6, mipsSimulator.getPC());

    instructions = new String[] {".text", "li $t0, 1", "beqz $t0, 4"};

    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(2, mipsSimulator.getPC());
  }

  @Test
  public void testbgez() {
    String[] instructions = {".text", "li $t0, 10", "bgez $t0, 10"};

    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(12, mipsSimulator.getPC());

    instructions = new String[] {".text", "li $t0, -10", "bgez $t0, 10"};

    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(2, mipsSimulator.getPC());
  }

  @Test
  public void testbgezal() {
    String[] instructions = {".text", "li $t0, 10", "bgezal $t0, 10"};

    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    long val = registerFile.read("$ra");
    assertEquals(3, val);
    assertEquals(12, mipsSimulator.getPC());

    instructions = new String[] {".text", "li $t0, -10", "bgezal $t0, 10"};

    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    val = registerFile.read("$ra");
    assertEquals(3, val);
    assertEquals(2, mipsSimulator.getPC());
  }

  @Test
  public void testbgtz() {
    String[] instructions = {".text", "li $t0, 10", "bgtz $t0, 20"};

    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(22, mipsSimulator.getPC());

    instructions = new String[] {".text", "li $t0, 0", "bgtz $t0, 20"};

    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(2, mipsSimulator.getPC());
  }

  @Test
  public void testblez() {
    String[] instructions = {
      ".text", "li $t0, 0", "blez $t0, 10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(12, mipsSimulator.getPC());

    instructions =
        new String[] {
          ".text", "li $t0, 10", "blez $t0, 10",
        };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(2, mipsSimulator.getPC());
  }

  @Test
  public void testbltz() {
    String[] instructions = {
      ".text", "li $t0, -10", "blez $t0, 10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(12, mipsSimulator.getPC());

    instructions =
        new String[] {
          ".text", "li $t0, 0", "bltz $t0, 10",
        };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(2, mipsSimulator.getPC());
  }

  @Test
  public void testbnez() {
    String[] instructions = {
      ".text", "li $t0, -10", "bnez $t0, 10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(12, mipsSimulator.getPC());

    instructions =
        new String[] {
          ".text", "li $t0, 10", "bnez $t0, 10",
        };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(12, mipsSimulator.getPC());

    instructions =
        new String[] {
          ".text", "li $t0, 0", "bnez $t0, 10",
        };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(2, mipsSimulator.getPC());
  }

  @Test
  public void testbltzal() {
    String[] instructions = {
      ".text", "li $t0, -10", "bltzal $t0, 10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    long val = registerFile.read("$ra");
    assertEquals(3, val);
    assertEquals(12, mipsSimulator.getPC());

    instructions =
        new String[] {
          ".text", "li $t0, 10", "bltzal $t0, 10",
        };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    val = registerFile.read("$ra");
    assertEquals(3, val);
    assertEquals(2, mipsSimulator.getPC());
  }

  @Test
  public void testjalr() {
    String[] instructions = {
      ".text", "li $t0, 10", "jalr $t1, $t0",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(10, mipsSimulator.getPC());
    assertEquals(3, registerFile.read("$t1"));
  }

  @Test
  public void testdivu() {
    String[] instructions = {
      ".text", "li $t0, -2", "li $t1, 2", "divu $t0, $t1",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(Integer.toUnsignedLong(-2) % 2, registerFile.accHI());
    assertEquals(Integer.toUnsignedLong(-2) / 2, registerFile.accLO());
  }

  @Test
  public void testmadd() {
    String[] instructions = {
      ".text", "li $t0, -2", "li $t1, 2", "madd $t0, $t1",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(-4, registerFile.getAccumulator());
  }

  @Test
  public void testmaddu() {
    String[] instructions = {
      ".text", "li $t0, -2", "li $t1, 2", "maddu $t0, $t1",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(Integer.toUnsignedLong(-2) * 2, registerFile.getAccumulator());
  }

  @Test
  public void testmsub() {
    String[] instructions = {
      ".text", "li $t0, -2", "li $t1, 2", "msub $t0, $t1",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(4, registerFile.getAccumulator());
  }

  @Test
  public void testmsubu() {
    String[] instructions = {
      ".text", "li $t0, -2", "li $t1, 2", "msubu $t0, $t1",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(Integer.toUnsignedLong(-2) * -2, registerFile.getAccumulator());
  }

  @Test
  public void testmult() {
    String[] instructions = {
      ".text", "li $t0, -2", "li $t1, 2", "mult $t0, $t1",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(-4, registerFile.getAccumulator());
  }

  @Test
  public void testmultu() {
    String[] instructions = {
      ".text", "li $t0, -2", "li $t1, 2", "multu $t0, $t1",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(Integer.toUnsignedLong(-2) * 2, registerFile.getAccumulator());
  }

  @Test
  public void testmfhi() {
    String[] instructions = {
      ".text", "li $t0, 3", "li $t1, 2", "div $t0, $t1", "mfhi $t0",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(1, registerFile.read("$t0"));
  }

  @Test
  public void testmflo() {
    String[] instructions = {
      ".text", "li $t0, 4", "li $t1, 2", "div $t0, $t1", "mflo $t0",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(2, registerFile.read("$t0"));
  }

  @Test
  public void testmthi() {
    String[] instructions = {
      ".text", "li $t0, -2", "mthi $t0",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(-2, registerFile.accHI());
  }

  @Test
  public void testmtlo() {
    String[] instructions = {
      ".text", "li $t0, -2", "mtlo $t0",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(-2, registerFile.accLO());
  }

  @Test
  public void testrotr() {
    String[] instructions = {
      ".text", "li $t1, 1", "rotr $t0, $t1, 2",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(1073741824, registerFile.read("$t0"));
  }

  @Test
  public void testrotrv() {
    String[] instructions = {
      ".text", "li $t1, 2", "li $t2, 2", "rotrv $t0, $t1, $t2",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(Integer.MIN_VALUE, registerFile.read("$t0"));
  }

  @Test
  public void testsra() {
    String[] instructions = {
      ".text", "li $t1, -2", "sra $t0, $t1, 2",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(-1, registerFile.read("$t0"));
  }

  @Test
  public void testsrav() {
    String[] instructions = {
      ".text", "li $t1, -2", "li $t2, 2", "srav $t0, $t1, $t2",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(-1, registerFile.read("$t0"));
  }

  @Test
  public void testsrlv() {
    String[] instructions = {
      ".text", "li $t1, -2", "li $t2, 2", "srlv $t0, $t1, $t2",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertTrue(registerFile.read("$t0") > 0);
  }

  @Test
  public void testseb() {
    String[] instructions = {
      ".text", "li $t1, -3", "seb $t0, $t1",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(-3, registerFile.read("$t0"));
  }

  @Test
  public void testseh() {
    String[] instructions = {
      ".text", "li $t1, -3", "seh $t0, $t1",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(-3, registerFile.read("$t0"));
  }

  @Test
  public void testwsbh() {
    String[] instructions = {
      ".text", "li $t1, 128", "wsbh $t0, $t1",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    assertEquals(32768, registerFile.read("$t0"));
  }

  @Test
  public void testll() {
    String[] instructions = {
      ".data", "bytes: .byte 0, 0, 0, 1, 5", ".text", "la $t4, bytes", "ll $t0, 0($t4)",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    long val = registerFile.read("$t0");
    assertEquals(1, val);
  }

  @Test
  public void testllWithNumberReg() {
    String[] instructions = {
      ".data", "bytes: .byte 0, 0, 0, 1, 5", ".text", "la $12, bytes", "ll $8, 0($12)",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    long val = registerFile.read("$t0");
    assertEquals(1, val);
  }

  @Test
  public void testsc() {
    String[] instructions = {
      ".data",
      "bytes: .byte 0, 0, 0, 1, 5",
      ".text",
      "la $t4, bytes",
      "li $t1, 2",
      "sc $t1, 0($t4)",
      "ll $t0, 0($t4)",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();
    while (mipsSimulator.isRunning())
      ;
    long val = registerFile.read("$t0");
    assertEquals(2, val);
  }

  @Test
  public void test_ldc1() {
    String[] instructions = {
      ".data", "fps: .word 0, 9, 1", ".text", "la $t4, fps", "ldc1 $f10, 0($t4)",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    long val = fpuRegisterFileArray.getFile("$f10").readDword();
    assertEquals(9, val);
  }

  @Test
  public void test_lwc1() {
    String[] instructions = {
      ".data", "fps: .word 5, 9, 1", ".text", "la $t4, fps", "lwc1 $f10, 0($t4)",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    int val = fpuRegisterFileArray.getFile("$f10").readWord();
    assertEquals(5, val);
  }

  @Test
  public void test_sdc1() {
    String[] instructions = {
      ".data",
      "fps: .word 5, 0, 0",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "sdc1 $f10, 0($t4)",
      "lwc1 $f10, 0x4($t4)",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    long val = fpuRegisterFileArray.getFile("$f10").readDword();
    assertEquals(5, val);
  }

  @Test
  public void test_swc1() {
    String[] instructions = {
      ".data",
      "fps: .word 5, 0, 0",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "swc1 $f10, 0x40($t4)",
      "lwc1 $f10, 0x40($t4)",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    long val = fpuRegisterFileArray.getFile("$f10").readDword();
    assertEquals(5, val);
  }

  @Test
  public void test_cfc1() {
    String[] instructions = {
      ".data",
      "fps: .word 5, 0x1004, 0x01000081",
      ".text",

      // load for first branch
      "la $t4, fps",
      "lwc1 $f31, 0($t4)",
      "cfc1 $t0, $f0",

      // load for second branch
      "la $t4, fps",
      "lwc1 $f31, 4($t4)",
      "cfc1 $t1, $f26",

      // load for third branch
      "la $t4, fps",
      "lwc1 $f31, 8($t4)",
      "cfc1 $t2, $f28",

      // load for fourth branch
      "la $t4, fps",
      "lwc1 $f31, 0($t4)",
      "cfc1 $t3, $f31",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    assertEquals(5, registerFile.read("$t0"));
    assertEquals(0x1004, registerFile.read("$t1"));

    assertEquals(0x00000085, registerFile.read("$t2"));
    assertEquals(5, registerFile.read("$t3"));
  }

  @Test
  public void test_ctc1() {
    String[] instructions = {
      ".data",
      "fps: .word 0, 0x1004, 0x00000081, 5",
      ".text",

      // load for first branch
      "la $t4, fps",
      "lwc1 $f31, 0($t4) # zero fcsr",
      "lw $t0, 4($t4)",
      "ctc1 $t0, $f26",
      "cfc1 $t0, $f26",

      // load for second branch
      "la $t4, fps",
      "lwc1 $f31, 0($t4) # zero fcsr",
      "lw $t1, 8($t4)",
      "ctc1 $t1, $f28",
      "cfc1 $t1, $f28",

      // load for third branch
      "la $t4, fps",
      "lwc1 $f31, 0($t4) # zero fcsr",
      "lw $t2, 12($t4)",
      "ctc1 $t2, $f31",
      "cfc1 $t2, $f31",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    assertEquals(0x1004, registerFile.read("$t0"));

    assertEquals(0x00000081, registerFile.read("$t1"));
    assertEquals(5, registerFile.read("$t2"));
  }

  @Test
  public void test_mfc1() {
    String[] instructions = {
      ".data", "fps: .word 5, 0, 0", ".text", "la $t4, fps", "lwc1 $f10, 0($t4)", "mfc1 $t1, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    long val = registerFile.read("$t1");
    assertEquals(5, val);
  }

  @Test
  public void test_mfhc1() {
    String[] instructions = {
      ".data",
      "fps: .double 0x0000000100000000, 0, 0",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "mfhc1 $t1, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    long val = registerFile.read("$t1");
    assertEquals(1106247680, val);
  }

  @Test
  public void test_mtc1() {
    String[] instructions = {
      ".data", "fps: .word 5, 0, 0", ".text", "la $t4, fps", "lw $t1, 0($t4)", "mtc1 $t1, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    int val = fpuRegisterFileArray.getFile("$f10").readWord();
    assertEquals(5, val);
  }

  @Test
  public void test_mthc1() {
    String[] instructions = {
      ".data",
      "fps: .word 0, 1, 0",
      ".text",
      "la $t4, fps",
      "lw $t1, 4($t4)",
      "ldc1 $f10, 0($t4)",
      "mthc1 $t1, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    long val = fpuRegisterFileArray.getFile("$f10").readDword();
    assertEquals(4294967297L, val);
  }

  @Test
  public void test_abs_s() {
    String[] instructions = {
      ".data",
      "fps: .word -5",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "cvt.s.w $f10, $f10",
      "abs.s $f10, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    float val = fpuRegisterFileArray.getFile("$f10").readSingle();
    assertEquals(5, val, 0.0);
  }

  @Test
  public void test_abs_d() {
    String[] instructions = {
      ".data",
      "fps: .word -5",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "cvt.d.w $f10, $f10",
      "abs.d $f10, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    double val = fpuRegisterFileArray.getFile("$f10").readDouble();
    assertEquals(5, val, 0.0);
  }

  @Test
  public void test_add_s() {
    String[] instructions = {
      ".data",
      "fps: .word -5",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 0($t4)",
      "cvt.s.w $f10, $f10",
      "cvt.s.w $f11, $f11",
      "add.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    float val = fpuRegisterFileArray.getFile("$f10").readSingle();
    assertEquals(-10, val, 0.0);
  }

  @Test
  public void test_add_d() {
    String[] instructions = {
      ".data",
      "fps: .word -5",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 0($t4)",
      "cvt.d.w $f10, $f10",
      "cvt.d.w $f11, $f11",
      "add.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    double val = fpuRegisterFileArray.getFile("$f10").readDouble();
    assertEquals(-10, val, 0.0);
  }

  @Test
  public void test_cmp_af_d() {
    String[] instructions = {
      ".data",
      "fps: .word -5",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 0($t4)",
      "cvt.d.w $f10, $f10",
      "cvt.d.w $f11, $f11",
      "cmp.af.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    double val = fpuRegisterFileArray.getFile("$f10").readDouble();
    assertEquals(0.0, val, 0.0);
  }

  @Test
  public void test_cmp_saf_d() {
    String[] instructions = {
      ".data",
      "fps: .word -5",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 0($t4)",
      "cvt.d.w $f10, $f10",
      "cvt.d.w $f11, $f11",
      "cmp.saf.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    double val = fpuRegisterFileArray.getFile("$f10").readDouble();
    assertEquals(0.0, val, 0.0);
  }

  @Test
  public void test_cmp_un_d() {
    String[] instructions = {
      ".data",
      "fps: .word 0x7ff80000, 00000000",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "ldc1 $f11, 0($t4)",
      "cmp.un.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readDouble() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_sun_d() {
    String[] instructions = {
      ".data",
      "fps: .word 0x7ff80000, 00000000",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "ldc1 $f11, 0($t4)",
      "cmp.sun.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readDouble() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_eq_d() {
    String[] instructions = {
      ".data",
      "fps: .word 0x7ff80000, 00000000",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 0($t4)",
      "cmp.eq.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readDouble() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_seq_d() {
    String[] instructions = {
      ".data",
      "fps: .word 0x7ff80000, 00000000",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 0($t4)",
      "cmp.seq.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readDouble() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_ueq_d() {
    String[] instructions = {
      ".data",
      "fps: .word 0x7ff80000, 00000000",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "ldc1 $f11, 0($t4)",
      "cmp.ueq.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readDouble() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_sueq_d() {
    String[] instructions = {
      ".data",
      "fps: .word 0x7ff80000, 00000000",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "ldc1 $f11, 0($t4)",
      "cmp.sueq.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readDouble() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_lt_d() {
    String[] instructions = {
      ".data",
      "fps: .double 3.8, 6.5",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "ldc1 $f11, 8($t4)",
      "cmp.lt.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readDouble() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_slt_d() {
    String[] instructions = {
      ".data",
      "fps: .double 5.8, 0.5",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 8($t4)",
      "ldc1 $f11, 0($t4)",
      "cmp.slt.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readDouble() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_ult_d() {
    String[] instructions = {
      ".data",
      "fps: .double 5.2, 0.5",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 8($t4)",
      "ldc1 $f11, 0($t4)",
      "cmp.ult.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readDouble() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_sult_d() {
    String[] instructions = {
      ".data",
      "fps: .double 5.8, 0.5",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 8($t4)",
      "ldc1 $f11, 0($t4)",
      "cmp.sult.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readDouble() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_le_d() {
    String[] instructions = {
      ".data",
      "fps: .double 5.0, 0.5",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 8($t4)",
      "ldc1 $f11, 0($t4)",
      "cmp.le.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readDouble() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_sle_d() {
    String[] instructions = {
      ".data",
      "fps: .double 5.8",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "ldc1 $f11, 0($t4)",
      "cmp.sle.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readDouble() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_ule_d() {
    String[] instructions = {
      ".data",
      "fps: .double 0x7ff8000000000000, 0.5",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 8($t4)",
      "ldc1 $f11, 0($t4)",
      "cmp.ule.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readDouble() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_sule_d() {
    String[] instructions = {
      ".data",
      "fps: .double 5.8",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "ldc1 $f11, 0($t4)",
      "cmp.sule.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readDouble() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_or_d() {
    String[] instructions = {
      ".data",
      "fps: .double 5, 0.5",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 8($t4)",
      "ldc1 $f11, 0($t4)",
      "cmp.or.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readDouble() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_sor_d() {
    String[] instructions = {
      ".data",
      "fps: .double 5.5",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "ldc1 $f11, 0($t4)",
      "cmp.sor.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readDouble() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_une_d() {
    String[] instructions = {
      ".data",
      "fps: .double 5",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "ldc1 $f11, 0($t4)",
      "cmp.une.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readDouble() != 0;
    assertFalse(val);
  }

  @Test
  public void test_cmp_sune_d() {
    String[] instructions = {
      ".data",
      "fps: .double 5, 6",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "ldc1 $f11, 8($t4)",
      "cmp.sune.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readDouble() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_ne_d() {
    String[] instructions = {
      ".data",
      "fps: .double 0x7ff8000000000000",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "ldc1 $f11, 0($t4)",
      "cmp.ne.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readDouble() != 0;
    assertFalse(val);
  }

  @Test
  public void test_cmp_sne_d() {
    String[] instructions = {
      ".data",
      "fps: .double 5, 6",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "ldc1 $f11, 8($t4)",
      "cmp.sne.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readDouble() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_af_s() {
    String[] instructions = {
      ".data",
      "fps: .word -5",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 0($t4)",
      "cvt.s.w $f10, $f10",
      "cvt.s.w $f11, $f11",
      "cmp.af.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    float val = fpuRegisterFileArray.getFile("$f10").readSingle();
    assertEquals(0.0, val, 0.0);
  }

  @Test
  public void test_cmp_saf_s() {
    String[] instructions = {
      ".data",
      "fps: .word -5",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 0($t4)",
      "cvt.s.w $f10, $f10",
      "cvt.s.w $f11, $f11",
      "cmp.saf.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    float val = fpuRegisterFileArray.getFile("$f10").readSingle();
    assertEquals(0.0, val, 0.0);
  }

  @Test
  public void test_cmp_un_s() {
    String[] instructions = {
      ".data",
      "fps: .word 0x7fc00000",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 0($t4)",
      "cmp.un.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readSingle() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_sun_s() {
    String[] instructions = {
      ".data",
      "fps: .word 0x7fc00000",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 0($t4)",
      "cmp.sun.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readSingle() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_eq_s() {
    String[] instructions = {
      ".data",
      "fps: .float 5.2",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 0($t4)",
      "cmp.eq.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readSingle() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_seq_s() {
    String[] instructions = {
      ".data",
      "fps: .word 0x7fc00000",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 0($t4)",
      "cmp.seq.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readSingle() == 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_ueq_s() {
    String[] instructions = {
      ".data",
      "fps: .word 0x7fc00000",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 0($t4)",
      "cmp.ueq.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readSingle() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_sueq_s() {
    String[] instructions = {
      ".data",
      "fps: .word 0x7fc00000",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 0($t4)",
      "cmp.sueq.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readSingle() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_lt_s() {
    String[] instructions = {
      ".data",
      "fps: .float 3.8, 6.5",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 4($t4)",
      "cmp.lt.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readSingle() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_slt_s() {
    String[] instructions = {
      ".data",
      "fps: .float 5.2, 0.5",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 4($t4)",
      "lwc1 $f11, 0($t4)",
      "cmp.slt.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readSingle() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_ult_s() {
    String[] instructions = {
      ".data",
      "fps: .float 5.2, 0.5",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 4($t4)",
      "lwc1 $f11, 0($t4)",
      "cmp.ult.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readSingle() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_sult_s() {
    String[] instructions = {
      ".data",
      "fps: .float 5.8, 0.5",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 4($t4)",
      "lwc1 $f11, 0($t4)",
      "cmp.sult.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readSingle() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_le_s() {
    String[] instructions = {
      ".data",
      "fps: .float 5.0, 0.5",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 4($t4)",
      "lwc1 $f11, 0($t4)",
      "cmp.le.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readSingle() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_sle_s() {
    String[] instructions = {
      ".data",
      "fps: .float 5.2",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 0($t4)",
      "cmp.sle.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readSingle() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_ule_s() {
    String[] instructions = {
      ".data",
      "fps: .float 0x7fc00000, 0.5",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 4($t4)",
      "lwc1 $f11, 0($t4)",
      "cmp.ule.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readSingle() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_sule_s() {
    String[] instructions = {
      ".data",
      "fps: .float 5.8",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 0($t4)",
      "cmp.sule.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readSingle() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_or_s() {
    String[] instructions = {
      ".data",
      "fps: .float 5, 0.5",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 4($t4)",
      "lwc1 $f11, 0($t4)",
      "cmp.or.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readSingle() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_sor_s() {
    String[] instructions = {
      ".data",
      "fps: .float 5.5",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 0($t4)",
      "cmp.sor.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readSingle() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_une_s() {
    String[] instructions = {
      ".data",
      "fps: .float 5",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 0($t4)",
      "cmp.une.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readSingle() != 0;
    assertFalse(val);
  }

  @Test
  public void test_cmp_sune_s() {
    String[] instructions = {
      ".data",
      "fps: .float 5, 6",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 4($t4)",
      "cmp.sune.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readSingle() != 0;
    assertTrue(val);
  }

  @Test
  public void test_cmp_ne_s() {
    String[] instructions = {
      ".data",
      "fps: .float 0x7fc00000",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 0($t4)",
      "cmp.ne.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readSingle() != 0;
    assertFalse(val);
  }

  @Test
  public void test_cmp_sne_s() {
    String[] instructions = {
      ".data",
      "fps: .float 5, 6",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 4($t4)",
      "cmp.sne.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    boolean val = fpuRegisterFileArray.getFile("$f10").readSingle() != 0;
    assertTrue(val);
  }

  @Test
  public void test_div_s() {
    String[] instructions = {
      ".data",
      "fps: .float 6.4, 2.0",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 4($t4)",
      "div.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    float val = fpuRegisterFileArray.getFile("$f10").readSingle();
    assertEquals(3.2, val, 0.0001);
  }

  @Test
  public void test_div_d() {
    String[] instructions = {
      ".data",
      "fps: .double 6.4, 2.0",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "ldc1 $f11, 8($t4)",
      "div.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    double val = fpuRegisterFileArray.getFile("$f10").readDouble();
    assertEquals(3.2, val, 0.0001);
  }

  @Test
  public void test_mul_s() {
    String[] instructions = {
      ".data",
      "fps: .float 6.4, 2.0",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 4($t4)",
      "mul.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    float val = fpuRegisterFileArray.getFile("$f10").readSingle();
    assertEquals(12.8, val, 0.0001);
  }

  @Test
  public void test_mul_d() {
    String[] instructions = {
      ".data",
      "fps: .double 6.4, 2.0",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "ldc1 $f11, 8($t4)",
      "mul.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    double val = fpuRegisterFileArray.getFile("$f10").readDouble();
    assertEquals(12.8, val, 0.1);
  }

  @Test
  public void test_neg_s() {
    String[] instructions = {
      ".data", "fps: .float 2.0", ".text", "la $t4, fps", "lwc1 $f11, 0($t4)", "neg.s $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    float val = fpuRegisterFileArray.getFile("$f10").readSingle();
    assertEquals(-2.0, val, 0.0);
  }

  @Test
  public void test_neg_d() {
    String[] instructions = {
      ".data", "fps: .double 2.0", ".text", "la $t4, fps", "ldc1 $f11, 0($t4)", "neg.d $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    double val = fpuRegisterFileArray.getFile("$f10").readDouble();
    assertEquals(-2.0, val, 0.0);
  }

  @Test
  public void test_sqrt_s() {
    String[] instructions = {
      ".data", "fps: .float 4.0", ".text", "la $t4, fps", "lwc1 $f11, 0($t4)", "sqrt.s $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    float val = fpuRegisterFileArray.getFile("$f10").readSingle();
    assertEquals(2.0, val, 0.0);
  }

  @Test
  public void test_sqrt_d() {
    String[] instructions = {
      ".data", "fps: .double 4.0", ".text", "la $t4, fps", "ldc1 $f11, 0($t4)", "sqrt.d $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    double val = fpuRegisterFileArray.getFile("$f10").readDouble();
    assertEquals(2.0, val, 0.0);
  }

  @Test
  public void test_sub_s() {
    String[] instructions = {
      ".data",
      "fps: .float 4.0, 2.0",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 4($t4)",
      "sub.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    float val = fpuRegisterFileArray.getFile("$f10").readSingle();
    assertEquals(2.0, val, 0.0);
  }

  @Test
  public void test_sub_d() {
    String[] instructions = {
      ".data",
      "fps: .double 4.0, 3.0",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "ldc1 $f11, 8($t4)",
      "sub.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    double val = fpuRegisterFileArray.getFile("$f10").readDouble();
    assertEquals(1.0, val, 0.0);
  }

  @Test
  public void test_recip_s() {
    String[] instructions = {
      ".data", "fps: .float 4.0", ".text", "la $t4, fps", "lwc1 $f10, 0($t4)", "recip.s $f10, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    float val = fpuRegisterFileArray.getFile("$f10").readSingle();
    assertEquals(0.25, val, 0.0);
  }

  @Test
  public void test_recip_d() {
    String[] instructions = {
      ".data",
      "fps: .double 4.0",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "recip.d $f10, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    double val = fpuRegisterFileArray.getFile("$f10").readDouble();
    assertEquals(0.250, val, 0.0);
  }

  @Test
  public void test_rsqrt_s() {
    String[] instructions = {
      ".data", "fps: .float 4.0", ".text", "la $t4, fps", "lwc1 $f10, 0($t4)", "rsqrt.s $f10, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    float val = fpuRegisterFileArray.getFile("$f10").readSingle();
    assertEquals(0.5, val, 0.0);
  }

  @Test
  public void test_rsqrt_d() {
    String[] instructions = {
      ".data",
      "fps: .double 4.0",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "rsqrt.d $f10, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    double val = fpuRegisterFileArray.getFile("$f10").readDouble();
    assertEquals(0.50, val, 0.0);
  }

  @Test
  public void test_maddf_s() {
    String[] instructions = {
      ".data",
      "fps: .float 4.0",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 0($t4)",
      "maddf.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    float val = fpuRegisterFileArray.getFile("$f10").readSingle();
    assertEquals(20.0, val, 0.0);
  }

  @Test
  public void test_maddf_d() {
    String[] instructions = {
      ".data",
      "fps: .double 4.0",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "ldc1 $f11, 0($t4)",
      "maddf.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    double val = fpuRegisterFileArray.getFile("$f10").readDouble();
    assertEquals(20.0, val, 0.0);
  }

  @Test
  public void test_msubf_s() {
    String[] instructions = {
      ".data",
      "fps: .float 4.0",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 0($t4)",
      "msubf.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    float val = fpuRegisterFileArray.getFile("$f10").readSingle();
    assertEquals(-12.0, val, 0.0);
  }

  @Test
  public void test_msubf_d() {
    String[] instructions = {
      ".data",
      "fps: .double 4.0",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "ldc1 $f11, 0($t4)",
      "msubf.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    double val = fpuRegisterFileArray.getFile("$f10").readDouble();
    assertEquals(-12.0, val, 0.0);
  }

  @Test
  public void test_max_s() {
    String[] instructions = {
      ".data",
      "fps: .float 4.0, 6.9",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 4($t4)",
      "max.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    float val = fpuRegisterFileArray.getFile("$f10").readSingle();
    assertEquals(6.9, val, 0.01);
  }

  @Test
  public void test_max_d() {
    String[] instructions = {
      ".data",
      "fps: .double 4.0, 6.9",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "ldc1 $f11, 8($t4)",
      "max.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    double val = fpuRegisterFileArray.getFile("$f10").readDouble();
    assertEquals(6.9, val, 0.01);
  }

  @Test
  public void test_maxa_s() {
    String[] instructions = {
      ".data",
      "fps: .float 4.0, -6.9",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 4($t4)",
      "maxa.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    float val = fpuRegisterFileArray.getFile("$f10").readSingle();
    assertEquals(-6.9, val, 0.01);
  }

  @Test
  public void test_maxa_d() {
    String[] instructions = {
      ".data",
      "fps: .double 4.0, -6.9",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "ldc1 $f11, 8($t4)",
      "maxa.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    double val = fpuRegisterFileArray.getFile("$f10").readDouble();
    assertEquals(-6.9, val, 0.01);
  }

  @Test
  public void test_min_s() {
    String[] instructions = {
      ".data",
      "fps: .float 4.0, 6.9",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 4($t4)",
      "min.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    float val = fpuRegisterFileArray.getFile("$f10").readSingle();
    assertEquals(4.0, val, 0.01);
  }

  @Test
  public void test_min_d() {
    String[] instructions = {
      ".data",
      "fps: .double 4.0, 6.9",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "ldc1 $f11, 8($t4)",
      "min.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    double val = fpuRegisterFileArray.getFile("$f10").readDouble();
    assertEquals(4.0, val, 0.01);
  }

  @Test
  public void test_mina_s() {
    String[] instructions = {
      ".data",
      "fps: .float 4.0, -6.9",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 4($t4)",
      "mina.s $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    float val = fpuRegisterFileArray.getFile("$f10").readSingle();
    assertEquals(4.0, val, 0.01);
  }

  @Test
  public void test_mina_d() {
    String[] instructions = {
      ".data",
      "fps: .double 4.0, -6.9",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "ldc1 $f11, 8($t4)",
      "mina.d $f10, $f10, $f11",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    double val = fpuRegisterFileArray.getFile("$f10").readDouble();
    assertEquals(4.0, val, 0.01);
  }

  @Test
  public void test_cvt_d_s() {
    String[] instructions = {
      ".data", "fps: .float 4.0", ".text", "la $t4, fps", "lwc1 $f10, 0($t4)", "cvt.d.s $f10, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    double val = fpuRegisterFileArray.getFile("$f10").readDouble();
    assertEquals(4.0, val, 0.0);
  }

  @Test
  public void test_cvt_d_w() {
    String[] instructions = {
      ".data", "fps: .word 4", ".text", "la $t4, fps", "lwc1 $f10, 0($t4)", "cvt.d.w $f10, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    double val = fpuRegisterFileArray.getFile("$f10").readDouble();
    assertEquals(4.0, val, 0.0);
  }

  @Test
  public void test_cvt_d_l() {
    String[] instructions = {
      ".data", "fps: .word 4", ".text", "la $t4, fps", "lwc1 $f10, 0($t4)", "cvt.d.l $f11, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    double val = fpuRegisterFileArray.getFile("$f11").readDouble();
    assertEquals(4.0, val, 0.0);
  }

  @Test
  public void test_cvt_s_d() {
    String[] instructions = {
      ".data",
      "fps: .double 4.0",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "cvt.s.d $f10, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    float val = fpuRegisterFileArray.getFile("$f10").readSingle();
    assertEquals(4.0, val, 0.0);
  }

  @Test
  public void test_cvt_s_w() {
    String[] instructions = {
      ".data", "fps: .word 4", ".text", "la $t4, fps", "lwc1 $f10, 0($t4)", "cvt.s.w $f10, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    float val = fpuRegisterFileArray.getFile("$f10").readSingle();
    assertEquals(4.0, val, 0.0);
  }

  @Test
  public void test_cvt_s_l() {
    String[] instructions = {
      ".data",
      "fps: .word 0, 400",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "cvt.s.l $f11, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    double val = fpuRegisterFileArray.getFile("$f11").readSingle();
    assertEquals(400.0, val, 0.0);
  }

  @Test
  public void test_cvt_l_s() {
    String[] instructions = {
      ".data", "fps: .float 4.0", ".text", "la $t4, fps", "lwc1 $f10, 0($t4)", "cvt.l.s $f11, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    long val = fpuRegisterFileArray.getFile("$f11").readDword();
    assertEquals(4, val);
  }

  @Test
  public void test_cvt_l_d() {
    String[] instructions = {
      ".data",
      "fps: .double 40.5",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "cvt.l.d $f11, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    long val = fpuRegisterFileArray.getFile("$f11").readDword();
    assertEquals(40, val);
  }

  @Test
  public void test_cvt_w_s() {
    String[] instructions = {
      ".data", "fps: .float 4.0", ".text", "la $t4, fps", "lwc1 $f10, 0($t4)", "cvt.w.s $f11, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    int val = fpuRegisterFileArray.getFile("$f11").readWord();
    assertEquals(4, val);
  }

  @Test
  public void test_cvt_w_d() {
    String[] instructions = {
      ".data",
      "fps: .double 40.5",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "cvt.w.d $f11, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    int val = fpuRegisterFileArray.getFile("$f11").readWord();
    assertEquals(40, val);
  }

  @Test
  public void test_rint_s() {
    String[] instructions = {
      ".data", "fps: .float 40.5", ".text", "la $t4, fps", "lwc1 $f10, 0($t4)", "rint.s $f11, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    float val = fpuRegisterFileArray.getFile("$f11").readSingle();
    assertEquals(40.0, val, 0.0);
  }

  @Test
  public void test_rint_d() {
    String[] instructions = {
      ".data",
      "fps: .double 40.5",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "rint.d $f11, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    double val = fpuRegisterFileArray.getFile("$f11").readDouble();
    assertEquals(40.0, val, 0.0);
  }

  @Test
  public void test_ceil_l_s() {
    String[] instructions = {
      ".data",
      "fps: .float 40.5",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "ceil.l.s $f11, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    long val = fpuRegisterFileArray.getFile("$f11").readDword();
    assertEquals(41, val);
  }

  @Test
  public void test_ceil_l_d() {
    String[] instructions = {
      ".data",
      "fps: .double 40.5",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "ceil.l.d $f11, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    long val = fpuRegisterFileArray.getFile("$f11").readDword();
    assertEquals(41, val);
  }

  @Test
  public void test_ceil_w_s() {
    String[] instructions = {
      ".data",
      "fps: .float 40.5",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "ceil.w.s $f11, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    int val = fpuRegisterFileArray.getFile("$f11").readWord();
    assertEquals(41, val);
  }

  @Test
  public void test_ceil_w_d() {
    String[] instructions = {
      ".data",
      "fps: .double 40.5",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "ceil.w.d $f11, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    int val = fpuRegisterFileArray.getFile("$f11").readWord();
    assertEquals(41, val);
  }

  @Test
  public void test_floor_l_s() {
    String[] instructions = {
      ".data",
      "fps: .float 40.5",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "floor.l.s $f11, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    long val = fpuRegisterFileArray.getFile("$f11").readDword();
    assertEquals(40, val);
  }

  @Test
  public void test_floor_l_d() {
    String[] instructions = {
      ".data",
      "fps: .double 40.5",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "floor.l.d $f11, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    long val = fpuRegisterFileArray.getFile("$f11").readDword();
    assertEquals(40, val);
  }

  @Test
  public void test_floor_w_s() {
    String[] instructions = {
      ".data",
      "fps: .float 40.5",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "floor.w.s $f11, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    int val = fpuRegisterFileArray.getFile("$f11").readWord();
    assertEquals(40, val);
  }

  @Test
  public void test_floor_w_d() {
    String[] instructions = {
      ".data",
      "fps: .double 40.5",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "floor.w.d $f11, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    int val = fpuRegisterFileArray.getFile("$f11").readWord();
    assertEquals(40, val);
  }

  @Test
  public void test_round_l_s() {
    String[] instructions = {
      ".data",
      "fps: .float 40.5",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "round.l.s $f11, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    long val = fpuRegisterFileArray.getFile("$f11").readDword();
    assertEquals(41, val);
  }

  @Test
  public void test_round_l_d() {
    String[] instructions = {
      ".data",
      "fps: .double 40.5",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "round.l.d $f11, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    long val = fpuRegisterFileArray.getFile("$f11").readDword();
    assertEquals(41, val);
  }

  @Test
  public void test_round_w_s() {
    String[] instructions = {
      ".data",
      "fps: .float 40.5",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "round.w.s $f11, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    int val = fpuRegisterFileArray.getFile("$f11").readWord();
    assertEquals(41, val);
  }

  @Test
  public void test_round_w_d() {
    String[] instructions = {
      ".data",
      "fps: .double 40.5",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "round.w.d $f11, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    int val = fpuRegisterFileArray.getFile("$f11").readWord();
    assertEquals(41, val);
  }

  @Test
  public void test_trunc_l_s() {
    String[] instructions = {
      ".data",
      "fps: .float 40.5",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "trunc.l.s $f11, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    long val = fpuRegisterFileArray.getFile("$f11").readDword();
    assertEquals(40, val);
  }

  @Test
  public void test_trunc_l_d() {
    String[] instructions = {
      ".data",
      "fps: .double 40.5",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "trunc.l.d $f11, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    long val = fpuRegisterFileArray.getFile("$f11").readDword();
    assertEquals(40, val);
  }

  @Test
  public void test_trunc_w_s() {
    String[] instructions = {
      ".data",
      "fps: .float 40.5",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "trunc.w.s $f11, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    int val = fpuRegisterFileArray.getFile("$f11").readWord();
    assertEquals(40, val);
  }

  @Test
  public void test_trunc_w_d() {
    String[] instructions = {
      ".data",
      "fps: .double 40.5",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 0($t4)",
      "trunc.w.d $f11, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    int val = fpuRegisterFileArray.getFile("$f11").readWord();
    assertEquals(40, val);
  }

  @Test
  public void test_mov_s() {
    String[] instructions = {
      ".data", "fps: .float 40.5", ".text", "la $t4, fps", "lwc1 $f10, 0($t4)", "mov.s $f11, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    float val = fpuRegisterFileArray.getFile("$f11").readSingle();
    assertEquals(40.5, val, 0.0);
  }

  @Test
  public void test_mov_d() {
    String[] instructions = {
      ".data", "fps: .double 40.5", ".text", "la $t4, fps", "ldc1 $f10, 0($t4)", "mov.d $f11, $f10",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    double val = fpuRegisterFileArray.getFile("$f11").readDouble();
    assertEquals(40.5, val, 0.0);
  }

  @Test
  public void test_sel_s() {
    String[] instructions = {
      ".data",
      "fps: .float 1.33, 40.5, 50.5",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 4($t4)",
      "lwc1 $f12, 8($t4)",
      "sel.s $f10, $f11, $f12",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    float val = fpuRegisterFileArray.getFile("$f10").readSingle();
    assertEquals(50.5, val, 0.0);
  }

  @Test
  public void test_sel_d() {
    String[] instructions = {
      ".data",
      "fps: .double 1.667, 40.5, 50.5",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 8($t4)",
      "ldc1 $f11, 0($t4)",
      "ldc1 $f12, 16($t4)",
      "sel.d $f11, $f10, $f12",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    double val = fpuRegisterFileArray.getFile("$f11").readDouble();
    assertEquals(50.5, val, 0.0);
  }

  @Test
  public void test_seleqz_s() {
    String[] instructions = {
      ".data",
      "fps: .float 40.5, 50.5, 1.33",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 4($t4)",
      "lwc1 $f12, 8($t4)",
      "seleqz.s $f10, $f11, $f12",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    float val = fpuRegisterFileArray.getFile("$f10").readSingle();
    assertEquals(0.0, val, 0.0);
  }

  @Test
  public void test_seleqz_d() {
    String[] instructions = {
      ".data",
      "fps: .double 50.4, 40.5, 1.667",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 8($t4)",
      "ldc1 $f11, 0($t4)",
      "ldc1 $f12, 16($t4)",
      "seleqz.d $f11, $f10, $f12",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    double val = fpuRegisterFileArray.getFile("$f11").readDouble();
    assertEquals(0.0, val, 0.0);
  }

  @Test
  public void test_selnez_s() {
    String[] instructions = {
      ".data",
      "fps: .float 0, 40.5, 1.33",
      ".text",
      "la $t4, fps",
      "lwc1 $f10, 0($t4)",
      "lwc1 $f11, 4($t4)",
      "lwc1 $f12, 8($t4)",
      "selnez.s $f10, $f11, $f12",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    float val = fpuRegisterFileArray.getFile("$f10").readSingle();
    assertEquals(40.5, val, 0.0);
  }

  @Test
  public void test_selnez_d() {
    String[] instructions = {
      ".data",
      "fps: .double 1.33, 40.5, 50.5",
      ".text",
      "la $t4, fps",
      "ldc1 $f10, 8($t4)",
      "ldc1 $f11, 0($t4)",
      "ldc1 $f12, 16($t4)",
      "selnez.d $f11, $f10, $f12",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    double val = fpuRegisterFileArray.getFile("$f11").readDouble();
    assertEquals(0.0, val, 0.0);
  }

  @Test
  public void test_bc1eqz() {
    String[] instructions = {
      ".data",
      "fps: .double 50.5, 40.5, 0",
      ".text",
      "la $t4, fps",
      "ldc1 $f12, 16($t4)",
      "bc1eqz $f12, 1",
      "ldc1 $f11, 0($t4)",
      "ldc1 $f10, 0($t4)",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    double val = fpuRegisterFileArray.getFile("$f11").readDouble();
    assertEquals(0.0, val, 0.0);

    val = fpuRegisterFileArray.getFile("$f10").readDouble();
    assertEquals(50.5, val, 0.0);
  }

  @Test
  public void test_bc1nez() {
    String[] instructions = {
      ".data",
      "fps: .double 50.5, 40.5, 1.667",
      ".text",
      "la $t4, fps",
      "ldc1 $f12, 16($t4)",
      "bc1nez $f12, 1",
      "ldc1 $f11, 0($t4)",
      "ldc1 $f10, 0($t4)",
    };
    mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
    mipsSimulator.running();

    while (mipsSimulator.isRunning())
      ;
    double val = fpuRegisterFileArray.getFile("$f11").readDouble();
    assertEquals(0.0, val, 0.0);

    val = fpuRegisterFileArray.getFile("$f10").readDouble();
    assertEquals(50.5, val, 0.0);
  }
}
