package com.cleverchuk.mips.compiler.lexer;

public class Token {
    private final TokenType tokenType;

    private final Object value;

    private final int line;

    private final int pos;

    private final String code;

    public Token(TokenType tokenType, Object value, int line) {
        this.tokenType = tokenType;
        this.value = value;
        this.line = line;
        this.pos = -1;
        this.code = null;
    }

    public Token(TokenType tokenType, Object value, int line, int pos, String code) {
        this.tokenType = tokenType;
        this.value = value;
        this.line = line;
        this.pos = pos;
        this.code = code;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public Object getValue() {
        return value;
    }

    public int getLine() {
        return line;
    }

    public int getPos() {
        return pos;
    }

    public String getCode() {
        return code;
    }

    public static TokenBuilder builder() {
        return new TokenBuilder();
    }

    public static class TokenBuilder {
        private TokenType tokenType;

        private Object value;

        private int line;

        private int pos;


        private String code;

        public TokenBuilder tokenType(TokenType tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public TokenBuilder value(Object value) {
            this.value = value;
            return this;
        }

        public TokenBuilder line(int line) {
            this.line = line;
            return this;
        }

        public TokenBuilder pos(int pos) {
            this.pos = pos;
            return this;
        }

        public Token build() {
            return new Token(tokenType, value, line, pos, code);
        }
    }
}
