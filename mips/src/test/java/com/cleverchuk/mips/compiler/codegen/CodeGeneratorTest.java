package com.cleverchuk.mips.compiler.codegen;

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
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class CodeGeneratorTest {
    RecursiveDescentParser parser = new RecursiveDescentParser(new MipsLexer(), new SemanticAnalyzer(new InstructionAnalyzer(
            new ZeroOpAnalyzer(),
            new OneOpAnalyzer(),
            new TwoOpAnalyzer(new TwoOpAnalyzer.LoadStoreAnalyzer(), new TwoOpAnalyzer.TwoRegOpcodeAnalyzer(), new TwoOpAnalyzer.BranchOpcodeAnalyzer()),
            new ThreeOpAnalyzer(new ThreeOpAnalyzer.ShiftRotateAnalyzer(), new ThreeOpAnalyzer.ConditionalTestingAndMoveAnalyzer(),
                    new ThreeOpAnalyzer.ArithmeticAndLogicalOpcodeAnalyzer()),
            new FourOpAnalyzer()
    )));

    @Test
    public void codeGen() {
        String source =
                ".text\n" +
                        "ins $t0, $t0, 0, 2\n" +
                        "lw $t0, 02*2-2*4($t1)\n";


        Node program = parser.parse(source);
        CodeGenerator codeGenerator = new CodeGenerator();

        assertNotNull(program);
        codeGenerator.generate(program);
    }

    @Test
    public void codeGenExt() {
        String source =
                ".text\n" +
                        "li $t0, -15\n" +
                        "ext $t0, $t0, 0, 5";

        Node program = parser.parse(source);
        CodeGenerator codeGenerator = new CodeGenerator();

        assertNotNull(program);
        codeGenerator.generate(program);
    }

    @Test
    public void codeGen0() {
        String source = ".text\n" +
                "add $t0, $t1, $t2 # comment\n" +
                "# hello no op\n" +
                "addi $t0, $t1, 400\n" +
                "beq $t0, $t1, label\n" +
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
        CodeGenerator codeGenerator = new CodeGenerator();

        assertNotNull(program);
        codeGenerator.generate(program);
    }

    @Test
    public void codeGenData() {
        String source =
                ".data\n" +
                        "dummy0: .word 5, 6, 7, 8\n" +
                        "dummy1: .half 5, 6, 7, 8\n" +
                        "dummy2: .asciiz \"hello world\"\n";
        Node program = parser.parse(source);
        CodeGenerator codeGenerator = new CodeGenerator();

        assertNotNull(program);
        codeGenerator.generate(program);
    }
}