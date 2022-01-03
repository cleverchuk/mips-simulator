package com.cleverchuk.mips.compiler.lexer;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MipsLexerTest {
    @Test
    public void lexStringWithDelimiter() {
        String source = ".data\nlabel: .asciiz \"Hello : World! ) ( . \"\n";
        MipsLexer lexer = new MipsLexer();
        lexer.tokenize(source.toCharArray());
        int count = 0;
        List<Token> actualTokens = new ArrayList<>();
        while (true) {
            count++;
            Token nextToken = lexer.getNextToken();
            actualTokens.add(nextToken);
            if (nextToken != null && nextToken.getTokenType() == TokenType.EOF) {
                break;
            }
        }
        assertEquals(count, 8);

        Token[] expectedTokens = new Token[]{
                new Token(TokenType.DOT, ".", 1),
                new Token(TokenType.DATA, "data", 1),
                new Token(TokenType.ID, "label", 2),
                new Token(TokenType.COLON, ":", 2),
                new Token(TokenType.DOT, ".", 2),
                new Token(TokenType.ASCIIZ, "asciiz", 2),
                new Token(TokenType.STRING, "\"Hello : World! ) ( . \"", 2),
                new Token(TokenType.EOF, null, 3),
        };

        for (int i = 0; i < count; i++) {
            assertEquals(expectedTokens[i].getTokenType(), actualTokens.get(i).getTokenType());
            assertEquals(expectedTokens[i].getValue(), actualTokens.get(i).getValue());
            assertEquals(expectedTokens[i].getLine(), actualTokens.get(i).getLine());
        }
    }

    @Test
    public void getNextToken() {
        String source = ".data\nlabel: .asciiz \"Hello World!\"\nlabel0:.ascii \"Hello World\"\nints: .word 2,3,4";
        MipsLexer lexer = new MipsLexer();
        lexer.tokenize(source.toCharArray());
        int count = 0;
        while (true) {
            count++;
            Token nextToken = lexer.getNextToken();
            if (nextToken != null && nextToken.getTokenType() == TokenType.EOF) {
                break;
            }
        }
        assertEquals(count, 22);
    }

    @Test
    public void getNextToken0() {
        String source = ".data\n" +
                "dummy0: .space 5\n" +
                "dummy1: .byte 10\n" +
                "dummy2: .half 10\n" +
                "dummy3: .word 5\n" +
                "dummy4: .float 50\n" +
                "dummy5: .double 60\n";
        MipsLexer lexer = new MipsLexer();
        lexer.tokenize(source.toCharArray());
        int count = 0;
        List<Token> actualTokens = new ArrayList<>();
        while (true) {
            count++;
            Token nextToken = lexer.getNextToken();
            actualTokens.add(nextToken);
            if (nextToken != null && nextToken.getTokenType() == TokenType.EOF) {
                break;
            }
        }
        Token[] expectedTokens = new Token[]{
                new Token(TokenType.DOT, ".", 1),
                new Token(TokenType.DATA, "data", 1),
                new Token(TokenType.ID, "dummy0", 2),
                new Token(TokenType.COLON, ":", 2),
                new Token(TokenType.DOT, ".", 2),
                new Token(TokenType.SPACESTORAGE, "space", 2),
                new Token(TokenType.DECI, "5", 2),
                new Token(TokenType.ID, "dummy1", 3),
                new Token(TokenType.COLON, ":", 3),
                new Token(TokenType.DOT, ".", 3),
                new Token(TokenType.BYTESTORAGE, "byte", 3),
                new Token(TokenType.DECI, "10", 3),
                new Token(TokenType.ID, "dummy2", 4),
                new Token(TokenType.COLON, ":", 4),
                new Token(TokenType.DOT, ".", 4),
                new Token(TokenType.HALFSTORAGE, "half", 4),
                new Token(TokenType.DECI, "10", 4),
                new Token(TokenType.ID, "dummy3", 5),
                new Token(TokenType.COLON, ":", 5),
                new Token(TokenType.DOT, ".", 5),
                new Token(TokenType.WORDSTORAGE, "word", 5),
                new Token(TokenType.DECI, "5", 5),
                new Token(TokenType.ID, "dummy4", 6),
                new Token(TokenType.COLON, ":", 6),
                new Token(TokenType.DOT, ".", 6),
                new Token(TokenType.FLOATSTORAGE, "float", 6),
                new Token(TokenType.DECI, "50", 6),
                new Token(TokenType.ID, "dummy5", 7),
                new Token(TokenType.COLON, ":", 7),
                new Token(TokenType.DOT, ".", 7),
                new Token(TokenType.DOUBLESTORAGE, "double", 7),
                new Token(TokenType.DECI, "60", 7),
                new Token(TokenType.EOF, null, 8),
        };
        assertEquals(expectedTokens.length, count);

        for (int i = 0; i < count; i++) {
            assertEquals(expectedTokens[i].getTokenType(), actualTokens.get(i).getTokenType());
            assertEquals(expectedTokens[i].getValue(), actualTokens.get(i).getValue());
            assertEquals(expectedTokens[i].getLine(), actualTokens.get(i).getLine());
        }
    }

    @Test
    public void getNextToken1() {
        String source = ".text\n" +
                "add $t0, $t1, $t2 # comment\n" +
                "# hello no op\n" +
                "addi $t0, $t1, 400\n" +
                "beq $t0, $t1, label\n" +
                "lw $t0, 2($t1   )\n" +
                "sw $t0, 67 (   $sp )\n" +
                "li $t0, 300\n" +
                "la $t0, label # comment\n" +
                "jal label\n" +
                "return:jr $ra\n" +
                "addi $t0, $zero, 300\n" +
                "add $t0, $t1,             $zero\n" +
                "li $v0,                       1\n" +
                "syscall\n" +
                "             \n" +
                "nop\n";
        MipsLexer lexer = new MipsLexer();
        lexer.tokenize(source.toCharArray());
        List<Token> actualTokens = new ArrayList<>();
        while (true) {
            Token nextToken = lexer.getNextToken();
            actualTokens.add(nextToken);
            if (nextToken != null && nextToken.getTokenType() == TokenType.EOF) {
                break;
            }
        }
        Token[] expectedTokens = new Token[]{
                new Token(TokenType.DOT, ".", 1),
                new Token(TokenType.TEXT, "text", 1),
                new Token(TokenType.IOPCODE, "add", 2),
                new Token(TokenType.DOLLARSIGN, "$", 2),
                new Token(TokenType.REG, "t0", 2),
                new Token(TokenType.COMMA, ",", 2),
                new Token(TokenType.DOLLARSIGN, "$", 2),
                new Token(TokenType.REG, "t1", 2),
                new Token(TokenType.COMMA, ",", 2),
                new Token(TokenType.DOLLARSIGN, "$", 2),
                new Token(TokenType.REG, "t2", 2),
                new Token(TokenType.IOPCODE, "addi", 4),
                new Token(TokenType.DOLLARSIGN, "$", 4),
                new Token(TokenType.REG, "t0", 4),
                new Token(TokenType.COMMA, ",", 4),
                new Token(TokenType.DOLLARSIGN, "$", 4),
                new Token(TokenType.REG, "t1", 4),
                new Token(TokenType.COMMA, ",", 4),
                new Token(TokenType.DECI, "400", 4),
                new Token(TokenType.IOPCODE, "beq", 5),
                new Token(TokenType.DOLLARSIGN, "$", 5),
                new Token(TokenType.REG, "t0", 5),
                new Token(TokenType.COMMA, ",", 5),
                new Token(TokenType.DOLLARSIGN, "$", 5),
                new Token(TokenType.REG, "t1", 5),
                new Token(TokenType.COMMA, ",", 5),
                new Token(TokenType.ID, "label", 5),
                new Token(TokenType.IOPCODE, "lw", 6),
                new Token(TokenType.DOLLARSIGN, "$", 6),
                new Token(TokenType.REG, "t0", 6),
                new Token(TokenType.COMMA, ",", 6),
                new Token(TokenType.DECI, "2", 6),
                new Token(TokenType.LPAREN, "(", 6),
                new Token(TokenType.DOLLARSIGN, "$", 6),
                new Token(TokenType.REG, "t1", 6),
                new Token(TokenType.RPAREN, ")", 6),
                new Token(TokenType.IOPCODE, "sw", 7),
                new Token(TokenType.DOLLARSIGN, "$", 7),
                new Token(TokenType.REG, "t0", 7),
                new Token(TokenType.COMMA, ",", 7),
                new Token(TokenType.DECI, "67", 7),
                new Token(TokenType.LPAREN, "(", 7),
                new Token(TokenType.DOLLARSIGN, "$", 7),
                new Token(TokenType.REG, "sp", 7),
                new Token(TokenType.RPAREN, ")", 7),
                new Token(TokenType.IOPCODE, "li", 8),
                new Token(TokenType.DOLLARSIGN, "$", 8),
                new Token(TokenType.REG, "t0", 8),
                new Token(TokenType.COMMA, ",", 8),
                new Token(TokenType.DECI, "300", 8),
                new Token(TokenType.IOPCODE, "la", 9),
                new Token(TokenType.DOLLARSIGN, "$", 9),
                new Token(TokenType.REG, "t0", 9),
                new Token(TokenType.COMMA, ",", 9),
                new Token(TokenType.ID, "label", 9),
                new Token(TokenType.IOPCODE, "jal", 10),
                new Token(TokenType.ID, "label", 10),
                new Token(TokenType.ID, "return", 11),
                new Token(TokenType.COLON, ":", 11),
                new Token(TokenType.IOPCODE, "jr", 11),
                new Token(TokenType.DOLLARSIGN, "$", 11),
                new Token(TokenType.REG, "ra", 11),
                new Token(TokenType.IOPCODE, "addi", 12),
                new Token(TokenType.DOLLARSIGN, "$", 12),
                new Token(TokenType.REG, "t0", 12),
                new Token(TokenType.COMMA, ",", 12),
                new Token(TokenType.DOLLARSIGN, "$", 12),
                new Token(TokenType.REG, "zero", 12),
                new Token(TokenType.COMMA, ",", 12),
                new Token(TokenType.DECI, "300", 12),
                new Token(TokenType.IOPCODE, "add", 13),
                new Token(TokenType.DOLLARSIGN, "$", 13),
                new Token(TokenType.REG, "t0", 13),
                new Token(TokenType.COMMA, ",", 13),
                new Token(TokenType.DOLLARSIGN, "$", 13),
                new Token(TokenType.REG, "t1", 13),
                new Token(TokenType.COMMA, ",", 13),
                new Token(TokenType.DOLLARSIGN, "$", 13),
                new Token(TokenType.REG, "zero", 13),
                new Token(TokenType.IOPCODE, "li", 14),
                new Token(TokenType.DOLLARSIGN, "$", 14),
                new Token(TokenType.REG, "v0", 14),
                new Token(TokenType.COMMA, ",", 14),
                new Token(TokenType.DECI, "1", 14),
                new Token(TokenType.IOPCODE, "syscall", 15),
                new Token(TokenType.IOPCODE, "nop", 17),
        };

        for (int i = 0; i < expectedTokens.length; i++) {
            assertEquals(expectedTokens[i].getTokenType(), actualTokens.get(i).getTokenType());
            assertEquals(expectedTokens[i].getValue(), actualTokens.get(i).getValue());
            assertEquals(expectedTokens[i].getLine(), actualTokens.get(i).getLine());
        }
    }
}