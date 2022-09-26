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
    SPACESTORAGE,
    BYTESTORAGE,
    HALFSTORAGE,
    WORDSTORAGE,
    FLOATSTORAGE,
    DOUBLESTORAGE,
    // REGEX
    ID,
    FLOAT,
    DECI,
    STRING,
    HEX,
    OCTAL,
    // LITERALS
    PLUS,
    MINUS,
    TIMES,
    DIV,
    DOLLARSIGN,
    COMMA,
    COLON,
    DOT,
    LPAREN,
    RPAREN,
    EOF
}
