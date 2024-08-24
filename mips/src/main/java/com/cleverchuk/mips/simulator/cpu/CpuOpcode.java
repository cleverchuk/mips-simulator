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

package com.cleverchuk.mips.simulator.cpu;

import androidx.annotation.NonNull;
import com.cleverchuk.mips.simulator.Opcode;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum CpuOpcode implements Opcode {
    // ARITHMETIC OPERATIONS
    ADD("add"),
    ADDI("addi"),
    ADDIU("addiu"),
    ADDU("addu"),
    CLO("clo"),
    CLZ("clz"),
    LA("la"),
    LI("li"),
    LUI("lui"),
    MOVE("move"),
    NEGU("negu"),
    SUB("sub"),
    SUBU("subu"),
    SEB("seb"),
    SEH("seh"),
    // SHIFT AND ROTATE OPERATIONS
    SLL("sll"),
    SLLV("sllv"),
    SRL("srl"),
    ROTR("rotr"),
    ROTRV("rotrv"),
    SRA("sra"),
    SRAV("srav"),
    SRLV("srlv"),
    // LOGICAL AND BIT-FIELD OPERATIONS
    AND("and"),
    ANDI("andi"),
    EXT("ext"),
    INS("ins"),
    NOP("nop"),
    NOR("nor"),
    NOT("not"),
    OR("or"),
    ORI("ori"),
    XOR("xor"),
    XORI("xori"),
    WSBH("wsbh"),
    // CONDITION TESTING AND CONDITIONAL MOVE OPERATIONS
    MOVN("movn"),
    MOVZ("movz"),
    SLT("slt"),
    SLTI("slti"),
    SLTIU("sltiu"),
    SLTU("sltu"),

    // MULTIPLY AND DIVIDE OPERATIONS
    DIV("div"),
    MUL("mul"),
    DIVU("divu"),
    MADD("madd"),
    MADDU("maddu"),
    MSUB("msub"),
    MSUBU("msubu"),
    MULT("mult"),
    MULTU("multu"),

    // JUMPS AND BRANCHES
    BEQ("beq"),
    BNE("bne"),
    JAL("jal"),
    J("j"),
    JR("jr"),
    B("b"),
    BAL("bal"),
    BEQZ("beqz"),
    BGEZ("bgez"),
    BGTZ("bgtz"),
    BGEZAL("bgezal"),
    BLEZ("blez"),
    BLTZ("bltz"),
    BNEZ("bnez"),
    BLTZAL("bltzal"),
    JALR("jalr"),

    // LOAD AND STORE OPERATIONS
    LW("lw"),
    SW("sw"),
    LB("lb"),
    LBU("lbu"),
    LH("lh"),
    LHU("lhu"),
    LWL("lwl"),
    LWR("lwr"),
    SB("sb"),
    SH("sh"),
    SWL("swl"),
    SWR("swr"),
    ULW("ulw"),
    USW("usw"),
    // ACCUMULATOR ACCESS OPERATIONS
    MFHI("mfhi"),
    MFLO("mflo"),
    MTHI("mthi"),
    MTLO("mtlo"),

    //ATOMIC READ-MODIFY-WRITE OPERATIONS
    LL("ll"),
    SC("sc"),

    SYSCALL("syscall");

    public static final Set<String> CPU_OPCODES = Arrays.stream(CpuOpcode.values()).map(CpuOpcode::getName)
            .collect(Collectors.toSet());

    private final String name;

    CpuOpcode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    public boolean same(String opcode) {
        return name.equals(opcode);
    }

    public static CpuOpcode parse(String opcode) {
        try {
            return valueOf(opcode.toUpperCase());
        } catch (Throwable ignore) {
        }
        return null;
    }
}
