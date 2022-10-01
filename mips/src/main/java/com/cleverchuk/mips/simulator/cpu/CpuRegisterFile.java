package com.cleverchuk.mips.simulator.cpu;

import androidx.annotation.NonNull;
import com.cleverchuk.mips.compiler.lexer.MipsLexer;
import java.util.Hashtable;
import java.util.Map;

@SuppressWarnings("all")
public class CpuRegisterFile {

    private final int[] DEFAULT = new int[]{0};

    private final Map<String, int[]> regFile = new Hashtable<>();

    private long accumulator;

    public CpuRegisterFile() {
        MipsLexer.CPU_REG
                .forEach(reg -> regFile.put("$" + reg, new int[1]));
    }

    public int read(@NonNull String file) {
        return regFile.getOrDefault(file, DEFAULT)[0];
    }

    public void write(String file, int value) {
        regFile.getOrDefault(file, DEFAULT)[0] = value;
    }

    public void accAdd(long value) {
        accumulator += value;
    }

    public void accSub(long value) {
        accumulator -= value;
    }

    public void accSet(long value) {
        accumulator = value;
    }

    public void accSetHI(int value) {
        int temp = (int) (accumulator & 0xffff_ffff);
        accumulator &= 0x0;
        accumulator |= value;
        accumulator <<= 0x20;
        accumulator |= temp;
    }

    public void accSetLO(int value) {
        accumulator |= value;
    }

    public int accLO() {
        return (int) (accumulator & 0x0000_0000_ffff_ffff);
    }

    public int accHI() {
        return (int) ((accumulator & 0xffff_ffff_0000_0000L) >>> 0x20);
    }

    public long getAccumulator() {
        return accumulator;
    }

    public String dump() {
        StringBuilder builder = new StringBuilder();
        for (String file : regFile.keySet()) {
            builder.append(file).append(" : ").append(read(file)).append("\n");
        }
        return builder.toString();
    }
}
