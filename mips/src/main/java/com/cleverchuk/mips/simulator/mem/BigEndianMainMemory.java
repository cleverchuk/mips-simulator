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

package com.cleverchuk.mips.simulator.mem;

import javax.inject.Inject;

public class BigEndianMainMemory implements Memory {
  private byte[] backingStore;

  private static final int factor = 2;

  @Inject
  public BigEndianMainMemory() {
    this(512);
  }

  public BigEndianMainMemory(int capacity) {
    this.backingStore = new byte[capacity];
  }

  @Override
  public byte read(int offset) {
    ensureCap(offset);
    return backingStore[offset];
  }

  @Override
  public short readHalf(int offset) {
    ensureCap(offset);
    int out = 0x0;
    out |= backingStore[offset];

    out &= 0xff;
    out <<= 0x8;
    out |= (0x00ff & backingStore[offset + 1]);

    return (short) out;
  }

  @Override
  public int readWord(int offset) {
    ensureCap(offset);
    long out = readHalf(offset);
    out &= 0xffff;

    out <<= 0x10;
    out |= (0x0000_ffff & readHalf(offset + 2));
    return (int) (out);
  }

  @Override
  public long readDWord(int offset) {
    ensureCap(offset);
    long out = readWord(offset);
    out &= 0xffff_ffffL;

    out <<= 0x20;
    out |= (0x0000_ffff_ffffL & readWord(offset + 4));
    return out;
  }

  @Override
  public void store(byte bite, int offset) {
    ensureCap(offset);
    backingStore[offset] = bite;
  }

  @Override
  public void storeHalf(short half, int offset) {
    ensureCap(offset + 2);
    backingStore[offset] = (byte) ((half >> 8) & 0xff);
    backingStore[offset + 1] = (byte) (half & 0xff);
  }

  @Override
  public void storeWord(int word, int offset) {
    ensureCap(offset + 4);
    backingStore[offset] = (byte) ((word >> 24) & 0xff);
    backingStore[offset + 1] = (byte) ((word >> 16) & 0xff);
    backingStore[offset + 2] = (byte) ((word >> 8) & 0xff);
    backingStore[offset + 3] = (byte) (word & 0xff);
  }

  @Override
  public void storeDword(long dword, int offset) {
    ensureCap(offset + 8);
    backingStore[offset] = (byte) ((dword >> 56) & 0xff);
    backingStore[offset + 1] = (byte) ((dword >> 48) & 0xff);
    backingStore[offset + 2] = (byte) ((dword >> 40) & 0xff);
    backingStore[offset + 3] = (byte) ((dword >> 32) & 0xff);
    backingStore[offset + 4] = (byte) ((dword >> 24) & 0xff);
    backingStore[offset + 5] = (byte) ((dword >> 16) & 0xff);
    backingStore[offset + 6] = (byte) ((dword >> 8) & 0xff);
    backingStore[offset + 7] = (byte) (dword & 0xff);
  }

  @Override
  public void resize(int size) {
    byte[] temp = new byte[size];
    System.arraycopy(backingStore, 0, temp, 0, backingStore.length);
    backingStore = temp;
  }

  @Override
  public int getCapacity() {
    return backingStore.length;
  }

  private void ensureCap(int offset) {
    if (offset > backingStore.length) {
      int newSize = backingStore.length;
      while (newSize < offset) {
        newSize *= factor;
      }
      resize(newSize);
    }
  }
}
