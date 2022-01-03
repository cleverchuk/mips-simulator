package com.cleverchuk.mips.compiler.lexer;

public enum TokenType {
    // RESERVED
    DATA,
    GLOBL,
    TEXT,
    IOPCODE,
    FOPCODE,
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
