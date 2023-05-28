package com.cleverchuk.mips.simulator.fpu;

import com.cleverchuk.mips.simulator.cpu.Cpu;
import com.cleverchuk.mips.simulator.cpu.CpuRegisterFile;
import com.cleverchuk.mips.simulator.mem.Memory;
import java.util.Objects;
import java.util.function.Supplier;

import static com.cleverchuk.mips.simulator.fpu.FpuOpcode.Condition.SIGNALING_MASK;

/**
 * A 64-bit register model Coprocessor. Even/Odd register is not supported
 */
public class CoProcessor {
    private final Memory memory;

    private final FpuRegisterFileArray fpuRegisterFileArray;

    private final Supplier<CpuRegisterFile> cpuRegisterFileSupplier;

    private final Supplier<Cpu> cpuSupplier;

    public CoProcessor(Memory memory, FpuRegisterFileArray fpuRegisterFileArray, Supplier<Cpu> cpuSupplier, Supplier<CpuRegisterFile> cpuRegisterFileSupplier) {
        this.memory = memory;
        this.fpuRegisterFileArray = fpuRegisterFileArray;
        this.cpuSupplier = cpuSupplier;
        this.cpuRegisterFileSupplier = cpuRegisterFileSupplier;
    }

    public FpuRegisterFileArray getFpuRegisterFile() {
        return fpuRegisterFileArray;
    }

    public void execute(FpuInstruction fpuInstruction) throws Exception, CoProcessorException {
        switch (fpuInstruction.opcode) {
            case LDC1:
                ldc1(fpuInstruction);
                break;
            case LWC1:
                lwc1(fpuInstruction);
                break;
            case SDC1:
                sdc1(fpuInstruction);
                break;
            case SWC1:
                swc1(fpuInstruction);
                break;
            case CFC1:
                cfc1(fpuInstruction);
                break;
            case CTC1:
                ctc1(fpuInstruction);
                break;
            case MFC1:
                mfc1(fpuInstruction);
                break;
            case MFHC1:
                mfhc1(fpuInstruction);
                break;
            case MTC1:
                mtc1(fpuInstruction);
                break;
            case MTHC1:
                mthc1(fpuInstruction);
                break;
            case ABS_S:
                absS(fpuInstruction);
                break;
            case ABS_D:
                absD(fpuInstruction);
                break;
            case ADD_S:
                addS(fpuInstruction);
                break;
            case ADD_D:
                addD(fpuInstruction);
                break;
            case CMP_AF_D:
            case CMP_UN_D:
            case CMP_EQ_D:
            case CMP_UEQ_D:
            case CMP_LT_D:
            case CMP_ULT_D:
            case CMP_LE_D:
            case CMP_ULE_D:
            case CMP_SAF_D:
            case CMP_SUN_D:
            case CMP_SEQ_D:
            case CMP_SUEQ_D:
            case CMP_SLT_D:
            case CMP_SULT_D:
            case CMP_SLE_D:
            case CMP_SULE_D:
            case CMP_AT_D:
            case CMP_UNE_D:
            case CMP_UGE_D:
            case CMP_OR_D:
            case CMP_NE_D:
            case CMP_OGE_D:
            case CMP_UGT_D:
            case CMP_OGT_D:
            case CMP_SAT_D:
            case CMP_SOR_D:
            case CMP_SUNE_D:
            case CMP_SNE_D:
            case CMP_SUGE_D:
            case CMP_SOGE_D:
            case CMP_SUGT_D:
            case CMP_SOGT_D:
                compareDouble(fpuInstruction);
                break;
            case CMP_AF_S:
            case CMP_UN_S:
            case CMP_EQ_S:
            case CMP_UEQ_S:
            case CMP_LT_S:
            case CMP_ULT_S:
            case CMP_LE_S:
            case CMP_ULE_S:
            case CMP_SAF_S:
            case CMP_SUN_S:
            case CMP_SEQ_S:
            case CMP_SUEQ_S:
            case CMP_SLT_S:
            case CMP_SULT_S:
            case CMP_SLE_S:
            case CMP_SULE_S:
            case CMP_AT_S:
            case CMP_OR_S:
            case CMP_UNE_S:
            case CMP_NE_S:
            case CMP_UGE_S:
            case CMP_OGE_S:
            case CMP_UGT_S:
            case CMP_OGT_S:
            case CMP_SAT_S:
            case CMP_SOR_S:
            case CMP_SUNE_S:
            case CMP_SNE_S:
            case CMP_SUGE_S:
            case CMP_SOGE_S:
            case CMP_SUGT_S:
            case CMP_SOGT_S:
                compareSingle(fpuInstruction);
                break;
            case DIV_S:
                divS(fpuInstruction);
                break;
            case DIV_D:
                divD(fpuInstruction);
                break;
            case MUL_S:
                mulS(fpuInstruction);
                break;
            case MUL_D:
                mulD(fpuInstruction);
                break;
            case NEG_S:
                negS(fpuInstruction);
                break;
            case NEG_D:
                negD(fpuInstruction);
                break;
            case SQRT_S:
                sqrtS(fpuInstruction);
                break;
            case SQRT_D:
                sqrtD(fpuInstruction);
                break;
            case SUB_S:
                subS(fpuInstruction);
                break;
            case SUB_D:
                subD(fpuInstruction);
                break;
            case RECIP_S:
                recipS(fpuInstruction);
                break;
            case RECIP_D:
                recipD(fpuInstruction);
                break;
            case RSQRT_S:
                rsqrtS(fpuInstruction);
                break;
            case RSQRT_D:
                rsqrtD(fpuInstruction);
                break;
            case MADDF_S:
                maddfS(fpuInstruction);
                break;
            case MADDF_D:
                maddfD(fpuInstruction);
                break;
            case MSUBF_S:
                msubfS(fpuInstruction);
                break;
            case MSUBF_D:
                msubfD(fpuInstruction);
                break;
            case CLASS_S:
                classS(fpuInstruction);
                break;
            case CLASS_D:
                classD(fpuInstruction);
                break;
            case MAX_S:
                maxS(fpuInstruction);
                break;
            case MAX_D:
                maxD(fpuInstruction);
                break;
            case MAXA_S:
                maxaS(fpuInstruction);
                break;
            case MAXA_D:
                maxaD(fpuInstruction);
                break;
            case MIN_S:
                minS(fpuInstruction);
                break;
            case MIN_D:
                minD(fpuInstruction);
                break;
            case MINA_S:
                minaS(fpuInstruction);
                break;
            case MINA_D:
                minaD(fpuInstruction);
                break;
            case CVT_D_S:
                cvtdS(fpuInstruction);
                break;
            case CVT_D_W:
                cvtdW(fpuInstruction);
                break;
            case CVT_D_L:
                cvtdL(fpuInstruction);
                break;
            case CVT_L_S:
                cvtlS(fpuInstruction);
                break;
            case CVT_L_D:
                cvtlD(fpuInstruction);
                break;
            case CVT_S_D:
                cvtsD(fpuInstruction);
                break;
            case CVT_S_W:
                cvtsW(fpuInstruction);
                break;
            case CVT_S_L:
                cvtsL(fpuInstruction);
                break;
            case CVT_W_S:
                cvtwS(fpuInstruction);
                break;
            case CVT_W_D:
                cvtwD(fpuInstruction);
                break;
            case RINT_S:
                rintS(fpuInstruction);
                break;
            case RINT_D:
                rintD(fpuInstruction);
                break;
            case CEIL_L_S:
                ceillS(fpuInstruction);
                break;
            case CEIL_L_D:
                ceillD(fpuInstruction);
                break;
            case CEIL_W_S:
                ceilwS(fpuInstruction);
                break;
            case CEIL_W_D:
                ceilwD(fpuInstruction);
                break;
            case FLOOR_L_S:
                floorlS(fpuInstruction);
                break;
            case FLOOR_L_D:
                floorlD(fpuInstruction);
                break;
            case FLOOR_W_S:
                floorwS(fpuInstruction);
                break;
            case FLOOR_W_D:
                floorwD(fpuInstruction);
                break;
            case ROUND_L_S:
                roundlS(fpuInstruction);
                break;
            case ROUND_L_D:
                roundlD(fpuInstruction);
                break;
            case ROUND_W_S:
                roundwS(fpuInstruction);
                break;
            case ROUND_W_D:
                roundwD(fpuInstruction);
                break;
            case TRUNC_L_S:
                trunclS(fpuInstruction);
                break;
            case TRUNC_L_D:
                trunclD(fpuInstruction);
                break;
            case TRUNC_W_S:
                truncwS(fpuInstruction);
                break;
            case TRUNC_W_D:
                truncwD(fpuInstruction);
                break;
            case MOV_S:
                movS(fpuInstruction);
                break;
            case MOV_D:
                movD(fpuInstruction);
                break;
            case SEL_S:
                selS(fpuInstruction);
                break;
            case SEL_D:
                selD(fpuInstruction);
                break;
            case SELEQZ_S:
                seleqzS(fpuInstruction);
                break;
            case SELEQZ_D:
                seleqzD(fpuInstruction);
                break;
            case SELNEZ_S:
                selnezS(fpuInstruction);
                break;
            case SELNEZ_D:
                selnezD(fpuInstruction);
                break;
            case BC1EQZ:
                bc1eqz(fpuInstruction);
                break;
            case BC1NEZ:
                bc1nez(fpuInstruction);
                break;
            default:
                throw new CoProcessorException("Unknown Opcode", 0xff);
        }
    }

    private void ldc1(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        int baseIndex = cpuRegisterFileSupplier.get().read(fpuInstruction.fs);
        long dWord = memory.readDWord(baseIndex + fpuInstruction.offset);

        fd.writeDword(dWord);
    }

    private void lwc1(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        int baseIndex = cpuRegisterFileSupplier.get().read(fpuInstruction.fs);
        int word = memory.readWord(baseIndex + fpuInstruction.offset);

        fd.writeWord(word);
    }

    private void sdc1(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        int baseIndex = cpuRegisterFileSupplier.get().read(fpuInstruction.fs);
        memory.storeDword(fd.readDword(), baseIndex + fpuInstruction.offset);
    }

    private void swc1(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        int baseIndex = cpuRegisterFileSupplier.get().read(fpuInstruction.fs);
        memory.storeWord(fd.readWord(), baseIndex + fpuInstruction.offset);
    }

    private void cfc1(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        cpuRegisterFileSupplier.get().write(fpuInstruction.fd, fs.readWord());
    }

    private void ctc1(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        int value = cpuRegisterFileSupplier.get().read(fpuInstruction.fd);
        fs.writeWord(value);
    }

    private void mfc1(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        cpuRegisterFileSupplier.get().write(fpuInstruction.fd, fs.readWord());
    }

    private void mfhc1(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        long value = fs.readDword();
        cpuRegisterFileSupplier.get().write(fpuInstruction.fd, (int) (value >>> 0x20));
    }

    private void mtc1(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        int value = cpuRegisterFileSupplier.get().read(fpuInstruction.fd);
        fs.writeWord(value);
    }

    private void mthc1(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        long value = cpuRegisterFileSupplier.get().read(fpuInstruction.fd);
        fs.writeDword((value << 0x20) | (fs.readDword() & 0xff_ff_ff_ff_00_00_00_00L));
    }

    private void absS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile registerFile = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        fpuRegisterFileArray.getFile(fpuInstruction.fd)
                .writeSingle(Math.abs(registerFile.readSingle()));
    }

    private void absD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile registerFile = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        fpuRegisterFileArray.getFile(fpuInstruction.fd)
                .writeDouble(Math.abs(registerFile.readDouble()));
    }

    private void addS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);
        fpuRegisterFileArray.getFile(fpuInstruction.fd)
                .writeSingle(fs.readSingle() + ft.readSingle());
    }

    private void addD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);
        fpuRegisterFileArray.getFile(fpuInstruction.fd)
                .writeSingle(fs.readSingle() + ft.readSingle());
    }

    private void compareDouble(FpuInstruction fpuInstruction) throws CoProcessorException {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        double fsV = fs.readDouble(), ftV = ft.readDouble();
        int cond = fpuInstruction.opcode.getCondEncoding();
        boolean unordered = Double.isNaN(fsV) || Double.isNaN(ftV);

        if ((cond & SIGNALING_MASK) > 0 && unordered) {
            throw new CoProcessorException("Signaling", 0xff);
        }

        boolean verdict = false;
        switch (fpuInstruction.opcode.getCondEncoding()) {
            case FpuOpcode.Condition.AF:
            case FpuOpcode.Condition.SAF:
                break;

            case FpuOpcode.Condition.UN:
            case FpuOpcode.Condition.SUN:
                verdict = unordered;
                break;

            case FpuOpcode.Condition.EQ:
            case FpuOpcode.Condition.SEQ:
                verdict = Objects.equals(fsV, ftV);
                break;

            case FpuOpcode.Condition.UEQ:
            case FpuOpcode.Condition.SUEQ:
                verdict = unordered || Objects.equals(fsV, ftV);
                break;

            case FpuOpcode.Condition.LT:
            case FpuOpcode.Condition.SLT:
                verdict = fsV < ftV;
                break;

            case FpuOpcode.Condition.ULT:
            case FpuOpcode.Condition.SULT:
                verdict = unordered || fsV < ftV;
                break;

            case FpuOpcode.Condition.LE:
            case FpuOpcode.Condition.SLE:
                verdict = Objects.equals(fsV, ftV) || fsV < ftV;
                break;

            case FpuOpcode.Condition.ULE:
            case FpuOpcode.Condition.SULE:
                verdict = unordered || Objects.equals(fsV, ftV) || fsV < ftV;
                break;

            case FpuOpcode.Condition.OR:
            case FpuOpcode.Condition.SOR:
                verdict = !unordered;
                break;

            case FpuOpcode.Condition.UNE:
            case FpuOpcode.Condition.SUNE:
                verdict = !Objects.equals(fsV, ftV);
                break;

            case FpuOpcode.Condition.NE:
            case FpuOpcode.Condition.SNE:
                verdict = !unordered && !Objects.equals(fsV, ftV);
                break;

            default:
                throw new CoProcessorException("Unimplemented", 0xff);
        }

        if (verdict) {
            fd.writeOnes(8);
        } else {
            fd.writeZeroes(8);
        }
    }

    private void compareSingle(FpuInstruction fpuInstruction) throws CoProcessorException {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        float fsV = fs.readSingle(), ftV = ft.readSingle();
        int cond = fpuInstruction.opcode.getCondEncoding();
        boolean unordered = Float.isNaN(fsV) || Float.isNaN(ftV);

        if ((cond & SIGNALING_MASK) > 0 && unordered) {
            throw new CoProcessorException("Signaling", 0xff);
        }

        boolean verdict = false;
        switch (fpuInstruction.opcode.getCondEncoding()) {
            case FpuOpcode.Condition.AF:
            case FpuOpcode.Condition.SAF:
                break;

            case FpuOpcode.Condition.UN:
            case FpuOpcode.Condition.SUN:
                verdict = unordered;
                break;

            case FpuOpcode.Condition.EQ:
            case FpuOpcode.Condition.SEQ:
                verdict = Objects.equals(fsV, ftV);
                break;

            case FpuOpcode.Condition.UEQ:
            case FpuOpcode.Condition.SUEQ:
                verdict = unordered || Objects.equals(fsV, ftV);
                break;

            case FpuOpcode.Condition.LT:
            case FpuOpcode.Condition.SLT:
                verdict = fsV < ftV;
                break;

            case FpuOpcode.Condition.ULT:
            case FpuOpcode.Condition.SULT:
                verdict = unordered || fsV < ftV;
                break;

            case FpuOpcode.Condition.LE:
            case FpuOpcode.Condition.SLE:
                verdict = Objects.equals(fsV, ftV) || fsV < ftV;
                break;

            case FpuOpcode.Condition.ULE:
            case FpuOpcode.Condition.SULE:
                verdict = unordered || Objects.equals(fsV, ftV) || fsV < ftV;
                break;

            case FpuOpcode.Condition.OR:
            case FpuOpcode.Condition.SOR:
                verdict = !unordered;
                break;

            case FpuOpcode.Condition.UNE:
            case FpuOpcode.Condition.SUNE:
                verdict = !Objects.equals(fsV, ftV);
                break;

            case FpuOpcode.Condition.NE:
            case FpuOpcode.Condition.SNE:
                verdict = !unordered && !Objects.equals(fsV, ftV);
                break;

            default:
                throw new CoProcessorException("Unimplemented", 0xff);
        }

        if (verdict) {
            fd.writeOnes(4);
        } else {
            fd.writeZeroes(4);
        }
    }

    private void divS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        fd.writeSingle(fs.readSingle() / ft.readSingle());
    }

    private void divD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        fd.writeDouble(fs.readDouble() / ft.readDouble());
    }

    private void mulS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        fd.writeSingle(fs.readSingle() * ft.readSingle());
    }

    private void mulD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        fd.writeDouble(fs.readDouble() * ft.readDouble());
    }

    private void negS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeSingle(-fs.readSingle());
    }

    private void negD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeDouble(-fs.readDouble());
    }

    private void sqrtS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeSingle((float) Math.sqrt(fs.readSingle()));
    }

    private void sqrtD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeDouble(Math.sqrt(fs.readDouble()));
    }

    private void subS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);
        fd.writeSingle(fs.readSingle() - ft.readSingle());
    }

    private void subD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);
        fd.writeDouble(fs.readDouble() - ft.readDouble());
    }

    private void recipS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeSingle(1.0F / fs.readSingle());
    }

    private void recipD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeDouble(1.0 / fs.readDouble());
    }

    private void rsqrtS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeSingle((float) (1.0F / Math.sqrt(fs.readSingle())));
    }

    private void rsqrtD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeDouble(1.0 / Math.sqrt(fs.readDouble()));
    }

    private void maddfS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        fd.writeSingle(fd.readSingle() + fs.readSingle() * ft.readSingle());
    }

    private void maddfD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        fd.writeDouble(fd.readDouble() + fs.readDouble() * ft.readDouble());
    }

    private void msubfS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        fd.writeSingle(fd.readSingle() - fs.readSingle() * ft.readSingle());
    }

    private void msubfD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        fd.writeDouble(fd.readDouble() - fs.readDouble() * ft.readDouble());
    }

    /**
     * @see <a href=https://bugs.openjdk.org/browse/JDK-8076373>sNaN and qNaN discussion</a>
     */
    private void classS(FpuInstruction fpuInstruction) throws CoProcessorException {
        throw new CoProcessorException("Unimplemented", 0xff);
    }

    private void classD(FpuInstruction fpuInstruction) throws CoProcessorException {
        throw new CoProcessorException("Unimplemented", 0xff);
    }

    private void maxS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        fd.writeSingle(Math.max(fs.readSingle(), ft.readSingle()));
    }

    private void maxD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        fd.writeDouble(Math.max(fs.readDouble(), ft.readDouble()));
    }


    private void maxaS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        float fsV = fs.readSingle(), ftV = ft.readSingle();
        if (Math.abs(fsV) > Math.abs(ftV)) {
            fd.writeSingle(fsV);
        } else {
            fd.writeSingle(ftV);
        }
    }

    private void maxaD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        double fsV = fs.readDouble(), ftV = ft.readDouble();
        if (Math.abs(fsV) > Math.abs(ftV)) {
            fd.writeDouble(fsV);
        } else {
            fd.writeDouble(ftV);
        }
    }

    private void minS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        fd.writeSingle(Math.min(fs.readSingle(), ft.readSingle()));
    }

    private void minD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        fd.writeDouble(Math.min(fs.readDouble(), ft.readDouble()));
    }


    private void minaS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        float fsV = fs.readSingle(), ftV = ft.readSingle();
        if (Math.abs(fsV) < Math.abs(ftV)) {
            fd.writeSingle(fsV);
        } else {
            fd.writeSingle(ftV);
        }
    }

    private void minaD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        double fsV = fs.readDouble(), ftV = ft.readDouble();
        if (Math.abs(fsV) < Math.abs(ftV)) {
            fd.writeDouble(fsV);
        } else {
            fd.writeDouble(ftV);
        }
    }

    private void cvtdS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        //FIXME no rounding
        fd.writeDouble((double) fs.readSingle());
    }

    private void cvtdW(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeDouble((double) fs.readWord());
    }

    private void cvtdL(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeDouble((double) fs.readDword());
    }


    private void cvtlS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeDword((long) fs.readSingle());
    }

    private void cvtlD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeDword((long) fs.readDouble());
    }

    private void cvtsD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        //FIXME no rounding
        fd.writeSingle((float) fs.readDouble());
    }

    private void cvtsW(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeSingle((float) fs.readWord());
    }

    private void cvtsL(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeSingle((float) fs.readDword());
    }

    private void cvtwS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeWord((int) fs.readSingle());
    }

    private void cvtwD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeWord((int) fs.readDouble());
    }

    private void rintS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeSingle((float) Math.rint(fs.readSingle()));
    }

    private void rintD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeDouble(Math.rint(fs.readDouble()));
    }

    private void ceillS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeDword((long) Math.ceil(fs.readSingle()));
    }

    private void ceillD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeDword((long) Math.ceil(fs.readDouble()));
    }

    private void ceilwS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeWord((int) Math.ceil(fs.readSingle()));
    }

    private void ceilwD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeWord((int) Math.ceil(fs.readDouble()));
    }

    private void floorlS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeDword((long) Math.floor(fs.readSingle()));
    }

    private void floorlD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeDword((long) Math.floor(fs.readDouble()));
    }

    private void floorwS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeWord((int) Math.floor(fs.readSingle()));
    }

    private void floorwD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeWord((int) Math.floor(fs.readDouble()));
    }

    private void roundlS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeDword((long) Math.round(fs.readSingle()));
    }

    private void roundlD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeDword((long) Math.round(fs.readDouble()));
    }

    private void roundwS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeWord((int) Math.round(fs.readSingle()));
    }

    private void roundwD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeWord((int) Math.round(fs.readDouble()));
    }

    private void trunclS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeDword((long) fs.readSingle());
    }

    private void trunclD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeDword((long) fs.readDouble());
    }

    private void truncwS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeWord((int) fs.readSingle());
    }

    private void truncwD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeWord((int) fs.readDouble());
    }

    private void movS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeSingle(fs.readSingle());
    }

    private void movD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);

        fd.writeDouble(fs.readDouble());
    }

    private void selS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        int fdV = fd.readWord();
        if ((fdV & 0x1) > 0) {
            fd.writeSingle(ft.readSingle());
        } else {
            fd.writeSingle(fs.readSingle());
        }
    }

    private void selD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        long fdV = fd.readDword();
        if ((fdV & 0x1) > 0) {
            fd.writeDouble(ft.readDouble());
        } else {
            fd.writeDouble(fs.readDouble());
        }
    }

    private void seleqzS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        int ftV = ft.readWord();
        if ((ftV & 0x1) > 0) {
            fd.writeSingle(0);
        } else {
            fd.writeSingle(fs.readSingle());
        }
    }

    private void seleqzD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        long ftV = ft.readDword();
        if ((ftV & 0x1) > 0) {
            fd.writeDouble(0.0);
        } else {
            fd.writeDouble(fs.readDouble());
        }
    }

    private void selnezS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        int ftV = ft.readWord();
        if ((ftV & 0x1) == 0) {
            fd.writeSingle(0);
        } else {
            fd.writeSingle(fs.readSingle());
        }
    }

    private void selnezD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        long ftV = ft.readDword();
        if ((ftV & 0x1) == 0) {
            fd.writeDouble(0.0);
        } else {
            fd.writeDouble(fs.readDouble());
        }
    }

    private void bc1eqz(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        int ftV = ft.readWord();
        if ((ftV & 0x1) == 0) {
            cpuSupplier.get().setPC(fpuInstruction.offset);
        }
    }

    private void bc1nez(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        long ftV = ft.readDword();
        if ((ftV & 0x1) > 0) {
            cpuSupplier.get().setPC(fpuInstruction.offset);
        }
    }

}
