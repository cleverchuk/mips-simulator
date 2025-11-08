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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

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

    Token[] expectedTokens =
        new Token[] {
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
    String source =
        ".data\nlabel: .asciiz \"Hello World!\"\nlabel0:.ascii \"Hello World\"\nints: .word 2,3,4";
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
    String source =
        ".data\n"
            + "dummy0: .space 5\n"
            + "dummy1: .byte 10\n"
            + "dummy2: .half 10\n"
            + "dummy3: .word 5\n"
            + "dummy4: .float 50\n"
            + "dummy5: .double 60\n";
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
    Token[] expectedTokens =
        new Token[] {
          new Token(TokenType.DOT, ".", 1),
          new Token(TokenType.DATA, "data", 1),
          new Token(TokenType.ID, "dummy0", 2),
          new Token(TokenType.COLON, ":", 2),
          new Token(TokenType.DOT, ".", 2),
          new Token(TokenType.SPACE_STORAGE, "space", 2),
          new Token(TokenType.DECI, "5", 2),
          new Token(TokenType.ID, "dummy1", 3),
          new Token(TokenType.COLON, ":", 3),
          new Token(TokenType.DOT, ".", 3),
          new Token(TokenType.BYTE_STORAGE, "byte", 3),
          new Token(TokenType.DECI, "10", 3),
          new Token(TokenType.ID, "dummy2", 4),
          new Token(TokenType.COLON, ":", 4),
          new Token(TokenType.DOT, ".", 4),
          new Token(TokenType.HALF_STORAGE, "half", 4),
          new Token(TokenType.DECI, "10", 4),
          new Token(TokenType.ID, "dummy3", 5),
          new Token(TokenType.COLON, ":", 5),
          new Token(TokenType.DOT, ".", 5),
          new Token(TokenType.WORD_STORAGE, "word", 5),
          new Token(TokenType.DECI, "5", 5),
          new Token(TokenType.ID, "dummy4", 6),
          new Token(TokenType.COLON, ":", 6),
          new Token(TokenType.DOT, ".", 6),
          new Token(TokenType.FLOAT_STORAGE, "float", 6),
          new Token(TokenType.DECI, "50", 6),
          new Token(TokenType.ID, "dummy5", 7),
          new Token(TokenType.COLON, ":", 7),
          new Token(TokenType.DOT, ".", 7),
          new Token(TokenType.DOUBLE_STORAGE, "double", 7),
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
    String source =
        ".text\n"
            + "add $t0, $t1, $t2 # comment\n"
            + "# hello no op\n"
            + "addi $t0, $t1, 400\n"
            + "beq $t0, $t1, label\n"
            + "lw $t0, 2($t1   )\n"
            + "sw $t0, 67 (   $sp )\n"
            + "li $t0, 300\n"
            + "la $t0, label # comment\n"
            + "jal label\n"
            + "return:jr $ra\n"
            + "addi $t0, $zero, 300\n"
            + "add $t0, $t1,             $zero\n"
            + "li $v0,                       1\n"
            + "syscall\n"
            + "             \n"
            + "nop\n";
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
    Token[] expectedTokens =
        new Token[] {
          new Token(TokenType.DOT, ".", 1),
          new Token(TokenType.TEXT, "text", 1),
          new Token(TokenType.OPCODE, "add", 2),
          new Token(TokenType.DOLLAR_SIGN, "$", 2),
          new Token(TokenType.REG, "t0", 2),
          new Token(TokenType.COMMA, ",", 2),
          new Token(TokenType.DOLLAR_SIGN, "$", 2),
          new Token(TokenType.REG, "t1", 2),
          new Token(TokenType.COMMA, ",", 2),
          new Token(TokenType.DOLLAR_SIGN, "$", 2),
          new Token(TokenType.REG, "t2", 2),
          new Token(TokenType.OPCODE, "addi", 4),
          new Token(TokenType.DOLLAR_SIGN, "$", 4),
          new Token(TokenType.REG, "t0", 4),
          new Token(TokenType.COMMA, ",", 4),
          new Token(TokenType.DOLLAR_SIGN, "$", 4),
          new Token(TokenType.REG, "t1", 4),
          new Token(TokenType.COMMA, ",", 4),
          new Token(TokenType.DECI, "400", 4),
          new Token(TokenType.OPCODE, "beq", 5),
          new Token(TokenType.DOLLAR_SIGN, "$", 5),
          new Token(TokenType.REG, "t0", 5),
          new Token(TokenType.COMMA, ",", 5),
          new Token(TokenType.DOLLAR_SIGN, "$", 5),
          new Token(TokenType.REG, "t1", 5),
          new Token(TokenType.COMMA, ",", 5),
          new Token(TokenType.ID, "label", 5),
          new Token(TokenType.OPCODE, "lw", 6),
          new Token(TokenType.DOLLAR_SIGN, "$", 6),
          new Token(TokenType.REG, "t0", 6),
          new Token(TokenType.COMMA, ",", 6),
          new Token(TokenType.DECI, "2", 6),
          new Token(TokenType.L_PAREN, "(", 6),
          new Token(TokenType.DOLLAR_SIGN, "$", 6),
          new Token(TokenType.REG, "t1", 6),
          new Token(TokenType.R_PAREN, ")", 6),
          new Token(TokenType.OPCODE, "sw", 7),
          new Token(TokenType.DOLLAR_SIGN, "$", 7),
          new Token(TokenType.REG, "t0", 7),
          new Token(TokenType.COMMA, ",", 7),
          new Token(TokenType.DECI, "67", 7),
          new Token(TokenType.L_PAREN, "(", 7),
          new Token(TokenType.DOLLAR_SIGN, "$", 7),
          new Token(TokenType.REG, "sp", 7),
          new Token(TokenType.R_PAREN, ")", 7),
          new Token(TokenType.OPCODE, "li", 8),
          new Token(TokenType.DOLLAR_SIGN, "$", 8),
          new Token(TokenType.REG, "t0", 8),
          new Token(TokenType.COMMA, ",", 8),
          new Token(TokenType.DECI, "300", 8),
          new Token(TokenType.OPCODE, "la", 9),
          new Token(TokenType.DOLLAR_SIGN, "$", 9),
          new Token(TokenType.REG, "t0", 9),
          new Token(TokenType.COMMA, ",", 9),
          new Token(TokenType.ID, "label", 9),
          new Token(TokenType.OPCODE, "jal", 10),
          new Token(TokenType.ID, "label", 10),
          new Token(TokenType.ID, "return", 11),
          new Token(TokenType.COLON, ":", 11),
          new Token(TokenType.OPCODE, "jr", 11),
          new Token(TokenType.DOLLAR_SIGN, "$", 11),
          new Token(TokenType.REG, "ra", 11),
          new Token(TokenType.OPCODE, "addi", 12),
          new Token(TokenType.DOLLAR_SIGN, "$", 12),
          new Token(TokenType.REG, "t0", 12),
          new Token(TokenType.COMMA, ",", 12),
          new Token(TokenType.DOLLAR_SIGN, "$", 12),
          new Token(TokenType.REG, "zero", 12),
          new Token(TokenType.COMMA, ",", 12),
          new Token(TokenType.DECI, "300", 12),
          new Token(TokenType.OPCODE, "add", 13),
          new Token(TokenType.DOLLAR_SIGN, "$", 13),
          new Token(TokenType.REG, "t0", 13),
          new Token(TokenType.COMMA, ",", 13),
          new Token(TokenType.DOLLAR_SIGN, "$", 13),
          new Token(TokenType.REG, "t1", 13),
          new Token(TokenType.COMMA, ",", 13),
          new Token(TokenType.DOLLAR_SIGN, "$", 13),
          new Token(TokenType.REG, "zero", 13),
          new Token(TokenType.OPCODE, "li", 14),
          new Token(TokenType.DOLLAR_SIGN, "$", 14),
          new Token(TokenType.REG, "v0", 14),
          new Token(TokenType.COMMA, ",", 14),
          new Token(TokenType.DECI, "1", 14),
          new Token(TokenType.OPCODE, "syscall", 15),
          new Token(TokenType.OPCODE, "nop", 17),
        };

    for (int i = 0; i < expectedTokens.length; i++) {
      assertEquals(expectedTokens[i].getTokenType(), actualTokens.get(i).getTokenType());
      assertEquals(expectedTokens[i].getValue(), actualTokens.get(i).getValue());
      assertEquals(expectedTokens[i].getLine(), actualTokens.get(i).getLine());
    }
  }
}
