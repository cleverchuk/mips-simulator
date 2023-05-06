package com.cleverchuk.mips.simulator;

import android.os.Handler;
import android.util.SparseIntArray;
import com.cleverchuk.mips.compiler.MipsCompiler;
import com.cleverchuk.mips.compiler.parser.ErrorRecorder;
import com.cleverchuk.mips.compiler.parser.SymbolTable;
import com.cleverchuk.mips.compiler.parser.SyntaxError;
import com.cleverchuk.mips.dev.OnUserInputListener;
import com.cleverchuk.mips.simulator.cpu.CpuInstruction;
import com.cleverchuk.mips.simulator.cpu.Cpu;
import com.cleverchuk.mips.simulator.fpu.CoProcessor;
import com.cleverchuk.mips.simulator.fpu.FpuInstruction;
import com.cleverchuk.mips.simulator.fpu.FpuRegisterFileArray;
import com.cleverchuk.mips.simulator.mem.Memory;
import java.util.ArrayList;
import java.util.Locale;

public class MipsSimulator extends Thread implements OnUserInputListener<Integer>, SystemServiceProvider {

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

    private int instructionEndPos = 0;

    private final ArrayList<VirtualInstruction> cpuInstructionMemory;

    private volatile State currentState = State.IDLE;

    private volatile State previousState = State.IDLE;

    private final Handler ioHandler;

    private final MipsCompiler compiler;

    private final Cpu cpu;

    private final CoProcessor cop;

    private final Memory memory;

    public MipsSimulator(Handler ioHandler, MipsCompiler compiler, Memory memory) {
        super("MipsSimulatorThread");
        cpuInstructionMemory = new ArrayList<>();
        this.cpu = new Cpu(memory, this);
        this.cop = new CoProcessor(memory, new FpuRegisterFileArray(), this.cpu::getRegisterFile);

        this.ioHandler = ioHandler;
        this.compiler = compiler;
        this.memory = memory;

    }

    public int getPC() {
        return cpu.getPC();
    }

    public Cpu getCpu() {
        return cpu;
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
            cpu.resetPC();
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
                VirtualInstruction instruction = cpuInstructionMemory.get(cpu.getNextPC());
                if (instruction instanceof CpuInstruction) {
                    cpu.execute((CpuInstruction) instruction);
                } else {
                    cop.execute((FpuInstruction) instruction);
                }

            } catch (Exception e) {
                previousState = currentState;
                currentState = State.HALTED;
                int computedPC = cpu.getPC() - 1;

                int line = computedPC >= 0 && computedPC < instructionEndPos ? cpuInstructionMemory.get(computedPC).line() : -1;
                String error = String.format(Locale.getDefault(), "[line : %d]\nERROR!!\n%s", line, e.getMessage());

                ioHandler.obtainMessage(100, error)
                        .sendToTarget();
                ioHandler.obtainMessage(10)
                        .sendToTarget();
            }
            if (cpu.getPC() >= instructionEndPos) {
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
            instructionEndPos = cpuInstructionMemory.size();

            cpu.setLabels(SymbolTable.getTable());
            cpu.setStackPointer(compiler.memBoundary() + 10); // initialize stack pointer

            if (cpu.getPC() >= instructionEndPos || isHalted() || isPaused()) {
                idle();
                cpu.resetPC();
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
        if (cpu.getPC() < instructionEndPos) {
            return cpuInstructionMemory.get(cpu.getPC()).line();
        }
        return 0;
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

    @Override
    public void requestService(int which) throws Exception {
        switch (which) {
            case 1:
                //Print Int
                ioHandler.obtainMessage(1, cpu.getRegisterFile().read("$a0"))
                        .sendToTarget();
                break;

            case 4: {
                //Print String
                int arg = cpu.getRegisterFile().read("$a0"), c;
                StringBuilder builder = new StringBuilder();
                while ((c = memory.read(arg++)) != 0) {
                    builder.append((char) c);
                }

                ioHandler.obtainMessage(4, builder.toString())
                        .sendToTarget();
                break;
            }

            case 11:
                //Print Char
                int arg = cpu.getRegisterFile().read("$a0");
                ioHandler.obtainMessage(11, (char) (arg))
                        .sendToTarget();
                break;

            case 10:
                previousState = currentState;
                currentState = State.HALTED;
                ioHandler.obtainMessage(10)
                        .sendToTarget();

                cpu.resetPC();
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

    @Override
    public void onInputComplete(Integer data) {
        cpu.getRegisterFile().write("$v0", data);
        if (cpu.getPC() >= instructionEndPos) {
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
