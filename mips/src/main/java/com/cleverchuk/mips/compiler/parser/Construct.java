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

public enum Construct {
    PROGRAM,
    DIRECTIVE,
    SEGMENT,
    DATASEG,
    DATADECLS,
    DATADECL,
    LABEL,
    DATA,
    DATAMODE,
    DATALIST,
    DATAEXPR,
    DATALISTS,
    EXPR,
    TERM,
    UNOP,
    CONSTANT,
    NEGCONSTANT,
    EXPRS,
    BINOP,

    TEXTSEG,
    TEXTDECLS,
    TEXTDECL,
    INSTRUCTION,
    THREEOP,
    TWOOP,
    ONEOP,
    ZEROOP,
    OPERAND,
    REGISTER,
    PARENREG,
    FACTOR,
    TERMS,
    OPCODE,
    FOUROP
}
