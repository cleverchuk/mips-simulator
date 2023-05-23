package com.cleverchuk.mips.simulator.fpu;

import com.cleverchuk.mips.simulator.cpu.CpuRegisterFile;
import com.cleverchuk.mips.simulator.mem.Memory;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A 64-bit register model Coprocessor. Even/Odd register is not supported
 */
public class CoProcessor {
    private final Memory memory;

    private final FpuRegisterFileArray fpuRegisterFileArray;

    private final Supplier<CpuRegisterFile> cpuRegisterFileSupplier;

    public CoProcessor(Memory memory, FpuRegisterFileArray fpuRegisterFileArray, Supplier<CpuRegisterFile> cpuRegisterFileSupplier) {
        this.memory = memory;
        this.fpuRegisterFileArray = fpuRegisterFileArray;
        this.cpuRegisterFileSupplier = cpuRegisterFileSupplier;
    }

    public FpuRegisterFileArray getFpuRegisterFile() {
        return fpuRegisterFileArray;
    }

    public void execute(FpuInstruction fpuInstruction) throws Exception {
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
                break;

            case DIV_S:
                break;
            case DIV_D:
                break;
            case MUL_S:
                break;
            case MUL_D:
                break;
            case NEG_S:
                break;
            case NEG_D:
                break;
            case SQRT_S:
                break;
            case SQRT_D:
                break;
            case SUB_S:
                break;
            case SUB_D:
                break;
            case RECIP_S:
                break;
            case RECIP_D:
                break;
            case RSQRT_S:
                break;
            case RSQRT_D:
                break;
            case MADDF_S:
                break;
            case MADDF_D:
                break;
            case MSUBF_S:
                break;
            case MSUBF_D:
                break;
            case CLASS_S:
                break;
            case CLASS_D:
                break;
            case MAX_S:
                break;
            case MAX_D:
                break;
            case MAXA_S:
                break;
            case MAXA_D:
                break;
            case MIN_S:
                break;
            case MIN_D:
                break;
            case MINA_S:
                break;
            case MINA_D:
                break;
            case CVT_D_S:
                break;
            case CVT_D_W:
                break;
            case CVT_D_L:
                break;
            case CVT_L_S:
                break;
            case CVT_L_D:
                break;
            case CVT_S_D:
                break;
            case CVT_S_W:
                break;
            case CVT_S_L:
                break;
            case CVT_W_S:
                break;
            case CVT_W_D:
                break;
            case RINT_S:
                break;
            case RINT_D:
                break;
            case CEIL_L_S:
                break;
            case CEIL_L_D:
                break;
            case CEIL_W_S:
                break;
            case CEIL_W_D:
                break;
            case FLOOR_L_S:
                break;
            case FLOOR_L_D:
                break;
            case FLOOR_W_S:
                break;
            case FLOOR_W_D:
                break;
            case ROUND_L_S:
                break;
            case ROUND_L_D:
                break;
            case ROUND_W_S:
                break;
            case ROUND_W_D:
                break;
            case TRUNC_L_S:
                break;
            case TRUNC_L_D:
                break;
            case TRUNC_W_S:
                break;
            case TRUNC_W_D:
                break;
            case MOV_S:
                break;
            case MOV_D:
                break;
            case SEL_S:
                break;
            case SEL_D:
                break;
            case SELEQZ_S:
                break;
            case SELEQZ_D:
                break;
            case SELNEZ_S:
                break;
            case SELNEZ_D:
                break;
            case BC1EQZ:
                break;
            case BC1NEZ:
                break;
            default:
                throw new Exception("Fatal error!");
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

    private void compareDouble(FpuInstruction  fpuInstruction){

    }
    private void cmpAfS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile file = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        file.writeSingle(0);
    }

    private void cmpAfD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile file = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        file.writeDouble(0);
    }

    private void cmpUnS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        if (Float.isNaN(fs.readSingle()) || Float.isNaN(ft.readSingle())) {
            fd.writeOnes(4);
        } else {
            fd.writeZeroes(4);
        }
    }

    private void cmpUnD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        if (Double.isNaN(fs.readDouble()) || Double.isNaN(ft.readDouble())) {
            fd.writeOnes(8);
        } else {
            fd.writeZeroes(8);
        }
    }

    private void cmpEqS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        if (Objects.equals(fs.readSingle(), ft.readSingle())) {
            fd.writeOnes(4);
        } else {
            fd.writeZeroes(4);
        }
    }

    private void cmpEqD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        if (Objects.equals(fs.readDouble(), ft.readDouble())) {
            fd.writeOnes(8);
        } else {
            fd.writeZeroes(8);
        }
    }

    private void cmpUeqS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        if (Float.isNaN(fs.readSingle()) || Float.isNaN(ft.readSingle())) {
            fd.writeOnes(4);
        } else if (Objects.equals(fs.readSingle(), ft.readSingle())) {
            fd.writeOnes(4);
        } else {
            fd.writeZeroes(4);
        }
    }

    private void cmpUeqD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        if (Double.isNaN(fs.readDouble()) || Double.isNaN(ft.readDouble())) {
            fd.writeOnes(8);
        } else if (Objects.equals(fs.readDouble(), ft.readDouble())) {
            fd.writeOnes(8);
        } else {
            fd.writeZeroes(8);
        }
    }

    private void cmpLtS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        if (fs.readSingle() < ft.readSingle()) {
            fd.writeOnes(4);
        } else {
            fd.writeZeroes(4);
        }
    }

    private void cmpLtD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        if (fs.readDouble() < ft.readDouble()) {
            fd.writeOnes(8);
        } else {
            fd.writeZeroes(8);
        }
    }

    private void cmpUltS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        if (Float.isNaN(fs.readSingle()) || Float.isNaN(ft.readSingle())) {
            fd.writeOnes(4);
        } else if (fs.readSingle() < ft.readSingle()) {
            fd.writeOnes(4);
        } else {
            fd.writeZeroes(4);
        }
    }

    private void cmpUltD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        if (Double.isNaN(fs.readDouble()) || Double.isNaN(ft.readDouble())) {
            fd.writeOnes(8);
        } else if (fs.readDouble() < ft.readDouble()) {
            fd.writeOnes(8);
        } else {
            fd.writeZeroes(8);
        }
    }

    private void cmpLeS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        if (fs.readSingle() < ft.readSingle() || Objects.equals(fs.readSingle(), ft.readSingle())) {
            fd.writeOnes(4);
        } else {
            fd.writeZeroes(4);
        }
    }

    private void cmpLeD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        if (fs.readDouble() < ft.readDouble() || Objects.equals(fs.readDouble(), ft.readDouble())) {
            fd.writeOnes(8);
        } else {
            fd.writeZeroes(8);
        }
    }

    private void cmpUleS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        if (Float.isNaN(fs.readSingle()) || Float.isNaN(ft.readSingle())) {
            fd.writeOnes(4);
        } else if (fs.readSingle() < ft.readSingle() || Objects.equals(fs.readSingle(), ft.readSingle())) {
            fd.writeOnes(4);
        } else {
            fd.writeZeroes(4);
        }
    }

    private void cmpUleD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        if (Double.isNaN(fs.readDouble()) || Double.isNaN(ft.readDouble())) {
            fd.writeOnes(8);
        } else if (fs.readDouble() < ft.readDouble() || Objects.equals(fs.readDouble(), ft.readDouble())) {
            fd.writeOnes(8);
        } else {
            fd.writeZeroes(8);
        }
    }

    private void cmpOrS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        if (Float.isNaN(fs.readSingle()) || Float.isNaN(ft.readSingle())) {
            fd.writeZeroes(4);
        } else {
            fd.writeOnes(4);
        }
    }

    private void cmpOrD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        if (Double.isNaN(fs.readDouble()) || Double.isNaN(ft.readDouble())) {
            fd.writeZeroes(8);
        } else {
            fd.writeOnes(8);
        }
    }


    private void cmpUneS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        if (Objects.equals(fs.readSingle(), ft.readSingle())) {
            fd.writeZeroes(4);
        } else {
            fd.writeOnes(4);
        }
    }

    private void cmpUneD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        if (Objects.equals(fs.readDouble(), ft.readDouble())) {
            fd.writeZeroes(8);
        } else {
            fd.writeOnes(8);
        }
    }

    private void cmpNeS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        if (Float.isNaN(fs.readSingle()) || Float.isNaN(ft.readSingle())) {
            fd.writeZeroes(4);
        } else if (!Objects.equals(fs.readSingle(), ft.readSingle())) {
            fd.writeZeroes(4);
        } else {
            fd.writeOnes(4);
        }
    }

    private void cmpNeD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        if (Double.isNaN(fs.readDouble()) || Double.isNaN(ft.readDouble())) {
            fd.writeZeroes(8);
        } else if (!Objects.equals(fs.readDouble(), ft.readDouble())) {
            fd.writeZeroes(8);
        } else {
            fd.writeOnes(8);
        }
    }

    private void cmpSafS(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        if (Float.isNaN(fs.readSingle()) || Float.isNaN(ft.readSingle())) {
            fd.writeOnes(4);
        } else if (fs.readSingle() < ft.readSingle() || Objects.equals(fs.readSingle(), ft.readSingle())) {
            fd.writeOnes(4);
        } else {
            fd.writeZeroes(4);
        }
    }

    private void cmpSafD(FpuInstruction fpuInstruction) {
        FpuRegisterFileArray.RegisterFile fd = fpuRegisterFileArray.getFile(fpuInstruction.fd);
        FpuRegisterFileArray.RegisterFile fs = fpuRegisterFileArray.getFile(fpuInstruction.fs);
        FpuRegisterFileArray.RegisterFile ft = fpuRegisterFileArray.getFile(fpuInstruction.ft);

        if (Double.isNaN(fs.readDouble()) || Double.isNaN(ft.readDouble())) {
            fd.writeOnes(8);
        } else if (fs.readDouble() < ft.readDouble() || Objects.equals(fs.readDouble(), ft.readDouble())) {
            fd.writeOnes(8);
        } else {
            fd.writeZeroes(8);
        }
    }
}
