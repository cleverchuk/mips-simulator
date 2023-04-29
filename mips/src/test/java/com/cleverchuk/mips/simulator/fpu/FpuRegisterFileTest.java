package com.cleverchuk.mips.simulator.fpu;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class FpuRegisterFileTest {

    @InjectMocks
    private FpuRegisterFile fpuRegisterFile;

    private final int register = 1;

    @Test
    public void testReadWord() {
        fpuRegisterFile.getFile(register).writeWord(400);
        assertEquals(400, fpuRegisterFile.getFile(register).readWord());
    }

    @Test
    public void testReadWord2() {
        fpuRegisterFile.getFile(register).writeWord(-400);
        assertEquals(-400, fpuRegisterFile.getFile(register).readWord());
    }

    @Test
    public void testReadDword() {
        fpuRegisterFile.getFile(register).writeDword(400);
        assertEquals(400, fpuRegisterFile.getFile(register).readDword());
    }

    @Test
    public void testReadDword2() {
        fpuRegisterFile.getFile(register).writeDword(-400);
        assertEquals(-400, fpuRegisterFile.getFile(register).readDword());
    }


    @Test
    public void testReadSingle() {
        fpuRegisterFile.getFile(register).writeSingle(400f);
        assertEquals(400f, fpuRegisterFile.getFile(register).readSingle(),0);
    }

    @Test
    public void testReadSingle2() {
        fpuRegisterFile.getFile(register).writeSingle(-400f);
        assertEquals(-400f, fpuRegisterFile.getFile(register).readSingle(),0);
    }


    @Test
    public void testReadDouble() {
        fpuRegisterFile.getFile(register).writeDouble(400.5);
        assertEquals(400.5, fpuRegisterFile.getFile(register).readDouble(),0);
    }

    @Test
    public void testReadDouble2() {
        fpuRegisterFile.getFile(register).writeDouble(-400.5);
        assertEquals(-400.5, fpuRegisterFile.getFile(register).readDouble(),0);
    }
}