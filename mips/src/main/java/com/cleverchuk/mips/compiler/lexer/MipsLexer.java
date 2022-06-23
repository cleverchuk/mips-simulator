package com.cleverchuk.mips.compiler.lexer;

import androidx.annotation.Nullable;
import com.cleverchuk.mips.simulator.Opcode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.inject.Inject;

public final class MipsLexer {
    private static final Pattern ID = Pattern.compile("[A-Za-z][A-Za-z0-9]*");

    private static final Pattern DECI = Pattern.compile("[0-9][0-9]*");

    private static final Pattern FLOAT = Pattern.compile("[0-9]+\\.[0-9]*");

    private static final Pattern HEX = Pattern.compile("0[xX][a-f0-9]+");

    private static final Pattern OCTAL = Pattern.compile("0[0-7]+");

    private static final Pattern STRING = Pattern.compile("^\".*\"$");

    private static final Pattern COMMENT = Pattern.compile("#.*");

    @Inject
    public MipsLexer() {
    }

    private static final Map<String, TokenType> RESERVED = new HashMap<String, TokenType>() {{
        put("data", TokenType.DATA);
        put("text", TokenType.TEXT);
        put("ascii", TokenType.ASCII);
        put("asciiz", TokenType.ASCIIZ);
        put("space", TokenType.SPACESTORAGE);
        put("byte", TokenType.BYTESTORAGE);
        put("half", TokenType.HALFSTORAGE);
        put("word", TokenType.WORDSTORAGE);
        put("float", TokenType.FLOATSTORAGE);
        put("double", TokenType.DOUBLESTORAGE);
        put("globl", TokenType.GLOBL);

    }};


    public static final Map<String, String> DECI_TO_REG = new HashMap<String, String>() {{
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


    public static final Set<String> REG = new HashSet<>(DECI_TO_REG.values());


    public static final Set<String> OPCODE = Opcode.OPCODE;

    private int state = 100;

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
                || c == ',' || c == ':' || c == '.' || c == '(' || c == ')';
    }

    @Nullable
    private Token buildToken(String value) {
        switch (state) {
            default:
            case 100:
                return null; // This must not happen
            case 1:
                state = 100;
                return Token.builder()
                        .tokenType(TokenType.FLOAT)
                        .value(value)
                        .line(lineNumber)
                        .pos(sourcePos - value.length())
                        .build();
            case 2:
                state = 100;
                return Token.builder()
                        .tokenType(TokenType.OCTAL)
                        .value(value)
                        .line(lineNumber)
                        .pos(sourcePos - value.length())
                        .build();
            case 3:
                state = 100;
                return Token.builder()
                        .tokenType(TokenType.DECI)
                        .value(value)
                        .line(lineNumber)
                        .pos(sourcePos - value.length())
                        .build();
            case 4:
                state = 100;
                return Token.builder()
                        .tokenType(TokenType.HEX)
                        .value(value)
                        .line(lineNumber)
                        .pos(sourcePos - value.length())
                        .build();
            case 5:
                state = 100;
                if (RESERVED.get(value) != null) {
                    return Token.builder()
                            .tokenType(RESERVED.get(value))
                            .value(value)
                            .line(lineNumber)
                            .pos(sourcePos - value.length())
                            .build();
                }
                if (REG.contains(value)) {
                    return Token.builder()
                            .tokenType(TokenType.REG)
                            .value(value)
                            .line(lineNumber)
                            .pos(sourcePos - value.length() - 1)
                            .build();
                }
                if (OPCODE.contains(value)) {
                    return Token.builder()
                            .tokenType(TokenType.IOPCODE)
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
        state = 100;
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
                return new Token(TokenType.EOF, null, lineNumber);
            }
            char c = source[sourcePos++];
            if (c == '\n' || c == 0 || (Character.isWhitespace(c) && state != 6 && state != 0)) {
                if (state == 0 || state == 100) {
                    stringBuilder.delete(0, stringBuilder.length());
                    if (c == '\n') {
                        ++lineNumber;
                    }
                    state = 100;
                    continue;
                }
                Token token = buildToken(stringBuilder.toString());
                if (c == '\n') {
                    ++lineNumber;
                }
                return token;
            }

            if (c > 126 && state != 6 && state != 0) {
                continue;
            }

            stringBuilder.append(c);
            if (COMMENT.matcher(stringBuilder.toString()).matches()) {
                state = 0;
            } else if (FLOAT.matcher(stringBuilder.toString()).matches()) {
                state = 1;
            } else if (OCTAL.matcher(stringBuilder.toString()).matches()) {
                state = 2;
            } else if (DECI.matcher(stringBuilder.toString()).matches()) {
                state = 3;
            } else if (HEX.matcher(stringBuilder.toString()).matches()) {
                state = 4;
            } else if (ID.matcher(stringBuilder.toString()).matches()) {
                state = 5;
            } else if (STRING.matcher(stringBuilder.toString()).matches()) {
                state = 100;
                return Token.builder()
                        .tokenType(TokenType.STRING)
                        .value(stringBuilder.toString())
                        .line(lineNumber)
                        .pos(sourcePos - stringBuilder.length())
                        .build();
            } else if (state != 6 && state != 0) {
                switch (c) {
                    case '.':
                        state = 100;
                        return Token.builder()
                                .tokenType(TokenType.DOT)
                                .value(".")
                                .line(lineNumber)
                                .pos(sourcePos - 1)
                                .build();
                    case '+':
                        state = 100;
                        return Token.builder()
                                .tokenType(TokenType.PLUS)
                                .value("+")
                                .line(lineNumber)
                                .pos(sourcePos - 1)
                                .build();
                    case '-':
                        state = 100;
                        return Token.builder()
                                .tokenType(TokenType.MINUS)
                                .value("-")
                                .line(lineNumber)
                                .pos(sourcePos - 1)
                                .build();
                    case '*':
                        state = 100;
                        return Token.builder()
                                .tokenType(TokenType.TIMES)
                                .value("*")
                                .line(lineNumber)
                                .pos(sourcePos - 1)
                                .build();
                    case '/':
                        state = 100;
                        return Token.builder()
                                .tokenType(TokenType.DIV)
                                .value("/")
                                .line(lineNumber)
                                .pos(sourcePos - 1)
                                .build();
                    case '$':
                        state = 100;
                        return Token.builder()
                                .tokenType(TokenType.DOLLARSIGN)
                                .value("$")
                                .line(lineNumber)
                                .pos(sourcePos - 1)
                                .build();
                    case ',':
                        state = 100;
                        return Token.builder()
                                .tokenType(TokenType.COMMA)
                                .value(",")
                                .line(lineNumber)
                                .pos(sourcePos - 1)
                                .build();
                    case ':':
                        state = 100;
                        return Token.builder()
                                .tokenType(TokenType.COLON)
                                .value(":")
                                .line(lineNumber)
                                .pos(sourcePos - 1)
                                .build();
                    case '(':
                        state = 100;
                        return Token.builder()
                                .tokenType(TokenType.LPAREN)
                                .value("(")
                                .line(lineNumber)
                                .pos(sourcePos - 1)
                                .build();
                    case ')':
                        state = 100;
                        return Token.builder()
                                .tokenType(TokenType.RPAREN)
                                .value(")")
                                .line(lineNumber)
                                .pos(sourcePos - 1)
                                .build();
                    case '"':
                        state = 6;
                }
            } else {
                if (state != 6) {
                    state = 100;
                }
            }
            if (isLiteral(source[sourcePos]) && state != 6 && state != 0) {
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
        return REG.contains(token);
    }

    public boolean hasNextToken() {
        return tokenPos < tokens.size();
    }
}
