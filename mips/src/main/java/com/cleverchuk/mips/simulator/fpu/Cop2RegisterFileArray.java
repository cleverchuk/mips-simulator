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

import java.util.HashMap;
import java.util.Map;

public class Cop2RegisterFileArray {
  private final Map<String, RegisterFile> registerFile = new HashMap<>();

  public Cop2RegisterFileArray() {
    registerFile.put("0", createReg(0));
    registerFile.put("1", createReg(1));
    registerFile.put("2", createReg(2));
    registerFile.put("3", createReg(3));
    registerFile.put("4", createReg(4));
  }

  public String regContents() {
    StringBuilder content = new StringBuilder();
    for (Map.Entry<String, RegisterFile> regEntry : registerFile.entrySet()) {
      content
          .append(regEntry.getKey())
          .append(": ")
          .append(regEntry.getValue().hexValue())
          .append("\n");
    }

    return content.toString();
  }

  public RegisterFile getFile(String reg) {
    return registerFile.get(reg);
  }

  private RegisterFile createReg(int id) {
    return new DefaultRegisterFile(id);
  }
}
