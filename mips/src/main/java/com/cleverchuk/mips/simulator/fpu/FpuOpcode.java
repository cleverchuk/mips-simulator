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
    MADDF_S("maddf.s"),
    MADDF_D("maddf.d"),
    MSUBF_S("msubf.s"),
    MSUBF_D("msubf.d"),

    // Floating Point Comparison Instructions
    CLASS_S("class.s"),
    CLASS_D("class.d"),
    MAX_S("max.s"),
    MAX_D("max.d"),
    MAXA_S("maxa.d"),
    MAXA_D("maxa.d"),
    MIN_S("min.s"),
    MIN_D("min.d"),
    MINA_S("mina.d"),
    MINA_D("mina.d"),

    // FPU Conversion Operations Using the FCSR Rounding Mode
    CVT_D_S("cvt.d.s"),
    CVT_D_W("cvt.d.w"),
    CVT_D_L("cvt.d.l"),
    CVT_L_S("cvt.l.s"),
    CVT_L_D("cvt.l.d"),
    CVT_S_D("cvt.s.d"),
    CVT_S_W("cvt.s.w"),
    CVT_S_L("cvt.s.l"),
    CVT_W_S("cvt.w.s"),
    CVT_W_D("cvt.w.d"),
    RINT_S("rint.s"),
    RINT_D("rint.d"),

    // FPU Conversion Operations Using a Directed Rounding Mode
    CEIL_L_S("ceil.l.s"),
    CEIL_L_D("ceil.l.d"),
    CEIL_W_S("ceil.w.s"),
    CEIL_W_D("ceil.w.d"),
    FLOOR_L_S("floor.l.s"),
    FLOOR_L_D("floor.l.d"),
    FLOOR_W_S("floor.w.s"),
    FLOOR_W_D("floor.w.d"),
    ROUND_L_S("round.l.s"),
    ROUND_L_D("round.l.d"),
    ROUND_W_S("round.w.s"),
    ROUND_W_D("round.w.d"),
    TRUNC_L_S("trunc.l.s"),
    TRUNC_L_D("trunc.l.d"),
    TRUNC_W_S("trunc.w.s"),
    TRUNC_W_D("trunc.w.d"),

    // FPU Formatted Unconditional Operand Move Instructions
    MOV_S("mov.s"),
    MOV_D("mov.d"),

    // FPU Conditional Select Instructions
    SEL_S("sel.s"),
    SEL_D("sel.d"),
    SELEQZ_S("seleqz.s"),
    SELEQZ_D("seleqz.d"),
    SELNEZ_S("selnez.s"),
    SELNEZ_D("selnez.d"),

    // FPU Conditional Branch Instructions
    BC1EQZ("bc1eqz"),
    BC1NEZ("bc1nez"),

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
