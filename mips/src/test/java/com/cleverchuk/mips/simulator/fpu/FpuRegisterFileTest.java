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

    @Test
    public void testReadWord() {
        String filename = "$f1";
        fpuRegisterFile.getFile(filename).writeWord(400);
        assertEquals(400, fpuRegisterFile.getFile(filename).readWord());
    }

    @Test
    public void testReadWord2() {
        String filename = "$f1";
        fpuRegisterFile.getFile(filename).writeWord(-400);
        assertEquals(-400, fpuRegisterFile.getFile(filename).readWord());
    }

    @Test
    public void testReadDword() {
        String filename = "$f1";
        fpuRegisterFile.getFile(filename).writeDword(400);
        assertEquals(400, fpuRegisterFile.getFile(filename).readDword());
    }

    @Test
    public void testReadDword2() {
        String filename = "$f1";
        fpuRegisterFile.getFile(filename).writeDword(-400);
        assertEquals(-400, fpuRegisterFile.getFile(filename).readDword());
    }


    @Test
    public void testReadSingle() {
        String filename = "$f1";
        fpuRegisterFile.getFile(filename).writeSingle(400f);
        assertEquals(400f, fpuRegisterFile.getFile(filename).readSingle(),0);
    }

    @Test
    public void testReadSingle2() {
        String filename = "$f1";
        fpuRegisterFile.getFile(filename).writeSingle(-400f);
        assertEquals(-400f, fpuRegisterFile.getFile(filename).readSingle(),0);
    }


    @Test
    public void testReadDouble() {
        String filename = "$f1";
        fpuRegisterFile.getFile(filename).writeDouble(400.5);
        assertEquals(400.5, fpuRegisterFile.getFile(filename).readDouble(),0);
    }

    @Test
    public void testReadDouble2() {
        String filename = "$f1";
        fpuRegisterFile.getFile(filename).writeDouble(-400.5);
        assertEquals(-400.5, fpuRegisterFile.getFile(filename).readDouble(),0);
    }
}