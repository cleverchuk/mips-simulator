package com.cleverchuk.mips.emulator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BigEndianRegisterFileTest {

    private final BigEndianRegisterFile bigEndianRegisterFile = new BigEndianRegisterFile();

    @Test
    public void readWord() {
        int value = 120000;
        bigEndianRegisterFile.writeWord("$t0", value);
        assertEquals(value, bigEndianRegisterFile.readWord("$t0"));
    }
}