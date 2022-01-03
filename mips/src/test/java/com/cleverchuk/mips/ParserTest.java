package com.cleverchuk.mips;

import com.cleverchuk.mips.compiler.parser.Parser;
import com.cleverchuk.mips.emulator.Instruction;
import com.cleverchuk.mips.emulator.Opcode;
import com.cleverchuk.mips.emulator.storage.Storage;
import com.cleverchuk.mips.emulator.storage.StorageType;
import java.util.Locale;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class ParserTest {
    @Test
    public void testParseData() throws Exception {
        String text = "str1: .asciiz \"Hello World\"\n";
        Storage storage = Parser.parseStringToData(text);
        assertEquals(StorageType.STRING, storage.getStorageType());
        assertEquals(StorageType.SPACE, Parser.parseStringToData("str1: .space     45").getStorageType());
        assertEquals(StorageType.SPACE, Parser.parseStringToData("str1: .space     45        ").getStorageType());
        assertEquals(StorageType.WORD, Parser.parseStringToData("str1: .word 5,6,7").getStorageType());
    }


    @Test
    public void testParseInstruction() throws Exception {
        String[] instructions = {
                "add $t0, $t1, $t2 # comment",
                "#hello no op",
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
                "             ",
                "nop"

        };

        Instruction[] expected = {
                new Instruction(Opcode.ADD, "$t0", "$t1", "$t2", null, -1),
                new Instruction(Opcode.NOP, null, null, null, null, -1),
                Instruction.builder().immediateValue(400).rd("$t0").opcode(Opcode.ADDI).rs("$t1").build(),
                new Instruction(Opcode.BEQ, null, "$t0", "$t1", "label", -1),
                new Instruction(Opcode.LW, "$t0", "$t1", null, null, 2),
                new Instruction(Opcode.SW, "$t0", "$sp", null, null, 67),
                Instruction.builder().immediateValue(300).rd("$t0").opcode(Opcode.LI).build(),
                new Instruction(Opcode.LA, "$t0", null, null, "label", -1),
                new Instruction(Opcode.JAL, null, null, null, "label", -1),
                new Instruction(Opcode.JR, null, null, null, "$ra", -1),
                Instruction.builder().immediateValue(300).rd("$t0").opcode(Opcode.ADDI).rs("$zero").build(),
                new Instruction(Opcode.ADD, "$t0", "$t1", "$zero", null, -1),
                Instruction.builder().immediateValue(1).rd("$v0").opcode(Opcode.LI).build(),
                new Instruction(Opcode.SYSCALL, null, null, null, null, -1),
                new Instruction(Opcode.NOP, null, null, null, null, -1),
                new Instruction(Opcode.NOP, null, null, null, null, -1),
        };

        for (int i = 0; i < instructions.length; i++) {
            System.out.println(String.format(Locale.getDefault(), "Instruction: %s", instructions[i]));
            Instruction instruction = Parser.parseToInstruction(instructions[i], i);
            assertEquals(expected[i].opcode, instruction.opcode);
            assertEquals(expected[i].rd, instruction.rd);
            assertEquals(expected[i].rt, instruction.rt);
            assertEquals(expected[i].rs, instruction.rs);
            assertEquals(expected[i].label, instruction.label);
            assertEquals(expected[i].immediateValue, instruction.immediateValue);
            assertEquals(expected[i].offset, instruction.offset);
        }
    }
}
