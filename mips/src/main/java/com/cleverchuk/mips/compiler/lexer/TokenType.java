package com.cleverchuk.mips.compiler.lexer;

public enum TokenType {
    // RESERVED
    DATA,
    GLOBL, // change this to grammar ie directive -> .ID
    TEXT,
    CPU_OPCODE,
    FPU_OPCODE,
    REG,
    ASCII,
    ASCIIZ,
    SPACE_STORAGE,
    BYTE_STORAGE,
    HALF_STORAGE,
    WORD_STORAGE,
    FLOAT_STORAGE,
    DOUBLE_STORAGE,
    // REGEX
    ID,
    FLOATING_POINT,
    DECI,
    STRING,
    HEX,
    OCTAL,
    // LITERALS
    PLUS,
    MINUS,
    TIMES,
    DIV,
    DOLLAR_SIGN,
    COMMA,
    COLON,
    DOT,
    L_PAREN,
    R_PAREN,
    EOF
}
