package com.cleverchuk.mips.simulator;

import androidx.annotation.NonNull;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum Opcode {
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
    add("beqz"),
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

    public static Set<String> OPCODE = Arrays.stream(Opcode.values()).map(Opcode::getValue)
            .collect(Collectors.toSet());

    private final String value;

    Opcode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @NonNull
    @Override
    public String toString() {
        return value;
    }

    public boolean same(String opcode){
        return value.equals(opcode);
    }

    public static Opcode parse(String opcode) {
        return valueOf(opcode.toUpperCase());
    }
}
