package com.cleverchuk.mips.compiler.parser;


import com.cleverchuk.mips.compiler.lexer.MipsLexer;
import com.cleverchuk.mips.compiler.semantic.SemanticAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.FourOpAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.InstructionAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.OneOpAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.ThreeOpAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.TwoOpAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.ZeroOpAnalyzer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class RecursiveDescentParserTest {
    RecursiveDescentParser parser = new RecursiveDescentParser(new MipsLexer(), new SemanticAnalyzer(new InstructionAnalyzer(
            new ZeroOpAnalyzer(),
            new OneOpAnalyzer(),
            new TwoOpAnalyzer(new TwoOpAnalyzer.LoadStoreAnalyzer(), new TwoOpAnalyzer.TwoRegOpcodeAnalyzer(), new TwoOpAnalyzer.BranchOpcodeAnalyzer()),
            new ThreeOpAnalyzer(new ThreeOpAnalyzer.ShiftRotateAnalyzer(), new ThreeOpAnalyzer.ConditionalTestingAndMoveAnalyzer(),
                    new ThreeOpAnalyzer.ArithmeticAndLogicalOpcodeAnalyzer()),
            new FourOpAnalyzer()
    )));

    @Test
    public void parseTextSuccess() {
        String source = ".text\n" +
                "add $t0, $t1, $t2 # comment\n" +
                "# hello no op\n" +
                "addi $t0, $t1, 400\n" +
                "beq $t0, $t1, 10\n" +
                "lw $t0, 2($t1   )\n" +
                "sw $t0, 67 (   $sp )\n" +
                "li $t0, 300\n" +
                "la $t0, label # comment\n" +
                "jal label\n" +
                "return:jr $ra\n" +
                "addi $t0, $zero, 300\n" +
                "add $t0, $t1,             $zero\n" +
                "li $v0,                       1\n" +
                "syscall\n" +
                "             \n" +
                "nop\n";
        Node program = parser.parse(source);
        assertNotNull(program);
        assertFalse(ErrorRecorder.hasErrors());
    }

    @Test
    public void parseTextFailure() {
        String source = ".text\n" +
                "add $t0, $t1, $t2 # comment\n" +
                "la $t0, # error here\n" +
                "jal label\n" +
                "return:jr $ra\n" +
                "addi $t0, $zero, 300\n" +
                "add $t0, $t1,             $zero\n" +
                "li $v0,                       1\n" +
                "syscall\n" +
                "             \n" +
                "nop\n";
        Node program = parser.parse(source);
        assertNotNull(program);
        assertTrue(ErrorRecorder.hasErrors());
    }

    @Test
    public void parseDataSuccess() {
        String source = ".data\n" +
                "dummy0: .space 5\n" +
                "dummy1: .byte 10\n" +
                "dummy2: .half 10\n" +
                "dummy3: .word 5\n" +
                "dummy4: .float 50\n" +
                "dummy5: .double 60\n" +
                "dummy6: .word 5, 12, 89, 10";

        Node program = parser.parse(source);
        assertNotNull(program);
        assertFalse(ErrorRecorder.hasErrors());
    }

    @Test
    public void parseDataSuccessThoughError() {
        String source = ".data\n" +
                "dummy0: .space 5\n" +
                "dummy1: .byte 10\n" +
                "dummy2: .half 10\n" +
                "dummy3: .word 5\n" +
                "dummy4: .float 50\n" +
                "dummy5: .double 60\n" +
                "dummy6: .word 5, 12, 89,# error";

        Node program = parser.parse(source);
        assertNotNull(program);
        assertTrue(ErrorRecorder.hasErrors());
    }


    @Test
    public void parseDataFailure() {
        String source = ".data\n" +
                "dummy0: .space 5\n" +
                "dummy1: .byte 10\n" +
                "dummy2: .half 10\n" +
                "dummy3: .word 5\n" +
                "dummy4: .float 50\n" +
                "dummy5: .double 60,\n" +
                "dummy6: .word 5, 12, 89";

        Node program = parser.parse(source);
        assertNotNull(program);
        assertTrue(ErrorRecorder.hasErrors());
    }

    @Test
    public void parseProgramThatBeginWithDataSuccess() {
        String source = "# this is a comment\n" +
                ".data\n" +
                "dummy0: .space 5\n" +
                "dummy1: .byte 10\n" +
                "dummy2: .half 10\n" +
                ".text\n" +
                "add $t0, $t1, $t2 # comment\n" +
                "# hello no op\n" +
                "addi $t0, $t1, 400\n" +
                "beq $t0, $t1, 10\n" +
                "lw $t0, 2($t1   )\n" +
                "sw $t0, 67 (   $sp )\n" +
                "li $t0, 300\n" +
                "la $t0, label # comment\n" +
                "jal label\n" +
                "return:jr $ra\n" +
                "addi $t0, $zero, 300\n" +
                "add $t0, $t1,             $zero\n" +
                "li $v0,                       1\n" +
                "syscall\n";

        Node program = parser.parse(source);
        assertNotNull(program);
        assertFalse(ErrorRecorder.hasErrors());
    }

    @Test
    public void parseProgramThatBeginWithTextSuccess() {
        String source = "# this is a comment\n" +
                ".text\n" +
                "add $t0, $t1, $t2 # comment\n" +
                "# hello no op\n" +
                "addi $t0, $t1, 400\n" +
                "beq $t0, $t1, 10\n" +
                "lw $t0, 2($t1   )\n" +
                "sw $t0, 67 (   $sp )\n" +
                "li $t0, 300\n" +
                "la $t0, label # comment\n" +
                "jal label\n" +
                "return:jr $ra\n" +
                "addi $t0, $zero, 300\n" +
                "add $t0, $t1,             $zero\n" +
                "li $v0,                       1\n" +
                "syscall\n" +
                ".data\n" +
                "dummy0: .space 5\n" +
                "dummy1: .byte 10\n" +
                "dummy2: .half 10\n";

        Node program = parser.parse(source);
        assertNotNull(program);
        assertFalse(ErrorRecorder.hasErrors());
    }

    @Test
    public void parseProgramThatBeginWithTextError() {
        String source = "# this is a comment\n" +
                ".text\n" +
                "add $t0, $t1, $t2 # comment\n" +
                "# hello no op\n" +
                "addi $t0, $t1, 400\n" +
                "beq $t0, $t1, 10\n" +
                "lw $t0, 2($t1   )\n" +
                "sw $t0, 67 (   $sp )\n" +
                "li $t0, 300\n" +
                "la $t0, label # comment\n" +
                "jal label\n" +
                "return:jr $ra\n" +
                "addi $t0, $zero, 300\n" +
                "add $t0, $t1,             $zero\n" +
                "li $v0,                       1\n" +
                ".data\n" +
                "dummy0: .space 5\n" +
                "dummy1: .byte 10\n" +
                "syscall\n" +
                "dummy2: .half 10\n";

        Node program = parser.parse(source);
        assertNotNull(program);
        assertTrue(ErrorRecorder.hasErrors());
    }

    @Test
    public void parseProgramThatHasExpression() {
        String source = "# this is a comment\n" +
                ".text\n" +
                "lw $t0, 2*4($t1)\n" +
                ".data\n" +
                "dummy2: .half 10,90,90+89,-9\n";

        Node program = parser.parse(source);
        assertNotNull(program);
        assertFalse(ErrorRecorder.hasErrors());
    }


    @Test
    public void parseProgramThatHasExpression0() {
        String source =
                ".text\n" +
                        "lw $t0, 2*4($t1)\n";

        Node program = parser.parse(source);
        assertNotNull(program);
        assertFalse(ErrorRecorder.hasErrors());
    }

    @Test
    public void parseProgram() {
        String source =
                ".text\n" +
                        "lw $t0, 2*4($t1)\n" +
                        "syscall\n" +
                        "\n" +
                        "la $t0, main\n" +
                        "syscall\n" +
                        "label:\n" +
                        "\n" +
                        "mul $t0, $t1, $t2\n";

        Node program = parser.parse(source);
        assertNotNull(program);
        assertFalse(ErrorRecorder.hasErrors());
    }

    @Test
    public void parseProgramWithHexAndOctalConstants() {
        String source =
                ".text\n" +
                        "li $t0, 027\n" +
                        "addi $t0, $t1, 0xf\n";

        Node program = parser.parse(source);
        assertNotNull(program);
        assertFalse(ErrorRecorder.hasErrors());
    }

    @Test
    public void parseProgramWithDeciReg() {
        String source =
                ".text\n" +
                        "li $9, 027\n" +
                        "addi $4, $8, 0xf\n";

        Node program = parser.parse(source);
        assertNotNull(program);
        assertFalse(ErrorRecorder.hasErrors());
    }


    @Test
    public void parseProgramWithIllFormedConstructs() {
        String source =
                ".data\n" +
                        "dummy0: .space 5, 5, 6\n" +
                        "dummy1: .byte 10,,67\n" +
                        "dummy2: .half ,10,\n";

        Node program = parser.parse(source);
        assertNotNull(program);
        assertTrue(ErrorRecorder.hasErrors());
    }
}