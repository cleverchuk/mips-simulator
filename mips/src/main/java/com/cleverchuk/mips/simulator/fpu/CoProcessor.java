package com.cleverchuk.mips.simulator.fpu;

import com.cleverchuk.mips.simulator.cpu.CpuRegisterFile;
import com.cleverchuk.mips.simulator.mem.Memory;
import java.util.function.Supplier;

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
                break;
            case ABS_D:
                break;
            case ADD_S:
                break;
            case ADD_D:
                break;
            case CMP_COND_S:
                break;
            case CMP_COND_D:
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
}
