package com.cleverchuk.mips.emulator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class Instruction {
    public String rd, rs, rt; // register names i.e $t0 , $v0 etc

    public String label; // label if any

    public Opcode opcode;

    public int immediateValue;

    public int size; // support for ext opcode

    public int pos; // support for ext opcode

    @Builder.Default
    public int offset = -1; // memory offset

    public int line;

    public Instruction(Opcode opcode, String rd, String rs, String rt, String label, int offset) {
        this.rd = rd;
        this.rs = rs;
        this.rt = rt;
        this.label = label;
        this.opcode = opcode;
        this.offset = offset;
    }
}
