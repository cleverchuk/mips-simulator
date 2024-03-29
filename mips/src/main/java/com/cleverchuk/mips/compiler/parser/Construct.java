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
