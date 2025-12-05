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

package com.cleverchuk.mips.simulator.fpu;

public class ReadOnlyRegisterFile implements RegisterFile {
  private final RegisterFile delegate;

  public ReadOnlyRegisterFile(RegisterFile delegate, int defaultValue) {
    this.delegate = delegate;
    this.delegate.writeWord(defaultValue);
  }

  @Override
  public int id() {
    return delegate.id();
  }

  @Override
  public String hexValue() {
    return delegate.hexValue();
  }

  @Override
  public int readWord() {
    return delegate.readWord();
  }

  @Override
  public long readDword() {
    return delegate.readWord();
  }

  @Override
  public float readSingle() {
    return delegate.readWord();
  }

  @Override
  public double readDouble() {
    return delegate.readWord();
  }
}
