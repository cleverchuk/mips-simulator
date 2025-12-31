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

package com.cleverchuk.mips.compiler;

import com.cleverchuk.mips.compiler.codegen.Assembler;
import com.cleverchuk.mips.compiler.parser.ErrorRecorder;
import com.cleverchuk.mips.compiler.parser.RecursiveDescentParser;
import com.cleverchuk.mips.compiler.parser.SyntaxError;
import com.cleverchuk.mips.simulator.mem.Memory;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public final class MipsCompiler {
  private final RecursiveDescentParser parser;

  private final Assembler assembler;

  @Inject
  public MipsCompiler(RecursiveDescentParser parser, Assembler assembler) {
    this.parser = parser;
    this.assembler = assembler;
    parser.addVisitor(assembler);
  }

  public void compile(String source) {
    assembler.resetInternalState();
    parser.parse(source);
    if (ErrorRecorder.hasErrors()) {
      throw new SyntaxError(ErrorRecorder.printErrors());
    }
  }

  public List<Integer> getInstructions() {
    int dataOffset = assembler.getDataOffset();
    int textOffset = assembler.getTextOffset();

    int start = Math.min(dataOffset, textOffset);
    int end = Math.max(dataOffset, textOffset);

    List<Integer> instructions = new ArrayList<>();
    Memory layout = assembler.getLayout();

    while (start < end) {
      int instruction = layout.readWord(start);
      instructions.add(instruction);
      start += 4;
    }

    return instructions;
  }

  public int dataSegmentOffset() {
    return assembler.getDataOffset();
  }

  public int textSegmentOffset() {
    return assembler.getTextOffset();
  }
}
