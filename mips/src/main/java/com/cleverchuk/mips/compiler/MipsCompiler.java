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

import com.cleverchuk.mips.compiler.codegen.CodeGenerator;
import com.cleverchuk.mips.compiler.parser.ErrorRecorder;
import com.cleverchuk.mips.compiler.parser.Node;
import com.cleverchuk.mips.compiler.parser.RecursiveDescentParser;
import com.cleverchuk.mips.compiler.parser.SyntaxError;
import com.cleverchuk.mips.simulator.VirtualInstruction;
import com.cleverchuk.mips.simulator.mem.Memory;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;

public final class MipsCompiler {
  private final RecursiveDescentParser parser;

  private final CodeGenerator codeGenerator;

  private int sourceHash = -1;

  @Inject
  public MipsCompiler(RecursiveDescentParser parser, CodeGenerator codeGenerator) {
    this.parser = parser;
    this.codeGenerator = codeGenerator;
  }

  public void compile(String source) {
    if (sourceHash == source.hashCode() && !codeGenerator.getInstructions().isEmpty()) {
      if (ErrorRecorder.hasErrors()) {
        throw new SyntaxError(ErrorRecorder.printErrors());
      }
      return;
    }

    sourceHash = source.hashCode();
    codeGenerator.flush();
    Node program = parser.parse(source);

    if (ErrorRecorder.hasErrors()) {
      throw new SyntaxError(ErrorRecorder.printErrors());
    }

    codeGenerator.generate(Objects.requireNonNull(program, "incorrect program"));
    if (ErrorRecorder.hasErrors()) {
      throw new SyntaxError(ErrorRecorder.printErrors());
    }
  }

  public Memory getDataSegment() {
    return codeGenerator.getMemory();
  }

  public List<VirtualInstruction> getTextSegment() {
    return codeGenerator.getInstructions();
  }

  public int dataSegmentOffset() {
    return codeGenerator.getDataSegmentOffset();
  }

  public int textSegmentOffset() {
    return codeGenerator.getTextSegmentOffset();
  }

  public int memBoundary() {
    return codeGenerator.getMemOffset();
  }
}
