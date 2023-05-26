package com.cleverchuk.mips.simulator.fpu;

import com.cleverchuk.mips.compiler.lexer.MipsLexer;
import java.util.HashMap;
import java.util.Map;

public class FpuRegisterFileArray {
    private final Map<String, RegisterFile> registerFile = new HashMap<>();

    public FpuRegisterFileArray() {
        MipsLexer.DECI_TO_FPU_REG.values().forEach(reg -> registerFile.put("$" + reg, new RegisterFile()));
    }

    public RegisterFile getFile(String reg) {
        return registerFile.get(reg);
    }

    public static class RegisterFile {
        private final byte[] dflops = new byte[8];

        private final short mask = (short) 0xff;

        private final byte shifts = (byte) 0x8;

        public void writeOnes(int length){
            for (int i = length - 1; i >= 0; i--){
                dflops[dflops.length - i - 1] = (byte) (0xff);
            }
        }


        public void writeZeroes(int length){
            for (int i = length - 1; i >= 0; i--){
                dflops[dflops.length - i - 1] = (byte) (0x0);
            }
        }

        public void writeWord(int word) {
            int i = 0;
            do {
                dflops[i++] = (byte) (word & mask);
                word >>>= shifts;
            }
            while (word > 0);
        }

        public void writeDword(long dword) {
            int i = 0;
            do {
                dflops[i++] = (byte) (dword & mask);
                dword >>>= shifts;
            }
            while (dword > 0);

        }

        public void writeSingle(float single) {
            int i = 0;
            int intBits = Float.floatToRawIntBits(single);

            do {
                dflops[i++] = (byte) (intBits & mask);
                intBits >>>= shifts;
            }
            while (intBits > 0);
        }

        public void writeDouble(double doubl) {
            int i = 0;
            long longBits = Double.doubleToRawLongBits(doubl);

            do {
                dflops[i++] = (byte) (longBits & mask);
                longBits >>>= shifts;
            }
            while (longBits > 0);
        }

        public int readWord() {
            int word = 0;
            for (int i = 7; i >= 0; i--) {
                word <<= shifts;
                word |= (mask & dflops[i]);
            }

            return word;
        }

        public long readDword() {
            long dWord = 0;
            for (int i = 7; i >= 0; i--) {
                dWord <<= shifts;
                dWord |= (mask & dflops[i]);
            }

            return dWord;
        }

        public float readSingle() {
            int single = 0;
            for (int i = 7; i >= 0; i--) {
                single <<= shifts;
                single |= (mask & dflops[i]);
            }

            return Float.intBitsToFloat(single);
        }

        public double readDouble() {
            long doubly = 0;
            for (int i = 7; i >= 0; i--) {
                doubly <<= shifts;
                doubly |= (mask & dflops[i]);
            }

            return Double.longBitsToDouble(doubly);
        }
    }
}
