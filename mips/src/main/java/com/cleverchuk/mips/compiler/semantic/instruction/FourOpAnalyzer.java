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
import com.cleverchuk.mips.simulator.binary.Opcode;
import java.util.List;
import javax.inject.Inject;

public class FourOpAnalyzer implements Analyzer {

  @Inject
  public FourOpAnalyzer() {}

  @Override
  public boolean analyze(Node opcodeKind) {
    List<Node> children = opcodeKind.getChildren();
    Node opcode = children.get(0);
    if (children.size() == 5) {
      if (Opcode.EXT.same((String) opcode.getValue())
          || Opcode.INS.same((String) opcode.getValue())) {
        return Construct.REGISTER == children.get(1).getConstruct()
            && Construct.REGISTER == children.get(2).getConstruct()
            && Construct.EXPR == children.get(3).getConstruct()
            && Construct.EXPR == children.get(4).getConstruct();
      }
    }
    return false;
  }
}
