package com.cleverchuk.mips.simulator.cpu;

import androidx.annotation.NonNull;
import com.cleverchuk.mips.compiler.lexer.MipsLexer;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("all")
public class CpuRegisterFile {

    private final int[] DEFAULT = new int[]{0};

    private final Map<String, int[]> regFile = new HashMap<>();

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
        long temp = value;
        accumulator &= 0x0000_0000_ffff_ffff;
        accumulator |= (temp << 0x20);
    }

    public void accSetLO(int value) {
        accumulator &= 0xffff_ffff_0000_0000L;
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
        regFile.keySet()
                .stream()
                .sorted()
                .forEach(file -> builder.append(file)
                        .append(" : ")
                        .append(read(file))
                        .append("\n")
                );

        return builder.toString();
    }
}
