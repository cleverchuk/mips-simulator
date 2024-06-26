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

package com.cleverchuk.mips.simulator.fpu;

import com.cleverchuk.mips.simulator.VirtualInstruction;

public class FpuInstruction implements VirtualInstruction {
    public final FpuOpcode opcode;

    public final String ft;

    public final String fs;

    public final String fd;

    public final String rt;

    public final int offset;

    public final int line;


    public FpuInstruction(FpuOpcode opcode, String ft, String fs, String fd, String rt, int offset, int line) {
        this.opcode = opcode;
        this.ft = ft;
        this.fs = fs;
        this.fd = fd;
        this.rt = rt;
        this.offset = offset;
        this.line = line;
    }


    @Override
    public int line() {
        return line;
    }

    public static FpuInstructionBuilder builder() {
        return new FpuInstructionBuilder();
    }

    public static class FpuInstructionBuilder {
        private FpuInstructionBuilder() {
        }

        private FpuOpcode opcode;

        private String ft;

        private String fs;

        private String fd;

        private String rt;

        private int offset;

        private int line;

        public FpuInstructionBuilder opcode(FpuOpcode opcode) {
            this.opcode = opcode;
            return this;
        }

        public FpuInstructionBuilder ft(String ft) {
            this.ft = ft;
            return this;
        }

        public FpuInstructionBuilder fs(String fs) {
            this.fs = fs;
            return this;
        }

        public FpuInstructionBuilder fd(String fd) {
            this.fd = fd;
            return this;
        }

        public FpuInstructionBuilder rt(String rt) {
            this.rt = rt;
            return this;
        }

        public FpuInstructionBuilder offset(int offset){
            this.offset = offset;
            return this;
        }
        public FpuInstructionBuilder line(int line) {
            this.line = line;
            return this;
        }

        public FpuInstruction build() {
            return new FpuInstruction(this.opcode, this.ft, this.fs, this.fd, this.rt, offset, this.line);
        }
    }
}
