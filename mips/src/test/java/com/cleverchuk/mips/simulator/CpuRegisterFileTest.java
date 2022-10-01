package com.cleverchuk.mips.simulator;

import com.cleverchuk.mips.simulator.cpu.CpuRegisterFile;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CpuRegisterFileTest {

    private final CpuRegisterFile bigEndianRegisterFile = new CpuRegisterFile();

    @Test
    public void readWord() {
        int value = 120000;
        bigEndianRegisterFile.write("$t0", value);
        assertEquals(value, bigEndianRegisterFile.read("$t0"));
    }
}