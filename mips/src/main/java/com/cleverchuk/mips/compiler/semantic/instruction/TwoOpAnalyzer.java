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
import java.util.Objects;
import javax.inject.Inject;

public class TwoOpAnalyzer implements Analyzer {

  private final LoadStoreAnalyzer loadStoreAnalyzer;

  private final TwoRegOpcodeAnalyzer twoRegOpcodeAnalyzer;

  private final BranchOpcodeAnalyzer branchOpcodeAnalyzer;

  @Inject
  public TwoOpAnalyzer(
      LoadStoreAnalyzer loadStoreAnalyzer,
      TwoRegOpcodeAnalyzer twoRegOpcodeAnalyzer,
      BranchOpcodeAnalyzer branchOpcodeAnalyzer) {
    this.loadStoreAnalyzer = loadStoreAnalyzer;
    this.twoRegOpcodeAnalyzer = twoRegOpcodeAnalyzer;
    this.branchOpcodeAnalyzer = branchOpcodeAnalyzer;
  }

  @Override
  public boolean analyze(Node opcodeKind) {
    /* listing of two operand opcode mnemonics
     * all load and store opcodes
     * all multiply and divide opcode except mul
     * clo
     * clz
     * move
     * negu
     * seb
     * seh
     * not
     * jalr
     * ll
     * sc
     * wsbh
     * la
     * li
     * lui
     * beqz
     * bgez
     * begzal
     * bgtz
     * blez
     * bltz
     * bltzal
     * bnez
     * */
    List<Node> children = opcodeKind.getChildren();
    return children.size() == 3
        && (loadStoreAnalyzer.analyze(opcodeKind)
            || twoRegOpcodeAnalyzer.analyze(opcodeKind)
            || branchOpcodeAnalyzer.analyze(opcodeKind)
            || isLaValid(children)
            || isLiValid(children)
            || isLuiValid(children));
  }

  private boolean isLaValid(List<Node> children) {
    Node node = children.get(2);
    Construct construct = findNode(node, Construct.LABEL).orElse(node).getConstruct();
    return children.get(1).getConstruct() == Construct.REGISTER && construct == Construct.LABEL;
  }

  private boolean isLiValid(List<Node> children) {

    Node node = children.get(2);
    Construct construct =
        findNode(node, Construct.CONSTANT)
            .orElse(findNode(node, Construct.NEGCONSTANT).orElse(node))
            .getConstruct();

    return children.get(1).getConstruct() == Construct.REGISTER
        && (construct == Construct.CONSTANT || construct == Construct.NEGCONSTANT);
  }

  private boolean isLuiValid(List<Node> children) {
    Node node = children.get(2);
    Construct construct =
        findNode(node, Construct.CONSTANT)
            .orElse(findNode(node, Construct.NEGCONSTANT).orElse(node))
            .getConstruct();

    return children.get(1).getConstruct() == Construct.REGISTER
        && (construct == Construct.CONSTANT || construct == Construct.NEGCONSTANT);
  }

  public static class LoadStoreAnalyzer implements Analyzer {

    @Inject
    public LoadStoreAnalyzer() {}

    @Override
    public boolean analyze(Node opcodeKind) {
      List<Node> children = opcodeKind.getChildren();
      Node opcode = children.get(0);
      Opcode op = Opcode.parse((String) opcode.getValue());

      switch (Objects.requireNonNull(op)) {
        default:
          return false;
        case LB:
        case LBU:
        case LH:
        case LHU:
        case LW:
        case LWL:
        case LWR:
        case SB:
        case SH:
        case SW:
        case SWL:
        case SWR:
        case ULW:
        case USW:
        case LL:
        case SC:
          return isValidMemAccess(children, children.get(2));
      }
    }

    private boolean isValidMemAccess(List<Node> children, Node operand) {
      List<Node> operandChildren = operand.getChildren();
      return Construct.REGISTER == children.get(1).getConstruct()
          && Construct.OPERAND == operand.getConstruct()
          && Construct.EXPR == operandChildren.get(0).getConstruct()
          && Construct.PARENREG == operandChildren.get(1).getConstruct();
    }
  }

  public static class TwoRegOpcodeAnalyzer implements Analyzer {

    @Inject
    public TwoRegOpcodeAnalyzer() {}

    @Override
    public boolean analyze(Node opcodeKind) {
      List<Node> children = opcodeKind.getChildren();
      Node node = children.get(2);
      Construct construct = findNode(node, Construct.REGISTER).orElse(node).getConstruct();

      Node opcode = children.get(0);
      switch (Objects.requireNonNull(Opcode.parse((String) opcode.getValue()))) {
        default:
          return false;
        case DIV:
        case DIVU:
        case MADD:
        case MADDU:
        case MSUB:
        case MSUBU:
        case MULT:
        case MULTU:
        case CLO:
        case CLZ:
        case NOT:
        case NEGU:
        case SEB:
        case SEH:
        case JALR:
        case WSBH:
          return Construct.REGISTER == children.get(1).getConstruct()
              && Construct.REGISTER == construct;
      }
    }
  }

  public static class BranchOpcodeAnalyzer implements Analyzer {

    @Inject
    public BranchOpcodeAnalyzer() {}

    @Override
    public boolean analyze(Node opcodeKind) {
      List<Node> children = opcodeKind.getChildren();
      Node node = children.get(2);
      Construct construct =
          findNode(node, Construct.CONSTANT)
              .orElse(
                  findNode(node, Construct.NEGCONSTANT)
                      .orElse(findNode(node, Construct.LABEL).orElse(node)))
              .getConstruct();

      Node opcode = children.get(0);
      switch (Objects.requireNonNull(Opcode.parse((String) opcode.getValue()))) {
        default:
          return false;
        case BEQZ:
        case BGEZ:
        case BGEZAL:
        case BGTZ:
        case BLEZ:
        case BLTZ:
        case BLTZAL:
        case BNEZ:
          return Construct.REGISTER == children.get(1).getConstruct()
              && (Construct.CONSTANT == construct
                  || Construct.NEGCONSTANT == construct
                  || Construct.LABEL == construct);
      }
    }
  }
}
