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

package com.cleverchuk.mips.compiler.semantic.instruction;

import com.cleverchuk.mips.compiler.parser.Construct;
import com.cleverchuk.mips.compiler.parser.Node;
import com.cleverchuk.mips.compiler.semantic.Analyzer;
import com.cleverchuk.mips.simulator.Opcode;
import com.cleverchuk.mips.simulator.fpu.FpuOpcode;
import javax.inject.Inject;

public class InstructionAnalyzer implements Analyzer {
  private final ZeroOpAnalyzer zeroOpAnalyzer;

  private final OneOpAnalyzer oneOpAnalyzer;

  private final TwoOpAnalyzer twoOpAnalyzer;

  private final ThreeOpAnalyzer threeOpAnalyzer;

  private final FourOpAnalyzer fourOpAnalyzer;

  @Inject
  public InstructionAnalyzer(
      ZeroOpAnalyzer zeroOpAnalyzer,
      OneOpAnalyzer oneOpAnalyzer,
      TwoOpAnalyzer twoOpAnalyzer,
      ThreeOpAnalyzer threeOpAnalyzer,
      FourOpAnalyzer fourOpAnalyzer) {
    this.zeroOpAnalyzer = zeroOpAnalyzer;
    this.oneOpAnalyzer = oneOpAnalyzer;
    this.twoOpAnalyzer = twoOpAnalyzer;
    this.threeOpAnalyzer = threeOpAnalyzer;
    this.fourOpAnalyzer = fourOpAnalyzer;
  }

  @Override
  public boolean analyze(Node instructionNode) {
    Node opcodeKind = instructionNode.getChildren().get(0);
    Construct construct = opcodeKind.getConstruct();

    Opcode parse = parse((String) opcodeKind.getChildren().get(0).getValue());
    if (parse instanceof FpuOpcode) {
      return true;
    }

    switch (construct) {
      case ZEROOP:
        return zeroOpAnalyzer.analyze(opcodeKind);
      case ONEOP:
        return oneOpAnalyzer.analyze(opcodeKind);
      case TWOOP:
        return twoOpAnalyzer.analyze(opcodeKind);
      case THREEOP:
        return threeOpAnalyzer.analyze(opcodeKind);
      case FOUROP:
        return fourOpAnalyzer.analyze(opcodeKind);
      default:
        return false;
    }
  }
}
