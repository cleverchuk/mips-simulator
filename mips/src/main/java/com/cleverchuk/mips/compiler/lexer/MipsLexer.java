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

package com.cleverchuk.mips.compiler.lexer;

import androidx.annotation.Nullable;
import com.cleverchuk.mips.simulator.cpu.CpuOpcode;
import com.cleverchuk.mips.simulator.fpu.FpuOpcode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.inject.Inject;

import static com.cleverchuk.mips.compiler.lexer.LexerState.LEX_COMMENT;
import static com.cleverchuk.mips.compiler.lexer.LexerState.LEX_DECI;
import static com.cleverchuk.mips.compiler.lexer.LexerState.LEX_FLOATING_POINT;
import static com.cleverchuk.mips.compiler.lexer.LexerState.LEX_HEX;
import static com.cleverchuk.mips.compiler.lexer.LexerState.LEX_IDENTIFIER;
import static com.cleverchuk.mips.compiler.lexer.LexerState.LEX_OCTAL;
import static com.cleverchuk.mips.compiler.lexer.LexerState.LEX_START;
import static com.cleverchuk.mips.compiler.lexer.LexerState.LEX_STRING;


public final class MipsLexer {
    private static final Pattern ID = Pattern.compile("[A-Za-z][A-Za-z0-9.]*");

    private static final Pattern DECI = Pattern.compile("[0-9]+");

    private static final Pattern FLOATING_POINT = Pattern.compile("[0-9]+\\.[0-9]*");

    private static final Pattern HEX = Pattern.compile("0[xX][a-f0-9]+");

    private static final Pattern OCTAL = Pattern.compile("0[0-7]+");

    private static final Pattern STRING = Pattern.compile("^\".*\"$");

    private static final Pattern COMMENT = Pattern.compile("#.*");

    @Inject
    public MipsLexer() {
    }

    public static final Map<String, TokenType> RESERVED = new HashMap<String, TokenType>() {{
        put("data", TokenType.DATA);
        put("text", TokenType.TEXT);
        put("asciiz", TokenType.ASCIIZ);
        put("ascii", TokenType.ASCII);
        put("space", TokenType.SPACE_STORAGE);
        put("byte", TokenType.BYTE_STORAGE);
        put("half", TokenType.HALF_STORAGE);
        put("word", TokenType.WORD_STORAGE);
        put("float", TokenType.FLOAT_STORAGE);
        put("double", TokenType.DOUBLE_STORAGE);
        put("globl", TokenType.GLOBL);

    }};

    public static final Map<String, String> DECI_TO_CPU_REG = new HashMap<String, String>() {{
        put("0", "zero");
        put("1", "at");
        put("2", "v0");
        put("3", "v1");
        put("4", "a0");
        put("5", "a1");
        put("6", "a2");
        put("7", "a3");
        put("8", "t0");
        put("9", "t1");
        put("10", "t2");
        put("11", "t3");
        put("12", "t4");
        put("13", "t5");
        put("14", "t6");
        put("15", "t7");
        put("16", "s0");
        put("17", "s1");
        put("18", "s2");
        put("19", "s3");
        put("20", "s4");
        put("21", "s5");
        put("22", "s6");
        put("23", "s7");
        put("24", "t8");
        put("25", "t9");
        put("26", "k0");
        put("27", "k1");
        put("28", "gp");
        put("29", "sp");
        put("30", "fp");
        put("31", "ra");
    }};

    public static final Map<String, String> DECI_TO_FPU_REG = new HashMap<String, String>() {{
        put("0", "f0"); //FIR readonly
        put("1", "f1");
        put("2", "f2");
        put("3", "f3");
        put("4", "f4");
        put("5", "f5");
        put("6", "f6");
        put("7", "f7");
        put("8", "f8");
        put("9", "f9");
        put("10", "f10");
        put("11", "f11");
        put("12", "f12");
        put("13", "f13");
        put("14", "f14");
        put("15", "f15");
        put("16", "f16");
        put("17", "f17");
        put("18", "f18");
        put("19", "f19");
        put("20", "f20");
        put("21", "f21");
        put("22", "f22");
        put("23", "f23");
        put("24", "f24");
        put("25", "f25");
        put("26", "f26");
        put("27", "f27");
        put("28", "f28");
        put("29", "f29");
        put("30", "f30");
        put("31", "f31"); //FCSR read-write
    }};


    public static final Map<String, Integer> CPU_REG_TO_DECI = new HashMap<String, Integer>() {{
        put("zero", 0);
        put("at", 1);
        put("v0", 2);
        put("v1", 3);
        put("a0", 4);
        put("a1", 5);
        put("a2", 6);
        put("a3", 7);
        put("t0", 8);
        put("t1", 9);
        put("t2", 10);
        put("t3", 11);
        put("t4", 12);
        put("t5", 13);
        put("t6", 14);
        put("t7", 15);
        put("s0", 16);
        put("s1", 17);
        put("s2", 18);
        put("s3", 19);
        put("s4", 20);
        put("s5", 21);
        put("s6", 22);
        put("s7", 23);
        put("t8", 24);
        put("t9", 25);
        put("k0", 26);
        put("k1", 27);
        put("gp", 28);
        put("sp", 29);
        put("fp", 30);
        put("ra", 31);
    }};

    public static final Map<String, Integer> FPU_REG_TO_DECI = new HashMap<String, Integer>() {{
        put("f0", 0); //FIR readonly
        put("f1", 1);
        put("f2", 2);
        put("f3", 3);
        put("f4", 4);
        put("f5", 5);
        put("f6", 6);
        put("f7", 7);
        put("f8", 8);
        put("f9", 9);
        put("f10", 10);
        put("f11", 11);
        put("f12", 12);
        put("f13", 13);
        put("f14", 14);
        put("f15", 15);
        put("f16", 16);
        put("f17", 17);
        put("f18", 18);
        put("f19", 19);
        put("f20", 20);
        put("f21", 21);
        put("f22", 22);
        put("f23", 23);
        put("f24", 24);
        put("f25", 25);
        put("f26", 26);
        put("f27", 27);
        put("f28", 28);
        put("f29", 29);
        put("f30", 30);
        put("f31", 31); //FCSR read-write
    }};


    public static final Set<String> CPU_REG = new HashSet<>(DECI_TO_CPU_REG.values());

    public static final Set<String> FPU_REG = new HashSet<>(DECI_TO_FPU_REG.values());

    public static final Set<String> CPU_OPCODES = CpuOpcode.CPU_OPCODES;

    public static final Set<String> FPU_OPCODES = FpuOpcode.FPU_OPCODES;

    private LexerState state = LEX_START;

    private char[] source;

    private int sourcePos = 0;

    private int tokenPos = 0;

    private final List<Token> tokens = new ArrayList<>(100);

    private int lineNumber = 1;

    public int getLineNumber() {
        return lineNumber;
    }

    private boolean isLiteral(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '$'
                || c == ',' || c == ':' || c == '(' || c == ')';
    }

    private boolean isDelimiter(char c) {
        return c == ' ';
    }

    @Nullable
    private Token buildToken(String value) {
        switch (state) {
            default:
            case LEX_START:
                return null; // This must not happen
            case LEX_FLOATING_POINT:
                state = LEX_START;
                return Token.builder()
                        .tokenType(TokenType.FLOATING_POINT)
                        .value(value)
                        .line(lineNumber)
                        .pos(sourcePos - value.length())
                        .build();
            case LEX_OCTAL:
                state = LEX_START;
                return Token.builder()
                        .tokenType(TokenType.OCTAL)
                        .value(value)
                        .line(lineNumber)
                        .pos(sourcePos - value.length())
                        .build();
            case LEX_DECI:
                state = LEX_START;
                return Token.builder()
                        .tokenType(TokenType.DECI)
                        .value(value)
                        .line(lineNumber)
                        .pos(sourcePos - value.length())
                        .build();
            case LEX_HEX:
                state = LEX_START;
                return Token.builder()
                        .tokenType(TokenType.HEX)
                        .value(value)
                        .line(lineNumber)
                        .pos(sourcePos - value.length())
                        .build();
            case LEX_IDENTIFIER:
                state = LEX_START;
                if (!tokens.isEmpty()) {
                    if (RESERVED.get(value) != null && tokens.get(tokens.size() - 1).getTokenType() == TokenType.DOT) {
                        return Token.builder()
                                .tokenType(RESERVED.get(value))
                                .value(value)
                                .line(lineNumber)
                                .pos(sourcePos - value.length())
                                .build();
                    }
                    if (CPU_REG.contains(value) && tokens.get(tokens.size() - 1).getTokenType() == TokenType.DOLLAR_SIGN) {
                        return Token.builder()
                                .tokenType(TokenType.REG)
                                .value(value)
                                .line(lineNumber)
                                .pos(sourcePos - value.length() - 1)
                                .build();
                    }

                    if (FPU_REG.contains(value) && tokens.get(tokens.size() - 1).getTokenType() == TokenType.DOLLAR_SIGN) {
                        return Token.builder()
                                .tokenType(TokenType.REG)
                                .value(value)
                                .line(lineNumber)
                                .pos(sourcePos - value.length() - 1)
                                .build();
                    }
                }
                if (CPU_OPCODES.contains(value)) {
                    return Token.builder()
                            .tokenType(TokenType.CPU_OPCODE)
                            .value(value)
                            .line(lineNumber)
                            .pos(sourcePos - value.length())
                            .build();
                }
                if (FPU_OPCODES.contains(value)) {
                    return Token.builder()
                            .tokenType(TokenType.FPU_OPCODE)
                            .value(value)
                            .line(lineNumber)
                            .pos(sourcePos - value.length())
                            .build();
                }
                return Token.builder()
                        .tokenType(TokenType.ID)
                        .value(value)
                        .line(lineNumber)
                        .pos(sourcePos - value.length())
                        .build();
        }
    }

    public void tokenize(char[] source) {
        tokens.clear();
        sourcePos = 0;
        tokenPos = 0;
        state = LEX_START;
        lineNumber = 1;

        this.source = new char[source.length + 1];
        System.arraycopy(source, 0, this.source, 0, source.length);
        this.source[source.length] = 0;
        Token nextToken;
        while ((nextToken = _getNextToken()).getTokenType() != TokenType.EOF) {
            tokens.add(nextToken);
        }
        tokens.add(nextToken);
    }

    public Token getNextToken() {
        if (hasNextToken()) {
            return tokens.get(tokenPos++);
        } else {
            return tokens.get(tokens.size() - 1);
        }
    }

    private Token _getNextToken() {
        StringBuilder stringBuilder = new StringBuilder();
        while (true) {
            if (sourcePos == source.length) {
                return Token.builder()
                        .tokenType(TokenType.EOF)
                        .value(null)
                        .line(lineNumber)
                        .build();
            }
            char c = source[sourcePos++];
            if (c == '\n' || c == 0 || (Character.isWhitespace(c) && state != LEX_STRING && state != LEX_COMMENT)) {
                if (state == LEX_COMMENT || state == LEX_START) {
                    stringBuilder.delete(0, stringBuilder.length());
                    if (c == '\n') {
                        ++lineNumber;
                    }
                    state = LEX_START;
                    continue;
                }
                Token token = buildToken(stringBuilder.toString());
                if (c == '\n') {
                    ++lineNumber;
                }
                return token;
            }

            if (c > 126 && state != LEX_STRING && state != LEX_COMMENT) {
                continue;
            }

            stringBuilder.append(c);
            if (COMMENT.matcher(stringBuilder.toString()).matches()) {
                state = LEX_COMMENT;
            } else if (FLOATING_POINT.matcher(stringBuilder.toString()).matches()) {
                state = LEX_FLOATING_POINT;
            } else if (OCTAL.matcher(stringBuilder.toString()).matches()) {
                state = LEX_OCTAL;
            } else if (DECI.matcher(stringBuilder.toString()).matches()) {
                state = LEX_DECI;
            } else if (HEX.matcher(stringBuilder.toString()).matches()) {
                state = LEX_HEX;
            } else if (ID.matcher(stringBuilder.toString()).matches()) {
                state = LEX_IDENTIFIER;
            } else if (STRING.matcher(stringBuilder.toString()).matches()) {
                state = LEX_START;
                return Token.builder()
                        .tokenType(TokenType.STRING)
                        .value(stringBuilder.toString())
                        .line(lineNumber)
                        .pos(sourcePos - stringBuilder.length())
                        .build();
            } else if (state != LEX_STRING && state != LEX_COMMENT) {
                switch (c) {
                    case '.':
                        state = LEX_START;
                        return Token.builder()
                                .tokenType(TokenType.DOT)
                                .value(".")
                                .line(lineNumber)
                                .pos(sourcePos - 1)
                                .build();
                    case '+':
                        state = LEX_START;
                        return Token.builder()
                                .tokenType(TokenType.PLUS)
                                .value("+")
                                .line(lineNumber)
                                .pos(sourcePos - 1)
                                .build();
                    case '-':
                        state = LEX_START;
                        return Token.builder()
                                .tokenType(TokenType.MINUS)
                                .value("-")
                                .line(lineNumber)
                                .pos(sourcePos - 1)
                                .build();
                    case '*':
                        state = LEX_START;
                        return Token.builder()
                                .tokenType(TokenType.TIMES)
                                .value("*")
                                .line(lineNumber)
                                .pos(sourcePos - 1)
                                .build();
                    case '/':
                        state = LEX_START;
                        return Token.builder()
                                .tokenType(TokenType.DIV)
                                .value("/")
                                .line(lineNumber)
                                .pos(sourcePos - 1)
                                .build();
                    case '$':
                        state = LEX_START;
                        return Token.builder()
                                .tokenType(TokenType.DOLLAR_SIGN)
                                .value("$")
                                .line(lineNumber)
                                .pos(sourcePos - 1)
                                .build();
                    case ',':
                        state = LEX_START;
                        return Token.builder()
                                .tokenType(TokenType.COMMA)
                                .value(",")
                                .line(lineNumber)
                                .pos(sourcePos - 1)
                                .build();
                    case ':':
                        state = LEX_START;
                        return Token.builder()
                                .tokenType(TokenType.COLON)
                                .value(":")
                                .line(lineNumber)
                                .pos(sourcePos - 1)
                                .build();
                    case '(':
                        state = LEX_START;
                        return Token.builder()
                                .tokenType(TokenType.L_PAREN)
                                .value("(")
                                .line(lineNumber)
                                .pos(sourcePos - 1)
                                .build();
                    case ')':
                        state = LEX_START;
                        return Token.builder()
                                .tokenType(TokenType.R_PAREN)
                                .value(")")
                                .line(lineNumber)
                                .pos(sourcePos - 1)
                                .build();
                    case '"':
                        state = LEX_STRING;
                }
            } else {
                if (state != LEX_STRING) {
                    state = LEX_START;
                }
            }
            if ((isLiteral(source[sourcePos]) || isDelimiter(source[sourcePos])) && state != LEX_STRING && state != LEX_COMMENT) {
                return buildToken(stringBuilder.toString());
            }
        }
    }

    public int getTokenPos() {
        return tokenPos;
    }

    public void reset(int pos) {
        tokenPos = pos;
    }

    public static boolean isRegister(String token) {
        return CPU_REG.contains(token) || FPU_REG.contains(token);
    }

    public static String registerNumberToName(String number) {
        if (DECI_TO_CPU_REG.containsKey(number)) {
            return DECI_TO_CPU_REG.get(number);
        }
        return DECI_TO_FPU_REG.get(number);
    }

    public boolean hasNextToken() {
        return tokenPos < tokens.size();
    }
}
