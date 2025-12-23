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

public class OneOpAnalyzer implements Analyzer {

  @Inject
  public OneOpAnalyzer() {}

  @Override
  public boolean analyze(Node opcodeKind) {
    /* listing of one operand opcode mnemonics
     * b
     * bal
     * j
     * jal
     * jr
     * MFHI
     * MFLO
     * MTHI
     * MTLO
     * */
    List<Node> children = opcodeKind.getChildren();
    Node opcode = children.get(0);
    return children.size() == 2
        && (isBvalid(opcode, children)
            || isBalValid(opcode, children)
            || isJvalid(opcode, children)
            || isJalValid(opcode, children)
            || isJrValid(opcode, children)
            || isMfhiValid(opcode, children)
            || isMfloValid(opcode, children)
            || isMthiValid(opcode, children)
            || isMtloValid(opcode, children));
  }

  private boolean isMthiValid(Node opcode, List<Node> children) {
    Node node = children.get(1);
    Construct construct = findNode(node, Construct.REGISTER).orElse(node).getConstruct();

    return Opcode.MTHI.same((String) opcode.getValue()) && Construct.REGISTER == construct;
  }

  private boolean isMtloValid(Node opcode, List<Node> children) {
    Node node = children.get(1);
    Construct construct = findNode(node, Construct.REGISTER).orElse(node).getConstruct();

    return Opcode.MTLO.same((String) opcode.getValue()) && Construct.REGISTER == construct;
  }

  private boolean isMfloValid(Node opcode, List<Node> children) {
    Node node = children.get(1);
    Construct construct = findNode(node, Construct.REGISTER).orElse(node).getConstruct();

    return Opcode.MFLO.same((String) opcode.getValue()) && Construct.REGISTER == construct;
  }

  private boolean isMfhiValid(Node opcode, List<Node> children) {
    Node node = children.get(1);
    Construct construct = findNode(node, Construct.REGISTER).orElse(node).getConstruct();

    return Opcode.MFHI.same((String) opcode.getValue()) && Construct.REGISTER == construct;
  }

  private boolean isJrValid(Node opcode, List<Node> children) {
    Node node = children.get(1);
    Construct construct = findNode(node, Construct.REGISTER).orElse(node).getConstruct();

    return (Opcode.JR.same((String) opcode.getValue())
            || Opcode.JALR.same((String) opcode.getValue()))
        && Construct.REGISTER == construct;
  }

  private boolean isJvalid(Node opcode, List<Node> children) {
    Node node = children.get(1);
    Construct construct = findNode(node, Construct.LABEL).orElse(node).getConstruct();

    return Opcode.J.same((String) opcode.getValue()) && Construct.LABEL == construct;
  }

  private boolean isJalValid(Node opcode, List<Node> children) {
    Node node = children.get(1);
    Construct construct = findNode(node, Construct.LABEL).orElse(node).getConstruct();

    return Opcode.JAL.same((String) opcode.getValue()) && Construct.LABEL == construct;
  }

  private boolean isBalValid(Node opcode, List<Node> children) {
    Node node = children.get(1);
    Construct construct =
        findNode(node, Construct.CONSTANT)
            .orElse(
                findNode(node, Construct.NEGCONSTANT)
                    .orElse(findNode(node, Construct.LABEL).orElse(node)))
            .getConstruct();

    return Opcode.BAL.same((String) opcode.getValue())
        && (Construct.CONSTANT == construct
            || Construct.NEGCONSTANT == construct
            || Construct.LABEL == construct);
  }

  private boolean isBvalid(Node opcode, List<Node> children) {
    Node node = children.get(1);
    Construct construct =
        findNode(node, Construct.CONSTANT)
            .orElse(
                findNode(node, Construct.NEGCONSTANT)
                    .orElse(findNode(node, Construct.LABEL).orElse(node)))
            .getConstruct();

    return Opcode.B.same((String) opcode.getValue())
        && (Construct.CONSTANT == construct
            || Construct.NEGCONSTANT == construct
            || Construct.LABEL == construct);
  }
}
