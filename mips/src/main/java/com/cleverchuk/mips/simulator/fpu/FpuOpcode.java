package com.cleverchuk.mips.simulator.fpu;

import androidx.annotation.NonNull;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
@SuppressWarnings("all")
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
    MTHC1("mthc1"),

    // FPU IEEE arithmetic operations
    ABS_S("abs.s"),
    ABS_D("abs.d"),
    ADD_S("add.s"),
    ADD_D("add.d"),
    CMP_COND_S("cmp.cond.s"),
    CMP_COND_D("cmp.cond.d"),
    DIV_S("div.s"),
    DIV_D("div.d"),
    MUL_S("mul.s"),
    MUL_D("mul.d"),
    NEG_S("neg.s"),
    NEG_D("neg.d"),
    SQRT_S("sqrt.s"),
    SQRT_D("sqrt.d"),
    SUB_S("sub.s"),
    SUB_D("sub.d"),

    // FPU non-IEEE-approximate Arithmetic Instructions
    RECIP_S("recip.s"),
    RECIP_D("recip.d"),
    RSQRT_S("rsqrt.s"),
    RSQRT_D("rsqrt.d"),

    // FPU Fused Multiply-Accumulate Arithmetic Operations (Release 6)


    ;
    public static Set<String> FPU_OPCODES = Arrays.stream(FpuOpcode.values()).map(FpuOpcode::getValue)
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
