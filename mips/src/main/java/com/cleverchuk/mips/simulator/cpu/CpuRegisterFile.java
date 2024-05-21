/*
 * MIT License
 *
 * Copyright (c) 2022 CleverChuk
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
