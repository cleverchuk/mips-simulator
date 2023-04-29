package com.cleverchuk.mips.simulator.fpu;

import com.cleverchuk.mips.simulator.VirtualInstruction;

public class FpuInstruction implements VirtualInstruction {
    private final int instructionEncoding;
    private final int line;

    public FpuInstruction(int instructionEncoding, int line) {
        this.instructionEncoding = instructionEncoding;
        this.line = line;
    }

    @Override
    public int line() {
        return line;
    }
}
