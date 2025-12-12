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

import com.cleverchuk.mips.compiler.lexer.MipsLexer;
import java.util.HashMap;
import java.util.Map;

public class FpuRegisterFileArray {
  private final Map<String, RegisterFile> registerFile = new HashMap<>();

  public FpuRegisterFileArray() {
    MipsLexer.DECI_TO_FPU_REG.forEach(
        (key, name) -> registerFile.put("$" + name, createReg(name, Integer.parseInt(key))));
  }

  public RegisterFile getFile(String reg) {
    return registerFile.get(reg);
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

  private RegisterFile createReg(String name, int id) {
    if (name.equals("f0")) {
      return new ReadOnlyRegisterFile(new DefaultRegisterFile(id), 0);
    }

    return new DefaultRegisterFile(id);
  }
}
