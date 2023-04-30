package com.cleverchuk.mips.simulator.fpu;

public class FpuInstructionDecoder {
    public static final int OPCODE_MASK = 0x3f_00_00_00;

    public static final int BASE_MASK = 0x03_e0_00_00;

    public static final int FT_MASK = 0x1f_00_00;

    public static final int FS_MASK = 0xf8_00;

    public static final int FD_MASK = 0x03_e0;

    public static final int FUNCTION_MASK = 0x3f;

    public static final int OFFSET_MASK = 0xff_ff;

    public int decodeOpcode(int instruction) {
        int opcode = instruction & OPCODE_MASK;
        return opcode >>> 0x17;
    }

    public int decodeFt(int instruction) {
        int ft = instruction & FT_MASK;
        return ft >>> 0xd;
    }

    public int decodeFs(int instruction) {
        int fs = instruction & FS_MASK;
        return fs >>> 0x8;
    }

    public int decodeFd(int instruction) {
        int fd = instruction & FD_MASK;
        return fd >>> 0x3;
    }

    public int decodeBase(int instruction) {
        int base = instruction & BASE_MASK;
        return base >>> 0x12;
    }

    public int decodeOffset(int instruction) {
        return instruction & OFFSET_MASK;
    }

    public int decodeFunction(int instruction) {
        return instruction & FUNCTION_MASK;
    }

    public int decodeSub(int instruction) {
        return decodeBase(instruction);
    }

    public int decodeFmt(int instruction) {
        return decodeBase(instruction);
    }

    public int decodeRt(int instruction) {
        return decodeFt(instruction);
    }
}
