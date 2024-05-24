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

package com.cleverchuk.mips.compiler.parser;

import com.cleverchuk.mips.compiler.lexer.MipsLexer;
import com.cleverchuk.mips.compiler.semantic.SemanticAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.FourOpAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.InstructionAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.OneOpAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.ThreeOpAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.TwoOpAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.ZeroOpAnalyzer;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InstructionVisitorTest {
    InstructionVisitor visitor = new InstructionVisitor();

    RecursiveDescentParser parser = new RecursiveDescentParser(new MipsLexer(), new SemanticAnalyzer(new InstructionAnalyzer(
            new ZeroOpAnalyzer(),
            new OneOpAnalyzer(),
            new TwoOpAnalyzer(new TwoOpAnalyzer.LoadStoreAnalyzer(), new TwoOpAnalyzer.TwoRegOpcodeAnalyzer(), new TwoOpAnalyzer.BranchOpcodeAnalyzer()),
            new ThreeOpAnalyzer(new ThreeOpAnalyzer.ShiftRotateAnalyzer(), new ThreeOpAnalyzer.ConditionalTestingAndMoveAnalyzer(),
                    new ThreeOpAnalyzer.ArithmeticAndLogicalOpcodeAnalyzer()),
            new FourOpAnalyzer()
    )), visitor);

    @Test
    public void testVisit() {
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
        parser.parse(source);
        Set<String> instructions = visitor.getInstructions();

        Set<String> expected = new LinkedHashSet<>();
        expected.add("add $t0 , $t1 , $t2");
        expected.add("addi $t0 , $t1 , 400");

        expected.add("beq $t0 , $t1 , 10");
        expected.add("lw $t0 , 2 ( $t1 )");
        expected.add("sw $t0 , 67 ( $sp )");

        expected.add("li $t0 , 300");
        expected.add("la $t0 , label");
        expected.add("jal label");

        expected.add("jr $ra");
        expected.add("addi $t0 , $zero , 300");
        expected.add("add $t0 , $t1 , $zero");

        expected.add("li $v0 , 1");
        expected.add("syscall");
        expected.add("nop");

        expected.retainAll(instructions);
        assertEquals(expected.size(), instructions.size());

    }
}