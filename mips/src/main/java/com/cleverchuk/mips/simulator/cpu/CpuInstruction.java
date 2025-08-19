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

package com.cleverchuk.mips.simulator.cpu;

import com.cleverchuk.mips.simulator.VirtualInstruction;

@SuppressWarnings({"UnusedReturnValue", "Unused"})
public class CpuInstruction implements VirtualInstruction {
  public String rd, rs, rt; // register names i.e $t0 , $v0 etc

  public String label; // label if any

  public CpuOpcode opcode;

  public int immediateValue;

  public int size; // support for ext opcode

  public int pos; // support for ext opcode

  public int offset; // memory offset

  public int line;

  public CpuInstruction(
      String rd,
      String rs,
      String rt,
      String label,
      CpuOpcode opcode,
      int immediateValue,
      int size,
      int pos,
      int offset,
      int line) {
    this.rd = rd;
    this.rs = rs;
    this.rt = rt;
    this.label = label;
    this.opcode = opcode;
    this.immediateValue = immediateValue;
    this.size = size;
    this.pos = pos;
    this.offset = offset;
    this.line = line;
  }

  public CpuInstruction(
      CpuOpcode opcode, String rd, String rs, String rt, String label, int offset) {
    this.rd = rd;
    this.rs = rs;
    this.rt = rt;
    this.label = label;
    this.opcode = opcode;
    this.offset = offset;
  }

  public static CpuInstructionBuilder builder() {
    return new CpuInstructionBuilder();
  }

  @Override
  public int line() {
    return line;
  }

  public static class CpuInstructionBuilder {
    private String rd, rs, rt; // register names i.e $t0 , $v0 etc

    private String label; // label if any

    private CpuOpcode CPUOpcode;

    private int immediateValue;

    private int size; // support for ext opcode

    private int pos; // support for ext opcode

    private int offset; // memory offset

    private int line;

    public CpuInstructionBuilder rd(String rd) {
      this.rd = rd;
      return this;
    }

    public CpuInstructionBuilder rs(String rs) {
      this.rs = rs;
      return this;
    }

    public CpuInstructionBuilder rt(String rt) {
      this.rt = rt;
      return this;
    }

    public CpuInstructionBuilder opcode(CpuOpcode CPUOpcode) {
      this.CPUOpcode = CPUOpcode;
      return this;
    }

    public CpuInstructionBuilder immediateValue(int immediateValue) {
      this.immediateValue = immediateValue;
      return this;
    }

    public CpuInstructionBuilder label(String label) {
      this.label = label;
      return this;
    }

    public CpuInstructionBuilder size(int size) {
      this.size = size;
      return this;
    }

    public CpuInstructionBuilder pos(int pos) {
      this.pos = pos;
      return this;
    }

    public CpuInstructionBuilder offset(int offset) {
      this.offset = offset;
      return this;
    }

    public CpuInstructionBuilder line(int line) {
      this.line = line;
      return this;
    }

    public CpuInstruction build() {
      return new CpuInstruction(
          rd, rs, rt, label, CPUOpcode, immediateValue, size, pos, offset, line);
    }
  }
}
