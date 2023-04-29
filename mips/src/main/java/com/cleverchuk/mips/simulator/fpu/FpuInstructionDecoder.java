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
        return instruction & OPCODE_MASK;
    }

    public int decodeFt(int instruction) {
        return instruction & FT_MASK;
    }

    public int decodeFs(int instruction) {
        return instruction & FS_MASK;
    }

    public int decodeFd(int instruction) {
        return instruction & FD_MASK;
    }

    public int decodeBase(int instruction) {
        return instruction & BASE_MASK;
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
