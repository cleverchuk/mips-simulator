package com.cleverchuk.mips.simulator.cpu;

import android.os.Handler;
import android.util.SparseIntArray;
import com.cleverchuk.mips.communication.OnUserInputListener;
import com.cleverchuk.mips.compiler.MipsCompiler;
import com.cleverchuk.mips.compiler.parser.ErrorRecorder;
import com.cleverchuk.mips.compiler.parser.SymbolTable;
import com.cleverchuk.mips.compiler.parser.SyntaxError;
import com.cleverchuk.mips.simulator.mem.BigEndianMainMemory;
import com.cleverchuk.mips.simulator.mem.Memory;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class MipsSimulator extends Thread implements OnUserInputListener<Integer> {

    private enum State {
        IDLE,
        WAITING,
        RUNNING,
        STEPPING,
        HALTED,
        ERROR,
        STOP,
        PAUSED
    }

    private SparseIntArray breakpoints;

    private int textSegmentOffset; // demarcate text section from data section

    private int PC;

    private int instructionEndPos = 0;

    private Map<Object, Integer> labels;

    private final ArrayList<CpuInstruction> cpuInstructionMemory;

    private Memory mainMemory;

    private static final String $ZEROREG = "$zero";

    private volatile State currentState = State.IDLE;

    private volatile State previousState = State.IDLE;

    private final Handler ioHandler;

    MipsCompiler compiler;

    public final CpuRegisterFileImpl registerFile;

    public MipsSimulator(Handler ioHandler, MipsCompiler compiler) {
        super("MipsSimulatorThread");
        cpuInstructionMemory = new ArrayList<>();
        mainMemory = new BigEndianMainMemory(0x400);

        this.ioHandler = ioHandler;
        this.compiler = compiler;
        registerFile = new CpuRegisterFileImpl();
    }

    public int getPC() {
        return PC;
    }

    public void stepping() {
        if (currentState == State.STEPPING || currentState == State.ERROR) {
            return;
        }
        previousState = currentState;
        currentState = State.STEPPING;
    }

    public void running() {
        if (currentState == State.ERROR) {
            return;
        }

        if (currentState != State.STEPPING) {
            PC = 0;
        }
        previousState = currentState;
        currentState = State.RUNNING;
    }

    public boolean isRunning() {
        return currentState == State.RUNNING;
    }

    public boolean isHalted() {
        return previousState == State.HALTED || currentState == State.HALTED;
    }

    public void shutDown() {
        previousState = currentState;
        currentState = State.STOP;
    }

    public void pause() {
        previousState = currentState;
        currentState = State.PAUSED;
    }

    public void idle() {
        previousState = currentState;
        currentState = State.IDLE;
    }

    private void step() {
        if (currentState == State.STEPPING || currentState == State.RUNNING) {
            try {
                CpuInstruction cpuInstruction = cpuInstructionMemory.get(PC++);
                execute(cpuInstruction);

            } catch (Exception e) {
                previousState = currentState;
                currentState = State.HALTED;
                int computedPC = PC - 1;

                int line = computedPC >= 0 && computedPC < instructionEndPos ? cpuInstructionMemory.get(computedPC).line : -1;
                String error = String.format(Locale.getDefault(), "[line : %d]\nERROR!!\n%s", line, e.getMessage());

                ioHandler.obtainMessage(100, error)
                        .sendToTarget();
                ioHandler.obtainMessage(10)
                        .sendToTarget();
            }
            if (PC >= instructionEndPos) {
                previousState = currentState;
                currentState = State.HALTED;
                ioHandler.obtainMessage(10)
                        .sendToTarget();
            }
        }
    }


    @Override
    public void run() {
        for (; ; ) {

            if (currentState == State.RUNNING && breakpoints != null && breakpoints.get(getLineNumberToExecute()) > 0) {
                previousState = currentState;
                currentState = State.WAITING;
                ioHandler.obtainMessage(101)
                        .sendToTarget();
            }

            if (currentState == State.RUNNING) {
                step();

            } else if (currentState == State.STEPPING) {
                step();
                previousState = currentState;
                currentState = State.WAITING;

                ioHandler.obtainMessage(101)
                        .sendToTarget();

            } else if (currentState == State.STOP) {
                return;

            } else if (currentState == State.WAITING) {
                ioHandler.obtainMessage(102)
                        .sendToTarget();
            }
        }
    }


    private void init(String raw) throws Exception {
        if (compiler.compile(raw) || isPaused() && !ErrorRecorder.hasErrors()) {
            textSegmentOffset = compiler.textSegmentOffset();
            boolean hasTextSectionSpecified = textSegmentOffset == -1; // .text section is missing if this is True

            if (hasTextSectionSpecified) {
                currentState = State.ERROR;
                throw new Exception("Must have .text section");
            }

            cpuInstructionMemory.clear();
            cpuInstructionMemory.addAll(compiler.getTextSegment());
            mainMemory = compiler.getDataSegment();

            labels = SymbolTable.getTable();
            instructionEndPos = cpuInstructionMemory.size();
            registerFile.writeWord("$sp", Math.max((int) (compiler.memBoundary() * 0.5), 0x200)); // initialize stack pointer

            if (PC >= instructionEndPos || isHalted() || isPaused()) {
                idle();
                PC = 0;
            }
        }
    }

    private boolean isPaused() {
        return currentState == State.PAUSED;
    }


    public int getTextSegmentOffset() {
        return textSegmentOffset;
    }

    public int getDataSegmentOffset() {
        return compiler.dataSegmentOffset();
    }

    public int getLineNumberToExecute() {
        if (PC < instructionEndPos) {
            return cpuInstructionMemory.get(PC).line;
        }
        return 0;
    }

    private void execute(CpuInstruction cpuInstruction) throws Exception {
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

    public void loadInstructions(String instructions, SparseIntArray breakpoints) {
        this.breakpoints = breakpoints;
        try {
            init(instructions);
        } catch (SyntaxError syntaxError) {
            previousState = currentState;
            currentState = State.ERROR;
            ioHandler.obtainMessage(100, syntaxError.getMessage())
                    .sendToTarget();

        } catch (Exception e) {
            previousState = currentState;
            currentState = State.ERROR;
            String error =
                    String.format("OoOps! Something went awry: %s\n If you think this is a bug, please report issue: https://github" +
                            ".com/CleverChuk/MipsIde-bug-track\n", e.getLocalizedMessage());

            ioHandler.obtainMessage(100, error)
                    .sendToTarget();
        }
    }

    public void loadInstructions(String instructions, SparseIntArray breakpoints, boolean silent) {
        this.breakpoints = breakpoints;
        try {
            init(instructions);
        } catch (SyntaxError syntaxError) {
            previousState = currentState;
            currentState = State.ERROR;
            if (!silent) {
                ioHandler.obtainMessage(100, syntaxError.getMessage())
                        .sendToTarget();
            }

        } catch (Exception e) {
            previousState = currentState;
            currentState = State.ERROR;
            String error =
                    String.format("OoOps! Something went awry: %s\n If you think this is a bug, please report issue: https://github" +
                            ".com/CleverChuk/MipsIde-bug-track\n", e.getLocalizedMessage());
            if (!silent) {
                ioHandler.obtainMessage(100, error)
                        .sendToTarget();
            }
        }
    }

    private void sw(CpuInstruction cpuInstruction) {
        int baseIndex = registerFile.readWord(cpuInstruction.rs);
        mainMemory.storeWord(registerFile.readWord(cpuInstruction.rd), baseIndex + cpuInstruction.offset);
    }

    private void srl(CpuInstruction cpuInstruction) {
        int t1Value = registerFile.readWord(cpuInstruction.rs);
        registerFile.writeWord(cpuInstruction.rd, t1Value >>> cpuInstruction.immediateValue);
    }

    private void sll(CpuInstruction cpuInstruction) {
        int t1Value = registerFile.readWord(cpuInstruction.rs);
        registerFile.writeWord(cpuInstruction.rd, t1Value << cpuInstruction.immediateValue);
    }

    private void li(CpuInstruction cpuInstruction) {
        registerFile.writeWord(cpuInstruction.rd, cpuInstruction.immediateValue);
    }

    private void la(CpuInstruction cpuInstruction) throws Exception {
        Integer address = labels.get(cpuInstruction.label);
        if (address == null) {
            throw new Exception(String.format("Fatal error! Illegal usage of %s on line: %d", cpuInstruction.CPUOpcode.name(), cpuInstruction.line));
        }
        registerFile.writeWord(cpuInstruction.rd, address);
    }

    private void lw(CpuInstruction cpuInstruction) throws Exception {
        if (cpuInstruction.rd == null) {
            throw new Exception(String.format("Fatal error! Illegal usage of %s on line: %d", cpuInstruction.CPUOpcode.name(), cpuInstruction.line));
        }
        if (cpuInstruction.rs != null) {
            int baseIndex = registerFile.readWord(cpuInstruction.rs);
            registerFile.writeWord(cpuInstruction.rd, mainMemory.readWord(baseIndex + cpuInstruction.offset));
        } else {
            throw new Exception(String.format("Fatal error! Illegal usage of %s on line: %d", cpuInstruction.CPUOpcode.name(), cpuInstruction.line));
        }
    }

    private void jal(CpuInstruction cpuInstruction) throws Exception {
        //set PC to the instruction given in the label
        Integer address = labels.get(cpuInstruction.label);
        if (address == null) {
            throw new Exception(String.format("Fatal error! Illegal usage of %s on line: %d", cpuInstruction.CPUOpcode.name(), cpuInstruction.line));
        }
        registerFile.writeWord("$ra", PC + 1);
        PC = address;
    }

    private void beq(CpuInstruction cpuInstruction) throws Exception {
        //if registers rs and rt are equal, then increment PC by immediate value
        int rd = registerFile.readWord(cpuInstruction.rd), rs = registerFile.readWord(cpuInstruction.rs);
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
        int rd = registerFile.readWord(cpuInstruction.rd), rs = registerFile.readWord(cpuInstruction.rs);
        if (rd != rs) {
            if (cpuInstruction.label == null) {
                PC += cpuInstruction.immediateValue;
            } else {
                jump(cpuInstruction);
            }
        }
    }

    private void add(CpuInstruction cpuInstruction) throws Exception {
        int leftOperand = registerFile.readWord(cpuInstruction.rs),
                rightOperand = registerFile.readWord(cpuInstruction.rt);
        if (cpuInstruction.rd == null) {
            throw new Exception(String.format("Fatal error! Illegal usage of %s on line: %d", cpuInstruction.CPUOpcode.name(), cpuInstruction.line));
        }
        registerFile.writeWord(cpuInstruction.rd, Math.addExact(leftOperand, rightOperand));
    }

    private void div(CpuInstruction cpuInstruction) {
        int rd = registerFile.readWord(cpuInstruction.rd),
                rs = registerFile.readWord(cpuInstruction.rs);
        registerFile.accSetHI(rd % rs);
        registerFile.accSetLO(rd / rs);

    }

    private void mul(CpuInstruction cpuInstruction) throws Exception {
        int leftOperand = registerFile.readWord(cpuInstruction.rs),
                rightOperand = registerFile.readWord(cpuInstruction.rt);
        if (cpuInstruction.rd == null) {
            throw new Exception(String.format("Fatal error! Illegal usage of %s on line: %d", cpuInstruction.CPUOpcode.name(), cpuInstruction.line));
        }
        registerFile.writeWord(cpuInstruction.rd, leftOperand * rightOperand);

    }

    private void sub(CpuInstruction cpuInstruction) throws Exception {
        int leftOperand = registerFile.readWord(cpuInstruction.rs),
                rightOperand = registerFile.readWord(cpuInstruction.rt);
        if (cpuInstruction.rd == null) {
            throw new Exception(String.format("Fatal error! Illegal usage of %s on line: %d", cpuInstruction.CPUOpcode.name(), cpuInstruction.line));
        }
        registerFile.writeWord(cpuInstruction.rd, Math.subtractExact(leftOperand, rightOperand));
    }

    private void addi(CpuInstruction cpuInstruction) {
        int leftOperand = registerFile.readWord(cpuInstruction.rs);
        registerFile.writeWord(cpuInstruction.rd, Math.addExact(leftOperand, cpuInstruction.immediateValue));
    }

    private void jr(CpuInstruction cpuInstruction) {
        PC = registerFile.readWord(cpuInstruction.rd);
    }

    private void jump(CpuInstruction cpuInstruction) throws Exception {
        Integer address = labels.get(cpuInstruction.label);
        if (address == null) {
            throw new Exception(String.format("Fatal error! Illegal usage of %s on line: %d", cpuInstruction.CPUOpcode.name(), cpuInstruction.line));
        }
        PC = address;
    }

    private void syscall() throws Exception {
        int which = registerFile.readWord("$v0");
        switch (which) {
            case 1:
                //Print Int
                ioHandler.obtainMessage(1, registerFile.readWord("$a0"))
                        .sendToTarget();
                break;

            case 4: {
                //Print String
                int arg = registerFile.readWord("$a0"), c;
                StringBuilder builder = new StringBuilder();
                while ((c = mainMemory.read(arg++)) != 0) {
                    builder.append((char) c);
                }

                ioHandler.obtainMessage(4, builder.toString())
                        .sendToTarget();
                break;
            }

            case 11:
                //Print Char
                int arg = registerFile.readWord("$a0");
                ioHandler.obtainMessage(11, (char) (arg))
                        .sendToTarget();
                break;

            case 10:
                previousState = currentState;
                currentState = State.HALTED;
                ioHandler.obtainMessage(10)
                        .sendToTarget();

                PC = 0;
                break;

            case 5: // Read int
                previousState = currentState;
                currentState = State.WAITING;
                ioHandler.obtainMessage(5)
                        .sendToTarget();

                break;

            case 12: //Read Char
                previousState = currentState;
                currentState = State.WAITING;
                ioHandler.obtainMessage(12)
                        .sendToTarget();
                break;

            default:
                throw new Exception(String.format(Locale.getDefault(), "Service %d not supported!", which));
        }
    }

    private void addiu(CpuInstruction cpuInstruction) {
        long leftOperand = Long.parseLong(Integer.toBinaryString(registerFile.readWord(cpuInstruction.rs)), 0x2);
        long rightOperand = Long.parseLong(Integer.toBinaryString(cpuInstruction.immediateValue), 0x2);
        registerFile.writeWord(cpuInstruction.rd, (int) (leftOperand + rightOperand));
    }

    private void addu(CpuInstruction cpuInstruction) throws Exception {
        long rightOperand = Long.parseLong(Integer.toBinaryString(registerFile.readWord(cpuInstruction.rt)), 0x2),
                leftOperand = Long.parseLong(Integer.toBinaryString(registerFile.readWord(cpuInstruction.rs)), 0x2);
        if (cpuInstruction.rd == null) {
            throw new Exception(String.format("Fatal error! Illegal usage of %s on line: %d", cpuInstruction.CPUOpcode.name(), cpuInstruction.line));
        }
        registerFile.writeWord(cpuInstruction.rd, (int) (leftOperand + rightOperand));
    }

    private void clo(CpuInstruction cpuInstruction) {
        int value = registerFile.readWord(cpuInstruction.rs), count = 0;
        String bits = Integer.toBinaryString(value);
        for (int i = 0; i < bits.length(); i++) {
            if (bits.charAt(i) == '0') {
                break;
            }
            count++;
        }
        registerFile.writeWord(cpuInstruction.rd, count);
    }

    private void clz(CpuInstruction cpuInstruction) {
        int value = registerFile.readWord(cpuInstruction.rs);
        registerFile.writeWord(cpuInstruction.rd, Integer.numberOfLeadingZeros(value));
    }

    private void lui(CpuInstruction cpuInstruction) {
        //the value of the rt is being parsed into Double and saved in rd
        registerFile.writeWord(cpuInstruction.rd, cpuInstruction.immediateValue << 0x10);
    }

    private void move(CpuInstruction cpuInstruction) {
        registerFile.writeWord(cpuInstruction.rd, registerFile.readWord(cpuInstruction.rs));
    }

    private void negu(CpuInstruction cpuInstruction) {
        registerFile.writeWord(cpuInstruction.rd, -registerFile.readWord(cpuInstruction.rs));
    }

    private void subu(CpuInstruction cpuInstruction) throws Exception {
        long rightOperand = Long.parseLong(Integer.toBinaryString(registerFile.readWord(cpuInstruction.rt)), 0x2),
                leftOperand = Long.parseLong(Integer.toBinaryString(registerFile.readWord(cpuInstruction.rs)), 0x2);

        if (cpuInstruction.rd == null) {
            throw new Exception(String.format("Fatal error! Illegal usage of %s on line: %d", cpuInstruction.CPUOpcode.name(), cpuInstruction.line));
        }
        registerFile.writeWord(cpuInstruction.rd, (int) (leftOperand - rightOperand));
    }

    private void sllv(CpuInstruction cpuInstruction) {
        int shiftAmount = registerFile.readWord(cpuInstruction.rt) & 0x1f;// uses last 5 bits as the shift amount
        int value = registerFile.readWord(cpuInstruction.rs);
        int result = value << shiftAmount;
        registerFile.writeWord(cpuInstruction.rd, result);
    }

    private void movn(CpuInstruction cpuInstruction) {
        int rt = registerFile.readWord(cpuInstruction.rt),
                rs = registerFile.readWord(cpuInstruction.rs);
        if (rt != 0) {
            registerFile.writeWord(cpuInstruction.rd, rs);
        }
    }

    private void movz(CpuInstruction cpuInstruction) {
        int rt = registerFile.readWord(cpuInstruction.rt), rs = registerFile.readWord(cpuInstruction.rs);
        if (rt == 0) {
            registerFile.writeWord(cpuInstruction.rd, rs);
        }
    }

    private void slt(CpuInstruction cpuInstruction) {
        int rt = registerFile.readWord(cpuInstruction.rt), rs = registerFile.readWord(cpuInstruction.rs);
        registerFile.writeWord(cpuInstruction.rd, rs < rt ? 1 : 0);
    }

    private void sltu(CpuInstruction cpuInstruction) {
        long rt = Long.parseLong(Integer.toBinaryString(registerFile.readWord(cpuInstruction.rt)), 0x2),
                rs = Long.parseLong(Integer.toBinaryString(registerFile.readWord(cpuInstruction.rs)), 0x2);
        registerFile.writeWord(cpuInstruction.rd, rs < rt ? 1 : 0);
    }

    private void slti(CpuInstruction cpuInstruction) {
        int constant = cpuInstruction.immediateValue, rs = registerFile.readWord(cpuInstruction.rs);
        registerFile.writeWord(cpuInstruction.rd, rs < constant ? 1 : 0);
    }

    private void sltiu(CpuInstruction cpuInstruction) {
        long constant = Long.parseLong(Integer.toBinaryString(cpuInstruction.immediateValue), 0x2),
                rs = Long.parseLong(Integer.toBinaryString(registerFile.readWord(cpuInstruction.rs)), 0x2);
        registerFile.writeWord(cpuInstruction.rd, rs < constant ? 1 : 0);
    }

    private void and(CpuInstruction cpuInstruction) {
        int rs = registerFile.readWord(cpuInstruction.rs);
        int rt = registerFile.readWord(cpuInstruction.rt);

        registerFile.writeWord(cpuInstruction.rd, rs & rt);
    }

    private void andi(CpuInstruction cpuInstruction) {
        int rs = registerFile.readWord(cpuInstruction.rs);
        int constant = cpuInstruction.immediateValue;

        registerFile.writeWord(cpuInstruction.rd, rs & constant);
    }

    private void nor(CpuInstruction cpuInstruction) {
        int rs = registerFile.readWord(cpuInstruction.rs);
        int rt = registerFile.readWord(cpuInstruction.rt);

        registerFile.writeWord(cpuInstruction.rd, ~(rs | rt));
    }

    private void not(CpuInstruction cpuInstruction) {
        int rs = registerFile.readWord(cpuInstruction.rs);
        registerFile.writeWord(cpuInstruction.rd, ~rs);
    }

    private void or(CpuInstruction cpuInstruction) {
        int rs = registerFile.readWord(cpuInstruction.rs);
        int rt = registerFile.readWord(cpuInstruction.rt);

        registerFile.writeWord(cpuInstruction.rd, rs | rt);
    }

    private void ori(CpuInstruction cpuInstruction) {
        int rs = registerFile.readWord(cpuInstruction.rs);
        int constant = cpuInstruction.immediateValue;

        registerFile.writeWord(cpuInstruction.rd, rs | constant);
    }

    private void xor(CpuInstruction cpuInstruction) {
        int rs = registerFile.readWord(cpuInstruction.rs);
        int rt = registerFile.readWord(cpuInstruction.rt);

        registerFile.writeWord(cpuInstruction.rd, rs ^ rt);
    }

    private void xori(CpuInstruction cpuInstruction) {
        int rs = registerFile.readWord(cpuInstruction.rs);
        int constant = cpuInstruction.immediateValue;

        registerFile.writeWord(cpuInstruction.rd, rs ^ constant);
    }

    private void ext(CpuInstruction cpuInstruction) throws Exception {
        int size = cpuInstruction.size;
        int pos = cpuInstruction.pos;
        validateSizePos(size, pos);

        int rs = registerFile.readWord(cpuInstruction.rs);
        registerFile.writeWord(cpuInstruction.rd, extractBits(rs, pos, size) >> pos);
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


        int rs = registerFile.readWord(cpuInstruction.rs);
        int extractedBits = extractBits(rs, pos, size);
        int mask = (mask0 | mask1) | extractedBits;

        int rd = registerFile.readWord(cpuInstruction.rd);
        registerFile.writeWord(cpuInstruction.rd, rd & mask);
    }

    private void lb(CpuInstruction cpuInstruction) {
        int index = registerFile.readWord(cpuInstruction.rs) + cpuInstruction.offset;
        registerFile.writeWord(cpuInstruction.rd, mainMemory.read(index));
    }

    private void lbu(CpuInstruction cpuInstruction) {
        int index = registerFile.readWord(cpuInstruction.rs) + cpuInstruction.offset;
        registerFile.writeWord(cpuInstruction.rd, mainMemory.read(index) & 0x00_00_00_ff);
    }


    private void lh(CpuInstruction cpuInstruction) {
        int index = registerFile.readWord(cpuInstruction.rs) + cpuInstruction.offset;
        registerFile.writeWord(cpuInstruction.rd, mainMemory.readHalf(index));
    }


    private void lhu(CpuInstruction cpuInstruction) {
        int index = registerFile.readWord(cpuInstruction.rs) + cpuInstruction.offset;
        registerFile.writeWord(cpuInstruction.rd, mainMemory.readHalf(index) & 0x00_00_ff_ff);
    }


    private void lwl(CpuInstruction cpuInstruction) {
        int index = registerFile.readWord(cpuInstruction.rs) + cpuInstruction.offset;
        registerFile.writeWord(cpuInstruction.rd, mainMemory.readWord(index));
    }


    private void lwr(CpuInstruction cpuInstruction) {
        int index = registerFile.readWord(cpuInstruction.rs) + cpuInstruction.offset;
        registerFile.writeWord(cpuInstruction.rd, mainMemory.readWord(index));
    }


    private void sb(CpuInstruction cpuInstruction) {
        int index = registerFile.readWord(cpuInstruction.rs) + cpuInstruction.offset;
        mainMemory.store((byte) registerFile.readWord(cpuInstruction.rd), index);
    }


    private void sh(CpuInstruction cpuInstruction) {
        int index = registerFile.readWord(cpuInstruction.rs) + cpuInstruction.offset;
        mainMemory.storeHalf((short) registerFile.readWord(cpuInstruction.rd), index);
    }


    private void swl(CpuInstruction cpuInstruction) {
        int index = registerFile.readWord(cpuInstruction.rs) + cpuInstruction.offset;
        mainMemory.storeWord(registerFile.readWord(cpuInstruction.rd), index);
    }


    private void swr(CpuInstruction cpuInstruction) {
        int index = registerFile.readWord(cpuInstruction.rs) + cpuInstruction.offset;
        mainMemory.storeWord(registerFile.readWord(cpuInstruction.rd), index);
    }


    private void ulw(CpuInstruction cpuInstruction) {
        int index = registerFile.readWord(cpuInstruction.rs) + cpuInstruction.offset;
        registerFile.writeWord(cpuInstruction.rd, mainMemory.readWord(index));
    }

    private void usw(CpuInstruction cpuInstruction) {
        int index = registerFile.readWord(cpuInstruction.rs) + cpuInstruction.offset;
        mainMemory.storeWord(registerFile.readWord(cpuInstruction.rd), index);
    }

    private void b(CpuInstruction cpuInstruction) throws Exception {
        if (cpuInstruction.label == null) {
            PC += cpuInstruction.immediateValue;
        } else {
            jump(cpuInstruction);
        }
    }

    private void bal(CpuInstruction cpuInstruction) throws Exception {
        registerFile.writeWord("$ra", PC + 1);
        if (cpuInstruction.label == null) {
            PC += cpuInstruction.immediateValue;
        } else {
            jump(cpuInstruction);
        }
    }

    private void beqz(CpuInstruction cpuInstruction) throws Exception {
        int rs = registerFile.readWord(cpuInstruction.rd);
        if (rs == 0) {
            if (cpuInstruction.label == null) {
                PC += cpuInstruction.immediateValue;
            } else {
                jump(cpuInstruction);
            }
        }
    }

    private void bgez(CpuInstruction cpuInstruction) throws Exception {
        int rs = registerFile.readWord(cpuInstruction.rd);
        if (rs >= 0) {
            if (cpuInstruction.label == null) {
                PC += cpuInstruction.immediateValue;
            } else {
                jump(cpuInstruction);
            }
        }
    }

    private void bgezal(CpuInstruction cpuInstruction) throws Exception {
        registerFile.writeWord("$ra", PC + 1);
        int rs = registerFile.readWord(cpuInstruction.rd);
        if (rs >= 0) {
            if (cpuInstruction.label == null) {
                PC += cpuInstruction.immediateValue;
            } else {
                jump(cpuInstruction);
            }
        }
    }

    private void bgtz(CpuInstruction cpuInstruction) throws Exception {
        int rs = registerFile.readWord(cpuInstruction.rd);
        if (rs > 0) {
            if (cpuInstruction.label == null) {
                PC += cpuInstruction.immediateValue;
            } else {
                jump(cpuInstruction);
            }
        }
    }

    private void blez(CpuInstruction cpuInstruction) throws Exception {
        int rs = registerFile.readWord(cpuInstruction.rd);
        if (rs <= 0) {
            if (cpuInstruction.label == null) {
                PC += cpuInstruction.immediateValue;
            } else {
                jump(cpuInstruction);
            }
        }
    }

    private void bltz(CpuInstruction cpuInstruction) throws Exception {
        int rs = registerFile.readWord(cpuInstruction.rd);
        if (rs < 0) {
            if (cpuInstruction.label == null) {
                PC += cpuInstruction.immediateValue;
            } else {
                jump(cpuInstruction);
            }
        }
    }

    private void bnez(CpuInstruction cpuInstruction) throws Exception {
        int rs = registerFile.readWord(cpuInstruction.rd);
        if (rs != 0) {
            if (cpuInstruction.label == null) {
                PC += cpuInstruction.immediateValue;
            } else {
                jump(cpuInstruction);
            }
        }
    }

    private void bltzal(CpuInstruction cpuInstruction) throws Exception {
        registerFile.writeWord("$ra", PC + 1);
        int rs = registerFile.readWord(cpuInstruction.rd);
        if (rs < 0) {
            if (cpuInstruction.label == null) {
                PC += cpuInstruction.immediateValue;
            } else {
                jump(cpuInstruction);
            }
        }

    }

    private void jalr(CpuInstruction cpuInstruction) {
        registerFile.writeWord(cpuInstruction.rd, PC + 1);
        PC = registerFile.readWord(cpuInstruction.rs);
    }

    private void divu(CpuInstruction cpuInstruction) {
        long rd = Long.parseLong(Integer.toBinaryString(registerFile.readWord(cpuInstruction.rd)), 0x2);
        long rs = Long.parseLong(Integer.toBinaryString(registerFile.readWord(cpuInstruction.rs)), 0x2);
        registerFile.accSetHI((int) (rd % rs));
        registerFile.accSetLO((int) (rd / rs));
    }

    private void madd(CpuInstruction cpuInstruction) {
        int rd = registerFile.readWord(cpuInstruction.rd);
        int rs = registerFile.readWord(cpuInstruction.rs);
        registerFile.accAdd((long) rd * rs);
    }

    private void maddu(CpuInstruction cpuInstruction) {
        long rd = Long.parseLong(Integer.toBinaryString(registerFile.readWord(cpuInstruction.rd)), 0x2);
        long rs = Long.parseLong(Integer.toBinaryString(registerFile.readWord(cpuInstruction.rs)), 0x2);
        registerFile.accAdd(rd * rs);
    }

    private void msub(CpuInstruction cpuInstruction) {
        int rd = registerFile.readWord(cpuInstruction.rd);
        int rs = registerFile.readWord(cpuInstruction.rs);
        registerFile.accSub((long) rd * rs);
    }

    private void msubu(CpuInstruction cpuInstruction) {
        long rd = Long.parseLong(Integer.toBinaryString(registerFile.readWord(cpuInstruction.rd)), 0x2);
        long rs = Long.parseLong(Integer.toBinaryString(registerFile.readWord(cpuInstruction.rs)), 0x2);
        registerFile.accSub(rd * rs);
    }

    private void mult(CpuInstruction cpuInstruction) {
        int rd = registerFile.readWord(cpuInstruction.rd);
        int rs = registerFile.readWord(cpuInstruction.rs);
        registerFile.accSet((long) rd * rs);
    }

    private void multu(CpuInstruction cpuInstruction) {
        long rd = Long.parseLong(Integer.toBinaryString(registerFile.readWord(cpuInstruction.rd)), 0x2);
        long rs = Long.parseLong(Integer.toBinaryString(registerFile.readWord(cpuInstruction.rs)), 0x2);
        registerFile.accSet(rd * rs);
    }

    private void mfhi(CpuInstruction cpuInstruction) {
        registerFile.writeWord(cpuInstruction.rd, registerFile.accHI());
    }

    private void mflo(CpuInstruction cpuInstruction) {
        registerFile.writeWord(cpuInstruction.rd, registerFile.accLO());
    }

    private void mthi(CpuInstruction cpuInstruction) {
        registerFile.accSetHI(registerFile.readWord(cpuInstruction.rd));
    }

    private void mtlo(CpuInstruction cpuInstruction) {
        registerFile.accSetLO(registerFile.readWord(cpuInstruction.rd));
    }

    private void rotr(CpuInstruction cpuInstruction) {
        int rs = registerFile.readWord(cpuInstruction.rs);
        int immediateValue = cpuInstruction.immediateValue & 0x1f;

        registerFile.writeWord(cpuInstruction.rd,
                (extractBits(rs, 0x0, immediateValue) << (0x20 - immediateValue)) |
                        (extractBits(rs, immediateValue, 0x20 - immediateValue) >>> immediateValue));
    }

    private void rotrv(CpuInstruction cpuInstruction) {
        int rs = registerFile.readWord(cpuInstruction.rs);
        int rt = registerFile.readWord(cpuInstruction.rt) & 0x1f;

        registerFile.writeWord(cpuInstruction.rd,
                (extractBits(rs, 0, rt) << (0x20 - rt)) |
                        (extractBits(rs, rt, 0x20 - rt)) >>> rt);
    }

    private void sra(CpuInstruction cpuInstruction) {
        int rs = registerFile.readWord(cpuInstruction.rs);
        int immediateValue = cpuInstruction.immediateValue & 0x1f;
        registerFile.writeWord(cpuInstruction.rd, rs >> immediateValue);
    }

    private void srav(CpuInstruction cpuInstruction) {
        int rs = registerFile.readWord(cpuInstruction.rs);
        int rt = registerFile.readWord(cpuInstruction.rt) & 0x1f;
        registerFile.writeWord(cpuInstruction.rd, rs >> rt);
    }

    private void srlv(CpuInstruction cpuInstruction) {
        int rs = registerFile.readWord(cpuInstruction.rs);
        int rt = registerFile.readWord(cpuInstruction.rt) & 0x1f;
        registerFile.writeWord(cpuInstruction.rd, rs >>> rt);
    }

    private void seb(CpuInstruction cpuInstruction) {
        int rs = registerFile.readWord(cpuInstruction.rs);
        registerFile.writeWord(cpuInstruction.rd, (rs < 0 ? -1 : 0) & 0xffff_ff00 | extractBits(rs, 0x0, 0x8));
    }

    private void seh(CpuInstruction cpuInstruction) {
        int rs = registerFile.readWord(cpuInstruction.rs);
        registerFile.writeWord(cpuInstruction.rd, (rs < 0 ? -1 : 0) & 0xffff_ff00 | extractBits(rs, 0x0, 0x10));
    }

    private void wsbh(CpuInstruction cpuInstruction) {
        int rs = registerFile.readWord(cpuInstruction.rs);
        registerFile.writeWord(cpuInstruction.rd, (extractBits(rs, 0x10, 0x8) << 0x18) | (extractBits(rs, 0x18, 0x8) << 0x10)
                | (extractBits(rs, 0x0, 0x8) << 0x8) | extractBits(rs, 0x8, 0x8));
    }

    private void ll(CpuInstruction cpuInstruction) {
        int base = registerFile.readWord(cpuInstruction.rs);
        int address = base + cpuInstruction.offset;
        throwIfNotWordAligned(address);
        registerFile.writeWord(cpuInstruction.rd, mainMemory.readWord(address));
    }

    private void sc(CpuInstruction cpuInstruction) {
        int base = registerFile.readWord(cpuInstruction.rs);
        int address = base + cpuInstruction.offset;
        throwIfNotWordAligned(address);
        mainMemory.storeWord(registerFile.readWord(cpuInstruction.rd), address);
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

    @Override
    public void onInputComplete(Integer data) {
        registerFile.writeWord("$v0", data);
        if (PC >= instructionEndPos) {
            currentState = State.HALTED;
        }

        if (previousState == State.RUNNING) {
            previousState = currentState;
            currentState = State.RUNNING;
        }

        if (previousState == State.STEPPING) {
            previousState = currentState;
            currentState = State.STEPPING;
        }
    }
}
