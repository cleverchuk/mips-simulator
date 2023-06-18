package com.cleverchuk.mips.simulator;

import com.cleverchuk.mips.simulator.fpu.FpuRegisterFileArray;

public interface Processor<T extends VirtualInstruction> {
    void execute(T instruction) throws Exception;

    default FpuRegisterFileArray registerFiles(){
        return new FpuRegisterFileArray();
    }
}
