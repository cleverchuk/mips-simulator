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

package com.cleverchuk.mips.simulator.binary;

import com.cleverchuk.mips.simulator.VirtualInstruction;

public class InstructionIR implements VirtualInstruction {

  private final Opcode opcode;

  private final int rd;

  private final int rs;

  private final int rt;

  private final int immediate;

  private final int offset;

  private final int size;

  private final int pos;

  private final int sa;

  private final String label;

  private InstructionIR(Builder builder) {
    this.opcode = builder.opcode;
    this.rd = builder.rd;
    this.rs = builder.rs;
    this.rt = builder.rt;
    this.immediate = builder.immediate;
    this.offset = builder.offset;
    this.size = builder.size;
    this.pos = builder.pos;
    this.sa = builder.sa;
    this.label = builder.label;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public int line() {
    return 0;
  }

  public Opcode getOpcode() {
    return opcode;
  }

  public int getRd() {
    return rd;
  }

  public int getRs() {
    return rs;
  }

  public int getRt() {
    return rt;
  }

  public int getImmediate() {
    return immediate;
  }

  public int getOffset() {
    return offset;
  }

  public int getSize() {
    return size;
  }

  public int getPos() {
    return pos;
  }

  public int getSa() {
    return sa;
  }

  public String getLabel() {
    return label;
  }

  /** Builder class for InstructionIR. */
  public static class Builder {
    private Opcode opcode;

    private int rd;

    private int rs;

    private int rt;

    private int immediate;

    private int offset;

    private int size;

    private int pos;

    private int sa;

    private String label;

    private Builder() {}

    public Builder withOpcode(Opcode opcode) {
      this.opcode = opcode;
      return this;
    }

    public Builder withRd(int rd) {
      this.rd = rd;
      return this;
    }

    public Builder withRs(int rs) {
      this.rs = rs;
      return this;
    }

    public Builder withRt(int rt) {
      this.rt = rt;
      return this;
    }

    public Builder withImmediate(int immediate) {
      this.immediate = immediate;
      return this;
    }

    public Builder withOffset(int offset) {
      this.offset = offset;
      return this;
    }

    public Builder withSize(int size) {
      this.size = size;
      return this;
    }

    public Builder withPos(int pos) {
      this.pos = pos;
      return this;
    }

    public Builder withSa(int sa) {
      this.sa = sa;
      return this;
    }

    public Builder withLabel(String label) {
      this.label = label;
      return this;
    }

    public InstructionIR build() {
      return new InstructionIR(this);
    }
  }
}
