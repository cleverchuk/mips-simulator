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

package com.cleverchuk.mips.simulator.registers;

public class DefaultRegisterFile implements RegisterFile {
  private final byte[] dflops = new byte[8];

  private final short mask = (short) 0xff;

  private final byte shifts = (byte) 0x8;

  private final int id;

  public DefaultRegisterFile(int id) {
    this.id = id;
  }

  @Override
  public int id() {
    return id;
  }

  public void writeOnes(int length) {
    for (int i = 0; i < length; i++) {
      dflops[i] = (byte) (0xff);
    }
  }

  public String hexValue() {
    long dword = readDword();
    return Long.toHexString(dword);
  }

  public void writeZeroes(int length) {
    for (int i = 0; i < length; i++) {
      dflops[i] = (byte) (0x0);
    }
  }

  public void writeWord(int word) {
    for (int i = 0; i < 4; i++) {
      dflops[i] = (byte) (word & mask);
      word >>>= shifts;
    }
  }

  public void writeDword(long dword) {
    for (int i = 0; i < 8; i++) {
      dflops[i] = (byte) (dword & mask);
      dword >>>= shifts;
    }
  }

  public void writeSingle(float single) {
    int intBits = Float.floatToRawIntBits(single);
    for (int i = 0; i < 4; i++) {
      dflops[i] = (byte) (intBits & mask);
      intBits >>>= shifts;
    }
  }

  public void writeDouble(double doubl) {
    long longBits = Double.doubleToRawLongBits(doubl);
    for (int i = 0; i < 8; i++) {
      dflops[i] = (byte) (longBits & mask);
      longBits >>>= shifts;
    }
  }

  public int readWord() {
    int word = 0;
    for (int i = 3; i >= 0; i--) {
      word <<= shifts;
      word |= (mask & dflops[i]);
    }

    return word;
  }

  public long readDword() {
    long dWord = 0;
    for (int i = 7; i >= 0; i--) {
      dWord <<= shifts;
      dWord |= (mask & dflops[i]);
    }

    return dWord;
  }

  public float readSingle() {
    int single = 0;
    for (int i = 3; i >= 0; i--) {
      single <<= shifts;
      single |= (mask & dflops[i]);
    }

    return Float.intBitsToFloat(single);
  }

  public double readDouble() {
    long doubly = 0;
    for (int i = 7; i >= 0; i--) {
      doubly <<= shifts;
      doubly |= (mask & dflops[i]);
    }

    return Double.longBitsToDouble(doubly);
  }
}
