package com.cleverchuk.mips.simulator.fpu;

import com.cleverchuk.mips.compiler.lexer.MipsLexer;
import java.util.HashMap;
import java.util.Map;

public class FpuRegisterFileArray {
    private final Map<String, RegisterFile> registerFile = new HashMap<>();

    public FpuRegisterFileArray() {
        MipsLexer.DECI_TO_FPU_REG.forEach((key, value) -> registerFile.put("$" + value, new DefaultRegisterFile(Integer.parseInt(key))));
    }

    public RegisterFile getFile(String reg) {
        return registerFile.get(reg);
    }

    public String regContents() {
        StringBuilder content = new StringBuilder();
        for (Map.Entry<String, RegisterFile> regEntry :
                registerFile.entrySet()) {
            content.append(regEntry.getKey()).append(": ")
                    .append(regEntry.getValue().hexValue())
                    .append("\n");
        }

        return content.toString();
    }

    public static class DefaultRegisterFile implements RegisterFile {
        private final byte[] dflops = new byte[8];

        private final short mask = (short) 0xff;

        private final byte shifts = (byte) 0x8;

        private final int id;

        public DefaultRegisterFile(int id) {
            this.id = id;
        }

        @Override
        public int id() {
            return id;
        }

        public void writeOnes(int length) {
            for (int i = 0; i < length; i++) {
                dflops[i] = (byte) (0xff);
            }
        }

        public String hexValue() {
            long dword = readDword();
            return Long.toHexString(dword);
        }

        public void writeZeroes(int length) {
            for (int i = 0; i < length; i++) {
                dflops[i] = (byte) (0x0);
            }
        }

        public void writeWord(int word) {
            for (int i = 0; i < 4; i++) {
                dflops[i] = (byte) (word & mask);
                word >>>= shifts;
            }
        }

        public void writeDword(long dword) {
            for (int i = 0; i < 8; i++) {
                dflops[i] = (byte) (dword & mask);
                dword >>>= shifts;
            }

        }

        public void writeSingle(float single) {
            int intBits = Float.floatToRawIntBits(single);
            for (int i = 0; i < 4; i++) {
                dflops[i] = (byte) (intBits & mask);
                intBits >>>= shifts;
            }
        }

        public void writeDouble(double doubl) {
            long longBits = Double.doubleToRawLongBits(doubl);
            for (int i = 0; i < 8; i++) {
                dflops[i] = (byte) (longBits & mask);
                longBits >>>= shifts;
            }
        }

        public int readWord() {
            int word = 0;
            for (int i = 3; i >= 0; i--) {
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
            for (int i = 3; i >= 0; i--) {
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

    public static class ReadOnlyRegisterFile implements RegisterFile {
        private final RegisterFile delegate;

        public ReadOnlyRegisterFile(RegisterFile delegate, int defaultValue) {
            this.delegate = delegate;
            this.delegate.writeWord(defaultValue);
        }

        @Override
        public int id() {
            return delegate.id();
        }

        @Override
        public String hexValue() {
            return delegate.hexValue();
        }

        @Override
        public int readWord() {
            return delegate.readWord();
        }

        @Override
        public long readDword() {
            return delegate.readWord();
        }

        @Override
        public float readSingle() {
            return delegate.readWord();
        }

        @Override
        public double readDouble() {
            return delegate.readWord();
        }
    }

    public interface RegisterFile {

        int id();

        String hexValue();

        default void writeOnes(int length) {
        }

        default void writeZeroes(int length) {
        }

        default void writeWord(int word) {
        }

        default void writeDword(long dword) {
        }

        default void writeSingle(float single) {
        }

        default void writeDouble(double doubl) {
        }

        int readWord();

        long readDword();

        float readSingle();

        double readDouble();

    }
}
