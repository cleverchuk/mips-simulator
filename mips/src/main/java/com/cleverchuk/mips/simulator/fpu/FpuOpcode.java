package com.cleverchuk.mips.simulator.fpu;

import androidx.annotation.NonNull;
import com.cleverchuk.mips.simulator.cpu.CpuOpcode;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum FpuOpcode {
    // Data transfer instructions
    LDC1("ldc1"),
    LWC1("lwc1"),
    SDC1("sdc1"),
    SWC1("swc1"),
    CFC1("cfc1"),
    CTC1("ctc1"),
    MFC1("mfc1"),
    MFHC1("mfhc1"),
    MTC1("mtc1"),
    MTHC1("mthc1")

    ;
    public static Set<String> FPU_OPCODE = Arrays.stream(CpuOpcode.values()).map(CpuOpcode::getValue)
            .collect(Collectors.toSet());

    private final String value;

    FpuOpcode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @NonNull
    @Override
    public String toString() {
        return value;
    }

    public boolean same(String opcode){
        return value.equals(opcode);
    }

    public static FpuOpcode parse(String opcode) {
        return valueOf(opcode.toUpperCase());
    }
}
