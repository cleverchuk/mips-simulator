package com.cleverchuk.mips.simulator.cpu;

import com.cleverchuk.mips.simulator.Processor;
import com.cleverchuk.mips.simulator.SystemServiceProvider;
import com.cleverchuk.mips.simulator.mem.Memory;
import java.util.Map;

public class Cpu implements Processor<CpuInstruction> {
    private Map<Object, Integer> labels;

    private final Memory memory;

    private static final String $ZEROREG = "$zero";

    private final CpuRegisterFile registerFile;

    private int PC;

    private final SystemServiceProvider serviceProvider;

    private final static String FATAL_ERROR_MSG_FMT = "Fatal error! Illegal usage of %s on line: %d";

    public Cpu(Memory memory, SystemServiceProvider serviceProvider) {
        this.registerFile = new CpuRegisterFile();
        this.serviceProvider = serviceProvider;
        this.memory = memory;
    }

    public CpuRegisterFile getRegisterFile() {
        return registerFile;
    }

    public void setLabels(Map<Object, Integer> labels) {
        this.labels = labels;
    }

    public void setStackPointer(int address) {
        registerFile.write("$sp", address);
    }

    public void execute(CpuInstruction cpuInstruction) throws Exception {
        if ($ZEROREG.equals(cpuInstruction.rd)) {
            throw new Exception("Fatal error! $zero cannot be used as destination register");
        }

        // execute the given instruction
        // use switch statement to select the right branch using the opcode
        switch (cpuInstruction.CPUOpcode) {
            case LI:
                li(cpuInstruction);
                break;
            case LA:
                la(cpuInstruction);
                break;
            case ADD:
                add(cpuInstruction);
                break;
            case ADDI:
                addi(cpuInstruction);
                break;
            case SUB:
                sub(cpuInstruction);
                break;
            case ADDIU:
                addiu(cpuInstruction);
                break;
            case ADDU:
                addu(cpuInstruction);
                break;
            case CLO:
                clo(cpuInstruction);
                break;
            case CLZ:
                clz(cpuInstruction);
                break;
            case LUI:
                lui(cpuInstruction);
                break;
            case MOVE:
                move(cpuInstruction);
                break;
            case NEGU:
                negu(cpuInstruction);
                break;
            case SUBU:
                subu(cpuInstruction);
                break;
            case SEB:
                seb(cpuInstruction);
                break;
            case SEH:
                seh(cpuInstruction);
                break;
            /*End Arithmetic*/
            case MUL:
                mul(cpuInstruction);
                break;
            case DIV:
                div(cpuInstruction);
                break;
            case DIVU:
                divu(cpuInstruction);
                break;
            case MADD:
                madd(cpuInstruction);
                break;
            case MSUB:
                msub(cpuInstruction);
                break;
            case MSUBU:
                msubu(cpuInstruction);
                break;
            case MULT:
                mult(cpuInstruction);
                break;
            case MADDU:
                maddu(cpuInstruction);
                break;
            case MULTU:
                multu(cpuInstruction);
                break;
            /*End mult and div*/
            case BNE:
                bne(cpuInstruction);
                break;
            case BEQ:
                beq(cpuInstruction);
                break;
            case JR:
                jr(cpuInstruction);
                break;
            case JAL:
                jal(cpuInstruction);
                break;
            case J:
                jump(cpuInstruction);
                break;
            case B:
                b(cpuInstruction);
                break;
            case BAL:
                bal(cpuInstruction);
                break;
            case BEQZ:
                beqz(cpuInstruction);
                break;
            case BGEZ:
                bgez(cpuInstruction);
                break;
            case BGTZ:
                bgtz(cpuInstruction);
                break;
            case BLEZ:
                blez(cpuInstruction);
                break;
            case BLTZ:
                bltz(cpuInstruction);
                break;
            case BNEZ:
                bnez(cpuInstruction);
                break;
            case JALR:
                jalr(cpuInstruction);
                break;
            case BGEZAL:
                bgezal(cpuInstruction);
                break;
            case BLTZAL:
                bltzal(cpuInstruction);
                break;
            /*End branch*/
            case SLL:
                sll(cpuInstruction);
                break;
            case SRL:
                srl(cpuInstruction);
                break;
            case SLLV:
                sllv(cpuInstruction);
                break;
            case ROTR:
                rotr(cpuInstruction);
                break;
            case SRA:
                sra(cpuInstruction);
                break;
            case SRAV:
                srav(cpuInstruction);
                break;
            case SRLV:
                srlv(cpuInstruction);
                break;
            case ROTRV:
                rotrv(cpuInstruction);
                break;
            /*End shift and rotate*/
            case SLT:
                slt(cpuInstruction);
                break;
            case SLTU:
                sltu(cpuInstruction);
                break;
            case SLTI:
                slti(cpuInstruction);
                break;
            case MOVN:
                movn(cpuInstruction);
                break;
            case MOVZ:
                movz(cpuInstruction);
                break;
            case SLTIU:
                sltiu(cpuInstruction);
                break;
            // End conditional test and move
            case AND:
                and(cpuInstruction);
                break;
            case ANDI:
                andi(cpuInstruction);
                break;
            case NOR:
                nor(cpuInstruction);
                break;
            case NOT:
                not(cpuInstruction);
                break;
            case OR:
                or(cpuInstruction);
                break;
            case ORI:
                ori(cpuInstruction);
                break;
            case XOR:
                xor(cpuInstruction);
                break;
            case XORI:
                xori(cpuInstruction);
                break;
            case EXT:
                ext(cpuInstruction);
                break;
            case INS:
                ins(cpuInstruction);
                break;
            case WSBH:
                wsbh(cpuInstruction);
                break;
            // End logical and bit field operations
            case LW:
                lw(cpuInstruction);
                break;
            case SW:
                sw(cpuInstruction);
                break;
            case LB:
                lb(cpuInstruction);
                break;
            case LH:
                lh(cpuInstruction);
                break;
            case SB:
                sb(cpuInstruction);
                break;
            case SH:
                sh(cpuInstruction);
                break;
            case LBU:
                lbu(cpuInstruction);
                break;
            case LWL:
                lwl(cpuInstruction);
                break;
            case LWR:
                lwr(cpuInstruction);
                break;
            case SWL:
                swl(cpuInstruction);
                break;
            case SWR:
                swr(cpuInstruction);
                break;
            case ULW:
                ulw(cpuInstruction);
                break;
            case USW:
                usw(cpuInstruction);
                break;
            case LHU:
                lhu(cpuInstruction);
                break;
            // END LOAD AND STORE OPERATIONS
            case MFHI:
                mfhi(cpuInstruction);
                break;
            case MFLO:
                mflo(cpuInstruction);
                break;
            case MTHI:
                mthi(cpuInstruction);
                break;
            case MTLO:
                mtlo(cpuInstruction);
                break;
            // END ACCUMULATOR ACCESS OPERATIONS
            case LL:
                ll(cpuInstruction);
                break;
            case SC:
                sc(cpuInstruction);
                break;
            // ATOMIC READ-MODIFY-WRITE OPERATIONS
            case NOP:
                break;
            case SYSCALL:
                syscall();
                break;
            default:
                throw new Exception("Fatal error!");
        }
    }

    private void sw(CpuInstruction cpuInstruction) {
        int baseIndex = registerFile.read(cpuInstruction.rs);
        memory.storeWord(registerFile.read(cpuInstruction.rd), baseIndex + cpuInstruction.offset);
    }

    private void srl(CpuInstruction cpuInstruction) {
        int t1Value = registerFile.read(cpuInstruction.rs);
        registerFile.write(cpuInstruction.rd, t1Value >>> cpuInstruction.immediateValue);
    }

    private void sll(CpuInstruction cpuInstruction) {
        int t1Value = registerFile.read(cpuInstruction.rs);
        registerFile.write(cpuInstruction.rd, t1Value << cpuInstruction.immediateValue);
    }

    private void li(CpuInstruction cpuInstruction) {
        registerFile.write(cpuInstruction.rd, cpuInstruction.immediateValue);
    }

    private void la(CpuInstruction cpuInstruction) throws Exception {
        Integer address = labels.get(cpuInstruction.label);
        if (address == null) {
            throw new Exception(String.format(FATAL_ERROR_MSG_FMT, cpuInstruction.CPUOpcode.name(), cpuInstruction.line));
        }
        registerFile.write(cpuInstruction.rd, address);
    }

    private void lw(CpuInstruction cpuInstruction) throws Exception {
        if (cpuInstruction.rd == null) {
            throw new Exception(String.format(FATAL_ERROR_MSG_FMT, cpuInstruction.CPUOpcode.name(), cpuInstruction.line));
        }
        if (cpuInstruction.rs != null) {
            int baseIndex = registerFile.read(cpuInstruction.rs);
            registerFile.write(cpuInstruction.rd, memory.readWord(baseIndex + cpuInstruction.offset));
        } else {
            throw new Exception(String.format(FATAL_ERROR_MSG_FMT, cpuInstruction.CPUOpcode.name(), cpuInstruction.line));
        }
    }

    private void jal(CpuInstruction cpuInstruction) throws Exception {
        //set PC to the instruction given in the label
        Integer address = labels.get(cpuInstruction.label);
        if (address == null) {
            throw new Exception(String.format(FATAL_ERROR_MSG_FMT, cpuInstruction.CPUOpcode.name(), cpuInstruction.line));
        }
        registerFile.write("$ra", PC + 1);
        PC = address;
    }

    private void beq(CpuInstruction cpuInstruction) throws Exception {
        //if registers rs and rt are equal, then increment PC by immediate value
        int rd = registerFile.read(cpuInstruction.rd), rs = registerFile.read(cpuInstruction.rs);
        if (rs == rd) {
            if (cpuInstruction.label == null) {
                PC += cpuInstruction.immediateValue;
            } else {
                jump(cpuInstruction);
            }
        }
    }

    private void bne(CpuInstruction cpuInstruction) throws Exception {
        //if registers rs and rt are equal, then increment PC by immediate value
        int rd = registerFile.read(cpuInstruction.rd), rs = registerFile.read(cpuInstruction.rs);
        if (rd != rs) {
            if (cpuInstruction.label == null) {
                PC += cpuInstruction.immediateValue;
            } else {
                jump(cpuInstruction);
            }
        }
    }

    private void add(CpuInstruction cpuInstruction) throws Exception {
        int leftOperand = registerFile.read(cpuInstruction.rs),
                rightOperand = registerFile.read(cpuInstruction.rt);
        if (cpuInstruction.rd == null) {
            throw new Exception(String.format(FATAL_ERROR_MSG_FMT, cpuInstruction.CPUOpcode.name(), cpuInstruction.line));
        }
        registerFile.write(cpuInstruction.rd, Math.addExact(leftOperand, rightOperand));
    }

    private void div(CpuInstruction cpuInstruction) {
        int rd = registerFile.read(cpuInstruction.rd),
                rs = registerFile.read(cpuInstruction.rs);
        registerFile.accSetHI(rd % rs);
        registerFile.accSetLO(rd / rs);

    }

    private void mul(CpuInstruction cpuInstruction) throws Exception {
        int leftOperand = registerFile.read(cpuInstruction.rs),
                rightOperand = registerFile.read(cpuInstruction.rt);
        if (cpuInstruction.rd == null) {
            throw new Exception(String.format(FATAL_ERROR_MSG_FMT, cpuInstruction.CPUOpcode.name(), cpuInstruction.line));
        }
        registerFile.write(cpuInstruction.rd, leftOperand * rightOperand);

    }

    private void sub(CpuInstruction cpuInstruction) throws Exception {
        int leftOperand = registerFile.read(cpuInstruction.rs),
                rightOperand = registerFile.read(cpuInstruction.rt);
        if (cpuInstruction.rd == null) {
            throw new Exception(String.format(FATAL_ERROR_MSG_FMT, cpuInstruction.CPUOpcode.name(), cpuInstruction.line));
        }
        registerFile.write(cpuInstruction.rd, Math.subtractExact(leftOperand, rightOperand));
    }

    private void addi(CpuInstruction cpuInstruction) {
        int leftOperand = registerFile.read(cpuInstruction.rs);
        registerFile.write(cpuInstruction.rd, Math.addExact(leftOperand, cpuInstruction.immediateValue));
    }

    private void jr(CpuInstruction cpuInstruction) {
        PC = registerFile.read(cpuInstruction.rd);
    }

    private void jump(CpuInstruction cpuInstruction) throws Exception {
        Integer address = labels.get(cpuInstruction.label);
        if (address == null) {
            throw new Exception(String.format(FATAL_ERROR_MSG_FMT, cpuInstruction.CPUOpcode.name(), cpuInstruction.line));
        }
        PC = address;
    }

    private void syscall() throws Exception {
        int which = registerFile.read("$v0");
        serviceProvider.requestService(which);
    }

    private void addiu(CpuInstruction cpuInstruction) {
        long leftOperand = Long.parseLong(Integer.toBinaryString(registerFile.read(cpuInstruction.rs)), 0x2);
        long rightOperand = Long.parseLong(Integer.toBinaryString(cpuInstruction.immediateValue), 0x2);
        registerFile.write(cpuInstruction.rd, (int) (leftOperand + rightOperand));
    }

    private void addu(CpuInstruction cpuInstruction) throws Exception {
        long rightOperand = Long.parseLong(Integer.toBinaryString(registerFile.read(cpuInstruction.rt)), 0x2),
                leftOperand = Long.parseLong(Integer.toBinaryString(registerFile.read(cpuInstruction.rs)), 0x2);
        if (cpuInstruction.rd == null) {
            throw new Exception(String.format(FATAL_ERROR_MSG_FMT, cpuInstruction.CPUOpcode.name(), cpuInstruction.line));
        }
        registerFile.write(cpuInstruction.rd, (int) (leftOperand + rightOperand));
    }

    private void clo(CpuInstruction cpuInstruction) {
        int value = registerFile.read(cpuInstruction.rs), count = 0;
        String bits = Integer.toBinaryString(value);
        for (int i = 0; i < bits.length(); i++) {
            if (bits.charAt(i) == '0') {
                break;
            }
            count++;
        }
        registerFile.write(cpuInstruction.rd, count);
    }

    private void clz(CpuInstruction cpuInstruction) {
        int value = registerFile.read(cpuInstruction.rs);
        registerFile.write(cpuInstruction.rd, Integer.numberOfLeadingZeros(value));
    }

    private void lui(CpuInstruction cpuInstruction) {
        //the value of the rt is being parsed into Double and saved in rd
        registerFile.write(cpuInstruction.rd, cpuInstruction.immediateValue << 0x10);
    }

    private void move(CpuInstruction cpuInstruction) {
        registerFile.write(cpuInstruction.rd, registerFile.read(cpuInstruction.rs));
    }

    private void negu(CpuInstruction cpuInstruction) {
        registerFile.write(cpuInstruction.rd, -registerFile.read(cpuInstruction.rs));
    }

    private void subu(CpuInstruction cpuInstruction) throws Exception {
        long rightOperand = Long.parseLong(Integer.toBinaryString(registerFile.read(cpuInstruction.rt)), 0x2),
                leftOperand = Long.parseLong(Integer.toBinaryString(registerFile.read(cpuInstruction.rs)), 0x2);

        if (cpuInstruction.rd == null) {
            throw new Exception(String.format(FATAL_ERROR_MSG_FMT, cpuInstruction.CPUOpcode.name(), cpuInstruction.line));
        }
        registerFile.write(cpuInstruction.rd, (int) (leftOperand - rightOperand));
    }

    private void sllv(CpuInstruction cpuInstruction) {
        int shiftAmount = registerFile.read(cpuInstruction.rt) & 0x1f;// uses last 5 bits as the shift amount
        int value = registerFile.read(cpuInstruction.rs);
        int result = value << shiftAmount;
        registerFile.write(cpuInstruction.rd, result);
    }

    private void movn(CpuInstruction cpuInstruction) {
        int rt = registerFile.read(cpuInstruction.rt),
                rs = registerFile.read(cpuInstruction.rs);
        if (rt != 0) {
            registerFile.write(cpuInstruction.rd, rs);
        }
    }

    private void movz(CpuInstruction cpuInstruction) {
        int rt = registerFile.read(cpuInstruction.rt), rs = registerFile.read(cpuInstruction.rs);
        if (rt == 0) {
            registerFile.write(cpuInstruction.rd, rs);
        }
    }

    private void slt(CpuInstruction cpuInstruction) {
        int rt = registerFile.read(cpuInstruction.rt), rs = registerFile.read(cpuInstruction.rs);
        registerFile.write(cpuInstruction.rd, rs < rt ? 1 : 0);
    }

    private void sltu(CpuInstruction cpuInstruction) {
        long rt = Long.parseLong(Integer.toBinaryString(registerFile.read(cpuInstruction.rt)), 0x2),
                rs = Long.parseLong(Integer.toBinaryString(registerFile.read(cpuInstruction.rs)), 0x2);
        registerFile.write(cpuInstruction.rd, rs < rt ? 1 : 0);
    }

    private void slti(CpuInstruction cpuInstruction) {
        int constant = cpuInstruction.immediateValue, rs = registerFile.read(cpuInstruction.rs);
        registerFile.write(cpuInstruction.rd, rs < constant ? 1 : 0);
    }

    private void sltiu(CpuInstruction cpuInstruction) {
        long constant = Long.parseLong(Integer.toBinaryString(cpuInstruction.immediateValue), 0x2),
                rs = Long.parseLong(Integer.toBinaryString(registerFile.read(cpuInstruction.rs)), 0x2);
        registerFile.write(cpuInstruction.rd, rs < constant ? 1 : 0);
    }

    private void and(CpuInstruction cpuInstruction) {
        int rs = registerFile.read(cpuInstruction.rs);
        int rt = registerFile.read(cpuInstruction.rt);

        registerFile.write(cpuInstruction.rd, rs & rt);
    }

    private void andi(CpuInstruction cpuInstruction) {
        int rs = registerFile.read(cpuInstruction.rs);
        int constant = cpuInstruction.immediateValue;

        registerFile.write(cpuInstruction.rd, rs & constant);
    }

    private void nor(CpuInstruction cpuInstruction) {
        int rs = registerFile.read(cpuInstruction.rs);
        int rt = registerFile.read(cpuInstruction.rt);

        registerFile.write(cpuInstruction.rd, ~(rs | rt));
    }

    private void not(CpuInstruction cpuInstruction) {
        int rs = registerFile.read(cpuInstruction.rs);
        registerFile.write(cpuInstruction.rd, ~rs);
    }

    private void or(CpuInstruction cpuInstruction) {
        int rs = registerFile.read(cpuInstruction.rs);
        int rt = registerFile.read(cpuInstruction.rt);

        registerFile.write(cpuInstruction.rd, rs | rt);
    }

    private void ori(CpuInstruction cpuInstruction) {
        int rs = registerFile.read(cpuInstruction.rs);
        int constant = cpuInstruction.immediateValue;

        registerFile.write(cpuInstruction.rd, rs | constant);
    }

    private void xor(CpuInstruction cpuInstruction) {
        int rs = registerFile.read(cpuInstruction.rs);
        int rt = registerFile.read(cpuInstruction.rt);

        registerFile.write(cpuInstruction.rd, rs ^ rt);
    }

    private void xori(CpuInstruction cpuInstruction) {
        int rs = registerFile.read(cpuInstruction.rs);
        int constant = cpuInstruction.immediateValue;

        registerFile.write(cpuInstruction.rd, rs ^ constant);
    }

    private void ext(CpuInstruction cpuInstruction) throws Exception {
        int size = cpuInstruction.size;
        int pos = cpuInstruction.pos;
        validateSizePos(size, pos);

        int rs = registerFile.read(cpuInstruction.rs);
        registerFile.write(cpuInstruction.rd, extractBits(rs, pos, size) >> pos);
    }

    private void ins(CpuInstruction cpuInstruction) throws Exception {

        int size = cpuInstruction.size;
        int pos = cpuInstruction.pos;
        validateSizePos(size, pos);

        int mask0 = 0, mask1 = -1;
        for (int i = 0; i < pos; i++) {
            mask0 = (mask0 | 1) << 1;
        }

        mask0 >>= 1; // undo extra shift
        mask1 <<= (size + pos);


        int rs = registerFile.read(cpuInstruction.rs);
        int extractedBits = extractBits(rs, pos, size);
        int mask = (mask0 | mask1) | extractedBits;

        int rd = registerFile.read(cpuInstruction.rd);
        registerFile.write(cpuInstruction.rd, rd & mask);
    }

    private void lb(CpuInstruction cpuInstruction) {
        int index = registerFile.read(cpuInstruction.rs) + cpuInstruction.offset;
        registerFile.write(cpuInstruction.rd, memory.read(index));
    }

    private void lbu(CpuInstruction cpuInstruction) {
        int index = registerFile.read(cpuInstruction.rs) + cpuInstruction.offset;
        registerFile.write(cpuInstruction.rd, memory.read(index) & 0x00_00_00_ff);
    }


    private void lh(CpuInstruction cpuInstruction) {
        int index = registerFile.read(cpuInstruction.rs) + cpuInstruction.offset;
        registerFile.write(cpuInstruction.rd, memory.readHalf(index));
    }


    private void lhu(CpuInstruction cpuInstruction) {
        int index = registerFile.read(cpuInstruction.rs) + cpuInstruction.offset;
        registerFile.write(cpuInstruction.rd, memory.readHalf(index) & 0x00_00_ff_ff);
    }


    private void lwl(CpuInstruction cpuInstruction) {
        int index = registerFile.read(cpuInstruction.rs) + cpuInstruction.offset;
        registerFile.write(cpuInstruction.rd, memory.readWord(index));
    }


    private void lwr(CpuInstruction cpuInstruction) {
        int index = registerFile.read(cpuInstruction.rs) + cpuInstruction.offset;
        registerFile.write(cpuInstruction.rd, memory.readWord(index));
    }


    private void sb(CpuInstruction cpuInstruction) {
        int index = registerFile.read(cpuInstruction.rs) + cpuInstruction.offset;
        memory.store((byte) registerFile.read(cpuInstruction.rd), index);
    }


    private void sh(CpuInstruction cpuInstruction) {
        int index = registerFile.read(cpuInstruction.rs) + cpuInstruction.offset;
        memory.storeHalf((short) registerFile.read(cpuInstruction.rd), index);
    }


    private void swl(CpuInstruction cpuInstruction) {
        int index = registerFile.read(cpuInstruction.rs) + cpuInstruction.offset;
        memory.storeWord(registerFile.read(cpuInstruction.rd), index);
    }


    private void swr(CpuInstruction cpuInstruction) {
        int index = registerFile.read(cpuInstruction.rs) + cpuInstruction.offset;
        memory.storeWord(registerFile.read(cpuInstruction.rd), index);
    }


    private void ulw(CpuInstruction cpuInstruction) {
        int index = registerFile.read(cpuInstruction.rs) + cpuInstruction.offset;
        registerFile.write(cpuInstruction.rd, memory.readWord(index));
    }

    private void usw(CpuInstruction cpuInstruction) {
        int index = registerFile.read(cpuInstruction.rs) + cpuInstruction.offset;
        memory.storeWord(registerFile.read(cpuInstruction.rd), index);
    }

    private void b(CpuInstruction cpuInstruction) throws Exception {
        if (cpuInstruction.label == null) {
            PC += cpuInstruction.immediateValue;
        } else {
            jump(cpuInstruction);
        }
    }

    private void bal(CpuInstruction cpuInstruction) throws Exception {
        registerFile.write("$ra", PC + 1);
        if (cpuInstruction.label == null) {
            PC += cpuInstruction.immediateValue;
        } else {
            jump(cpuInstruction);
        }
    }

    private void beqz(CpuInstruction cpuInstruction) throws Exception {
        int rs = registerFile.read(cpuInstruction.rd);
        if (rs == 0) {
            if (cpuInstruction.label == null) {
                PC += cpuInstruction.immediateValue;
            } else {
                jump(cpuInstruction);
            }
        }
    }

    private void bgez(CpuInstruction cpuInstruction) throws Exception {
        int rs = registerFile.read(cpuInstruction.rd);
        if (rs >= 0) {
            if (cpuInstruction.label == null) {
                PC += cpuInstruction.immediateValue;
            } else {
                jump(cpuInstruction);
            }
        }
    }

    private void bgezal(CpuInstruction cpuInstruction) throws Exception {
        registerFile.write("$ra", PC + 1);
        int rs = registerFile.read(cpuInstruction.rd);
        if (rs >= 0) {
            if (cpuInstruction.label == null) {
                PC += cpuInstruction.immediateValue;
            } else {
                jump(cpuInstruction);
            }
        }
    }

    private void bgtz(CpuInstruction cpuInstruction) throws Exception {
        int rs = registerFile.read(cpuInstruction.rd);
        if (rs > 0) {
            if (cpuInstruction.label == null) {
                PC += cpuInstruction.immediateValue;
            } else {
                jump(cpuInstruction);
            }
        }
    }

    private void blez(CpuInstruction cpuInstruction) throws Exception {
        int rs = registerFile.read(cpuInstruction.rd);
        if (rs <= 0) {
            if (cpuInstruction.label == null) {
                PC += cpuInstruction.immediateValue;
            } else {
                jump(cpuInstruction);
            }
        }
    }

    private void bltz(CpuInstruction cpuInstruction) throws Exception {
        int rs = registerFile.read(cpuInstruction.rd);
        if (rs < 0) {
            if (cpuInstruction.label == null) {
                PC += cpuInstruction.immediateValue;
            } else {
                jump(cpuInstruction);
            }
        }
    }

    private void bnez(CpuInstruction cpuInstruction) throws Exception {
        int rs = registerFile.read(cpuInstruction.rd);
        if (rs != 0) {
            if (cpuInstruction.label == null) {
                PC += cpuInstruction.immediateValue;
            } else {
                jump(cpuInstruction);
            }
        }
    }

    private void bltzal(CpuInstruction cpuInstruction) throws Exception {
        registerFile.write("$ra", PC + 1);
        int rs = registerFile.read(cpuInstruction.rd);
        if (rs < 0) {
            if (cpuInstruction.label == null) {
                PC += cpuInstruction.immediateValue;
            } else {
                jump(cpuInstruction);
            }
        }

    }

    private void jalr(CpuInstruction cpuInstruction) {
        registerFile.write(cpuInstruction.rd, PC + 1);
        PC = registerFile.read(cpuInstruction.rs);
    }

    private void divu(CpuInstruction cpuInstruction) {
        long rd = Long.parseLong(Integer.toBinaryString(registerFile.read(cpuInstruction.rd)), 0x2);
        long rs = Long.parseLong(Integer.toBinaryString(registerFile.read(cpuInstruction.rs)), 0x2);
        registerFile.accSetHI((int) (rd % rs));
        registerFile.accSetLO((int) (rd / rs));
    }

    private void madd(CpuInstruction cpuInstruction) {
        int rd = registerFile.read(cpuInstruction.rd);
        int rs = registerFile.read(cpuInstruction.rs);
        registerFile.accAdd((long) rd * rs);
    }

    private void maddu(CpuInstruction cpuInstruction) {
        long rd = Long.parseLong(Integer.toBinaryString(registerFile.read(cpuInstruction.rd)), 0x2);
        long rs = Long.parseLong(Integer.toBinaryString(registerFile.read(cpuInstruction.rs)), 0x2);
        registerFile.accAdd(rd * rs);
    }

    private void msub(CpuInstruction cpuInstruction) {
        int rd = registerFile.read(cpuInstruction.rd);
        int rs = registerFile.read(cpuInstruction.rs);
        registerFile.accSub((long) rd * rs);
    }

    private void msubu(CpuInstruction cpuInstruction) {
        long rd = Long.parseLong(Integer.toBinaryString(registerFile.read(cpuInstruction.rd)), 0x2);
        long rs = Long.parseLong(Integer.toBinaryString(registerFile.read(cpuInstruction.rs)), 0x2);
        registerFile.accSub(rd * rs);
    }

    private void mult(CpuInstruction cpuInstruction) {
        int rd = registerFile.read(cpuInstruction.rd);
        int rs = registerFile.read(cpuInstruction.rs);
        registerFile.accSet((long) rd * rs);
    }

    private void multu(CpuInstruction cpuInstruction) {
        long rd = Long.parseLong(Integer.toBinaryString(registerFile.read(cpuInstruction.rd)), 0x2);
        long rs = Long.parseLong(Integer.toBinaryString(registerFile.read(cpuInstruction.rs)), 0x2);
        registerFile.accSet(rd * rs);
    }

    private void mfhi(CpuInstruction cpuInstruction) {
        registerFile.write(cpuInstruction.rd, registerFile.accHI());
    }

    private void mflo(CpuInstruction cpuInstruction) {
        registerFile.write(cpuInstruction.rd, registerFile.accLO());
    }

    private void mthi(CpuInstruction cpuInstruction) {
        registerFile.accSetHI(registerFile.read(cpuInstruction.rd));
    }

    private void mtlo(CpuInstruction cpuInstruction) {
        registerFile.accSetLO(registerFile.read(cpuInstruction.rd));
    }

    private void rotr(CpuInstruction cpuInstruction) {
        int rs = registerFile.read(cpuInstruction.rs);
        int immediateValue = cpuInstruction.immediateValue & 0x1f;

        registerFile.write(cpuInstruction.rd,
                (extractBits(rs, 0x0, immediateValue) << (0x20 - immediateValue)) |
                        (extractBits(rs, immediateValue, 0x20 - immediateValue) >>> immediateValue));
    }

    private void rotrv(CpuInstruction cpuInstruction) {
        int rs = registerFile.read(cpuInstruction.rs);
        int rt = registerFile.read(cpuInstruction.rt) & 0x1f;

        registerFile.write(cpuInstruction.rd,
                (extractBits(rs, 0, rt) << (0x20 - rt)) |
                        (extractBits(rs, rt, 0x20 - rt)) >>> rt);
    }

    private void sra(CpuInstruction cpuInstruction) {
        int rs = registerFile.read(cpuInstruction.rs);
        int immediateValue = cpuInstruction.immediateValue & 0x1f;
        registerFile.write(cpuInstruction.rd, rs >> immediateValue);
    }

    private void srav(CpuInstruction cpuInstruction) {
        int rs = registerFile.read(cpuInstruction.rs);
        int rt = registerFile.read(cpuInstruction.rt) & 0x1f;
        registerFile.write(cpuInstruction.rd, rs >> rt);
    }

    private void srlv(CpuInstruction cpuInstruction) {
        int rs = registerFile.read(cpuInstruction.rs);
        int rt = registerFile.read(cpuInstruction.rt) & 0x1f;
        registerFile.write(cpuInstruction.rd, rs >>> rt);
    }

    private void seb(CpuInstruction cpuInstruction) {
        int rs = registerFile.read(cpuInstruction.rs);
        registerFile.write(cpuInstruction.rd, (rs < 0 ? -1 : 0) & 0xffff_ff00 | extractBits(rs, 0x0, 0x8));
    }

    private void seh(CpuInstruction cpuInstruction) {
        int rs = registerFile.read(cpuInstruction.rs);
        registerFile.write(cpuInstruction.rd, (rs < 0 ? -1 : 0) & 0xffff_ff00 | extractBits(rs, 0x0, 0x10));
    }

    private void wsbh(CpuInstruction cpuInstruction) {
        int rs = registerFile.read(cpuInstruction.rs);
        registerFile.write(cpuInstruction.rd, (extractBits(rs, 0x10, 0x8) << 0x18) | (extractBits(rs, 0x18, 0x8) << 0x10)
                | (extractBits(rs, 0x0, 0x8) << 0x8) | extractBits(rs, 0x8, 0x8));
    }

    private void ll(CpuInstruction cpuInstruction) {
        int base = registerFile.read(cpuInstruction.rs);
        int address = base + cpuInstruction.offset;
        throwIfNotWordAligned(address);
        registerFile.write(cpuInstruction.rd, memory.readWord(address));
    }

    private void sc(CpuInstruction cpuInstruction) {
        int base = registerFile.read(cpuInstruction.rs);
        int address = base + cpuInstruction.offset;
        throwIfNotWordAligned(address);
        memory.storeWord(registerFile.read(cpuInstruction.rd), address);
    }


    /**
     * helper function to extract bit strings from right to left
     *
     * @param source where to extract from
     * @param pos    where to start
     * @param size   number of bits to extract
     * @return extract value as int
     */
    private int extractBits(int source, int pos, int size) {
        int mask = 0;

        for (int i = 0; i < size; i++) {
            mask = (mask | 1) << 1;
        }

        mask >>= 1; // undo extra shift
        mask <<= pos; // align mask with bits to extract
        return (source & mask);
    }

    private void throwIfNotWordAligned(int address) {
        if ((address & 0xffff_fffc) > 0) {
            throw new RuntimeException("Address not word aligned");
        }
    }

    private void validateSizePos(int size, int pos) throws Exception {
        if (size <= 0 || pos < 0) {
            throw new Exception("invalid operand");
        }

        if (size + pos > 32) {
            throw new Exception("size + pos must be <= 32");
        }
    }

    public int getPC() {
        return PC;
    }

    public synchronized void resetPC() {
        PC = 0;
    }

    public synchronized int getNextPC() {
        return PC++;
    }

    public synchronized void incrementPC(int offset) {
        PC += offset;
    }

}
