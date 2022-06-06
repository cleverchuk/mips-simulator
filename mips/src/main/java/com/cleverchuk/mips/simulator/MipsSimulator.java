package com.cleverchuk.mips.simulator;

import android.os.Handler;
import android.util.SparseIntArray;
import com.cleverchuk.mips.communication.OnUserInputListener;
import com.cleverchuk.mips.compiler.MipsCompiler;
import com.cleverchuk.mips.compiler.parser.ErrorRecorder;
import com.cleverchuk.mips.compiler.parser.SymbolTable;
import com.cleverchuk.mips.compiler.parser.SyntaxError;
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

    private final ArrayList<Instruction> instructionMemory;

    private Memory mainMemory;

    private static final String $ZEROREG = "$zero";

    private volatile State currentState = State.IDLE;

    private volatile State previousState = State.IDLE;

    private final Handler ioHandler;

    MipsCompiler compiler;

    public final BigEndianRegisterFile registerFile;

    public MipsSimulator(Handler ioHandler, MipsCompiler compiler) {
        super("MipsEmulatorThread");
        instructionMemory = new ArrayList<>();
        mainMemory = new BigEndianMainMemory(0x400);

        this.ioHandler = ioHandler;
        this.compiler = compiler;
        registerFile = new BigEndianRegisterFile();
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

    public void pause(){
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
                Instruction instruction = instructionMemory.get(PC++);
                execute(instruction);

            } catch (Exception e) {
                previousState = currentState;
                currentState = State.HALTED;

                String error = String.format(Locale.getDefault(), "[line : %d]\nERROR!!\n%s",
                        instructionMemory.get(PC - 1).line, e.getMessage());

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

            instructionMemory.clear();
            instructionMemory.addAll(compiler.getTextSegment());
            mainMemory = compiler.getDataSegment();

            labels = SymbolTable.getTable();
            instructionEndPos = instructionMemory.size();
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
            return instructionMemory.get(PC).line;
        }
        return 0;
    }

    private void execute(Instruction instruction) throws Exception {
        if ($ZEROREG.equals(instruction.rd)) {
            throw new Exception("Fatal error! $zero cannot be used as destination register");
        }

        // execute the given instruction
        // use switch statement to select the right branch using the opcode
        switch (instruction.opcode) {
            case LI:
                li(instruction);
                break;
            case LA:
                la(instruction);
                break;
            case ADD:
                add(instruction);
                break;
            case ADDI:
                addi(instruction);
                break;
            case SUB:
                sub(instruction);
                break;
            case ADDIU:
                addiu(instruction);
                break;
            case ADDU:
                addu(instruction);
                break;
            case CLO:
                clo(instruction);
                break;
            case CLZ:
                clz(instruction);
                break;
            case LUI:
                lui(instruction);
                break;
            case MOVE:
                move(instruction);
                break;
            case NEGU:
                negu(instruction);
                break;
            case SUBU:
                subu(instruction);
                break;
            case SEB:
                seb(instruction);
                break;
            case SEH:
                seh(instruction);
                break;
            /*End Arithmetic*/
            case MUL:
                mul(instruction);
                break;
            case DIV:
                div(instruction);
                break;
            case DIVU:
                divu(instruction);
                break;
            case MADD:
                madd(instruction);
                break;
            case MSUB:
                msub(instruction);
                break;
            case MSUBU:
                msubu(instruction);
                break;
            case MULT:
                mult(instruction);
                break;
            case MADDU:
                maddu(instruction);
                break;
            case MULTU:
                multu(instruction);
                break;
            /*End mult and div*/
            case BNE:
                bne(instruction);
                break;
            case BEQ:
                beq(instruction);
                break;
            case JR:
                jr(instruction);
                break;
            case JAL:
                jal(instruction);
                break;
            case J:
                jump(instruction);
                break;
            case B:
                b(instruction);
                break;
            case BAL:
                bal(instruction);
                break;
            case BEQZ:
                beqz(instruction);
                break;
            case BGEZ:
                bgez(instruction);
                break;
            case BGTZ:
                bgtz(instruction);
                break;
            case BLEZ:
                blez(instruction);
                break;
            case BLTZ:
                bltz(instruction);
                break;
            case BNEZ:
                bnez(instruction);
                break;
            case JALR:
                jalr(instruction);
                break;
            case BGEZAL:
                bgezal(instruction);
                break;
            case BLTZAL:
                bltzal(instruction);
                break;
            /*End branch*/
            case SLL:
                sll(instruction);
                break;
            case SRL:
                srl(instruction);
                break;
            case SLLV:
                sllv(instruction);
                break;
            case ROTR:
                rotr(instruction);
                break;
            case SRA:
                sra(instruction);
                break;
            case SRAV:
                srav(instruction);
                break;
            case SRLV:
                srlv(instruction);
                break;
            case ROTRV:
                rotrv(instruction);
                break;
            /*End shift and rotate*/
            case SLT:
                slt(instruction);
                break;
            case SLTU:
                sltu(instruction);
                break;
            case SLTI:
                slti(instruction);
                break;
            case MOVN:
                movn(instruction);
                break;
            case MOVZ:
                movz(instruction);
                break;
            case SLTIU:
                sltiu(instruction);
                break;
            // End conditional test and move
            case AND:
                and(instruction);
                break;
            case ANDI:
                andi(instruction);
                break;
            case NOR:
                nor(instruction);
                break;
            case NOT:
                not(instruction);
                break;
            case OR:
                or(instruction);
                break;
            case ORI:
                ori(instruction);
                break;
            case XOR:
                xor(instruction);
                break;
            case XORI:
                xori(instruction);
                break;
            case EXT:
                ext(instruction);
                break;
            case INS:
                ins(instruction);
                break;
            case WSBH:
                wsbh(instruction);
                break;
            // End logical and bit field operations
            case LW:
                lw(instruction);
                break;
            case SW:
                sw(instruction);
                break;
            case LB:
                lb(instruction);
                break;
            case LH:
                lh(instruction);
                break;
            case SB:
                sb(instruction);
                break;
            case SH:
                sh(instruction);
                break;
            case LBU:
                lbu(instruction);
                break;
            case LWL:
                lwl(instruction);
                break;
            case LWR:
                lwr(instruction);
                break;
            case SWL:
                swl(instruction);
                break;
            case SWR:
                swr(instruction);
                break;
            case ULW:
                ulw(instruction);
                break;
            case USW:
                usw(instruction);
                break;
            case LHU:
                lhu(instruction);
                break;
            // END LOAD AND STORE OPERATIONS
            case MFHI:
                mfhi(instruction);
                break;
            case MFLO:
                mflo(instruction);
                break;
            case MTHI:
                mthi(instruction);
                break;
            case MTLO:
                mtlo(instruction);
                break;
            // END ACCUMULATOR ACCESS OPERATIONS
            case LL:
                ll(instruction);
                break;
            case SC:
                sc(instruction);
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

    private void sw(Instruction instruction) {
        int baseIndex = registerFile.readWord(instruction.rs);
        mainMemory.storeWord(registerFile.readWord(instruction.rd), baseIndex + instruction.offset);
    }

    private void srl(Instruction instruction) {
        int t1Value = registerFile.readWord(instruction.rs);
        registerFile.writeWord(instruction.rd, t1Value >>> instruction.immediateValue);
    }

    private void sll(Instruction instruction) {
        int t1Value = registerFile.readWord(instruction.rs);
        registerFile.writeWord(instruction.rd, t1Value << instruction.immediateValue);
    }

    private void li(Instruction instruction) {
        registerFile.writeWord(instruction.rd, instruction.immediateValue);
    }

    private void la(Instruction instruction) throws Exception {
        Integer address = labels.get(instruction.label);
        if (address == null) {
            throw new Exception(String.format("Fatal error! Illegal usage of %s on line: %d", instruction.opcode.name(), instruction.line));
        }
        registerFile.writeWord(instruction.rd, address);
    }

    private void lw(Instruction instruction) throws Exception {
        if (instruction.rd == null) {
            throw new Exception(String.format("Fatal error! Illegal usage of %s on line: %d", instruction.opcode.name(), instruction.line));
        }
        if (instruction.rs != null) {
            int baseIndex = registerFile.readWord(instruction.rs);
            registerFile.writeWord(instruction.rd, mainMemory.readWord(baseIndex + instruction.offset));
        } else {
            throw new Exception(String.format("Fatal error! Illegal usage of %s on line: %d", instruction.opcode.name(), instruction.line));
        }
    }

    private void jal(Instruction instruction) throws Exception {
        //set PC to the instruction given in the label
        Integer address = labels.get(instruction.label);
        if (address == null) {
            throw new Exception(String.format("Fatal error! Illegal usage of %s on line: %d", instruction.opcode.name(), instruction.line));
        }
        registerFile.writeWord("$ra", PC + 1);
        PC = address;
    }

    private void beq(Instruction instruction) throws Exception {
        //if registers rs and rt are equal, then increment PC by immediate value
        int rd = registerFile.readWord(instruction.rd), rs = registerFile.readWord(instruction.rs);
        if (rs == rd) {
            if (instruction.label == null) {
                PC += instruction.immediateValue;
            } else {
                jump(instruction);
            }
        }
    }

    private void bne(Instruction instruction) throws Exception {
        //if registers rs and rt are equal, then increment PC by immediate value
        int rd = registerFile.readWord(instruction.rd), rs = registerFile.readWord(instruction.rs);
        if (rd != rs) {
            if (instruction.label == null) {
                PC += instruction.immediateValue;
            } else {
                jump(instruction);
            }
        }
    }

    private void add(Instruction instruction) throws Exception {
        int leftOperand = registerFile.readWord(instruction.rs),
                rightOperand = registerFile.readWord(instruction.rt);
        if (instruction.rd == null) {
            throw new Exception(String.format("Fatal error! Illegal usage of %s on line: %d", instruction.opcode.name(), instruction.line));
        }
        registerFile.writeWord(instruction.rd, Math.addExact(leftOperand, rightOperand));
    }

    private void div(Instruction instruction) {
        int rd = registerFile.readWord(instruction.rd),
                rs = registerFile.readWord(instruction.rs);
        registerFile.accSetHI(rd % rs);
        registerFile.accSetLO(rd / rs);

    }

    private void mul(Instruction instruction) throws Exception {
        int leftOperand = registerFile.readWord(instruction.rs),
                rightOperand = registerFile.readWord(instruction.rt);
        if (instruction.rd == null) {
            throw new Exception(String.format("Fatal error! Illegal usage of %s on line: %d", instruction.opcode.name(), instruction.line));
        }
        registerFile.writeWord(instruction.rd, leftOperand * rightOperand);

    }

    private void sub(Instruction instruction) throws Exception {
        int leftOperand = registerFile.readWord(instruction.rs),
                rightOperand = registerFile.readWord(instruction.rt);
        if (instruction.rd == null) {
            throw new Exception(String.format("Fatal error! Illegal usage of %s on line: %d", instruction.opcode.name(), instruction.line));
        }
        registerFile.writeWord(instruction.rd, Math.subtractExact(leftOperand, rightOperand));
    }

    private void addi(Instruction instruction) {
        int leftOperand = registerFile.readWord(instruction.rs);
        registerFile.writeWord(instruction.rd, Math.addExact(leftOperand, instruction.immediateValue));
    }

    private void jr(Instruction instruction) {
        PC = registerFile.readWord(instruction.rd);
    }

    private void jump(Instruction instruction) throws Exception {
        Integer address = labels.get(instruction.label);
        if (address == null) {
            throw new Exception(String.format("Fatal error! Illegal usage of %s on line: %d", instruction.opcode.name(), instruction.line));
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

    private void addiu(Instruction instruction) {
        long leftOperand = Long.parseLong(Integer.toBinaryString(registerFile.readWord(instruction.rs)), 0x2);
        long rightOperand = Long.parseLong(Integer.toBinaryString(instruction.immediateValue), 0x2);
        registerFile.writeWord(instruction.rd, (int) (leftOperand + rightOperand));
    }

    private void addu(Instruction instruction) throws Exception {
        long rightOperand = Long.parseLong(Integer.toBinaryString(registerFile.readWord(instruction.rt)), 0x2),
                leftOperand = Long.parseLong(Integer.toBinaryString(registerFile.readWord(instruction.rs)), 0x2);
        if (instruction.rd == null) {
            throw new Exception(String.format("Fatal error! Illegal usage of %s on line: %d", instruction.opcode.name(), instruction.line));
        }
        registerFile.writeWord(instruction.rd, (int) (leftOperand + rightOperand));
    }

    private void clo(Instruction instruction) {
        int value = registerFile.readWord(instruction.rs), count = 0;
        String bits = Integer.toBinaryString(value);
        for (int i = 0; i < bits.length(); i++) {
            if (bits.charAt(i) == '0') {
                break;
            }
            count++;
        }
        registerFile.writeWord(instruction.rd, count);
    }

    private void clz(Instruction instruction) {
        int value = registerFile.readWord(instruction.rs);
        registerFile.writeWord(instruction.rd, Integer.numberOfLeadingZeros(value));
    }

    private void lui(Instruction instruction) {
        //the value of the rt is being parsed into Double and saved in rd
        registerFile.writeWord(instruction.rd, instruction.immediateValue << 0x10);
    }

    private void move(Instruction instruction) {
        registerFile.writeWord(instruction.rd, registerFile.readWord(instruction.rs));
    }

    private void negu(Instruction instruction) {
        registerFile.writeWord(instruction.rd, -registerFile.readWord(instruction.rs));
    }

    private void subu(Instruction instruction) throws Exception {
        long rightOperand = Long.parseLong(Integer.toBinaryString(registerFile.readWord(instruction.rt)), 0x2),
                leftOperand = Long.parseLong(Integer.toBinaryString(registerFile.readWord(instruction.rs)), 0x2);

        if (instruction.rd == null) {
            throw new Exception(String.format("Fatal error! Illegal usage of %s on line: %d", instruction.opcode.name(), instruction.line));
        }
        registerFile.writeWord(instruction.rd, (int) (leftOperand - rightOperand));
    }

    private void sllv(Instruction instruction) {
        int shiftAmount = registerFile.readWord(instruction.rt) & 0x1f;// uses last 5 bits as the shift amount
        int value = registerFile.readWord(instruction.rs);
        int result = value << shiftAmount;
        registerFile.writeWord(instruction.rd, result);
    }

    private void movn(Instruction instruction) {
        int rt = registerFile.readWord(instruction.rt),
                rs = registerFile.readWord(instruction.rs);
        if (rt != 0) {
            registerFile.writeWord(instruction.rd, rs);
        }
    }

    private void movz(Instruction instruction) {
        int rt = registerFile.readWord(instruction.rt), rs = registerFile.readWord(instruction.rs);
        if (rt == 0) {
            registerFile.writeWord(instruction.rd, rs);
        }
    }

    private void slt(Instruction instruction) {
        int rt = registerFile.readWord(instruction.rt), rs = registerFile.readWord(instruction.rs);
        registerFile.writeWord(instruction.rd, rs < rt ? 1 : 0);
    }

    private void sltu(Instruction instruction) {
        long rt = Long.parseLong(Integer.toBinaryString(registerFile.readWord(instruction.rt)), 0x2),
                rs = Long.parseLong(Integer.toBinaryString(registerFile.readWord(instruction.rs)), 0x2);
        registerFile.writeWord(instruction.rd, rs < rt ? 1 : 0);
    }

    private void slti(Instruction instruction) {
        int constant = instruction.immediateValue, rs = registerFile.readWord(instruction.rs);
        registerFile.writeWord(instruction.rd, rs < constant ? 1 : 0);
    }

    private void sltiu(Instruction instruction) {
        long constant = Long.parseLong(Integer.toBinaryString(instruction.immediateValue), 0x2),
                rs = Long.parseLong(Integer.toBinaryString(registerFile.readWord(instruction.rs)), 0x2);
        registerFile.writeWord(instruction.rd, rs < constant ? 1 : 0);
    }

    private void and(Instruction instruction) {
        int rs = registerFile.readWord(instruction.rs);
        int rt = registerFile.readWord(instruction.rt);

        registerFile.writeWord(instruction.rd, rs & rt);
    }

    private void andi(Instruction instruction) {
        int rs = registerFile.readWord(instruction.rs);
        int constant = instruction.immediateValue;

        registerFile.writeWord(instruction.rd, rs & constant);
    }

    private void nor(Instruction instruction) {
        int rs = registerFile.readWord(instruction.rs);
        int rt = registerFile.readWord(instruction.rt);

        registerFile.writeWord(instruction.rd, ~(rs | rt));
    }

    private void not(Instruction instruction) {
        int rs = registerFile.readWord(instruction.rs);
        registerFile.writeWord(instruction.rd, ~rs);
    }

    private void or(Instruction instruction) {
        int rs = registerFile.readWord(instruction.rs);
        int rt = registerFile.readWord(instruction.rt);

        registerFile.writeWord(instruction.rd, rs | rt);
    }

    private void ori(Instruction instruction) {
        int rs = registerFile.readWord(instruction.rs);
        int constant = instruction.immediateValue;

        registerFile.writeWord(instruction.rd, rs | constant);
    }

    private void xor(Instruction instruction) {
        int rs = registerFile.readWord(instruction.rs);
        int rt = registerFile.readWord(instruction.rt);

        registerFile.writeWord(instruction.rd, rs ^ rt);
    }

    private void xori(Instruction instruction) {
        int rs = registerFile.readWord(instruction.rs);
        int constant = instruction.immediateValue;

        registerFile.writeWord(instruction.rd, rs ^ constant);
    }

    private void ext(Instruction instruction) throws Exception {
        int size = instruction.size;
        int pos = instruction.pos;
        validateSizePos(size, pos);

        int rs = registerFile.readWord(instruction.rs);
        registerFile.writeWord(instruction.rd, extractBits(rs, pos, size) >> pos);
    }

    private void ins(Instruction instruction) throws Exception {

        int size = instruction.size;
        int pos = instruction.pos;
        validateSizePos(size, pos);

        int mask0 = 0, mask1 = -1;
        for (int i = 0; i < pos; i++) {
            mask0 = (mask0 | 1) << 1;
        }

        mask0 >>= 1; // undo extra shift
        mask1 <<= (size + pos);


        int rs = registerFile.readWord(instruction.rs);
        int extractedBits = extractBits(rs, pos, size);
        int mask = (mask0 | mask1) | extractedBits;

        int rd = registerFile.readWord(instruction.rd);
        registerFile.writeWord(instruction.rd, rd & mask);
    }

    private void lb(Instruction instruction) {
        int index = registerFile.readWord(instruction.rs) + instruction.offset;
        registerFile.writeWord(instruction.rd, mainMemory.read(index));
    }

    private void lbu(Instruction instruction) {
        int index = registerFile.readWord(instruction.rs) + instruction.offset;
        registerFile.writeWord(instruction.rd, mainMemory.read(index) & 0x00_00_00_ff);
    }


    private void lh(Instruction instruction) {
        int index = registerFile.readWord(instruction.rs) + instruction.offset;
        registerFile.writeWord(instruction.rd, mainMemory.readHalf(index));
    }


    private void lhu(Instruction instruction) {
        int index = registerFile.readWord(instruction.rs) + instruction.offset;
        registerFile.writeWord(instruction.rd, mainMemory.readHalf(index) & 0x00_00_ff_ff);
    }


    private void lwl(Instruction instruction) {
        int index = registerFile.readWord(instruction.rs) + instruction.offset;
        registerFile.writeWord(instruction.rd, mainMemory.readWord(index));
    }


    private void lwr(Instruction instruction) {
        int index = registerFile.readWord(instruction.rs) + instruction.offset;
        registerFile.writeWord(instruction.rd, mainMemory.readWord(index));
    }


    private void sb(Instruction instruction) {
        int index = registerFile.readWord(instruction.rs) + instruction.offset;
        mainMemory.store((byte) registerFile.readWord(instruction.rd), index);
    }


    private void sh(Instruction instruction) {
        int index = registerFile.readWord(instruction.rs) + instruction.offset;
        mainMemory.storeHalf((short) registerFile.readWord(instruction.rd), index);
    }


    private void swl(Instruction instruction) {
        int index = registerFile.readWord(instruction.rs) + instruction.offset;
        mainMemory.storeWord(registerFile.readWord(instruction.rd), index);
    }


    private void swr(Instruction instruction) {
        int index = registerFile.readWord(instruction.rs) + instruction.offset;
        mainMemory.storeWord(registerFile.readWord(instruction.rd), index);
    }


    private void ulw(Instruction instruction) {
        int index = registerFile.readWord(instruction.rs) + instruction.offset;
        registerFile.writeWord(instruction.rd, mainMemory.readWord(index));
    }

    private void usw(Instruction instruction) {
        int index = registerFile.readWord(instruction.rs) + instruction.offset;
        mainMemory.storeWord(registerFile.readWord(instruction.rd), index);
    }

    private void b(Instruction instruction) throws Exception {
        if (instruction.label == null) {
            PC += instruction.immediateValue;
        } else {
            jump(instruction);
        }
    }

    private void bal(Instruction instruction) throws Exception {
        registerFile.writeWord("$ra", PC + 1);
        if (instruction.label == null) {
            PC += instruction.immediateValue;
        } else {
            jump(instruction);
        }
    }

    private void beqz(Instruction instruction) throws Exception {
        int rs = registerFile.readWord(instruction.rd);
        if (rs == 0) {
            if (instruction.label == null) {
                PC += instruction.immediateValue;
            } else {
                jump(instruction);
            }
        }
    }

    private void bgez(Instruction instruction) throws Exception {
        int rs = registerFile.readWord(instruction.rd);
        if (rs >= 0) {
            if (instruction.label == null) {
                PC += instruction.immediateValue;
            } else {
                jump(instruction);
            }
        }
    }

    private void bgezal(Instruction instruction) throws Exception {
        registerFile.writeWord("$ra", PC + 1);
        int rs = registerFile.readWord(instruction.rd);
        if (rs >= 0) {
            if (instruction.label == null) {
                PC += instruction.immediateValue;
            } else {
                jump(instruction);
            }
        }
    }

    private void bgtz(Instruction instruction) throws Exception {
        int rs = registerFile.readWord(instruction.rd);
        if (rs > 0) {
            if (instruction.label == null) {
                PC += instruction.immediateValue;
            } else {
                jump(instruction);
            }
        }
    }

    private void blez(Instruction instruction) throws Exception {
        int rs = registerFile.readWord(instruction.rd);
        if (rs <= 0) {
            if (instruction.label == null) {
                PC += instruction.immediateValue;
            } else {
                jump(instruction);
            }
        }
    }

    private void bltz(Instruction instruction) throws Exception {
        int rs = registerFile.readWord(instruction.rd);
        if (rs < 0) {
            if (instruction.label == null) {
                PC += instruction.immediateValue;
            } else {
                jump(instruction);
            }
        }
    }

    private void bnez(Instruction instruction) throws Exception {
        int rs = registerFile.readWord(instruction.rd);
        if (rs != 0) {
            if (instruction.label == null) {
                PC += instruction.immediateValue;
            } else {
                jump(instruction);
            }
        }
    }

    private void bltzal(Instruction instruction) throws Exception {
        registerFile.writeWord("$ra", PC + 1);
        int rs = registerFile.readWord(instruction.rd);
        if (rs < 0) {
            if (instruction.label == null) {
                PC += instruction.immediateValue;
            } else {
                jump(instruction);
            }
        }

    }

    private void jalr(Instruction instruction) {
        registerFile.writeWord(instruction.rd, PC + 1);
        PC = registerFile.readWord(instruction.rs);
    }

    private void divu(Instruction instruction) {
        long rd = Long.parseLong(Integer.toBinaryString(registerFile.readWord(instruction.rd)), 0x2);
        long rs = Long.parseLong(Integer.toBinaryString(registerFile.readWord(instruction.rs)), 0x2);
        registerFile.accSetHI((int) (rd % rs));
        registerFile.accSetLO((int) (rd / rs));
    }

    private void madd(Instruction instruction) {
        int rd = registerFile.readWord(instruction.rd);
        int rs = registerFile.readWord(instruction.rs);
        registerFile.accAdd((long) rd * rs);
    }

    private void maddu(Instruction instruction) {
        long rd = Long.parseLong(Integer.toBinaryString(registerFile.readWord(instruction.rd)), 0x2);
        long rs = Long.parseLong(Integer.toBinaryString(registerFile.readWord(instruction.rs)), 0x2);
        registerFile.accAdd(rd * rs);
    }

    private void msub(Instruction instruction) {
        int rd = registerFile.readWord(instruction.rd);
        int rs = registerFile.readWord(instruction.rs);
        registerFile.accSub((long) rd * rs);
    }

    private void msubu(Instruction instruction) {
        long rd = Long.parseLong(Integer.toBinaryString(registerFile.readWord(instruction.rd)), 0x2);
        long rs = Long.parseLong(Integer.toBinaryString(registerFile.readWord(instruction.rs)), 0x2);
        registerFile.accSub(rd * rs);
    }

    private void mult(Instruction instruction) {
        int rd = registerFile.readWord(instruction.rd);
        int rs = registerFile.readWord(instruction.rs);
        registerFile.accSet((long) rd * rs);
    }

    private void multu(Instruction instruction) {
        long rd = Long.parseLong(Integer.toBinaryString(registerFile.readWord(instruction.rd)), 0x2);
        long rs = Long.parseLong(Integer.toBinaryString(registerFile.readWord(instruction.rs)), 0x2);
        registerFile.accSet(rd * rs);
    }

    private void mfhi(Instruction instruction) {
        registerFile.writeWord(instruction.rd, registerFile.accHI());
    }

    private void mflo(Instruction instruction) {
        registerFile.writeWord(instruction.rd, registerFile.accLO());
    }

    private void mthi(Instruction instruction) {
        registerFile.accSetHI(registerFile.readWord(instruction.rd));
    }

    private void mtlo(Instruction instruction) {
        registerFile.accSetLO(registerFile.readWord(instruction.rd));
    }

    private void rotr(Instruction instruction) {
        int rs = registerFile.readWord(instruction.rs);
        int immediateValue = instruction.immediateValue & 0x1f;

        registerFile.writeWord(instruction.rd,
                (extractBits(rs, 0x0, immediateValue) << (0x20 - immediateValue)) |
                        (extractBits(rs, immediateValue, 0x20 - immediateValue) >>> immediateValue));
    }

    private void rotrv(Instruction instruction) {
        int rs = registerFile.readWord(instruction.rs);
        int rt = registerFile.readWord(instruction.rt) & 0x1f;

        registerFile.writeWord(instruction.rd,
                (extractBits(rs, 0, rt) << (0x20 - rt)) |
                        (extractBits(rs, rt, 0x20 - rt)) >>> rt);
    }

    private void sra(Instruction instruction) {
        int rs = registerFile.readWord(instruction.rs);
        int immediateValue = instruction.immediateValue & 0x1f;
        registerFile.writeWord(instruction.rd, rs >> immediateValue);
    }

    private void srav(Instruction instruction) {
        int rs = registerFile.readWord(instruction.rs);
        int rt = registerFile.readWord(instruction.rt) & 0x1f;
        registerFile.writeWord(instruction.rd, rs >> rt);
    }

    private void srlv(Instruction instruction) {
        int rs = registerFile.readWord(instruction.rs);
        int rt = registerFile.readWord(instruction.rt) & 0x1f;
        registerFile.writeWord(instruction.rd, rs >>> rt);
    }

    private void seb(Instruction instruction) {
        int rs = registerFile.readWord(instruction.rs);
        registerFile.writeWord(instruction.rd, (rs < 0 ? -1 : 0) & 0xffff_ff00 | extractBits(rs, 0x0, 0x8));
    }

    private void seh(Instruction instruction) {
        int rs = registerFile.readWord(instruction.rs);
        registerFile.writeWord(instruction.rd, (rs < 0 ? -1 : 0) & 0xffff_ff00 | extractBits(rs, 0x0, 0x10));
    }

    private void wsbh(Instruction instruction) {
        int rs = registerFile.readWord(instruction.rs);
        registerFile.writeWord(instruction.rd, (extractBits(rs, 0x10, 0x8) << 0x18) | (extractBits(rs, 0x18, 0x8) << 0x10)
                | (extractBits(rs, 0x0, 0x8) << 0x8) | extractBits(rs, 0x8, 0x8));
    }

    private void ll(Instruction instruction) {
        int base = registerFile.readWord(instruction.rs);
        int address = base + instruction.offset;
        throwIfNotWordAligned(address);
        registerFile.writeWord(instruction.rd, mainMemory.readWord(address));
    }

    private void sc(Instruction instruction) {
        int base = registerFile.readWord(instruction.rs);
        int address = base + instruction.offset;
        throwIfNotWordAligned(address);
        mainMemory.storeWord(registerFile.readWord(instruction.rd), address);
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
