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

package com.cleverchuk.mips.compiler.parser;

public interface NodeVisitor {
  default void visitTextSegment(Node text) {}

  default void visitLabel(Node label) {}

  default void visitOpcode(Node opcode) {}

  default void visitReg(Node register) {}

  default void visitBaseRegister(Node register) {}

  default void visitExpression(Node expr) {}

  default void visitConstant(Node number) {}

  default void visitDataSegment(Node dataSeg) {}

  default void visitDataMode(Node dataMode) {}

  default void visitData(Node data) {}

  default void visitSegment(Node segment) {}

  default void visitOperand(Node operand) {}

  default void visitInstruction(Node instruction) {}

  default void visitProgram(Node program) {}

  default void visitDataDecls(Node dataDecls) {}

  default void visitTextDecls(Node textDecls) {}

  default void visitTextDecl(Node textDecl) {}

  default void visitDataDecl(Node dataDecl) {}

  default void visitDataList(Node dataList){}

  default void visitDataLists(Node dataLists){}

  default void visitTerm(Node term){}

  default void visitTerms(Node terms){}

  default void visitFactor(Node factor){}

  default void visitNegConstant(Node negConstant){}

  default void visitUnOp(Node unOp){}

  default void visitExprs(Node exprs){}

  default void visitBinOp(Node binOp){}

  default void visitFourOp(Node fourOp){}

  default void visitThreeOp(Node threeOp){}

  default void visitTwoOp(Node twoOp){}

  default void visitOneOp(Node oneOp){}

  default void visitZeroOp(Node zeroOp){}
}
