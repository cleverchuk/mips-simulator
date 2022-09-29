package com.cleverchuk.mips.compiler.lexer;

enum LexerState {
        LEX_START,
        LEX_FLOAT,
        LEX_OCTAL,
        LEX_DECI,
        LEX_HEX,
        LEX_IDENTIFIER,
        LEX_STRING,
        LEX_COMMENT,
    }