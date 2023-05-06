package com.cleverchuk.mips.simulator.fpu;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class FpuRegisterFileTest {

    @InjectMocks
    private FpuRegisterFileArray fpuRegisterFileArray;

    private final String register = "$f1";

    @Test
    public void testReadWord() {
        fpuRegisterFileArray.getFile(register).writeWord(400);
        assertEquals(400, fpuRegisterFileArray.getFile(register).readWord());
    }

    @Test
    public void testReadWord2() {
        fpuRegisterFileArray.getFile(register).writeWord(-400);
        assertEquals(-400, fpuRegisterFileArray.getFile(register).readWord());
    }

    @Test
    public void testReadDword() {
        fpuRegisterFileArray.getFile(register).writeDword(400);
        assertEquals(400, fpuRegisterFileArray.getFile(register).readDword());
    }

    @Test
    public void testReadDword2() {
        fpuRegisterFileArray.getFile(register).writeDword(-400);
        assertEquals(-400, fpuRegisterFileArray.getFile(register).readDword());
    }


    @Test
    public void testReadSingle() {
        fpuRegisterFileArray.getFile(register).writeSingle(400f);
        assertEquals(400f, fpuRegisterFileArray.getFile(register).readSingle(),0);
    }

    @Test
    public void testReadSingle2() {
        fpuRegisterFileArray.getFile(register).writeSingle(-400f);
        assertEquals(-400f, fpuRegisterFileArray.getFile(register).readSingle(),0);
    }


    @Test
    public void testReadDouble() {
        fpuRegisterFileArray.getFile(register).writeDouble(400.5);
        assertEquals(400.5, fpuRegisterFileArray.getFile(register).readDouble(),0);
    }

    @Test
    public void testReadDouble2() {
        fpuRegisterFileArray.getFile(register).writeDouble(-400.5);
        assertEquals(-400.5, fpuRegisterFileArray.getFile(register).readDouble(),0);
    }
}