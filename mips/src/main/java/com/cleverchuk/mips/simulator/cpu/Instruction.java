package com.cleverchuk.mips.simulator.cpu;


public class Instruction {
    public String rd, rs, rt; // register names i.e $t0 , $v0 etc

    public String label; // label if any

    public CpuOpcode CPUOpcode;

    public int immediateValue;

    public int size; // support for ext opcode

    public int pos; // support for ext opcode

    public int offset; // memory offset

    public int line;

    public Instruction(String rd, String rs, String rt, String label, CpuOpcode CPUOpcode, int immediateValue, int size, int pos, int offset, int line) {
        this.rd = rd;
        this.rs = rs;
        this.rt = rt;
        this.label = label;
        this.CPUOpcode = CPUOpcode;
        this.immediateValue = immediateValue;
        this.size = size;
        this.pos = pos;
        this.offset = offset;
        this.line = line;
    }

    public Instruction(CpuOpcode CPUOpcode, String rd, String rs, String rt, String label, int offset) {
        this.rd = rd;
        this.rs = rs;
        this.rt = rt;
        this.label = label;
        this.CPUOpcode = CPUOpcode;
        this.offset = offset;
    }

    public static InstructionBuilder builder() {
        return new InstructionBuilder();
    }

    public static class InstructionBuilder {
        private String rd, rs, rt; // register names i.e $t0 , $v0 etc

        private String label; // label if any

        private CpuOpcode CPUOpcode;

        private int immediateValue;

        private int size; // support for ext opcode

        private int pos; // support for ext opcode

        private int offset; // memory offset

        private int line;

        public InstructionBuilder rd(String rd) {
            this.rd = rd;
            return this;
        }

        public InstructionBuilder rs(String rs) {
            this.rs = rs;
            return this;
        }

        public InstructionBuilder rt(String rt) {
            this.rt = rt;
            return this;
        }

        public InstructionBuilder opcode(CpuOpcode CPUOpcode) {
            this.CPUOpcode = CPUOpcode;
            return this;
        }

        public InstructionBuilder immediateValue(int immediateValue) {
            this.immediateValue = immediateValue;
            return this;
        }

        public InstructionBuilder label(String label) {
            this.label = label;
            return this;
        }

        public InstructionBuilder size(int size) {
            this.size = size;
            return this;
        }

        public InstructionBuilder pos(int pos) {
            this.pos = pos;
            return this;
        }

        public InstructionBuilder offset(int offset) {
            this.offset = offset;
            return this;
        }

        public InstructionBuilder line(int line) {
            this.line = line;
            return this;
        }

        public Instruction build() {
            return new Instruction(rd, rs, rt, label, CPUOpcode, immediateValue, size, pos, offset, line);
        }

    }
}
