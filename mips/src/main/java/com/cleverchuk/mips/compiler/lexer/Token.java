package com.cleverchuk.mips.compiler.lexer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Token {
    private final TokenType tokenType;
    private final Object value;
    private int line;
    private int pos;

    public Token(TokenType tokenType, Object value, int line) {
        this.tokenType = tokenType;
        this.value = value;
        this.line = line;
    }
}
