package com.cleverchuk.mips.simulator;

import com.cleverchuk.mips.simulator.fpu.FpuInstruction;

public class NoOpProcessor implements Processor<FpuInstruction>{
    @Override
    public void execute(FpuInstruction instruction) throws Exception {

    }
}
