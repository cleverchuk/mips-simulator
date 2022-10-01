package com.cleverchuk.mips.simulator.cpu;

import androidx.annotation.NonNull;
import com.cleverchuk.mips.compiler.lexer.MipsLexer;
import java.util.Hashtable;
import java.util.Map;

@SuppressWarnings("all")
public class CpuRegisterFileImpl implements CpuRegisterFile {

    private final int[] DEFAULT = new int[]{0};

    private final Map<String, int[]> regFile = new Hashtable<>();

    private long accumulator;

    public CpuRegisterFileImpl() {
        MipsLexer.CPU_REG
                .forEach(reg -> regFile.put("$" + reg, new int[1]));
    }

    @Override
    public int readWord(@NonNull String file) {
        return regFile.getOrDefault(file, DEFAULT)[0];
    }

    @Override
    public void writeWord(String file, int value) {
        regFile.getOrDefault(file, DEFAULT)[0] = value;
    }

    @Override
    public void accAdd(long value) {
        accumulator += value;
    }

    @Override
    public void accSub(long value) {
        accumulator -= value;
    }

    @Override
    public void accSet(long value) {
        accumulator = value;
    }

    @Override
    public void accSetHI(int value) {
        int temp = (int) (accumulator & 0xffff_ffff);
        accumulator &= 0x0;
        accumulator |= value;
        accumulator <<= 0x20;
        accumulator |= temp;
    }

    @Override
    public void accSetLO(int value) {
        accumulator |= value;
    }

    @Override
    public int accLO() {
        return (int) (accumulator & 0x0000_0000_ffff_ffff);
    }

    @Override
    public int accHI() {
        return (int) ((accumulator & 0xffff_ffff_0000_0000L) >>> 0x20);
    }

    public long getAccumulator() {
        return accumulator;
    }

    @Override
    public String dump() {
        StringBuilder builder = new StringBuilder();
        for (String file : regFile.keySet()) {
            builder.append(file).append(" : ").append(readWord(file)).append("\n");
        }
        return builder.toString();
    }
}
