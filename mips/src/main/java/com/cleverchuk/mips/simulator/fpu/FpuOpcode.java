package com.cleverchuk.mips.simulator.fpu;

import androidx.annotation.NonNull;
import com.cleverchuk.mips.simulator.Opcode;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("all")
public enum FpuOpcode implements Opcode {

    // Data transfer instructions
    LDC1("ldc1", Condition.UNDEFINED),
    LWC1("lwc1", Condition.UNDEFINED),
    SDC1("sdc1", Condition.UNDEFINED),
    SWC1("swc1", Condition.UNDEFINED),
    CFC1("cfc1", Condition.UNDEFINED),
    CTC1("ctc1", Condition.UNDEFINED),
    MFC1("mfc1", Condition.UNDEFINED),
    MFHC1("mfhc1", Condition.UNDEFINED),
    MTC1("mtc1", Condition.UNDEFINED),
    MTHC1("mthc1", Condition.UNDEFINED),

    // FPU IEEE arithmetic operations
    ABS_S("abs.s", Condition.UNDEFINED),
    ABS_D("abs.d", Condition.UNDEFINED),
    ADD_S("add.s", Condition.UNDEFINED),
    ADD_D("add.d", Condition.UNDEFINED),
    CMP_AF_S("cmp.af.s", Condition.AF),
    CMP_AF_D("cmp.af.d", Condition.AF),
    CMP_UN_S("cmp.un.s", Condition.UN),
    CMP_UN_D("cmp.un.d", Condition.UN),
    CMP_EQ_S("cmp.eq.s", Condition.EQ),
    CMP_EQ_D("cmp.eq.d", Condition.EQ),
    CMP_UEQ_S("cmp.ueq.s", Condition.UEQ),
    CMP_UEQ_D("cmp.ueq.d", Condition.UEQ),
    CMP_LT_S("cmp.lt.s", Condition.LT),
    CMP_LT_D("cmp.lt.d", Condition.LT),
    CMP_ULT_S("cmp.ult.s", Condition.ULT),
    CMP_ULT_D("cmp.ult.d", Condition.ULT),
    CMP_LE_S("cmp.le.s", Condition.LE),
    CMP_LE_D("cmp.le.d", Condition.LE),
    CMP_ULE_S("cmp.ule.s", Condition.ULE),
    CMP_ULE_D("cmp.ule.d", Condition.ULE),
    CMP_SAF_S("cmp.saf.s", Condition.SAF),
    CMP_SAF_D("cmp.saf.d", Condition.SAF),
    CMP_SUN_S("cmp.sun.s", Condition.SUN),
    CMP_SUN_D("cmp.sun.d", Condition.SUN),
    CMP_SEQ_S("cmp.seq.s", Condition.SEQ),
    CMP_SEQ_D("cmp.seq.d", Condition.SEQ),
    CMP_SUEQ_S("cmp.sueq.s", Condition.SUEQ),
    CMP_SUEQ_D("cmp.sueq.d", Condition.SUEQ),
    CMP_SLT_S("cmp.slt.s", Condition.SLT),
    CMP_SLT_D("cmp.slt.d", Condition.SLT),
    CMP_SULT_S("cmp.sult.s", Condition.SULT),
    CMP_SULT_D("cmp.sult.d", Condition.SULT),
    CMP_SLE_S("cmp.sle.s", Condition.SLE),
    CMP_SLE_D("cmp.sle.d", Condition.SLE),
    CMP_SULE_S("cmp.sule.s", Condition.SULE),
    CMP_SULE_D("cmp.sule.d", Condition.SULE),
    CMP_AT_S("cmp.at.s", Condition.AT),
    CMP_AT_D("cmp.at.d", Condition.AT),
    CMP_OR_S("cmp.or.s", Condition.OR),
    CMP_OR_D("cmp.or.d", Condition.OR),
    CMP_UNE_S("cmp.une.s", Condition.UNE),
    CMP_UNE_D("cmp.une.d", Condition.UNE),
    CMP_NE_S("cmp.ne.s", Condition.NE),
    CMP_NE_D("cmp.ne.d", Condition.NE),
    CMP_UGE_S("cmp.uge.s", Condition.UGE),
    CMP_UGE_D("cmp.uge.d", Condition.UGE),
    CMP_OGE_S("cmp.oge.s", Condition.OGE),
    CMP_OGE_D("cmp.oge.d", Condition.OGE),
    CMP_UGT_S("cmp.ugt.s", Condition.UGT),
    CMP_UGT_D("cmp.ugt.d", Condition.UGT),
    CMP_OGT_S("cmp.ogt.s", Condition.OGT),
    CMP_OGT_D("cmp.ogt.d", Condition.OGT),
    CMP_SAT_S("cmp.sat.s", Condition.SAT),
    CMP_SAT_D("cmp.sat.d", Condition.SAT),
    CMP_SOR_S("cmp.sor.s", Condition.SOR),
    CMP_SOR_D("cmp.sor.d", Condition.SOR),
    CMP_SUNE_S("cmp.sune.s", Condition.SUNE),
    CMP_SUNE_D("cmp.sune.d", Condition.SUNE),
    CMP_SNE_S("cmp.sne.s", Condition.SNE),
    CMP_SNE_D("cmp.sne.d", Condition.SNE),
    CMP_SUGE_S("cmp.suge.s", Condition.SUGE),
    CMP_SUGE_D("cmp.suge.d", Condition.SUGE),
    CMP_SOGE_S("cmp.soge.s", Condition.SOGE),
    CMP_SOGE_D("cmp.soge.d", Condition.SOGE),
    CMP_SUGT_S("cmp.sugt.s", Condition.SUGT),
    CMP_SUGT_D("cmp.sugt.d", Condition.SUGT),
    CMP_SOGT_S("cmp.sogt.s", Condition.SOGT),
    CMP_SOGT_D("cmp.sogt.d", Condition.SOGT),
    DIV_S("div.s", Condition.UNDEFINED),
    DIV_D("div.d", Condition.UNDEFINED),
    MUL_S("mul.s", Condition.UNDEFINED),
    MUL_D("mul.d", Condition.UNDEFINED),
    NEG_S("neg.s", Condition.UNDEFINED),
    NEG_D("neg.d", Condition.UNDEFINED),
    SQRT_S("sqrt.s", Condition.UNDEFINED),
    SQRT_D("sqrt.d", Condition.UNDEFINED),
    SUB_S("sub.s", Condition.UNDEFINED),
    SUB_D("sub.d", Condition.UNDEFINED),

    // FPU non-IEEE-approximate Arithmetic Instructions
    RECIP_S("recip.s", Condition.UNDEFINED),
    RECIP_D("recip.d", Condition.UNDEFINED),
    RSQRT_S("rsqrt.s", Condition.UNDEFINED),
    RSQRT_D("rsqrt.d", Condition.UNDEFINED),

    // FPU Fused Multiply-Accumulate Arithmetic Operations (Release 6)
    MADDF_S("maddf.s", Condition.UNDEFINED),
    MADDF_D("maddf.d", Condition.UNDEFINED),
    MSUBF_S("msubf.s", Condition.UNDEFINED),
    MSUBF_D("msubf.d", Condition.UNDEFINED),

    // Floating Point Comparison Instructions
    CLASS_S("class.s", Condition.UNDEFINED),
    CLASS_D("class.d", Condition.UNDEFINED),
    MAX_S("max.s", Condition.UNDEFINED),
    MAX_D("max.d", Condition.UNDEFINED),
    MAXA_S("maxa.d", Condition.UNDEFINED),
    MAXA_D("maxa.d", Condition.UNDEFINED),
    MIN_S("min.s", Condition.UNDEFINED),
    MIN_D("min.d", Condition.UNDEFINED),
    MINA_S("mina.d", Condition.UNDEFINED),
    MINA_D("mina.d", Condition.UNDEFINED),

    // FPU Conversion Operations Using the FCSR Rounding Mode
    CVT_D_S("cvt.d.s", Condition.UNDEFINED),
    CVT_D_W("cvt.d.w", Condition.UNDEFINED),
    CVT_D_L("cvt.d.l", Condition.UNDEFINED),
    CVT_L_S("cvt.l.s", Condition.UNDEFINED),
    CVT_L_D("cvt.l.d", Condition.UNDEFINED),
    CVT_S_D("cvt.s.d", Condition.UNDEFINED),
    CVT_S_W("cvt.s.w", Condition.UNDEFINED),
    CVT_S_L("cvt.s.l", Condition.UNDEFINED),
    CVT_W_S("cvt.w.s", Condition.UNDEFINED),
    CVT_W_D("cvt.w.d", Condition.UNDEFINED),
    RINT_S("rint.s", Condition.UNDEFINED),
    RINT_D("rint.d", Condition.UNDEFINED),

    // FPU Conversion Operations Using a Directed Rounding Mode
    CEIL_L_S("ceil.l.s", Condition.UNDEFINED),
    CEIL_L_D("ceil.l.d", Condition.UNDEFINED),
    CEIL_W_S("ceil.w.s", Condition.UNDEFINED),
    CEIL_W_D("ceil.w.d", Condition.UNDEFINED),
    FLOOR_L_S("floor.l.s", Condition.UNDEFINED),
    FLOOR_L_D("floor.l.d", Condition.UNDEFINED),
    FLOOR_W_S("floor.w.s", Condition.UNDEFINED),
    FLOOR_W_D("floor.w.d", Condition.UNDEFINED),
    ROUND_L_S("round.l.s", Condition.UNDEFINED),
    ROUND_L_D("round.l.d", Condition.UNDEFINED),
    ROUND_W_S("round.w.s", Condition.UNDEFINED),
    ROUND_W_D("round.w.d", Condition.UNDEFINED),
    TRUNC_L_S("trunc.l.s", Condition.UNDEFINED),
    TRUNC_L_D("trunc.l.d", Condition.UNDEFINED),
    TRUNC_W_S("trunc.w.s", Condition.UNDEFINED),
    TRUNC_W_D("trunc.w.d", Condition.UNDEFINED),

    // FPU Formatted Unconditional Operand Move Instructions
    MOV_S("mov.s", Condition.UNDEFINED),
    MOV_D("mov.d", Condition.UNDEFINED),

    // FPU Conditional Select Instructions
    SEL_S("sel.s", Condition.UNDEFINED),
    SEL_D("sel.d", Condition.UNDEFINED),
    SELEQZ_S("seleqz.s", Condition.UNDEFINED),
    SELEQZ_D("seleqz.d", Condition.UNDEFINED),
    SELNEZ_S("selnez.s", Condition.UNDEFINED),
    SELNEZ_D("selnez.d", Condition.UNDEFINED),

    // FPU Conditional Branch Instructions
    BC1EQZ("bc1eqz", Condition.UNDEFINED),
    BC1NEZ("bc1nez", Condition.UNDEFINED),

    ;

    public static Set<String> FPU_OPCODES = Arrays.stream(FpuOpcode.values()).map(FpuOpcode::getName)
            .collect(Collectors.toSet());

    private final String name;

    private final int condEncoding;

    FpuOpcode(String name, int condEncoding) {
        this.name = name;
        this.condEncoding = condEncoding;
    }

    public String getName() {
        return name;
    }

    public int getCondEncoding(){
        return condEncoding;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    public boolean same(String opcode) {
        return name.equals(opcode);
    }

    public static FpuOpcode parse(String opcode) {
        try {
            return valueOf(opcode.toUpperCase().replace('.', '_'));
        } catch (Throwable ignore) {
        }
        return null;
    }

    public static class Condition {
        public static final int AF = 0x0;
        public static final int UN = 0x1;
        public static final int EQ = 0x2;
        public static final int UEQ = 0x3;
        public static final int LT = 0x4;
        public static final int ULT = 0x5;
        public static final int LE = 0x6;
        public static final int ULE = 0x7;
        public static final int AT = 0x10;
        public static final int OR = 0x11;
        public static final int UNE = 0x12;
        public static final int NE = 0x13;
        public static final int UGE = 0x14;
        public static final int OGE = 0x15;
        public static final int UGT = 0x16;
        public static final int OGT = 0x17;

        public static final int SAF = 0x8;
        public static final int SUN = 0x9;
        public static final int SEQ = 0xa;
        public static final int SUEQ = 0xb;
        public static final int SLT = 0xc;
        public static final int SULT = 0xd;
        public static final int SLE = 0xe;
        public static final int SULE = 0xf;
        public static final int SAT = 0x18;
        public static final int SOR = 0x19;
        public static final int SUNE = 0x1a;
        public static final int SNE = 0x1b;
        public static final int SUGE = 0x1c;
        public static final int SOGE = 0x1d;
        public static final int SUGT = 0x1e;
        public static final int SOGT = 0x1f;
        public static final int UNDEFINED = 0xff;

        public static final int SIGNALING_MASK = 0x10;

    }
}
