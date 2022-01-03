package com.cleverchuk.mips.compiler.parser;

public class SyntaxError extends RuntimeException {
    public SyntaxError(String message) {
        super(message);
    }
}
