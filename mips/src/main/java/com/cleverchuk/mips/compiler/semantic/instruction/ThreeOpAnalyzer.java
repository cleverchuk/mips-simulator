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

public class ThreeOpAnalyzer implements Analyzer {
  private final ShiftRotateAnalyzer shiftRotateAnalyzer;

  private final ConditionalTestingAndMoveAnalyzer conditionalTestingAndMoveAnalyzer;

  private final ArithmeticAndLogicalOpcodeAnalyzer arithmeticAndLogicalOpcodeAnalyzer;

  @Inject
  public ThreeOpAnalyzer(
      ShiftRotateAnalyzer shiftRotateAnalyzer,
      ConditionalTestingAndMoveAnalyzer conditionalTestingAndMoveAnalyzer,
      ArithmeticAndLogicalOpcodeAnalyzer arithmeticAndLogicalOpcodeAnalyzer) {
    this.shiftRotateAnalyzer = shiftRotateAnalyzer;
    this.conditionalTestingAndMoveAnalyzer = conditionalTestingAndMoveAnalyzer;
    this.arithmeticAndLogicalOpcodeAnalyzer = arithmeticAndLogicalOpcodeAnalyzer;
  }

  @Override
  public boolean analyze(Node opcodeKind) {
    /* listing of three operand opcode mnemonics
     * all shift and rotate opcodes
     * all conditional testing and conditional move opcode
     * add
     * addu
     * sub
     * subu
     * mul
     * nor
     * or
     * and
     * xor
     * addi
     * addiu
     * andi
     * ori
     * xori
     * beq
     * bne
     * */
    List<Node> children = opcodeKind.getChildren();
    return children.size() == 4
        && (shiftRotateAnalyzer.analyze(opcodeKind)
            || conditionalTestingAndMoveAnalyzer.analyze(opcodeKind)
            || arithmeticAndLogicalOpcodeAnalyzer.analyze(opcodeKind)
            || isBranchValid(children));
  }

  private boolean isBranchValid(List<Node> children) {
    Node node = children.get(3);
    Construct construct =
        findNode(node, Construct.CONSTANT)
            .orElse(
                findNode(node, Construct.NEGCONSTANT)
                    .orElse(findNode(node, Construct.LABEL).orElse(node)))
            .getConstruct();

    Node opcode = children.get(0);
    Object opcodeValue = opcode.getValue();
    return (Opcode.BEQ.same((String) opcodeValue) || Opcode.BNE.same((String) opcodeValue))
        && (Construct.REGISTER == children.get(1).getConstruct()
            && Construct.REGISTER == children.get(2).getConstruct()
            && (Construct.CONSTANT == construct
                || Construct.NEGCONSTANT == construct
                || Construct.LABEL == construct));
  }

  public static class ShiftRotateAnalyzer implements Analyzer {

    @Inject
    public ShiftRotateAnalyzer() {}

    @Override
    public boolean analyze(Node opcodeKind) {
      List<Node> children = opcodeKind.getChildren();
      Node node = children.get(3);
      Construct construct =
          findNode(node, Construct.CONSTANT)
              .orElse(
                  findNode(node, Construct.REGISTER)
                      .orElse(findNode(node, Construct.NEGCONSTANT).orElse(node)))
              .getConstruct();

      Node opcode = children.get(0);
      switch (Objects.requireNonNull(Opcode.parse((String) opcode.getValue()))) {
        default:
          return false;
        case SLLV:
        case SRAV:
        case SRLV:
        case ROTRV:
          return Construct.REGISTER == children.get(1).getConstruct()
              && Construct.REGISTER == children.get(2).getConstruct()
              && Construct.REGISTER == construct;

        case ROTR:
        case SLL:
        case SRA:
        case SRL:
          return Construct.REGISTER == children.get(1).getConstruct()
              && Construct.REGISTER == children.get(2).getConstruct()
              && (construct == Construct.CONSTANT || construct == Construct.NEGCONSTANT);
      }
    }
  }

  public static class ConditionalTestingAndMoveAnalyzer implements Analyzer {

    @Inject
    public ConditionalTestingAndMoveAnalyzer() {}

    @Override
    public boolean analyze(Node opcodeKind) {
      List<Node> children = opcodeKind.getChildren();
      Node node = children.get(3);
      Construct construct =
          findNode(node, Construct.CONSTANT)
              .orElse(
                  findNode(node, Construct.REGISTER)
                      .orElse(findNode(node, Construct.NEGCONSTANT).orElse(node)))
              .getConstruct();

      Node opcode = children.get(0);
      switch (Objects.requireNonNull(Opcode.parse((String) opcode.getValue()))) {
        default:
          return false;
        case MOVN:
        case MOVZ:
        case SLT:
        case SLTU:
          return Construct.REGISTER == children.get(1).getConstruct()
              && Construct.REGISTER == children.get(2).getConstruct()
              && Construct.REGISTER == construct;
        case SLTI:
        case SLTIU:
          return Construct.REGISTER == children.get(1).getConstruct()
              && Construct.REGISTER == children.get(2).getConstruct()
              && (construct == Construct.CONSTANT || construct == Construct.NEGCONSTANT);
      }
    }
  }

  public static class ArithmeticAndLogicalOpcodeAnalyzer implements Analyzer {

    @Inject
    public ArithmeticAndLogicalOpcodeAnalyzer() {}

    @Override
    public boolean analyze(Node opcodeKind) {
      List<Node> children = opcodeKind.getChildren();
      Node node = children.get(3);
      Construct construct =
          findNode(node, Construct.CONSTANT)
              .orElse(
                  findNode(node, Construct.REGISTER)
                      .orElse(findNode(node, Construct.NEGCONSTANT).orElse(node)))
              .getConstruct();

      Node opcode = children.get(0);
      switch (Objects.requireNonNull(Opcode.parse((String) opcode.getValue()))) {
        default:
          return true;
        case ADD:
        case ADDU:
        case SUB:
        case SUBU:
        case MUL:
        case AND:
        case OR:
        case NOR:
        case XOR:
          return Construct.REGISTER == children.get(1).getConstruct()
              && Construct.REGISTER == children.get(2).getConstruct()
              && Construct.REGISTER == construct;
        case ADDIU:
        case ANDI:
        case ORI:
        case XORI:
          return Construct.REGISTER == children.get(1).getConstruct()
              && Construct.REGISTER == children.get(2).getConstruct()
              && (construct == Construct.CONSTANT || construct == Construct.NEGCONSTANT);
      }
    }
  }
}
