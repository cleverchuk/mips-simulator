package com.cleverchuk.mips.compiler.lexer;

import com.cleverchuk.mips.simulator.fpu.DataFormat;

public class Token {
    private final TokenType tokenType;

    private final Object value;

    private final int line;

    private final int pos;

    private final String code;

    private DataFormat format;
    public Token(TokenType tokenType, Object value, int line) {
        this.tokenType = tokenType;
        this.value = value;
        this.line = line;
        this.pos = -1;
        this.code = null;
        this.format = null;
    }

    public Token(TokenType tokenType, Object value, int line, int pos, String code, DataFormat format) {
        this.tokenType = tokenType;
        this.value = value;
        this.line = line;
        this.pos = pos;
        this.code = code;
        this.format = format;
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

    public DataFormat getFormat() {
        return format;
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

        private DataFormat format;

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

        public TokenBuilder format(DataFormat format) {
            this.format = format;
            return this;
        }

        public Token build() {
            return new Token(tokenType, value, line, pos, code, format);
        }
    }
}
