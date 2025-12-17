/*
 * MIT License
 *
 * Copyright (c) 2022 CleverChuk
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.cleverchuk.mips.simulator;

import static com.cleverchuk.mips.simulator.SystemService.DEBUG;
import static com.cleverchuk.mips.simulator.SystemService.HALT;
import static com.cleverchuk.mips.simulator.SystemService.PRINT_CHAR;
import static com.cleverchuk.mips.simulator.SystemService.PRINT_DOUBLE;
import static com.cleverchuk.mips.simulator.SystemService.PRINT_FLOAT;
import static com.cleverchuk.mips.simulator.SystemService.PRINT_INT;
import static com.cleverchuk.mips.simulator.SystemService.PRINT_STRING;
import static com.cleverchuk.mips.simulator.SystemService.READ_CHAR;
import static com.cleverchuk.mips.simulator.SystemService.READ_DOUBLE;
import static com.cleverchuk.mips.simulator.SystemService.READ_FLOAT;
import static com.cleverchuk.mips.simulator.SystemService.READ_INT;

import android.os.Handler;
import android.util.SparseIntArray;
import com.cleverchuk.mips.compiler.MipsCompiler;
import com.cleverchuk.mips.compiler.parser.ErrorRecorder;
import com.cleverchuk.mips.compiler.parser.SymbolTable;
import com.cleverchuk.mips.compiler.parser.SyntaxError;
import com.cleverchuk.mips.dev.TerminalInputListener;
import com.cleverchuk.mips.simulator.cpu.Cpu;
import com.cleverchuk.mips.simulator.cpu.CpuInstruction;
import com.cleverchuk.mips.simulator.fpu.CoProcessor1;
import com.cleverchuk.mips.simulator.fpu.FpuInstruction;
import com.cleverchuk.mips.simulator.mem.Memory;
import com.cleverchuk.mips.simulator.registers.FpuRegisterFileArray;
import java.util.ArrayList;
import java.util.Locale;

public class MipsSimulator extends Thread implements TerminalInputListener, SystemServiceProvider {

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

  private final ArrayList<VirtualInstruction> virtualInstructions;

  private volatile State currentState = State.IDLE;

  private volatile State previousState = State.IDLE;

  private final Handler ioHandler;

  private final MipsCompiler compiler;

  private final Cpu cpu;

  private final Processor<FpuInstruction> cop;

  private final Memory memory;

  public MipsSimulator(
      Handler ioHandler, MipsCompiler compiler, Memory memory, byte processorFlags) {
    super("MipsSimulatorThread");
    virtualInstructions = new ArrayList<>();
    this.cpu = new Cpu(memory, this);
    if ((processorFlags & 0x2) > 0) {
      this.cop =
          new CoProcessor1(
              memory, new FpuRegisterFileArray(), this::getCpu, this.cpu::getRegisterFile);
    } else {
      this.cop = new NoOpProcessor();
    }

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

  public Processor<FpuInstruction> getCop() {
    return cop;
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

  public SparseIntArray getBreakpoints() {
    return breakpoints;
  }

  public int getInstructionEndPos() {
    return instructionEndPos;
  }

  public ArrayList<VirtualInstruction> getVirtualInstructions() {
    return virtualInstructions;
  }

  public State getCurrentState() {
    return currentState;
  }

  public State getPreviousState() {
    return previousState;
  }

  public Handler getIoHandler() {
    return ioHandler;
  }

  public MipsCompiler getCompiler() {
    return compiler;
  }

  public Memory getMemory() {
    return memory;
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
        VirtualInstruction instruction = virtualInstructions.get(cpu.getNextPC());
        if (instruction instanceof CpuInstruction) {
          cpu.execute((CpuInstruction) instruction);
        } else {
          cop.execute((FpuInstruction) instruction);
        }

      } catch (Exception e) {
        previousState = currentState;
        currentState = State.HALTED;
        int computedPC = cpu.getPC() - 1;

        int line =
            computedPC >= 0 && computedPC < instructionEndPos
                ? virtualInstructions.get(computedPC).line()
                : -1;
        String error =
            String.format(Locale.getDefault(), "[line : %d]\nERROR!!\n%s", line, e.getMessage());

        ioHandler.obtainMessage(PRINT_STRING.code, error).sendToTarget();
        ioHandler.obtainMessage(HALT.code).sendToTarget();
      }
      if (cpu.getPC() >= instructionEndPos) {
        previousState = currentState;
        currentState = State.HALTED;
        ioHandler.obtainMessage(HALT.code).sendToTarget();
      }
    }
  }

  @Override
  public void run() {
    for (; ; ) {

      if (currentState == State.RUNNING
          && breakpoints != null
          && breakpoints.get(getLineNumberToExecute()) > 0) {
        previousState = currentState;
        currentState = State.WAITING;
        ioHandler.obtainMessage(DEBUG.code).sendToTarget();
      }

      if (currentState == State.RUNNING) {
        step();

      } else if (currentState == State.STEPPING) {
        step();
        previousState = currentState;
        currentState = State.WAITING;

        ioHandler.obtainMessage(DEBUG.code).sendToTarget();

      } else if (currentState == State.STOP) {
        return;
      }
    }
  }

  private void init(String raw) throws Exception {
    compiler.compile(raw);
    if (!isPaused() && !ErrorRecorder.hasErrors()) {
      textSegmentOffset = compiler.textSegmentOffset();
      boolean hasTextSectionSpecified =
          textSegmentOffset == -1; // .text section is missing if this is True

      if (hasTextSectionSpecified) {
        currentState = State.ERROR;
        throw new Exception("Must have .text section");
      }

      virtualInstructions.clear();
      virtualInstructions.addAll(compiler.getTextSegment());
      instructionEndPos = virtualInstructions.size();

      cpu.setLabels(SymbolTable.getTable());
      cpu.setStackPointer(memory.getCapacity() - 10); // initialize stack pointer

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
      return virtualInstructions.get(cpu.getPC()).line();
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
      ioHandler.obtainMessage(PRINT_STRING.code, syntaxError.getMessage()).sendToTarget();

    } catch (Exception e) {
      previousState = currentState;
      currentState = State.ERROR;
      String error =
          String.format(
              "OoOps! Something went awry: %s\n If you think this is a bug, please report issue: https://github"
                  + ".com/CleverChuk/MipsIde-bug-track\n",
              e.getLocalizedMessage());

      ioHandler.obtainMessage(PRINT_STRING.code, error).sendToTarget();
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
        ioHandler.obtainMessage(PRINT_STRING.code, syntaxError.getMessage()).sendToTarget();
      }

    } catch (Exception e) {
      previousState = currentState;
      currentState = State.ERROR;
      String error =
          String.format(
              "OoOps! Something went awry: %s\n If you think this is a bug, please report issue: https://github"
                  + ".com/CleverChuk/MipsIde-bug-track\n",
              e.getLocalizedMessage());
      if (!silent) {
        ioHandler.obtainMessage(PRINT_STRING.code, error).sendToTarget();
      }
    }
  }

  @Override
  public void requestService(int which) throws Exception {
    SystemService systemService = SystemService.parse(which);
    switch (systemService) {
      case PRINT_INT:
        ioHandler.obtainMessage(PRINT_INT.code, cpu.getRegisterFile().read("$a0")).sendToTarget();
        break;

      case PRINT_STRING:
        {
          int arg = cpu.getRegisterFile().read("$a0"), c;
          StringBuilder builder = new StringBuilder();
          while ((c = memory.read(arg++)) != 0) {
            builder.append((char) c);
          }
          ioHandler.obtainMessage(PRINT_STRING.code, builder.toString()).sendToTarget();
          break;
        }

      case PRINT_CHAR:
        int arg = cpu.getRegisterFile().read("$a0");
        ioHandler.obtainMessage(PRINT_CHAR.code, (char) (arg)).sendToTarget();
        break;

      case PRINT_FLOAT:
        float single = cop.registerFiles().getFile("$f12").readSingle();
        ioHandler.obtainMessage(PRINT_FLOAT.code, single).sendToTarget();
        break;

      case PRINT_DOUBLE:
        double doubl = cop.registerFiles().getFile("$f12").readDouble();
        ioHandler.obtainMessage(PRINT_DOUBLE.code, doubl).sendToTarget();
        break;

      case HALT:
        previousState = currentState;
        currentState = State.HALTED;
        ioHandler.obtainMessage(HALT.code).sendToTarget();
        cpu.resetPC();
        break;

      case READ_INT: // Read int
        previousState = currentState;
        currentState = State.WAITING;
        ioHandler.obtainMessage(READ_INT.code).sendToTarget();
        break;

      case READ_CHAR: // Read Char
        previousState = currentState;
        currentState = State.WAITING;
        ioHandler.obtainMessage(READ_CHAR.code).sendToTarget();
        break;

      case READ_FLOAT: // Read float
        previousState = currentState;
        currentState = State.WAITING;
        ioHandler.obtainMessage(READ_FLOAT.code).sendToTarget();
        break;

      case READ_DOUBLE: // Read double
        previousState = currentState;
        currentState = State.WAITING;
        ioHandler.obtainMessage(READ_DOUBLE.code).sendToTarget();
        break;

      default:
        throw new Exception(String.format(Locale.getDefault(), "Service %d not supported!", which));
    }
  }

  private void transitionStateOnInput() {
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

  @Override
  public void onIntInput(int data) {
    cpu.getRegisterFile().write("$v0", data);
    transitionStateOnInput();
  }

  @Override
  public void onCharInput(char data) {
    onIntInput(data);
  }

  @Override
  public void onFloatInput(float data) {
    cop.registerFiles().getFile("$f0").writeSingle(data);
    transitionStateOnInput();
  }

  @Override
  public void onDoubleInput(double data) {
    cop.registerFiles().getFile("$f0").writeDouble(data);
    transitionStateOnInput();
  }

  @Override
  public void onStringInput(String data) {
    int address = cpu.getRegisterFile().read("$a0");
    int length = cpu.getRegisterFile().read("$a1");
    for (int offset = 0; offset < length; offset++) {
      memory.store((byte) data.charAt(offset), address + offset);
    }
    transitionStateOnInput();
  }
}
