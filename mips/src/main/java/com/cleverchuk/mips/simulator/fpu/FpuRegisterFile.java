package com.cleverchuk.mips.simulator.fpu;

import com.cleverchuk.mips.compiler.lexer.MipsLexer;
import java.util.HashMap;
import java.util.Map;

public class FpuRegisterFile {
    private final Map<String, RegisterFile> registerFile = new HashMap<>();

    public FpuRegisterFile() {
        MipsLexer.FPU_REG.forEach(reg -> registerFile.put("$" + reg, new RegisterFile()));
    }

    public RegisterFile getFile(String name) {
        return registerFile.get(name);
    }

    public static class RegisterFile {
        private final byte[] dflops = new byte[8];

        private final short mask = (short) 0xff;

        private final byte shifts = (byte) 0x8;

        public void writeWord(int word) {
            int i = 0;
            do {
                dflops[i++] = (byte) (word & mask);
                word >>>= shifts;
            }
            while (word > 0);
        }

        public void writeDword(long word) {
            int i = 0;
            do {
                dflops[i++] = (byte) (word  & mask);
                word >>>= shifts;
            }
            while (word > 0);

        }

        public void writeSingle(float word) {
            int i = 0;
            int intBits = Float.floatToIntBits(word);

            do {
                dflops[i++] = (byte) (intBits  & mask);
                intBits >>>= shifts;
            }
            while (intBits > 0);
        }

        public void writeDouble(double word) {
            int i = 0;
            long longBits = Double.doubleToLongBits(word);

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
